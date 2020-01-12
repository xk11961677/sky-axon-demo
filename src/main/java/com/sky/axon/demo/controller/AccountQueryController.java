package com.sky.axon.demo.controller;

import com.sky.axon.demo.model.dto.query.AccountQueryDTO;
import com.sky.axon.demo.model.entity.Account;
import com.sky.axon.demo.service.query.AccountQueryService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author
 */
@RestController
@RequestMapping(value = "/bank-accounts")
@Api(value = "Account Queries", description = "Account Query Events Endpoint", tags = "Account Queries")
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
