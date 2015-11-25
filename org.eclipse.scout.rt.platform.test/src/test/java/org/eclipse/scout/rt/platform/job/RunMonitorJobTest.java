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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.commons.ICancellable;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.Times;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class RunMonitorJobTest {

  @After
  public void after() {
    RunMonitorEx.CURRENT.remove();
  }

  @Test
  public void testCurrentAndExplicitMonitor() throws Exception {
    final RunMonitorEx currentMonitor = new RunMonitorEx();
    final RunMonitorEx explicitMonitor = new RunMonitorEx();

    RunMonitorEx.CURRENT.set(currentMonitor);
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertEquals(1, currentMonitor.getChildCount());//+1 from RunContexts.copyCurrent
        assertEquals(1, explicitMonitor.getChildCount());//+1 from job cancellable
        assertSame(explicitMonitor, RunMonitorEx.CURRENT.get());
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent().withRunMonitor(explicitMonitor)));

    assertSame(currentMonitor, RunMonitorEx.CURRENT.get());

    awaitFutureDone(future); // do not wait on Future via 'awaitDoneAndGet' because cleanup is done shortly after 'done' (JobManager implementation detail).
    assertEquals(1, currentMonitor.getChildCount());//+1 from RunContexts.copyCurrent
    assertEquals(0, explicitMonitor.getChildCount());
  }

  @Test
  public void testNoCurrentAndExplicitMonitor() throws Exception {
    final RunMonitorEx explicitMonitor = new RunMonitorEx();

    RunMonitor.CURRENT.remove();
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertEquals(1, explicitMonitor.getChildCount());//+1 from job cancellable
        assertSame(explicitMonitor, RunMonitorEx.CURRENT.get());
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent().withRunMonitor(explicitMonitor)));

    assertNull(RunMonitorEx.CURRENT.get());

    awaitFutureDone(future); // do not wait on Future via 'awaitDoneAndGet' because cleanup is done shortly after 'done' (JobManager implementation detail).
    assertEquals(0, explicitMonitor.getChildCount());
  }

  @Times(200)
  @Test
  public void testCurrentAndNoExplicitMonitor() throws Exception {
    final RunMonitorEx currentMonitor = new RunMonitorEx();

    RunMonitorEx.CURRENT.set(currentMonitor);
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertTrue(currentMonitor.containsCancellable(RunMonitorEx.CURRENT.get()));
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    assertSame(currentMonitor, RunMonitorEx.CURRENT.get());

    awaitFutureDone(future); // do not wait on Future via 'awaitDoneAndGet' because cleanup is done shortly after 'done' (JobManager implementation detail).
    assertEquals(1, currentMonitor.getChildCount());//+1 from RunContexts.copyCurrent
  }

  @Test
  public void testNoCurrentAndNoExplicitMonitor() throws Exception {
    RunMonitorEx.CURRENT.remove();
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertNotNull(RunMonitorEx.CURRENT.get());
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    assertNull(RunMonitorEx.CURRENT.get());
  }

  private void awaitFutureDone(IFuture<Void> future) throws InterruptedException {
    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);
    future.whenDone(new IDoneHandler<Void>() {

      @Override
      public void onDone(DoneEvent<Void> event) {
        latch.countDown();
      }
    }, null);
    latch.await();
  }

  private static class RunMonitorEx extends RunMonitor {
    public int getChildCount() {
      return getCancellables().size();
    }

    public boolean containsCancellable(ICancellable c) {
      return getCancellables().contains(c);
    }
  }
}
