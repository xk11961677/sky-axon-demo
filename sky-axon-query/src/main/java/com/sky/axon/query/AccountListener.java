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
package com.sky.axon.query;

import com.sky.axon.events.AccountCreatedEvent;
import com.sky.axon.events.AccountModifiedEvent;
import com.sky.axon.events.AccountRemovedEvent;
import com.sky.axon.query.model.Account;
import com.sky.axon.query.repository.AccountMongodbDao;
import com.sky.axon.query.repository.AccountTestRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * query side
 * 操作业务库
 *
 * @author
 */
@Component
@Slf4j
public class AccountListener {

    @Resource
    private AccountMongodbDao accountMongodbDao;

    @Resource
    private AccountTestRepository accountTestRepository;

    /**
     * 创建账号业务逻辑
     * 异步创建
     *
     * @param accountCreatedEvent
     */
    @EventHandler
    protected void on(AccountCreatedEvent accountCreatedEvent) {
        log.info("===>>EventHandler AccountCreatedEvent: {}", accountCreatedEvent);
        Account account = Account.builder()
                .id(accountCreatedEvent.id)
                .accountBalance(accountCreatedEvent.accountBalance)
                .currency(accountCreatedEvent.currency)
                .address(accountCreatedEvent.address)
                .disabled(accountCreatedEvent.disabled)
                .build();
//        DataSourceContext.setDataSource("111111111");
        //accountMongodbDao.save(account);
        accountTestRepository.save(account);
    }


    @EventHandler
    protected void on(AccountModifiedEvent accountModifiedEvent) {
        log.info("===>>EventHandler AccountModifiedEvent: {}", accountModifiedEvent);
        Account account = Account.builder()
                .accountBalance(accountModifiedEvent.accountBalance)
                .currency(accountModifiedEvent.currency)
                .address(accountModifiedEvent.address)
                .build();
        accountMongodbDao.updateById(accountModifiedEvent.id, account);
    }


    @EventHandler
    protected void on(AccountRemovedEvent accountRemovedEvent) {
        log.info("===>>EventHandler AccountRemovedEvent: {}", accountRemovedEvent);
        Account account = Account.builder()
                .disabled(accountRemovedEvent.disabled)
                .build();
        accountMongodbDao.updateById(accountRemovedEvent.id, account);
    }

}
