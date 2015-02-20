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
package org.eclipse.scout.commons.job;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.eclipse.scout.commons.job.interceptor.AsyncFutureCallable;
import org.eclipse.scout.commons.job.interceptor.Chainable;
import org.eclipse.scout.commons.job.interceptor.ExceptionTranslator;
import org.eclipse.scout.commons.job.interceptor.InitThreadLocalCallable;
import org.eclipse.scout.commons.job.interceptor.ThreadNameDecorator;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

@SuppressWarnings("unchecked")
public class JobCallableChainTest {

  /**
   * Tests the correct order of interceptors in {@link Job}.
   */
  @Test
  public void testCallableChain() throws Exception {
    final IJobManager jobManager = mock(IJobManager.class);

    // install captor to intercept Callable.
    ArgumentCaptor<Callable> callableCaptor = ArgumentCaptor.forClass(Callable.class);
    doReturn(mock(Future.class)).when(jobManager).schedule(any(IJob.class), callableCaptor.capture());

    final Callable<Void> targetInvoker = mock(Callable.class);
    Job<Void> job = new Job<Void>("job-1") {

      @Override
      protected IJobManager createJobManager() {
        return jobManager;
      }

      @Override
      protected Void call() throws Exception {
        throw new RuntimeException();
      }

      @Override
      protected Callable<Void> createCallInvoker() {
        return targetInvoker;
      }
    };

    // run the test
    job.schedule();

    // verify Callable-Cahin

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

    // 5. AsyncFutureCallable
    AsyncFutureCallable c5 = getNextAndAssert(c4, AsyncFutureCallable.class);

    // 6. ExceptionTranslator
    ExceptionTranslator c6 = getNextAndAssert(c5, ExceptionTranslator.class);

    // 7. TargetInvoker
    assertSame(targetInvoker, c6.getNext());
  }

  /**
   * Tests that new contributions can be installed after the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsAfter() throws Exception {
    final IJobManager jobManager = mock(IJobManager.class);

    // install captor to intercept Callable.
    ArgumentCaptor<Callable> callableCaptor = ArgumentCaptor.forClass(Callable.class);
    doReturn(mock(Future.class)).when(jobManager).schedule(any(IJob.class), callableCaptor.capture());

    final Callable<Void> targetInvoker = mock(Callable.class);
    Job<Void> job = new Job<Void>("job-1") {

      @Override
      protected Callable<Void> interceptCallable(Callable<Void> next) {
        Callable<Void> p2 = new Contribution2(next); // executed 3th
        Callable<Void> p1 = new Contribution1(p2); // executed 2nd
        Callable<Void> head = super.interceptCallable(p1); // executed 1st
        return head;
      }

      @Override
      protected IJobManager createJobManager() {
        return jobManager;
      }

      @Override
      protected Void call() throws Exception {
        throw new RuntimeException();
      }

      @Override
      protected Callable<Void> createCallInvoker() {
        return targetInvoker;
      }
    };

    // run the test
    job.schedule();

    // verify Callable-Cahin

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

    // 5. Contribution1
    Contribution1 c5 = getNextAndAssert(c4, Contribution1.class);

    // 6. Contribution2
    Contribution2 c6 = getNextAndAssert(c5, Contribution2.class);

    // 7. AsyncFutureCallable
    AsyncFutureCallable c7 = getNextAndAssert(c6, AsyncFutureCallable.class);

    // 8. ExceptionTranslator
    ExceptionTranslator c8 = getNextAndAssert(c7, ExceptionTranslator.class);

    // 9. TargetInvoker
    assertSame(targetInvoker, c8.getNext());
  }

  /**
   * Tests that new contributions can be installed before the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsBefore() throws Exception {
    final IJobManager jobManager = mock(IJobManager.class);

    // install captor to intercept Callable.
    ArgumentCaptor<Callable> callableCaptor = ArgumentCaptor.forClass(Callable.class);
    doReturn(mock(Future.class)).when(jobManager).schedule(any(IJob.class), callableCaptor.capture());

    final Callable<Void> targetInvoker = mock(Callable.class);
    Job<Void> job = new Job<Void>("job-1") {

      @Override
      protected Callable<Void> interceptCallable(Callable<Void> next) {
        Callable<Void> p2 = super.interceptCallable(next); // executed 3th
        Callable<Void> p1 = new Contribution2(p2); // executed 2nd
        Callable<Void> head = new Contribution1(p1); // executed 1st
        return head;
      }

      @Override
      protected IJobManager createJobManager() {
        return jobManager;
      }

      @Override
      protected Void call() throws Exception {
        throw new RuntimeException();
      }

      @Override
      protected Callable<Void> createCallInvoker() {
        return targetInvoker;
      }
    };

    // run the test
    job.schedule();

    // verify Callable-Cahin

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

    // 7. AsyncFutureCallable
    AsyncFutureCallable c7 = getNextAndAssert(c6, AsyncFutureCallable.class);

    // 8. ExceptionTranslator
    ExceptionTranslator c8 = getNextAndAssert(c7, ExceptionTranslator.class);

    // 9. TargetInvoker
    assertSame(targetInvoker, c8.getNext());
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
