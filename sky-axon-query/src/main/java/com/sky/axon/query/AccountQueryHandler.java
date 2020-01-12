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
package com.sky.axon.query;

import com.sky.axon.api.query.AccountQueryDTO;
import com.sky.axon.query.model.Account;
import com.sky.axon.query.repository.AccountMongodbDao;
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
