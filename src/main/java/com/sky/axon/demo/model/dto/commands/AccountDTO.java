package com.sky.axon.demo.model.dto.commands;

import lombok.Data;

import java.util.List;

/**
 * @author
 */
@Data
public class AccountDTO {

    private String id;

    private String startingBalance;

    private String currency;

    private List<AddressDTO> address;
}
