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
package com.sky.axon.command.service;

import com.sky.axon.api.commands.AccountDTO;
import com.sky.axon.api.commands.EventDTO;
import com.sky.axon.command.core.aggregate.AccountAggregate;
import com.sky.axon.command.core.command.CreateAccountCommand;
import com.sky.axon.command.core.command.ModifyAccountCommand;
import com.sky.axon.command.core.command.RemoveAccountCommand;
import com.sky.axon.common.config.axon.CustomMongoEventStorageEngine;
import com.sky.axon.common.config.axon.CustomSpringAggregateSnapshotter;
import com.sky.axon.common.constant.AxonExtendConstants;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.messaging.MetaData;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author
 */
@Service
@Slf4j
public class AccountCommandServiceImpl implements AccountCommandService {

    @Resource
    private CommandGateway commandGateway;

    @Resource
    private QueryGateway queryGateway;

    @Resource
    private CustomMongoEventStorageEngine eventStore;

    @Resource
    private CustomSpringAggregateSnapshotter snapshotter;


    @Override
    public String createAccount(AccountDTO accountCreateDTO) throws ExecutionException, InterruptedException {
        String id = UUID.randomUUID().toString();
        /*SubscriptionQueryResult<String, Account> queryResult = queryGateway.subscriptionQuery(new AccountQueryDTO(id),
                ResponseTypes.instanceOf(String.class),
                ResponseTypes.instanceOf(Account.class)
        );*/
        CreateAccountCommand createAccountCommand = new CreateAccountCommand(id,
                accountCreateDTO.getStartingBalance(),
                accountCreateDTO.getCurrency(),
                accountCreateDTO.getAddress());
        CompletableFuture<String> result = commandGateway.send(createAccountCommand);
//        try {
//            Account account = queryResult.updates().blockFirst();
//            return account.getId();
//        } finally {
//            queryResult.close();
//        }
        log.info("=========aaaaaaaaaaaa=======>>:{}", result.isCompletedExceptionally());
        return result.join();
    }

    @Override
    public String modifyAccount(AccountDTO accountCreateDTO) {
        ModifyAccountCommand modifyAccountCommand = new ModifyAccountCommand(accountCreateDTO.getId(),
                accountCreateDTO.getStartingBalance(),
                accountCreateDTO.getCurrency(),
                accountCreateDTO.getAddress(),
                accountCreateDTO.getReversion());
        commandGateway.send(modifyAccountCommand, (commandMessage, commandResultMessage) -> {
            int a = 1 / 0;
        });
        return "1";
    }

    @Override
    public void removeAccount(String id) {
        commandGateway.send(new RemoveAccountCommand(id));
    }

    @Override
    public void snapshotAccount(EventDTO eventDTO) {
        /*List<?> list = eventStore.readEvents(eventDTO.getId(), eventDTO.getSequenceNumber()).asStream().map(s -> s.getPayload()).collect(Collectors.toList());
        snapshotter.scheduleSnapshot(AccountAggregate.class, eventDTO.getId());*/

        DomainEventStream eventStream = eventStore.readEvents(eventDTO.getId(), eventDTO.getBeginSequenceNumber());
        long firstEventSequenceNumber = eventStream.peek().getSequenceNumber();
        List<? extends DomainEventMessage<?>> list = eventStream.asStream().filter(f -> {
            return (f.getSequenceNumber() >= eventDTO.getBeginSequenceNumber() && f.getSequenceNumber() <= eventDTO.getEndSequenceNumber());
        }).collect(Collectors.toList());

        log.info("======>>snapshotAccount event total :{}", list.size());

        DomainEventMessage snapshotEvent = snapshotter.createSnapshot(AccountAggregate.class, eventDTO.getId(), DomainEventStream.of(list));

        MetaData metaData = snapshotEvent.getMetaData();
        Map<String, Object> map = new HashMap<>();
        for (Iterator<String> iterator = metaData.keySet().iterator(); iterator.hasNext(); ) {
            map.put(iterator.next(), metaData.get(iterator.next()));
        }
        map.put(AxonExtendConstants.TAG, eventDTO.getTag());
        map.put(AxonExtendConstants.TENANT_CODE, "tenantCode_1");
        map.put(AxonExtendConstants.REVERSION, eventDTO.getReversion());
        DomainEventMessage domainEventMessage = snapshotEvent.withMetaData(map);
        if (snapshotEvent != null && snapshotEvent.getSequenceNumber() > firstEventSequenceNumber) {
            eventStore.storeSnapshot(domainEventMessage);
        }
    }
}
