package com.sky.axon.demo.service.query;

import com.sky.axon.demo.model.dto.query.AccountQueryDTO;
import com.sky.axon.demo.model.entity.Account;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author
 */
@Service
public class AccountQueryServiceImpl implements AccountQueryService {

    @Resource
    private EventStore eventStore;

    @Resource
    private QueryGateway queryGateway;

    @Override
    public List<Object> listEventsForAccount(String id) {
        return eventStore.readEvents(id).asStream().map(s -> s.getPayload()).collect(Collectors.toList());
    }

    @Override
    public List<Account> findAccount(AccountQueryDTO accountQueryDTO) {
        return queryGateway.query(accountQueryDTO, ResponseTypes.multipleInstancesOf(Account.class)).join();
    }
}
