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
