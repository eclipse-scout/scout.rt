package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.TimeoutException;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobManagerLoadTest {

  private static final String JOB_IDENTIFIER = UUID.randomUUID().toString();

  private static final long JOB_COUNT = 50_000;

  @Test(timeout = 20_000)
  public void testImmediateExecuting() {
    IFilter<IFuture<?>> filter = Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_IDENTIFIER)
        .toFilter();

    final AtomicLong counter = new AtomicLong();
    for (int i = 0; i < JOB_COUNT; i++) {
      Jobs.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          counter.incrementAndGet();
        }
      }, Jobs.newInput()
          .withExecutionHint(JOB_IDENTIFIER));
    }

    try {
      Jobs.getJobManager().awaitDone(filter, 10, TimeUnit.SECONDS);
      assertEquals(JOB_COUNT, counter.get());
    }
    catch (TimeoutException e) {
      Jobs.getJobManager().cancel(filter, true);
      fail("Scheduling 50'000 jobs took longer than 10s");
    }
  }

  @Test(timeout = 20_000)
  public void testDelayedExecuting() {
    IFilter<IFuture<?>> filter = Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_IDENTIFIER)
        .toFilter();

    Date startDate = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3));

    final AtomicLong counter = new AtomicLong();
    for (int i = 0; i < JOB_COUNT; i++) {
      Jobs.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          counter.incrementAndGet();
        }
      }, Jobs.newInput()
          .withExecutionHint(JOB_IDENTIFIER)
          .withExecutionTrigger(Jobs.newExecutionTrigger()
              .withStartAt(startDate)));
    }

    try {
      Jobs.getJobManager().awaitDone(filter, 10, TimeUnit.SECONDS);
      assertEquals(JOB_COUNT, counter.get());
    }
    catch (TimeoutException e) {
      Jobs.getJobManager().cancel(filter, true);
      fail("Scheduling 50'000 took longer than 10s");
    }
  }
}
