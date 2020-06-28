package com.mirror.transactionpropagation.service.impl;

import com.mirror.transactionpropagation.common.Global;
import com.mirror.transactionpropagation.exception.RollbackException;
import com.mirror.transactionpropagation.service.BarService;
import com.mirror.transactionpropagation.service.FooService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: mirror
 * @date: 2020/6/28 11:19
 * @description:
 */
@Service
public class BarServiceImpl implements BarService {

    @Autowired
    private FooService fooService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 有事务
     *
     * @throws RollbackException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hasTransactional() throws RollbackException {

        // REQUIRED传播属性-调用者有事务,抛异常 被调用者无异常
        fooService.testRequiredNoException();

        // SUPPORT传播属性-调用者有事务,抛异常 被调用者无异常
        //fooService.testSupportsNoException();

        // MANDATORY传播属性-调用者有事务,抛异常 被调用者无异常
        //fooService.testMandatoryNoException();

        // REQUIRES_NEW传播属性-调用者有事务,抛异常 被调用者无异常
        //fooService.testRequiresNewNoException();

        // NOT_SUPPORTED传播属性-调用者有事务,抛异常 被调用者无异常
        //fooService.testNotSupportNoException();

        // NESTED传播属性-调用者有事务,抛异常 被调用者无异常
        //fooService.testNestedNoException();

        throw new RollbackException();

    }

    /**
     * 无事务
     *
     * @throws RollbackException
     */
    @Override
    public void noTransactional() throws RollbackException {

        // REQUIRED传播属性-调用者无事务,不抛异常 被调用者有异常
        fooService.testRequiredHasException();

        // SUPPORT传播属性-调用者无事务,不抛异常 被调用者有异常
        //fooService.testSupportsHasException();

        // MANDATORY传播属性-调用者无事务,不抛异常 被调用者有异常
        //fooService.testMandatoryHasException();

        // REQUIRES_NEW传播属性-调用者无事务,不抛异常 被调用者有异常
        //fooService.testRequiresNewHasException();

        // NOT_SUPPORTED传播属性-调用者无事务,不抛异常 被调用者有异常
        //fooService.testNotSupportHasException();

        // NESTED传播属性-调用者无事务,不抛异常 被调用者有异常
        //fooService.testNestedHasException();

    }

    @Override
    @Transactional()
    public void hasTransactionalNoException() throws RollbackException {
        // NESTED传播属性-调用者有事务,不抛异常 被调用者有异常
        jdbcTemplate.execute("INSERT INTO FOO (BAR) VALUES ('" + Global.NESTED_HAS_EXCEPTION_TWO + "')");
        fooService.testNestedHasException();
    }

}
