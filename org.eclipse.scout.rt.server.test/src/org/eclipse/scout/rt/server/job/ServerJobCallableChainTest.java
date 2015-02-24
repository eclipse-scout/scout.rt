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
package org.eclipse.scout.rt.server.job;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.IJobManager;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.interceptor.AsyncFutureCallable;
import org.eclipse.scout.commons.job.interceptor.Chainable;
import org.eclipse.scout.commons.job.interceptor.ExceptionTranslator;
import org.eclipse.scout.commons.job.interceptor.InitThreadLocalCallable;
import org.eclipse.scout.commons.job.interceptor.ThreadNameDecorator;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.servletfilter.HttpServletRoundtrip;
import org.eclipse.scout.rt.server.job.interceptor.SubjectCallable;
import org.eclipse.scout.rt.server.job.interceptor.TwoPhaseTransactionBoundaryCallable;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.testing.platform.ScoutPlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

// TODO remove OSGi dependencies
@SuppressWarnings("unchecked")
@RunWith(ScoutPlatformTestRunner.class)
public class ServerJobCallableChainTest {

  /**
   * Tests the correct order of interceptors in {@link ServerJobWithResult}.
   */
  @Test
  public void testCallableChain() throws Exception {
    final IJobManager jobManager = mock(IJobManager.class);

    // verify Callable-Chain
    ArgumentCaptor<Callable> callableCaptor = ArgumentCaptor.forClass(Callable.class);
    doReturn(mock(Future.class)).when(jobManager).schedule(any(IJob.class), callableCaptor.capture());

    final Callable<Void> callInvoker = mock(Callable.class);
    ServerJob job = new ServerJob("job-1", mock(IServerSession.class)) {

      @Override
      protected IJobManager createJobManager() {
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

    // 5. InitThreadLocalCallable for ServiceTunnelServlet.CURRENT_REQUEST
    InitThreadLocalCallable c5 = getNextAndAssert(c4, InitThreadLocalCallable.class);
    assertSame(HttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST, ((InitThreadLocalCallable) c5).getThreadLocal());

    // 6. InitThreadLocalCallable for ServiceTunnelServlet.CURRENT_RESPONSE
    InitThreadLocalCallable c6 = getNextAndAssert(c5, InitThreadLocalCallable.class);
    assertSame(HttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE, ((InitThreadLocalCallable) c6).getThreadLocal());

    // 7. InitThreadLocalCallable for ISession.CURRENT
    InitThreadLocalCallable c7 = getNextAndAssert(c6, InitThreadLocalCallable.class);
    assertSame(ISession.CURRENT, ((InitThreadLocalCallable) c7).getThreadLocal());

    // 8. InitThreadLocalCallable for ScoutLocale.CURRENT
    InitThreadLocalCallable c8 = getNextAndAssert(c7, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c8).getThreadLocal());

    // 9. InitThreadLocalCallable for ScoutTexts.CURRENT
    InitThreadLocalCallable c9 = getNextAndAssert(c8, InitThreadLocalCallable.class);
    assertSame(ScoutTexts.CURRENT, ((InitThreadLocalCallable) c9).getThreadLocal());

    // 10. InitThreadLocalCallable for ITransaction.CURRENT
    InitThreadLocalCallable c10 = getNextAndAssert(c9, InitThreadLocalCallable.class);
    assertSame(ITransaction.CURRENT, ((InitThreadLocalCallable) c10).getThreadLocal());

    // 11. SubjectCallable
    SubjectCallable c11 = getNextAndAssert(c10, SubjectCallable.class);

    // 12. TwoPhaseTransactionBoundaryCallable
    TwoPhaseTransactionBoundaryCallable c12 = getNextAndAssert(c11, TwoPhaseTransactionBoundaryCallable.class);

    // 13. AsyncFutureCallable
    AsyncFutureCallable c13 = getNextAndAssert(c12, AsyncFutureCallable.class);

    // 14. ExceptionTranslator
    ExceptionTranslator c14 = getNextAndAssert(c13, ExceptionTranslator.class);

    // 15. TargetInvoker
    assertSame(callInvoker, c14.getNext());
  }

  /**
   * Tests that new contributions can be installed after the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsAfter() throws Exception {
    final IJobManager jobManager = mock(IJobManager.class);

    // verify Callable-Chain
    ArgumentCaptor<Callable> callableCaptor = ArgumentCaptor.forClass(Callable.class);
    doReturn(mock(Future.class)).when(jobManager).schedule(any(IJob.class), callableCaptor.capture());

    final Callable<Void> callInvoker = mock(Callable.class);
    ServerJob job = new ServerJob("job-1", mock(IServerSession.class)) {

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

    // 5. InitThreadLocalCallable for ServiceTunnelServlet.CURRENT_REQUEST
    InitThreadLocalCallable c5 = getNextAndAssert(c4, InitThreadLocalCallable.class);
    assertSame(HttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST, ((InitThreadLocalCallable) c5).getThreadLocal());

    // 6. InitThreadLocalCallable for ServiceTunnelServlet.CURRENT_RESPONSE
    InitThreadLocalCallable c6 = getNextAndAssert(c5, InitThreadLocalCallable.class);
    assertSame(HttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE, ((InitThreadLocalCallable) c6).getThreadLocal());

    // 7. InitThreadLocalCallable for ISession.CURRENT
    InitThreadLocalCallable c7 = getNextAndAssert(c6, InitThreadLocalCallable.class);
    assertSame(ISession.CURRENT, ((InitThreadLocalCallable) c7).getThreadLocal());

    // 8. InitThreadLocalCallable for ScoutLocale.CURRENT
    InitThreadLocalCallable c8 = getNextAndAssert(c7, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c8).getThreadLocal());

    // 9. InitThreadLocalCallable for ScoutTexts.CURRENT
    InitThreadLocalCallable c9 = getNextAndAssert(c8, InitThreadLocalCallable.class);
    assertSame(ScoutTexts.CURRENT, ((InitThreadLocalCallable) c9).getThreadLocal());

    // 10. InitThreadLocalCallable for ITransaction.CURRENT
    InitThreadLocalCallable c10 = getNextAndAssert(c9, InitThreadLocalCallable.class);
    assertSame(ITransaction.CURRENT, ((InitThreadLocalCallable) c10).getThreadLocal());

    // 11. SubjectCallable
    SubjectCallable c11 = getNextAndAssert(c10, SubjectCallable.class);

    // 12. TwoPhaseTransactionBoundaryCallable
    TwoPhaseTransactionBoundaryCallable c12 = getNextAndAssert(c11, TwoPhaseTransactionBoundaryCallable.class);

    // 13. Contribution1
    Contribution1 c13 = getNextAndAssert(c12, Contribution1.class);

    // 14. Contribution2
    Contribution2 c14 = getNextAndAssert(c13, Contribution2.class);

    // 15. AsyncFutureCallable
    AsyncFutureCallable c15 = getNextAndAssert(c14, AsyncFutureCallable.class);

    // 16. ExceptionTranslator
    ExceptionTranslator c16 = getNextAndAssert(c15, ExceptionTranslator.class);

    // 17. TargetInvoker
    assertSame(callInvoker, c16.getNext());
  }

  /**
   * Tests that new contributions can be installed before the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsBefore() throws Exception {
    final IJobManager jobManager = mock(IJobManager.class);

    // verify Callable-Chain
    ArgumentCaptor<Callable> callableCaptor = ArgumentCaptor.forClass(Callable.class);
    doReturn(mock(Future.class)).when(jobManager).schedule(any(IJob.class), callableCaptor.capture());

    final Callable<Void> callInvoker = mock(Callable.class);
    ServerJob job = new ServerJob("job-1", mock(IServerSession.class)) {

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

    // 7. InitThreadLocalCallable for ServiceTunnelServlet.CURRENT_REQUEST
    InitThreadLocalCallable c7 = getNextAndAssert(c6, InitThreadLocalCallable.class);
    assertSame(HttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST, ((InitThreadLocalCallable) c7).getThreadLocal());

    // 8. InitThreadLocalCallable for ServiceTunnelServlet.CURRENT_RESPONSE
    InitThreadLocalCallable c8 = getNextAndAssert(c7, InitThreadLocalCallable.class);
    assertSame(HttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE, ((InitThreadLocalCallable) c8).getThreadLocal());

    // 9. InitThreadLocalCallable for ISession.CURRENT
    InitThreadLocalCallable c9 = getNextAndAssert(c8, InitThreadLocalCallable.class);
    assertSame(ISession.CURRENT, ((InitThreadLocalCallable) c9).getThreadLocal());

    // 10. InitThreadLocalCallable for ScoutLocale.CURRENT
    InitThreadLocalCallable c10 = getNextAndAssert(c9, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c10).getThreadLocal());

    // 11. InitThreadLocalCallable for ScoutTexts.CURRENT
    InitThreadLocalCallable c11 = getNextAndAssert(c10, InitThreadLocalCallable.class);
    assertSame(ScoutTexts.CURRENT, ((InitThreadLocalCallable) c11).getThreadLocal());

    // 12. InitThreadLocalCallable for ITransaction.CURRENT
    InitThreadLocalCallable c12 = getNextAndAssert(c11, InitThreadLocalCallable.class);
    assertSame(ITransaction.CURRENT, ((InitThreadLocalCallable) c12).getThreadLocal());

    // 13. SubjectCallable
    SubjectCallable c13 = getNextAndAssert(c12, SubjectCallable.class);

    // 14. TwoPhaseTransactionBoundaryCallable
    TwoPhaseTransactionBoundaryCallable c14 = getNextAndAssert(c13, TwoPhaseTransactionBoundaryCallable.class);

    // 15. AsyncFutureCallable
    AsyncFutureCallable c15 = getNextAndAssert(c14, AsyncFutureCallable.class);

    // 16. ExceptionTranslator
    ExceptionTranslator c16 = getNextAndAssert(c15, ExceptionTranslator.class);

    // 17. TargetInvoker
    assertSame(callInvoker, c16.getNext());
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
