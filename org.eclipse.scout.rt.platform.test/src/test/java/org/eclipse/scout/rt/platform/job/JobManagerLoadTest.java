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

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.testcategory.ResourceIntensiveTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(ResourceIntensiveTest.class) // uses up to 25k native threads
@RunWith(PlatformTestRunner.class)
public class JobManagerLoadTest {

  private static final String JOB_IDENTIFIER = UUID.randomUUID().toString();

  /*
   * Be careful when changing this constant.
   * Performance testing based on execution time is not very accurate because it expects a minimal base performance of
   * the system the test is executed on. Further, this particular test creates a lot of new threads, i.e. OS resources
   * that might be limited for the executing user or process.
   */
  private static final long JOB_COUNT = 25_000;

  @Test(timeout = 20_000)
  public void testImmediateExecuting() {
    Predicate<IFuture<?>> filter = Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_IDENTIFIER)
        .toFilter();

    final AtomicLong counter = new AtomicLong();
    for (int i = 0; i < JOB_COUNT; i++) {
      Jobs.schedule(() -> {
        counter.incrementAndGet();
      }, Jobs.newInput()
          .withExecutionHint(JOB_IDENTIFIER));
    }

    try {
      Jobs.getJobManager().awaitDone(filter, 10, TimeUnit.SECONDS);
      assertEquals(JOB_COUNT, counter.get());
    }
    catch (TimedOutError e) {
      Jobs.getJobManager().cancel(filter, true);
      fail("Scheduling 25'000 jobs took longer than 10s");
    }
  }

  @Test(timeout = 20_000)
  public void testDelayedExecuting() {
    Predicate<IFuture<?>> filter = Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_IDENTIFIER)
        .toFilter();

    Date startDate = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3));

    final AtomicLong counter = new AtomicLong();
    for (int i = 0; i < JOB_COUNT; i++) {
      Jobs.schedule(() -> {
        counter.incrementAndGet();
      }, Jobs.newInput()
          .withExecutionHint(JOB_IDENTIFIER)
          .withExecutionTrigger(Jobs.newExecutionTrigger()
              .withStartAt(startDate)));
    }

    try {
      Jobs.getJobManager().awaitDone(filter, 10, TimeUnit.SECONDS);
      assertEquals(JOB_COUNT, counter.get());
    }
    catch (TimedOutError e) {
      Jobs.getJobManager().cancel(filter, true);
      fail("Scheduling 25'000 took longer than 10s");
    }
  }
}
