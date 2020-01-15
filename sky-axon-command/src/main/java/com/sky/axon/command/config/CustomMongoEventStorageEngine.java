package com.sky.axon.command.config;

import org.axonframework.extensions.mongo.eventsourcing.eventstore.MongoEventStorageEngine;

/**
 * @author
 */
public class CustomMongoEventStorageEngine extends MongoEventStorageEngine {



    public CustomMongoEventStorageEngine(Builder builder) {
        super(builder);
    }


}
