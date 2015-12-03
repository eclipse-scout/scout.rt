/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.Callable;

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
import org.junit.Ignore;
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
    chain.add(new ExceptionProcessor<String>(jobInput));
    String result = chain.call(new Callable<String>() {

      @Override
      public String call() throws Exception {
        return "result";
      }
    });

    assertEquals("result", result);
  }

  @Test
  public void testDefaultSettingsWithException() throws Exception {
    final RuntimeException exception = new RuntimeException();

    JobInput jobInput = Jobs.newInput();

    CallableChain<String> chain = new CallableChain<>();
    chain.add(new ExceptionProcessor<String>(jobInput));
    try {
      chain.call(new Callable<String>() {

        @Override
        public String call() throws Exception {
          throw exception;
        }
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
    final RuntimeException exception = new RuntimeException();

    JobInput jobInput = Jobs.newInput().withExceptionHandling(ExceptionHandler.class, true);

    CallableChain<String> chain = new CallableChain<>();
    chain.add(new ExceptionProcessor<String>(jobInput));
    chain.call(new Callable<String>() {

      @Override
      public String call() throws Exception {
        throw exception;
      }
    });
    verify(m_exceptionHandler, times(1)).handle(eq(exception));
    verifyNoMoreInteractions(m_exceptionHandler);
  }

  @Test
  public void testWithNullExceptionHandlerAndSwallow() throws Exception {
    final RuntimeException exception = new RuntimeException();

    JobInput jobInput = Jobs.newInput().withExceptionHandling(null, true);

    CallableChain<String> chain = new CallableChain<>();
    chain.add(new ExceptionProcessor<String>(jobInput));
    chain.call(new Callable<String>() {

      @Override
      public String call() throws Exception {
        throw exception;
      }
    });
    verify(m_exceptionHandler, never()).handle(eq(exception));
  }

  @Test
  public void testWithNullExceptionHandlerAndPropagate() throws Exception {
    final RuntimeException exception = new RuntimeException();

    JobInput jobInput = Jobs.newInput().withExceptionHandling(null, false);

    CallableChain<String> chain = new CallableChain<>();
    chain.add(new ExceptionProcessor<String>(jobInput));
    try {
      chain.call(new Callable<String>() {

        @Override
        public String call() throws Exception {
          throw exception;
        }
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
    IBean<Object> testExceptionHandlerBean = Platform.get().getBeanManager().registerBean(new BeanMetaData(TestExceptionHandler.class).withOrder(Long.MAX_VALUE));
    try {
      final RuntimeException exception = new RuntimeException();

      JobInput jobInput = Jobs.newInput().withExceptionHandling(TestExceptionHandler.class, true);

      CallableChain<String> chain = new CallableChain<>();
      chain.add(new ExceptionProcessor<String>(jobInput));
      chain.call(new Callable<String>() {

        @Override
        public String call() throws Exception {
          throw exception;
        }
      });
      assertSame(exception, BEANS.get(TestExceptionHandler.class).m_throwable);
      verify(m_exceptionHandler, never()).handle(eq(exception));
    }
    finally {
      Platform.get().getBeanManager().unregisterBean(testExceptionHandlerBean);
    }
  }

  @Ignore
  public static class TestExceptionHandler extends ExceptionHandler {

    private Throwable m_throwable;

    @Override
    public void handle(Throwable t) {
      m_throwable = t;
    }
  }
}
