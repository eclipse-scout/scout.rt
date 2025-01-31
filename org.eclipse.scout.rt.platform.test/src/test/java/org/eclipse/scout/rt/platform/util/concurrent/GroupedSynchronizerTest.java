/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.concurrent;

import static org.eclipse.scout.rt.platform.util.SleepUtil.sleepSafe;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class GroupedSynchronizerTest {

  @Test
  public void testSameGroupIsBlocked() throws InterruptedException {
    GroupedSynchronizer<String, String> lck = new GroupedSynchronizer<>();
    String groupKey = "group";
    String result = "finish";
    CountDownLatch task1Started = new CountDownLatch(1);
    CountDownLatch finish = new CountDownLatch(1);

    IFuture<Void> task1 = Jobs.schedule(() -> lck.runInGroupLock(groupKey, () -> signalFirstAwaitSecond(task1Started, finish), Function.identity()), Jobs.newInput());
    task1Started.await(1, TimeUnit.MINUTES);

    IFuture<String> task2 = Jobs.schedule(() -> lck.applyInGroupLock(groupKey, obj -> result, Function.identity()), Jobs.newInput());
    try {
      task2.awaitDone(1, TimeUnit.SECONDS);
      fail("Task2 completed while Task1 is running");
    }
    catch (TimedOutError e) {
      assertNotNull(e);
    }
    finish.countDown(); // let the first task finish
    task1.awaitDone(1, TimeUnit.MINUTES);
    assertEquals(result, task2.awaitDoneAndGet(1, TimeUnit.MINUTES));
    assertEquals(0, lck.numLockedRootLocks());
  }

  @Test
  public void testDifferentGroupsCanRunInParallel() throws InterruptedException {
    GroupedSynchronizer<String, String> lck = new GroupedSynchronizer<>();
    String result = "finish";
    CountDownLatch task1Started = new CountDownLatch(1);
    CountDownLatch task2Finished = new CountDownLatch(1);

    IFuture<Void> task1 = Jobs.schedule(() -> lck.runInGroupLock("group1", () -> signalFirstAwaitSecond(task1Started, task2Finished), Function.identity()), Jobs.newInput());
    task1Started.await(1, TimeUnit.MINUTES);

    IFuture<String> task2 = Jobs.schedule(() -> lck.applyInGroupLock("group2", obj -> result, Function.identity()), Jobs.newInput());
    assertEquals(result, task2.awaitDoneAndGet(1, TimeUnit.MINUTES));
    task2Finished.countDown();
    task1.awaitDone(1, TimeUnit.MINUTES);
    assertEquals(2, lck.size());
  }

  /**
   * Tests that the locks are released if there occurs an exception in the group factory.<br>
   */
  @Test
  public void testNumLockedRootGroups() {
    GroupedSynchronizer<String, String> lck = new GroupedSynchronizer<>();
    Runnable runner = spy(Runnable.class);
    RuntimeException expectedException = new RuntimeException();
    try {
      lck.runInGroupLock("key", runner, key -> {
        assertEquals(0, lck.numLockedRootLocks()); // no write lock required for group acquisition
        throw expectedException;
      });
    }
    catch (RuntimeException e) {
      assertSame(expectedException, e);
    }
    assertEquals(0, lck.numLockedRootLocks());
    verify(runner, times(0)).run();
  }

  @Test
  public void testWithExceptionInTask() {
    GroupedSynchronizer<String, String> lck = new GroupedSynchronizer<>();
    RuntimeException expectedException = new RuntimeException();
    String key = "key";
    String returnVal = "ret";
    try {
      lck.runInGroupLock(key, () -> {
        assertEquals(0, lck.numLockedRootLocks());
        throw expectedException;
      }, Function.identity());
    }
    catch (RuntimeException e) {
      assertSame(expectedException, e);
    }

    // lock is available again:
    assertSame(returnVal, lck.applyInGroupLock(key, lock -> returnVal, Function.identity()));
  }

  @Test
  public void testRemoveWhileExecutingTask() throws InterruptedException {
    GroupedSynchronizer<String, String> lck = new GroupedSynchronizer<>();
    String groupKey = "group";
    CountDownLatch task1Started = new CountDownLatch(1);
    CountDownLatch finish = new CountDownLatch(1);

    IFuture<Void> task1 = Jobs.schedule(() -> lck.runInGroupLock(groupKey, () -> signalFirstAwaitSecond(task1Started, finish), Function.identity()), Jobs.newInput());
    task1Started.await(1, TimeUnit.MINUTES);

    IFuture<String> task2 = Jobs.schedule(() -> lck.remove(groupKey), Jobs.newInput());
    try {
      task2.awaitDone(1, TimeUnit.SECONDS);
      fail("Task2 completed while Task1 is running");
    }
    catch (TimedOutError e) {
      assertNotNull(e);
    }
    finish.countDown(); // let the first task finish
    task1.awaitDone(1, TimeUnit.MINUTES);
    assertEquals(groupKey, task2.awaitDoneAndGet(1, TimeUnit.MINUTES));
    assertEquals(0, lck.size());
    assertEquals(0, lck.numLockedRootLocks());
  }

  @Test
  public void testRemoveWhileAcquiringRootLock() throws InterruptedException {

    HashObj keyObj = new HashObj(1);
    Object valObj = new Object();
    GroupedSynchronizer<HashObj, Object> lck = new GroupedSynchronizer<>();
    CountDownLatch task1Started = new CountDownLatch(1);
    CountDownLatch finish = new CountDownLatch(1);

    IFuture<Void> task1 = Jobs.schedule(() -> lck.runInGroupLock(keyObj, () -> signalFirstAwaitSecond(task1Started, finish), key -> {
      signalFirstAwaitSecond(task1Started, finish);
      return valObj;
    }), Jobs.newInput());
    task1Started.await(1, TimeUnit.MINUTES);

    IFuture<Object> task2 = Jobs.schedule(() -> lck.remove(keyObj), Jobs.newInput());
    try {
      task2.awaitDone(1, TimeUnit.SECONDS);
      fail("Task2 completed while Task1 is running");
    }
    catch (TimedOutError e) {
      assertNotNull(e);
    }
    finish.countDown(); // let the first task finish
    task1.awaitDone(1, TimeUnit.MINUTES);
    assertSame(valObj, task2.awaitDoneAndGet(1, TimeUnit.MINUTES));
    assertEquals(0, lck.size());
    assertEquals(0, lck.numLockedRootLocks());
  }

  @Test
  public void testWithExceptionInRemove() {
    GroupedSynchronizer<String, String> lck = new GroupedSynchronizer<>();
    RuntimeException expectedException = new RuntimeException();
    String key = "key";
    lck.runInGroupLock(key, () -> {
    }, Function.identity());

    assertEquals(1, lck.size());
    assertNull(lck.remove("nonExisting"));
    assertEquals(0, lck.numLockedRootLocks());

    try {
      lck.remove("key", obj -> {
        assertEquals(1, lck.numLockedRootLocks());
        throw expectedException;
      });
    }
    catch (RuntimeException e) {
      assertSame(expectedException, e);
    }

    // lock is available again:
    assertEquals(0, lck.numLockedRootLocks());
    assertEquals(0, lck.size()); // remove is done (event there was an exception)
  }

  @Test
  public void testRemoveWithVeto() {
    GroupedSynchronizer<String, String> lck = new GroupedSynchronizer<>();
    String key = "key";
    lck.runInGroupLock(key, () -> {
    }, Function.identity());

    assertEquals(1, lck.size());
    assertEquals(0, lck.numLockedRootLocks());

    lck.remove("key", obj -> false);
    assertEquals(0, lck.numLockedRootLocks());
    assertEquals(1, lck.size());
    assertEquals(1, lck.toMap().size());
  }

  @Test
  public void testSameGlobalLockButDifferentLocalLockDoesNotBlockAcquisitionOfLocal() throws InterruptedException {
    GroupedSynchronizer<HashObj, Object> lck = new GroupedSynchronizer<>();
    HashObj obj1 = new HashObj(0);
    HashObj obj2 = new HashObj(0);
    CountDownLatch task1Started = new CountDownLatch(1);
    CountDownLatch finish = new CountDownLatch(1);

    IFuture<Void> task1 = Jobs.schedule(() -> lck.runInGroupLock(obj1, () -> signalFirstAwaitSecond(task1Started, finish), Function.identity()), Jobs.newInput());
    task1Started.await(1, TimeUnit.MINUTES);

    IFuture<Object> task2 = Jobs.schedule(() -> lck.applyInGroupLock(obj2, obj -> new Object(), Function.identity()), Jobs.newInput());
    task2.awaitDone(1, TimeUnit.MINUTES);

    finish.countDown();
    task1.awaitDone(1, TimeUnit.MINUTES);
    assertEquals(0, lck.numLockedRootLocks());
  }

  @Test
  public void testMultiReadPossible() throws InterruptedException {
    GroupedSynchronizer<HashObj, Object> lck = new GroupedSynchronizer<>();
    HashObj obj1 = new HashObj(0);
    HashObj obj2 = new HashObj(0);
    Object grp1 = new Object();
    Object grp2 = new Object();
    CountDownLatch task1Started = new CountDownLatch(1);
    CountDownLatch finish = new CountDownLatch(1);

    IFuture<Void> task1 = Jobs.schedule(() -> lck.runInGroupLock(obj1, () -> signalFirstAwaitSecond(task1Started, finish), k -> grp1), Jobs.newInput());
    task1Started.await(1, TimeUnit.MINUTES);

    IFuture<Object> task2 = Jobs.schedule(() -> lck.applyInGroupLock(obj1, obj -> new Object(), k -> grp1), Jobs.newInput());
    //TODO: countDown in finally clauses!!

    IFuture<Object> task3 = Jobs.schedule(() -> lck.applyInGroupLock(obj2, obj -> new Object(), k -> grp2), Jobs.newInput());
    try {
      task3.awaitDoneAndGet(4, TimeUnit.SECONDS);
    }
    finally {
      finish.countDown();
      task1.awaitDone(1, TimeUnit.MINUTES);
      task2.awaitDone(1, TimeUnit.MINUTES);
    }
    assertEquals(0, lck.numLockedRootLocks());
  }

  @Test
  public void testWithManyCallers() throws InterruptedException {
    int numRoots = 2;
    int numGroups = numRoots * 5;
    int numClients = numGroups * 100;
    int[] markers = new int[numGroups];
    for (int i = 0; i < markers.length; i++) {
      markers[i] = 0;
    }
    CountDownLatch allReady = new CountDownLatch(numClients);
    GroupedSynchronizer<String, String> lck = new GroupedSynchronizer<>(numRoots);
    CountDownLatch start = new CountDownLatch(1);
    for (int i = 0; i < numClients; i++) {
      final int groupIndex = i % numGroups;
      String groupKey = Integer.toString(groupIndex);
      Jobs.schedule(() -> {
        allReady.countDown();
        start.await(1, TimeUnit.MINUTES);
        lck.acceptInGroupLock(groupKey, val -> {
          assertEquals(0, markers[groupIndex]);
          markers[groupIndex]++;
          sleepSafe(4, TimeUnit.MILLISECONDS);
          assertEquals(1, markers[groupIndex]);
          markers[groupIndex]--;
          assertEquals(0, markers[groupIndex]);
        }, Function.identity());
      }, Jobs.newInput().withExecutionHint(GroupedSynchronizerTest.class.getName(), true));
    }

    allReady.await(1, TimeUnit.MINUTES);
    start.countDown();

    Jobs.getJobManager()
        .getFutures(Jobs.newFutureFilterBuilder()
            .andMatchExecutionHint(GroupedSynchronizerTest.class.getName()).toFilter())
        .stream()
        .forEach(IFuture::awaitDoneAndGet);
  }

  protected void signalFirstAwaitSecond(CountDownLatch first, CountDownLatch second) {
    first.countDown();
    try {
      second.await(1, TimeUnit.MINUTES);
    }
    catch (InterruptedException e) {
      throw new PlatformException("Interrupted", e);
    }
  }

  private static class HashObj {

    private final int m_hash;

    private HashObj(int hash) {
      m_hash = hash;
    }

    @Override
    public int hashCode() {
      return m_hash;
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj;
    }
  }
}
