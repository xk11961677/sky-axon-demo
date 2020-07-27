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
package com.sky.axon.common.config.axon;

import com.sky.axon.common.util.DataSourceContext;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.AxonConfigurationException;
import org.axonframework.common.transaction.NoTransactionManager;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.eventsourcing.AggregateSnapshotter;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.annotation.HandlerDefinition;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.modelling.command.ConcurrencyException;
import org.axonframework.modelling.command.RepositoryProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * @author
 */
@Slf4j
public class CustomSpringAggregateSnapshotter extends AggregateSnapshotter implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    protected CustomSpringAggregateSnapshotter(Builder builder) {
        super(builder);
    }

    public static CustomSpringAggregateSnapshotter.Builder builder() {
        return new CustomSpringAggregateSnapshotter.Builder();
    }

//    public void scheduleSnapshot(Class<?> aggregateType, String aggregateIdentifier, String dataSource) {
//        String dataSource1 = DataSourceContext.getDataSource();
//        log.info("custom snapshot datasource :{}" + dataSource1 + "==" + dataSource);
//        DataSourceContext.setDataSource(dataSource);
//        this.scheduleSnapshot(aggregateType, aggregateIdentifier);
//    }

    @Override
    public void scheduleSnapshot(Class<?> aggregateType, String aggregateIdentifier) {
        String dataSource = DataSourceContext.getDataSource();
        log.info("CustomSpringAggregateSnapshotter.scheduleSnapshot datasource :{}", dataSource);
        getExecutor().execute(new SilentTask(() -> {
            NoTransactionManager.INSTANCE.executeInTransaction(this.createSnapshotterTask(aggregateType, aggregateIdentifier));
        }, dataSource));
    }


//    /**
//     * Creates an instance of a task that contains the actual snapshot creation logic.
//     *
//     * @param aggregateType       The type of the aggregate to create a snapshot for
//     * @param aggregateIdentifier The identifier of the aggregate to create a snapshot for
//     * @return the task containing snapshot creation logic
//     */
//    @Override
//    protected Runnable createSnapshotterTask(Class<?> aggregateType, String aggregateIdentifier) {
//        String dataSource = DataSourceContext.getDataSource();
//        return new CreateSnapshotTask(aggregateType, aggregateIdentifier, dataSource);
//    }


    @Override
    protected AggregateFactory<?> getAggregateFactory(Class<?> aggregateType) {
        AggregateFactory<?> aggregateFactory = super.getAggregateFactory(aggregateType);
        if (aggregateFactory == null) {
            Optional<AggregateFactory> factory =
                    applicationContext.getBeansOfType(AggregateFactory.class).values().stream()
                            .filter(af -> Objects.equals(af.getAggregateType(), aggregateType))
                            .findFirst();
            if (!factory.isPresent()) {
                factory = applicationContext.getBeansOfType(EventSourcingRepository.class).values().stream()
                        .map((Function<EventSourcingRepository, AggregateFactory>) EventSourcingRepository::getAggregateFactory)
                        .filter(af -> Objects.equals(af.getAggregateType(), aggregateType))
                        .findFirst();
                if (factory.isPresent()) {
                    aggregateFactory = factory.get();
                    registerAggregateFactory(aggregateFactory);
                }
            }

            if (factory.isPresent()) {
                aggregateFactory = factory.get();
                registerAggregateFactory(aggregateFactory);
            }
        }
        return aggregateFactory;
    }

    @Override
    public DomainEventMessage createSnapshot(Class<?> aggregateType, String aggregateIdentifier, DomainEventStream eventStream) {
        return super.createSnapshot(aggregateType, aggregateIdentifier, eventStream);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private static class SilentTask implements Runnable {

        private final Runnable snapshotterTask;

        private final String dataSource;

        private SilentTask(Runnable snapshotterTask, String dataSource) {
            this.snapshotterTask = snapshotterTask;
            this.dataSource = dataSource;
        }

        @Override
        public void run() {
            try {
                DataSourceContext.setDataSource(dataSource);
                snapshotterTask.run();
            } catch (ConcurrencyException e) {
                log.info("An up-to-date snapshot entry already exists, ignoring this attempt.");
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.warn("An attempt to create and store a snapshot resulted in an exception:", e);
                } else {
                    log.warn("An attempt to create and store a snapshot resulted in an exception. " +
                            "Exception summary: {}", e.getMessage());
                }
            } finally {
                DataSourceContext.clearDataSource();
            }
        }
    }


    /*private final class CreateSnapshotTask implements Runnable {

        private final Class<?> aggregateType;
        private final String identifier;
        private final String dataSource;

        private CreateSnapshotTask(Class<?> aggregateType, String identifier, String dataSource) {
            this.aggregateType = aggregateType;
            this.identifier = identifier;
            this.dataSource = dataSource;
        }

        @Override
        public void run() {
            DataSourceContext.setDataSource(dataSource);
            log.info("CustomSrpingAggregateSnapshotter.CreateSnapshotTask datasource :{} " + dataSource);
            DomainEventStream eventStream = getEventStore().readEvents(identifier);
            // a snapshot should only be stored if the snapshot replaces at least more than one event
            long firstEventSequenceNumber = eventStream.peek().getSequenceNumber();
            DomainEventMessage snapshotEvent = createSnapshot(aggregateType, identifier, eventStream);
            if (snapshotEvent != null && snapshotEvent.getSequenceNumber() > firstEventSequenceNumber) {
                getEventStore().storeSnapshot(snapshotEvent);
            }
            DataSourceContext.clearDataSource();
        }
    }*/

    public static class Builder extends org.axonframework.eventsourcing.AggregateSnapshotter.Builder {

        public TransactionManager transactionManager;

        public Builder() {
            this.aggregateFactories(Collections.emptyList());
        }

        @Override
        public CustomSpringAggregateSnapshotter.Builder eventStore(EventStore eventStore) {
            super.eventStore(eventStore);
            return this;
        }

        @Override
        public CustomSpringAggregateSnapshotter.Builder executor(Executor executor) {
            super.executor(executor);
            return this;
        }

        @Override
        public CustomSpringAggregateSnapshotter.Builder transactionManager(TransactionManager transactionManager) {
            super.transactionManager(transactionManager);
            return this;
        }

        @Override
        public CustomSpringAggregateSnapshotter.Builder repositoryProvider(RepositoryProvider repositoryProvider) {
            super.repositoryProvider(repositoryProvider);
            return this;
        }

        @Override
        public CustomSpringAggregateSnapshotter.Builder parameterResolverFactory(ParameterResolverFactory parameterResolverFactory) {
            super.parameterResolverFactory(parameterResolverFactory);
            return this;
        }

        @Override
        public CustomSpringAggregateSnapshotter.Builder handlerDefinition(HandlerDefinition handlerDefinition) {
            super.handlerDefinition(handlerDefinition);
            return this;
        }

        @Override
        public CustomSpringAggregateSnapshotter build() {
            return new CustomSpringAggregateSnapshotter(this);
        }

        @Override
        protected void validate() throws AxonConfigurationException {
            super.validate();
        }
    }
}
