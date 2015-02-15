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
import org.eclipse.scout.commons.job.JobManager;
import org.eclipse.scout.commons.job.interceptor.AsyncFutureNotifier;
import org.eclipse.scout.commons.job.interceptor.Chainable;
import org.eclipse.scout.commons.job.interceptor.ExceptionTranslator;
import org.eclipse.scout.commons.job.interceptor.ThreadLocalInitializer;
import org.eclipse.scout.commons.job.interceptor.ThreadNameDecorator;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

@SuppressWarnings("unchecked")
public class ClientJobCallableChainTest {

  /**
   * Tests the correct order of interceptors in {@link ClientJob}.
   */
  @Test
  public void testCallableChain() throws Exception {
    final JobManager jobManager = mock(JobManager.class);

    // install captor to intercept Callable.
    ArgumentCaptor<Callable> callableCaptor = ArgumentCaptor.forClass(Callable.class);
    doReturn(mock(Future.class)).when(jobManager).schedule(any(IJob.class), callableCaptor.capture());

    final Callable<Void> targetInvoker = mock(Callable.class);
    ClientJob<Void> job = new ClientJob<Void>("job-1", mock(IClientSession.class)) {

      @Override
      protected JobManager createJobManager() {
        return jobManager;
      }

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws Exception {
        throw new RuntimeException();
      }

      @Override
      protected Callable<Void> createTargetInvoker() {
        return targetInvoker;
      }
    };

    // run the test
    job.schedule();

    // verify Callable-Chain

    // 1. ThreadNameDecorator
    ThreadNameDecorator c1 = getFirstAndAssert(callableCaptor, ThreadNameDecorator.class);

    // 2. ThreadLocalInitializer for IJob.CURRENT
    ThreadLocalInitializer c2 = getNextAndAssert(c1, ThreadLocalInitializer.class);
    assertSame(IJob.CURRENT, ((ThreadLocalInitializer) c2).getThreadLocal());

    // 3. ThreadLocalInitializer for JobContext.CURRENT
    ThreadLocalInitializer c3 = getNextAndAssert(c2, ThreadLocalInitializer.class);
    assertSame(JobContext.CURRENT, ((ThreadLocalInitializer) c3).getThreadLocal());

    // 4. ThreadLocalInitializer for ISession.CURRENT
    ThreadLocalInitializer c4 = getNextAndAssert(c3, ThreadLocalInitializer.class);
    assertSame(ISession.CURRENT, ((ThreadLocalInitializer) c4).getThreadLocal());

    // 5. ThreadLocalInitializer for LocaleThreadLocal.CURRENT
    ThreadLocalInitializer c5 = getNextAndAssert(c4, ThreadLocalInitializer.class);
    assertSame(NlsLocale.CURRENT, ((ThreadLocalInitializer) c5).getThreadLocal());

    // 6. ThreadLocalInitializer for ScoutTexts.CURRENT
    ThreadLocalInitializer c6 = getNextAndAssert(c5, ThreadLocalInitializer.class);
    assertSame(ScoutTexts.CURRENT, ((ThreadLocalInitializer) c6).getThreadLocal());

    // 7. AsyncFutureNotifier
    AsyncFutureNotifier c7 = getNextAndAssert(c6, AsyncFutureNotifier.class);

    // 8. ExceptionTranslator
    ExceptionTranslator c8 = getNextAndAssert(c7, ExceptionTranslator.class);

    // 9. TargetInvoker
    assertSame(targetInvoker, c8.getNext());
  }

  /**
   * Tests that new contributions can be installed after the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsAfter() throws Exception {
    final JobManager jobManager = mock(JobManager.class);

    // install captor to intercept Callable.
    ArgumentCaptor<Callable> callableCaptor = ArgumentCaptor.forClass(Callable.class);
    doReturn(mock(Future.class)).when(jobManager).schedule(any(IJob.class), callableCaptor.capture());

    final Callable<Void> targetInvoker = mock(Callable.class);
    ClientJob<Void> job = new ClientJob<Void>("job-1", mock(IClientSession.class)) {

      @Override
      protected Callable<Void> interceptCallable(Callable<Void> next) {
        Callable<Void> p2 = new Contribution2(next); // executed 3th
        Callable<Void> p1 = new Contribution1(p2); // executed 2nd
        Callable<Void> head = super.interceptCallable(p1); // executed 1st
        return head;
      }

      @Override
      protected JobManager createJobManager() {
        return jobManager;
      }

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws Exception {
        throw new RuntimeException();
      }

      @Override
      protected Callable<Void> createTargetInvoker() {
        return targetInvoker;
      }
    };

    // run the test
    job.schedule();

    // verify Callable-Chain

    // 1. ThreadNameDecorator
    ThreadNameDecorator c1 = getFirstAndAssert(callableCaptor, ThreadNameDecorator.class);

    // 2. ThreadLocalInitializer for IJob.CURRENT
    ThreadLocalInitializer c2 = getNextAndAssert(c1, ThreadLocalInitializer.class);
    assertSame(IJob.CURRENT, ((ThreadLocalInitializer) c2).getThreadLocal());

    // 3. ThreadLocalInitializer for JobContext.CURRENT
    ThreadLocalInitializer c3 = getNextAndAssert(c2, ThreadLocalInitializer.class);
    assertSame(JobContext.CURRENT, ((ThreadLocalInitializer) c3).getThreadLocal());

    // 4. ThreadLocalInitializer for ISession.CURRENT
    ThreadLocalInitializer c4 = getNextAndAssert(c3, ThreadLocalInitializer.class);
    assertSame(ISession.CURRENT, ((ThreadLocalInitializer) c4).getThreadLocal());

    // 5. ThreadLocalInitializer for LocaleThreadLocal.CURRENT
    ThreadLocalInitializer c5 = getNextAndAssert(c4, ThreadLocalInitializer.class);
    assertSame(NlsLocale.CURRENT, ((ThreadLocalInitializer) c5).getThreadLocal());

    // 6. ThreadLocalInitializer for ScoutTexts.CURRENT
    ThreadLocalInitializer c6 = getNextAndAssert(c5, ThreadLocalInitializer.class);
    assertSame(ScoutTexts.CURRENT, ((ThreadLocalInitializer) c6).getThreadLocal());

    // 7. Contribution1
    Contribution1 c7 = getNextAndAssert(c6, Contribution1.class);

    // 8. Contribution2
    Contribution2 c8 = getNextAndAssert(c7, Contribution2.class);

    // 9. AsyncFutureNotifier
    AsyncFutureNotifier c9 = getNextAndAssert(c8, AsyncFutureNotifier.class);

    // 10. ExceptionTranslator
    ExceptionTranslator c10 = getNextAndAssert(c9, ExceptionTranslator.class);

    // 11. TargetInvoker
    assertSame(targetInvoker, c10.getNext());
  }

  /**
   * Tests that new contributions can be installed before the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsBefore() throws Exception {
    final JobManager jobManager = mock(JobManager.class);

    // install captor to intercept Callable.
    ArgumentCaptor<Callable> callableCaptor = ArgumentCaptor.forClass(Callable.class);
    doReturn(mock(Future.class)).when(jobManager).schedule(any(IJob.class), callableCaptor.capture());

    final Callable<Void> targetInvoker = mock(Callable.class);
    ClientJob<Void> job = new ClientJob<Void>("job-1", mock(IClientSession.class)) {

      @Override
      protected Callable<Void> interceptCallable(Callable<Void> next) {
        Callable<Void> p2 = super.interceptCallable(next); // executed 3th
        Callable<Void> p1 = new Contribution2(p2); // executed 2nd
        Callable<Void> head = new Contribution1(p1); // executed 1st
        return head;
      }

      @Override
      protected JobManager createJobManager() {
        return jobManager;
      }

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws Exception {
        throw new RuntimeException();
      }

      @Override
      protected Callable<Void> createTargetInvoker() {
        return targetInvoker;
      }
    };

    // run the test
    job.schedule();

    // verify Callable-Chain

    // 1. Contribution1
    Contribution1 c1 = getFirstAndAssert(callableCaptor, Contribution1.class);

    // 2. Contribution2
    Contribution2 c2 = getNextAndAssert(c1, Contribution2.class);

    // 3. ThreadNameDecorator
    ThreadNameDecorator c3 = getNextAndAssert(c2, ThreadNameDecorator.class);

    // 4. ThreadLocalInitializer for IJob.CURRENT
    ThreadLocalInitializer c4 = getNextAndAssert(c3, ThreadLocalInitializer.class);
    assertSame(IJob.CURRENT, ((ThreadLocalInitializer) c4).getThreadLocal());

    // 5. ThreadLocalInitializer for JobContext.CURRENT
    ThreadLocalInitializer c5 = getNextAndAssert(c4, ThreadLocalInitializer.class);
    assertSame(JobContext.CURRENT, ((ThreadLocalInitializer) c5).getThreadLocal());

    // 6. ThreadLocalInitializer for ISession.CURRENT
    ThreadLocalInitializer c6 = getNextAndAssert(c5, ThreadLocalInitializer.class);
    assertSame(ISession.CURRENT, ((ThreadLocalInitializer) c6).getThreadLocal());

    // 7. ThreadLocalInitializer for LocaleThreadLocal.CURRENT
    ThreadLocalInitializer c7 = getNextAndAssert(c6, ThreadLocalInitializer.class);
    assertSame(NlsLocale.CURRENT, ((ThreadLocalInitializer) c7).getThreadLocal());

    // 8. ThreadLocalInitializer for ScoutTexts.CURRENT
    ThreadLocalInitializer c8 = getNextAndAssert(c7, ThreadLocalInitializer.class);
    assertSame(ScoutTexts.CURRENT, ((ThreadLocalInitializer) c8).getThreadLocal());

    // 9. AsyncFutureNotifier
    AsyncFutureNotifier c9 = getNextAndAssert(c8, AsyncFutureNotifier.class);

    // 10. ExceptionTranslator
    ExceptionTranslator c10 = getNextAndAssert(c9, ExceptionTranslator.class);

    // 11. TargetInvoker
    assertSame(targetInvoker, c10.getNext());
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
