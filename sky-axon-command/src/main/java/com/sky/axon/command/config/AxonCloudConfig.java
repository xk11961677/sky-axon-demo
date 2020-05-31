package com.sky.axon.command.config;

import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;
import org.axonframework.commandhandling.distributed.CommandRouter;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.commandhandling.gateway.IntervalRetryScheduler;
import org.axonframework.extensions.springcloud.commandhandling.SpringCloudCommandRouter;
import org.axonframework.extensions.springcloud.commandhandling.SpringHttpCommandBusConnector;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Bean
    @Primary
    public CommandGateway commandGateway(@Qualifier("distributedBus") DistributedCommandBus commandBus) throws Exception {
        IntervalRetryScheduler retryScheduler = IntervalRetryScheduler.builder().retryExecutor(Executors.newSingleThreadScheduledExecutor())
                .retryInterval(1000)
                .retryInterval(1).build();
        return DefaultCommandGateway.builder().commandBus(commandBus)
                .retryScheduler(retryScheduler)
                .build();
    }

    @Bean
    @Primary
    @Qualifier("springCloudRouter")
    public CommandRouter springCloudCommandRouter(DiscoveryClient discoveryClient, Registration localServiceInstance) {
        return SpringCloudCommandRouter.builder().discoveryClient(discoveryClient)
                .localServiceInstance(localServiceInstance)
                .routingStrategy(new AnnotationRoutingStrategy())
                .build();
    }

    @Bean
    @Primary
    @Qualifier("springCloudConnector")
    public SpringHttpCommandBusConnector connector() {
        return SpringHttpCommandBusConnector.builder()
                .localCommandBus(SimpleCommandBus.builder().build())
                .restOperations(new RestTemplate())
                .serializer(JacksonSerializer.defaultSerializer()).build();
    }

    @Bean
    @Primary
    @Qualifier("distributedBus")
    public DistributedCommandBus springCloudDistributedCommandBus(@Qualifier("springCloudRouter") CommandRouter router) {
        return DistributedCommandBus.builder()
                .commandRouter(router)
                .connector(connector()).build();
    }
}
