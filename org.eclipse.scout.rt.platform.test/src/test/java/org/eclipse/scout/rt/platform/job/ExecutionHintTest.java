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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ExecutionHintTest {

  @Test
  public void testWaitingForTaggedJobs() throws InterruptedException {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(4);
    final BlockingCountDownLatch finishLatch = new BlockingCountDownLatch(3);

    // job-1
    Jobs.schedule(() -> {
      try {
        setupLatch.countDownAndBlock();
      }
      catch (InterruptedException e) {
        protocol.add("job-1-interrupted");
        finishLatch.countDown();
      }
    }, Jobs.newInput()
        .withName("job-1")
        .withExecutionHint("UI-JOB"));

    // job-2
    Jobs.schedule(() -> {
      try {
        setupLatch.countDownAndBlock();
      }
      catch (InterruptedException e) {
        protocol.add("job-2-interrupted");
        finishLatch.countDown();
      }
    }, Jobs.newInput()
        .withName("job-2")
        .withExecutionHint("UI-JOB"));

    // job-3
    Jobs.schedule(() -> {
      try {
        setupLatch.countDownAndBlock();
      }
      catch (InterruptedException e) {
        protocol.add("job-3-interrupted");
      }
    }, Jobs.newInput()
        .withName("job-3")
        .withExecutionHint("COMPUTATION-JOB"));

    // job-4
    Jobs.schedule(() -> {
      try {
        IFuture.CURRENT.get().addExecutionHint("UI-JOB");
        setupLatch.countDownAndBlock();
      }
      catch (InterruptedException e) {
        protocol.add("job-4-interrupted");
        finishLatch.countDown();
      }
    }, Jobs.newInput()
        .withName("job-4")
        .withExecutionHint("COMPUTATION-JOB"));

    assertTrue(setupLatch.await());

    // cancel all jobs tagged as 'UI-JOB'. That should be job1, job2, and job3
    Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint("UI-JOB")
        .toFilter(), true);

    assertTrue(finishLatch.await());

    Set<String> expected = new HashSet<>();
    expected.add("job-1-interrupted");
    expected.add("job-2-interrupted");
    expected.add("job-4-interrupted");
    assertEquals(expected, protocol);

    setupLatch.unblock();
  }

  @Test
  public void testHintChangeWhileRunning() {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);
    final String hint = "USER-INTERACTION-REQUIRED";

    IFuture<Void> future = Jobs.schedule(() -> {
      protocol.add("a");

      IFuture.CURRENT.get().addExecutionHint(hint);

      latch.countDownAndBlock();

      protocol.add("c");
    }, Jobs.newInput());

    // Wait until the job completes, or until tagged as 'USER-INTERACTION-REQUIRED'
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .andMatchNotExecutionHint(hint)
        .toFilter(), 10, TimeUnit.SECONDS);

    protocol.add("b");

    latch.unblock();

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .andMatchExecutionHint(hint)
        .toFilter(), 10, TimeUnit.SECONDS);

    assertEquals(Arrays.asList("a", "b", "c"), protocol);
  }
}
