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
