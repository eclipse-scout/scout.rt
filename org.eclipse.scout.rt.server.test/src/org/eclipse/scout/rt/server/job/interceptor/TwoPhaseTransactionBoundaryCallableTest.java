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
package org.eclipse.scout.rt.server.job.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TwoPhaseTransactionBoundaryCallableTest {

  @Mock
  private ITransaction m_transaction;

  @Mock
  private Callable<String> m_next;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    IJob.CURRENT.set(mock(IJob.class));
  }

  @After
  public void after() {
    IJob.CURRENT.remove();
  }

  @Test
  public void testSuccess() throws Exception {
    when(m_next.call()).thenReturn("success");
    when(m_transaction.commitPhase1()).thenReturn(true);

    TwoPhaseTransactionBoundaryCallable<String> coordinator = new TwoPhaseTransactionBoundaryCallable<>(m_next, m_transaction);
    assertEquals("success", coordinator.call());

    InOrder inOrder = inOrder(m_transaction);

    inOrder.verify(m_transaction, times(1)).commitPhase1();
    inOrder.verify(m_transaction, times(1)).commitPhase2();
    inOrder.verify(m_transaction, never()).rollback();
    inOrder.verify(m_transaction, times(1)).release();
  }

  @Test
  public void testError_RuntimeException() throws Exception {
    RuntimeException re = new RuntimeException();
    when(m_next.call()).thenThrow(re);
    when(m_transaction.getFailures()).thenReturn(new Throwable[]{re});
    when(m_transaction.hasFailures()).thenReturn(true);

    TwoPhaseTransactionBoundaryCallable<String> coordinator = new TwoPhaseTransactionBoundaryCallable<>(m_next, m_transaction);
    try {
      coordinator.call();
      fail();
    }
    catch (Exception e) {
      assertSame(re, e);

      InOrder inOrder = inOrder(m_transaction);

      inOrder.verify(m_transaction, times(1)).addFailure(same(re));
      inOrder.verify(m_transaction, never()).commitPhase1();
      inOrder.verify(m_transaction, never()).commitPhase2();
      inOrder.verify(m_transaction, times(1)).rollback();
      inOrder.verify(m_transaction, times(1)).release();
    }
  }

  @Test
  public void testError_ProcessingExceptionWithCause() throws Exception {
    RuntimeException cause = new RuntimeException();
    ProcessingException pe = new ProcessingException("", cause);
    when(m_next.call()).thenThrow(pe);
    when(m_transaction.getFailures()).thenReturn(new Throwable[]{cause});
    when(m_transaction.hasFailures()).thenReturn(true);

    TwoPhaseTransactionBoundaryCallable<String> coordinator = new TwoPhaseTransactionBoundaryCallable<>(m_next, m_transaction);
    try {
      coordinator.call();
      fail();
    }
    catch (Exception e) {
      assertSame(pe, e);

      InOrder inOrder = inOrder(m_transaction);

      inOrder.verify(m_transaction, times(1)).addFailure(same(cause));
      inOrder.verify(m_transaction, never()).commitPhase1();
      inOrder.verify(m_transaction, never()).commitPhase2();
      inOrder.verify(m_transaction, times(1)).rollback();
      inOrder.verify(m_transaction, times(1)).release();
    }
  }

  @Test
  public void testError_ProcessingException() throws Exception {
    ProcessingException pe = new ProcessingException();
    when(m_next.call()).thenThrow(pe);
    when(m_transaction.getFailures()).thenReturn(new Throwable[]{pe});
    when(m_transaction.hasFailures()).thenReturn(true);

    TwoPhaseTransactionBoundaryCallable<String> coordinator = new TwoPhaseTransactionBoundaryCallable<>(m_next, m_transaction);
    try {
      coordinator.call();
      fail();
    }
    catch (Exception e) {
      assertSame(pe, e);

      InOrder inOrder = inOrder(m_transaction);

      inOrder.verify(m_transaction, times(1)).addFailure(same(pe));
      inOrder.verify(m_transaction, never()).commitPhase1();
      inOrder.verify(m_transaction, never()).commitPhase2();
      inOrder.verify(m_transaction, times(1)).rollback();
      inOrder.verify(m_transaction, times(1)).release();
    }
  }

  @Test
  public void testError_rollback_failure() throws Exception {
    ProcessingException pe = new ProcessingException();
    when(m_next.call()).thenThrow(pe);
    when(m_transaction.getFailures()).thenReturn(new Throwable[]{pe});
    when(m_transaction.hasFailures()).thenReturn(true);
    doThrow(new RuntimeException()).when(m_transaction).rollback();

    TwoPhaseTransactionBoundaryCallable<String> coordinator = new TwoPhaseTransactionBoundaryCallable<>(m_next, m_transaction);
    try {
      coordinator.call();
      fail();
    }
    catch (Exception e) {
      assertSame(pe, e);

      InOrder inOrder = inOrder(m_transaction);

      inOrder.verify(m_transaction, times(1)).addFailure(same(pe));
      inOrder.verify(m_transaction, never()).commitPhase1();
      inOrder.verify(m_transaction, never()).commitPhase2();
      inOrder.verify(m_transaction, times(1)).rollback();
      inOrder.verify(m_transaction, times(1)).release();
    }
  }

  @Test
  public void testSuccess_2pc_phase1_failsWithFalse() throws Exception {
    when(m_next.call()).thenReturn("success");
    when(m_transaction.commitPhase1()).thenReturn(false);

    TwoPhaseTransactionBoundaryCallable<String> coordinator = new TwoPhaseTransactionBoundaryCallable<>(m_next, m_transaction);
    assertEquals("success", coordinator.call());

    InOrder inOrder = inOrder(m_transaction);

    inOrder.verify(m_transaction, times(1)).commitPhase1();
    inOrder.verify(m_transaction, never()).commitPhase2();
    inOrder.verify(m_transaction, times(1)).rollback();
    inOrder.verify(m_transaction, times(1)).release();
  }

  @Test
  public void testSuccess_2pc_phase1_fails_withException() throws Exception {
    when(m_next.call()).thenReturn("success");
    when(m_transaction.commitPhase1()).thenThrow(new RuntimeException());

    TwoPhaseTransactionBoundaryCallable<String> coordinator = new TwoPhaseTransactionBoundaryCallable<>(m_next, m_transaction);
    assertEquals("success", coordinator.call());

    InOrder inOrder = inOrder(m_transaction);

    inOrder.verify(m_transaction, times(1)).commitPhase1();
    inOrder.verify(m_transaction, never()).commitPhase2();
    inOrder.verify(m_transaction, times(1)).rollback();
    inOrder.verify(m_transaction, times(1)).release();
  }

  @Test
  public void testSuccess_2pc_phase2_fails_withException() throws Exception {
    when(m_next.call()).thenReturn("success");
    when(m_transaction.commitPhase1()).thenReturn(true);
    doThrow(new RuntimeException()).when(m_transaction).commitPhase2();

    TwoPhaseTransactionBoundaryCallable<String> coordinator = new TwoPhaseTransactionBoundaryCallable<>(m_next, m_transaction);
    assertEquals("success", coordinator.call());

    InOrder inOrder = inOrder(m_transaction);

    inOrder.verify(m_transaction, times(1)).commitPhase1();
    inOrder.verify(m_transaction, times(1)).commitPhase2();
    inOrder.verify(m_transaction, times(1)).rollback();
    inOrder.verify(m_transaction, times(1)).release();
  }

  @Test
  public void testSuccess_release_withException() throws Exception {
    when(m_next.call()).thenReturn("success");
    when(m_transaction.commitPhase1()).thenReturn(true);
    doThrow(new RuntimeException()).when(m_transaction).release();

    TwoPhaseTransactionBoundaryCallable<String> coordinator = new TwoPhaseTransactionBoundaryCallable<>(m_next, m_transaction);
    assertEquals("success", coordinator.call());

    InOrder inOrder = inOrder(m_transaction);

    inOrder.verify(m_transaction, times(1)).commitPhase1();
    inOrder.verify(m_transaction, times(1)).commitPhase2();
    inOrder.verify(m_transaction, never()).rollback();
    inOrder.verify(m_transaction, times(1)).release();
  }
}
