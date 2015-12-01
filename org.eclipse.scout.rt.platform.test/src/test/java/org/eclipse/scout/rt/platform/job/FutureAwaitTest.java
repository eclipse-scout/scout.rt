/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class FutureAwaitTest {

  private IBean<IJobManager> m_jobManagerBean;

  @Before
  public void before() {
    m_jobManagerBean = JobTestUtil.registerJobManager();
  }

  @After
  public void after() {
    JobTestUtil.unregisterJobManager(m_jobManagerBean);
  }

  @Test(timeout = 5000)
  public void testAwaitDone_Interrupted() throws InterruptedException {
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
          fail();
        }
        catch (ProcessingException e) {
          assertTrue(e.isInterruption());
        }
      }
    }, Jobs.newInput());
    assertTrue(controller.awaitDone(10, TimeUnit.SECONDS));
    setupLatch.unblock();
  }

  @Test(timeout = 5000)
  public void testAwaitDone_Cancelled() throws InterruptedException {
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
      fail();
    }
    catch (ProcessingException e) {
      assertTrue(e.isCancellation());
    }
  }

  @Test(timeout = 5000)
  public void testAwaitDone_ComputationFailed() throws InterruptedException {
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
    catch (ProcessingException e) {
      fail();
    }

    // Run the test and verify
    try {
      future.awaitDoneAndGet();
      fail();
    }
    catch (ProcessingException e) {
      assertSame(computationException, e.getCause());
    }
  }

  @Test(timeout = 5000)
  public void testAwaitDoneWithTimeout_Interrupted() throws InterruptedException {
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
          fail();
        }
        catch (ProcessingException e) {
          assertTrue(e.isInterruption());
        }
      }
    }, Jobs.newInput());
    assertTrue(controller.awaitDone(10, TimeUnit.SECONDS));
    setupLatch.unblock();
  }

  @Test(timeout = 5000)
  public void testAwaitDoneWithTimeout_Cancelled() throws InterruptedException {
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
    assertTrue(future.awaitDone(10, TimeUnit.SECONDS));
  }

  @Test(timeout = 5000)
  public void testAwaitDoneWithTimeout_Timeout() throws InterruptedException {
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
    assertFalse(future.awaitDone(5, TimeUnit.MILLISECONDS));
    setupLatch.unblock();
  }

  @Test(timeout = 5000)
  public void testAwaitDoneWithTimeout_ComputationFailed() throws InterruptedException {
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
    try {
      assertTrue(future.awaitDone(10, TimeUnit.SECONDS));
    }
    catch (ProcessingException e) {
      fail();
    }

    // Run the test and verify
    try {
      future.awaitDoneAndGet();
      fail();
    }
    catch (ProcessingException e) {
      assertSame(computationException, e.getCause());
    }
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGet_Interrupted() throws InterruptedException {
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
          fail();
        }
        catch (ProcessingException e) {
          assertTrue(e.isInterruption());
        }
      }
    }, Jobs.newInput());
    assertTrue(controller.awaitDone(10, TimeUnit.SECONDS));
    setupLatch.unblock();
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGet_Cancelled() throws InterruptedException {
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
      fail();
    }
    catch (ProcessingException e) {
      assertTrue(e.isCancellation());
      assertTrue(future.isCancelled());
    }
    setupLatch.unblock();
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGet_ComputationFailed() throws InterruptedException {
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
    catch (ProcessingException e) {
      assertSame(computationException, e.getCause());
    }
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGetWithTimeout_Interrupted() throws InterruptedException {
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
          fail();
        }
        catch (ProcessingException e) {
          assertTrue(e.isInterruption());
        }
      }
    }, Jobs.newInput());
    assertTrue(controller.awaitDone(10, TimeUnit.SECONDS));
    setupLatch.unblock();
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGetWithTimeout_Cancelled() throws InterruptedException {
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
      fail();
    }
    catch (ProcessingException e) {
      assertTrue(e.isCancellation());
      assertTrue(future.isCancelled());
    }

    setupLatch.unblock();
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGetWithTimeout_ComputationFailed() throws InterruptedException {
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
      fail();
    }
    catch (ProcessingException e) {
      assertSame(computationException, e.getCause());
    }
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGetWithTimeout_Timeout() throws InterruptedException {
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
      fail();
    }
    catch (ProcessingException e) {
      assertTrue(e.isTimeout());
    }
  }

  @Test(timeout = 5000)
  public void testJobManagerAwaitDone_Interrupted() throws InterruptedException {
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
          assertTrue(Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
              .andMatchFuture(future)
              .toFilter(), 10, TimeUnit.SECONDS));
          fail();
        }
        catch (ProcessingException e) {
          assertTrue(e.isInterruption());
        }
      }
    }, Jobs.newInput());
    assertTrue(controller.awaitDone(10, TimeUnit.SECONDS));
    setupLatch.unblock();
  }

  @Test(timeout = 5000)
  public void testJobManagerAwaitDone_Cancelled() throws InterruptedException {
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
    assertTrue(Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 10, TimeUnit.SECONDS));
    assertTrue(future.isCancelled());

    setupLatch.unblock();
  }

  @Test(timeout = 5000)
  public void testJobManagerAwaitDone_Timeout() throws InterruptedException {
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
    assertFalse(Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 1, TimeUnit.MILLISECONDS));
  }

  @Test(timeout = 5000)
  public void testJobManagerAwaitDone_ComputationFailed() throws InterruptedException {
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
    assertTrue(Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 10, TimeUnit.SECONDS));
  }

  @Test(timeout = 5000)
  public void testBlockingConditionWaitFor_Interrupted() throws InterruptedException {
    final IBlockingCondition bc = Jobs.getJobManager().createBlockingCondition("BC", true);

    // Run the test in a separate thread
    IFuture<Void> controller = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        Thread.currentThread().interrupt();

        try {
          bc.waitFor();
          fail();
        }
        catch (ProcessingException e) {
          assertTrue(e.isInterruption());
        }
      }
    }, Jobs.newInput());

    assertTrue(controller.awaitDone(10, TimeUnit.SECONDS));
  }

  @Test(timeout = 5000)
  public void testBlockingConditionWaitFor_Timeout() throws InterruptedException {
    final IBlockingCondition bc = Jobs.getJobManager().createBlockingCondition("BC", true);

    // Run test and verify
    assertFalse(bc.waitFor(1, TimeUnit.NANOSECONDS));
  }
}
