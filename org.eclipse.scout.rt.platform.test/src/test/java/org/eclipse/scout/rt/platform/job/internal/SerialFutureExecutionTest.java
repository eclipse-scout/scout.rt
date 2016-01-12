package org.eclipse.scout.rt.platform.job.internal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.job.FixedDelayScheduleBuilder;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;

@RunWith(PlatformTestRunner.class)
public class SerialFutureExecutionTest {

  /**
   * Tests that a future is not run concurrently.
   * <p>
   * For that, we schedule a job periodically at a fixed rate (every millisecond), but sleep in the Runnable of the
   * first round, so it does not complete. Upon continuation, we expect one last consolidated round to be executed.
   * <p>
   * Misfire policy: {@link SimpleTrigger#MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT}
   */
  @Test
  public void testAtFixedRate_NowWithRemainingCount() throws InterruptedException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.
    final String jobIdentifier = UUID.randomUUID().toString();

    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);

    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("before");
        latch.countDownAndBlock();
        protocol.add("after");
      }
    }, Jobs.newInput()
        .withExecutionHint(jobIdentifier)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(1)
                .withMisfireHandlingInstructionNowWithRemainingCount()
                .withRepeatCount(100))));

    latch.await();
    Thread.sleep(6000); // Wait some time until trigger fired for all rounds

    // Verify no concurrent execution
    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("before"); // first round started
    assertEquals(expectedProtocol, protocol);
    latch.unblock();

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(jobIdentifier)
        .toFilter(), 10, TimeUnit.SECONDS);

    expectedProtocol.add("after"); // first round completed
    expectedProtocol.add("before"); // consolidated round
    expectedProtocol.add("after"); // consolidated round
    assertEquals(expectedProtocol, protocol);
  }

  /**
   * Tests that a future is not run concurrently.
   * <p>
   * For that, we schedule a job periodically at a fixed rate (every millisecond), but sleep in the Runnable of the
   * first round, so it does not complete. Upon continuation, we expect all pending rounds to be executed.
   * <p>
   * Misfire policy: {@link SimpleTrigger#MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT}
   */
  @Test
  public void testAtFixedRate_NowWithExistingCount() throws InterruptedException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.
    final String jobIdentifier = UUID.randomUUID().toString();

    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);

    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("before");
        latch.countDownAndBlock();
        protocol.add("after");
      }
    }, Jobs.newInput()
        .withExecutionHint(jobIdentifier)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(1)
                .withMisfireHandlingInstructionNowWithExistingCount()
                .withRepeatCount(99)))); // see JavaDoc of withRepeatCount: first + repeat count

    latch.await();
    Thread.sleep(5000); // Wait some time until trigger fired for all rounds

    // Verify no concurrent execution
    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("before"); // first round started
    assertEquals(expectedProtocol, protocol);
    latch.unblock();

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(jobIdentifier)
        .toFilter(), 10, TimeUnit.SECONDS);

    expectedProtocol.add("after"); // first round completed
    for (int i = 0; i < 99; i++) {
      expectedProtocol.add("before"); // other 99 rounds
      expectedProtocol.add("after"); // other 99 rounds
    }

    assertEquals(expectedProtocol, protocol);
  }

  /**
   * Tests that a future is not run concurrently.
   * <p>
   * For that, we schedule a job periodically with a fixed delay (every millisecond), but sleep in the Runnable of the
   * first round, so it does not complete. Upon continuation, we expect all other rounds to be made.
   */
  @Test
  public void testWithFixedDelay() throws InterruptedException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.
    final String jobIdentifier = UUID.randomUUID().toString();

    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);

    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("before");
        latch.countDownAndBlock();
        protocol.add("after");
      }
    }, Jobs.newInput()
        .withExecutionHint(jobIdentifier)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(FixedDelayScheduleBuilder.repeatForTotalCount(100, 1, TimeUnit.MILLISECONDS))));

    latch.await();
    Thread.sleep(5000); // Wait some time until trigger fired for all rounds

    // Verify no concurrent execution
    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("before"); // first round started
    assertEquals(expectedProtocol, protocol);
    latch.unblock();

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(jobIdentifier)
        .toFilter(), 10, TimeUnit.SECONDS);

    expectedProtocol.add("after"); // first round completed
    for (int i = 0; i < 99; i++) {
      expectedProtocol.add("before"); // other 99 rounds
      expectedProtocol.add("after"); // other 99 rounds
    }

    assertEquals(expectedProtocol, protocol);
  }
}
