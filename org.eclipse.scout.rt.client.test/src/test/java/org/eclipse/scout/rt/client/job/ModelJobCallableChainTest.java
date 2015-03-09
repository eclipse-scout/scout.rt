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
import static org.mockito.Mockito.mock;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.internal.JobManager;
import org.eclipse.scout.commons.job.internal.callable.Chainable;
import org.eclipse.scout.commons.job.internal.callable.ExceptionTranslator;
import org.eclipse.scout.commons.job.internal.callable.InitThreadLocalCallable;
import org.eclipse.scout.commons.job.internal.callable.SubjectCallable;
import org.eclipse.scout.commons.job.internal.callable.ThreadNameDecorator;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.internal.ModelJobManager;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.platform.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class ModelJobCallableChainTest {

  @Mock
  private Callable<Void> m_targetCallable;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    ISession.CURRENT.set(mock(IClientSession.class));
  }

  @After
  public void after() {
    ISession.CURRENT.remove();
  }

  /**
   * Tests the correct order of interceptors in {@link JobManager}.
   */
  @Test
  public void testCallableChain() throws Exception {
    Callable<Void> actualCallable = new _ModelJobManager().interceptCallable(m_targetCallable, ClientJobInput.defaults());

    // 1. ExceptionTranslator
    ExceptionTranslator c1 = getFirstAndAssert(actualCallable, ExceptionTranslator.class);

    // 2. ThreadNameDecorator
    ThreadNameDecorator c2 = getNextAndAssert(c1, ThreadNameDecorator.class);

    // 3. SubjectCallable
    SubjectCallable c3 = getNextAndAssert(c2, SubjectCallable.class);

    // 4. InitThreadLocalCallable for JobContext.CURRENT
    InitThreadLocalCallable c4 = getNextAndAssert(c3, InitThreadLocalCallable.class);
    assertSame(JobContext.CURRENT, ((InitThreadLocalCallable) c4).getThreadLocal());

    // 5. InitThreadLocalCallable for NlsLocale.CURRENT
    InitThreadLocalCallable c5 = getNextAndAssert(c4, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c5).getThreadLocal());

    // 6. InitThreadLocalCallable for ISession.CURRENT
    InitThreadLocalCallable c6 = getNextAndAssert(c5, InitThreadLocalCallable.class);
    assertSame(ISession.CURRENT, ((InitThreadLocalCallable) c6).getThreadLocal());

    // 7. InitThreadLocalCallable for UserAgent.CURRENT
    InitThreadLocalCallable c7 = getNextAndAssert(c6, InitThreadLocalCallable.class);
    assertSame(UserAgent.CURRENT, ((InitThreadLocalCallable) c7).getThreadLocal());

    // 8. InitThreadLocalCallable for ScoutTexts.CURRENT
    InitThreadLocalCallable c8 = getNextAndAssert(c7, InitThreadLocalCallable.class);
    assertSame(ScoutTexts.CURRENT, ((InitThreadLocalCallable) c8).getThreadLocal());

    // 9. Target
    assertSame(m_targetCallable, c8.getNext());
  }

  /**
   * Tests that new contributions can be installed after the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsAfter() throws Exception {
    _ModelJobManager jobManager = new _ModelJobManager() {

      @Override
      public <RESULT> Callable<RESULT> interceptCallable(Callable<RESULT> next, ClientJobInput input) {
        Callable<RESULT> p2 = new Contribution2<>(next); // executed 3th
        Callable<RESULT> p1 = new Contribution1<>(p2); // executed 2nd
        Callable<RESULT> head = super.interceptCallable(p1, input); // executed 1st
        return head;
      }
    };

    Callable<Void> actualCallable = jobManager.interceptCallable(m_targetCallable, ClientJobInput.defaults());

    // 1. ExceptionTranslator
    ExceptionTranslator c1 = getFirstAndAssert(actualCallable, ExceptionTranslator.class);

    // 2. ThreadNameDecorator
    ThreadNameDecorator c2 = getNextAndAssert(c1, ThreadNameDecorator.class);

    // 3. SubjectCallable
    SubjectCallable c3 = getNextAndAssert(c2, SubjectCallable.class);

    // 4. InitThreadLocalCallable for JobContext.CURRENT
    InitThreadLocalCallable c4 = getNextAndAssert(c3, InitThreadLocalCallable.class);
    assertSame(JobContext.CURRENT, ((InitThreadLocalCallable) c4).getThreadLocal());

    // 5. InitThreadLocalCallable for NlsLocale.CURRENT
    InitThreadLocalCallable c5 = getNextAndAssert(c4, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c5).getThreadLocal());

    // 6. InitThreadLocalCallable for ISession.CURRENT
    InitThreadLocalCallable c6 = getNextAndAssert(c5, InitThreadLocalCallable.class);
    assertSame(ISession.CURRENT, ((InitThreadLocalCallable) c6).getThreadLocal());

    // 7. InitThreadLocalCallable for UserAgent.CURRENT
    InitThreadLocalCallable c7 = getNextAndAssert(c6, InitThreadLocalCallable.class);
    assertSame(UserAgent.CURRENT, ((InitThreadLocalCallable) c7).getThreadLocal());

    // 8. InitThreadLocalCallable for ScoutTexts.CURRENT
    InitThreadLocalCallable c8 = getNextAndAssert(c7, InitThreadLocalCallable.class);
    assertSame(ScoutTexts.CURRENT, ((InitThreadLocalCallable) c8).getThreadLocal());

    // 9. Contribution1
    Contribution1 c9 = getNextAndAssert(c8, Contribution1.class);

    // 10. Contribution2
    Contribution2 c10 = getNextAndAssert(c9, Contribution2.class);

    // 11. Target
    assertSame(m_targetCallable, c10.getNext());
  }

  /**
   * Tests that new contributions can be installed before the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsBefore() throws Exception {
    _ModelJobManager jobManager = new _ModelJobManager() {

      @Override
      public <RESULT> Callable<RESULT> interceptCallable(Callable<RESULT> next, ClientJobInput input) {
        Callable<RESULT> p2 = super.interceptCallable(next, input); // executed 3th
        Callable<RESULT> p1 = new Contribution2<>(p2); // executed 2nd
        Callable<RESULT> head = new Contribution1<>(p1); // executed 1st
        return head;
      }
    };

    Callable<Void> actualCallable = jobManager.interceptCallable(m_targetCallable, ClientJobInput.defaults());

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

    // 7. InitThreadLocalCallable for NlsLocale.CURRENT
    InitThreadLocalCallable c7 = getNextAndAssert(c6, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c7).getThreadLocal());

    // 8. InitThreadLocalCallable for ISession.CURRENT
    InitThreadLocalCallable c8 = getNextAndAssert(c7, InitThreadLocalCallable.class);
    assertSame(ISession.CURRENT, ((InitThreadLocalCallable) c8).getThreadLocal());

    // 9. InitThreadLocalCallable for UserAgent.CURRENT
    InitThreadLocalCallable c9 = getNextAndAssert(c8, InitThreadLocalCallable.class);
    assertSame(UserAgent.CURRENT, ((InitThreadLocalCallable) c9).getThreadLocal());

    // 10. InitThreadLocalCallable for ScoutTexts.CURRENT
    InitThreadLocalCallable c10 = getNextAndAssert(c9, InitThreadLocalCallable.class);
    assertSame(ScoutTexts.CURRENT, ((InitThreadLocalCallable) c10).getThreadLocal());

    // 11. Target
    assertSame(m_targetCallable, c10.getNext());
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

  private class _ModelJobManager extends ModelJobManager {

    @Override
    public <RESULT> Callable<RESULT> interceptCallable(final Callable<RESULT> next, final ClientJobInput input) {
      return super.interceptCallable(next, input);
    }
  }
}
