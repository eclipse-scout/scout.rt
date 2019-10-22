package org.eclipse.scout.rt.platform.job;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.job.internal.JobFutureTask;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.ThreadInfo;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.junit.Test;

public class JobFutureTaskTest {
  private static final int ROUNDS = 100000;

  /**
   * Test that cancelling a Job will not interrupt a wrong Thread. The test only runs for a short time, so the
   * reliability is low.
   * <p>
   * The parameter {@link #ROUNDS} is set to a reasonable low value in order to have a fast test run.
   * <p>
   * However, to have a more reliable test result, set {@link #ROUNDS} to 100000000.
   */
  @Test
  public void testRaceConditionOnCancel() throws InterruptedException, ExecutionException, TimeoutException {
    ExecutorService service = Executors.newFixedThreadPool(50);
    Semaphore scheduleLock = new Semaphore(100, true);
    AtomicInteger failureCount = new AtomicInteger();
    AtomicBoolean active = new AtomicBoolean(true);
    Random rnd = new Random(1234L);

    //progress
    AtomicInteger runCount = new AtomicInteger();
    service.submit(() -> {
      long t0 = System.currentTimeMillis();
      while (active.get()) {
        SleepUtil.sleepSafe(1, TimeUnit.SECONDS);
        long c = runCount.get();
        long t1 = System.currentTimeMillis();
        System.out.println("Run: " + c + " [" + (1000L * c / (t1 - t0)) + " run/sec]");
      }
    });
    try {
      for (int i = 0; i < ROUNDS; i++) {
        scheduleLock.acquire(1);
        RunMonitor r = new RunMonitor();
        JobFutureTask<String> f = new JobFutureTask<String>((JobManager) Jobs.getJobManager(), r, Jobs.newInput(), new CallableChain<String>(), () -> "FOO");
        service.submit(() -> {
          Thread.interrupted();//clear state
          ThreadInfo.CURRENT.set(new ThreadInfo(Thread.currentThread(), "foo-thread", 123L));
          RunMonitor.CURRENT.set(r);
          IFuture.CURRENT.set(f);
          f.run();
        });
        service.submit(() -> {
          Thread.interrupted();//clear state
          try {
            Thread.sleep(0L, rnd.nextInt(100000));
          }
          catch (InterruptedException e) {
            failureCount.incrementAndGet();
            System.out.println("FAILURE: 'f.cancel(true)' cancelled the wrong thread (sleep)");
          }
          try {
            f.cancel(true);
            String s = f.awaitDoneAndGet();
            if (s == null) {
              failureCount.incrementAndGet();
              System.out.println("FAILURE: value is null");
            }
          }
          catch (FutureCancelledError e) {
            //nop
          }
          catch (Throwable t) {
            t.printStackTrace();
          }
          runCount.incrementAndGet();
          scheduleLock.release(1);
        });
      }
    }
    finally {
      active.set(false);
      service.shutdown();
      service.awaitTermination(1, TimeUnit.MINUTES);
    }
    Assertions.assertEqual(0, failureCount.get());
  }
}
