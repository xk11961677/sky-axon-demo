package com.sky.axon.demo.service.query;

import com.sky.axon.demo.model.dto.query.AccountQueryDTO;
import com.sky.axon.demo.model.entity.Account;

import java.util.List;

/**
 * @author
 */
public interface AccountQueryService {

    /**
     * 查询某一账号所有操作事件
     *
     * @param id
     * @return
     */
    List<Object> listEventsForAccount(String id);

    /**
     * 查询账号
     *
     * @param accountQueryDTO
     * @return
     */
    List<Account> findAccount(AccountQueryDTO accountQueryDTO);
}
