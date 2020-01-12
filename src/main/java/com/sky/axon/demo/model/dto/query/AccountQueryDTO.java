package com.sky.axon.demo.model.dto.query;

import lombok.Data;

/**
 * @author
 */
@Data
public class AccountQueryDTO {

    private String id;

    private Double startingBalance;

    private String currency;
}
