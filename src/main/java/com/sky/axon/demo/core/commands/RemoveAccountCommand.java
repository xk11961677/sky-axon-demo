package com.sky.axon.demo.core.commands;

/**
 * 删除账号
 *
 * @author
 */
public class RemoveAccountCommand extends BaseCommand<String> {

    public RemoveAccountCommand(String id) {
        super(id);
    }
}
