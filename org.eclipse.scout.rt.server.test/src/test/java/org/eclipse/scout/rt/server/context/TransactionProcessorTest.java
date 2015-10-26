/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.context;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.chain.InvocationChain.Chain;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanInstanceProducer;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.server.context.TransactionProcessor;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.server.transaction.TransactionRequiredException;
import org.eclipse.scout.rt.server.transaction.TransactionScope;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@RunWith(PlatformTestRunner.class)
public class TransactionProcessorTest {

  private List<IBean<?>> m_beans;

  @Mock
  private ITransaction m_transaction;
  @Mock
  private Chain<Object> m_chain;

  private List<Throwable> m_txErrors;

  @Before
  public void before() throws Exception {
    MockitoAnnotations.initMocks(this);

    m_txErrors = new ArrayList<>();
    m_beans = TestingUtility.registerBeans(new BeanMetaData(ITransaction.class).withOrder(-1000).withProducer(new IBeanInstanceProducer<ITransaction>() {
      @Override
      public ITransaction produce(IBean<ITransaction> bean) {
        return m_transaction;
      }
    }));

    // mock the transaction
    // ITransaction.commitPhase1
    when(m_transaction.commitPhase1()).thenReturn(true);

    // ITransaction.addFailure
    doAnswer(new Answer<Void>() {

      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        m_txErrors.add((Throwable) invocation.getArguments()[0]);
        return null;
      }
    }).when(m_transaction).addFailure(any(Throwable.class));

    // ITransaction.hasFailures
    doAnswer(new Answer<Boolean>() {

      @Override
      public Boolean answer(InvocationOnMock invocation) throws Throwable {
        return !m_txErrors.isEmpty();
      }
    }).when(m_transaction).hasFailures();

    RunMonitor.CURRENT.set(new RunMonitor());
  }

  @After
  public void after() {
    RunMonitor.CURRENT.remove();
    TestingUtility.unregisterBeans(m_beans);
    m_beans.clear();
    m_txErrors.clear();
  }

  @Test(expected = TransactionRequiredException.class)
  public void testMandatoryWithoutExistingTransaction() throws Exception {
    new TransactionProcessor<>(null, TransactionScope.MANDATORY).intercept(m_chain);
  }

  @Test
  public void testMandatoryWithExistingTransactionAndSuccess() throws Exception {
    ITransaction callingTransaction = mock(ITransaction.class);
    TransactionProcessor<Object> processor = new TransactionProcessor<>(callingTransaction, TransactionScope.MANDATORY);

    // run the test
    processor.intercept(m_chain);

    // verify
    verifyZeroInteractions(m_transaction);
    verifyZeroInteractions(callingTransaction);
  }

  @Test
  public void testMandatoryWithExistingTransactionAndError() throws Exception {
    ITransaction callingTransaction = mock(ITransaction.class);
    TransactionProcessor<Object> processor = new TransactionProcessor<>(callingTransaction, TransactionScope.MANDATORY);

    when(m_chain.continueChain()).thenThrow(new RuntimeException());

    // run the test
    try {
      processor.intercept(m_chain);
      fail();
    }
    catch (Exception e) {
      // verify
      verifyZeroInteractions(m_transaction);

      verify(callingTransaction, never()).commitPhase1();
      verify(callingTransaction, never()).commitPhase2();
      verify(callingTransaction, never()).rollback();
      verify(callingTransaction, never()).release();
      verify(callingTransaction, times(1)).addFailure(any(Exception.class));
    }
  }

  @Test
  public void testRequiresNewWithoutExistingTransactionAndSuccess() throws Exception {
    TransactionProcessor<Object> processor = new TransactionProcessor<>(null, TransactionScope.REQUIRES_NEW);

    // run the test
    processor.intercept(m_chain);

    // verify
    verify(m_transaction, times(1)).release();

    InOrder inOrder = Mockito.inOrder(m_transaction);

    inOrder.verify(m_transaction, times(1)).commitPhase1();
    inOrder.verify(m_transaction, times(1)).commitPhase2();
    inOrder.verify(m_transaction, never()).rollback();
    inOrder.verify(m_transaction, times(1)).release();
  }

  @Test
  public void testRequiresNewWithoutExistingTransactionAndError() throws Exception {
    TransactionProcessor<Object> processor = new TransactionProcessor<>(null, TransactionScope.REQUIRES_NEW);
    when(m_chain.continueChain()).thenThrow(new RuntimeException());

    // run the test
    try {
      processor.intercept(m_chain);
      fail();
    }
    catch (Exception e) {

      // verify
      verify(m_transaction, times(1)).release();

      InOrder inOrder = Mockito.inOrder(m_transaction);

      inOrder.verify(m_transaction, never()).commitPhase1();
      inOrder.verify(m_transaction, never()).commitPhase2();
      inOrder.verify(m_transaction, times(1)).rollback();
      inOrder.verify(m_transaction, times(1)).release();
    }
  }

  @Test
  public void testRequiresNewWithExistingTransactionAndSuccess() throws Exception {
    ITransaction callingTransaction = mock(ITransaction.class);
    TransactionProcessor<Object> processor = new TransactionProcessor<>(callingTransaction, TransactionScope.REQUIRES_NEW);

    // run the test
    processor.intercept(m_chain);

    // verify
    verifyZeroInteractions(callingTransaction);
    verify(m_transaction, times(1)).release();

    InOrder inOrder = Mockito.inOrder(m_transaction);

    inOrder.verify(m_transaction, times(1)).commitPhase1();
    inOrder.verify(m_transaction, times(1)).commitPhase2();
    inOrder.verify(m_transaction, never()).rollback();
    inOrder.verify(m_transaction, times(1)).release();
  }

  @Test
  public void testRequiresNewWithExistingTransactionAndError() throws Exception {
    ITransaction callingTransaction = mock(ITransaction.class);
    TransactionProcessor<Object> processor = new TransactionProcessor<>(callingTransaction, TransactionScope.REQUIRES_NEW);
    when(m_chain.continueChain()).thenThrow(new RuntimeException());

    // run the test
    try {
      processor.intercept(m_chain);
      fail();
    }
    catch (Exception e) {
      // verify
      verifyZeroInteractions(callingTransaction);
      verify(m_transaction, times(1)).release();

      InOrder inOrder = Mockito.inOrder(m_transaction);

      inOrder.verify(m_transaction, never()).commitPhase1();
      inOrder.verify(m_transaction, never()).commitPhase2();
      inOrder.verify(m_transaction, times(1)).rollback();
      inOrder.verify(m_transaction, times(1)).release();
    }
  }

  @Test
  public void testRequiredWithoutExistingTransactionAndSuccess() throws Exception {
    TransactionProcessor<Object> processor = new TransactionProcessor<>(null, TransactionScope.REQUIRED);

    // run the test
    processor.intercept(m_chain);

    // verify
    verify(m_transaction, times(1)).release();

    InOrder inOrder = Mockito.inOrder(m_transaction);

    inOrder.verify(m_transaction, times(1)).commitPhase1();
    inOrder.verify(m_transaction, times(1)).commitPhase2();
    inOrder.verify(m_transaction, never()).rollback();
    inOrder.verify(m_transaction, times(1)).release();
  }

  @Test
  public void testRequiredWithoutExistingTransactionAndError() throws Exception {
    TransactionProcessor<Object> processor = new TransactionProcessor<>(null, TransactionScope.REQUIRED);
    when(m_chain.continueChain()).thenThrow(new RuntimeException());

    // run the test
    try {
      processor.intercept(m_chain);
      fail();
    }
    catch (Exception e) {

      // verify
      verify(m_transaction, times(1)).release();

      InOrder inOrder = Mockito.inOrder(m_transaction);

      inOrder.verify(m_transaction, never()).commitPhase1();
      inOrder.verify(m_transaction, never()).commitPhase2();
      inOrder.verify(m_transaction, times(1)).rollback();
      inOrder.verify(m_transaction, times(1)).release();
    }
  }

  @Test
  public void testRequiredWithExistingTransactionAndSuccess() throws Exception {
    ITransaction callingTransaction = mock(ITransaction.class);
    TransactionProcessor<Object> processor = new TransactionProcessor<>(callingTransaction, TransactionScope.REQUIRED);

    // run the test
    processor.intercept(m_chain);

    // verify
    verifyZeroInteractions(m_transaction);
    verifyZeroInteractions(callingTransaction);
  }

  @Test
  public void testRequiredWithExistingTransactionAndError() throws Exception {
    ITransaction callingTransaction = mock(ITransaction.class);
    TransactionProcessor<Object> processor = new TransactionProcessor<>(callingTransaction, TransactionScope.REQUIRED);
    when(m_chain.continueChain()).thenThrow(new RuntimeException());

    // run the test
    try {
      processor.intercept(m_chain);
      fail();
    }
    catch (Exception e) {
      // verify
      verifyZeroInteractions(m_transaction);

      verify(callingTransaction, never()).commitPhase1();
      verify(callingTransaction, never()).commitPhase2();
      verify(callingTransaction, never()).rollback();
      verify(callingTransaction, times(1)).addFailure(any(Throwable.class));
    }
  }
}
