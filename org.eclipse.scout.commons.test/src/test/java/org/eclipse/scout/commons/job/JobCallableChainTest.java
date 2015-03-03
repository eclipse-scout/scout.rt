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

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.job.internal.JobManager;
import org.eclipse.scout.commons.job.internal.callable.Chainable;
import org.eclipse.scout.commons.job.internal.callable.ExceptionTranslator;
import org.eclipse.scout.commons.job.internal.callable.InitThreadLocalCallable;
import org.eclipse.scout.commons.job.internal.callable.SubjectCallable;
import org.eclipse.scout.commons.job.internal.callable.ThreadNameDecorator;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JobCallableChainTest {

  @Mock
  private Callable<Void> m_targetCallable;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Tests the correct order of interceptors in {@link JobManager}.
   */
  @Test
  public void testCallableChain() throws Exception {
    Callable<Void> actualCallable = new _JobManager().interceptCallable(m_targetCallable, JobInput.empty());

    // 1. ExceptionTranslator
    ExceptionTranslator c1 = getFirstAndAssert(actualCallable, ExceptionTranslator.class);

    // 2. ThreadNameDecorator
    ThreadNameDecorator c2 = getNextAndAssert(c1, ThreadNameDecorator.class);

    // 3. SubjectCallable
    SubjectCallable c3 = getNextAndAssert(c2, SubjectCallable.class);

    // 4. InitThreadLocalCallable for JobContext.CURRENT
    InitThreadLocalCallable c4 = getNextAndAssert(c3, InitThreadLocalCallable.class);
    assertSame(JobContext.CURRENT, ((InitThreadLocalCallable) c4).getThreadLocal());

    // 5. InitThreadLocalCallable for ScoutTexts.CURRENT
    InitThreadLocalCallable c5 = getNextAndAssert(c4, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c5).getThreadLocal());

    // 6. Target
    assertSame(m_targetCallable, c5.getNext());
  }

  /**
   * Tests that new contributions can be installed after the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsAfter() throws Exception {
    _JobManager jobManager = new _JobManager() {
      @Override
      public <RESULT> Callable<RESULT> interceptCallable(Callable<RESULT> next, IJobInput input) {
        Callable<RESULT> p2 = new Contribution2<>(next); // executed 3th
        Callable<RESULT> p1 = new Contribution1<>(p2); // executed 2nd
        Callable<RESULT> head = super.interceptCallable(p1, input); // executed 1st
        return head;
      }
    };

    Callable<Void> actualCallable = jobManager.interceptCallable(m_targetCallable, JobInput.empty());

    // 1. ExceptionTranslator
    ExceptionTranslator c1 = getFirstAndAssert(actualCallable, ExceptionTranslator.class);

    // 2. ThreadNameDecorator
    ThreadNameDecorator c2 = getNextAndAssert(c1, ThreadNameDecorator.class);

    // 3. SubjectCallable
    SubjectCallable c3 = getNextAndAssert(c2, SubjectCallable.class);

    // 4. InitThreadLocalCallable for JobContext.CURRENT
    InitThreadLocalCallable c4 = getNextAndAssert(c3, InitThreadLocalCallable.class);
    assertSame(JobContext.CURRENT, ((InitThreadLocalCallable) c4).getThreadLocal());

    // 5. InitThreadLocalCallable for ScoutTexts.CURRENT
    InitThreadLocalCallable c5 = getNextAndAssert(c4, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c5).getThreadLocal());

    // 6. Contribution1
    Contribution1 c6 = getNextAndAssert(c5, Contribution1.class);

    // 7. Contribution2
    Contribution2 c7 = getNextAndAssert(c6, Contribution2.class);

    // 8. Target
    assertSame(m_targetCallable, c7.getNext());
  }

  /**
   * Tests that new contributions can be installed before the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsBefore() throws Exception {
    _JobManager jobManager = new _JobManager() {
      @Override
      public <RESULT> Callable<RESULT> interceptCallable(Callable<RESULT> next, IJobInput input) {
        Callable<RESULT> p2 = super.interceptCallable(next, input); // executed 3th
        Callable<RESULT> p1 = new Contribution2<>(p2); // executed 2nd
        Callable<RESULT> head = new Contribution1<>(p1); // executed 1st
        return head;
      }
    };

    Callable<Void> actualCallable = jobManager.interceptCallable(m_targetCallable, JobInput.empty());

    // 1. Contribution1
    Contribution1 c1 = getFirstAndAssert(actualCallable, Contribution1.class);

    // 2. Contribution2
    Contribution2 c2 = getNextAndAssert(c1, Contribution2.class);

    // 3. ExceptionTranslator
    ExceptionTranslator c3 = getNextAndAssert(c2, ExceptionTranslator.class);

    // 4. ThreadNameDecorator
    ThreadNameDecorator c4 = getNextAndAssert(c3, ThreadNameDecorator.class);

    // 5. SubjectCallable
    SubjectCallable c5 = getNextAndAssert(c4, SubjectCallable.class);

    // 6. InitThreadLocalCallable for JobContext.CURRENT
    InitThreadLocalCallable c6 = getNextAndAssert(c5, InitThreadLocalCallable.class);
    assertSame(JobContext.CURRENT, ((InitThreadLocalCallable) c6).getThreadLocal());

    // 7. InitThreadLocalCallable for ScoutTexts.CURRENT
    InitThreadLocalCallable c7 = getNextAndAssert(c6, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c7).getThreadLocal());

    // 8. Target
    assertSame(m_targetCallable, c7.getNext());
  }

  @SuppressWarnings("unchecked")
  private static <RESULT, TYPE> TYPE getFirstAndAssert(Callable<RESULT> first, Class<TYPE> expectedType) {
    assertTrue(expectedType.equals(first.getClass()));
    return (TYPE) first;
  }

  @SuppressWarnings("unchecked")
  private static <RESULT, TYPE> TYPE getNextAndAssert(Chainable<?> c, Class<TYPE> expectedType) {
    Callable<?> next = c.getNext();
    assertTrue(expectedType.equals(next.getClass()));
    return (TYPE) next;
  }

  private static class Contribution1<RESULT> implements Callable<RESULT>, Chainable<RESULT> {

    private final Callable<RESULT> m_next;

    public Contribution1(Callable<RESULT> next) {
      m_next = next;
    }

    @Override
    public RESULT call() throws Exception {
      return m_next.call();
    }

    @Override
    public Callable<RESULT> getNext() {
      return m_next;
    }
  }

  private static class Contribution2<RESULT> implements Callable<RESULT>, Chainable<RESULT> {

    private final Callable<RESULT> m_next;

    public Contribution2(Callable<RESULT> next) {
      m_next = next;
    }

    @Override
    public RESULT call() throws Exception {
      return m_next.call();
    }

    @Override
    public Callable<RESULT> getNext() {
      return m_next;
    }
  }

  private class _JobManager extends JobManager<IJobInput> {

    public _JobManager() {
      super("scout-thread");
    }

    @Override
    public <RESULT> Callable<RESULT> interceptCallable(Callable<RESULT> next, IJobInput input) {
      return super.interceptCallable(next, input);
    }

    @Override
    protected void finalize() throws Throwable {
      shutdown();
    }
  }
}
