/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.util.concurrent.CancellationException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.InterruptedException;
import org.eclipse.scout.rt.platform.util.concurrent.TimeoutException;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class FutureAwaitTest {

  @Test(timeout = 5000)
  public void testAwaitDone_Interrupted() throws java.lang.InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    // Init
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    // Run the test in a separate thread
    IFuture<Void> controller = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        Thread.currentThread().interrupt();
        try {
          future.awaitDone();
          fail("interruption expected");
        }
        catch (InterruptedException e) {
          assertTrue(Thread.currentThread().isInterrupted());
        }
      }
    }, Jobs.newInput());
    controller.awaitDoneAndGet(10, TimeUnit.SECONDS);
    setupLatch.unblock();
    future.awaitDone(10, TimeUnit.SECONDS);
  }

  @Test(timeout = 5000)
  public void testAwaitDone_Cancelled() throws java.lang.InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    // Init
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    // Wait until ready
    assertTrue(setupLatch.await());

    // Run the test and verify
    future.cancel(false);
    assertTrue(future.isCancelled());
    future.awaitDone();
    try {
      future.awaitDoneAndGet();
      fail("cancellation expected");
    }
    catch (CancellationException e) {
      // NOOP
    }
  }

  @Test(timeout = 5000)
  public void testAwaitDone_ComputationFailed() throws java.lang.InterruptedException {
    final RuntimeException computationException = new RuntimeException();

    // Init
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        throw computationException;
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, false));

    // Run the test and verify
    try {
      future.awaitDone();
    }
    catch (RuntimeException e) {
      fail();
    }

    // Run the test and verify
    try {
      future.awaitDoneAndGet();
      fail();
    }
    catch (RuntimeException e) {
      assertSame(computationException, e);
    }
  }

  @Test(timeout = 5000)
  public void testAwaitDoneWithTimeout_Interrupted() throws java.lang.InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    // Init
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    // Run the test in a separate thread
    IFuture<Void> controller = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        Thread.currentThread().interrupt();
        try {
          future.awaitDone(10, TimeUnit.SECONDS);
          fail("interruption expected");
        }
        catch (InterruptedException e) {
          assertTrue(Thread.currentThread().isInterrupted());
        }
      }
    }, Jobs.newInput());
    controller.awaitDoneAndGet(10, TimeUnit.SECONDS);
    setupLatch.unblock();
    future.awaitDone(10, TimeUnit.SECONDS);
  }

  @Test(timeout = 5000)
  public void testAwaitDoneWithTimeout_Cancelled() throws java.lang.InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    // Init
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    // Wait until ready
    assertTrue(setupLatch.await());

    // Run the test and verify
    future.cancel(false);
    assertTrue(future.isCancelled());
    future.awaitDone(10, TimeUnit.SECONDS);
  }

  @Test(timeout = 5000)
  public void testAwaitDoneWithTimeout_Timeout() throws java.lang.InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    // Init
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    // Wait until ready
    assertTrue(setupLatch.await());

    // Run the test and verify
    try {
      future.awaitDone(5, TimeUnit.MILLISECONDS);
      fail("timeout expected");
    }
    catch (TimeoutException e) {
      // NOOP
    }
    setupLatch.unblock();
  }

  @Test(timeout = 5000)
  public void testAwaitDoneWithTimeout_ComputationFailed() throws java.lang.InterruptedException {
    final RuntimeException computationException = new RuntimeException();

    // Init
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        throw computationException;
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, false));

    // Run test and verify
    future.awaitDone(10, TimeUnit.SECONDS);

    // Run the test and verify
    try {
      future.awaitDoneAndGet();
      fail();
    }
    catch (RuntimeException e) {
      assertSame(computationException, e);
    }
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGet_Interrupted() throws java.lang.InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    // Init
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    // Run the test in a separate thread
    IFuture<Void> controller = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        Thread.currentThread().interrupt();
        try {
          future.awaitDoneAndGet();
          fail("interruption expected");
        }
        catch (InterruptedException e) {
          assertTrue(Thread.currentThread().isInterrupted());
        }
      }
    }, Jobs.newInput());
    controller.awaitDoneAndGet(10, TimeUnit.SECONDS);
    setupLatch.unblock();
    future.awaitDone(10, TimeUnit.SECONDS);
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGet_Cancelled() throws java.lang.InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    // Init
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    // Wait until ready
    assertTrue(setupLatch.await());

    // Run the test and verify
    future.cancel(false);
    try {
      future.awaitDoneAndGet();
      fail("cancellation expected");
    }
    catch (CancellationException e) {
      assertTrue(future.isCancelled());
    }
    setupLatch.unblock();
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGet_ComputationFailed() throws java.lang.InterruptedException {
    final RuntimeException computationException = new RuntimeException();

    // Init
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        throw computationException;
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, false));

    // Run the test and verify
    try {
      future.awaitDoneAndGet();
      fail();
    }
    catch (RuntimeException e) {
      assertSame(computationException, e);
    }
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGetWithTimeout_Interrupted() throws java.lang.InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    // Init
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    // Run the test in a separate thread
    IFuture<Void> controller = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        Thread.currentThread().interrupt();
        try {
          future.awaitDoneAndGet(10, TimeUnit.SECONDS);
          fail("interruption expected");
        }
        catch (InterruptedException e) {
          assertTrue(Thread.currentThread().isInterrupted());
        }
      }
    }, Jobs.newInput());
    controller.awaitDoneAndGet(10, TimeUnit.SECONDS);
    setupLatch.unblock();
    future.awaitDone(10, TimeUnit.SECONDS);
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGetWithTimeout_Cancelled() throws java.lang.InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    // Init
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    // Wait until ready
    assertTrue(setupLatch.await());

    // Run the test and verify
    future.cancel(false);
    try {
      future.awaitDoneAndGet(10, TimeUnit.SECONDS);
      fail("cancellation expected");
    }
    catch (CancellationException e) {
      assertTrue(future.isCancelled());
    }

    setupLatch.unblock();
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGetWithTimeout_ComputationFailed() throws java.lang.InterruptedException {
    final RuntimeException computationException = new RuntimeException();

    // Init
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        throw computationException;
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, false));

    // Run the test and verify
    try {
      future.awaitDoneAndGet(10, TimeUnit.SECONDS);
      fail("execution exception expected");
    }
    catch (TimeoutException e) {
      fail("execution exception expected");
    }
    catch (RuntimeException e) {
      assertSame(computationException, e);
    }
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGetWithTimeout_Timeout() throws java.lang.InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    // Init
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, false));

    // Wait until ready
    assertTrue(setupLatch.await());

    // Run the test and verify
    try {
      future.awaitDoneAndGet(5, TimeUnit.MILLISECONDS);
      fail("timeout expected");
    }
    catch (TimeoutException e) {
      // NOOP
    }

    // cleanup
    setupLatch.unblock();
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 10, TimeUnit.SECONDS);
  }

  @Test //(timeout = 5000)
  public void testJobManagerAwaitDone_Interrupted() throws java.lang.InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    // Init
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    // Run the test in a separate thread
    IFuture<Void> controller = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        Thread.currentThread().interrupt();
        try {
          Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
              .andMatchFuture(future)
              .toFilter(), 10, TimeUnit.SECONDS);
          fail("interruption expected");
        }
        catch (InterruptedException e) {
          assertTrue(Thread.currentThread().isInterrupted());
        }
      }
    }, Jobs.newInput());
    controller.awaitDoneAndGet(10, TimeUnit.SECONDS);
    setupLatch.unblock();
    future.awaitDone(10, TimeUnit.SECONDS);
  }

  @Test(timeout = 5000)
  public void testJobManagerAwaitDone_Cancelled() throws java.lang.InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    // Init
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    // Wait until ready
    assertTrue(setupLatch.await());

    // Run the test and verify
    future.cancel(false);
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 10, TimeUnit.SECONDS);
    assertTrue(future.isCancelled());

    setupLatch.unblock();
  }

  @Test(timeout = 5000)
  public void testJobManagerAwaitDone_Timeout() throws java.lang.InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    // Init
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    }, Jobs.newInput().withRunContext(RunContexts.copyCurrent()));

    // Wait until ready
    assertTrue(setupLatch.await());

    // Run the test and verify
    try {
      Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
          .andMatchFuture(future)
          .toFilter(), 1, TimeUnit.MILLISECONDS);
      fail("timeout expected");
    }
    catch (TimeoutException e) {
      // NOOP
    }

    // cleanup
    setupLatch.unblock();
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 10, TimeUnit.SECONDS);
  }

  @Test(timeout = 5000)
  public void testJobManagerAwaitDone_ComputationFailed() throws java.lang.InterruptedException {
    final RuntimeException computationException = new RuntimeException();

    // Init
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        throw computationException;
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, false));

    // Run the test and verify
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 10, TimeUnit.SECONDS);
  }

  @Test(timeout = 5000)
  public void testBlockingConditionWaitFor_Interrupted() throws java.lang.InterruptedException {
    final IBlockingCondition condition = Jobs.newBlockingCondition(true);

    // Run the test in a separate thread
    IFuture<Void> controller = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        Thread.currentThread().interrupt();

        try {
          condition.waitFor();
          fail("interruption expected");
        }
        catch (InterruptedException e) {
          assertTrue(Thread.currentThread().isInterrupted());
        }
      }
    }, Jobs.newInput());

    controller.awaitDoneAndGet(10, TimeUnit.SECONDS);
  }

  @Test(timeout = 5000)
  public void testBlockingConditionWaitFor_Timeout() {
    final IBlockingCondition condition = Jobs.newBlockingCondition(true);

    try {
      condition.waitFor(1, TimeUnit.NANOSECONDS);
      fail("timeout expected");
    }
    catch (TimeoutException e) {
      // NOOP
    }
  }
}
