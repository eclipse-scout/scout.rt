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
package org.eclipse.scout.rt.platform.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.util.Iterator;

import org.eclipse.scout.rt.platform.chain.IChainable;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.security.SubjectProcessor;
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
  public void testCallableChain() throws Exception {
    CallableChain<Object> chain = new CallableChain<Object>();
    new RunContext().interceptCallableChain(chain);

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

    assertFalse(chainIterator.hasNext());
  }
}
