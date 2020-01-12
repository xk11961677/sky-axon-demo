package com.sky.axon.demo.model.entity;

import com.sky.axon.demo.model.dto.commands.AddressDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author
 */
@Data
@Builder
public class Account {

    private String id;

    private String accountBalance;

    private String currency;

    private List<AddressDTO> address;

    private Integer disabled;
}
