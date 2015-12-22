/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.util.Iterator;

import org.eclipse.scout.rt.platform.chain.IChainable;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.context.PropertyMap;
import org.eclipse.scout.rt.platform.context.RunContextIdentifiers;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.security.SubjectProcessor;
import org.eclipse.scout.rt.platform.util.ThreadLocalProcessor;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationCollector;
import org.eclipse.scout.rt.server.clientnotification.IClientNodeId;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ServerRunContextChainTest {

  @Before
  public void before() {
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
    CallableChain<Object> chain = new CallableChain<Object>();
    new ServerRunContext().interceptCallableChain(chain);

    Iterator<IChainable> chainIterator = chain.values().iterator();

    // 1. ThreadLocalProcessor for RunMonitor.CURRENT
    IChainable c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(RunMonitor.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 2. SubjectProcessor
    c = (IChainable) chainIterator.next();
    assertEquals(SubjectProcessor.class, c.getClass());

    // 3. DiagnosticContextValueProcessor
    c = chainIterator.next();
    assertEquals(DiagnosticContextValueProcessor.class, c.getClass());
    assertEquals("subject.principal.name", ((DiagnosticContextValueProcessor) c).getMdcKey());

    // 4. ThreadLocalProcessor for NlsLocale.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(NlsLocale.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 5. ThreadLocalProcessor for PropertyMap.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(PropertyMap.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 6. ThreadLocalProcessor for RunContextIdentifiers.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(RunContextIdentifiers.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 7. ThreadLocalProcessor for ISession.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(ISession.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 8. DiagnosticContextValueProcessor
    c = chainIterator.next();
    assertEquals(DiagnosticContextValueProcessor.class, c.getClass());
    assertEquals("scout.user.name", ((DiagnosticContextValueProcessor) c).getMdcKey());

    // 9. DiagnosticContextValueProcessor
    c = chainIterator.next();
    assertEquals(DiagnosticContextValueProcessor.class, c.getClass());
    assertEquals("scout.session.id", ((DiagnosticContextValueProcessor) c).getMdcKey());

    // 10. ThreadLocalProcessor for ISession.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(UserAgent.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 11. ThreadLocalProcessor for ClientNodeId.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(IClientNodeId.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 12. ThreadLocalProcessor for TransactionalClientNotificationCollector.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(ClientNotificationCollector.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 13. ThreadLocalProcessor for ScoutTexts.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(ScoutTexts.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 14. TransactionProcessor
    c = chainIterator.next();
    assertEquals(TransactionProcessor.class, c.getClass());

    assertFalse(chainIterator.hasNext());
  }
}
