package com.sky.axon.common.config.axon;

import com.sky.axon.common.config.mongo.DataSourceContext;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.SnapshotTrigger;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.axonframework.messaging.unitofwork.CurrentUnitOfWork;

import java.io.Serializable;

/**
 * @author
 */
@Slf4j
public class CustomEventCountSnapshotTriggerDefinition implements SnapshotTriggerDefinition {

    private final Snapshotter snapshotter;

    private final int threshold;

    /**
     * Initialized the SnapshotTriggerDefinition to threshold snapshots using the given {@code snapshotter}
     * when {@code threshold} events have been applied to an Aggregate instance
     *
     * @param snapshotter the snapshotter to notify when a snapshot needs to be taken
     * @param threshold   the number of events that will threshold the creation of a snapshot event
     */
    public CustomEventCountSnapshotTriggerDefinition(Snapshotter snapshotter, int threshold) {
        this.snapshotter = snapshotter;
        this.threshold = threshold;
    }

    @Override
    public SnapshotTrigger prepareTrigger(Class<?> aggregateType) {
        log.info("CustomEventCountSnapshotTriggerDefinition.prepareTrigger dataSource :{}", DataSourceContext.getDataSource());
        return new CustomEventCountSnapshotTriggerDefinition.EventCountSnapshotTrigger(snapshotter, aggregateType, threshold);
    }

    @Override
    public SnapshotTrigger reconfigure(Class<?> aggregateType, SnapshotTrigger trigger) {
        if (trigger instanceof CustomEventCountSnapshotTriggerDefinition.EventCountSnapshotTrigger) {
            ((CustomEventCountSnapshotTriggerDefinition.EventCountSnapshotTrigger) trigger).setSnapshotter(snapshotter);
            return trigger;
        }
        return new CustomEventCountSnapshotTriggerDefinition.EventCountSnapshotTrigger(snapshotter, aggregateType, threshold);
    }

    private static class EventCountSnapshotTrigger implements SnapshotTrigger, Serializable {

        private final Class<?> aggregateType;
        private final int threshold;

        private transient Snapshotter snapshotter;
        private int counter = 0;

        public EventCountSnapshotTrigger(Snapshotter snapshotter, Class<?> aggregateType, int threshold) {
            this.snapshotter = snapshotter;
            this.aggregateType = aggregateType;
            this.threshold = threshold;
        }

        @Override
        public void eventHandled(EventMessage<?> msg) {
            String dataSource = DataSourceContext.getDataSource();
            log.info("CustomEventCountSnapshotTriggerDefinition eventHandled :{}", dataSource);
            if (++counter >= threshold && msg instanceof DomainEventMessage) {
                if (CurrentUnitOfWork.isStarted()) {
                    CurrentUnitOfWork.get().onPrepareCommit(
                            u -> scheduleSnapshot((DomainEventMessage) msg));
                } else {
                    scheduleSnapshot((DomainEventMessage) msg);
                }
                counter = 0;
            }
        }

        protected void scheduleSnapshot(DomainEventMessage msg) {
            String dataSource = DataSourceContext.getDataSource();
            long id = Thread.currentThread().getId();

            log.info("CustomEventCountSnapshotTriggerDefinition scheduleSnapshot :{} , threadId:{}", dataSource, id);

            ((CustomSpringAggregateSnapshotter) snapshotter).scheduleSnapshot(aggregateType, msg.getAggregateIdentifier(), dataSource);
            counter = 0;
        }

        @Override
        public void initializationFinished() {
        }

        public void setSnapshotter(Snapshotter snapshotter) {
            this.snapshotter = snapshotter;
        }
    }
}
