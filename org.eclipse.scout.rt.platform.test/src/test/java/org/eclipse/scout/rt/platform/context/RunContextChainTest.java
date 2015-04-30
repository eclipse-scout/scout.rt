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
package org.eclipse.scout.rt.platform.context;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.IChainable;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.context.internal.InitThreadLocalCallable;
import org.eclipse.scout.rt.platform.context.internal.RunMonitorCallable;
import org.eclipse.scout.rt.platform.context.internal.SubjectCallable;
import org.eclipse.scout.rt.platform.job.PropertyMap;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class RunContextChainTest {

  @Mock
  private Callable<Void> m_targetCallable;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Tests the correct order of interceptors in {@link RunContext}.
   */
  @Test
  public void testCallableChain() throws Exception {
    Callable<Void> actualCallable = new RunContext().interceptCallable(m_targetCallable);

    // 1. RunMonitorCallable
    RunMonitorCallable c1 = getFirstAndAssert(actualCallable, RunMonitorCallable.class);

    // 2. SubjectCallable
    SubjectCallable c2 = getNextAndAssert(c1, SubjectCallable.class);

    // 2. InitThreadLocalCallable for NlsLocale.CURRENT
    InitThreadLocalCallable c3 = getNextAndAssert(c2, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c3).getThreadLocal());

    // 3. InitThreadLocalCallable for PropertyMap.CURRENT
    InitThreadLocalCallable c4 = getNextAndAssert(c3, InitThreadLocalCallable.class);
    assertSame(PropertyMap.CURRENT, ((InitThreadLocalCallable) c4).getThreadLocal());

    // 4. Target
    assertSame(m_targetCallable, c4.getNext());
  }

  /**
   * Tests that new contributions can be installed after the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsAfter() throws Exception {
    RunContext runContext = new RunContext() {

      @Override
      protected <RESULT> Callable<RESULT> interceptCallable(Callable<RESULT> next) {
        Callable<RESULT> p2 = new Contribution2<>(next); // executed 3th
        Callable<RESULT> p1 = new Contribution1<>(p2); // executed 2nd
        Callable<RESULT> head = super.interceptCallable(p1); // executed 1st
        return head;
      }
    };

    Callable<Void> actualCallable = runContext.interceptCallable(m_targetCallable);

    // 1. RunMonitorCallable
    RunMonitorCallable c1 = getFirstAndAssert(actualCallable, RunMonitorCallable.class);

    // 2. SubjectCallable
    SubjectCallable c2 = getNextAndAssert(c1, SubjectCallable.class);

    // 3. InitThreadLocalCallable for NlsLocale.CURRENT
    InitThreadLocalCallable c3 = getNextAndAssert(c2, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c3).getThreadLocal());

    // 4. InitThreadLocalCallable for PropertyMap.CURRENT
    InitThreadLocalCallable c4 = getNextAndAssert(c3, InitThreadLocalCallable.class);
    assertSame(PropertyMap.CURRENT, ((InitThreadLocalCallable) c4).getThreadLocal());

    // 5. Contribution1
    Contribution1 c5 = getNextAndAssert(c4, Contribution1.class);

    // 6. Contribution2
    Contribution2 c6 = getNextAndAssert(c5, Contribution2.class);

    // 7. Target
    assertSame(m_targetCallable, c6.getNext());
  }

  /**
   * Tests that new contributions can be installed before the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsBefore() throws Exception {
    RunContext runContext = new RunContext() {

      @Override
      protected <RESULT> Callable<RESULT> interceptCallable(Callable<RESULT> next) {
        Callable<RESULT> p2 = super.interceptCallable(next); // executed 3th
        Callable<RESULT> p1 = new Contribution2<>(p2); // executed 2nd
        Callable<RESULT> head = new Contribution1<>(p1); // executed 1st
        return head;
      }
    };

    Callable<Void> actualCallable = runContext.interceptCallable(m_targetCallable);

    // 1. Contribution1
    Contribution1 c1 = getFirstAndAssert(actualCallable, Contribution1.class);

    // 2. Contribution2
    Contribution2 c2 = getNextAndAssert(c1, Contribution2.class);

    // 3. RunMonitorCallable
    RunMonitorCallable c3 = getNextAndAssert(c2, RunMonitorCallable.class);

    // 4. SubjectCallable
    SubjectCallable c4 = getNextAndAssert(c3, SubjectCallable.class);

    // 5. InitThreadLocalCallable for NlsLocale.CURRENT
    InitThreadLocalCallable c5 = getNextAndAssert(c4, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c5).getThreadLocal());

    // 6. InitThreadLocalCallable for PropertyMap.CURRENT
    InitThreadLocalCallable c6 = getNextAndAssert(c5, InitThreadLocalCallable.class);
    assertSame(PropertyMap.CURRENT, ((InitThreadLocalCallable) c6).getThreadLocal());

    // 7. Target
    assertSame(m_targetCallable, c6.getNext());
  }

  @SuppressWarnings("unchecked")
  private static <RESULT, TYPE> TYPE getFirstAndAssert(Callable<RESULT> first, Class<TYPE> expectedType) {
    assertTrue(expectedType.equals(first.getClass()));
    return (TYPE) first;
  }

  @SuppressWarnings("unchecked")
  private static <RESULT, TYPE> TYPE getNextAndAssert(IChainable<?> c, Class<TYPE> expectedType) {
    Callable<?> next = (Callable<?>) c.getNext();
    assertTrue(expectedType.equals(next.getClass()));
    return (TYPE) next;
  }

  private static class Contribution1<RESULT> implements Callable<RESULT>, IChainable<Callable<RESULT>> {

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

  private static class Contribution2<RESULT> implements Callable<RESULT>, IChainable<Callable<RESULT>> {

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
}
