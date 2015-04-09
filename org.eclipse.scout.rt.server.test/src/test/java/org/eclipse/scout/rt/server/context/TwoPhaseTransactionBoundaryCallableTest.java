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
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.server.transaction.ITransactionProvider;
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
public class TwoPhaseTransactionBoundaryCallableTest {

  private List<IBean<?>> m_beans;

  @Mock
  private ITransactionProvider m_transactionProvider;
  @Mock
  private ITransaction m_transaction;
  @Mock
  private ICallable<?> m_next;

  private List<Throwable> m_txErrors;

  @Before
  public void before() throws Exception {
    MockitoAnnotations.initMocks(this);

    m_txErrors = new ArrayList<>();
    m_beans = TestingUtility.registerBeans(new BeanMetaData(ITransactionProvider.class).initialInstance(m_transactionProvider).order(-1000));

    when(m_transactionProvider.provide(anyLong())).thenReturn(m_transaction);

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
  }

  @After
  public void after() {
    TestingUtility.unregisterBeans(m_beans);
    m_beans.clear();
    m_txErrors.clear();
  }

  @Test(expected = TransactionRequiredException.class)
  public void testMandatoryWithoutExistingTransaction() throws Exception {
    ITransaction.CURRENT.remove();
    new TwoPhaseTransactionBoundaryCallable<>(m_next, TransactionScope.MANDATORY, 1L).call();
  }

  @Test
  public void testMandatoryWithExistingTransactionAndSuccess() throws Exception {
    ICallable<?> callable = new TwoPhaseTransactionBoundaryCallable<>(m_next, TransactionScope.MANDATORY, 1L);
    ITransaction currentTransaction = mock(ITransaction.class);
    ITransaction.CURRENT.set(currentTransaction);

    // run the test
    callable.call();

    // verify
    verifyZeroInteractions(m_transactionProvider);
    verifyZeroInteractions(currentTransaction);
  }

  @Test
  public void testMandatoryWithExistingTransactionAndError() throws Exception {
    ICallable<?> callable = new TwoPhaseTransactionBoundaryCallable<>(m_next, TransactionScope.MANDATORY, 1L);
    ITransaction currentTransaction = mock(ITransaction.class);
    ITransaction.CURRENT.set(currentTransaction);
    when(m_next.call()).thenThrow(new RuntimeException());

    // run the test
    try {
      callable.call();
      fail();
    }
    catch (Exception e) {
      // verify
      verifyZeroInteractions(m_transactionProvider);

      verify(currentTransaction, never()).commitPhase1();
      verify(currentTransaction, never()).commitPhase2();
      verify(currentTransaction, never()).rollback();
      verify(currentTransaction, never()).release();
      verify(currentTransaction, times(1)).addFailure(any(Exception.class));
    }
  }

  @Test
  public void testRequiresNewWithoutExistingTransactionAndSuccess() throws Exception {
    ICallable<?> callable = new TwoPhaseTransactionBoundaryCallable<>(m_next, TransactionScope.REQUIRES_NEW, 1L);
    ITransaction.CURRENT.remove();

    // run the test
    callable.call();

    // verify
    verify(m_transactionProvider, times(1)).provide(eq(1L));
    verifyNoMoreInteractions(m_transactionProvider);

    InOrder inOrder = Mockito.inOrder(m_transaction);

    inOrder.verify(m_transaction, times(1)).commitPhase1();
    inOrder.verify(m_transaction, times(1)).commitPhase2();
    inOrder.verify(m_transaction, never()).rollback();
    inOrder.verify(m_transaction, times(1)).release();
  }

  @Test
  public void testRequiresNewWithoutExistingTransactionAndError() throws Exception {
    ICallable<?> callable = new TwoPhaseTransactionBoundaryCallable<>(m_next, TransactionScope.REQUIRES_NEW, 1L);
    ITransaction.CURRENT.remove();
    when(m_next.call()).thenThrow(new RuntimeException());

    // run the test
    try {
      callable.call();
      fail();
    }
    catch (Exception e) {

      // verify
      verify(m_transactionProvider, times(1)).provide(eq(1L));
      verifyNoMoreInteractions(m_transactionProvider);

      InOrder inOrder = Mockito.inOrder(m_transaction);

      inOrder.verify(m_transaction, never()).commitPhase1();
      inOrder.verify(m_transaction, never()).commitPhase2();
      inOrder.verify(m_transaction, times(1)).rollback();
      inOrder.verify(m_transaction, times(1)).release();
    }
  }

  @Test
  public void testRequiresNewWithExistingTransactionAndSuccess() throws Exception {
    ICallable<?> callable = new TwoPhaseTransactionBoundaryCallable<>(m_next, TransactionScope.REQUIRES_NEW, 1L);
    ITransaction currentTransaction = mock(ITransaction.class);
    ITransaction.CURRENT.set(currentTransaction);

    // run the test
    callable.call();

    // verify
    verifyZeroInteractions(currentTransaction);
    verify(m_transactionProvider, times(1)).provide(eq(1L));
    verifyNoMoreInteractions(m_transactionProvider);

    InOrder inOrder = Mockito.inOrder(m_transaction);

    inOrder.verify(m_transaction, times(1)).commitPhase1();
    inOrder.verify(m_transaction, times(1)).commitPhase2();
    inOrder.verify(m_transaction, never()).rollback();
    inOrder.verify(m_transaction, times(1)).release();
  }

  @Test
  public void testRequiresNewWithExistingTransactionAndError() throws Exception {
    ICallable<?> callable = new TwoPhaseTransactionBoundaryCallable<>(m_next, TransactionScope.REQUIRES_NEW, 1L);
    ITransaction currentTransaction = mock(ITransaction.class);
    ITransaction.CURRENT.set(currentTransaction);
    when(m_next.call()).thenThrow(new RuntimeException());

    // run the test
    try {
      callable.call();
      fail();
    }
    catch (Exception e) {
      // verify
      verifyZeroInteractions(currentTransaction);
      verify(m_transactionProvider, times(1)).provide(eq(1L));
      verifyNoMoreInteractions(m_transactionProvider);

      InOrder inOrder = Mockito.inOrder(m_transaction);

      inOrder.verify(m_transaction, never()).commitPhase1();
      inOrder.verify(m_transaction, never()).commitPhase2();
      inOrder.verify(m_transaction, times(1)).rollback();
      inOrder.verify(m_transaction, times(1)).release();
    }
  }

  @Test
  public void testRequiredWithoutExistingTransactionAndSuccess() throws Exception {
    ICallable<?> callable = new TwoPhaseTransactionBoundaryCallable<>(m_next, TransactionScope.REQUIRED, 1L);
    ITransaction.CURRENT.remove();

    // run the test
    callable.call();

    // verify
    verify(m_transactionProvider, times(1)).provide(eq(1L));
    verifyNoMoreInteractions(m_transactionProvider);

    InOrder inOrder = Mockito.inOrder(m_transaction);

    inOrder.verify(m_transaction, times(1)).commitPhase1();
    inOrder.verify(m_transaction, times(1)).commitPhase2();
    inOrder.verify(m_transaction, never()).rollback();
    inOrder.verify(m_transaction, times(1)).release();
  }

  @Test
  public void testRequiredWithoutExistingTransactionAndError() throws Exception {
    ICallable<?> callable = new TwoPhaseTransactionBoundaryCallable<>(m_next, TransactionScope.REQUIRED, 1L);
    ITransaction.CURRENT.remove();
    when(m_next.call()).thenThrow(new RuntimeException());

    // run the test
    try {
      callable.call();
      fail();
    }
    catch (Exception e) {

      // verify
      verify(m_transactionProvider, times(1)).provide(eq(1L));
      verifyNoMoreInteractions(m_transactionProvider);

      InOrder inOrder = Mockito.inOrder(m_transaction);

      inOrder.verify(m_transaction, never()).commitPhase1();
      inOrder.verify(m_transaction, never()).commitPhase2();
      inOrder.verify(m_transaction, times(1)).rollback();
      inOrder.verify(m_transaction, times(1)).release();
    }
  }

  @Test
  public void testRequiredWithExistingTransactionAndSuccess() throws Exception {
    ICallable<?> callable = new TwoPhaseTransactionBoundaryCallable<>(m_next, TransactionScope.REQUIRED, 1L);
    ITransaction currentTransaction = mock(ITransaction.class);
    ITransaction.CURRENT.set(currentTransaction);

    // run the test
    callable.call();

    // verify
    verifyZeroInteractions(m_transactionProvider);
    verifyZeroInteractions(currentTransaction);
  }

  @Test
  public void testRequiredWithExistingTransactionAndError() throws Exception {
    ICallable<?> callable = new TwoPhaseTransactionBoundaryCallable<>(m_next, TransactionScope.REQUIRED, 1L);
    ITransaction currentTransaction = mock(ITransaction.class);
    ITransaction.CURRENT.set(currentTransaction);
    when(m_next.call()).thenThrow(new RuntimeException());

    // run the test
    try {
      callable.call();
      fail();
    }
    catch (Exception e) {
      // verify
      verifyZeroInteractions(m_transactionProvider);

      verify(currentTransaction, never()).commitPhase1();
      verify(currentTransaction, never()).commitPhase2();
      verify(currentTransaction, never()).rollback();
      verify(currentTransaction, times(1)).addFailure(any(Throwable.class));
    }
  }
}
