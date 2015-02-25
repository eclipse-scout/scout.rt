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
package org.eclipse.scout.rt.client.job;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.interceptor.AsyncFutureCallable;
import org.eclipse.scout.commons.job.interceptor.Chainable;
import org.eclipse.scout.commons.job.interceptor.ExceptionTranslator;
import org.eclipse.scout.commons.job.interceptor.InitThreadLocalCallable;
import org.eclipse.scout.commons.job.interceptor.ThreadNameDecorator;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

@SuppressWarnings("unchecked")
public class ModelJobCallableChainTest {

  /**
   * Tests the correct order of interceptors in {@link ClientJob}.
   */
  @Test
  public void testCallableChain() throws Exception {
    final IModelJobManager jobManager = mock(IModelJobManager.class);

    // install captor to intercept Callable.
    ArgumentCaptor<Callable> callableCaptor = ArgumentCaptor.forClass(Callable.class);
    doReturn(mock(Future.class)).when(jobManager).schedule(any(ModelJob.class), callableCaptor.capture());

    final Callable<Void> callInvoker = mock(Callable.class);
    ModelJob job = new ModelJob("job-1", mock(IClientSession.class)) {

      @Override
      protected IModelJobManager createJobManager(IClientSession clientSession) {
        return jobManager;
      }

      @Override
      protected void run() throws Exception {
        throw new RuntimeException();
      }

      @Override
      protected Callable<Void> createCallInvoker() {
        return callInvoker;
      }
    };

    // run the test
    job.schedule();

    // verify Callable-Chain

    // 1. InitThreadLocalCallable for IJob.CURRENT
    InitThreadLocalCallable c1 = getFirstAndAssert(callableCaptor, InitThreadLocalCallable.class);
    assertSame(IJob.CURRENT, ((InitThreadLocalCallable) c1).getThreadLocal());

    // 2. InitThreadLocalCallable for IProgressMonitor.CURRENT
    InitThreadLocalCallable c2 = getNextAndAssert(c1, InitThreadLocalCallable.class);
    assertSame(IProgressMonitor.CURRENT, ((InitThreadLocalCallable) c2).getThreadLocal());

    // 3. ThreadNameDecorator
    ThreadNameDecorator c3 = getNextAndAssert(c2, ThreadNameDecorator.class);

    // 4. InitThreadLocalCallable for JobContext.CURRENT
    InitThreadLocalCallable c4 = getNextAndAssert(c3, InitThreadLocalCallable.class);
    assertSame(JobContext.CURRENT, ((InitThreadLocalCallable) c4).getThreadLocal());

    // 5. InitThreadLocalCallable for ISession.CURRENT
    InitThreadLocalCallable c5 = getNextAndAssert(c4, InitThreadLocalCallable.class);
    assertSame(ISession.CURRENT, ((InitThreadLocalCallable) c5).getThreadLocal());

    // 6. InitThreadLocalCallable for LocaleThreadLocal.CURRENT
    InitThreadLocalCallable c6 = getNextAndAssert(c5, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c6).getThreadLocal());

    // 7. InitThreadLocalCallable for ScoutTexts.CURRENT
    InitThreadLocalCallable c7 = getNextAndAssert(c6, InitThreadLocalCallable.class);
    assertSame(ScoutTexts.CURRENT, ((InitThreadLocalCallable) c7).getThreadLocal());

    // 8. AsyncFutureCallable
    AsyncFutureCallable c8 = getNextAndAssert(c7, AsyncFutureCallable.class);

    // 9. ExceptionTranslator
    ExceptionTranslator c9 = getNextAndAssert(c8, ExceptionTranslator.class);

    // 10. TargetInvoker
    assertSame(callInvoker, c9.getNext());
  }

  /**
   * Tests that new contributions can be installed after the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsAfter() throws Exception {
    final IModelJobManager jobManager = mock(IModelJobManager.class);

    // install captor to intercept Callable.
    ArgumentCaptor<Callable> callableCaptor = ArgumentCaptor.forClass(Callable.class);
    doReturn(mock(Future.class)).when(jobManager).schedule(any(ModelJob.class), callableCaptor.capture());

    final Callable<Void> callInvoker = mock(Callable.class);
    ModelJob job = new ModelJob("job-1", mock(IClientSession.class)) {

      @Override
      protected Callable<Void> interceptCallable(Callable<Void> next) {
        Callable<Void> p2 = new Contribution2(next); // executed 3th
        Callable<Void> p1 = new Contribution1(p2); // executed 2nd
        Callable<Void> head = super.interceptCallable(p1); // executed 1st
        return head;
      }

      @Override
      protected IModelJobManager createJobManager(IClientSession clientSession) {
        return jobManager;
      }

      @Override
      protected void run() throws Exception {
        throw new RuntimeException();
      }

      @Override
      protected Callable<Void> createCallInvoker() {
        return callInvoker;
      }
    };

    // run the test
    job.schedule();

    // verify Callable-Chain

    // 1. InitThreadLocalCallable for IJob.CURRENT
    InitThreadLocalCallable c1 = getFirstAndAssert(callableCaptor, InitThreadLocalCallable.class);
    assertSame(IJob.CURRENT, ((InitThreadLocalCallable) c1).getThreadLocal());

    // 2. InitThreadLocalCallable for IProgressMonitor.CURRENT
    InitThreadLocalCallable c2 = getNextAndAssert(c1, InitThreadLocalCallable.class);
    assertSame(IProgressMonitor.CURRENT, ((InitThreadLocalCallable) c2).getThreadLocal());

    // 3. ThreadNameDecorator
    ThreadNameDecorator c3 = getNextAndAssert(c2, ThreadNameDecorator.class);

    // 4. InitThreadLocalCallable for JobContext.CURRENT
    InitThreadLocalCallable c4 = getNextAndAssert(c3, InitThreadLocalCallable.class);
    assertSame(JobContext.CURRENT, ((InitThreadLocalCallable) c4).getThreadLocal());

    // 5. InitThreadLocalCallable for ISession.CURRENT
    InitThreadLocalCallable c5 = getNextAndAssert(c4, InitThreadLocalCallable.class);
    assertSame(ISession.CURRENT, ((InitThreadLocalCallable) c5).getThreadLocal());

    // 6. InitThreadLocalCallable for LocaleThreadLocal.CURRENT
    InitThreadLocalCallable c6 = getNextAndAssert(c5, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c6).getThreadLocal());

    // 7. InitThreadLocalCallable for ScoutTexts.CURRENT
    InitThreadLocalCallable c7 = getNextAndAssert(c6, InitThreadLocalCallable.class);
    assertSame(ScoutTexts.CURRENT, ((InitThreadLocalCallable) c7).getThreadLocal());

    // 8. Contribution1
    Contribution1 c8 = getNextAndAssert(c7, Contribution1.class);

    // 9. Contribution2
    Contribution2 c9 = getNextAndAssert(c8, Contribution2.class);

    // 10. AsyncFutureCallable
    AsyncFutureCallable c10 = getNextAndAssert(c9, AsyncFutureCallable.class);

    // 11. ExceptionTranslator
    ExceptionTranslator c11 = getNextAndAssert(c10, ExceptionTranslator.class);

    // 12. TargetInvoker
    assertSame(callInvoker, c11.getNext());
  }

  /**
   * Tests that new contributions can be installed before the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsBefore() throws Exception {
    final IModelJobManager jobManager = mock(IModelJobManager.class);

    // install captor to intercept Callable.
    ArgumentCaptor<Callable> callableCaptor = ArgumentCaptor.forClass(Callable.class);
    doReturn(mock(Future.class)).when(jobManager).schedule(any(ModelJob.class), callableCaptor.capture());

    final Callable<Void> callInvoker = mock(Callable.class);
    ModelJob job = new ModelJob("job-1", mock(IClientSession.class)) {

      @Override
      protected Callable<Void> interceptCallable(Callable<Void> next) {
        Callable<Void> p2 = super.interceptCallable(next); // executed 3th
        Callable<Void> p1 = new Contribution2(p2); // executed 2nd
        Callable<Void> head = new Contribution1(p1); // executed 1st
        return head;
      }

      @Override
      protected IModelJobManager createJobManager(IClientSession clientSession) {
        return jobManager;
      }

      @Override
      protected void run() throws Exception {
        throw new RuntimeException();
      }

      @Override
      protected Callable<Void> createCallInvoker() {
        return callInvoker;
      }
    };

    // run the test
    job.schedule();

    // verify Callable-Chain

    // 1. InitThreadLocalCallable for IJob.CURRENT
    InitThreadLocalCallable c1 = getFirstAndAssert(callableCaptor, InitThreadLocalCallable.class);
    assertSame(IJob.CURRENT, ((InitThreadLocalCallable) c1).getThreadLocal());

    // 2. InitThreadLocalCallable for IProgressMonitor.CURRENT
    InitThreadLocalCallable c2 = getNextAndAssert(c1, InitThreadLocalCallable.class);
    assertSame(IProgressMonitor.CURRENT, ((InitThreadLocalCallable) c2).getThreadLocal());

    // 3. Contribution1
    Contribution1 c3 = getNextAndAssert(c2, Contribution1.class);

    // 4. Contribution2
    Contribution2 c4 = getNextAndAssert(c3, Contribution2.class);

    // 5. ThreadNameDecorator
    ThreadNameDecorator c5 = getNextAndAssert(c4, ThreadNameDecorator.class);

    // 6. InitThreadLocalCallable for JobContext.CURRENT
    InitThreadLocalCallable c6 = getNextAndAssert(c5, InitThreadLocalCallable.class);
    assertSame(JobContext.CURRENT, ((InitThreadLocalCallable) c6).getThreadLocal());

    // 7. InitThreadLocalCallable for ISession.CURRENT
    InitThreadLocalCallable c7 = getNextAndAssert(c6, InitThreadLocalCallable.class);
    assertSame(ISession.CURRENT, ((InitThreadLocalCallable) c7).getThreadLocal());

    // 8. InitThreadLocalCallable for LocaleThreadLocal.CURRENT
    InitThreadLocalCallable c8 = getNextAndAssert(c7, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c8).getThreadLocal());

    // 9. InitThreadLocalCallable for ScoutTexts.CURRENT
    InitThreadLocalCallable c9 = getNextAndAssert(c8, InitThreadLocalCallable.class);
    assertSame(ScoutTexts.CURRENT, ((InitThreadLocalCallable) c9).getThreadLocal());

    // 10. AsyncFutureCallable
    AsyncFutureCallable c10 = getNextAndAssert(c9, AsyncFutureCallable.class);

    // 11. ExceptionTranslator
    ExceptionTranslator c11 = getNextAndAssert(c10, ExceptionTranslator.class);

    // 12. TargetInvoker
    assertSame(callInvoker, c11.getNext());
  }

  private static <T> T getFirstAndAssert(ArgumentCaptor<Callable> captor, Class<T> expectedType) {
    Object first = captor.getValue();
    assertTrue(expectedType.equals(first.getClass()));
    return (T) first;
  }

  private static <T> T getNextAndAssert(Chainable c, Class<T> expectedType) {
    Object next = c.getNext();
    assertTrue(expectedType.equals(next.getClass()));
    return (T) next;
  }

  private static class Contribution1 implements Callable<Void>, Chainable {

    private final Callable<Void> m_next;

    public Contribution1(Callable<Void> next) {
      m_next = next;
    }

    @Override
    public Void call() throws Exception {
      return m_next.call();
    }

    @Override
    public Object getNext() {
      return m_next;
    }
  }

  private static class Contribution2 implements Callable<Void>, Chainable {

    private final Callable<Void> m_next;

    public Contribution2(Callable<Void> next) {
      m_next = next;
    }

    @Override
    public Void call() throws Exception {
      return m_next.call();
    }

    @Override
    public Object getNext() {
      return m_next;
    }
  }
}
