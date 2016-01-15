package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.InterruptedRuntimeException;
import org.eclipse.scout.rt.platform.util.concurrent.TimeoutException;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil.ICondition;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class BlockingConditionInterruptionTest {

  private static final String JOB_IDENTIFIER = UUID.randomUUID().toString();

  @Test
  public void testInterruptibly_interruptBeforeEntering() {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    runTest(protocol, WaitMethod.WAIT_INTERRUPTIBLY, InterruptionAction.INTERRUPT_BEFORE_ENTERING);

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("beforeBlockingCondition");
    expectedProtocol.add("InterruptedException");
    expectedProtocol.add("threadInterrupted");
    assertEquals(expectedProtocol, protocol);
  }

  @Test
  public void testInterruptibly_interruptWhileBlocking() {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    runTest(protocol, WaitMethod.WAIT_INTERRUPTIBLY, InterruptionAction.INTERRUPT_WHILE_BLOCKING);

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("beforeBlockingCondition");
    expectedProtocol.add("InterruptedException");
    expectedProtocol.add("threadInterrupted");
    assertEquals(expectedProtocol, protocol);
  }

  @Test
  public void testUninterruptibly_interruptBeforeEntering() {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    runTest(protocol, WaitMethod.WAIT_FOR_UNINTERRUPTIBLY, InterruptionAction.INTERRUPT_BEFORE_ENTERING);

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("beforeBlockingCondition");
    expectedProtocol.add("afterBlockingCondition");
    expectedProtocol.add("threadInterrupted");
    assertEquals(expectedProtocol, protocol);
  }

  @Test
  public void testUninterruptibly_interruptWhileBlocking() {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    runTest(protocol, WaitMethod.WAIT_FOR_UNINTERRUPTIBLY, InterruptionAction.INTERRUPT_WHILE_BLOCKING);

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("beforeBlockingCondition");
    expectedProtocol.add("afterBlockingCondition");
    expectedProtocol.add("threadInterrupted");
    assertEquals(expectedProtocol, protocol);
  }

  //

  @Test
  public void testInterruptiblyWithTimeout_interruptBeforeEntering() {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    runTest(protocol, WaitMethod.WAIT_FOR_INTERRUPTIBLY_WITH_TIMEOUT, InterruptionAction.INTERRUPT_BEFORE_ENTERING);

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("beforeBlockingCondition");
    expectedProtocol.add("TimeoutException");
    expectedProtocol.add("threadInterrupted");
    assertEquals(expectedProtocol, protocol);
  }

  @Test
  public void testUninterruptiblyWithTimeout_interruptBeforeEntering() {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    runTest(protocol, WaitMethod.WAIT_FOR_UNINTERRUPTIBLY_WITH_TIMEOUT, InterruptionAction.INTERRUPT_BEFORE_ENTERING);

    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("beforeBlockingCondition");
    expectedProtocol.add("TimeoutException");
    expectedProtocol.add("threadInterrupted");
    assertEquals(expectedProtocol, protocol);
  }

  private static void runTest(final List<String> protocol, final WaitMethod waitForMethod, final InterruptionAction interruptionAction) {
    final String HINT_BLOCKED = "blocked";
    final AtomicReference<Thread> runnerThread = new AtomicReference<>();

    final IBlockingCondition bc = Jobs.newBlockingCondition(true);

    // Schedule job to enter blocking condition
    final IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        runnerThread.set(Thread.currentThread());

        if (InterruptionAction.INTERRUPT_BEFORE_ENTERING.equals(interruptionAction)) {
          Thread.currentThread().interrupt();
        }

        try {
          protocol.add("beforeBlockingCondition");

          switch (waitForMethod) {
            case WAIT_INTERRUPTIBLY:
              bc.waitFor(HINT_BLOCKED);
              break;
            case WAIT_FOR_INTERRUPTIBLY_WITH_TIMEOUT:
              bc.waitFor(0, TimeUnit.MILLISECONDS, HINT_BLOCKED);
              break;
            case WAIT_FOR_UNINTERRUPTIBLY:
              bc.waitForUninterruptibly(HINT_BLOCKED);
              break;
            case WAIT_FOR_UNINTERRUPTIBLY_WITH_TIMEOUT:
              bc.waitForUninterruptibly(0, TimeUnit.MILLISECONDS, HINT_BLOCKED);
              break;
            default:
              throw new UnsupportedOperationException();
          }

          protocol.add("afterBlockingCondition");
        }
        catch (InterruptedRuntimeException e) {
          protocol.add("InterruptedException");
        }
        catch (TimeoutException e) {
          protocol.add("TimeoutException");
        }

        if (Thread.currentThread().isInterrupted()) {
          protocol.add("threadInterrupted");
        }
      }
    }, Jobs.newInput()
        .withName("test job")
        .withExecutionHint(JOB_IDENTIFIER));

    // Wait until the job enters blocking condition, or is done.
    JobTestUtil.waitForCondition(new ICondition() {

      @Override
      public boolean isFulfilled() {
        return future.containsExecutionHint(HINT_BLOCKED) || future.isDone();
      }
    });

    if (InterruptionAction.INTERRUPT_WHILE_BLOCKING.equals(interruptionAction)) {
      runnerThread.get().interrupt();

      // Sleep some time so that the runner is interrupted.
      // That is because a thread's interruption is asynchronous, meaning that once a thread (a) interrupts another threads
      // (b), and in turn thread (a) unblocks the condition which thread (b) is waiting for, it is not ensured that thread
      // (b) exists with an {@link InterruptedException}. However, many tests expect exactly that behavior.
      SleepUtil.sleepSafe(100, TimeUnit.MILLISECONDS);
    }

    bc.setBlocking(false);

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_IDENTIFIER)
        .toFilter(), 10, TimeUnit.SECONDS);
  }

  private static enum WaitMethod {
    WAIT_INTERRUPTIBLY,
    WAIT_FOR_INTERRUPTIBLY_WITH_TIMEOUT,
    WAIT_FOR_UNINTERRUPTIBLY,
    WAIT_FOR_UNINTERRUPTIBLY_WITH_TIMEOUT,
  }

  private static enum InterruptionAction {
    INTERRUPT_BEFORE_ENTERING,
    INTERRUPT_WHILE_BLOCKING,
  }
}
