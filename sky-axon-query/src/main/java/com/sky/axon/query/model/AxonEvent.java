package com.sky.axon.query.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author
 */
@Document(collection = "sky_axon_events")
@Data
public class AxonEvent {

    private long sequenceNumber;

    private String type;

    private String aggregateIdentifier;
}
