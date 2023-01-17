/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.internal;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.Times;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class DevelopmentThreadNameDecoratorTest {

  private IBean<? extends ThreadNameDecorator> m_developmentThreadNameDecorator;

  @Before
  public void before() {
    // simulate development mode
    m_developmentThreadNameDecorator = BEANS.getBeanManager().registerClass(DevelopmentThreadNameDecorator.class);
  }

  @After
  public void after() {
    Platform.get().getBeanManager().unregisterBean(m_developmentThreadNameDecorator);
  }

  @Test
  @Times(100) // regression; do no remove
  public void testThreadName() throws InterruptedException {
    final AtomicReference<Thread> workerThread = new AtomicReference<>();

    final IBlockingCondition condition = Jobs.newBlockingCondition(true);
    final BlockingCountDownLatch latch1 = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch latch2 = new BlockingCountDownLatch(1);

    IExecutionSemaphore semaphore = Jobs.newExecutionSemaphore(1);
    Jobs.schedule(() -> {
      workerThread.set(Thread.currentThread());
      latch1.countDownAndBlock();
      condition.waitFor(10, TimeUnit.SECONDS);
      latch2.countDownAndBlock();
    }, Jobs.newInput()
        .withExecutionSemaphore(semaphore)
        .withThreadName("test-thread")
        .withName("job-1"));

    // Test while running
    assertTrue(latch1.await());
    assertTrue("actual=" + workerThread.get().getName(), workerThread.get().getName().matches("test-thread-\\d+ job-1"));
    latch1.unblock();

    // Test while blocked
    JobTestUtil.waitForPermitCompetitors(semaphore, 0);
    assertTrue("actual=" + workerThread.get().getName(), workerThread.get().getName().matches("test-thread-\\d+ \\(WAITING_FOR_BLOCKING_CONDITION\\) job-1"));

    // Test while waiting for permit
    semaphore.withPermits(0);
    condition.setBlocking(false);
    JobTestUtil.waitForPermitCompetitors(semaphore, 1);
    assertTrue("actual=" + workerThread.get().getName(), workerThread.get().getName().matches("test-thread-\\d+ \\(WAITING_FOR_PERMIT\\) job-1"));

    // Test while running
    semaphore.withPermits(1);
    assertTrue(latch2.await());
    assertTrue("actual=" + workerThread.get().getName(), workerThread.get().getName().matches("test-thread-\\d+ job-1"));
    latch2.unblock();
  }
}
