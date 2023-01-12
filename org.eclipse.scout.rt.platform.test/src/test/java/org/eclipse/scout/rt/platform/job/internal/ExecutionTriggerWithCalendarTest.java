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

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.impl.calendar.CronCalendar;

@RunWith(PlatformTestRunner.class)
public class ExecutionTriggerWithCalendarTest {

  /**
   * This tests only accept every 2nd second.
   */
  @Test
  public void testExclusion() throws ParseException {
    final AtomicInteger counter = new AtomicInteger();
    IFuture<Void> future = Jobs.schedule(() -> {
      counter.incrementAndGet();
    }, Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withModifiedByCalendar(new CronCalendar("0/2 * * ? * *"))
            .withEndIn(6, TimeUnit.SECONDS)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(1)
                .repeatForever())));
    future.awaitDone(20, TimeUnit.SECONDS);
    assertEquals(3, counter.get());
  }
}
