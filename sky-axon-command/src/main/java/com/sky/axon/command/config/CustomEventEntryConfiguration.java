package com.sky.axon.command.config;

import org.axonframework.extensions.mongo.eventsourcing.eventstore.documentperevent.EventEntryConfiguration;

/**
 * @author
 */
public class CustomEventEntryConfiguration {

    private final String timestampProperty, eventIdentifierProperty, aggregateIdentifierProperty,
            sequenceNumberProperty, typeProperty, payloadTypeProperty, payloadRevisionProperty, payloadProperty,
            metaDataProperty;

    /**
     * Returns the default {@link EventEntryConfiguration}.
     *
     * @return the default {@link EventEntryConfiguration}
     */
    public static CustomEventEntryConfiguration getDefault() {
        return builder().build();
    }

    private CustomEventEntryConfiguration(CustomEventEntryConfiguration.Builder builder) {
        timestampProperty = builder.timestampProperty;
        eventIdentifierProperty = builder.eventIdentifierProperty;
        aggregateIdentifierProperty = builder.aggregateIdentifierProperty;
        sequenceNumberProperty = builder.sequenceNumberProperty;
        typeProperty = builder.typeProperty;
        payloadTypeProperty = builder.payloadTypeProperty;
        payloadRevisionProperty = builder.payloadRevisionProperty;
        payloadProperty = builder.payloadProperty;
        metaDataProperty = builder.metaDataProperty;
    }

    /**
     * Returns a new Builder for an {@link EventEntryConfiguration} initialized with default settings.
     *
     * @return a new Builder with default settings
     */
    public static CustomEventEntryConfiguration.Builder builder() {
        return new CustomEventEntryConfiguration.Builder();
    }

    /**
     * Get the name of the property with the timestamp of the event.
     *
     * @return the name of the property with the timestamp
     */
    public String timestampProperty() {
        return timestampProperty;
    }

    /**
     * Get the name of the property with the identifier of the event.
     *
     * @return the name of the propery with the event identifier
     */
    public String eventIdentifierProperty() {
        return eventIdentifierProperty;
    }

    /**
     * Get the name of the property with the aggregate identifier of the event.
     *
     * @return the name of the property with the aggregate identifier
     */
    public String aggregateIdentifierProperty() {
        return aggregateIdentifierProperty;
    }

    /**
     * Get the name of the property with the aggregate sequence number of the event.
     *
     * @return the name of the property with the aggregate sequence number
     */
    public String sequenceNumberProperty() {
        return sequenceNumberProperty;
    }

    /**
     * Get the name of the property with the aggregate type.
     *
     * @return the name of the property with the aggregate type
     */
    public String typeProperty() {
        return typeProperty;
    }

    /**
     * Get the name of the property with the payload type.
     *
     * @return the name of the property with the payload type
     */
    public String payloadTypeProperty() {
        return payloadTypeProperty;
    }

    /**
     * Get the name of the property with the payload revision.
     *
     * @return the name of the property with the payload revision
     */
    public String payloadRevisionProperty() {
        return payloadRevisionProperty;
    }

    /**
     * Get the name of the property with the payload data.
     *
     * @return the name of the property with the payload data
     */
    public String payloadProperty() {
        return payloadProperty;
    }

    /**
     * Get the name of the property with the metadata.
     *
     * @return the name of the property with the metadata
     */
    public String metaDataProperty() {
        return metaDataProperty;
    }

    public static class Builder {

        private String timestampProperty = "timestamp";
        private String eventIdentifierProperty = "eventIdentifier";
        private String aggregateIdentifierProperty = "aggregateIdentifier";
        private String sequenceNumberProperty = "sequenceNumber";
        private String typeProperty = "type";
        private String payloadTypeProperty = "payloadType";
        private String payloadRevisionProperty = "payloadRevision";
        private String payloadProperty = "serializedPayload";
        private String metaDataProperty = "serializedMetaData";

        public CustomEventEntryConfiguration.Builder timestampProperty(String timestampProperty) {
            this.timestampProperty = timestampProperty;
            return this;
        }

        public CustomEventEntryConfiguration.Builder eventIdentifierProperty(String eventIdentifierProperty) {
            this.eventIdentifierProperty = eventIdentifierProperty;
            return this;
        }

        public CustomEventEntryConfiguration.Builder aggregateIdentifierProperty(String aggregateIdentifierProperty) {
            this.aggregateIdentifierProperty = aggregateIdentifierProperty;
            return this;
        }

        public CustomEventEntryConfiguration.Builder sequenceNumberProperty(String sequenceNumberProperty) {
            this.sequenceNumberProperty = sequenceNumberProperty;
            return this;
        }

        public CustomEventEntryConfiguration.Builder typeProperty(String typeProperty) {
            this.typeProperty = typeProperty;
            return this;
        }

        public CustomEventEntryConfiguration.Builder payloadTypeProperty(String payloadTypeProperty) {
            this.payloadTypeProperty = payloadTypeProperty;
            return this;
        }

        public CustomEventEntryConfiguration.Builder payloadRevisionProperty(String payloadRevisionProperty) {
            this.payloadRevisionProperty = payloadRevisionProperty;
            return this;
        }

        public CustomEventEntryConfiguration.Builder payloadProperty(String payloadProperty) {
            this.payloadProperty = payloadProperty;
            return this;
        }

        public CustomEventEntryConfiguration.Builder metaDataProperty(String metaDataProperty) {
            this.metaDataProperty = metaDataProperty;
            return this;
        }

        public CustomEventEntryConfiguration build() {
            return new CustomEventEntryConfiguration(this);
        }
    }
}
