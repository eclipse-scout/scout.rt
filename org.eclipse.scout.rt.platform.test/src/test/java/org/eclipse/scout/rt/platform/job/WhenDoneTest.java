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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class WhenDoneTest {

  private IJobManager m_jobManager;

  @Before
  public void before() {
    m_jobManager = new JobManager();
  }

  @After
  public void after() {
    m_jobManager.shutdown();
  }

  @Test
  public void testSuccess() throws InterruptedException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.
    final Holder<DoneEvent<String>> eventHolder = new Holder<>();

    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        protocol.add("1");
        return "result";
      }
    }, 1, TimeUnit.SECONDS); // delayed

    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);
    future.whenDone(new IDoneCallback<String>() {

      @Override
      public void onDone(DoneEvent<String> event) {
        protocol.add("2");
        if (future.isDone()) {
          protocol.add("done");
        }
        if (future.isCancelled()) {
          protocol.add("cancelled");
        }
        eventHolder.setValue(event);
        verifyLatch.countDown();
      }
    });
    assertTrue(verifyLatch.await());

    assertEquals(CollectionUtility.arrayList("1", "2", "done"), protocol);
    assertNull(eventHolder.getValue().getError());
    assertEquals("result", eventHolder.getValue().getResult());
    assertFalse(eventHolder.getValue().isCancelled());
    assertFalse(eventHolder.getValue().isFailed());
  }

  @Test
  public void testSuccessWithJobAlreadyCompleted() throws InterruptedException, ProcessingException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.
    final Holder<DoneEvent<String>> eventHolder = new Holder<>();

    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        protocol.add("1");
        return "result";
      }
    });
    future.awaitDoneAndGet();

    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);
    future.whenDone(new IDoneCallback<String>() {

      @Override
      public void onDone(DoneEvent<String> event) {
        protocol.add("2");
        if (future.isDone()) {
          protocol.add("done");
        }
        if (future.isCancelled()) {
          protocol.add("cancelled");
        }
        eventHolder.setValue(event);
        verifyLatch.countDown();
      }
    });
    assertTrue(verifyLatch.await());

    assertEquals(CollectionUtility.arrayList("1", "2", "done"), protocol);
    assertNull(eventHolder.getValue().getError());
    assertEquals("result", eventHolder.getValue().getResult());
    assertFalse(eventHolder.getValue().isCancelled());
    assertFalse(eventHolder.getValue().isFailed());
  }

  @Test
  public void testError() throws InterruptedException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.
    final Holder<DoneEvent<String>> eventHolder = new Holder<>();
    final ProcessingException pe = new ProcessingException();

    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        protocol.add("1");
        throw pe;
      }
    }, 1, TimeUnit.SECONDS, Jobs.newInput(null).logOnError(false)); // delayed

    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);
    future.whenDone(new IDoneCallback<String>() {

      @Override
      public void onDone(DoneEvent<String> event) {
        protocol.add("2");
        if (future.isDone()) {
          protocol.add("done");
        }
        if (future.isCancelled()) {
          protocol.add("cancelled");
        }
        eventHolder.setValue(event);
        verifyLatch.countDown();
      }
    });
    assertTrue(verifyLatch.await());

    assertEquals(CollectionUtility.arrayList("1", "2", "done"), protocol);
    assertSame(pe, eventHolder.getValue().getError());
    assertNull(eventHolder.getValue().getResult());
    assertFalse(eventHolder.getValue().isCancelled());
    assertTrue(eventHolder.getValue().isFailed());
  }

  @Test
  public void testErrorWithJobAlreadyCompleted() throws InterruptedException, ProcessingException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.
    final Holder<DoneEvent<String>> eventHolder = new Holder<>();
    final Exception error = new ProcessingException();

    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        protocol.add("1");
        throw error;
      }
    }, Jobs.newInput(null).logOnError(false));
    try {
      future.awaitDoneAndGet();
      fail("exception expected");
    }
    catch (ProcessingException e) {
      final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);
      future.whenDone(new IDoneCallback<String>() {

        @Override
        public void onDone(DoneEvent<String> event) {
          protocol.add("2");
          if (future.isDone()) {
            protocol.add("done");
          }
          if (future.isCancelled()) {
            protocol.add("cancelled");
          }
          eventHolder.setValue(event);
          verifyLatch.countDown();
        }
      });

      assertTrue(verifyLatch.await());
      assertEquals(CollectionUtility.arrayList("1", "2", "done"), protocol);
      assertSame(error, eventHolder.getValue().getError());
      assertNull(eventHolder.getValue().getResult());
      assertFalse(eventHolder.getValue().isCancelled());
      assertTrue(eventHolder.getValue().isFailed());
    }
  }

  @Test
  public void testCancel() throws InterruptedException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.
    final Holder<DoneEvent<String>> eventHolder = new Holder<>();

    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        protocol.add("1");
        return "result";
      }
    }, 1, TimeUnit.SECONDS); // delayed

    future.cancel(true);

    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);
    future.whenDone(new IDoneCallback<String>() {

      @Override
      public void onDone(DoneEvent<String> event) {
        protocol.add("2");
        if (future.isDone()) {
          protocol.add("done");
        }
        if (future.isCancelled()) {
          protocol.add("cancelled");
        }
        eventHolder.setValue(event);
        verifyLatch.countDown();
      }
    });
    assertTrue(verifyLatch.await());

    assertEquals(CollectionUtility.arrayList("2", "done", "cancelled"), protocol);
    assertNull(eventHolder.getValue().getError());
    assertNull(eventHolder.getValue().getResult());
    assertTrue(eventHolder.getValue().isCancelled());
    assertFalse(eventHolder.getValue().isFailed());
  }

  /**
   * Tests that 'Future.whenDone' returns once the Future is cancelled, even if that job is still runnning.
   */
  @Test
  public void testCancelButStillRunning() throws InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch continueRunningLatch = new BlockingCountDownLatch(1);

    final IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          continueRunningLatch.countDownAndBlock(); // continue running
        }
      }
    });

    assertTrue(setupLatch.await());

    // run the test.
    future.cancel(true);

    // verify that whenDone immediately returns even if not completed yet.
    final AtomicBoolean onDone = new AtomicBoolean(false);

    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);
    future.whenDone(new IDoneCallback<Void>() {

      @Override
      public void onDone(DoneEvent<Void> event) {
        onDone.set(true);
        verifyLatch.countDown();
      }
    });
    assertTrue(verifyLatch.await());

    assertTrue(onDone.get());
    continueRunningLatch.release();
  }

  @Test
  public void testCancelWithJobAlreadyCompleted() throws InterruptedException, ProcessingException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.
    final Holder<DoneEvent<String>> eventHolder = new Holder<>();

    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        protocol.add("1");
        return "result";
      }
    });
    future.awaitDoneAndGet();
    future.cancel(true);

    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);
    future.whenDone(new IDoneCallback<String>() {

      @Override
      public void onDone(DoneEvent<String> event) {
        protocol.add("2");
        if (future.isDone()) {
          protocol.add("done");
        }
        if (future.isCancelled()) {
          protocol.add("cancelled");
        }
        eventHolder.setValue(event);
        verifyLatch.countDown();
      }
    });
    verifyLatch.await();

    assertEquals(CollectionUtility.arrayList("1", "2", "done"), protocol);
    assertNull(eventHolder.getValue().getError());
    assertEquals("result", eventHolder.getValue().getResult());
    assertFalse(eventHolder.getValue().isCancelled());
    assertFalse(eventHolder.getValue().isFailed());
  }
}
