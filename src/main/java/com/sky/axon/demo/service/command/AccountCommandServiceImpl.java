package com.sky.axon.demo.service.command;

import com.sky.axon.demo.config.CustomSpringAggregateSnapshotter;
import com.sky.axon.demo.core.aggregates.AccountAggregate;
import com.sky.axon.demo.core.commands.CreateAccountCommand;
import com.sky.axon.demo.core.commands.ModifyAccountCommand;
import com.sky.axon.demo.core.commands.RemoveAccountCommand;
import com.sky.axon.demo.model.dto.commands.AccountDTO;
import com.sky.axon.demo.model.dto.commands.EventDTO;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventsourcing.AbstractSnapshotter;
import org.axonframework.eventsourcing.Snapshotter;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.spring.eventsourcing.SpringAggregateSnapshotter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
    private EventStore eventStore;

    @Resource
    private CustomSpringAggregateSnapshotter snapshotter;

    @Override
    public String createAccount(AccountDTO accountCreateDTO) {
        CreateAccountCommand createAccountCommand = new CreateAccountCommand(UUID.randomUUID().toString(),
                accountCreateDTO.getStartingBalance(),
                accountCreateDTO.getCurrency(),
                accountCreateDTO.getAddress());
        CompletableFuture<String> result = commandGateway.send(createAccountCommand);
        return result.join();
    }

    @Override
    public String modifyAccount(AccountDTO accountCreateDTO) {
        ModifyAccountCommand modifyAccountCommand = new ModifyAccountCommand(accountCreateDTO.getId(),
                accountCreateDTO.getStartingBalance(),
                accountCreateDTO.getCurrency(),
                accountCreateDTO.getAddress());
        CompletableFuture<String> result = commandGateway.send(modifyAccountCommand);
        return result.join();
    }

    @Override
    public void removeAccount(String id) {
        commandGateway.send(new RemoveAccountCommand(id));
    }

    @Override
    public void snapshotAccount(EventDTO eventDTO) {
        //List<?> list = eventStore.readEvents(eventDTO.getId(), eventDTO.getSequenceNumber()).asStream().map(s -> s.getPayload()).collect(Collectors.toList());
//        snapshotter.scheduleSnapshot(AccountAggregate.class,eventDTO.getId());

        DomainEventStream eventStream = eventStore.readEvents(eventDTO.getId(), eventDTO.getBeginSequenceNumber());
        long firstEventSequenceNumber = eventStream.peek().getSequenceNumber();
        List<? extends DomainEventMessage<?>> list = eventStream.asStream().filter(f -> {
            return (f.getSequenceNumber() >= eventDTO.getBeginSequenceNumber() && f.getSequenceNumber() <= eventDTO.getEndSequenceNumber());
        }).collect(Collectors.toList());

        log.info("======>>snapshotAccount event total :{}", list.size());

        DomainEventMessage snapshotEvent = snapshotter.createSnapshot(AccountAggregate.class, eventDTO.getId(), DomainEventStream.of(list));
        if (snapshotEvent != null && snapshotEvent.getSequenceNumber() > firstEventSequenceNumber) {
            eventStore.storeSnapshot(snapshotEvent);
        }
    }
}
