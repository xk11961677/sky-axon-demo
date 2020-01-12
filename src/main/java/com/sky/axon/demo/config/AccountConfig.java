//package com.sky.axon.demo.config;
//
//import com.sky.axon.demo.core.aggregates.AccountAggregate;
//import org.axonframework.common.transaction.TransactionManager;
//import org.axonframework.eventsourcing.AggregateFactory;
//import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
//import org.axonframework.eventsourcing.EventSourcingRepository;
//import org.axonframework.eventsourcing.Snapshotter;
//import org.axonframework.eventsourcing.eventstore.EventStore;
//import org.axonframework.messaging.annotation.ParameterResolverFactory;
//import org.axonframework.spring.eventsourcing.SpringAggregateSnapshotter;
//import org.axonframework.spring.eventsourcing.SpringAggregateSnapshotterFactoryBean;
//import org.axonframework.spring.eventsourcing.SpringPrototypeAggregateFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//
///**
// * @author
// */
//@Configuration
//public class AccountConfig {
//
//    @Bean
//    public SpringAggregateSnapshotterFactoryBean springAggregateSnapshotterFactoryBean() {
//        return new SpringAggregateSnapshotterFactoryBean();
//    }
//
//    @Bean
//    public SpringAggregateSnapshotter snapshotter(ParameterResolverFactory parameterResolverFactory,
//                                                  EventStore eventStore,
//                                                  TransactionManager transactionManager) {
//        Executor executor = Executors.newFixedThreadPool(10);
//        return SpringAggregateSnapshotter.builder()
//                .eventStore(eventStore)
//                .parameterResolverFactory(parameterResolverFactory)
//                .executor(executor)
//                .transactionManager(transactionManager)
//                .build();
//    }
//
//    @Bean("accountAggregateRepository")
//    public EventSourcingRepository<AccountAggregate> accountRepository(Snapshotter snapshotter,
//                                                                       EventStore eventStore,
//                                                                       ParameterResolverFactory parameterResolverFactory) {
//
//        return EventSourcingRepository.builder(AccountAggregate.class)
//                .eventStore(eventStore)
//                .aggregateFactory(accountAggregateFactory())
//                .parameterResolverFactory(parameterResolverFactory)
//                .snapshotTriggerDefinition(new EventCountSnapshotTriggerDefinition(snapshotter, 2))
//                .build();
//    }
//
//    @Bean(name = "accountAggregateFactory")
//    public AggregateFactory<AccountAggregate> accountAggregateFactory() {
//        return new SpringPrototypeAggregateFactory<>("accountAggregate");
//    }
//}
