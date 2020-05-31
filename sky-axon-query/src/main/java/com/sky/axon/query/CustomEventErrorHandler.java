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
package com.sky.axon.query;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventMessageHandler;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.eventhandling.ListenerInvocationErrorHandler;
import org.axonframework.extensions.mongo.MongoTemplate;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * 将异常抛出,并将axon event数据删除
 *
 * @author
 */
@Component
@Slf4j
public class CustomEventErrorHandler implements ListenerInvocationErrorHandler {

    @Resource
    private MongoTemplate defaultMongoTemplate;

    public CustomEventErrorHandler() {
    }


    @Override
    public void onError(Exception exception, EventMessage<?> eventMessage, EventMessageHandler eventMessageHandler) throws Exception {
        try {
            log.warn("EventListener [{}] failed to handle event [{}] ({}). Starting retry mode",
                    eventMessageHandler.getClass().getSimpleName(),
                    eventMessage.getIdentifier(),
                    eventMessage.getPayloadType().getName(),
                    exception);
            if (eventMessage instanceof GenericDomainEventMessage) {
                GenericDomainEventMessage genericDomainEventMessage = (GenericDomainEventMessage) eventMessage;
            /*
            //直接删除
            defaultMongoTemplate.eventCollection().deleteOne(and(
                eq("aggregateIdentifier", genericDomainEventMessage.getAggregateIdentifier()),
                eq("sequenceNumber", genericDomainEventMessage.getSequenceNumber()),
                eq("type", genericDomainEventMessage.getType())));*/

                //更新字段 + _REMOVE
                String version = DateFormatUtils.format(new Date(), "yyyyMMddHHmmsssss");
                Bson filter = and(eq("aggregateIdentifier", genericDomainEventMessage.getAggregateIdentifier()),
                        eq("sequenceNumber", genericDomainEventMessage.getSequenceNumber()),
                        eq("type", genericDomainEventMessage.getType()));

                Bson update = new Document("$set",
                        new Document()
                                .append("aggregateIdentifier", genericDomainEventMessage.getAggregateIdentifier() + "_REMOVE_" + version)
                                .append("type", genericDomainEventMessage.getType() + "_REMOVE_" + version));
                defaultMongoTemplate.eventCollection().updateOne(filter, update);
            } else {
                log.warn("EventListener [{}] failed to delete axon event collection [{}] ({})", eventMessageHandler.getClass().getSimpleName(),
                        eventMessage.getIdentifier(), eventMessage.getPayloadType().getName());
            }
        } finally {
            throw exception;
        }
    }
}
