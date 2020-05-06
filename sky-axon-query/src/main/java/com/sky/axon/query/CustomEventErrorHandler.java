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
