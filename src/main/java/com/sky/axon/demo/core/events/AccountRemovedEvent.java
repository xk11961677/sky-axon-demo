package com.sky.axon.demo.core.events;

import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

/**
 * 账号删除事件
 *
 * @author
 */
@Revision("1.0.0")
@NoArgsConstructor
public class AccountRemovedEvent extends BaseEvent<String> {

    public Integer disabled;

    public AccountRemovedEvent(String id, Integer disabled) {
        super(id);
        this.disabled = disabled;
    }
}
