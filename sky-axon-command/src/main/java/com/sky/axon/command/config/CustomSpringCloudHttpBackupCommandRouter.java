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

import com.sky.axon.common.util.JacksonUtils;
import org.apache.commons.lang.StringUtils;
import org.axonframework.commandhandling.distributed.CommandMessageFilter;
import org.axonframework.commandhandling.distributed.ConsistentHashChangeListener;
import org.axonframework.commandhandling.distributed.Member;
import org.axonframework.commandhandling.distributed.RoutingStrategy;
import org.axonframework.commandhandling.distributed.commandfilter.DenyAll;
import org.axonframework.extensions.springcloud.commandhandling.MessageRoutingInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static org.axonframework.common.BuilderUtils.assertNonNull;
import static org.axonframework.common.BuilderUtils.assertThat;

/**
 * @author
 */
//@RestController
@RequestMapping("/custom-message-routing-information")
public class CustomSpringCloudHttpBackupCommandRouter extends CustomSpringCloudCommandRouter {

    private static final Logger logger = LoggerFactory.getLogger(CustomSpringCloudHttpBackupCommandRouter.class);

    private static final Predicate<ServiceInstance> ACCEPT_ALL_INSTANCES_FILTER = serviceInstance -> true;

    private final RestTemplate restTemplate;
    private final String messageRoutingInformationEndpoint;
    private final MessageRoutingInformation unreachableService;
    private final boolean enforceHttpDiscovery;

    private volatile MessageRoutingInformation messageRoutingInfo;

    protected CustomSpringCloudHttpBackupCommandRouter(Builder builder) {
        super(builder);
        this.restTemplate = builder.restTemplate;
        this.messageRoutingInformationEndpoint = builder.messageRoutingInformationEndpoint;
        this.enforceHttpDiscovery = builder.enforceHttpDiscovery;
        messageRoutingInfo = null;
        unreachableService = new MessageRoutingInformation(0, DenyAll.INSTANCE, serializer);
    }


    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void updateMembership(int loadFactor, CommandMessageFilter commandFilter) {
        messageRoutingInfo = new MessageRoutingInformation(loadFactor, commandFilter, serializer);
        super.updateMembership(loadFactor, commandFilter);
    }


    @GetMapping
    @ResponseBody
    public MessageRoutingInformation getLocalMessageRoutingInformation() {
        return messageRoutingInfo;
    }

    @Override
    protected Optional<MessageRoutingInformation> getMessageRoutingInformation(ServiceInstance serviceInstance) {
        if (enforceHttpDiscovery) {
            return requestMessageRoutingInformation(serviceInstance);
        }

        Optional<MessageRoutingInformation> defaultMessageRoutingInfo =
                super.getMessageRoutingInformation(serviceInstance);
        return defaultMessageRoutingInfo.isPresent() ?
                defaultMessageRoutingInfo : requestMessageRoutingInformation(serviceInstance);
    }

    private Optional<MessageRoutingInformation> requestMessageRoutingInformation(ServiceInstance serviceInstance) {
        URI destinationUri = null;
        try {
            Member member = buildMember(serviceInstance);
            if (member.local()) {
                return Optional.of(getLocalMessageRoutingInformation());
            }

            URI endpoint = member.getConnectionEndpoint(URI.class)
                    .orElseThrow(() -> new IllegalArgumentException(String.format(
                            "No Connection Endpoint found in Member [%s] for protocol [%s] to send a " +
                                    "%s request to", member,
                            URI.class, MessageRoutingInformation.class.getSimpleName()
                    )));
            destinationUri = buildURIForPath(endpoint, messageRoutingInformationEndpoint);
        } catch (Exception e) {
            logger.error("=====>>", e);
            return Optional.of(getLocalMessageRoutingInformation());
        }

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(destinationUri,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    String.class);

            if (StringUtils.isBlank(responseEntity.getBody())) {
                throw new RuntimeException("");
            }
            MessageRoutingInformation messageRoutingInformation = JacksonUtils.json2pojo(responseEntity.getBody(), MessageRoutingInformation.class);
            return Optional.ofNullable(messageRoutingInformation);
        } catch (HttpClientErrorException e) {
            logger.info(
                    "Blacklisting Service [" + serviceInstance.getServiceId() + "], "
                            + "as requesting message routing information from it resulted in an exception.",
                    logger.isDebugEnabled() ? e : null
            );
            return Optional.empty();
        } catch (Exception e) {
//            logger.info(
//                    destinationUri + "   Failed to receive message routing information from Service ["
//                            + serviceInstance.getServiceId() + "] due to an exception. "
//                            + "Will temporarily set this instance to deny all incoming messages",
//                    logger.isDebugEnabled() ? e : null
//            );
            return Optional.of(unreachableService);
        }
    }

    private static URI buildURIForPath(URI uri, String appendToPath) {
        return UriComponentsBuilder.fromUri(uri)
                .path(appendToPath)
                .build()
                .toUri();
    }

    public static class Builder extends CustomSpringCloudCommandRouter.Builder {

        private RestTemplate restTemplate;
        private String messageRoutingInformationEndpoint = "/message-routing-information";
        private boolean enforceHttpDiscovery = false;

        public Builder() {
            serviceInstanceFilter(ACCEPT_ALL_INSTANCES_FILTER);
        }

        @Override
        public Builder discoveryClient(DiscoveryClient discoveryClient) {
            super.discoveryClient(discoveryClient);
            return this;
        }

        @Override
        public Builder localServiceInstance(Registration localServiceInstance) {
            super.localServiceInstance(localServiceInstance);
            return this;
        }

        @Override
        public Builder routingStrategy(RoutingStrategy routingStrategy) {
            super.routingStrategy(routingStrategy);
            return this;
        }

        @Override
        public Builder serviceInstanceFilter(
                Predicate<ServiceInstance> serviceInstanceFilter) {
            super.serviceInstanceFilter(serviceInstanceFilter);
            return this;
        }

        @Override
        public Builder consistentHashChangeListener(ConsistentHashChangeListener consistentHashChangeListener) {
            super.consistentHashChangeListener(consistentHashChangeListener);
            return this;
        }

        @Override
        public Builder contextRootMetadataPropertyName(String contextRootMetadataPropertyName) {
            super.contextRootMetadataPropertyName(contextRootMetadataPropertyName);
            return this;
        }

        public Builder restTemplate(RestTemplate restTemplate) {
            assertNonNull(restTemplate, "RestTemplate may not be null");
            this.restTemplate = restTemplate;
            return this;
        }


        public Builder messageRoutingInformationEndpoint(String messageRoutingInformationEndpoint) {
            assertMessageRoutingInfoEndpoint(messageRoutingInformationEndpoint,
                    "The messageRoutingInformationEndpoint may not be null or empty");
            this.messageRoutingInformationEndpoint = messageRoutingInformationEndpoint;
            return this;
        }


        public Builder enforceHttpDiscovery() {
            this.enforceHttpDiscovery = true;
            return this;
        }

        @Override
        public CustomSpringCloudHttpBackupCommandRouter build() {
            return new CustomSpringCloudHttpBackupCommandRouter(this);
        }

        @Override
        protected void validate() {
            super.validate();
            assertNonNull(restTemplate, "The RestTemplate is a hard requirement and should be provided");
            assertMessageRoutingInfoEndpoint(
                    messageRoutingInformationEndpoint,
                    "The messageRoutingInformationEndpoint is a hard requirement and should be provided"
            );
        }

        private void assertMessageRoutingInfoEndpoint(String messageRoutingInfoEndpoint, String exceptionMessage) {
            assertThat(messageRoutingInfoEndpoint, name -> Objects.nonNull(name) && !"".equals(name), exceptionMessage);
        }
    }
}
