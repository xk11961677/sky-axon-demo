package com.sky.axon.demo.core.commands;


import org.axonframework.modelling.command.TargetAggregateIdentifier;

/**
 * @param <T>
 * @author
 */
public class BaseCommand<T> {

    @TargetAggregateIdentifier
    public final T id;

    public BaseCommand(T id) {
        this.id = id;
    }
}
