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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ModelJobTest {

  private IClientSession m_clientSession1;
  private IClientSession m_clientSession2;

  @Before
  public void before() {
    m_clientSession1 = mock(IClientSession.class);
    when(m_clientSession1.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));

    m_clientSession2 = mock(IClientSession.class);
    when(m_clientSession2.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));
  }

  @Test(expected = AssertionException.class)
  public void testNoSession() {
    ClientRunContexts.empty().run(() -> ModelJobs.schedule(mock(IRunnable.class), ModelJobs.newInput(ClientRunContexts.copyCurrent())));
  }

  @Test
  public void testModelThread() {
    final AtomicBoolean modelThread = new AtomicBoolean();

    assertFalse(ModelJobs.isModelThread());

    ModelJobs.schedule(() -> modelThread.set(ModelJobs.isModelThread()), ModelJobs.newInput(ClientRunContexts.empty().withSession(m_clientSession1, true))).awaitDoneAndGet();

    assertFalse(ModelJobs.isModelThread());
    assertTrue(modelThread.get());
  }

  @Test
  public void testThreadName() {
    ClientRunContext clientRunContext = ClientRunContexts.empty().withSession(m_clientSession1, true);
    assertEquals("scout-model-thread", ModelJobs.newInput(clientRunContext).getThreadName());
  }

  /**
   * We have 2 model jobs scheduled in sequence. Due to the mutex, the second model job only commences execution once
   * the first model job completed. However, job 1 yields its permit, so that job-2 can commence execution.
   */
  @Test
  public void testYield() throws InterruptedException {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<>()); // synchronized because modified/read by different threads.
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch finishLatch = new BlockingCountDownLatch(1);

    final ClientRunContext runContext = ClientRunContexts.empty().withSession(m_clientSession1, true);

    // Schedule first model job
    ModelJobs.schedule(() -> {
      protocol.add("job-1-running");

      setupLatch.await();
      protocol.add("job-1-before-yield");
      ModelJobs.yield();
      protocol.add("job-1-after-yield");

      finishLatch.countDown();
    }, ModelJobs.newInput(runContext.copy())
        .withName("job-1"));

    // Schedule second model job
    ModelJobs.schedule(() -> {
      protocol.add("job-2-running");
    }, ModelJobs.newInput(runContext.copy())
        .withName("job-2"));

    setupLatch.countDown();
    finishLatch.await();

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("job-1-running");
    expectedProtocol.add("job-1-before-yield");
    expectedProtocol.add("job-2-running");
    expectedProtocol.add("job-1-after-yield");
  }
}
