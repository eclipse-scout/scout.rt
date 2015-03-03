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
package org.eclipse.scout.rt.server.job.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.RunnableScheduledFuture;

import org.eclipse.scout.commons.job.internal.Futures;
import org.eclipse.scout.commons.job.internal.Futures.JobFuture;
import org.eclipse.scout.commons.job.internal.IProgressMonitorProvider;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ServerJobFutureTest {

  @Mock
  private RunnableScheduledFuture<Void> m_delegate;
  @Mock
  private ITransaction m_tx1;
  @Mock
  private ITransaction m_tx2;
  @Mock
  private ITransaction m_tx3;

  private JobFuture<Void> m_jobFuture;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);

    when(m_delegate.cancel(anyBoolean())).thenReturn(true);
    when(m_tx1.cancel()).thenReturn(true);
    when(m_tx2.cancel()).thenReturn(true);
    when(m_tx3.cancel()).thenReturn(true);

    m_jobFuture = Futures.jobFuture(m_delegate, ServerJobInput.empty(), mock(IProgressMonitorProvider.class));
  }

  @Test
  public void testCancelSuccess() {
    ServerJobFuture future = new ServerJobFuture<>(m_jobFuture);

    future.register(m_tx1);
    future.register(m_tx2);
    future.register(m_tx3);

    // Run the test
    assertTrue(future.cancel(true));

    // Verify
    InOrder inOrder = inOrder(m_delegate, m_tx1, m_tx2, m_tx3);

    inOrder.verify(m_tx1, times(1)).cancel();
    verifyNoMoreInteractions(m_tx1);

    inOrder.verify(m_tx2, times(1)).cancel();
    verifyNoMoreInteractions(m_tx2);

    inOrder.verify(m_tx3, times(1)).cancel();
    verifyNoMoreInteractions(m_tx3);

    inOrder.verify(m_delegate, times(1)).cancel(eq(true));
  }

  @Test
  public void testCancelFailed() {
    when(m_delegate.cancel(anyBoolean())).thenReturn(false);

    ServerJobFuture future = new ServerJobFuture<>(m_jobFuture);

    future.register(m_tx1);
    future.register(m_tx2);
    future.register(m_tx3);

    // Run the test
    assertFalse(future.cancel(true));

    // Verify
    InOrder inOrder = inOrder(m_delegate, m_tx1, m_tx2, m_tx3);

    inOrder.verify(m_tx1, times(1)).cancel();
    verifyNoMoreInteractions(m_tx1);

    inOrder.verify(m_tx2, times(1)).cancel();
    verifyNoMoreInteractions(m_tx2);

    inOrder.verify(m_tx3, times(1)).cancel();
    verifyNoMoreInteractions(m_tx3);

    inOrder.verify(m_delegate, times(1)).cancel(eq(true));
  }

  @Test
  public void testAlreadyCancelled() {
    when(m_delegate.isCancelled()).thenReturn(true);

    ServerJobFuture future = new ServerJobFuture<>(m_jobFuture);

    future.register(m_tx1);
    future.register(m_tx2);
    future.register(m_tx3);

    // Run the test
    assertFalse(future.cancel(true));

    // Verify
    verify(m_delegate, never()).cancel(eq(true));

    verifyZeroInteractions(m_tx1);
    verifyZeroInteractions(m_tx2);
    verifyZeroInteractions(m_tx3);
  }

  @Test
  public void testTxCancelFailed() {
    when(m_tx2.cancel()).thenReturn(false);

    ServerJobFuture future = new ServerJobFuture<>(m_jobFuture);

    future.register(m_tx1);
    future.register(m_tx2);
    future.register(m_tx3);

    // Run the test
    assertFalse(future.cancel(true));

    // Verify
    InOrder inOrder = inOrder(m_delegate, m_tx1, m_tx2, m_tx3);

    inOrder.verify(m_tx1, times(1)).cancel();
    verifyNoMoreInteractions(m_tx1);

    inOrder.verify(m_tx2, times(1)).cancel();
    verifyNoMoreInteractions(m_tx2);

    inOrder.verify(m_tx3, times(1)).cancel();
    verifyNoMoreInteractions(m_tx3);

    inOrder.verify(m_delegate, times(1)).cancel(eq(true));
  }

  @Test
  public void testTxCancelException() {
    when(m_tx2.cancel()).thenThrow(new RuntimeException("tx-cancel-exception"));

    ServerJobFuture future = new ServerJobFuture<>(m_jobFuture);

    future.register(m_tx1);
    future.register(m_tx2);
    future.register(m_tx3);

    // Run the test
    assertFalse(future.cancel(true));

    // Verify
    InOrder inOrder = inOrder(m_delegate, m_tx1, m_tx2, m_tx3);

    inOrder.verify(m_tx1, times(1)).cancel();
    verifyNoMoreInteractions(m_tx1);

    inOrder.verify(m_tx2, times(1)).cancel();
    verifyNoMoreInteractions(m_tx2);

    inOrder.verify(m_tx3, times(1)).cancel();
    verifyNoMoreInteractions(m_tx3);

    inOrder.verify(m_delegate, times(1)).cancel(eq(true));
  }
}
