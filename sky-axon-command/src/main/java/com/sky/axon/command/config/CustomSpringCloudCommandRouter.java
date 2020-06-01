/*
 * The MIT License (MIT)
 * Copyright © 2020 <sky>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sky.axon.command.config;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.distributed.*;
import org.axonframework.extensions.springcloud.commandhandling.MessageRoutingInformation;
import org.axonframework.extensions.springcloud.commandhandling.SpringCloudCommandRouter;
import org.axonframework.serialization.SerializedObject;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.SimpleSerializedObject;
import org.axonframework.serialization.xml.XStreamSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.event.EventListener;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.axonframework.common.BuilderUtils.assertNonNull;

/**
 * @author
 */
public class CustomSpringCloudCommandRouter implements CommandRouter {

    private static final Logger logger = LoggerFactory.getLogger(SpringCloudCommandRouter.class);

    private static final String LOAD_FACTOR = "loadFactor";
    private static final String SERIALIZED_COMMAND_FILTER = "serializedCommandFilter";
    private static final String SERIALIZED_COMMAND_FILTER_CLASS_NAME = "serializedCommandFilterClassName";

    private final DiscoveryClient discoveryClient;
    private final Registration localServiceInstance;
    private final RoutingStrategy routingStrategy;
    private final Predicate<ServiceInstance> serviceInstanceFilter;
    private final ConsistentHashChangeListener consistentHashChangeListener;
    private final String contextRootMetadataPropertyname;
    protected final Serializer serializer;

    private final AtomicReference<ConsistentHash> atomicConsistentHash = new AtomicReference<>(new ConsistentHash());
    private final Set<ServiceInstance> blackListedServiceInstances = new HashSet<>();

    private volatile boolean registered = false;

    protected CustomSpringCloudCommandRouter(CustomSpringCloudCommandRouter.Builder builder) {
        builder.validate();
        discoveryClient = builder.discoveryClient;
        localServiceInstance = builder.localServiceInstance;
        routingStrategy = builder.routingStrategy;
        serviceInstanceFilter = builder.serviceInstanceFilter;
        consistentHashChangeListener = builder.consistentHashChangeListener;
        contextRootMetadataPropertyname = builder.contextRootMetadataPropertyname;
        serializer = builder.serializerSupplier.get();
    }


    public static CustomSpringCloudCommandRouter.Builder builder() {
        return new CustomSpringCloudCommandRouter.Builder();
    }


    public static boolean serviceInstanceMetadataContainsMessageRoutingInformation(ServiceInstance serviceInstance) {
        Map<String, String> serviceInstanceMetadata = serviceInstance.getMetadata();
        return serviceInstanceMetadata != null &&
                serviceInstanceMetadata.containsKey(LOAD_FACTOR) &&
                serviceInstanceMetadata.containsKey(SERIALIZED_COMMAND_FILTER) &&
                serviceInstanceMetadata.containsKey(SERIALIZED_COMMAND_FILTER_CLASS_NAME);
    }

    @Override
    public Optional<Member> findDestination(CommandMessage<?> commandMessage) {
        return atomicConsistentHash.get().getMember(routingStrategy.getRoutingKey(commandMessage), commandMessage);
    }

    @Override
    public void updateMembership(int loadFactor, CommandMessageFilter commandFilter) {
        Map<String, String> localServiceInstanceMetadata = localServiceInstance.getMetadata();
        if (localServiceInstanceMetadata != null) {
            localServiceInstanceMetadata.put(LOAD_FACTOR, Integer.toString(loadFactor));
            SerializedObject<String> serializedCommandFilter = serializer.serialize(commandFilter, String.class);
            localServiceInstanceMetadata.put(SERIALIZED_COMMAND_FILTER, serializedCommandFilter.getData());
            localServiceInstanceMetadata.put(
                    SERIALIZED_COMMAND_FILTER_CLASS_NAME, serializedCommandFilter.getType().getName()
            );
        }

        updateMembershipForServiceInstance(localServiceInstance, atomicConsistentHash)
                .ifPresent(consistentHashChangeListener::onConsistentHashChanged);
    }


    @EventListener
    @SuppressWarnings("UnusedParameters")
    public void resetLocalMembership(InstanceRegisteredEvent event) {
        registered = true;

        Optional<Member> startUpPhaseLocalMember =
                atomicConsistentHash.get().getMembers().stream()
                        .filter(Member::local)
                        .findFirst();

        updateMemberships();

        startUpPhaseLocalMember.ifPresent(m -> {
            if (logger.isDebugEnabled()) {
                logger.debug("Resetting local membership for [{}].", m);
            }
            atomicConsistentHash.updateAndGet(consistentHash -> consistentHash.without(m));
        });
    }

    @EventListener
    @SuppressWarnings("UnusedParameters")
    public void updateMemberships(HeartbeatEvent event) {
        updateMemberships();
    }

    private void updateMemberships() {
        try {
            AtomicReference<ConsistentHash> updatedConsistentHash = new AtomicReference<>(new ConsistentHash());

            List<ServiceInstance> instances = discoveryClient.getServices().stream()
                    .map(discoveryClient::getInstances)
                    .flatMap(Collection::stream)
                    .filter(serviceInstanceFilter)
                    .collect(Collectors.toList());

            cleanBlackList(instances);

            instances.stream()
                    .filter(this::ifNotBlackListed)
                    .forEach(serviceInstance -> updateMembershipForServiceInstance(serviceInstance,
                            updatedConsistentHash));

            ConsistentHash newConsistentHash = updatedConsistentHash.get();
            atomicConsistentHash.set(newConsistentHash);
            consistentHashChangeListener.onConsistentHashChanged(newConsistentHash);
        } catch (Exception e) {
            logger.error("===>", e);
        }
    }

    private void cleanBlackList(List<ServiceInstance> instances) {
        blackListedServiceInstances.removeIf(
                blackListedInstance -> instances.stream().noneMatch(instance -> equals(instance, blackListedInstance))
        );
    }

    private boolean ifNotBlackListed(ServiceInstance serviceInstance) {
        return blackListedServiceInstances.stream()
                .noneMatch(blackListedServiceInstance -> equals(serviceInstance,
                        blackListedServiceInstance));
    }


    @SuppressWarnings("SimplifiableIfStatement")
    private boolean equals(ServiceInstance serviceInstance, ServiceInstance blackListedServiceInstance) {
        if (serviceInstance == blackListedServiceInstance) {
            return true;
        }
        if (blackListedServiceInstance == null) {
            return false;
        }
        return Objects.equals(serviceInstance.getServiceId(), blackListedServiceInstance.getServiceId())
                && Objects.equals(serviceInstance.getHost(), blackListedServiceInstance.getHost())
                && Objects.equals(serviceInstance.getPort(), blackListedServiceInstance.getPort());
    }

    private Optional<ConsistentHash> updateMembershipForServiceInstance(ServiceInstance serviceInstance,
                                                                        AtomicReference<ConsistentHash> atomicConsistentHash) {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating membership for service instance: [{}]", serviceInstance);
        }

        Member member = null;
        try {
            member = buildMember(serviceInstance);
        } catch (Exception e) {
            return Optional.empty();
        }


        Optional<MessageRoutingInformation> optionalMessageRoutingInfo = getMessageRoutingInformation(serviceInstance);

        if (optionalMessageRoutingInfo.isPresent()) {
            MessageRoutingInformation messageRoutingInfo = optionalMessageRoutingInfo.get();
            Member finalMember = member;
            return Optional.of(atomicConsistentHash.updateAndGet(
                    consistentHash -> consistentHash.with(finalMember,
                            messageRoutingInfo.getLoadFactor(),
                            messageRoutingInfo.getCommandFilter(serializer))
            ));
        } else {
            logger.info(
                    "Black listed ServiceInstance [{}] under host [{}] and port [{}] since we could not retrieve the "
                            + "required Message Routing Information from it.",
                    serviceInstance.getServiceId(), serviceInstance.getHost(), serviceInstance.getPort()
            );
            blackListedServiceInstances.add(serviceInstance);
        }
        return Optional.empty();
    }

    protected Member buildMember(ServiceInstance serviceInstance) {
        return isLocalServiceInstance(serviceInstance)
                ? buildLocalMember(serviceInstance)
                : buildRemoteMember(serviceInstance);
    }

    private boolean isLocalServiceInstance(ServiceInstance serviceInstance) {
        try {
            return serviceInstance.equals(localServiceInstance)
                    || Objects.equals(serviceInstance.getUri(), localServiceInstance.getUri());
        } catch (Exception e) {
            // logger.error("=======:", e);
            return false;
        }
    }

    private Member buildLocalMember(ServiceInstance localServiceInstance) {
        String localServiceId = localServiceInstance.getServiceId();
        URI emptyEndpoint = null;
        //noinspection ConstantConditions | added null variable for clarity
        return registered
                ? new SimpleMember<>(buildSimpleMemberName(localServiceId, buildRemoteUriWithContextRoot(localServiceInstance)),
                localServiceInstance.getUri(),
                SimpleMember.LOCAL_MEMBER,
                this::suspect)
                : new SimpleMember<>(localServiceId.toUpperCase() + "[LOCAL]",
                emptyEndpoint,
                SimpleMember.LOCAL_MEMBER,
                this::suspect);
    }

    private Member buildRemoteMember(ServiceInstance remoteServiceInstance) {
        URI serviceWithContextRootUri = buildRemoteUriWithContextRoot(remoteServiceInstance);

        return new SimpleMember<>(buildSimpleMemberName(remoteServiceInstance.getServiceId(),
                serviceWithContextRootUri),
                serviceWithContextRootUri,
                SimpleMember.REMOTE_MEMBER,
                this::suspect);
    }

    private String buildSimpleMemberName(String serviceId, URI serviceUri) {
        return serviceId.toUpperCase() + "[" + serviceUri + "]";
    }

    private URI buildRemoteUriWithContextRoot(ServiceInstance serviceInstance) {
        if (contextRootMetadataPropertyname == null) {
            return serviceInstance.getUri();
        }

        if (serviceInstance.getMetadata() == null) {
            logger.warn("A contextRootMetadataPropertyName [{}] has been provided, but the metadata is null. " +
                    "Defaulting to '/' as the context root.", contextRootMetadataPropertyname);
            return serviceInstance.getUri();
        }

        if (!serviceInstance.getMetadata().containsKey(contextRootMetadataPropertyname)) {
            logger.info("The service instance metadata does not contain a property with name '{}'. " +
                    "Defaulting to '/' as the context root.", contextRootMetadataPropertyname);
            return serviceInstance.getUri();
        }

        return UriComponentsBuilder.fromUri(serviceInstance.getUri())
                .path(serviceInstance.getMetadata().get(contextRootMetadataPropertyname))
                .build()
                .toUri();
    }

    private ConsistentHash suspect(Member member) {
        ConsistentHash newConsistentHash =
                atomicConsistentHash.updateAndGet(consistentHash -> consistentHash.without(member));
        consistentHashChangeListener.onConsistentHashChanged(newConsistentHash);
        return newConsistentHash;
    }


    protected Optional<MessageRoutingInformation> getMessageRoutingInformation(ServiceInstance serviceInstance) {
        if (!serviceInstanceMetadataContainsMessageRoutingInformation(serviceInstance)) {
            return Optional.empty();
        }

        Map<String, String> serviceInstanceMetadata = serviceInstance.getMetadata();

        int loadFactor = Integer.parseInt(serviceInstanceMetadata.get(LOAD_FACTOR));
        SimpleSerializedObject<String> serializedCommandFilter = new SimpleSerializedObject<>(
                serviceInstanceMetadata.get(SERIALIZED_COMMAND_FILTER), String.class,
                serviceInstanceMetadata.get(SERIALIZED_COMMAND_FILTER_CLASS_NAME), null
        );
        return Optional.of(new MessageRoutingInformation(loadFactor, serializedCommandFilter));
    }


    public static class Builder {

        private DiscoveryClient discoveryClient;
        private Registration localServiceInstance;
        private RoutingStrategy routingStrategy;
        private Predicate<ServiceInstance> serviceInstanceFilter =
                SpringCloudCommandRouter::serviceInstanceMetadataContainsMessageRoutingInformation;
        private ConsistentHashChangeListener consistentHashChangeListener = ConsistentHashChangeListener.noOp();
        private String contextRootMetadataPropertyname;
        private Supplier<Serializer> serializerSupplier = XStreamSerializer::defaultSerializer;


        public CustomSpringCloudCommandRouter.Builder discoveryClient(DiscoveryClient discoveryClient) {
            assertNonNull(discoveryClient, "DiscoveryClient may not be null");
            this.discoveryClient = discoveryClient;
            return this;
        }

        public CustomSpringCloudCommandRouter.Builder localServiceInstance(Registration localServiceInstance) {
            assertNonNull(localServiceInstance, "Registration may not be null");
            this.localServiceInstance = localServiceInstance;
            return this;
        }


        public CustomSpringCloudCommandRouter.Builder routingStrategy(RoutingStrategy routingStrategy) {
            assertNonNull(routingStrategy, "RoutingStrategy may not be null");
            this.routingStrategy = routingStrategy;
            return this;
        }


        public CustomSpringCloudCommandRouter.Builder serviceInstanceFilter(Predicate<ServiceInstance> serviceInstanceFilter) {
            assertNonNull(serviceInstanceFilter, "ServiceInstanceFilter may not be null");
            this.serviceInstanceFilter = serviceInstanceFilter;
            return this;
        }


        public CustomSpringCloudCommandRouter.Builder consistentHashChangeListener(ConsistentHashChangeListener consistentHashChangeListener) {
            assertNonNull(consistentHashChangeListener, "ConsistentHashChangeListener may not be null");
            this.consistentHashChangeListener = consistentHashChangeListener;
            return this;
        }


        public CustomSpringCloudCommandRouter.Builder contextRootMetadataPropertyName(String contextRootMetadataPropertyName) {
            this.contextRootMetadataPropertyname = contextRootMetadataPropertyName;
            return this;
        }


        public CustomSpringCloudCommandRouter.Builder serializer(Serializer serializer) {
            assertNonNull(serializer, "Serializer may not be null");
            this.serializerSupplier = () -> serializer;
            return this;
        }


        public CustomSpringCloudCommandRouter build() {
            return new CustomSpringCloudCommandRouter(this);
        }


        protected void validate() {
            assertNonNull(discoveryClient, "The DiscoveryClient is a hard requirement and should be provided");
            assertNonNull(localServiceInstance, "The Registration is a hard requirement and should be provided");
            assertNonNull(routingStrategy, "The RoutingStrategy is a hard requirement and should be provided");
            assertNonNull(serviceInstanceFilter,
                    "The ServiceInstanceFilter is a hard requirement and should be provided");
            assertNonNull(consistentHashChangeListener,
                    "The ConsistentHashChangeListener is a hard requirement and should be provided");
        }
    }
}
