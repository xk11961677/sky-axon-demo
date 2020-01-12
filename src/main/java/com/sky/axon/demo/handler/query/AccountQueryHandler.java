package com.sky.axon.demo.handler.query;

import com.sky.axon.demo.model.dto.query.AccountQueryDTO;
import com.sky.axon.demo.model.entity.Account;
import com.sky.axon.demo.repository.AccountMongodbDao;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 */
@Component
@Slf4j
public class AccountQueryHandler {

    @Resource
    private AccountMongodbDao accountMongodbDao;

    /**
     * 处理查询视图
     *
     * @param accountQueryDTO
     * @return
     */
    @QueryHandler
    public List<Account> handle(AccountQueryDTO accountQueryDTO) {
        log.info("AccountQueryHandler AccountQueryDTO :{}", accountQueryDTO);
        Account account = accountMongodbDao.findById(accountQueryDTO.getId());
        List<Account> list = new ArrayList<>();
        list.add(account);
        return list;
    }

}
