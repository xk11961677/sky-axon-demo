package com.sky.axon.demo.model.dto.commands;

import lombok.Data;

import java.io.Serializable;

/**
 * @author
 */
@Data
public class AddressDTO implements Serializable {

    private String city;

    private String street;
}
