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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.platform.job.FixedDelayScheduleBuilder;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SimpleScheduleBuilder;

@RunWith(PlatformTestRunner.class)
public class PermitAcquisitionOrderTest {

  /**
   * Tests that schedule aware jobs scheduled in a row respect the scheduling order when acquiring a permit.
   */
  @Test
  public void testOrderIfStartingInSequence() {
    int regressionCount = 100; // regression

    final List<String> protocol = Collections.synchronizedList(new ArrayList<>()); // synchronized because modified/read by different threads.
    final String jobIdentifier = UUID.randomUUID().toString();

    IExecutionSemaphore semaphore = Jobs.newExecutionSemaphore(1);

    // Schedule 100 jobs to start at the same time
    for (int i = 0; i < regressionCount; i++) {
      Jobs.schedule(() -> {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
      }, Jobs.newInput()
          .withName("job-{}", i)
          .withExecutionSemaphore(semaphore)
          .withExecutionHint(jobIdentifier));
    }

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(jobIdentifier)
        .toFilter(), 10, TimeUnit.SECONDS);

    List<String> expectedProtocol = new ArrayList<>();
    for (int i = 0; i < regressionCount; i++) {
      expectedProtocol.add("job-" + i);
    }
    assertEquals(expectedProtocol, protocol);
  }

  /**
   * Tests that schedule aware jobs scheduled in sequence but start at a specific point in time respect the scheduling
   * order when acquiring a permit.
   */
  @Test
  public void testOrderIfStartingAtSameTime() {
    int regressionCount = 100; // regression

    final List<String> protocol = Collections.synchronizedList(new ArrayList<>()); // synchronized because modified/read by different threads.
    final String jobIdentifier = UUID.randomUUID().toString();

    IExecutionSemaphore semaphore = Jobs.newExecutionSemaphore(1);

    final Date date = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10));

    // Schedule 100 jobs to start at the same time
    for (int i = 0; i < regressionCount; i++) {
      Jobs.schedule(() -> {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
      }, Jobs.newInput()
          .withName("job-{}", i)
          .withExecutionSemaphore(semaphore)
          .withExecutionTrigger(Jobs.newExecutionTrigger()
              .withStartAt(date))
          .withExecutionHint(jobIdentifier));
    }

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(jobIdentifier)
        .toFilter(), 60, TimeUnit.SECONDS);

    List<String> expectedProtocol = new ArrayList<>();
    for (int i = 0; i < regressionCount; i++) {
      expectedProtocol.add("job-" + i);
    }
    assertEquals(expectedProtocol, protocol);
  }

  /**
   * Tests permit acquisition of repetitive job at fixed delay.
   */
  @Test
  public void testOrderWithinFixedDelayRepetitiveJob() {
    int regressionCount = 100; // regression

    final List<Integer> protocol = Collections.synchronizedList(new ArrayList<>()); // synchronized because modified/read by different threads.
    final String jobIdentifier = UUID.randomUUID().toString();
    final IExecutionSemaphore semaphore = Jobs.newExecutionSemaphore(1);

    final AtomicInteger counter = new AtomicInteger();

    // Schedule repetitive semaphore aware job
    Jobs.schedule(() -> {
      final int a = counter.incrementAndGet();
      final int b = counter.incrementAndGet();

      protocol.add(a);

      Jobs.schedule(() -> {
        protocol.add(b);
      }, Jobs.newInput()
          .withName("inner")
          .withExecutionSemaphore(semaphore)
          .withExecutionHint(jobIdentifier));
    }, Jobs.newInput()
        .withName("outer")
        .withExecutionSemaphore(semaphore)
        .withExecutionHint(jobIdentifier)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(FixedDelayScheduleBuilder.repeatForTotalCount(regressionCount, 1, TimeUnit.MILLISECONDS))));

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(jobIdentifier)
        .toFilter(), Long.MAX_VALUE, TimeUnit.SECONDS);

    List<Integer> expectedProtocol = new ArrayList<>();
    for (int i = 1; i <= regressionCount * 2; i++) {
      expectedProtocol.add(i);
    }
    assertEquals(expectedProtocol, protocol);
  }

  /**
   * Tests that a repetitive semaphore aware job does not acquire the permit before the scheduled job of the last round.
   */
  @Test
  public void testOrderWithinFixedRateRepetitiveJob() {
    int regressionCount = 100; // regression

    final List<Integer> protocol = Collections.synchronizedList(new ArrayList<>()); // synchronized because modified/read by different threads.
    final String jobIdentifier = UUID.randomUUID().toString();
    final IExecutionSemaphore semaphore = Jobs.newExecutionSemaphore(1);

    final AtomicInteger counter = new AtomicInteger();

    // Schedule repetitive semaphore aware job
    Jobs.schedule(() -> {
      final int a = counter.incrementAndGet();
      final int b = counter.incrementAndGet();

      protocol.add(a);

      Jobs.schedule(() -> {
        protocol.add(b);
      }, Jobs.newInput()
          .withExecutionSemaphore(semaphore)
          .withExecutionHint(jobIdentifier));
    }, Jobs.newInput()
        .withExecutionSemaphore(semaphore)
        .withExecutionHint(jobIdentifier)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(1)
                .withRepeatCount(regressionCount))));

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(jobIdentifier)
        .toFilter(), 10, TimeUnit.SECONDS);

    // Assert, that permits are acquires in sequence.
    // However, it is likely that rounds were consolidated, so simply compare the ascending order.
    for (int i = 0; i < protocol.size(); i++) {
      int expectedOrder = i + 1;
      int actualOrder = protocol.get(i).intValue();
      if (expectedOrder != actualOrder) {
        fail("expected=" + expectedOrder + ", actual=" + actualOrder + ", actualProtocol=" + protocol);
      }
    }
  }
}
