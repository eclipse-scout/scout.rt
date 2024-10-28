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
import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
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
    testFlushDeferredQueue(2, TimeUnit.HOURS.toMillis(10), false, queue -> {
      queue.add("A");
      queue.add("B");
    }, List.of(List.of("A", "B")));
  }

  @Test(timeout = 1000)
  public void testFlushDeferredQueueHasBatchSizeElementsSingleRun() {
    testFlushDeferredQueue(2, TimeUnit.HOURS.toMillis(10), true, queue -> {
      queue.add("A");
      queue.add("B");
    }, List.of(List.of("A", "B")));
  }

  @Test(timeout = 1000)
  public void testFlushDeferredQueueHasBatchSizeElementsUsingAddAll() {
    testFlushDeferredQueue(2, TimeUnit.HOURS.toMillis(10), false, queue -> queue.addAll(Stream.of("A", "B")), List.of(List.of("A", "B")));
  }

  @Test(timeout = 1000)
  public void testFlushDeferredQueueHasBatchSizeElementsUsingAddAllSingleRun() {
    testFlushDeferredQueue(2, TimeUnit.HOURS.toMillis(10), true, queue -> queue.addAll(Stream.of("A", "B")), List.of(List.of("A", "B")));
  }

  @Test(timeout = 1000)
  public void testFlushDeferredQueueHasBatchSizeElementsUsingAddAndAddAll() {
    testFlushDeferredQueue(2, TimeUnit.HOURS.toMillis(10), false, queue -> {
      queue.add("A");
      queue.addAll(Stream.of("B"));
    }, List.of(List.of("A", "B")));
  }

  @Test(timeout = 1000)
  public void testFlushDeferredQueueHasBatchSizeElementsUsingAddAndAddAllSingleRun() {
    testFlushDeferredQueue(2, TimeUnit.HOURS.toMillis(10), true, queue -> {
      queue.add("A");
      queue.addAll(Stream.of("B"));
    }, List.of(List.of("A", "B")));
  }

  @Test(timeout = 750)
  public void testFlushDeferredQueueHasLessThanBatchSizeElements() {
    testFlushDeferredQueue(2, 500, false, (queue) -> queue.add("A"), List.of(List.of("A")));
  }

  @Test(timeout = 750)
  public void testFlushDeferredQueueHasLessThanBatchSizeElementsSingleRun() {
    testFlushDeferredQueue(2, 500, true, (queue) -> queue.add("A"), List.of(List.of("A")));
  }

  @Test(timeout = 750)
  public void testFlushDeferredQueueHasMoreThanBatchSizeElements() {
    testFlushDeferredQueue(2, 500, false, (queue) -> queue.addAll(Stream.of("A", "B", "C", "D", "E")), List.of(List.of("A", "B"), List.of("C", "D"), List.of("E")));
  }

  @Test(timeout = 1000)
  public void testFlushDeferredQueueHasMoreThanBatchSizeElementsSingleRun() {
    testFlushDeferredQueue(2, TimeUnit.HOURS.toMillis(10), true, (queue) -> queue.addAll(Stream.of("A", "B", "C", "D", "E")), List.of(List.of("A", "B")));
  }

  @Test(timeout = 1000)
  public void testFlushDeferredQueueHasNoElements() {
    testFlushDeferredQueue(2, TimeUnit.HOURS.toMillis(10), true, nop(), List.of());
  }

  @Test(timeout = 1000)
  public void testFlushDeferredQueueHasNoElementsSingleRun() {
    testFlushDeferredQueue(2, TimeUnit.HOURS.toMillis(10), true, nop(), List.of());
  }

  private <T> void testFlushDeferredQueue(int batchSize, long maxDelayMillis, boolean singleRun, Consumer<DeferredOperationQueue<T>> addElements, List<List<T>> expectedBatches) {
    FixtureDeferredOperationQueue<T> queue = new FixtureDeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, batchSize, maxDelayMillis, b -> {});
    RunContexts.empty().run(() -> addElements.accept(queue));
    queue.flushDeferred(singleRun);
    assertEquals(expectedBatches, queue.getExecutedBatches());
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

  @Test(timeout = 5000)
  public void testScheduleFlushJobThrowsException() throws Exception {
    List<String> batch = new ArrayList<>();
    AtomicBoolean throwException = new AtomicBoolean(true);
    CountDownLatch flushedLatch = new CountDownLatch(1);
    DeferredOperationQueue<String> queue = new DeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 2, TimeUnit.HOURS.toMillis(10), batch::addAll) {
      @Override
      protected void scheduleFlushJob() {
        if (throwException.get()) {
          throw new RuntimeException("expected exception");
        }
        super.scheduleFlushJob();
      }

      @Override
      protected void flushDeferred(boolean singleRun) {
        super.flushDeferred(true);
        flushedLatch.countDown();
      }
    };

    RunContexts.empty().run(() -> queue.add("first element"));
    assertEquals(Collections.emptyList(), batch);

    throwException.set(false);
    RunContexts.empty().run(() -> queue.add("second element"));

    assertTrue(flushedLatch.await(2, TimeUnit.SECONDS));
    assertEquals(asList("first element", "second element"), batch);
  }

  @Test
  public void testTransactionalBatchOperation() throws Exception {
    List<List<String>> batches = new ArrayList<>();
    CountDownLatch flushedLatch = new CountDownLatch(1);

    DeferredOperationQueue<String> queue = new DeferredOperationQueue<>(QUEUE_TRANSACTION_MEMBER_ID, 2, 1000, batch -> getOrCreateTransactionMember(batches).addAll(batch)) {
      @Override
      protected void flushDeferred(boolean singleRun) {
        super.flushDeferred(singleRun);
        flushedLatch.countDown();
      }
    };

    RunContexts.empty().run(() -> queue.addAll(Stream.of("A", "B", "C", "D", "E", "F")));

    assertTrue(flushedLatch.await(1, TimeUnit.SECONDS));
    assertEquals(List.of(
        List.of("A", "B"),
        List.of("C", "D"),
        List.of("E", "F")), batches);
  }

  protected P_FixtureTransactionMember getOrCreateTransactionMember(List<List<String>> batches) {
    ITransaction transaction = assertNotNull(ITransaction.CURRENT.get(), "Not running within a transaction");
    P_FixtureTransactionMember transactionMember = (P_FixtureTransactionMember) transaction.getMember(P_FixtureTransactionMember.ID);
    if (transactionMember == null) {
      transactionMember = new P_FixtureTransactionMember(batches);
      transaction.registerMember(transactionMember);
    }
    return transactionMember;
  }

  protected class P_FixtureTransactionMember extends AbstractTransactionMember {

    public static final String ID = "P_FixtureTransactionMember";

    private final List<List<String>> m_batches;
    private final List<String> m_elements = new ArrayList<>();

    public P_FixtureTransactionMember(List<List<String>> batches) {
      super(ID);
      m_batches = assertNotNull(batches);
    }

    public void add(String element) {
      m_elements.add(element);
    }

    public void addAll(Collection<String> element) {
      m_elements.addAll(element);
    }

    @Override
    public boolean needsCommit() {
      return !m_elements.isEmpty();
    }

    @Override
    public void commitPhase2() {
      m_batches.add(m_elements);
    }
  }

  protected static <T> Consumer<T> nop() {
    return x -> {};
  }
}
