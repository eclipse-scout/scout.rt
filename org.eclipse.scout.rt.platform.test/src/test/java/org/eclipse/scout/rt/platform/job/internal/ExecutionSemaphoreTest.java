package org.eclipse.scout.rt.platform.job.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.internal.ExecutionSemaphore.AcquisitionTask;
import org.eclipse.scout.rt.platform.job.internal.ExecutionSemaphore.QueuePosition;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutException;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.Times;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ExecutionSemaphoreTest {

  @Test
  public void testZeroPermits() {
    IExecutionSemaphore semaphore = Jobs.newExecutionSemaphore(0);

    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-running");
      }
    }, Jobs.newInput()
        .withName("job")
        .withExecutionSemaphore(semaphore));

    SleepUtil.sleepSafe(1, TimeUnit.SECONDS);
    assertTrue(protocol.isEmpty());
    assertFalse(future.isDone());

    // Change permits to 1 --> job should start running
    semaphore.withPermits(1);
    future.awaitDone(10, TimeUnit.SECONDS);
    assertEquals(CollectionUtility.hashSet(
        "job-running"), protocol);
  }

  /**
   * Tests execution semaphore with 3 permits.
   */
  @Test
  @Times(500) // regression
  public void testThreePermits() throws InterruptedException {
    IExecutionSemaphore semaphore = Jobs.newExecutionSemaphore(3);

    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch latchGroup1 = new BlockingCountDownLatch(3);
    final BlockingCountDownLatch latchGroup2 = new BlockingCountDownLatch(3);
    final BlockingCountDownLatch latchGroup3 = new BlockingCountDownLatch(2);

    // job-1
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-1-running");
        latchGroup1.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-1")
        .withExecutionSemaphore(semaphore));

    // job-2
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-2-running");
        latchGroup1.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-2")
        .withExecutionSemaphore(semaphore));

    // job-3
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-3-running");
        latchGroup1.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-3")
        .withExecutionSemaphore(semaphore));

    // job-4
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-4-running");
        latchGroup2.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-4")
        .withExecutionSemaphore(semaphore));

    // job-5
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-5-running");
        latchGroup2.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-5")
        .withExecutionSemaphore(semaphore));

    // job-6
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-6-running");
        latchGroup2.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-6")
        .withExecutionSemaphore(semaphore));

    // job-7
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-7-running");
        latchGroup3.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-7")
        .withExecutionSemaphore(semaphore));

    // job-8
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-8-running");
        latchGroup3.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-8")
        .withExecutionSemaphore(semaphore));

    // verify group-1
    assertTrue(latchGroup1.await());
    assertEquals(CollectionUtility.hashSet(
        "job-1-running",
        "job-2-running",
        "job-3-running"), protocol);

    // verify group-2
    protocol.clear();
    latchGroup1.unblock();
    assertTrue(latchGroup2.await());
    assertEquals(CollectionUtility.hashSet(
        "job-4-running",
        "job-5-running",
        "job-6-running"), protocol);

    // verify group-3
    protocol.clear();
    latchGroup2.unblock();
    assertTrue(latchGroup3.await());
    assertEquals(CollectionUtility.hashSet(
        "job-7-running",
        "job-8-running"), protocol);

    // job-9
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-9-running");
      }
    }, Jobs.newInput()
        .withName("job-9")
        .withExecutionSemaphore(semaphore))
        .awaitDone();

    assertEquals(CollectionUtility.hashSet(
        "job-7-running",
        "job-8-running",
        "job-9-running"), protocol);

    // cleanup
    latchGroup3.unblock();
  }

  /**
   * Tests execution semaphore with 3 permits and with a blocking condition involved.
   * <p>
   * In total, 7 jobs are scheduled. Thereby, job-1 and job-3 never finish, and job-2 enters a blocking condition.
   * <p>
   * This test tests, that because job-2 enters a blocking condition, job-4 starts running. Once job-4 completed, job-5
   * starts running. Then, job-5 unblocks the conditions, with lets job-2 to continue after job-5 finished. After job-2
   * finished, job-6, and then job-7 start running.
   * <p>
   */
  @Test
  @Times(500) // regression
  public void testThreePermitsAndBlocking() throws InterruptedException {
    final IExecutionSemaphore semaphore = Jobs.newExecutionSemaphore(3);

    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.

    final IBlockingCondition condition = Jobs.newBlockingCondition(true);

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(3);
    final BlockingCountDownLatch finishLatch = new BlockingCountDownLatch(2);
    final BlockingCountDownLatch latchJob2 = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch latchJob5 = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch latchJob6 = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch latchJob7 = new BlockingCountDownLatch(1);

    // job-1
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-1-running");
        setupLatch.countDownAndBlock();
        finishLatch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-1")
        .withExecutionSemaphore(semaphore));

    // job-2
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-2-running (a)");
        condition.waitFor(30, TimeUnit.SECONDS);
        protocol.add("job-2-running (b)");
        latchJob2.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-2")
        .withExecutionSemaphore(semaphore));

    // job-3
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-3-running");
        setupLatch.countDownAndBlock();
        finishLatch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-3")
        .withExecutionSemaphore(semaphore));

    // job-4
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-4-running");
        setupLatch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-4")
        .withExecutionSemaphore(semaphore));

    // job-5
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-5-running");
        condition.setBlocking(false);

        // Wait until job-2 is competing for a permit anew.
        // Otherwise, job-6 might get the permit before job-2.
        JobTestUtil.waitForPermitCompetitors(semaphore, 6); // permit-owners: job-1, job-3, job-5, queue: job-2 (RE-ACQUIRE), job-6, job-7

        latchJob5.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-5")
        .withExecutionSemaphore(semaphore));

    // job-6
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-6-running");
        latchJob6.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-6")
        .withExecutionSemaphore(semaphore));

    // job-7
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-7-running");
        latchJob7.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-7")
        .withExecutionSemaphore(semaphore));

    // verify
    assertTrue(setupLatch.await());
    assertEquals(CollectionUtility.hashSet(
        "job-1-running",
        "job-2-running (a)",
        "job-3-running",
        "job-4-running"), protocol);

    protocol.clear();
    setupLatch.unblock();
    assertTrue(latchJob5.await());
    assertEquals(CollectionUtility.hashSet(
        "job-5-running"), protocol);

    protocol.clear();
    latchJob5.unblock();
    try {
      assertTrue(latchJob2.await());
    }
    catch (AssertionError e) {
      System.out.println(protocol);
      throw e;
    }
    assertEquals(CollectionUtility.hashSet(
        "job-2-running (b)"), protocol);

    protocol.clear();
    latchJob2.unblock();
    assertTrue(latchJob6.await());
    assertEquals(CollectionUtility.hashSet(
        "job-6-running"), protocol);

    protocol.clear();
    latchJob6.unblock();
    assertTrue(latchJob7.await());
    assertEquals(CollectionUtility.hashSet(
        "job-7-running"), protocol);

    latchJob7.unblock();
    finishLatch.unblock();
  }

  @Test(expected = AssertionException.class)
  public void testSealSemaphore() {
    IExecutionSemaphore semaphore = Jobs.newExecutionSemaphore(1).seal();
    semaphore.withPermits(2);
  }

  /**
   * Tests no permit available upon completion
   */
  @Test
  public void testChangePermits1() {
    final IExecutionSemaphore semaphore = Jobs.newExecutionSemaphore(1);

    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.

    final String executionHint = UUID.randomUUID().toString();

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-1-running");

        Jobs.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            protocol.add("job-2-running");
          }
        }, Jobs.newInput()
            .withName("job-2")
            .withExecutionHint(executionHint)
            .withExceptionHandling(null, false)
            .withExecutionSemaphore(semaphore));

        // Change the permits to 0
        semaphore.withPermits(0);
      }
    }, Jobs.newInput()
        .withName("job-1")
        .withExecutionHint(executionHint)
        .withExecutionSemaphore(semaphore));

    future.awaitDone(1, TimeUnit.SECONDS);
    assertEquals(CollectionUtility.hashSet("job-1-running"), protocol);

    IFilter<IFuture<?>> job2Filter = Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(executionHint)
        .toFilter();

    try {
      Jobs.getJobManager().awaitDone(job2Filter, 1, TimeUnit.SECONDS);
      fail("timeout expected because no permit available");
    }
    catch (TimedOutException e) {
      // NOOP
    }

    // Change permits to 1 --> job-2 should run
    protocol.clear();
    semaphore.withPermits(1);
    Jobs.getJobManager().awaitDone(job2Filter, 1, TimeUnit.SECONDS);
    assertEquals(CollectionUtility.hashSet("job-2-running"), protocol);
  }

  /**
   * Tests no permit available upon schedule
   */
  @Test
  public void testChangePermits2() throws InterruptedException {
    IExecutionSemaphore semaphore = Jobs.newExecutionSemaphore(10);

    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.
    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);

    // job-1
    IFuture<Void> future1 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-1-running");
        latch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-1")
        .withExceptionHandling(null, true)
        .withExecutionSemaphore(semaphore));

    assertTrue(latch.await());
    assertEquals(CollectionUtility.hashSet(
        "job-1-running"), protocol);
    protocol.clear();

    // Change permits to zero.
    semaphore.withPermits(0);
    latch.unblock();
    future1.awaitDone();

    // job-2
    IFuture<Void> future2 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-2-running");
      }
    }, Jobs.newInput()
        .withName("job-2")
        .withExecutionSemaphore(semaphore));

    SleepUtil.sleepSafe(1, TimeUnit.SECONDS);
    assertTrue(protocol.isEmpty());
    assertFalse(future2.isDone());

    // Change permits to 1 --> job should run
    semaphore.withPermits(1);
    future2.awaitDone(10, TimeUnit.SECONDS);
    assertEquals(CollectionUtility.hashSet(
        "job-2-running"), protocol);
  }

  /**
   * Tests multiple permits available after permit change.
   */
  @Test
  public void testChangePermits3() throws InterruptedException {
    IExecutionSemaphore semaphore = Jobs.newExecutionSemaphore(0);

    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.
    final BlockingCountDownLatch finishLatch = new BlockingCountDownLatch(3);

    // job-1
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-1-running");
        finishLatch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-1")
        .withExceptionHandling(null, true)
        .withExecutionSemaphore(semaphore));

    // job-2
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-2-running");
        finishLatch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-2")
        .withExceptionHandling(null, true)
        .withExecutionSemaphore(semaphore));

    // job-3
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-3-running");
        finishLatch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-3")
        .withExceptionHandling(null, true)
        .withExecutionSemaphore(semaphore));

    JobTestUtil.waitForPermitCompetitors(semaphore, 3); // job-1, job-2, job-3
    semaphore.withPermits(3);

    assertTrue(finishLatch.await());
    assertEquals(CollectionUtility.hashSet(
        "job-1-running",
        "job-2-running",
        "job-3-running"), protocol);
    finishLatch.unblock();
  }

  /**
   * Test with change of permits to serial execution.
   */
  @Test
  @Times(500) // regression
  public void testChangePermits4() throws InterruptedException {
    IExecutionSemaphore semaphore = Jobs.newExecutionSemaphore(3);

    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(3);

    // job-1
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-1-running");
        setupLatch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-1")
        .withExecutionSemaphore(semaphore));

    // job-2
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-2-running");
        setupLatch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-2")
        .withExecutionSemaphore(semaphore));

    // job-3
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-3-running");
        setupLatch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-3")
        .withExecutionSemaphore(semaphore));

    // job-4
    final BlockingCountDownLatch latchJob4 = new BlockingCountDownLatch(1);
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-4-running");
        latchJob4.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-4")
        .withExecutionSemaphore(semaphore));

    // job-5
    final BlockingCountDownLatch latchJob5 = new BlockingCountDownLatch(1);
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-5-running");
        latchJob5.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-5")
        .withExecutionSemaphore(semaphore));

    // job-6
    final BlockingCountDownLatch latchJob6 = new BlockingCountDownLatch(1);
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("job-6-running");
        latchJob6.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withName("job-6")
        .withExecutionSemaphore(semaphore));

    assertTrue(setupLatch.await());
    assertEquals(CollectionUtility.hashSet(
        "job-1-running",
        "job-2-running",
        "job-3-running"), protocol);
    protocol.clear();
    semaphore.withPermits(1); // serial execution
    setupLatch.unblock();

    latchJob4.await();
    assertEquals(CollectionUtility.hashSet("job-4-running"), protocol);
    protocol.clear();
    latchJob4.unblock();

    latchJob5.await();
    assertEquals(CollectionUtility.hashSet("job-5-running"), protocol);
    protocol.clear();
    latchJob5.unblock();

    latchJob6.await();
    assertEquals(CollectionUtility.hashSet("job-6-running"), protocol);
    protocol.clear();
    latchJob6.unblock();
  }

  /**
   * Tests an internal of {@link ExecutionSemaphore}, that {@link AcquisitionTask#notifyPermitAcquired()} is invoked
   * outside the {@link ExecutionSemaphore} lock.
   * <p>
   * Otherwise, a deadlock might occur, once the resuming job-1 tries to re-acquire the permit, namely exactly the time
   * when owning acquisitionLock in {@link ExecutionSemaphore#acquire(IFuture, QueuePosition)} and querying
   * 'isPermitOwner'. Thereto, job-1 must compete for the semaphore lock, while job-2 (owning semaphore lock) tries to
   * notify the resuming job-1 via {@link AcquisitionTask#notifyPermitAcquired()}, but cannot get monitor of
   * acquisitionLock.
   */
  @Test
  @Times(1_000) // regression; do not remove
  public void testInternalDeadlock() {
    final IExecutionSemaphore semaphore = Jobs.newExecutionSemaphore(1);

    final IBlockingCondition condition = Jobs.newBlockingCondition(true);

    final AtomicReference<IFuture<?>> future2Ref = new AtomicReference<>();
    IFuture<Void> future1 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        future2Ref.set(Jobs.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            condition.setBlocking(false);
          }
        }, Jobs.newInput()
            .withName("job-2")
            .withExecutionSemaphore(semaphore)));

        condition.waitFor();
      }
    }, Jobs.newInput()
        .withName("job-1")
        .withExecutionSemaphore(semaphore));

    try {
      future1.awaitDoneAndGet(5, TimeUnit.SECONDS);
    }
    catch (TimedOutException e) {
      fail(String.format("Deadlock while passing permit from 'job-2' to 'job-1' [job-1-state=%s, job-2-state=%s", future1.getState(), future2Ref.get().getState()));
    }
  }
}
