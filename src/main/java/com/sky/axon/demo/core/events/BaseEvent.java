package com.sky.axon.demo.core.events;

/**
 * @param <T>
 * @author
 */
public class BaseEvent<T> {

    public T id;

    public BaseEvent() {
    }

    public BaseEvent(T id) {
        this.id = id;
    }
}
