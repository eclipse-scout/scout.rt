package org.eclipse.scout.rt.platform.job.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.TimeoutException;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SimpleScheduleBuilder;

@RunWith(PlatformTestRunner.class)
public class MisfireTest {

  /**
   * Long running test because default Quartz 'misfireThreshold' in RAMJobStore is set to 5s and cannot be changed.
   * <p>
   * Tests that a last round is scheduled upon a 'misfire' with the end-time arrived.
   */
  @Test
  public void testFinalRunAfterMisfire() {
    final int defaultMisfireThreshold = 5000; // RAMJobStore#misfireThreshold
    final int endsIn = 1000;
    final int schedulingInterval = 100;

    final AtomicInteger roundCounter = new AtomicInteger();

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (roundCounter.incrementAndGet() == 1) {
          SleepUtil.sleepSafe(defaultMisfireThreshold + schedulingInterval + 1, TimeUnit.MILLISECONDS);
        }
      }
    }, Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withEndIn(endsIn, TimeUnit.MILLISECONDS)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(schedulingInterval)
                .repeatForever())));

    // Wait until done
    try {
      future.awaitDone(10, TimeUnit.SECONDS);
    }
    catch (TimeoutException e) {
      future.cancel(true);
      fail("Job is hanging because no last round scheduled upon misfire with end-time arrived");
    }

    // Verify
    assertTrue(((JobFutureTask<?>) future).isFinalRun());
    assertFalse(((JobFutureTask<?>) future).hasNextExecution());
  }
}
