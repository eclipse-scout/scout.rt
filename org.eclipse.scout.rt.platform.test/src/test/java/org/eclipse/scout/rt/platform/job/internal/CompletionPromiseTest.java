/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.internal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.Times;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class CompletionPromiseTest {

  private ExecutorService m_executor;

  @Before
  public void before() {
    m_executor = Executors.newSingleThreadExecutor();
  }

  @After
  public void after() {
    m_executor.shutdown();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test() throws InterruptedException, ExecutionException, TimeoutException {
    final List<String> protocol = new ArrayList<>();
    final Thread callingThread = Thread.currentThread();

    // Latch to wait until ready
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(3);

    // Latch to wait until done-handling is done
    final BlockingCountDownLatch doneLatch = new BlockingCountDownLatch(3);

    final JobFutureTask<String> future = mock(JobFutureTask.class);
    when(future.isDone()).thenReturn(false);

    final CompletionPromise<String> promise = new CompletionPromise<>(future, m_executor);

    // Schedule job-1
    Jobs.schedule(() -> {
      protocol.add("1");
      setupLatch.countDownAndBlock();

      // Simulate the Future to be in done state
      when(future.isDone()).thenReturn(true);

      // Run the test
      promise.done();
    }, Jobs.newInput().withName("job-1"));

    // Schedule job-2
    Jobs.schedule(() -> {
      setupLatch.countDown();
      promise.awaitDoneAndGet();
      doneLatch.countDown();
    }, Jobs.newInput().withName("job-2"));

    // Schedule job-3
    Jobs.schedule(() -> {
      setupLatch.countDown();
      promise.awaitDoneAndGet(10, TimeUnit.SECONDS);
      doneLatch.countDown();
    }, Jobs.newInput().withName("job-3"));

    // Wait until all jobs are running
    assertTrue(setupLatch.await());

    // Asynchronous callback invocation
    promise.whenDone(event -> {
      protocol.add("3");
      if (callingThread != Thread.currentThread()) {
        protocol.add("4");
      }
      doneLatch.countDown();
    }, RunContexts.copyCurrent());

    protocol.add("2");

    // Let all jobs finish
    setupLatch.unblock();

    // Wait until done-handling is done
    assertTrue(doneLatch.await());

    // Synchronous callback invocation
    promise.whenDone(event -> {
      protocol.add("5");
      if (callingThread == Thread.currentThread()) {
        protocol.add("6");
      }
      doneLatch.countDown();
    }, RunContexts.copyCurrent());
    protocol.add("7");

    // Any get-call should not block, because Future is in done-state.
    promise.awaitDoneAndGet(10, TimeUnit.SECONDS);
    promise.awaitDoneAndGet();

    assertEquals(Arrays.asList("1", "2", "3", "4", "5", "6", "7"), protocol);
  }

  @Test
  @Times(1000) // regression; do not remove
  public void testAwaitDone1() {
    IFuture<Void> future = Jobs.schedule(mock(IRunnable.class), Jobs.newInput());

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 10, TimeUnit.SECONDS);
    assertTrue(future.isDone()); // ensure to be 'done' after being released
  }

  @Test
  @Times(1000) // regression; do not remove
  public void testAwaitDone2() {
    IFuture<Void> future = Jobs.schedule(mock(IRunnable.class), Jobs.newInput());

    future.awaitDone();
    assertTrue(future.isDone()); // ensure to be 'done' after being released
  }
}
