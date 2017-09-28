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

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ThreadNameDecoratorTest {

  private IBean<ThreadNameDecorator> m_threadNameDecorator;
  private Pattern m_threadNamePattern;

  @Before
  public void before() {
    // simulate productive mode
    m_threadNameDecorator = BEANS.getBeanManager().registerClass(ThreadNameDecorator.class);
    if (Platform.get().inDevelopmentMode()) {
      m_threadNamePattern = Pattern.compile("test-thread-\\d+.*");
    }
    else {
      m_threadNamePattern = Pattern.compile("test-thread-\\d+");
    }
  }

  @After
  public void after() {
    Platform.get().getBeanManager().unregisterBean(m_threadNameDecorator);
  }

  @Test
  public void testThreadName() throws InterruptedException {
    final AtomicReference<Thread> workerThread = new AtomicReference<>();

    final IBlockingCondition condition = Jobs.newBlockingCondition(true);
    final BlockingCountDownLatch latch1 = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch latch2 = new BlockingCountDownLatch(1);

    IExecutionSemaphore semaphore = Jobs.newExecutionSemaphore(1);
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        workerThread.set(Thread.currentThread());
        latch1.countDownAndBlock();
        condition.waitFor(10, TimeUnit.SECONDS);
        latch2.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withExecutionSemaphore(semaphore)
        .withThreadName("test-thread")
        .withName("job-1"));

    // Test while running
    assertTrue(latch1.await());
    assertTrue("actual=" + workerThread.get().getName(), m_threadNamePattern.matcher(workerThread.get().getName()).matches());
    latch1.unblock();

    // Test while blocked
    JobTestUtil.waitForPermitCompetitors(semaphore, 0);
    assertTrue("actual=" + workerThread.get().getName(), m_threadNamePattern.matcher(workerThread.get().getName()).matches());

    // Test while waiting for permit
    semaphore.withPermits(0);
    condition.setBlocking(false);
    JobTestUtil.waitForPermitCompetitors(semaphore, 1);
    assertTrue("actual=" + workerThread.get().getName(), m_threadNamePattern.matcher(workerThread.get().getName()).matches());

    // Test while running
    semaphore.withPermits(1);
    assertTrue(latch2.await());
    assertTrue("actual=" + workerThread.get().getName(), m_threadNamePattern.matcher(workerThread.get().getName()).matches());
    latch2.unblock();
  }
}
