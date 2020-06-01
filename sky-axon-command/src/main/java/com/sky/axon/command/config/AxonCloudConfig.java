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

import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;
import org.axonframework.commandhandling.distributed.CommandRouter;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.commandhandling.gateway.IntervalRetryScheduler;
import org.axonframework.extensions.springcloud.commandhandling.SpringHttpCommandBusConnector;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executors;

/**
 * @author
 */
@Configuration
public class AxonCloudConfig {

    @Value("${axon.distributed.spring-cloud.fallback-url}")
    private String messageRoutingInformationEndpoint;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @Primary
    public CommandGateway commandGateway(@Qualifier("distributedBus") DistributedCommandBus commandBus) {
        IntervalRetryScheduler retryScheduler = IntervalRetryScheduler.builder().retryExecutor(Executors.newSingleThreadScheduledExecutor())
                .retryInterval(1000)
                .retryInterval(1).build();
        return DefaultCommandGateway.builder().commandBus(commandBus)
                .retryScheduler(retryScheduler)
                .build();
    }

    /*@Bean
    @Primary
    @Qualifier("springCloudRouter")
    public CommandRouter springCloudCommandRouter(DiscoveryClient discoveryClient, Registration localServiceInstance) {
        return SpringCloudCommandRouter.builder().discoveryClient(discoveryClient)
                .localServiceInstance(localServiceInstance)
                .routingStrategy(new AnnotationRoutingStrategy())
                .build();
    }*/

    @Bean
    @Primary
//    @Qualifier("springCloudConnector")
    public SpringHttpCommandBusConnector connector(RestTemplate restTemplate) {
        return SpringHttpCommandBusConnector.builder()
                .localCommandBus(SimpleCommandBus.builder().build())
                .restOperations(restTemplate)
                .serializer(JacksonSerializer.defaultSerializer()).build();
    }

    /*@Bean
    @Primary
    @Qualifier("springCloudRouter")
    public CommandRouter springCloudCommandRouter(DiscoveryClient discoveryClient, Registration localServiceInstance, RestTemplate restTemplate) {
        return SpringCloudHttpBackupCommandRouter.builder().discoveryClient(discoveryClient)
                .routingStrategy(new AnnotationRoutingStrategy())
                .restTemplate(restTemplate)
                .localServiceInstance(localServiceInstance)
                .messageRoutingInformationEndpoint(messageRoutingInformationEndpoint).build();
    }*/

    @Bean
    @Primary
    @Qualifier("springCloudRouter")
    public CommandRouter springCloudCommandRouter(DiscoveryClient discoveryClient, Registration localServiceInstance, RestTemplate restTemplate) {
        return CustomSpringCloudHttpBackupCommandRouter.builder().discoveryClient(discoveryClient)
                .routingStrategy(new AnnotationRoutingStrategy())
                .restTemplate(restTemplate)
                .enforceHttpDiscovery()
                .localServiceInstance(localServiceInstance)
//                .contextRootMetadataPropertyName("root")
                .messageRoutingInformationEndpoint(messageRoutingInformationEndpoint).build();
    }

    @Bean
    @Primary
    @Qualifier("distributedBus")
    public DistributedCommandBus springCloudDistributedCommandBus(@Qualifier("springCloudRouter") CommandRouter router, RestTemplate restTemplate) {
        return DistributedCommandBus.builder()
                .commandRouter(router)
                .connector(connector(restTemplate)).build();
    }
}
