/*
 * The MIT License (MIT)
 * Copyright © 2020 <sky>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sky.axon.events;

import com.sky.axon.api.commands.AddressDTO;
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

    public Integer disabled;

    public AccountModifiedEvent(String id, String accountBalance, String currency,
                                List<AddressDTO> address, Integer disabled) {
        super(id);
        this.accountBalance = accountBalance;
        this.currency = currency;
        this.address = address;
        this.disabled = disabled;
    }
}
