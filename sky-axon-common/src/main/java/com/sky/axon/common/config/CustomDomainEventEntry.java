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
package com.sky.axon.common.config;

import com.sky.axon.common.constant.AxonExtendConstants;
import org.axonframework.common.DateTimeUtils;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.extensions.mongo.eventsourcing.eventstore.documentperevent.EventEntry;
import org.axonframework.extensions.mongo.eventsourcing.eventstore.documentperevent.EventEntryConfiguration;
import org.axonframework.messaging.MetaData;
import org.axonframework.serialization.Serializer;
import org.bson.Document;

import java.util.Objects;

/**
 * @author
 */
public class CustomDomainEventEntry extends EventEntry {

    private String tenantCode;

    private String tag;

    private String reversion;

    public CustomDomainEventEntry(DomainEventMessage<?> event, Serializer serializer) {
        super(event, serializer);
        MetaData metaData = event.getMetaData();
        this.tenantCode = Objects.toString(metaData.get(AxonExtendConstants.TENANT_CODE));
        this.tag = Objects.toString(metaData.get(AxonExtendConstants.TAG));
        this.reversion = Objects.toString(metaData.get(AxonExtendConstants.REVERSION));
    }

    public CustomDomainEventEntry(Document dbObject, EventEntryConfiguration entryConfiguration) {
        super(dbObject, entryConfiguration);
        this.tenantCode = Objects.toString(dbObject.get(AxonExtendConstants.TENANT_CODE));
        this.tag = Objects.toString(dbObject.get(AxonExtendConstants.TAG));
        this.reversion = Objects.toString(dbObject.get(AxonExtendConstants.REVERSION));
    }


    public Document asDocument(CustomEventEntryConfiguration configuration) {
        return (new Document(configuration.aggregateIdentifierProperty(), super.getAggregateIdentifier()))
                .append(configuration.typeProperty(), super.getType())
                .append(configuration.sequenceNumberProperty(), super.getSequenceNumber())
                .append(configuration.payloadProperty(), super.getPayload().getData())
                .append(configuration.timestampProperty(), DateTimeUtils.formatInstant(super.getTimestamp()))
                .append(configuration.payloadTypeProperty(), super.getPayload().getType().getName())
                .append(configuration.payloadRevisionProperty(), super.getPayload().getType().getRevision())
                .append(configuration.metaDataProperty(), super.getMetaData().getData())
                .append(configuration.eventIdentifierProperty(), super.getEventIdentifier())
                .append(AxonExtendConstants.TENANT_CODE, this.tenantCode)
                .append(AxonExtendConstants.TAG, this.tag)
                .append(AxonExtendConstants.REVERSION, this.reversion);
    }


}
