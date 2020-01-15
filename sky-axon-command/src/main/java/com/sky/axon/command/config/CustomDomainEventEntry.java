package com.sky.axon.command.config;

import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.extensions.mongo.eventsourcing.eventstore.documentperevent.EventEntry;
import org.axonframework.extensions.mongo.eventsourcing.eventstore.documentperevent.EventEntryConfiguration;
import org.axonframework.messaging.MetaData;
import org.axonframework.serialization.Serializer;
import org.bson.Document;

/**
 * @author
 */
public class CustomDomainEventEntry extends EventEntry {

    private String tenantCode;

    private String tag;

    public CustomDomainEventEntry(DomainEventMessage<?> event, Serializer serializer) {
        super(event, serializer);
        MetaData metaData = event.getMetaData();
        /*this.tenantCode = ObjectUtils.toString(metaData.get("tenantCode"));
        this.tag = ObjectUtils.toString(metaData.get("tag"));*/
    }

    public CustomDomainEventEntry(Document dbObject, CustomEventEntryConfiguration configuration, EventEntryConfiguration entryConfiguration) {
        super(dbObject, entryConfiguration);
        this.tenantCode = (String) dbObject.get("tenantCode");
        this.tag = (String) dbObject.get("tag");
    }


    public Document asDocument(CustomEventEntryConfiguration configuration) {
        return (new Document(configuration.aggregateIdentifierProperty(), super.getAggregateIdentifier()))
                .append(configuration.typeProperty(), super.getType())
                .append(configuration.sequenceNumberProperty(), super.getSequenceNumber())
                .append(configuration.payloadProperty(), super.getPayload().getData())
                .append(configuration.timestampProperty(), super.getTimestamp())
                .append(configuration.payloadTypeProperty(), super.getPayload().getType().getName())
                .append(configuration.payloadRevisionProperty(), super.getPayload().getType().getRevision())
                .append(configuration.metaDataProperty(), super.getMetaData().getData())
                .append(configuration.eventIdentifierProperty(), super.getEventIdentifier())
                .append("tenantCode", this.tenantCode)
                .append("tag", this.tag)
                ;
    }


}
