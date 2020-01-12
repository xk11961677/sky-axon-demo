package com.sky.axon.demo.repository;

import com.sky.axon.demo.repository.base.AbstractBaseRepository;
import com.sky.axon.demo.model.entity.Account;
import org.springframework.stereotype.Repository;

/**
 * @author
 */
@Repository("accountMongodbDao")
public class AccountMongodbDaoImpl extends AbstractBaseRepository<Account> implements AccountMongodbDao {
}
