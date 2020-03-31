/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.context;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.eclipse.scout.rt.platform.chain.IChainable;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.security.SubjectProcessor;
import org.eclipse.scout.rt.platform.transaction.TransactionProcessor;
import org.eclipse.scout.rt.platform.util.ThreadLocalProcessor;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class RunContextChainTest {

  /**
   * Tests the correct order of interceptors in {@link RunContext}.
   */
  @Test
  public void testCallableChain() {
    CallableChain<Object> chain = new RunContext().createCallableChain();

    Iterator<IChainable> chainIterator = chain.values().iterator();

    // 1. RunMonitorCancellableProcessor for null parent
    IChainable c = chainIterator.next();
    assertEquals(RunMonitorCancellableProcessor.class, c.getClass());
    assertSame(null, ((RunMonitorCancellableProcessor) c).getParentRunMonitor());

    // 2. ThreadLocalProcessor for RunContext.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(RunContext.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 3. ThreadLocalProcessor for CorrelationId.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(CorrelationId.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 4. ThreadLocalProcessor for RunMonitor.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(RunMonitor.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 5. SubjectProcessor
    c = chainIterator.next();
    assertEquals(SubjectProcessor.class, c.getClass());

    // 6. DiagnosticContextValueProcessor
    c = chainIterator.next();
    assertEquals(DiagnosticContextValueProcessor.class, c.getClass());
    assertEquals("subject.principal.name", ((DiagnosticContextValueProcessor) c).getMdcKey());

    // 7. DiagnosticContextValueProcessor
    c = chainIterator.next();
    assertEquals(DiagnosticContextValueProcessor.class, c.getClass());
    assertEquals("scout.correlation.id", ((DiagnosticContextValueProcessor) c).getMdcKey());

    // 8. ThreadLocalProcessor for NlsLocale.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(NlsLocale.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 9. ThreadLocalProcessor for PropertyMap.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(PropertyMap.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 9. TransactionProcessor
    c = chainIterator.next();
    assertEquals(TransactionProcessor.class, c.getClass());
    assertFalse(chainIterator.hasNext());
  }
}
