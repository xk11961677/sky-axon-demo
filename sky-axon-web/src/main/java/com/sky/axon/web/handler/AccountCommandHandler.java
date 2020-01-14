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
package com.sky.axon.web.handler;

import com.sky.axon.web.core.aggregate.AccountAggregate;
import com.sky.axon.web.core.command.ModifyAccountCommand;
import com.sky.axon.web.core.command.RemoveAccountCommand;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.Aggregate;
import org.axonframework.modelling.command.Repository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author
 */
@Component
@Slf4j
public class AccountCommandHandler {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Resource(name = "accountAggregateRepository")
    private Repository<AccountAggregate> repository;


    /**
     * 新增账号信息
     *
     * @param createAccountCommand
     */
    /*@CommandHandler
    protected String handle(CreateAccountCommand createAccountCommand, MetaData metaData) throws Exception {
        return repository.newInstance((Callable<AccountAggregate>) new AccountAggregate(createAccountCommand))
                .identifierAsString();
    }*/

    /**
     * 修改账号信息
     *
     * @param modifyAccountCommand
     */
    @CommandHandler
    protected String handle(ModifyAccountCommand modifyAccountCommand, MetaData metaData) {

        Aggregate<AccountAggregate> aggregate = repository.load(modifyAccountCommand.id);
        aggregate.execute(accountAggregate -> accountAggregate.modifyAccount(modifyAccountCommand));
        return aggregate.identifierAsString();
    }


    /**
     * 删除账号信息
     *
     * @param removeAccountCommand
     */
    @CommandHandler
    protected void handle(RemoveAccountCommand removeAccountCommand) {
        Aggregate<AccountAggregate> aggregate = repository.load(removeAccountCommand.id);
        aggregate.execute(accountAggregate -> accountAggregate.removeAccount(removeAccountCommand));
    }

}
