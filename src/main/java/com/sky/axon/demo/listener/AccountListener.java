package com.sky.axon.demo.listener;

import com.sky.axon.demo.core.events.AccountCreatedEvent;
import com.sky.axon.demo.core.events.AccountModifiedEvent;
import com.sky.axon.demo.core.events.AccountRemovedEvent;
import com.sky.axon.demo.model.entity.Account;
import com.sky.axon.demo.repository.AccountMongodbDao;
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
        accountMongodbDao.save(account);
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
