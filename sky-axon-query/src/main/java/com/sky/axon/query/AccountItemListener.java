package com.sky.axon.query;

import com.sky.axon.events.AccountCreatedItemEvent;
import com.sky.axon.query.model.AccountItem;
import com.sky.axon.query.repository.AccountItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author
 */
@Component
@Slf4j
@ProcessingGroup("accountItemListener")
public class AccountItemListener {

    @Resource
    private AccountItemRepository accountItemRepository;


    @EventHandler
    public void onHandler(AccountCreatedItemEvent event) {
        log.info("save db");
        AccountItem accountItem = AccountItem.builder().id(event.id).test(event.getTest()).build();
        accountItemRepository.save(accountItem);
    }
}
