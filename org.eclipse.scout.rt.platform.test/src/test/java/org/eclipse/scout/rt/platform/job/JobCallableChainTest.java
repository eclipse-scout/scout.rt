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
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IChainable;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.platform.job.internal.callable.ApplyRunContextCallable;
import org.eclipse.scout.rt.platform.job.internal.callable.HandleExceptionCallable;
import org.eclipse.scout.rt.platform.job.internal.callable.ThreadNameDecorator;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class JobCallableChainTest {

  @Mock
  private ICallable<Void> m_targetCallable;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Tests the correct order of interceptors in {@link JobManager}.
   */
  @Test
  public void testCallableChain() throws Exception {
    ICallable<Void> actualCallable = new _JobManager().interceptCallable(m_targetCallable, JobInput.fillEmpty());

    // 1. HandleExceptionCallable
    HandleExceptionCallable c1 = getFirstAndAssert(actualCallable, HandleExceptionCallable.class);

    // 2. ThreadNameDecorator
    ThreadNameDecorator c2 = getNextAndAssert(c1, ThreadNameDecorator.class);

    // 3. ApplyContextCallable
    ApplyRunContextCallable c3 = getNextAndAssert(c2, ApplyRunContextCallable.class);

    // 4. Target
    assertSame(m_targetCallable, c3.getNext());
  }

  /**
   * Tests that new contributions can be installed after the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsAfter() throws Exception {
    _JobManager jobManager = new _JobManager() {
      @Override
      public <RESULT> ICallable<RESULT> interceptCallable(ICallable<RESULT> next, JobInput input) {
        ICallable<RESULT> p2 = new Contribution2<>(next); // executed 3th
        ICallable<RESULT> p1 = new Contribution1<>(p2); // executed 2nd
        ICallable<RESULT> head = super.interceptCallable(p1, input); // executed 1st
        return head;
      }
    };

    ICallable<Void> actualCallable = jobManager.interceptCallable(m_targetCallable, JobInput.fillEmpty());

    // 1. HandleExceptionCallable
    HandleExceptionCallable c1 = getFirstAndAssert(actualCallable, HandleExceptionCallable.class);

    // 2. ThreadNameDecorator
    ThreadNameDecorator c2 = getNextAndAssert(c1, ThreadNameDecorator.class);

    // 3. ApplyContextCallable
    ApplyRunContextCallable c3 = getNextAndAssert(c2, ApplyRunContextCallable.class);

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
    _JobManager jobManager = new _JobManager() {
      @Override
      public <RESULT> ICallable<RESULT> interceptCallable(ICallable<RESULT> next, JobInput input) {
        ICallable<RESULT> p2 = super.interceptCallable(next, input); // executed 3th
        ICallable<RESULT> p1 = new Contribution2<>(p2); // executed 2nd
        ICallable<RESULT> head = new Contribution1<>(p1); // executed 1st
        return head;
      }
    };

    ICallable<Void> actualCallable = jobManager.interceptCallable(m_targetCallable, JobInput.fillEmpty());

    // 1. Contribution1
    Contribution1 c1 = getFirstAndAssert(actualCallable, Contribution1.class);

    // 2. Contribution2
    Contribution2 c2 = getNextAndAssert(c1, Contribution2.class);

    // 3. HandleExceptionCallable
    HandleExceptionCallable c3 = getNextAndAssert(c2, HandleExceptionCallable.class);

    // 4. ThreadNameDecorator
    ThreadNameDecorator c4 = getNextAndAssert(c3, ThreadNameDecorator.class);

    // 5. ApplyContextCallable
    ApplyRunContextCallable c5 = getNextAndAssert(c4, ApplyRunContextCallable.class);

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

  private class _JobManager extends JobManager {

    @Override
    public <RESULT> ICallable<RESULT> interceptCallable(ICallable<RESULT> next, JobInput input) {
      return super.interceptCallable(next, input);
    }

    @Override
    protected void finalize() throws Throwable {
      shutdown();
    }
  }
}
