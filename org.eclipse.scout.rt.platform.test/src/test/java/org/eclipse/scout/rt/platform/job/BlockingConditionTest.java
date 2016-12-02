package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class BlockingConditionTest {

  @Test
  public void testHints() throws Throwable {
    final IExecutionSemaphore semaphore = Jobs.newExecutionSemaphore(1);
    final IBlockingCondition blockingCondition = Jobs.newBlockingCondition(true);
    final P_ExceptionCapturer exceptionCapturer = new P_ExceptionCapturer();

    final IFuture<Void> future1 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        blockingCondition.waitFor("hint-blocking");
        assertFalse("hint not unset", IFuture.CURRENT.get().containsExecutionHint("hint-blocking"));
      }
    }, Jobs.newInput()
        .withName("job-1")
        .withExceptionHandling(exceptionCapturer, true)
        .withExecutionSemaphore(semaphore));

    final IFuture<Void> future2 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertTrue("hint not set", future1.containsExecutionHint("hint-blocking"));
        blockingCondition.setBlocking(false);
        assertFalse("hint not unset (immediately)", future1.containsExecutionHint("hint-blocking"));
      }
    }, Jobs.newInput()
        .withName("job-2")
        .withExceptionHandling(exceptionCapturer, true)
        .withExecutionSemaphore(semaphore));

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future1, future2)
        .toFilter(), 10, TimeUnit.SECONDS);

    exceptionCapturer.throwOnError();
  }

  @Test
  public void testHintsUponTimeout() throws Throwable {
    final IBlockingCondition blockingCondition = Jobs.newBlockingCondition(true);
    final P_ExceptionCapturer exceptionCapturer = new P_ExceptionCapturer();

    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          blockingCondition.waitFor(1, TimeUnit.NANOSECONDS, "hint-blocking");
          fail("TimedOutError expected");
        }
        catch (TimedOutError e) {
          assertFalse("hint not unset", IFuture.CURRENT.get().containsExecutionHint("hint-blocking"));
        }
      }
    }, Jobs.newInput()
        .withExceptionHandling(exceptionCapturer, true))
        .awaitDone();

    exceptionCapturer.throwOnError();
  }

  @Test
  public void testHintsUponInterruption() throws Throwable {
    final IBlockingCondition blockingCondition = Jobs.newBlockingCondition(true);
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final P_ExceptionCapturer exceptionCapturer = new P_ExceptionCapturer();

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        setupLatch.countDown();

        try {
          blockingCondition.waitFor(10, TimeUnit.SECONDS, "hint-blocking");
          fail("ThreadInterruptedError expected");
        }
        catch (ThreadInterruptedError e) {
          assertFalse("hint not unset", IFuture.CURRENT.get().containsExecutionHint("hint-blocking"));
        }
      }
    }, Jobs.newInput()
        .withExceptionHandling(exceptionCapturer, true));

    setupLatch.await();
    future.cancel(true);
    future.awaitFinished(10, TimeUnit.SECONDS);

    exceptionCapturer.throwOnError();
  }

  private static class P_ExceptionCapturer extends ExceptionHandler {

    private final AtomicReference<Throwable> throwable = new AtomicReference<>();

    @Override
    public void handle(Throwable t) {
      throwable.set(t);
    }

    public void throwOnError() throws Throwable {
      if (throwable.get() != null) {
        throw throwable.get();
      }
    }
  }
}
