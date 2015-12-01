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
package org.eclipse.scout.rt.client.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.util.Iterator;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.chain.IChainable;
import org.eclipse.scout.rt.platform.chain.InvocationChain;
import org.eclipse.scout.rt.platform.context.PropertyMap;
import org.eclipse.scout.rt.platform.context.RunContextIdentifiers;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.security.SubjectProcessor;
import org.eclipse.scout.rt.platform.util.ThreadLocalProcessor;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ClientRunContextChainTest {

  @Before
  public void before() {
    ISession.CURRENT.set(mock(IClientSession.class));
  }

  @After
  public void after() {
    ISession.CURRENT.remove();
  }

  /**
   * Tests the correct order of interceptors in {@link ClientRunContext}.
   */
  @Test
  public void testCallableChain() throws Exception {
    InvocationChain<Object> chain = new InvocationChain<Object>();
    new ClientRunContext().interceptInvocationChain(chain);

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

    // 9. ThreadLocalProcessor for UserAgent.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(UserAgent.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 10. ThreadLocalProcessor for ScoutTexts.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(ScoutTexts.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 11. ThreadLocalProcessor for IDesktop.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(IDesktop.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 12. ThreadLocalProcessor for IOutline.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(IOutline.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 13. ThreadLocalProcessor for IForm.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(IForm.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    assertFalse(chainIterator.hasNext());
  }
}
