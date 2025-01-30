/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.context;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.chain.IChainable;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.PropertyMap;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.context.RunMonitorCancellableProcessor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.opentelemetry.OpenTelemetryContextProcessor;
import org.eclipse.scout.rt.platform.security.SubjectProcessor;
import org.eclipse.scout.rt.platform.transaction.TransactionProcessor;
import org.eclipse.scout.rt.platform.util.ThreadLocalProcessor;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ClientRunContextChainTest {

  /**
   * Tests the correct order of interceptors in {@link ClientRunContext}.
   */
  @Test
  public void testCallableChain() {
    CallableChain<Object> chain = new ClientRunContext() {
      @Override
      protected <RESULT> CallableChain<RESULT> createCallableChain() { // overwrite to be accessible in test
        return super.createCallableChain();
      }
    }.createCallableChain();

    Iterator<IChainable> chainIterator = chain.values().iterator();

    // 1. RunMonitorCancellableProcessor for null parent
    IChainable c = chainIterator.next();
    assertEquals(RunMonitorCancellableProcessor.class, c.getClass());
    assertSame(null, ((RunMonitorCancellableProcessor) c).getParentRunMonitor());

    // 2. ThreadLocalProcessor for RunContext.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(RunContext.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 3. OpenTelemetryContextProcessor
    c = chainIterator.next();
    assertEquals(OpenTelemetryContextProcessor.class, c.getClass());
    assertNull(RunContext.CURRENT.get().getOpenTelemetryContext());

    // 4. ThreadLocalProcessor for CorrelationId.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(CorrelationId.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 5. ThreadLocalProcessor for RunMonitor.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(RunMonitor.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 6. SubjectProcessor
    c = chainIterator.next();
    assertEquals(SubjectProcessor.class, c.getClass());

    // 7. DiagnosticContextValueProcessor
    c = chainIterator.next();
    assertEquals(DiagnosticContextValueProcessor.class, c.getClass());
    assertEquals("subject.principal.name", ((DiagnosticContextValueProcessor) c).getMdcKey());

    // 8. DiagnosticContextValueProcessor
    c = chainIterator.next();
    assertEquals(DiagnosticContextValueProcessor.class, c.getClass());
    assertEquals("scout.correlation.id", ((DiagnosticContextValueProcessor) c).getMdcKey());

    // 9. ThreadLocalProcessor for NlsLocale.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(NlsLocale.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 10. ThreadLocalProcessor for PropertyMap.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(PropertyMap.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 11. ThreadLocalProcessor for ISession.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(ISession.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 12. DiagnosticContextValueProcessor
    c = chainIterator.next();
    assertEquals(DiagnosticContextValueProcessor.class, c.getClass());
    assertEquals("scout.user.name", ((DiagnosticContextValueProcessor) c).getMdcKey());

    // 13. DiagnosticContextValueProcessor
    c = chainIterator.next();
    assertEquals(DiagnosticContextValueProcessor.class, c.getClass());
    assertEquals("scout.session.id", ((DiagnosticContextValueProcessor) c).getMdcKey());

    // 14. ThreadLocalProcessor for UserAgent.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(UserAgent.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 15. ThreadLocalProcessor for IDesktop.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(IDesktop.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 16. ThreadLocalProcessor for IOutline.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(IOutline.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 17. ThreadLocalProcessor for IForm.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(IForm.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 18. TransactionProcessor
    c = chainIterator.next();
    assertEquals(TransactionProcessor.class, c.getClass());
    assertFalse(chainIterator.hasNext());
  }
}
