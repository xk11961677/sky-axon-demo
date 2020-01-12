package com.sky.axon.demo.core.aggregates;

import com.sky.axon.demo.core.commands.CreateAccountCommand;
import com.sky.axon.demo.core.commands.ModifyAccountCommand;
import com.sky.axon.demo.core.commands.RemoveAccountCommand;
import com.sky.axon.demo.core.events.AccountCreatedEvent;
import com.sky.axon.demo.core.events.AccountModifiedEvent;
import com.sky.axon.demo.core.events.AccountRemovedEvent;
import com.sky.axon.demo.model.dto.commands.AddressDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.common.DefaultIdentifierFactory;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.List;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;
import static org.axonframework.modelling.command.AggregateLifecycle.markDeleted;

/**
 * 删除, markDeleted();
 *
 * @author
 */
@Aggregate(snapshotTriggerDefinition = "customSnapshotTriggerDefinition")
//@Aggregate(repository = "accountAggregateRepository")
@Slf4j
@Data
public class AccountAggregate {

    @AggregateIdentifier
    private String id;

    private String accountBalance;

    private String currency;

    private List<AddressDTO> address;

    private Integer disabled;

    public AccountAggregate() {
    }

    @CommandHandler
    public AccountAggregate(CreateAccountCommand createAccountCommand) {
        //axon默认使用java util uuid
        //String identifier = DefaultIdentifierFactory.getInstance().generateIdentifier();
        //log.info("accountAggregate identifier:{}", identifier);
        apply(new AccountCreatedEvent(createAccountCommand.id, createAccountCommand.accountBalance, createAccountCommand.currency, createAccountCommand.address, 0));
    }

    /**
     * 修改账号发送事件
     *
     * @param modifyAccountCommand
     */
    public void modifyAccount(ModifyAccountCommand modifyAccountCommand) {
        apply(new AccountModifiedEvent(modifyAccountCommand.id, modifyAccountCommand.accountBalance, modifyAccountCommand.currency, modifyAccountCommand.address));
    }

    public void removeAccount(RemoveAccountCommand removeAccountCommand) {
        apply(new AccountRemovedEvent(removeAccountCommand.id, 1));
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
        log.info("====================>>EventSourcingHandler AccountCreatedEvent :{}", accountCreatedEvent);
    }

    @EventSourcingHandler
    protected void on(AccountModifiedEvent accountModifiedEvent) {
        this.id = accountModifiedEvent.id;
        this.currency = accountModifiedEvent.currency;
        this.accountBalance = accountModifiedEvent.accountBalance;
        this.address = accountModifiedEvent.address;
    }

    @EventSourcingHandler
    protected void on(AccountRemovedEvent accountRemovedEvent) {
        this.id = accountRemovedEvent.id;
        this.disabled = accountRemovedEvent.disabled;
        markDeleted();
    }
}
