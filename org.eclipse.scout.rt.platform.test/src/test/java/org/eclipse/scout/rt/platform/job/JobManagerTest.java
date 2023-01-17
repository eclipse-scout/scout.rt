/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobManagerTest {

  private IBean<IJobManager> m_jobManagerBean;

  @Before
  public void before() {
    // Use dedicated job manager because job manager is shutdown in tests.
    m_jobManagerBean = JobTestUtil.replaceCurrentJobManager(new JobManager() {
      // must be a subclass in order to replace JobManager
    });
  }

  @After
  public void after() {
    JobTestUtil.unregisterAndShutdownJobManager(m_jobManagerBean);
  }

  @Test
  public void testVisit() throws Exception {
    final BlockingCountDownLatch latch = new BlockingCountDownLatch(3);

    IFuture<Void> future1 = Jobs.getJobManager().schedule(() -> {
      latch.countDownAndBlock();
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    IFuture<Void> future2 = Jobs.getJobManager().schedule(() -> {
      latch.countDownAndBlock();
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    IFuture<Void> future3 = Jobs.getJobManager().schedule(() -> {
      latch.countDownAndBlock();
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    assertTrue(latch.await());

    // RUN THE TEST
    Set<IFuture<?>> futures = Jobs.getJobManager().getFutures(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future1, future2, future3)
        .toFilter());

    // VERIFY
    assertEquals(CollectionUtility.hashSet(future1, future2, future3), futures);
  }

  @Test
  public void testShutdown() throws Exception {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(3);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(3);

    Jobs.getJobManager().schedule(() -> {
      try {
        setupLatch.countDownAndBlock();
      }
      catch (InterruptedException e) {
        protocol.add("interrupted-1");
      }
      finally {
        verifyLatch.countDown();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    Jobs.getJobManager().schedule(() -> {
      try {
        setupLatch.countDownAndBlock();
      }
      catch (InterruptedException e) {
        protocol.add("interrupted-2");
      }
      finally {
        verifyLatch.countDown();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    Jobs.getJobManager().schedule(() -> {
      try {
        setupLatch.countDownAndBlock();
      }
      catch (InterruptedException e) {
        protocol.add("interrupted-3");
      }
      finally {
        verifyLatch.countDown();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExceptionHandling(null, false));

    assertTrue(setupLatch.await());

    // RUN THE TEST
    Jobs.getJobManager().shutdown();

    // VERIFY
    assertTrue(verifyLatch.await());

    assertEquals(CollectionUtility.hashSet("interrupted-1", "interrupted-2", "interrupted-3"), protocol);

    try {
      Jobs.schedule(mock(IRunnable.class), Jobs.newInput());
      fail("AssertionError expected");
    }
    catch (AssertionException e) {
      // NOOP
    }
  }
}
