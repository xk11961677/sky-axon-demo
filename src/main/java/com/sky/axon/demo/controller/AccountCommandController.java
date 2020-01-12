package com.sky.axon.demo.controller;

import com.sky.axon.demo.model.dto.commands.AccountDTO;
import com.sky.axon.demo.model.dto.commands.EventDTO;
import com.sky.axon.demo.service.command.AccountCommandService;
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
