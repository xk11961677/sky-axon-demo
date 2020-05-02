package com.sky.axon.query;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventMessageHandler;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.eventhandling.ListenerInvocationErrorHandler;
import org.axonframework.extensions.mongo.MongoTemplate;
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
public class CustomErrorHandler implements ListenerInvocationErrorHandler {

    @Resource
    private MongoTemplate defaultMongoTemplate;

    public CustomErrorHandler() {
    }


    @Override
    public void onError(Exception exception, EventMessage<?> eventMessage, EventMessageHandler eventMessageHandler) throws Exception {
        log.warn("EventListener [{}] failed to handle event [{}] ({}). Starting retry mode",
                eventMessageHandler.getClass().getSimpleName(),
                eventMessage.getIdentifier(),
                eventMessage.getPayloadType().getName(),
                exception);
        GenericDomainEventMessage genericDomainEventMessage = (GenericDomainEventMessage) eventMessage;
        defaultMongoTemplate.eventCollection().deleteOne(and(
                eq("aggregateIdentifier", genericDomainEventMessage.getAggregateIdentifier()),
                eq("sequenceNumber", genericDomainEventMessage.getSequenceNumber()),
                eq("type", genericDomainEventMessage.getType())));
        throw exception;
    }
}
