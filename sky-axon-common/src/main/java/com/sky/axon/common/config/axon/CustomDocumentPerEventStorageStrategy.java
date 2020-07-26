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

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Sorts;
import com.sky.axon.common.config.mongo.DataSourceContext;
import com.sky.axon.common.constant.AxonExtendConstants;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.DomainEventData;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventUtils;
import org.axonframework.extensions.mongo.eventsourcing.eventstore.documentperevent.DocumentPerEventStorageStrategy;
import org.axonframework.messaging.MetaData;
import org.axonframework.serialization.Serializer;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.mongodb.client.model.Filters.*;
import static java.util.stream.StreamSupport.stream;

/**
 * @author
 */
@Slf4j
public class CustomDocumentPerEventStorageStrategy extends DocumentPerEventStorageStrategy {

    @Getter
    @Setter
    private CustomEventEntryConfiguration customEventEntryConfiguration;

    @Override
    protected Document createSnapshotDocument(DomainEventMessage<?> snapshot, Serializer serializer) {
        return (new CustomDomainEventEntry(snapshot, serializer)).asDocument(this.getCustomEventEntryConfiguration());
    }

    @Override
    protected Stream<Document> createEventDocuments(List<? extends EventMessage<?>> events, Serializer serializer) {
        return events.stream().map(EventUtils::asDomainEventMessage).map(event -> new CustomDomainEventEntry(event, serializer))
                .map(entry -> entry.asDocument(this.getCustomEventEntryConfiguration()));
    }

    @Override
    public void ensureIndexes(MongoCollection<Document> eventsCollection,
                              MongoCollection<Document> snapshotsCollection) {
        eventsCollection.createIndex(new BasicDBObject(customEventEntryConfiguration.aggregateIdentifierProperty(), ORDER_ASC)
                        .append(customEventEntryConfiguration.sequenceNumberProperty(), ORDER_ASC),
                new IndexOptions().unique(true).name("uniqueAggregateIndex"));

        eventsCollection.createIndex(new BasicDBObject(customEventEntryConfiguration.timestampProperty(), ORDER_ASC)
                        .append(customEventEntryConfiguration.sequenceNumberProperty(), ORDER_ASC),
                new IndexOptions().unique(false).name("orderedEventStreamIndex"));
        snapshotsCollection.createIndex(new BasicDBObject(customEventEntryConfiguration.aggregateIdentifierProperty(), ORDER_ASC)
                        .append(customEventEntryConfiguration.sequenceNumberProperty(), ORDER_ASC),
                new IndexOptions().unique(false).name("noUniqueAggregateIndex"));
    }


    @Override
    protected DomainEventData<?> extractSnapshot(Document object) {
        return this.extractEvent(object);
    }

    @Override
    public Stream<? extends DomainEventData<?>> extractEvents(Document object) {
        return Stream.of(this.extractEvent(object));
    }

    private CustomDomainEventEntry extractEvent(Document object) {
        return new CustomDomainEventEntry(object, super.eventConfiguration());
    }

    @Override
    public void appendSnapshot(MongoCollection<Document> snapshotCollection, DomainEventMessage<?> snapshot, Serializer serializer) {
        String dataSource = DataSourceContext.getDataSource();
        log.info("DocumentPerEventStorage == "+dataSource);

        MetaData metaData = snapshot.getMetaData();
        if (!StringUtils.isEmpty(metaData.get(AxonExtendConstants.TAG))) {
            Document snapshotDocument = this.createSnapshotDocument(snapshot, serializer);
            this.deleteSnapshots(snapshotCollection, snapshot.getAggregateIdentifier(), snapshot.getSequenceNumber(), metaData);
            snapshotCollection.insertOne(snapshotDocument);
            return;
        }
        super.appendSnapshot(snapshotCollection, snapshot, serializer);
    }

    @Override
    public void deleteSnapshots(MongoCollection<Document> snapshotCollection, String aggregateIdentifier, long sequenceNumber) {
        snapshotCollection.deleteMany(and(eq(this.customEventEntryConfiguration.aggregateIdentifierProperty(), aggregateIdentifier),
                lt(this.customEventEntryConfiguration.sequenceNumberProperty(), sequenceNumber)));
    }

    @Override
    public List<? extends DomainEventData<?>> findDomainEvents(MongoCollection<Document> collection,
                                                               String aggregateIdentifier, long firstSequenceNumber,
                                                               int batchSize) {
        FindIterable<Document> cursor = collection
                .find(and(eq(this.customEventEntryConfiguration.aggregateIdentifierProperty(), aggregateIdentifier),
                        gte(this.customEventEntryConfiguration.sequenceNumberProperty(), firstSequenceNumber),
                        eq(this.customEventEntryConfiguration.reversion(), "null"),
                        eq(this.customEventEntryConfiguration.tag(), "null")

                ))
                .sort(new BasicDBObject(eventConfiguration().sequenceNumberProperty(), ORDER_ASC));
        cursor = cursor.batchSize(batchSize);
        List<? extends DomainEventData<?>> collect = stream(cursor.spliterator(), false).flatMap(this::extractEvents)
                .filter(event -> event.getSequenceNumber() >= firstSequenceNumber).collect(Collectors.toList());
        return collect;
    }

    @Override
    public Stream<? extends DomainEventData<?>> findSnapshots(MongoCollection<Document> snapshotCollection, String aggregateIdentifier) {
        FindIterable<Document> cursor = snapshotCollection.find(Filters.eq(this.customEventEntryConfiguration.aggregateIdentifierProperty(), aggregateIdentifier)).sort(Sorts.orderBy(new Bson[]{Sorts.descending(new String[]{this.customEventEntryConfiguration.sequenceNumberProperty()})}));
        return StreamSupport.stream(cursor.spliterator(), false).map(this::extractSnapshot);
    }

    /**
     * 删除快照,不能有重复版本
     *
     * @param snapshotCollection
     * @param aggregateIdentifier
     * @param sequenceNumber
     * @param metaData
     */
    public void deleteSnapshots(MongoCollection<Document> snapshotCollection, String aggregateIdentifier, long sequenceNumber, MetaData metaData) {
        snapshotCollection.deleteMany(and(eq(this.customEventEntryConfiguration.aggregateIdentifierProperty(), aggregateIdentifier),
//                eq(this.customEventEntryConfiguration.sequenceNumberProperty(), sequenceNumber),
                eq(this.customEventEntryConfiguration.tag(), metaData.get(AxonExtendConstants.TAG))
        ));
    }
}
