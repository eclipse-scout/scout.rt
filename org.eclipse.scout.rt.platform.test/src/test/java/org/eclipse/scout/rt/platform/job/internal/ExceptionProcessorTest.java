/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.internal;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ExceptionProcessorTest {

  private ExceptionHandler m_exceptionHandler;
  private IBean<ExceptionHandler> m_exceptionHandlerBean;

  @Before
  public void before() {
    m_exceptionHandler = mock(ExceptionHandler.class);
    m_exceptionHandlerBean = Platform.get().getBeanManager().registerBean(new BeanMetaData(BEANS.get(ExceptionHandler.class).getClass(), m_exceptionHandler).withReplace(true).withOrder(-Long.MAX_VALUE));
    assertSame(m_exceptionHandler, BEANS.get(ExceptionHandler.class));
  }

  @After
  public void after() {
    Platform.get().getBeanManager().unregisterBean(m_exceptionHandlerBean);
  }

  @Test
  public void testSuccess() throws Exception {
    JobInput jobInput = Jobs.newInput();

    CallableChain<String> chain = new CallableChain<>();
    chain.add(new ExceptionProcessor<>(jobInput));
    String result = chain.call(() -> "result");

    assertEquals("result", result);
  }

  @Test
  public void testDefaultSettingsWithException() throws Exception {
    final RuntimeException exception = new RuntimeException("expected JUnit test exception");

    JobInput jobInput = Jobs.newInput();

    CallableChain<String> chain = new CallableChain<>();
    chain.add(new CallableChainExceptionHandler<>());
    chain.add(new ExceptionProcessor<>(jobInput));
    try {
      chain.call(() -> {
        throw exception;
      });
      fail();
    }
    catch (RuntimeException e) {
      assertSame(exception, e);
      verify(m_exceptionHandler, times(1)).handle(eq(exception));
      verifyNoMoreInteractions(m_exceptionHandler);
    }
  }

  @Test
  public void testWithSwallow() throws Exception {
    final RuntimeException exception = new RuntimeException("expected JUnit test exception");

    JobInput jobInput = Jobs.newInput().withExceptionHandling(BEANS.get(ExceptionHandler.class), true);

    CallableChain<String> chain = new CallableChain<>();
    chain.add(new ExceptionProcessor<>(jobInput));
    chain.call(() -> {
      throw exception;
    });
    verify(m_exceptionHandler, times(1)).handle(eq(exception));
    verifyNoMoreInteractions(m_exceptionHandler);
  }

  @Test
  public void testWithNullExceptionHandlerAndSwallow() throws Exception {
    final RuntimeException exception = new RuntimeException("expected JUnit test exception");

    JobInput jobInput = Jobs.newInput().withExceptionHandling(null, true);

    CallableChain<String> chain = new CallableChain<>();
    chain.add(new ExceptionProcessor<>(jobInput));
    chain.call(() -> {
      throw exception;
    });
    verify(m_exceptionHandler, never()).handle(eq(exception));
  }

  @Test
  public void testWithNullExceptionHandlerAndPropagate() throws Exception {
    final RuntimeException exception = new RuntimeException("expected JUnit test exception");

    JobInput jobInput = Jobs.newInput().withExceptionHandling(null, false);

    CallableChain<String> chain = new CallableChain<>();
    chain.add(new ExceptionProcessor<>(jobInput));
    try {
      chain.call(() -> {
        throw exception;
      });
      fail();
    }
    catch (RuntimeException e) {
      assertSame(exception, e);
      verify(m_exceptionHandler, never()).handle(eq(exception));
    }
  }

  @Test
  public void testWithCustomExceptionHandler() throws Exception {
    final RuntimeException exception = new RuntimeException("expected JUnit test exception");

    final AtomicReference<Throwable> error = new AtomicReference<>();

    JobInput jobInput = Jobs.newInput()
        .withExceptionHandling(new ExceptionHandler() {

          @Override
          public void handle(Throwable t) {
            error.set(t);
          }
        }, true);

    CallableChain<String> chain = new CallableChain<>();
    chain.add(new ExceptionProcessor<>(jobInput));
    chain.call(() -> {
      throw exception;
    });
    assertSame(exception, error.get());
    verify(m_exceptionHandler, never()).handle(eq(exception));
  }
}
