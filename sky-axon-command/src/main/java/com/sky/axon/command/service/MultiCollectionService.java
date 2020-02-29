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
package com.sky.axon.command.service;

import com.sky.axon.query.model.AccountTestOne;
import com.sky.axon.query.model.AccountTestTwo;
import com.sky.axon.query.repository.AccountTestOneRepository;
import com.sky.axon.query.repository.AccountTestTwoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 测试mongo 4.0多文档事物
 *
 * @author
 */
@Service
public class MultiCollectionService {

    @Autowired
    private AccountTestOneRepository accountTestOneRepository;

    @Autowired
    private AccountTestTwoRepository accountTestTwoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Transactional
    public void add() {
        AccountTestOne one = AccountTestOne.builder().accountBalance("123").build();
        AccountTestTwo two = AccountTestTwo.builder().accountBalance("456").build();
//        accountTestOneRepository.save(one);
//        accountTestTwoRepository.save(two);
        mongoTemplate.save(one);
        mongoTemplate.save(two);
//        int i = 1 / 0;
    }
}
