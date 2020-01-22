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

import com.mongodb.MongoClient;
import com.sky.axon.common.config.axon.CustomDocumentPerEventStorageStrategy;
import com.sky.axon.common.config.axon.CustomEventEntryConfiguration;
import com.sky.axon.common.config.axon.CustomMongoEventStorageEngine;
import com.sky.axon.common.config.axon.CustomSpringAggregateSnapshotter;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.disruptor.commandhandling.DisruptorCommandBus;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.extensions.mongo.DefaultMongoTemplate;
import org.axonframework.extensions.mongo.MongoTemplate;
import org.axonframework.extensions.mongo.eventhandling.saga.repository.MongoSagaStore;
import org.axonframework.extensions.mongo.eventsourcing.tokenstore.MongoTokenStore;
import org.axonframework.messaging.interceptors.CorrelationDataInterceptor;
import org.axonframework.messaging.interceptors.LoggingInterceptor;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.spring.config.AxonConfiguration;
import org.axonframework.springboot.autoconfig.AxonAutoConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author
 */
@Configuration
@AutoConfigureAfter(AxonAutoConfiguration.class)
public class AxonConfig implements ApplicationContextAware {

    @Value("${spring.data.mongodb.database}")
    private String mongoDbName;

    @Value("${axon.mongodb.collection.events}")
    private String eventsCollectionName;

    @Value("${axon.mongodb.collection.snapshot}")
    private String snapshotCollectionName;

    @Value("${mongodb.tracking.tokens.collection.name}")
    private String trackingTokensCollectionName;

    @Value("${mongodb.sagas.collection.name}")
    private String sagasCollectionName;

    @Value("${mongodb.token.store.node.id}")
    private String nodeId;

    @Value("${spring.application.name}")
    private String applicationName;

    private final AtomicInteger threadPrefix = new AtomicInteger(1);

    private ApplicationContext applicationContext;

    /*@Bean
    public CustomCommandGateway customGateway(CommandBus commandBus) {
        CommandGatewayFactory factory = CommandGatewayFactory.builder().commandBus(commandBus).build();
        return factory.createGateway(CustomCommandGateway.class);
    }*/

    /*@Autowired
    public void configure(EventProcessingConfigurer config) {
        config.usingSubscribingEventProcessors();
    }*/

    /*@Bean
    public EventProcessingModule eventProcessingConfiguration() {
        EventProcessingModule config = new EventProcessingModule();
        config.assignProcessingGroup("processor1", "processor2");
        config.assignProcessingGroup(group -> group.contains("3") ? "subscribingProcessor" : group);
        config.registerSubscribingEventProcessor("subscribingProcessor");
        config.registerDefaultHandlerInterceptor((configuration, name) -> new LoggingInterceptor<>());
        return config;
    }*/

    @Bean
    public CommandBus customCommandBus(TransactionManager txManager, AxonConfiguration axonConfiguration) {
        DisruptorCommandBus commandBus =
                DisruptorCommandBus.builder()
                        .transactionManager(txManager)
                        .messageMonitor(axonConfiguration.messageMonitor(DisruptorCommandBus.class, "commandBus"))
                        .build();
        commandBus.registerHandlerInterceptor(new CorrelationDataInterceptor<>(axonConfiguration.correlationDataProviders()));
        commandBus.registerHandlerInterceptor(new LoggingInterceptor());
        commandBus.registerDispatchInterceptor(new LoggingInterceptor());
        return commandBus;
    }

    @Bean
    public CustomSpringAggregateSnapshotter customSpringAggregateSnapshotter(EventStore eventStore) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 300, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1000), r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(applicationName + "-" + threadPrefix.getAndIncrement());
            return thread;
        });
        CustomSpringAggregateSnapshotter snapshotter = CustomSpringAggregateSnapshotter.builder()
                .eventStore(eventStore)
                .executor(threadPoolExecutor)
                .build();
        snapshotter.setApplicationContext(applicationContext);
        return snapshotter;
    }

    @Bean
    public SnapshotTriggerDefinition customSnapshotTriggerDefinition(EventStore eventStore) {
        return new EventCountSnapshotTriggerDefinition(customSpringAggregateSnapshotter(eventStore), 3);
    }


    @Primary
    @Bean
    public Serializer axonJsonSerializer() {
        return JacksonSerializer.builder().build();
    }

    @Bean
    public CustomMongoEventStorageEngine eventStorageEngine(Serializer serializer, MongoClient client) {
        CustomDocumentPerEventStorageStrategy storageStrategy = new CustomDocumentPerEventStorageStrategy();
        storageStrategy.setCustomEventEntryConfiguration(CustomEventEntryConfiguration.getDefault());
        CustomMongoEventStorageEngine.Builder builder = CustomMongoEventStorageEngine.builder().eventSerializer(serializer).mongoTemplate(axonMongoTemplate(client))
                .snapshotSerializer(serializer)
                .storageStrategy(storageStrategy);
        return builder.build();
    }

    @Bean(name = "axonMongoTemplate")
    public MongoTemplate axonMongoTemplate(MongoClient client) {
        MongoTemplate template = DefaultMongoTemplate.builder().mongoDatabase(client, mongoDbName)
                .domainEventsCollectionName(eventsCollectionName)
                .snapshotEventsCollectionName(snapshotCollectionName)
                .trackingTokensCollectionName(trackingTokensCollectionName)
                .sagasCollectionName(sagasCollectionName)
                .build();
        return template;
    }

    @Bean
    public MongoTokenStore mongoTokenStore(MongoClient client) {
        return MongoTokenStore.builder()
                .serializer(axonJsonSerializer())
                .mongoTemplate(axonMongoTemplate(client))
                .nodeId(nodeId)
                .build();
    }

    @Bean
    public SagaStore sagaStore(MongoClient client) {
        return MongoSagaStore.builder()
                .mongoTemplate(axonMongoTemplate(client))
                .serializer(axonJsonSerializer())
                .build();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
