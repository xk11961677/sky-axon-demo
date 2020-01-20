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
package com.sky.axon.command.controller;

import com.sky.axon.api.query.AccountQueryDTO;
import com.sky.axon.query.model.Account;
import com.sky.axon.query.service.AccountQueryService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author
 */
@RestController
@RequestMapping(value = "/bank-accounts")
@Api(value = "Account Queries", tags = "Account Queries")
public class AccountQueryController {

    @Resource
    private AccountQueryService accountQueryService;


    @GetMapping("/findEvents")
    public List<Object> findEvents(@RequestParam("id") String id) {
        return accountQueryService.listEventsForAccount(id);
    }

    @GetMapping("/findAccount")
    public List<Account> findAccount(@RequestBody AccountQueryDTO accountQueryDTO) {
        return accountQueryService.findAccount(accountQueryDTO);
    }
}