/*
 * Copyright (c) 2010-2024 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.opentelemetry;

import static org.junit.Assert.*;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.filter.future.FutureFilterBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.helpers.MessageFormatter;

public class PeriodicAsyncMeasurementTest {

  private IExecutionSemaphore m_asyncJobExecutionSemaphore;
  private int m_origExecutionSemaphorePermits;

  @Before
  public void before() {
    m_asyncJobExecutionSemaphore = PeriodicAsyncMeasurement.ASYNC_JOB_EXECUTION_SEMAPHORE.get();
    m_origExecutionSemaphorePermits = m_asyncJobExecutionSemaphore.getPermits();
    m_asyncJobExecutionSemaphore.withPermits(1);
  }

  @After
  public void after() {
    m_asyncJobExecutionSemaphore.withPermits(m_origExecutionSemaphorePermits);
  }

  @Test
  public void testSimple() throws Exception {
    assertEquals(0, m_asyncJobExecutionSemaphore.getCompetitorCount());

    final long asyncObservationInterval = 100;
    Callable<Long> callable = mockCallable();
    AtomicLong value = new AtomicLong(10);
    Mockito.when(callable.call()).then(invocation -> {
      Thread.sleep(3); // simulate "db access"
      return value.getAndAdd(5);
    });
    try (PeriodicAsyncMeasurement<Long> asyncMeasurement = new PeriodicAsyncMeasurement<>("testSimple", callable, () -> RunContexts.empty(), () -> Boolean.TRUE, asyncObservationInterval)) {

      // first observation --> no measurement, but trigger async job
      assertNull(asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 1);
      assertEquals(15, value.get());

      // second observation --> first measurement, but no trigger async job
      assertEquals(Long.valueOf(10L), asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 1);

      // third observation --> second measurement, still no trigger async job
      assertEquals(Long.valueOf(10L), asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 1);

      awaitNextAsyncObservationInterval(asyncObservationInterval);

      // forth observation --> third measurement, trigger async job
      assertEquals(Long.valueOf(10L), asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 2);
      assertEquals(20, value.get());

      // fifth observation --> forth measurement, no trigger async job
      assertEquals(Long.valueOf(15L), asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 2);
    }
  }

  @Test
  public void testLongRunningAsyncJob() throws Exception {
    assertEquals(0, m_asyncJobExecutionSemaphore.getCompetitorCount());

    final long asyncObservationInterval = 100;
    final String asyncJobName = "testLongRunningAsyncJob";
    Callable<Long> callable = mockCallable();
    Mockito.when(callable.call()).then(invocation -> {
      Thread.sleep(TimeUnit.SECONDS.toMillis(3));
      return 2L;
    });
    try (PeriodicAsyncMeasurement<Long> asyncMeasurement = new PeriodicAsyncMeasurement<>(asyncJobName, callable, () -> RunContexts.empty(), () -> Boolean.TRUE, asyncObservationInterval)) {

      // first observation --> no measurement, but trigger async job
      assertNull(asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 1);

      // second observation --> no measurement, but no trigger async job
      assertNull(asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 1);

      // third observation --> no measurement, still no trigger async job
      assertNull(asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 1);

      awaitNextAsyncObservationInterval(asyncObservationInterval);

      Set<IFuture<?>> runningAsyncJobs = getRunningAsyncJobs();
      assertEquals(1, runningAsyncJobs.size());
      IFuture<?> firstAsyncJob = runningAsyncJobs.iterator().next();

      // forth observation --> no measurement, trigger async job
      assertNull(asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 2);
      runningAsyncJobs = getRunningAsyncJobs();
      assertEquals(1, runningAsyncJobs.size());
      assertNotSame(firstAsyncJob, runningAsyncJobs.iterator().next());

      // fifth observation --> no measurement, no trigger async job
      assertNull(asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 2);

      runningAsyncJobs.iterator().next().cancel(true);
      awaitAsyncJobFinished(asyncJobName);
    }
  }

  @Test
  public void testFailingAsyncJob() throws Exception {
    assertEquals(0, m_asyncJobExecutionSemaphore.getCompetitorCount());

    final long asyncObservationInterval = 100;
    Callable<Long> callable = mockCallable();
    Mockito.when(callable.call()).thenThrow(RuntimeException.class);
    try (PeriodicAsyncMeasurement<Long> asyncMeasurement = new PeriodicAsyncMeasurement<>("testFailingAsyncJob", callable, () -> RunContexts.empty(), () -> Boolean.TRUE, asyncObservationInterval)) {

      // first observation --> no measurement, but trigger async job
      assertNull(asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 1);

      // second observation --> no measurement, but no trigger async job
      assertNull(asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 1);

      // third observation --> no measurement, still no trigger async job
      assertNull(asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 1);

      awaitNextAsyncObservationInterval(asyncObservationInterval);

      // forth observation --> no measurement, trigger async job
      assertNull(asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 2);
    }
  }

  @Test
  public void testLimitParallelExecution() throws Exception {
    assertEquals(0, m_asyncJobExecutionSemaphore.getCompetitorCount());
    assertEquals(1, m_asyncJobExecutionSemaphore.getPermits());

    final long asyncObservationInterval = 200;
    BlockingAsyncObservableLongMeasurementMock blockingMock1 = new BlockingAsyncObservableLongMeasurementMock("testLimitParallelExecution1", 1, asyncObservationInterval);
    BlockingAsyncObservableLongMeasurementMock blockingMock2 = new BlockingAsyncObservableLongMeasurementMock("testLimitParallelExecution2", 2, asyncObservationInterval);

    // first observation --> no measurement, but trigger async jobs
    assertNull(blockingMock1.runObservation());
    assertNull(blockingMock2.runObservation());
    assertEquals(2, getRunningAsyncJobs().size());
    assertEquals(2, m_asyncJobExecutionSemaphore.getCompetitorCount());
    assertAsyncJobTrigger(blockingMock1.m_callable, 1);
    assertAsyncJobTrigger(blockingMock2.m_callable, 0);

    blockingMock1.unblockAndAwaitFinished();
    assertEquals(1, getRunningAsyncJobs().size());
    assertEquals(1, m_asyncJobExecutionSemaphore.getCompetitorCount());
    assertAsyncJobTrigger(blockingMock1.m_callable, 1);
    assertAsyncJobTrigger(blockingMock2.m_callable, 1);

    // second observation --> first measurement1, but no trigger async jobs
    assertEquals(Long.valueOf(1L), blockingMock1.runObservation());
    assertNull(blockingMock2.runObservation());
    assertEquals(1, getRunningAsyncJobs().size());
    assertEquals(1, m_asyncJobExecutionSemaphore.getCompetitorCount());
    assertAsyncJobTrigger(blockingMock1.m_callable, 1);
    assertAsyncJobTrigger(blockingMock2.m_callable, 1);

    blockingMock2.unblockAndAwaitFinished();
    assertEquals(0, getRunningAsyncJobs().size());
    assertEquals(0, m_asyncJobExecutionSemaphore.getCompetitorCount());
    assertAsyncJobTrigger(blockingMock1.m_callable, 1);
    assertAsyncJobTrigger(blockingMock2.m_callable, 1);

    awaitNextAsyncObservationInterval(asyncObservationInterval);

    // third observation --> second measurement1, first measurement2, trigger async jobs
    assertEquals(Long.valueOf(1L), blockingMock1.runObservation());
    assertEquals(Long.valueOf(2L), blockingMock2.runObservation());
    awaitAsyncJobFinished(blockingMock1.m_name);
    awaitAsyncJobFinished(blockingMock2.m_name);
    assertEquals(0, getRunningAsyncJobs().size());
    assertEquals(0, m_asyncJobExecutionSemaphore.getCompetitorCount());
    assertAsyncJobTrigger(blockingMock1.m_callable, 2);
    assertAsyncJobTrigger(blockingMock2.m_callable, 2);
  }

  @Test
  public void testActiveOnThisNode() throws Exception {
    assertEquals(0, m_asyncJobExecutionSemaphore.getCompetitorCount());

    final long asyncObservationInterval = 100;
    Callable<Long> callable = mockCallable();
    Mockito.when(callable.call()).thenReturn(111L);
    AtomicBoolean activeOnThisNode = new AtomicBoolean(false);
    try (PeriodicAsyncMeasurement<Long> asyncMeasurement = new PeriodicAsyncMeasurement<>("testActiveOnThisNode", callable, () -> RunContexts.empty(), () -> activeOnThisNode.get(), asyncObservationInterval)) {

      // first observation --> no measurement and no trigger async job
      assertNull(asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 0);

      // second observation --> no measurement, but no trigger async job
      assertNull(asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 0);

      awaitNextAsyncObservationInterval(asyncObservationInterval);

      // third observation --> no measurement and no trigger async job
      assertNull(asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 0);

      activeOnThisNode.set(Boolean.TRUE);

      // forth observation --> no measurement, but trigger async job
      assertNull(asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 1);

      Thread.sleep(20);
      // fifth observation --> first measurement, but no trigger async job
      assertEquals(Long.valueOf(111L), asyncMeasurement.getAndNext());
      assertAsyncJobTrigger(callable, 1);
    }
  }

  private Set<IFuture<?>> getRunningAsyncJobs() {
    return BEANS.get(IJobManager.class).getFutures(newAsyncJobFilter().toFilter());
  }

  private void awaitAsyncJobFinished(String name) {
    BEANS.get(IJobManager.class).awaitFinished(newAsyncJobFilter()
        .andMatchName(MessageFormatter.arrayFormat(PeriodicAsyncMeasurement.ASYNC_JOB_NAME_PATTERN, new String[]{name}).getMessage())
        .toFilter(), 1, TimeUnit.SECONDS);
  }

  private FutureFilterBuilder newAsyncJobFilter() {
    return Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(PeriodicAsyncMeasurement.ASYNC_JOB_EXECUTION_HINT);
  }

  private void awaitNextAsyncObservationInterval(long asyncObservationIntervalMillis) throws InterruptedException {
    Thread.sleep(asyncObservationIntervalMillis + 50);
  }

  private void assertAsyncJobTrigger(Callable<?> callable, int expectedTriggerCount) throws Exception {
    Thread.sleep(20);
    Mockito.verify(callable, Mockito.times(expectedTriggerCount)).call();
  }

  private Callable<Long> mockCallable() {
    //noinspection unchecked
    Callable<Long> callable = Mockito.mock(Callable.class);
    return callable;
  }

  private class BlockingAsyncObservableLongMeasurementMock {

    private final String m_name;
    private final CountDownLatch m_callbackEntry;
    private final Callable<Long> m_callable;
    private final PeriodicAsyncMeasurement<Long> m_asyncMeasurement;

    public BlockingAsyncObservableLongMeasurementMock(String name, long value, long asyncObservationInterval) throws Exception {
      m_name = name;
      m_callbackEntry = new CountDownLatch(1);
      m_callable = mockCallable();
      Mockito.when(m_callable.call()).then(invocation -> {
        m_callbackEntry.await();
        return value;
      });
      m_asyncMeasurement = new PeriodicAsyncMeasurement<>(name, m_callable, () -> RunContexts.empty(), () -> Boolean.TRUE, asyncObservationInterval);
    }

    public Long runObservation() {
      return m_asyncMeasurement.getAndNext();
    }

    public void unblockAndAwaitFinished() throws InterruptedException {
      m_callbackEntry.countDown();
      awaitAsyncJobFinished(m_name);
      Thread.sleep(20);
    }
  }
}
