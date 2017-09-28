/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.util.Iterator;

import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.chain.IChainable;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.context.RunContextRunner;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.util.ThreadLocalProcessor;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobManagerChainTest {

  @Test
  public void testCallableChain() throws Exception {
    CallableChain<Object> chain = new CallableChain<Object>();

    new JobManager().interceptCallableChain(chain, mock(JobFutureTask.class), mock(RunMonitor.class), mock(JobInput.class));

    Iterator<IChainable> chainIterator = chain.values().iterator();

    // 1. CallableChainExceptionHandler
    IChainable c = chainIterator.next();
    assertEquals(CallableChainExceptionHandler.class, c.getClass());

    // 2. ThreadLocalProcessor for IFuture.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(IFuture.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 3. ThreadLocalProcessor for RunMonitor.CURRENT
    c = chainIterator.next();
    assertEquals(ThreadLocalProcessor.class, c.getClass());
    assertSame(RunMonitor.CURRENT, ((ThreadLocalProcessor) c).getThreadLocal());

    // 4. ThreadNameDecorator
    c = (IChainable) chainIterator.next();
    if (Platform.get().inDevelopmentMode()) {
      assertEquals(DevelopmentThreadNameDecorator.class, c.getClass());
    }
    else {
      assertEquals(ThreadNameDecorator.class, c.getClass());
    }

    // 5. JobNameContextValueProvider (MDC)
    c = (IChainable) chainIterator.next();
    assertEquals(DiagnosticContextValueProcessor.class, c.getClass());
    assertEquals("scout.job.name", ((DiagnosticContextValueProcessor) c).getMdcKey());

    // 6. RunContextRunner
    c = (IChainable) chainIterator.next();
    assertEquals(RunContextRunner.class, c.getClass());

    // 7. ExceptionProcessor
    c = (IChainable) chainIterator.next();
    assertEquals(ExceptionProcessor.class, c.getClass());

    assertFalse(chainIterator.hasNext());
  }
}
