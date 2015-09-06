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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class FutureAwaitTest {

  private static ExecutorService s_executor;
  private IBean<Object> m_bean;

  @Before
  public void before() {
    m_bean = Platform.get().getBeanManager().registerBean(new BeanMetaData(JobManager.class, new JobManager()).withReplace(true).withOrder(-1));
  }

  @After
  public void after() {
    Jobs.getJobManager().shutdown();
    Platform.get().getBeanManager().unregisterBean(m_bean);
  }

  @BeforeClass
  public static void beforeClass() {
    s_executor = Executors.newFixedThreadPool(5);
  }

  @AfterClass
  public static void afterClass() {
    s_executor.shutdown();
  }

  @Test(timeout = 5000)
  public void testAwaitDone_Interrupted() throws InterruptedException, ProcessingException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Thread> thread = new AtomicReference<>();
    final AtomicReference<Throwable> error = new AtomicReference<>();
    thread.set(Thread.currentThread());

    // Test job
    IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    });

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          assertTrue(setupLatch.await());
          Thread.sleep(500);
          thread.get().interrupt();
          setupLatch.unblock();
        }
        catch (Throwable e) {
          error.set(e);
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      future.awaitDone();
      fail();
    }
    catch (ProcessingException e) {
      assertTrue(e.isInterruption());
    }
    assertNull(error.get());
  }

  @Test(timeout = 5000)
  public void testAwaitDone_Cancelled() throws InterruptedException, ProcessingException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Throwable> error = new AtomicReference<>();

    // Test job
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    });

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          assertTrue(setupLatch.await());
          Thread.sleep(500);
          future.cancel(false);
          setupLatch.unblock();
        }
        catch (Throwable e) {
          error.set(e);
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      future.awaitDone();
      assertTrue(future.isCancelled());
    }
    catch (ProcessingException e) {
      fail();
    }
    assertNull(error.get());
  }

  @Test(timeout = 5000)
  public void testAwaitDone_ComputationFailed() throws InterruptedException, ProcessingException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Throwable> error = new AtomicReference<>();
    final RuntimeException computationException = new RuntimeException();

    // Test job
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        throw computationException;
      }
    }, Jobs.newInput(null).withLogOnError(false));

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          assertTrue(setupLatch.await());
          Thread.sleep(500);
          setupLatch.unblock();
        }
        catch (Throwable e) {
          error.set(e);
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      future.awaitDone();
    }
    catch (ProcessingException e) {
      fail();
    }

    try {
      future.awaitDoneAndGet();
      fail();
    }
    catch (ProcessingException e) {
      assertSame(computationException, e.getCause());
    }
    assertNull(error.get());
  }

  @Test(timeout = 5000)
  public void testAwaitDoneWithTimeout_Interrupted() throws InterruptedException, ProcessingException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Thread> thread = new AtomicReference<>();
    final AtomicReference<Throwable> error = new AtomicReference<>();
    thread.set(Thread.currentThread());

    // Test job
    IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    });

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          assertTrue(setupLatch.await());
          Thread.sleep(500);
          thread.get().interrupt();
          setupLatch.unblock();
        }
        catch (Throwable e) {
          error.set(e);
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      assertTrue(future.awaitDone(10, TimeUnit.SECONDS));
      fail();
    }
    catch (ProcessingException e) {
      assertTrue(e.isInterruption());
    }
    assertNull(error.get());
  }

  @Test(timeout = 5000)
  public void testAwaitDoneWithTimeout_Cancelled() throws InterruptedException, ProcessingException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Throwable> error = new AtomicReference<>();

    // Test job
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    });

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          assertTrue(setupLatch.await());
          Thread.sleep(500);
          future.cancel(false);
          setupLatch.unblock();
        }
        catch (Throwable e) {
          error.set(e);
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      assertTrue(future.awaitDone(10, TimeUnit.SECONDS));
      assertTrue(future.isCancelled());
    }
    catch (ProcessingException e) {
      fail();
    }
    assertNull(error.get());
  }

  @Test(timeout = 5000)
  public void testAwaitDoneWithTimeout_Timeout() throws InterruptedException, ProcessingException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Throwable> error = new AtomicReference<>();

    // Test job
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    });

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          assertTrue(setupLatch.await());
          Thread.sleep(500);
          future.cancel(false);
          setupLatch.unblock();
        }
        catch (Throwable e) {
          error.set(e);
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      assertFalse(future.awaitDone(0, TimeUnit.SECONDS));
    }
    catch (ProcessingException e) {
      fail();
    }
    assertNull(error.get());
  }

  @Test(timeout = 5000)
  public void testAwaitDoneWithTimeout_ComputationFailed() throws InterruptedException, ProcessingException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Throwable> error = new AtomicReference<>();
    final RuntimeException computationException = new RuntimeException();

    // Test job
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        throw computationException;
      }
    }, Jobs.newInput(null).withLogOnError(false));

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          assertTrue(setupLatch.await());
          Thread.sleep(500);
          setupLatch.unblock();
        }
        catch (Throwable e) {
          error.set(e);
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      assertTrue(future.awaitDone(10, TimeUnit.SECONDS));
    }
    catch (ProcessingException e) {
      fail();
    }

    try {
      future.awaitDoneAndGet();
      fail();
    }
    catch (ProcessingException e) {
      assertSame(computationException, e.getCause());
    }
    assertNull(error.get());
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGet_Interrupted() throws InterruptedException, ProcessingException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Thread> thread = new AtomicReference<>();
    final AtomicReference<Throwable> error = new AtomicReference<>();
    thread.set(Thread.currentThread());

    // Test job
    IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    });

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          assertTrue(setupLatch.await());
          Thread.sleep(500);
          thread.get().interrupt();
          setupLatch.unblock();
        }
        catch (Throwable e) {
          error.set(e);
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      future.awaitDoneAndGet();
      fail();
    }
    catch (ProcessingException e) {
      assertTrue(e.isInterruption());
    }
    assertNull(error.get());
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGet_Cancelled() throws InterruptedException, ProcessingException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Throwable> error = new AtomicReference<>();

    // Test job
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    });

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          assertTrue(setupLatch.await());
          Thread.sleep(500);
          future.cancel(false);
          setupLatch.unblock();
        }
        catch (Throwable e) {
          error.set(e);
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      future.awaitDoneAndGet();
      fail();
    }
    catch (ProcessingException e) {
      assertTrue(e.isCancellation());
      assertTrue(future.isCancelled());
    }
    assertNull(error.get());
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGet_ComputationFailed() throws InterruptedException, ProcessingException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Throwable> error = new AtomicReference<>();
    final RuntimeException computationException = new RuntimeException();

    // Test job
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        throw computationException;
      }
    }, Jobs.newInput(null).withLogOnError(false));

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          assertTrue(setupLatch.await());
          Thread.sleep(500);
          setupLatch.unblock();
        }
        catch (Throwable e) {
          error.set(e);
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      future.awaitDoneAndGet();
      fail();
    }
    catch (ProcessingException e) {
      assertSame(computationException, e.getCause());
    }
    assertNull(error.get());
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGetWithTimeout_Interrupted() throws InterruptedException, ProcessingException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Thread> thread = new AtomicReference<>();
    final AtomicReference<Throwable> error = new AtomicReference<>();
    thread.set(Thread.currentThread());

    // Test job
    IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    });

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          assertTrue(setupLatch.await());
          Thread.sleep(500);
          thread.get().interrupt();
          setupLatch.unblock();
        }
        catch (Throwable e) {
          error.set(e);
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      future.awaitDoneAndGet(10, TimeUnit.SECONDS);
      fail();
    }
    catch (ProcessingException e) {
      assertTrue(e.isInterruption());
    }
    assertNull(error.get());
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGetWithTimeout_Cancelled() throws InterruptedException, ProcessingException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Throwable> error = new AtomicReference<>();

    // Test job
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    });

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          assertTrue(setupLatch.await());
          Thread.sleep(500);
          future.cancel(false);
          setupLatch.unblock();
        }
        catch (Throwable e) {
          error.set(e);
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      future.awaitDoneAndGet(10, TimeUnit.SECONDS);
      fail();
    }
    catch (ProcessingException e) {
      assertTrue(e.isCancellation());
      assertTrue(future.isCancelled());
    }
    assertNull(error.get());
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGetWithTimeout_ComputationFailed() throws InterruptedException, ProcessingException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Throwable> error = new AtomicReference<>();
    final RuntimeException computationException = new RuntimeException();

    // Test job
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        throw computationException;
      }
    }, Jobs.newInput(null).withLogOnError(false));

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          assertTrue(setupLatch.await());
          Thread.sleep(500);
          setupLatch.unblock();
        }
        catch (Throwable e) {
          error.set(e);
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      future.awaitDoneAndGet(10, TimeUnit.SECONDS);
      fail();
    }
    catch (ProcessingException e) {
      assertSame(computationException, e.getCause());
    }
    assertNull(error.get());
  }

  @Test(timeout = 5000)
  public void testAwaitDoneAndGetWithTimeout_Timeout() throws InterruptedException, ProcessingException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Throwable> error = new AtomicReference<>();

    // Test job
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    }, Jobs.newInput(null).withLogOnError(false));

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          assertTrue(setupLatch.await());
          Thread.sleep(500);
          setupLatch.unblock();
        }
        catch (Throwable e) {
          error.set(e);
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      future.awaitDoneAndGet(0, TimeUnit.SECONDS);
      fail();
    }
    catch (ProcessingException e) {
      assertTrue(e.isTimeout());
    }
    assertNull(error.get());
  }

  @Test(timeout = 5000)
  public void testJobManagerAwaitDone_Interrupted() throws InterruptedException, ProcessingException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Thread> thread = new AtomicReference<>();
    final AtomicReference<Throwable> error = new AtomicReference<>();
    thread.set(Thread.currentThread());

    // Test job
    IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    });

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          assertTrue(setupLatch.await());
          Thread.sleep(500);
          thread.get().interrupt();
          setupLatch.unblock();
        }
        catch (Throwable e) {
          error.set(e);
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      assertTrue(Jobs.getJobManager().awaitDone(Jobs.newFutureFilter().andMatchAnyFuture(future), 10, TimeUnit.SECONDS));
      fail();
    }
    catch (ProcessingException e) {
      assertTrue(e.isInterruption());
    }
    assertNull(error.get());
  }

  @Test(timeout = 5000)
  public void testJobManagerAwaitDone_Cancelled() throws InterruptedException, ProcessingException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Throwable> error = new AtomicReference<>();

    // Test job
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    });

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          assertTrue(setupLatch.await());
          Thread.sleep(500);
          future.cancel(false);
          setupLatch.unblock();
        }
        catch (Throwable e) {
          error.set(e);
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      assertTrue(Jobs.getJobManager().awaitDone(Jobs.newFutureFilter().andMatchAnyFuture(future), 10, TimeUnit.SECONDS));
      assertTrue(future.isCancelled());
    }
    catch (ProcessingException e) {
      fail();
    }
    assertNull(error.get());
  }

  @Test(timeout = 5000)
  public void testJobManagerAwaitDone_Timeout() throws InterruptedException, ProcessingException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Throwable> error = new AtomicReference<>();

    // Test job
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        return "result";
      }
    });

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          assertTrue(setupLatch.await());
          Thread.sleep(500);
          future.cancel(false);
          setupLatch.unblock();
        }
        catch (Throwable e) {
          error.set(e);
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      assertFalse(Jobs.getJobManager().awaitDone(Jobs.newFutureFilter().andMatchAnyFuture(future), 1, TimeUnit.NANOSECONDS));
    }
    catch (ProcessingException e) {
      fail();
    }
    assertNull(error.get());
  }

  @Test(timeout = 5000)
  public void testJobManagerAwaitDone_ComputationFailed() throws InterruptedException, ProcessingException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final AtomicReference<Throwable> error = new AtomicReference<>();
    final RuntimeException computationException = new RuntimeException();

    // Test job
    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        setupLatch.countDownAndBlock();
        throw computationException;
      }
    }, Jobs.newInput(null).withLogOnError(false));

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          assertTrue(setupLatch.await());
          Thread.sleep(500);
          setupLatch.unblock();
        }
        catch (Throwable e) {
          error.set(e);
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      assertTrue(Jobs.getJobManager().awaitDone(Jobs.newFutureFilter().andMatchAnyFuture(future), 10, TimeUnit.SECONDS));
    }
    catch (ProcessingException e) {
      fail();
    }

    assertNull(error.get());
  }

  @Test(timeout = 5000)
  public void testBlockingConditionWaitFor_Interrupted() throws InterruptedException, ProcessingException {
    final IBlockingCondition bc = Jobs.getJobManager().createBlockingCondition("BC", true);

    final AtomicReference<Thread> thread = new AtomicReference<>();
    thread.set(Thread.currentThread());

    // Test controller
    s_executor.submit(new Runnable() {

      @Override
      public void run() {
        try {
          Thread.sleep(500);
          thread.get().interrupt();
        }
        catch (Throwable e) {
          // NOOP
        }
      }
    });

    // Run test and verify
    try {
      bc.waitFor();
      fail();
    }
    catch (ProcessingException e) {
      assertTrue(e.isInterruption());
    }
  }

  @Test(timeout = 5000)
  public void testBlockingConditionWaitFor_Timeout() throws InterruptedException, ProcessingException {
    final IBlockingCondition bc = Jobs.getJobManager().createBlockingCondition("BC", true);

    // Run test and verify
    try {
      assertFalse(bc.waitFor(1, TimeUnit.NANOSECONDS));
    }
    catch (ProcessingException e) {
      fail();
    }
  }
}
