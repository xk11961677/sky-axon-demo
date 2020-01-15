package com.sky.axon.command.config;

import lombok.Getter;
import lombok.Setter;
import org.axonframework.eventhandling.DomainEventData;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventUtils;
import org.axonframework.extensions.mongo.eventsourcing.eventstore.documentperevent.DocumentPerEventStorageStrategy;
import org.axonframework.serialization.Serializer;
import org.bson.Document;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author
 */
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
    protected DomainEventData<?> extractSnapshot(Document object) {
        return this.extractEvent(object);
    }

    private CustomDomainEventEntry extractEvent(Document object) {
        /*Date date = (Date) object.get(super.eventConfiguration().timestampProperty());
        String format = DateUtil.format(date, "YYYY-MM-DD HH:mm:ss:sss");
        object.replace(super.eventConfiguration().timestampProperty(), format);*/
        return new CustomDomainEventEntry(object, this.customEventEntryConfiguration, super.eventConfiguration());
    }


}
