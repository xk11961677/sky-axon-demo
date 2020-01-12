package com.sky.axon.demo.config;

import org.axonframework.common.AxonConfigurationException;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.eventsourcing.AggregateSnapshotter;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.annotation.HandlerDefinition;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
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
public class CustomSpringAggregateSnapshotter extends AggregateSnapshotter implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    protected CustomSpringAggregateSnapshotter(Builder builder) {
        super(builder);
    }

    public static CustomSpringAggregateSnapshotter.Builder builder() {
        return new CustomSpringAggregateSnapshotter.Builder();
    }

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

    public static class Builder extends org.axonframework.eventsourcing.AggregateSnapshotter.Builder {
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
