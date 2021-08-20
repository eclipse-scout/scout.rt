/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util.concurrent;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.concurrent.FixtureDeferredOperationQueue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class DeferredOperationQueueTest {

  private static final String QUEUE_TRANSACTION_MEMBER_ID = "DeferredOperationQueueTest#QueueTransactionMemberId";

  @Test
  public void testCreateInstance() {
    Assert.assertThrows(AssertionException.class, () -> new DeferredOperationQueue<>(null, 10, 0, nop()));
    Assert.assertThrows(AssertionException.class, () -> new DeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, -1, 10, nop()));
    Assert.assertThrows(AssertionException.class, () -> new DeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 0, 10, nop()));
    Assert.assertThrows(AssertionException.class, () -> new DeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 10, -1, nop()));
    Assert.assertThrows(AssertionException.class, () -> new DeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 10, 10, null));

    Consumer<List<Object>> operation = nop();
    DeferredOperationQueue<Object> queue = new DeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 10, 0, operation);
    assertEquals(10, queue.getBatchSize());
    assertEquals(0, queue.getMaxDelayMillis());
    assertSame(operation, queue.getBatchOperation());
  }

  @Test
  public void testAdd() {
    Assert.assertThrows(AssertionException.class, () -> new DeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 1, 1, nop()).add(null));

    // assert adding first element schedules flush job
    FixtureDeferredOperationQueue<String> queue = new FixtureDeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 1, TimeUnit.HOURS.toMillis(10), nop());
    RunContexts.empty().run(() -> queue.add("first element schedules flush job"));
    assertTrue(queue.getAndResetScheduleFlushJobWasInvoked());
    RunContexts.empty().run(() -> queue.add("consecutive elements do not schedule flush job (current one is still 'running')"));
    assertFalse(queue.getAndResetScheduleFlushJobWasInvoked());
  }

  @Test
  public void testAddAll() {
    Assert.assertThrows(AssertionException.class, () -> new DeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 1, 1, nop()).addAll(null));

    // assert adding first element schedules flush job
    FixtureDeferredOperationQueue<String> queue = new FixtureDeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 1, TimeUnit.HOURS.toMillis(10), nop());
    RunContexts.empty().run(() -> queue.addAll(Stream.of("first element schedules flush job")));
    assertTrue(queue.getAndResetScheduleFlushJobWasInvoked());
    RunContexts.empty().run(() -> queue.addAll(Stream.of("consecutive elements do not schedule flush job (current one is still 'running')")));
    assertFalse(queue.getAndResetScheduleFlushJobWasInvoked());
  }

  @Test
  public void testAddOnCommit() {
    // adding element has no effect if transaction not committed
    List<String> batch = new ArrayList<>();
    FixtureDeferredOperationQueue<String> queue = new FixtureDeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 1, TimeUnit.HOURS.toMillis(10), batch::addAll);
    RunContexts.empty().run(() -> queue.add("A"));
    RunContexts.empty().run(() -> {
      queue.add("B");
      queue.flushDeferred(true);
      assertEquals(asList("A"), batch);
    });
    try {
      RunContexts.empty().run(() -> {
        queue.add("C");
        throw new ProcessingException("rollback");
      });
    }
    catch (ProcessingException e) {
      // expected and ignored
    }
    queue.flushDeferred(true);
    assertEquals(asList("A", "B"), batch);
  }

  @Test
  public void testAddAllOnCommit() {
    // adding element has no effect if transaction not committed
    List<String> batch = new ArrayList<>();
    FixtureDeferredOperationQueue<String> queue = new FixtureDeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 1, TimeUnit.HOURS.toMillis(10), batch::addAll);
    RunContexts.empty().run(() -> queue.add("A"));
    RunContexts.empty().run(() -> {
      queue.addAll(Stream.of("B", "C"));
      queue.flushDeferred(true);
      assertEquals(asList("A"), batch);
    });
    try {
      RunContexts.empty().run(() -> {
        queue.add("C");
        throw new ProcessingException("rollback");
      });
    }
    catch (ProcessingException e) {
      // expected and ignored
    }
    queue.flushDeferred(true);
    queue.flushDeferred(true); // batch size 1
    assertEquals(asList("A", "B", "C"), batch);
  }

  @Test
  public void testConsumerException() {
    @SuppressWarnings("unchecked")
    Consumer<List<String>> consumer = mock(Consumer.class);
    doThrow(new RuntimeException("Test Exception")).when(consumer).accept(any());

    DeferredOperationQueue<String> queue = new DeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 1, TimeUnit.MILLISECONDS.toMillis(1), consumer);
    RunContexts.empty().run(() -> queue.add("first element"));
    SleepUtil.sleepSafe(100, TimeUnit.MILLISECONDS);
    RunContexts.empty().run(() -> queue.add("second element"));
    SleepUtil.sleepSafe(100, TimeUnit.MILLISECONDS);

    // assert that queue keeps calling the batch consumer even in the event of exceptions
    verify(consumer, times(2)).accept(any());
  }

  @Test(timeout = 1000)
  public void testFlushDeferredQueueHasBatchSizeElements() {
    List<String> batch = new ArrayList<>();
    FixtureDeferredOperationQueue<String> queue = new FixtureDeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 2, TimeUnit.HOURS.toMillis(10), batch::addAll);
    RunContexts.empty().run(() -> {
      queue.add("first");
      queue.add("second");
    });
    queue.flushDeferred(true);
    assertEquals(asList("first", "second"), batch);
  }

  @Test(timeout = 1000)
  public void testFlushDeferredQueueHasBatchSizeElementsUsingAddAll() {
    List<String> batch = new ArrayList<>();
    FixtureDeferredOperationQueue<String> queue = new FixtureDeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 2, TimeUnit.HOURS.toMillis(10), batch::addAll);
    RunContexts.empty().run(() -> queue.addAll(Stream.of("first", "second")));
    queue.flushDeferred(true);
    assertEquals(asList("first", "second"), batch);
  }

  @Test(timeout = 1000)
  public void testFlushDeferredQueueHasBatchSizeElementsUsingAddAndAddAll() {
    List<String> batch = new ArrayList<>();
    FixtureDeferredOperationQueue<String> queue = new FixtureDeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 2, TimeUnit.HOURS.toMillis(10), batch::addAll);
    RunContexts.empty().run(() -> {
      queue.add("first");
      queue.addAll(Stream.of("second"));
    });
    queue.flushDeferred(true);
    assertEquals(asList("first", "second"), batch);
  }

  @Test(timeout = 1000)
  public void testFlushDeferredQueueHasLessThanBatchSizeElements() {
    List<String> batch = new ArrayList<>();
    FixtureDeferredOperationQueue<String> queue = new FixtureDeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 2, 100, batch::addAll);
    RunContexts.empty().run(() -> queue.add("single"));
    queue.flushDeferred(true);
    assertEquals(asList("single"), batch);
  }

  @Test(timeout = 1000)
  public void testFlushDeferredQueueHasNoElements() {
    List<String> batch = new ArrayList<>();
    FixtureDeferredOperationQueue<String> queue = new FixtureDeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 2, 100, batch::addAll);
    queue.flushDeferred(true);
    assertEquals(emptyList(), batch);
  }

  @Test(timeout = 3000)
  public void testFlushDeferredQueueElementsAreDroppingIn() throws Exception {
    List<String> batch = new ArrayList<>();
    FixtureDeferredOperationQueue<String> queue = new FixtureDeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 3, TimeUnit.HOURS.toMillis(10), batch::addAll);
    RunContexts.empty().run(() -> queue.add("first"));

    CountDownLatch flushIsRunning = new CountDownLatch(1);
    IFuture<Void> future = Jobs.schedule(() -> {
      flushIsRunning.countDown();
      queue.flushDeferred(true);
    }, Jobs.newInput());

    flushIsRunning.await();
    SleepUtil.sleepSafe(100, TimeUnit.MILLISECONDS);
    assertFalse(future.isDone());

    RunContexts.empty().run(() -> queue.add("second"));
    SleepUtil.sleepSafe(100, TimeUnit.MILLISECONDS);
    assertFalse(future.isDone());

    RunContexts.empty().run(() -> queue.add("third"));
    future.awaitDone();
    assertEquals(asList("first", "second", "third"), batch);
  }

  @Test
  public void testFlushEmptyWithoutFlushJobRunning() {
    List<String> batch = new ArrayList<>();
    FixtureDeferredOperationQueue<String> queue = new FixtureDeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 2, TimeUnit.HOURS.toMillis(10), batch::addAll);
    queue.flush();
    assertEquals(emptyList(), batch);
  }

  @Test
  public void testFlushEmptyWithFlushJobRunning() {
    List<String> batch = new ArrayList<>();
    DeferredOperationQueue<String> queue = new DeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 2, TimeUnit.HOURS.toMillis(10), batch::addAll);

    RunContexts.empty().run(() -> {
      queue.add("first");
      queue.add("second");
    });
    // give flush job some time to flush the queue
    SleepUtil.sleepSafe(100, TimeUnit.MILLISECONDS);

    assertEquals(asList("first", "second"), batch);

    queue.flush();
    assertEquals(asList("first", "second"), batch);
  }

  @Test
  public void testFlushPendingElements() {
    List<String> batch = new ArrayList<>();
    DeferredOperationQueue<String> queue = new DeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 2, TimeUnit.HOURS.toMillis(10), batch::addAll);

    RunContexts.empty().run(() -> queue.add("first"));
    // give flush job some time to flush the queue
    SleepUtil.sleepSafe(100, TimeUnit.MILLISECONDS);

    assertEquals(emptyList(), batch);

    queue.flush();
    assertEquals(asList("first"), batch);
  }

  /**
   * This test tries to cover the different branches of {@link DeferredOperationQueue#flushDeferred(boolean)}
   * <p>
   * <b>Note:</b> this test is based on pure random and it is not guaranteed that the different execution cases are
   * tested. Code coverage can show whether all paths have been covered.
   */
  @Test
  public void testAddAndFlushWithRandom() {
    List<String> batch = new ArrayList<>();
    int timeoutMillis = 5;
    DeferredOperationQueue<String> queue = new DeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 3, timeoutMillis, batch::addAll);
    final int elementCount = queue.getBatchSize() * 150;

    Random rnd = new SecureRandom();
    for (int i = 0; i < elementCount; i++) {
      final int ii = i;
      RunContexts.empty().run(() -> queue.add("e" + ii));
      if (rnd.nextBoolean()) {
        SleepUtil.sleepSafe(rnd.nextInt(10), TimeUnit.MILLISECONDS);
      }
    }

    SleepUtil.sleepSafe(3 * timeoutMillis, TimeUnit.MILLISECONDS);

    assertEquals(
        IntStream.range(0, elementCount)
            .mapToObj(i -> "e" + i)
            .collect(Collectors.toList()),
        batch);
  }

  protected static <T> Consumer<T> nop() {
    return x -> {};
  }
}
