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
package com.sky.axon.common.config.axon;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoBulkWriteException;
import com.sky.axon.common.constant.AxonExtendConstants;
import com.sky.axon.common.util.DataSourceContext;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.AxonConfigurationException;
import org.axonframework.common.jdbc.PersistenceExceptionResolver;
import org.axonframework.eventhandling.*;
import org.axonframework.eventsourcing.eventstore.BatchingEventStorageEngine;
import org.axonframework.extensions.mongo.MongoTemplate;
import org.axonframework.extensions.mongo.eventsourcing.eventstore.MongoEventStorageEngine;
import org.axonframework.extensions.mongo.eventsourcing.eventstore.MongoTrackingToken;
import org.axonframework.extensions.mongo.eventsourcing.eventstore.StorageStrategy;
import org.axonframework.extensions.mongo.eventsourcing.eventstore.documentperevent.DocumentPerEventStorageStrategy;
import org.axonframework.messaging.MetaData;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.upcasting.event.EventUpcaster;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.axonframework.common.BuilderUtils.assertNonNull;

/**
 * @author
 */
@Slf4j
public class CustomMongoEventStorageEngine extends BatchingEventStorageEngine {


    private final MongoTemplate template;

    private final StorageStrategy storageStrategy;

    private Predicate<? super DomainEventData<?>> snapshotFilter;


    protected CustomMongoEventStorageEngine(CustomMongoEventStorageEngine.Builder builder) {
        super(builder);
        this.template = builder.template;
        this.storageStrategy = builder.storageStrategy;
        this.snapshotFilter = builder.snapshotFilter;
        if (builder.snapshotFilter == null) {
            this.snapshotFilter = (i) -> true;
        }
        //todo 多数据源时去掉
        //ensureIndexes();
    }

    public static CustomMongoEventStorageEngine.Builder builder() {
        return new CustomMongoEventStorageEngine.Builder();
    }

    private static boolean isDuplicateKeyException(Exception exception) {
        return exception instanceof DuplicateKeyException || (exception instanceof MongoBulkWriteException &&
                ((MongoBulkWriteException) exception).getWriteErrors().stream().anyMatch(e -> e.getCode() == 11000));
    }

    /*@Override
    public DomainEventStream readEvents(String aggregateIdentifier, long firstSequenceNumber) {
        Stream<? extends DomainEventData<?>> input = this.readEventData(aggregateIdentifier, firstSequenceNumber);
        return EventStreamUtils.upcastAndDeserializeDomainEvents(input, getEventSerializer(), this.upcasterChain);
    }*/

    /*@Override
    public Optional<DomainEventMessage<?>> readSnapshot(String aggregateIdentifier) {
        return this.readSnapshotData(aggregateIdentifier).filter(snapshotFilter).map((snapshot) -> {
            System.out.println("================snapshot");
            return EventStreamUtils.upcastAndDeserializeDomainEvents(Stream.of(snapshot), getEventSerializer(), this.upcasterChain);
        }).flatMap(DomainEventStream::asStream).findFirst().map((event) -> {
            return event;
        });
    }*/

    @Deprecated
    public void ensureIndexes() {
        storageStrategy.ensureIndexes(template.eventCollection(), template.snapshotCollection());
    }

    @Override
    protected void appendEvents(List<? extends EventMessage<?>> events, Serializer serializer) {
        if (!events.isEmpty()) {
            try {
                log.info("CustomMongoEventStorageEngine dataSource :{}", DataSourceContext.getDataSource());
                storageStrategy.appendEvents(template.eventCollection(), events, serializer);
            } catch (Exception e) {
                handlePersistenceException(e, events.get(0));
            }
        }
    }

    @Override
    protected void storeSnapshot(DomainEventMessage<?> snapshot, Serializer serializer) {
        try {
            String dataSource = DataSourceContext.getDataSource();
            log.info("CustomMongoEventStorageEngine.storeSnapshot() " + dataSource);
            storageStrategy.appendSnapshot(template.snapshotCollection(), snapshot, serializer);
            MetaData metaData = snapshot.getMetaData();
            if (!StringUtils.isEmpty(metaData.get(AxonExtendConstants.TAG))) {
                return;
            }
            storageStrategy.deleteSnapshots(template.snapshotCollection(), snapshot.getAggregateIdentifier(), snapshot.getSequenceNumber());
        } catch (Exception e) {
            handlePersistenceException(e, snapshot);
        }
    }


    @Override
    protected Stream<? extends DomainEventData<?>> readSnapshotData(String aggregateIdentifier) {
        log.info("CustomMongoEventStorageEngin.readSnapshotData dataSource:{} ", DataSourceContext.getDataSource());
        return storageStrategy.findSnapshots(template.snapshotCollection(), aggregateIdentifier);
    }

    @Override
    protected List<? extends DomainEventData<?>> fetchDomainEvents(String aggregateIdentifier, long firstSequenceNumber,
                                                                   int batchSize) {
        log.info("CustomMongoEventStorageEngin.fetchDomainEvents dataSource:{} ", DataSourceContext.getDataSource());
        return storageStrategy.findDomainEvents(template.eventCollection(), aggregateIdentifier, firstSequenceNumber, batchSize);
    }

    @Override
    protected List<? extends TrackedEventData<?>> fetchTrackedEvents(TrackingToken lastToken, int batchSize) {
        return storageStrategy.findTrackedEvents(template.eventCollection(), lastToken, batchSize);
    }

    @Override
    public Optional<Long> lastSequenceNumberFor(String aggregateIdentifier) {
        log.info("CustomMongoEventStorageEngin.lastSequenceNumberFor dataSource:{} ", DataSourceContext.getDataSource());
        return storageStrategy.lastSequenceNumberFor(template.eventCollection(), aggregateIdentifier);
    }

    @Override
    public TrackingToken createTailToken() {
        return storageStrategy.createTailToken(template.eventCollection());
    }

    @Override
    public TrackingToken createHeadToken() {
        return createTokenAt(Instant.now());
    }

    @Override
    public TrackingToken createTokenAt(Instant dateTime) {
        return MongoTrackingToken.of(dateTime, Collections.emptyMap());
    }


    public static class Builder extends BatchingEventStorageEngine.Builder {

        private MongoTemplate template;
        private StorageStrategy storageStrategy = new CustomDocumentPerEventStorageStrategy();
        private Predicate<? super DomainEventData<?>> snapshotFilter;

        private Builder() {
            persistenceExceptionResolver(CustomMongoEventStorageEngine::isDuplicateKeyException);
        }

        @Override
        public CustomMongoEventStorageEngine.Builder snapshotSerializer(Serializer snapshotSerializer) {
            super.snapshotSerializer(snapshotSerializer);
            return this;
        }

        @Override
        public CustomMongoEventStorageEngine.Builder upcasterChain(EventUpcaster upcasterChain) {
            super.upcasterChain(upcasterChain);
            return this;
        }

        @Override
        public CustomMongoEventStorageEngine.Builder persistenceExceptionResolver(PersistenceExceptionResolver persistenceExceptionResolver) {
            super.persistenceExceptionResolver(persistenceExceptionResolver);
            return this;
        }

        @Override
        public CustomMongoEventStorageEngine.Builder eventSerializer(Serializer eventSerializer) {
            super.eventSerializer(eventSerializer);
            return this;
        }

        @Override
        public CustomMongoEventStorageEngine.Builder snapshotFilter(Predicate<? super DomainEventData<?>> snapshotFilter) {
            super.snapshotFilter(snapshotFilter);
            this.snapshotFilter = snapshotFilter;
            return this;
        }

        @Override
        public CustomMongoEventStorageEngine.Builder batchSize(int batchSize) {
            super.batchSize(batchSize);
            return this;
        }

        /**
         * Sets the {@link MongoTemplate} used to obtain the database and the collections.
         *
         * @param template the {@link MongoTemplate} used to obtain the database and the collections
         * @return the current Builder instance, for fluent interfacing
         */
        public CustomMongoEventStorageEngine.Builder mongoTemplate(MongoTemplate template) {
            assertNonNull(template, "MongoTemplate may not be null");
            this.template = template;
            return this;
        }

        /**
         * Sets the {@link StorageStrategy} specifying how to store and retrieve events and snapshots from the
         * collections. Defaults to a {@link DocumentPerEventStorageStrategy}, causing every event and snapshot to be
         * stored in a separate Mongo Document.
         *
         * @param storageStrategy the {@link StorageStrategy} specifying how to store and retrieve events and snapshots
         *                        from the collections
         * @return the current Builder instance, for fluent interfacing
         */
        public CustomMongoEventStorageEngine.Builder storageStrategy(StorageStrategy storageStrategy) {
            assertNonNull(storageStrategy, "StorageStrategy may not be null");
            this.storageStrategy = storageStrategy;
            return this;
        }

        /**
         * Initializes a {@link MongoEventStorageEngine} as specified through this Builder.
         *
         * @return a {@link MongoEventStorageEngine} as specified through this Builder
         */
        public CustomMongoEventStorageEngine build() {
            return new CustomMongoEventStorageEngine(this);
        }

        /**
         * Validates whether the fields contained in this Builder are set accordingly.
         *
         * @throws AxonConfigurationException if one field is asserted to be incorrect according to the Builder's
         *                                    specifications
         */
        @Override
        protected void validate() throws AxonConfigurationException {
            super.validate();
            assertNonNull(template, "The MongoTemplate is a hard requirement and should be provided");
        }
    }
}
