package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledException;
import org.eclipse.scout.rt.platform.util.concurrent.IBiFunction;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedException;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutException;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.Times;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
@Times(100) // for regression purpose
public class WhenDoneScheduleTest {

  private static final String JOB_MARKER = UUID.randomUUID().toString();

  @Test
  public void testCascading() {
    IFuture<String> future = Jobs.schedule(
        new Callable<String>() {

          @Override
          public String call() throws Exception {
            return "a";
          }
        }, Jobs.newInput())
        .whenDoneSchedule(new IBiFunction<String, Throwable, String>() {

          @Override
          public String apply(String result, Throwable u) {
            return result + "b";
          }
        }, Jobs.newInput())
        .whenDoneSchedule(new IBiFunction<String, Throwable, String>() {

          @Override
          public String apply(String result, Throwable u) {
            return result + "c";
          }
        }, Jobs.newInput())
        .whenDoneSchedule(new IBiFunction<String, Throwable, String>() {

          @Override
          public String apply(String result, Throwable u) {
            return result + "d";
          }
        }, Jobs.newInput());

    assertEquals("abcd", future.awaitDoneAndGet());
  }

  @Test
  public void testCascadingException() {
    final Exception exception1 = new Exception("JUnit test exception 1");
    final RuntimeException exception2 = new RuntimeException("JUnit test exception 2");
    final RuntimeException exception3 = new RuntimeException("JUnit test exception 3");
    final RuntimeException exception4 = new RuntimeException("JUnit test exception 4");

    IFuture<String> future = Jobs.schedule(
        new Callable<String>() {

          @Override
          public String call() throws Exception {
            throw exception1;
          }
        }, Jobs.newInput()
            .withExceptionHandling(null, false))
        .whenDoneSchedule(new IBiFunction<String, Throwable, String>() {

          @Override
          public String apply(String result, Throwable e) {
            assertSame(exception1, e);
            throw exception2;
          }
        }, Jobs.newInput()
            .withExceptionHandling(null, false))
        .whenDoneSchedule(new IBiFunction<String, Throwable, String>() {

          @Override
          public String apply(String result, Throwable e) {
            assertSame(exception2, e);
            throw exception3;
          }
        }, Jobs.newInput()
            .withExceptionHandling(null, false))
        .whenDoneSchedule(new IBiFunction<String, Throwable, String>() {

          @Override
          public String apply(String result, Throwable e) {
            assertSame(exception3, e);
            throw exception4;
          }
        }, Jobs.newInput()
            .withExceptionHandling(null, false));

    try {
      future.awaitDoneAndGet();
      fail("exception expected");
    }
    catch (Exception e) {
      assertSame(exception4, e);
    }
  }

  @Test
  public void testNotSameFuture() {
    // Schedule future
    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
      }
    }, Jobs.newInput());

    // Schedule function
    IFuture<Void> functionFuture = future.whenDoneSchedule(new IBiFunction<Void, Throwable, Void>() {

      @Override
      public Void apply(Void t, Throwable u) {
        return null;
      }
    }, Jobs.newInput());

    assertNotSame(future, functionFuture);
  }

  @Test
  public void testResult() {
    // Schedule future
    IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        return "abc";
      }
    }, Jobs.newInput());

    // Schedule function
    IFuture<Integer> functionFuture = future.whenDoneSchedule(new IBiFunction<String, Throwable, Integer>() {

      @Override
      public Integer apply(String result, Throwable error) {
        return result.length();
      }
    }, Jobs.newInput());

    assertEquals("abc", future.awaitDoneAndGet());
    assertEquals(3, functionFuture.awaitDoneAndGet().intValue());
  }

  @Test
  public void testCancellationOfFuture() throws InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    // Schedule future
    IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "abc";
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, true) // to not work with JUnitExceptionHandler
        .withExecutionHint(JOB_MARKER));

    // Schedule function
    final AtomicBoolean functionExecuted = new AtomicBoolean(false);
    IFuture<Void> functionFuture = future.whenDoneSchedule(new IBiFunction<String, Throwable, Void>() {

      @Override
      public Void apply(String result, Throwable error) {
        functionExecuted.set(true);
        return null;
      }
    }, Jobs.newInput()
        .withExecutionHint(JOB_MARKER));

    assertTrue(setupLatch.await());
    future.cancel(true);

    // Wait until the job jobs are done
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_MARKER)
        .toFilter(), 5, TimeUnit.SECONDS);

    // Verify that the function is not executed
    assertFalse(functionExecuted.get());

    // Verify that both futures are cancelled
    assertFutureCancelled(future);
    assertFutureCancelled(functionFuture);
  }

  @Test
  public void testCancellationOfFunctionFuture() throws InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    // Schedule future
    IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "abc";
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, true) // to not work with JUnitExceptionHandler
        .withExecutionHint(JOB_MARKER));

    // Schedule function
    final AtomicBoolean functionExecuted = new AtomicBoolean(false);
    IFuture<Void> functionFuture = future.whenDoneSchedule(new IBiFunction<String, Throwable, Void>() {

      @Override
      public Void apply(String result, Throwable error) {
        functionExecuted.set(true);
        return null;
      }
    }, Jobs.newInput()
        .withExecutionHint(JOB_MARKER));

    assertTrue(setupLatch.await());
    functionFuture.cancel(true);
    setupLatch.unblock();

    // Wait until the job jobs are done
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_MARKER)
        .toFilter(), 5, TimeUnit.SECONDS);

    // Verify that the function is not executed
    assertFalse(functionExecuted.get());

    // Verify that future is not cancelled
    assertEquals("abc", future.awaitDoneAndGet());
    assertFalse(future.isCancelled());
    assertTrue(future.isDone());

    // Verify that function future is cancelled
    assertFutureCancelled(functionFuture);
  }

  @Test
  public void testCancellationOfFutureRunMonitor() throws InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    RunMonitor runMonitor = BEANS.get(RunMonitor.class);

    // Schedule future
    IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "abc";
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, true) // to not work with JUnitExceptionHandler
        .withRunContext(RunContexts.empty().withRunMonitor(runMonitor))
        .withExecutionHint(JOB_MARKER));

    // Schedule function
    final AtomicBoolean functionExecuted = new AtomicBoolean(false);
    IFuture<Void> functionFuture = future.whenDoneSchedule(new IBiFunction<String, Throwable, Void>() {

      @Override
      public Void apply(String result, Throwable error) {
        functionExecuted.set(true);
        return null;
      }
    }, Jobs.newInput()
        .withExecutionHint(JOB_MARKER));

    assertTrue(setupLatch.await());
    runMonitor.cancel(true);

    // Wait until the job jobs are done
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_MARKER)
        .toFilter(), 5, TimeUnit.SECONDS);

    // Verify that the function is not executed
    assertFalse(functionExecuted.get());

    // Verify that both futures are cancelled
    assertFutureCancelled(future);
    assertFutureCancelled(functionFuture);
  }

  @Test
  public void testCancellationOfFunctionRunMonitor() throws InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    // Schedule future
    IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "abc";
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, true) // to not work with JUnitExceptionHandler
        .withExecutionHint(JOB_MARKER));

    // Schedule function
    final AtomicBoolean functionExecuted = new AtomicBoolean(false);

    RunMonitor functionRunMonitor = BEANS.get(RunMonitor.class);
    IFuture<Void> functionFuture = future.whenDoneSchedule(new IBiFunction<String, Throwable, Void>() {

      @Override
      public Void apply(String result, Throwable error) {
        functionExecuted.set(true);
        return null;
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty().withRunMonitor(functionRunMonitor))
        .withExecutionHint(JOB_MARKER));

    assertTrue(setupLatch.await());
    functionRunMonitor.cancel(true);
    setupLatch.unblock();

    // Wait until the job jobs are done
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_MARKER)
        .toFilter(), 5, TimeUnit.SECONDS);

    // Verify that the function is not executed
    assertFalse(functionExecuted.get());

    // Verify that future is not cancelled
    assertEquals("abc", future.awaitDoneAndGet());
    assertFalse(future.isCancelled());
    assertTrue(future.isDone());

    // Verify that function future is cancelled
    assertFutureCancelled(functionFuture);
  }

  @Test
  public void testCancellationOfSharedRunMonitor() throws InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    RunMonitor sharedRunMonitor = BEANS.get(RunMonitor.class);

    // Schedule future
    IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "abc";
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, true) // to not work with JUnitExceptionHandler
        .withRunContext(RunContexts.empty().withRunMonitor(sharedRunMonitor))
        .withExecutionHint(JOB_MARKER));

    // Schedule function
    final AtomicBoolean functionExecuted = new AtomicBoolean(false);

    IFuture<Void> functionFuture = future.whenDoneSchedule(new IBiFunction<String, Throwable, Void>() {

      @Override
      public Void apply(String result, Throwable error) {
        functionExecuted.set(true);
        return null;
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty().withRunMonitor(sharedRunMonitor))
        .withExecutionHint(JOB_MARKER));

    assertTrue(setupLatch.await());
    sharedRunMonitor.cancel(true);

    // Wait until the job jobs are done
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_MARKER)
        .toFilter(), 5, TimeUnit.SECONDS);

    // Verify that the function is not executed
    assertFalse(functionExecuted.get());

    // Verify that both futures are cancelled
    assertFutureCancelled(future);
    assertFutureCancelled(functionFuture);
  }

  @Test
  public void testPostCancellation() throws InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    // Schedule future
    IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        return "abc";
      }
    }, Jobs.newInput()
        .withExecutionHint(JOB_MARKER));

    // Schedule function
    IFuture<String> functionFuture = future.whenDoneSchedule(new IBiFunction<String, Throwable, String>() {

      @Override
      public String apply(String result, Throwable error) {
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          throw new ThreadInterruptedException("", e);
        }
        return result.toUpperCase();
      }
    }, Jobs.newInput()
        .withExecutionHint(JOB_MARKER));

    assertTrue(setupLatch.await());
    // Cancel future which already completed
    future.cancel(true);
    setupLatch.unblock();
    assertEquals("abc", future.awaitDoneAndGet());
    assertEquals("ABC", functionFuture.awaitDoneAndGet());
    assertFalse(future.isCancelled());
    assertFalse(functionFuture.isCancelled());
  }

  @Test
  @Times(20)
  public void testSemaphore() throws InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    IExecutionSemaphore mutex = Jobs.newExecutionSemaphore(1).seal();

    // Schedule arbitrary job with same semaphore
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        setupLatch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withExecutionSemaphore(mutex));

    // Schedule future
    IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        return "abc";
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, true)); // to not work with JUnitExceptionHandler

    // Schedule function with same semaphore
    final AtomicBoolean functionExecuted = new AtomicBoolean(false);
    IFuture<Void> functionFuture = future.whenDoneSchedule(new IBiFunction<String, Throwable, Void>() {

      @Override
      public Void apply(String result, Throwable error) {
        functionExecuted.set(true);
        return null;
      }
    }, Jobs.newInput()
        .withExecutionSemaphore(mutex));

    assertEquals("abc", future.awaitDoneAndGet(5, TimeUnit.SECONDS));

    JobTestUtil.waitForPermitCompetitors(mutex, 2);

    // Function future must not have commenced execution yet.
    try {
      functionFuture.awaitDone(100, TimeUnit.MILLISECONDS);
      fail("timeout expected");
    }
    catch (TimedOutException e) {
      // NOOP
    }

    assertFalse(functionExecuted.get());
    setupLatch.unblock();
    functionFuture.awaitDone(5, TimeUnit.SECONDS);
    assertTrue(functionExecuted.get());

    assertFalse(future.isCancelled());
    assertTrue(future.isDone());
    assertFalse(functionFuture.isCancelled());
    assertTrue(functionFuture.isDone());
  }

  @Test
  public void testException() throws Exception {
    final Exception testException = new Exception("JUnit test exception");

    final AtomicReference<Throwable> actualException = new AtomicReference<>();
    final AtomicReference<Object> actualResult = new AtomicReference<>();

    IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        throw testException;
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, false)); // to not work with JUnitExceptionHandler

    IFuture<Integer> functionFuture = future.whenDoneSchedule(new IBiFunction<String, Throwable, Integer>() {

      @Override
      public Integer apply(String result, Throwable error) {
        actualResult.set(result);
        actualException.set(error);
        return 123;
      }
    }, Jobs.newInput());

    // Wait until function future completed
    functionFuture.awaitDone();

    try {
      future.awaitDoneAndGet(DefaultExceptionTranslator.class);
      fail("exception expected");
    }
    catch (Exception e) {
      assertSame(testException, e);
    }

    assertEquals(123, functionFuture.awaitDoneAndGet(DefaultExceptionTranslator.class).intValue());
    assertSame(testException, actualException.get());
    assertNull(actualResult.get());
    assertFalse(future.isCancelled());
    assertFalse(functionFuture.isCancelled());
  }

  @Test
  public void testExceptionInFunction() throws Exception {
    final RuntimeException testException = new RuntimeException("JUnit test exception");

    final AtomicReference<Throwable> actualException = new AtomicReference<>();
    final AtomicReference<Object> actualResult = new AtomicReference<>();

    IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        return "abc";
      }
    }, Jobs.newInput());

    IFuture<Integer> functionFuture = future.whenDoneSchedule(new IBiFunction<String, Throwable, Integer>() {

      @Override
      public Integer apply(String result, Throwable error) {
        actualResult.set(result);
        actualException.set(error);
        throw testException;
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, false)); // to not work with JUnitExceptionHandler

    // Wait until function future completed
    functionFuture.awaitDone();

    assertEquals("abc", future.awaitDoneAndGet());
    assertNull(actualException.get());
    assertEquals("abc", actualResult.get());

    try {
      functionFuture.awaitDoneAndGet();
      fail("exception expected");
    }
    catch (Exception e) {
      assertSame(testException, e);
    }

    assertFalse(future.isCancelled());
    assertFalse(functionFuture.isCancelled());
  }

  @Test
  public void testExecutionHints() throws InterruptedException {
    // Schedule future
    IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        return "abc";
      }
    }, Jobs.newInput()
        .withExecutionHint(JOB_MARKER));

    final BlockingCountDownLatch hint1AddedLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch hint1RemovedLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch hint2AddedLach = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch hint2RemovedLatch = new BlockingCountDownLatch(1);

    // Schedule function
    IFuture<String> functionFuture = future.whenDoneSchedule(new IBiFunction<String, Throwable, String>() {

      @Override
      public String apply(String result, Throwable error) {
        assertTrue(IFuture.CURRENT.get().containsExecutionHint(JOB_MARKER));
        try {
          // Make hint changes to the future
          IFuture.CURRENT.get().addExecutionHint("HINT1");
          hint1AddedLatch.countDownAndBlock();
          IFuture.CURRENT.get().removeExecutionHint("HINT1");
          hint1RemovedLatch.countDownAndBlock();

          // Verify that external hint changes are reflected
          assertTrue(hint2AddedLach.await());
          assertTrue(IFuture.CURRENT.get().containsExecutionHint("HINT2"));
          hint2AddedLach.unblock();

          assertTrue(hint2RemovedLatch.await());
          assertFalse(IFuture.CURRENT.get().containsExecutionHint("HINT2"));
          hint2RemovedLatch.unblock();
        }
        catch (InterruptedException e) {
          throw new ThreadInterruptedException("", e);
        }
        return result.toUpperCase();
      }
    }, Jobs.newInput()
        .withExecutionHint(JOB_MARKER));

    try {
      assertTrue(functionFuture.containsExecutionHint(JOB_MARKER));
      assertTrue(hint1AddedLatch.await());

      // Verify that internal hint changes are reflected
      assertTrue(functionFuture.containsExecutionHint("HINT1"));
      hint1AddedLatch.unblock();
      assertTrue(hint1RemovedLatch.await());
      assertFalse(functionFuture.containsExecutionHint("HINT1"));
      hint1RemovedLatch.unblock();

      // Make hint changes to the future
      functionFuture.addExecutionHint("HINT2");
      assertTrue(hint2AddedLach.countDownAndBlock());
      functionFuture.removeExecutionHint("HINT2");
      assertTrue(hint2RemovedLatch.countDownAndBlock());

      assertEquals("abc", future.awaitDoneAndGet());
      assertEquals("ABC", functionFuture.awaitDoneAndGet());
    }
    finally {
      Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
          .andMatchExecutionHint(JOB_MARKER)
          .toFilter(), true);
    }
  }

  private static void assertFutureCancelled(IFuture<?> future) {
    try {
      future.awaitDoneAndGet();
      fail("cancellation excepted");
    }
    catch (FutureCancelledException e) {
      assertTrue(future.isCancelled());
      assertTrue(future.isDone());
    }
  }
}
