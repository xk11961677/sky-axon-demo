package com.sky.axon.demo.model.dto.commands;

import lombok.Data;

/**
 * @author
 */
@Data
public class EventDTO {

    String id;

    String type;

    Long beginSequenceNumber;

    Long endSequenceNumber;

}
