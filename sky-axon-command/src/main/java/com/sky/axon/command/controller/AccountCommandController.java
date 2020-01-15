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

import com.sky.axon.api.commands.AccountDTO;
import com.sky.axon.api.commands.EventDTO;
import com.sky.axon.command.service.AccountCommandService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author
 */
@RestController
@RequestMapping(value = "/bank-accounts")
@Api(value = "Account Commands", description = "Account Commands Related Endpoints", tags = "Account Commands")
public class AccountCommandController {

    @Resource
    private AccountCommandService accountCommandService;

    /**
     * 创建账号
     *
     * @param accountDTO
     * @return
     */
    @PostMapping("/createAccount")
    public String createAccount(@RequestBody AccountDTO accountDTO) {
        return accountCommandService.createAccount(accountDTO);
    }

    @PostMapping(value = "/modifyAccount")
    public String modifyAccount(@RequestBody AccountDTO accountDTO) {
        return accountCommandService.modifyAccount(accountDTO);
    }

    @PostMapping(value = "/removeAccount")
    public boolean removeAccount(@RequestBody AccountDTO accountDTO) {
        accountCommandService.removeAccount(accountDTO.getId());
        return true;
    }

    @PostMapping(value = "/snapshotAccount")
    public boolean snapshotAccount(@RequestBody EventDTO eventDTO) {
        accountCommandService.snapshotAccount(eventDTO);
        return true;
    }
}
