package com.mirror.transactionpropagation.service;

import com.mirror.transactionpropagation.exception.RollbackException;

/**
 * @author: mirror
 * @date: 2020/6/28 11:12
 * @description:
 */
public interface FooService {

    void insertRecord();

    void insertThenRollback() throws RollbackException;

    void invokeInsertThenRollback() throws RollbackException;

    void testRequiredHasException() throws RollbackException;

    void testRequiredNoException() throws RollbackException;

    void testSupportsHasException() throws RollbackException;

    void testSupportsNoException() throws RollbackException;

    void testMandatoryHasException() throws RollbackException;

    void testMandatoryNoException() throws RollbackException;

    void testRequiresNewHasException() throws RollbackException;

    void testRequiresNewNoException() throws RollbackException;

    void testNotSupportHasException() throws RollbackException;

    void testNotSupportNoException() throws RollbackException;

    void testNestedHasException() throws RollbackException;

    void testNestedNoException() throws RollbackException;


}
