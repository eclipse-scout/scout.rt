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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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
    RunMonitor.CURRENT.remove();
  }

  @Test
  public void testCurrentAndExplicitMonitor() throws Exception {
    final RunMonitor currentMonitor = new RunMonitor();
    final RunMonitor explicitMonitor = new RunMonitor();

    RunMonitor.CURRENT.set(currentMonitor);
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertSame(explicitMonitor, RunMonitor.CURRENT.get());
        assertFalse(explicitMonitor.isEmpty());
        assertTrue(currentMonitor.isEmpty());
      }
    }, Jobs.newInput(RunContexts.copyCurrent().runMonitor(explicitMonitor)));

    assertSame(currentMonitor, RunMonitor.CURRENT.get());

    awaitFutureDone(future); // do not wait on Future via 'awaitDoneAndGet' because cleanup is done shortly after 'done' (JobManager implementation detail).
    assertTrue(currentMonitor.isEmpty());
    assertTrue(explicitMonitor.isEmpty());
  }

  @Test
  public void testNoCurrentAndExplicitMonitor() throws Exception {
    final RunMonitor explicitMonitor = new RunMonitor();

    RunMonitor.CURRENT.remove();
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertSame(explicitMonitor, RunMonitor.CURRENT.get());
        assertFalse(explicitMonitor.isEmpty());
      }
    }, Jobs.newInput(RunContexts.copyCurrent().runMonitor(explicitMonitor)));

    assertNull(RunMonitor.CURRENT.get());

    awaitFutureDone(future); // do not wait on Future via 'awaitDoneAndGet' because cleanup is done shortly after 'done' (JobManager implementation detail).
    assertTrue(explicitMonitor.isEmpty());
  }

  @Times(200)
  @Test
  public void testCurrentAndNoExplicitMonitor() throws Exception {
    final RunMonitor currentMonitor = new RunMonitor();

    RunMonitor.CURRENT.set(currentMonitor);
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertSame(currentMonitor, RunMonitor.CURRENT.get());
        assertFalse(currentMonitor.isEmpty());
      }
    }, Jobs.newInput(RunContexts.copyCurrent()));

    assertSame(currentMonitor, RunMonitor.CURRENT.get());

    awaitFutureDone(future); // do not wait on Future via 'awaitDoneAndGet' because cleanup is done shortly after 'done' (JobManager implementation detail).
    assertTrue(currentMonitor.isEmpty());
  }

  @Test
  public void testNoCurrentAndNoExplicitMonitor() throws Exception {
    RunMonitor.CURRENT.remove();
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertNotNull(RunMonitor.CURRENT.get());
      }
    }, Jobs.newInput(RunContexts.copyCurrent()));

    assertNull(RunMonitor.CURRENT.get());
  }

  private void awaitFutureDone(IFuture<Void> future) throws InterruptedException {
    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);
    future.whenDone(new IDoneCallback<Void>() {

      @Override
      public void onDone(DoneEvent<Void> event) {
        latch.countDown();
      }
    });
    latch.await();
  }
}
