package org.eclipse.scout.rt.platform.job.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.impl.calendar.CronCalendar;

@RunWith(PlatformTestRunner.class)
public class ExecutionTriggerWithCalendarTest {

  private CronCalendar m_calendar;

  @Before
  public void before() throws ParseException {
    m_calendar = new CronCalendar("0/2 * * ? * *");
    Jobs.getJobManager().addCalendar("acceptEvery2ndSecond", m_calendar, true, true);
  }

  @After
  public void after() {
    Jobs.getJobManager().removeCalendar("acceptEvery2ndSecond");
  }

  @Test(expected = AssertionException.class)
  public void testUnknownCalendar() {
    Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withModifiedByCalendar("unknown")));
  }

  /**
   * This tests only accept every 2nd second.
   */
  @Test
  public void testExclusion() {
    final AtomicInteger counter = new AtomicInteger();
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        counter.incrementAndGet();
      }
    }, Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withModifiedByCalendar("acceptEvery2ndSecond")
            .withEndIn(6, TimeUnit.SECONDS)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(1)
                .repeatForever())));
    future.awaitDone(20, TimeUnit.SECONDS);
    assertEquals(3, counter.get());
  }
}
