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
package com.sky.axon.command.core.aggregate;

import com.sky.axon.api.commands.AddressDTO;
import com.sky.axon.command.core.command.CreateAccountCommand;
import com.sky.axon.command.core.command.ModifyAccountCommand;
import com.sky.axon.command.core.command.RemoveAccountCommand;
import com.sky.axon.common.config.mongo.DataSourceContext;
import com.sky.axon.common.constant.AxonExtendConstants;
import com.sky.axon.events.AccountCreatedEvent;
import com.sky.axon.events.AccountModifiedEvent;
import com.sky.axon.events.AccountRemovedEvent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcedAggregate;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.modelling.command.ApplyMore;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

/**
 * 删除, markDeleted();
 *
 * @author
 */
@Aggregate(snapshotTriggerDefinition = "customSnapshotTriggerDefinition")
@Slf4j
@Data
@NoArgsConstructor
public class AccountAggregate {

    @AggregateIdentifier
    private String id;

    private String accountBalance;

    private String currency;

    private List<AddressDTO> address;

    private Integer disabled = 0;

    @AggregateMember
    private List<AccountItemAggregate> accountItemAggregates = new ArrayList<>();

    @CommandHandler
    public AccountAggregate(CreateAccountCommand createAccountCommand) {
        //axon默认使用java util uuid
        //String identifier = DefaultIdentifierFactory.getInstance().generateIdentifier();
        //log.info("accountAggregate identifier:{}", identifier);
        Map<String, String> map = new HashMap<>();
        //map.put(AxonExtendConstants.TAG, "tag_1");
        map.put(AxonExtendConstants.TENANT_CODE, "tenantCode_1");
        //map.put(AxonExtendConstants.REVERSION, "v1");
        //commandGateway.sendAndWait(new CreateAccountItemCommand(UUID.randomUUID().toString(),"test"));

        ApplyMore apply = apply(new AccountCreatedEvent(createAccountCommand.id, createAccountCommand.accountBalance, createAccountCommand.currency, createAccountCommand.address, 0), MetaData.from(map));
        EventSourcedAggregate aggregate = (EventSourcedAggregate) apply;
        Long version = aggregate.version();
        if (version == null) {
            version = 0L;
        }
//        log.info(""+version);
    }


    /**
     * 修改账号发送事件
     *
     * @param modifyAccountCommand
     */
    public void modifyAccount(ModifyAccountCommand modifyAccountCommand) {
        Map<String, String> map = new HashMap<>();
        //map.put(AxonExtendConstants.TAG, "tag_1");
        map.put(AxonExtendConstants.TENANT_CODE, "tenantCode_1");
        //map.put(AxonExtendConstants.REVERSION, modifyAccountCommand.reversion);
        String dataSource = DataSourceContext.getDataSource();
        log.info("accountAggregate dataSource  :{} ", dataSource);

        ApplyMore apply = apply(new AccountModifiedEvent(modifyAccountCommand.id, modifyAccountCommand.accountBalance, modifyAccountCommand.currency, modifyAccountCommand.address, 0), MetaData.from(map));
        EventSourcedAggregate aggregate = (EventSourcedAggregate) apply;
        Long version = aggregate.version();
//        log.info(""+version);
    }

    public void removeAccount(RemoveAccountCommand removeAccountCommand) {
        ApplyMore apply = apply(new AccountRemovedEvent(removeAccountCommand.id, 1));
        EventSourcedAggregate aggregate = (EventSourcedAggregate) apply;
        Long version = aggregate.version();
//        log.info(""+version);
    }


    /**
     * 溯源使用,将变化必须在此处写一遍
     *
     * @param accountCreatedEvent
     */
    @EventSourcingHandler
    protected void on(AccountCreatedEvent accountCreatedEvent) {
        this.id = accountCreatedEvent.id;
        this.accountBalance = accountCreatedEvent.accountBalance;
        this.currency = accountCreatedEvent.currency;
        this.address = accountCreatedEvent.address;
        this.disabled = accountCreatedEvent.disabled;
//        log.info("====================>>EventSourcingHandler AccountCreatedEvent :{}", accountCreatedEvent);
    }

    @EventSourcingHandler
    protected void on(AccountModifiedEvent accountModifiedEvent) {
        this.id = accountModifiedEvent.id;
        this.currency = accountModifiedEvent.currency;
        this.accountBalance = accountModifiedEvent.accountBalance;
        this.address = accountModifiedEvent.address;
        this.disabled = accountModifiedEvent.disabled;
//        log.info("====================>>EventSourcingHandler AccountModifiedEvent :{}", accountModifiedEvent);
    }

    @EventSourcingHandler
    protected void on(AccountRemovedEvent accountRemovedEvent) {
        this.id = accountRemovedEvent.id;
        this.disabled = accountRemovedEvent.disabled;
        /*markDeleted();*/
//        log.info("====================>>EventSourcingHandler AccountRemovedEvent :{}", accountRemovedEvent);
    }
}
