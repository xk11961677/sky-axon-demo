package com.sky.axon.demo.core.events;

import com.sky.axon.demo.model.dto.commands.AddressDTO;
import org.axonframework.serialization.Revision;

import java.util.List;

/**
 * 账号创建事件
 *
 * @author
 */
@Revision("1.0.0")
public class AccountCreatedEvent extends BaseEvent<String> {

    public String accountBalance;

    public String currency;

    public List<AddressDTO> address;

    public Integer disabled;

    public AccountCreatedEvent() {
        super();
    }

    public AccountCreatedEvent(String id, String accountBalance, String currency,
                               List<AddressDTO> address, Integer disabled) {
        super(id);
        this.accountBalance = accountBalance;
        this.currency = currency;
        this.address = address;
        this.disabled = disabled;
    }
}
