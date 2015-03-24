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

import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IChainable;
import org.eclipse.scout.commons.nls.NlsLocale;
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
  private ICallable<Void> m_targetCallable;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Tests the correct order of interceptors in {@link RunContext}.
   */
  @Test
  public void testCallableChain() throws Exception {
    ICallable<Void> actualCallable = new RunContext().interceptCallable(m_targetCallable);

    // 1. SubjectCallable
    SubjectCallable c1 = getFirstAndAssert(actualCallable, SubjectCallable.class);

    // 2. InitThreadLocalCallable for NlsLocale.CURRENT
    InitThreadLocalCallable c2 = getNextAndAssert(c1, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c2).getThreadLocal());

    // 3. InitThreadLocalCallable for PropertyMap.CURRENT
    InitThreadLocalCallable c3 = getNextAndAssert(c2, InitThreadLocalCallable.class);
    assertSame(PropertyMap.CURRENT, ((InitThreadLocalCallable) c3).getThreadLocal());

    // 4. Target
    assertSame(m_targetCallable, c3.getNext());
  }

  /**
   * Tests that new contributions can be installed after the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsAfter() throws Exception {
    RunContext runContext = new RunContext() {

      @Override
      protected <RESULT> ICallable<RESULT> interceptCallable(ICallable<RESULT> next) {
        ICallable<RESULT> p2 = new Contribution2<>(next); // executed 3th
        ICallable<RESULT> p1 = new Contribution1<>(p2); // executed 2nd
        ICallable<RESULT> head = super.interceptCallable(p1); // executed 1st
        return head;
      }
    };

    ICallable<Void> actualCallable = runContext.interceptCallable(m_targetCallable);

    // 1. SubjectCallable
    SubjectCallable c1 = getFirstAndAssert(actualCallable, SubjectCallable.class);

    // 2. InitThreadLocalCallable for NlsLocale.CURRENT
    InitThreadLocalCallable c2 = getNextAndAssert(c1, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c2).getThreadLocal());

    // 3. InitThreadLocalCallable for PropertyMap.CURRENT
    InitThreadLocalCallable c3 = getNextAndAssert(c2, InitThreadLocalCallable.class);
    assertSame(PropertyMap.CURRENT, ((InitThreadLocalCallable) c3).getThreadLocal());

    // 4. Contribution1
    Contribution1 c4 = getNextAndAssert(c3, Contribution1.class);

    // 5. Contribution2
    Contribution2 c5 = getNextAndAssert(c4, Contribution2.class);

    // 6. Target
    assertSame(m_targetCallable, c5.getNext());
  }

  /**
   * Tests that new contributions can be installed before the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsBefore() throws Exception {
    RunContext runContext = new RunContext() {

      @Override
      protected <RESULT> ICallable<RESULT> interceptCallable(ICallable<RESULT> next) {
        ICallable<RESULT> p2 = super.interceptCallable(next); // executed 3th
        ICallable<RESULT> p1 = new Contribution2<>(p2); // executed 2nd
        ICallable<RESULT> head = new Contribution1<>(p1); // executed 1st
        return head;
      }
    };

    ICallable<Void> actualCallable = runContext.interceptCallable(m_targetCallable);

    // 1. Contribution1
    Contribution1 c1 = getFirstAndAssert(actualCallable, Contribution1.class);

    // 2. Contribution2
    Contribution2 c2 = getNextAndAssert(c1, Contribution2.class);

    // 3. SubjectCallable
    SubjectCallable c3 = getNextAndAssert(c2, SubjectCallable.class);

    // 4. InitThreadLocalCallable for NlsLocale.CURRENT
    InitThreadLocalCallable c4 = getNextAndAssert(c3, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c4).getThreadLocal());

    // 5. InitThreadLocalCallable for PropertyMap.CURRENT
    InitThreadLocalCallable c5 = getNextAndAssert(c4, InitThreadLocalCallable.class);
    assertSame(PropertyMap.CURRENT, ((InitThreadLocalCallable) c5).getThreadLocal());

    // 6. Target
    assertSame(m_targetCallable, c5.getNext());
  }

  @SuppressWarnings("unchecked")
  private static <RESULT, TYPE> TYPE getFirstAndAssert(ICallable<RESULT> first, Class<TYPE> expectedType) {
    assertTrue(expectedType.equals(first.getClass()));
    return (TYPE) first;
  }

  @SuppressWarnings("unchecked")
  private static <RESULT, TYPE> TYPE getNextAndAssert(IChainable<?> c, Class<TYPE> expectedType) {
    ICallable<?> next = (ICallable<?>) c.getNext();
    assertTrue(expectedType.equals(next.getClass()));
    return (TYPE) next;
  }

  private static class Contribution1<RESULT> implements ICallable<RESULT>, IChainable<ICallable<RESULT>> {

    private final ICallable<RESULT> m_next;

    public Contribution1(ICallable<RESULT> next) {
      m_next = next;
    }

    @Override
    public RESULT call() throws Exception {
      return m_next.call();
    }

    @Override
    public ICallable<RESULT> getNext() {
      return m_next;
    }
  }

  private static class Contribution2<RESULT> implements ICallable<RESULT>, IChainable<ICallable<RESULT>> {

    private final ICallable<RESULT> m_next;

    public Contribution2(ICallable<RESULT> next) {
      m_next = next;
    }

    @Override
    public RESULT call() throws Exception {
      return m_next.call();
    }

    @Override
    public ICallable<RESULT> getNext() {
      return m_next;
    }
  }
}
