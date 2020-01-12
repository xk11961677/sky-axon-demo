package com.sky.axon.demo.service.command;

import com.sky.axon.demo.model.dto.commands.AccountDTO;
import com.sky.axon.demo.model.dto.commands.EventDTO;

import java.util.concurrent.CompletableFuture;

/**
 * @author
 */
public interface AccountCommandService {

    /**
     * 创建账号
     *
     * @param accountCreateDTO
     * @return
     */
    String createAccount(AccountDTO accountCreateDTO);

    /**
     * 更新账号
     *
     * @param accountCreateDTO
     * @return
     */
    String modifyAccount(AccountDTO accountCreateDTO);

    /**
     * 删除账号
     *
     * @param id
     * @return
     */
    void removeAccount(String id);

    /**
     * 根据事件打快照
     *
     * @param eventDTO
     */
    void snapshotAccount(EventDTO eventDTO);

}
