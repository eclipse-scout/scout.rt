/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.job;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.job.filter.future.SessionFutureFilter;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class MultipleSessionTest {

  private static final String JOB_IDENTIFIER = UUID.randomUUID().toString();

  private IClientSession m_clientSession1;
  private IClientSession m_clientSession2;

  @Before
  public void before() {
    m_clientSession1 = mock(IClientSession.class);
    when(m_clientSession1.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));
    m_clientSession2 = mock(IClientSession.class);
    when(m_clientSession2.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));
  }

  @Test
  public void testMutalExclusion() throws InterruptedException {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch latch1 = new BlockingCountDownLatch(2);
    final BlockingCountDownLatch latch2 = new BlockingCountDownLatch(2);

    ModelJobs.schedule(() -> {
      protocol.add("job1-S1");
      latch1.countDownAndBlock();
    }, ModelJobs.newInput(ClientRunContexts.empty().withSession(m_clientSession1, true))
        .withName("job-1-S1")
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    ModelJobs.schedule(() -> {
      protocol.add("job2-S1");
      latch2.countDownAndBlock();
    }, ModelJobs.newInput(ClientRunContexts.empty().withSession(m_clientSession1, true))
        .withName("job-2-S1")
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    ModelJobs.schedule(() -> {
      protocol.add("job1-S2");
      latch1.countDownAndBlock();
    }, ModelJobs.newInput(ClientRunContexts.empty().withSession(m_clientSession2, true))
        .withName("job-1-S2")
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    ModelJobs.schedule(() -> {
      protocol.add("job2-S2");
      latch2.countDownAndBlock();
    }, ModelJobs.newInput(ClientRunContexts.empty().withSession(m_clientSession2, true))
        .withName("job-2-S2")
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    assertTrue(latch1.await());
    assertEquals(CollectionUtility.hashSet("job1-S1", "job1-S2"), protocol);
    latch1.unblock();

    assertTrue(latch2.await());
    assertEquals(CollectionUtility.hashSet("job1-S1", "job1-S2", "job2-S1", "job2-S2"), protocol);
    latch2.unblock();

    // Wait until all jobs completed
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_IDENTIFIER)
        .toFilter(), 10, TimeUnit.SECONDS);
  }

  @Test
  public void testCancel() throws InterruptedException {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch setupLatch1 = new BlockingCountDownLatch(2);
    final BlockingCountDownLatch setupLatch2 = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch interruptedJob1_S1_Latch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch awaitAllCancelledLatch = new BlockingCountDownLatch(1);

    // Session 1 (job1)
    ModelJobs.schedule(() -> {
      protocol.add("job1-S1");
      try {
        setupLatch1.countDownAndBlock();
      }
      catch (InterruptedException e) {
        protocol.add("job1-S1-interrupted");
      }
      finally {
        interruptedJob1_S1_Latch.countDown();
      }

      Thread.interrupted(); // ensure the thread's interrupted status to be cleared in order to continue the test.
      awaitAllCancelledLatch.await();
    }, ModelJobs.newInput(ClientRunContexts.empty().withSession(m_clientSession1, true))
        .withName("job-1-S1")
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    // Session 1 (job2) --> never starts running because cancelled while job1 is mutex-owner
    ModelJobs.schedule(() -> {
      protocol.add("job2-S1");
      try {
        setupLatch2.countDownAndBlock();
      }
      catch (InterruptedException e) {
        protocol.add("job2-S1-interrupted");
      }
    }, ModelJobs.newInput(ClientRunContexts.empty().withSession(m_clientSession1, true))
        .withName("job-2-S1")
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    // Session 2 (job1)
    ModelJobs.schedule(() -> {
      protocol.add("job1-S2");
      try {
        setupLatch1.countDownAndBlock();
      }
      catch (InterruptedException e) {
        protocol.add("job1-S2-interrupted");
      }
    }, ModelJobs.newInput(ClientRunContexts.empty().withSession(m_clientSession2, true))
        .withName("job-1-S2")
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    // Session 2 (job2)
    ModelJobs.schedule(() -> {
      protocol.add("job2-S2");
      try {
        setupLatch2.countDownAndBlock();
      }
      catch (InterruptedException e) {
        protocol.add("job2-S2-interrupted");
      }
    }, ModelJobs.newInput(ClientRunContexts.empty().withSession(m_clientSession2, true))
        .withName("job-2-S2")
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    assertTrue(setupLatch1.await());
    assertEquals(CollectionUtility.hashSet("job1-S1", "job1-S2"), protocol);

    Jobs.getJobManager().cancel(ModelJobs.newFutureFilterBuilder()
        .andMatch(new SessionFutureFilter(m_clientSession1))
        .toFilter(), true); // cancels job-1-S1 and job-2-S1, meaning that job-2-S1 never starts running.
    awaitAllCancelledLatch.unblock();

    assertTrue(interruptedJob1_S1_Latch.await());

    setupLatch1.unblock();

    assertTrue(setupLatch2.await());
    assertEquals(CollectionUtility.hashSet("job1-S1", "job1-S1-interrupted", "job1-S2", "job2-S2"), protocol);
    setupLatch2.unblock();

    // Wait until all jobs completed
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_IDENTIFIER)
        .toFilter(), 10, TimeUnit.SECONDS);
  }
}
