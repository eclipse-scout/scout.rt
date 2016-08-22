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
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class RunMonitorJobTest {

  @After
  public void after() {
    RunMonitor.CURRENT.remove();
  }

  @Test
  public void testCurrentAndExplicitMonitor() throws Exception {
    final RunMonitorEx currentMonitor = new RunMonitorEx();
    final RunMonitorEx explicitMonitor = new RunMonitorEx();

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    RunMonitor.CURRENT.set(currentMonitor);
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        setupLatch.countDownAndBlock();

        assertSame(explicitMonitor, RunMonitor.CURRENT.get());
        assertNotSame(explicitMonitor, currentMonitor);
        assertEquals(1, currentMonitor.getCancellablesCount());//+1 from RunContexts.copyCurrent
        assertEquals(3, explicitMonitor.getCancellablesCount());//+1 from job cancellable, +1 from run monitor, +1 from transaction
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent().withRunMonitor(explicitMonitor)));

    assertTrue(setupLatch.await());
    assertSame(currentMonitor, RunMonitor.CURRENT.get());
    setupLatch.unblock();

    future.awaitDoneAndGet();
    assertEquals(1, currentMonitor.getCancellablesCount());//+1 from RunContexts.copyCurrent
    waitUntilCancellableCount(explicitMonitor, 0);
    assertSame(currentMonitor, RunMonitor.CURRENT.get());
  }

  @Test
  public void testNoCurrentAndExplicitMonitor() throws Exception {
    final RunMonitorEx explicitMonitor = new RunMonitorEx();

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    RunMonitor.CURRENT.remove();
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        setupLatch.countDownAndBlock();
        assertEquals(3, explicitMonitor.getCancellablesCount());//+1 from job cancellable, +1 from run monitor cancellable, +1 from transaction
        assertSame(explicitMonitor, RunMonitor.CURRENT.get());
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent().withRunMonitor(explicitMonitor)));

    assertTrue(setupLatch.await());
    assertNull(RunMonitor.CURRENT.get());
    setupLatch.unblock();

    future.awaitDoneAndGet();
    waitUntilCancellableCount(explicitMonitor, 0);
    assertNull(RunMonitor.CURRENT.get());
  }

  @Test
  public void testCurrentAndNoExplicitMonitor() throws Exception {
    final RunMonitorEx currentMonitor = new RunMonitorEx();
    RunMonitor.CURRENT.set(currentMonitor);

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        setupLatch.countDownAndBlock();

        assertTrue(currentMonitor.containsCancellable(RunMonitor.CURRENT.get()));
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    assertTrue(setupLatch.await());
    assertSame(currentMonitor, RunMonitor.CURRENT.get());
    setupLatch.unblock();

    future.awaitDoneAndGet();
    assertEquals(1, currentMonitor.getCancellablesCount());//+1 from RunContexts.copyCurrent
    assertSame(currentMonitor, RunMonitor.CURRENT.get());
  }

  @Test
  public void testNoCurrentAndNoExplicitMonitor() throws Exception {
    RunMonitor.CURRENT.remove();

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        setupLatch.countDownAndBlock();

        assertNotNull(RunMonitor.CURRENT.get());
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    assertTrue(setupLatch.await());
    assertNull(RunMonitor.CURRENT.get());
    setupLatch.unblock();

    future.awaitDoneAndGet();
    assertNull(RunMonitor.CURRENT.get());
  }

  private static class RunMonitorEx extends RunMonitor {

    public int getCancellablesCount() {
      return getCancellables().size();
    }

    public boolean containsCancellable(ICancellable cancellable) {
      return getCancellables().contains(cancellable);
    }
  }

  private static void waitUntilCancellableCount(RunMonitorEx monitor, int expectedCount) {
    final long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);

    while (monitor.getCancellablesCount() != expectedCount) {
      if (System.currentTimeMillis() > deadline) {
        fail(String.format("Timeout elapsed while waiting for a 'cancellable' count. [expectedCancellableCount=%s, actualCancellableCount=%s]", expectedCount, monitor.getCancellablesCount()));
      }
      Thread.yield();
    }
  }
}
