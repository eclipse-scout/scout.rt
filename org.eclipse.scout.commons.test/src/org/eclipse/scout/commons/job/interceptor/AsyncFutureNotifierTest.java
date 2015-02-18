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
package org.eclipse.scout.commons.job.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IAsyncFuture;
import org.eclipse.scout.commons.job.IJob;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class AsyncFutureNotifierTest {

  private IAsyncFuture<String> m_asyncFuture;
  private Callable<String> m_next;
  private ArgumentCaptor<ProcessingException> m_errorExceptionCaptor;
  private ArgumentCaptor<ProcessingException> m_doneExceptionCaptor;

  @SuppressWarnings("unchecked")
  @Before
  public void before() {
    m_asyncFuture = mock(IAsyncFuture.class);
    m_next = mock(Callable.class);

    m_errorExceptionCaptor = ArgumentCaptor.forClass(ProcessingException.class);
    m_doneExceptionCaptor = ArgumentCaptor.forClass(ProcessingException.class);
    doNothing().when(m_asyncFuture).onError(m_errorExceptionCaptor.capture());
    doNothing().when(m_asyncFuture).onDone(anyString(), m_doneExceptionCaptor.capture());

    IJob.CURRENT.set(mock(IJob.class));
  }

  @After
  public void after() {
    IJob.CURRENT.remove();
  }

  @Test
  public void testSuccess1() throws Exception {
    when(m_next.call()).thenReturn("ABC");

    AsyncFutureNotifier<String> notifier = new AsyncFutureNotifier<>(m_next, m_asyncFuture);

    assertEquals("ABC", notifier.call());
    verify(m_asyncFuture, times(1)).onSuccess(eq("ABC"));
    verify(m_asyncFuture, never()).onError(any(ProcessingException.class));
    verify(m_asyncFuture, times(1)).onDone(eq("ABC"), isNull(ProcessingException.class));
  }

  @Test
  public void testSuccess2() throws Exception {
    when(m_next.call()).thenReturn("ABC");

    AsyncFutureNotifier<String> notifier = new AsyncFutureNotifier<>(m_next, null);

    assertEquals("ABC", notifier.call());
  }

  @Test
  public void testErrorRuntimeExceptionWithAsyncFuture() throws Exception {
    RuntimeException exception = new RuntimeException();
    when(m_next.call()).thenThrow(exception);

    AsyncFutureNotifier<String> notifier = new AsyncFutureNotifier<>(m_next, m_asyncFuture);

    try {
      notifier.call();
      fail();
    }
    catch (ProcessingException e) {
      verify(m_asyncFuture, never()).onSuccess(anyString());
      verify(m_asyncFuture, times(1)).onError(any(ProcessingException.class));
      verify(m_asyncFuture, times(1)).onDone(isNull(String.class), any(ProcessingException.class));

      ProcessingException pe = m_errorExceptionCaptor.getValue();
      assertSame(exception, pe.getCause());
      assertSame(pe, e);

      pe = m_doneExceptionCaptor.getValue();
      assertSame(exception, pe.getCause());
      assertSame(pe, e);
    }
  }

  @Test
  public void testErrorRuntimeException() throws Exception {
    RuntimeException exception = new RuntimeException();
    when(m_next.call()).thenThrow(exception);

    AsyncFutureNotifier<String> notifier = new AsyncFutureNotifier<>(m_next, null);

    try {
      notifier.call();
      fail();
    }
    catch (Exception e) {
      assertSame(exception, e);
    }
  }

  @Test
  public void testErrorProcessingException2() throws Exception {
    ProcessingException exception = new ProcessingException("error");
    when(m_next.call()).thenThrow(exception);

    AsyncFutureNotifier<String> notifier = new AsyncFutureNotifier<>(m_next, null);

    try {
      notifier.call();
      fail();
    }
    catch (Exception e) {
      assertSame(exception, e);
    }
  }

  @Test
  public void testErrorProcessingExceptionWithAsyncFuture() throws Exception {
    ProcessingException exception = new ProcessingException("error");
    when(m_next.call()).thenThrow(exception);

    AsyncFutureNotifier<String> notifier = new AsyncFutureNotifier<>(m_next, m_asyncFuture);

    try {
      notifier.call();
      fail();
    }
    catch (ProcessingException e) {
      verify(m_asyncFuture, never()).onSuccess(anyString());
      verify(m_asyncFuture, times(1)).onError(any(ProcessingException.class));
      verify(m_asyncFuture, times(1)).onDone(isNull(String.class), any(ProcessingException.class));

      ProcessingException pe = m_errorExceptionCaptor.getValue();
      assertSame(exception, pe);
      assertSame(pe, e);

      pe = m_doneExceptionCaptor.getValue();
      assertSame(exception, pe);
      assertSame(pe, e);
    }
  }
}
