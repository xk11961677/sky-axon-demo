package com.sky.axon.demo.core.commands;

import com.sky.axon.demo.model.dto.commands.AddressDTO;

import java.util.List;

/**
 * 创建账号
 *
 * @author
 */
public class ModifyAccountCommand extends BaseCommand<String> {

    public final String accountBalance;

    public final String currency;

    public final List<AddressDTO> address;

    public ModifyAccountCommand(String id, String accountBalance, String currency,
                                List<AddressDTO> address) {
        super(id);
        this.accountBalance = accountBalance;
        this.currency = currency;
        this.address = address;
    }
}
