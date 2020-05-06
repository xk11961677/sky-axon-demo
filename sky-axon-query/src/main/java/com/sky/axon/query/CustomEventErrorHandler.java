package com.sky.axon.query;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventMessageHandler;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.eventhandling.ListenerInvocationErrorHandler;
import org.axonframework.extensions.mongo.MongoTemplate;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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
            GenericDomainEventMessage genericDomainEventMessage = (GenericDomainEventMessage) eventMessage;
            /*
            //直接删除
            defaultMongoTemplate.eventCollection().deleteOne(and(
                eq("aggregateIdentifier", genericDomainEventMessage.getAggregateIdentifier()),
                eq("sequenceNumber", genericDomainEventMessage.getSequenceNumber()),
                eq("type", genericDomainEventMessage.getType())));*/

            //更新字段 + _REMOVE
            Bson filter = and(eq("aggregateIdentifier", genericDomainEventMessage.getAggregateIdentifier()),
                    eq("sequenceNumber", genericDomainEventMessage.getSequenceNumber()),
                    eq("type", genericDomainEventMessage.getType()));

            Bson update = new Document("$set",
                    new Document()
                            .append("aggregateIdentifier", genericDomainEventMessage.getAggregateIdentifier() + "_REMOVE")
                            .append("type", genericDomainEventMessage.getType() + "_REMOVE"));
            defaultMongoTemplate.eventCollection().updateOne(filter, update);
        } finally {
            throw exception;
        }
    }
}
