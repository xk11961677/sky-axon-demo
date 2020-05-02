package com.sky.axon.command.core.aggregate;

import com.sky.axon.command.core.command.CreateAccountItemCommand;
import com.sky.axon.events.AccountCreatedItemEvent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

/**
 * @author
 */
@Aggregate(snapshotTriggerDefinition = "customSnapshotTriggerDefinition")
@Slf4j
@Data
public class AccountItemAggregate {

    @AggregateIdentifier
    private String id;

    private String test;

    private AccountItemAggregate() {
    }


    @CommandHandler
    private AccountItemAggregate(CreateAccountItemCommand createAccountItemCommand) {
        log.info("=====================>>:{}");
        apply(new AccountCreatedItemEvent(createAccountItemCommand.id, createAccountItemCommand.test));
    }


    @EventSourcingHandler
    public void handler(AccountCreatedItemEvent event) {
        this.id = event.id;
        this.test = event.getTest();
    }



}
