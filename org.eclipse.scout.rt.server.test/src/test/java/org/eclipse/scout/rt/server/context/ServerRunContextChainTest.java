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
package org.eclipse.scout.rt.server.context;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.IChainable;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.context.internal.CurrentSubjectLogCallable;
import org.eclipse.scout.rt.platform.context.internal.InitThreadLocalCallable;
import org.eclipse.scout.rt.platform.context.internal.SubjectCallable;
import org.eclipse.scout.rt.platform.job.PropertyMap;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationContainer;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationNodeId;
import org.eclipse.scout.rt.server.context.internal.CurrentSessionLogCallable;
import org.eclipse.scout.rt.server.context.internal.TwoPhaseTransactionBoundaryCallable;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.OfflineState;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class ServerRunContextChainTest {

  @Mock
  private Callable<Void> m_targetCallable;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    ISession.CURRENT.set(mock(IServerSession.class));
  }

  @After
  public void after() {
    ISession.CURRENT.remove();
  }

  /**
   * Tests the correct order of interceptors in {@link ServerRunContext}.
   */
  @Test
  public void testCallableChain() throws Exception {
    Callable<Void> actualCallable = new ServerRunContext().interceptCallable(m_targetCallable);

    // 1. Callable Chain
    IChainable<?> last = assertCallableChain((IChainable<?>) actualCallable);

    // 2. Target
    assertSame(m_targetCallable, last.getNext());
  }

  /**
   * Tests that new contributions can be installed after the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsAfter() throws Exception {
    ServerRunContext serverRunContext = new ServerRunContext() {

      @Override
      protected <RESULT> Callable<RESULT> interceptCallable(Callable<RESULT> next) {
        Callable<RESULT> p2 = new Contribution2<>(next); // executed 3th
        Callable<RESULT> p1 = new Contribution1<>(p2); // executed 2nd
        Callable<RESULT> head = super.interceptCallable(p1); // executed 1st
        return head;
      }
    };

    Callable<Void> actualCallable = serverRunContext.interceptCallable(m_targetCallable);

    // 1. Callable Chain
    IChainable<?> last = assertCallableChain((IChainable<?>) actualCallable);

    // 2. Contribution1
    Contribution1 contribution1 = getNextAndAssert(last, Contribution1.class);

    // 3. Contribution2
    Contribution2 contribution2 = getNextAndAssert(contribution1, Contribution2.class);

    // 4. Target
    assertSame(m_targetCallable, contribution2.getNext());
  }

  /**
   * Tests that new contributions can be installed before the default contributions.
   */
  @Test
  public void testCallableChainWithContributionsBefore() throws Exception {
    ServerRunContext serverRunContext = new ServerRunContext() {

      @Override
      protected <RESULT> Callable<RESULT> interceptCallable(Callable<RESULT> next) {
        Callable<RESULT> p2 = super.interceptCallable(next); // executed 3th
        Callable<RESULT> p1 = new Contribution2<>(p2); // executed 2nd
        Callable<RESULT> head = new Contribution1<>(p1); // executed 1st
        return head;
      }
    };

    @SuppressWarnings("unchecked")
    IChainable<Void> actualCallable = (IChainable<Void>) serverRunContext.interceptCallable(m_targetCallable);

    // 1. Contribution1
    assertTrue(Contribution1.class.equals(actualCallable.getClass()));

    // 2. Contribution2
    Contribution2 contribution2 = getNextAndAssert(actualCallable, Contribution2.class);

    // 3. Callable Chain
    IChainable<?> last = assertCallableChain((IChainable<?>) contribution2.getNext());

    // 4. Target
    assertSame(m_targetCallable, last.getNext());
  }

  private IChainable<?> assertCallableChain(IChainable<?> c1) throws Exception {
    // 1. InitThreadLocalCallable for RunMonitor.CURRENT
    assertTrue(InitThreadLocalCallable.class.equals(c1.getClass()));
    assertSame(RunMonitor.CURRENT, ((InitThreadLocalCallable) c1).getThreadLocal());

    // 2. SubjectCallable
    SubjectCallable c2 = getNextAndAssert(c1, SubjectCallable.class);

    // 3. CurrentSubjectLogCallable
    CurrentSubjectLogCallable c3 = getNextAndAssert(c2, CurrentSubjectLogCallable.class);

    // 4. InitThreadLocalCallable for NlsLocale.CURRENT
    InitThreadLocalCallable c4 = getNextAndAssert(c3, InitThreadLocalCallable.class);
    assertSame(NlsLocale.CURRENT, ((InitThreadLocalCallable) c4).getThreadLocal());

    // 5. InitThreadLocalCallable for PropertyMap.CURRENT
    InitThreadLocalCallable c5 = getNextAndAssert(c4, InitThreadLocalCallable.class);
    assertSame(PropertyMap.CURRENT, ((InitThreadLocalCallable) c5).getThreadLocal());

    // 6. InitThreadLocalCallable for OfflineState.CURRENT
    InitThreadLocalCallable c6 = getNextAndAssert(c5, InitThreadLocalCallable.class);
    assertSame(OfflineState.CURRENT, ((InitThreadLocalCallable) c6).getThreadLocal());

    // 7. InitThreadLocalCallable for ISession.CURRENT
    InitThreadLocalCallable c7 = getNextAndAssert(c6, InitThreadLocalCallable.class);

    // 8. CurrentSessionLogCallable
    CurrentSessionLogCallable c8 = getNextAndAssert(c7, CurrentSessionLogCallable.class);

    // 9. InitThreadLocalCallable for UserAgent.CURRENT
    InitThreadLocalCallable c9 = getNextAndAssert(c8, InitThreadLocalCallable.class);
    assertSame(UserAgent.CURRENT, ((InitThreadLocalCallable) c9).getThreadLocal());

    // 10. InitThreadLocalCallable for NotificationNodeId.CURRENT
    InitThreadLocalCallable c10 = getNextAndAssert(c9, InitThreadLocalCallable.class);
    assertSame(ClientNotificationNodeId.CURRENT, ((InitThreadLocalCallable) c10).getThreadLocal());

    // 11. InitThreadLocalCallable for NotificationNodeId.CURRENT
    InitThreadLocalCallable c11 = getNextAndAssert(c10, InitThreadLocalCallable.class);
    assertSame(ClientNotificationContainer.CURRENT, ((InitThreadLocalCallable) c11).getThreadLocal());

    // 12. InitThreadLocalCallable for ScoutTexts.CURRENT
    InitThreadLocalCallable c12 = getNextAndAssert(c11, InitThreadLocalCallable.class);
    assertSame(ScoutTexts.CURRENT, ((InitThreadLocalCallable) c12).getThreadLocal());

    // 13. TwoPhaseTransactionBoundaryCallable
    TwoPhaseTransactionBoundaryCallable c13 = getNextAndAssert(c12, TwoPhaseTransactionBoundaryCallable.class);

    return c13;
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
