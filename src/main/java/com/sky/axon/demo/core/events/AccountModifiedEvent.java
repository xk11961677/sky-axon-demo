package com.sky.axon.demo.core.events;

import com.sky.axon.demo.model.dto.commands.AddressDTO;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import java.util.List;

/**
 * 账号创建事件
 *
 * @author
 */
@Revision("1.0.0")
@NoArgsConstructor
public class AccountModifiedEvent extends BaseEvent<String> {

    public String accountBalance;

    public String currency;

    public List<AddressDTO> address;

    public AccountModifiedEvent(String id, String accountBalance, String currency,
                                List<AddressDTO> address) {
        super(id);
        this.accountBalance = accountBalance;
        this.currency = currency;
        this.address = address;
    }
}
