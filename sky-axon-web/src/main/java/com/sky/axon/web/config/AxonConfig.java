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
package com.sky.axon.web.config;

import com.mongodb.MongoClient;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.extensions.mongo.DefaultMongoTemplate;
import org.axonframework.extensions.mongo.MongoTemplate;
import org.axonframework.extensions.mongo.eventhandling.saga.repository.MongoSagaStore;
import org.axonframework.extensions.mongo.eventsourcing.eventstore.MongoEventStorageEngine;
import org.axonframework.extensions.mongo.eventsourcing.eventstore.documentperevent.DocumentPerEventStorageStrategy;
import org.axonframework.extensions.mongo.eventsourcing.tokenstore.MongoTokenStore;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.spring.eventsourcing.SpringAggregateSnapshotter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.Executors;

/**
 * @author
 */
@Configuration
public class AxonConfig {

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


    /*@Bean(name = "productRepository")
    public Repository<ProductAggregate> repository(EventStore eventStore){
        return new EventSourcingRepository<ProductAggregate>(ProductAggregate.class, eventStore);
    }*/

    @SuppressWarnings("AlibabaThreadPoolCreation")
    @Bean
    public CustomSpringAggregateSnapshotter customSpringAggregateSnapshotter(EventStore eventStore) {
        return CustomSpringAggregateSnapshotter.builder()
                .eventStore(eventStore)
                .executor(Executors.newFixedThreadPool(10))
                .build();
    }

    @Bean
    public SnapshotTriggerDefinition customSnapshotTriggerDefinition(EventStore eventStore) {
        return new EventCountSnapshotTriggerDefinition(customSpringAggregateSnapshotter(eventStore), 10);
    }


    @Primary
    @Bean
    public Serializer axonJsonSerializer() {
        return JacksonSerializer.builder().build();
    }

    @Bean
    public EventStorageEngine eventStorageEngine(Serializer serializer, MongoClient client) {
        return MongoEventStorageEngine.builder().eventSerializer(serializer).mongoTemplate(axonMongoTemplate(client))
                .snapshotSerializer(serializer)
                .storageStrategy(new DocumentPerEventStorageStrategy()).build();
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
}
