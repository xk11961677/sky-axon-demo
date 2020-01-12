package com.sky.axon.demo.handler.command;

import com.sky.axon.demo.core.aggregates.AccountAggregate;
import com.sky.axon.demo.core.commands.CreateAccountCommand;
import com.sky.axon.demo.core.commands.ModifyAccountCommand;
import com.sky.axon.demo.core.commands.RemoveAccountCommand;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.Aggregate;
import org.axonframework.modelling.command.Repository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.Callable;

/**
 * @author
 */
@Component
@Slf4j
public class AccountCommandHandler {

    @Resource
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
    protected String handle(ModifyAccountCommand modifyAccountCommand) {
        Aggregate<AccountAggregate> aggregate = repository.load(modifyAccountCommand.id);
        aggregate.execute(accountAggregate -> {
            accountAggregate.modifyAccount(modifyAccountCommand);
        });
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
        aggregate.execute(accountAggregate -> {
            accountAggregate.removeAccount(removeAccountCommand);
        });
    }

}
