package org.eclipse.scout.rt.platform.job.internal;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
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
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        counter.incrementAndGet();
      }
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
