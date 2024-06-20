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
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.filter.future.FutureFilterBuilder;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.helpers.MessageFormatter;

public class PeriodicAsyncMeasurementTest {

  private static final int DEFAULT_TIMEOUT_CONDITION_MET_MILLIS = 100;

  private IExecutionSemaphore m_asyncJobExecutionSemaphore;
  private int m_origExecutionSemaphorePermits;

  @Before
  public void before() {
    m_asyncJobExecutionSemaphore = PeriodicAsyncMeasurement.ASYNC_JOB_EXECUTION_SEMAPHORE.get();
    m_origExecutionSemaphorePermits = m_asyncJobExecutionSemaphore.getPermits();
    m_asyncJobExecutionSemaphore.withPermits(1);
  }

  @After
  public void after() throws Exception {
    try {
      awaitCondition(() -> m_asyncJobExecutionSemaphore.getCompetitorCount() == 0, DEFAULT_TIMEOUT_CONDITION_MET_MILLIS);
    }
    finally {
      m_asyncJobExecutionSemaphore.withPermits(m_origExecutionSemaphorePermits);
    }
  }

  @Test
  public void testSimple() throws Exception {
    assertEquals(0, m_asyncJobExecutionSemaphore.getCompetitorCount());

    final long asyncObservationInterval = 100;
    AtomicLong value = new AtomicLong(10);
    PeriodicAsyncMeasurementMock mock = new PeriodicAsyncMeasurementMock("testSimple", () -> value.getAndAdd(5), asyncObservationInterval);

    // first observation --> no measurement, but trigger async job
    assertNull(mock.runObservation());
    mock.verifyAsyncJobTriggered();
    mock.verifyAsyncJobFinished();
    assertEquals(15, value.get());

    // second observation --> first measurement, but no trigger async job
    assertEquals(Long.valueOf(10L), mock.runObservation());
    mock.verifyNoAsyncJobTriggered();

    // third observation --> second measurement, still no trigger async job
    assertEquals(Long.valueOf(10L), mock.runObservation());
    mock.verifyNoAsyncJobTriggered();

    awaitNextAsyncObservationInterval(asyncObservationInterval);

    // forth observation --> third measurement, trigger async job
    assertEquals(Long.valueOf(10L), mock.runObservation());
    mock.verifyAsyncJobTriggered();
    mock.verifyAsyncJobFinished();
    assertEquals(20, value.get());

    // second observation --> first measurement, but no trigger async job
    assertEquals(Long.valueOf(15L), mock.runObservation());
    mock.verifyNoAsyncJobTriggered();
  }

  @Test
  public void testLongRunningAsyncJob() throws Exception {
    assertEquals(0, m_asyncJobExecutionSemaphore.getCompetitorCount());

    final long asyncObservationInterval = 100;
    PeriodicAsyncMeasurementMock mock = new PeriodicAsyncMeasurementMock("testLongRunningAsyncJob", () -> 2L, asyncObservationInterval);

    // first observation --> no measurement, but trigger async job
    assertNull(mock.runObservation());
    mock.verifyAsyncJobTriggered();

    // second observation --> no measurement, but no trigger async job
    assertNull(mock.runObservation());
    mock.verifyNoAsyncJobTriggered();

    // third observation --> no measurement, still no trigger async job
    assertNull(mock.runObservation());
    mock.verifyNoAsyncJobTriggered();

    awaitNextAsyncObservationInterval(asyncObservationInterval);

    Set<IFuture<?>> runningAsyncJobs = getRunningAsyncJobs();
    assertEquals(1, runningAsyncJobs.size());
    IFuture<?> firstAsyncJob = runningAsyncJobs.iterator().next();

    // forth observation --> no measurement, trigger async job
    assertNull(mock.runObservation());
    mock.verifyAsyncJobTriggered();
    runningAsyncJobs = getRunningAsyncJobs();
    assertEquals(1, runningAsyncJobs.size());
    assertNotSame(firstAsyncJob, runningAsyncJobs.iterator().next());

    // fifth observation --> no measurement, no trigger async job
    assertNull(mock.runObservation());
    mock.verifyNoAsyncJobTriggered();

    runningAsyncJobs.iterator().next().cancel(true);
    mock.awaitAsyncJobFinished();
  }

  @Test
  public void testFailingAsyncJob() throws Exception {
    assertEquals(0, m_asyncJobExecutionSemaphore.getCompetitorCount());

    final long asyncObservationInterval = 200;
    PeriodicAsyncMeasurementMock mock = new PeriodicAsyncMeasurementMock("testFailingAsyncJob", () -> {
      throw new RuntimeException("fail");
    }, asyncObservationInterval);

    // first observation --> no measurement, but trigger async job
    assertNull(mock.runObservation());
    mock.verifyAsyncJobTriggered();
    mock.verifyAsyncJobFinished();

    // second observation --> no measurement, but no trigger async job
    assertNull(mock.runObservation());
    mock.verifyNoAsyncJobTriggered();

    // third observation --> no measurement, still no trigger async job
    assertNull(mock.runObservation());
    mock.verifyNoAsyncJobTriggered();

    awaitNextAsyncObservationInterval(asyncObservationInterval);

    // forth observation --> no measurement, trigger async job
    assertNull(mock.runObservation());
    mock.verifyAsyncJobTriggered();
    mock.verifyAsyncJobFinished();
  }

  @Test
  public void testLimitParallelExecution() throws Exception {
    assertEquals(0, m_asyncJobExecutionSemaphore.getCompetitorCount());
    assertEquals(1, m_asyncJobExecutionSemaphore.getPermits());

    final long asyncObservationInterval = 200;
    PeriodicAsyncMeasurementMock blockingMock1 = new PeriodicAsyncMeasurementMock("testLimitParallelExecution1", () -> 1L, asyncObservationInterval);
    PeriodicAsyncMeasurementMock blockingMock2 = new PeriodicAsyncMeasurementMock("testLimitParallelExecution2", () -> 2L, asyncObservationInterval);

    // first observation --> no measurement, but trigger async jobs
    assertNull(blockingMock1.runObservation());
    assertNull(blockingMock2.runObservation());
    assertEquals(2, getRunningAsyncJobs().size());
    assertEquals(2, m_asyncJobExecutionSemaphore.getCompetitorCount());
    blockingMock1.verifyAsyncJobTriggered();
    blockingMock2.verifyNoAsyncJobTriggered();

    blockingMock1.verifyAsyncJobFinished();
    assertEquals(1, getRunningAsyncJobs().size());
    assertEquals(1, m_asyncJobExecutionSemaphore.getCompetitorCount());
    blockingMock1.verifyNoAsyncJobTriggered();
    blockingMock2.verifyAsyncJobTriggered();

    // second observation --> first measurement1, but no trigger async jobs
    assertEquals(Long.valueOf(1L), blockingMock1.runObservation());
    assertNull(blockingMock2.runObservation());
    assertEquals(1, getRunningAsyncJobs().size());
    assertEquals(1, m_asyncJobExecutionSemaphore.getCompetitorCount());
    blockingMock1.verifyNoAsyncJobTriggered();
    blockingMock2.verifyNoAsyncJobTriggered();

    blockingMock2.verifyAsyncJobFinished();
    assertEquals(0, getRunningAsyncJobs().size());
    assertEquals(0, m_asyncJobExecutionSemaphore.getCompetitorCount());
    blockingMock1.verifyNoAsyncJobTriggered();
    blockingMock2.verifyNoAsyncJobTriggered();

    awaitNextAsyncObservationInterval(asyncObservationInterval);

    // third observation --> second measurement1, first measurement2, trigger async jobs
    assertEquals(Long.valueOf(1L), blockingMock1.runObservation());
    assertEquals(Long.valueOf(2L), blockingMock2.runObservation());
    blockingMock1.verifyAsyncJobTriggered();
    blockingMock2.verifyNoAsyncJobTriggered();
    blockingMock1.verifyAsyncJobFinished();
    blockingMock2.verifyAsyncJobTriggered();
    blockingMock2.verifyAsyncJobFinished();
    assertEquals(0, getRunningAsyncJobs().size());
  }

  @Test
  public void testActiveOnThisNode() throws Exception {
    assertEquals(0, m_asyncJobExecutionSemaphore.getCompetitorCount());

    final long asyncObservationInterval = 100;
    AtomicBoolean activeOnThisNode = new AtomicBoolean(false);
    PeriodicAsyncMeasurementMock mock = new PeriodicAsyncMeasurementMock("testActiveOnThisNode", () -> 111L, () -> activeOnThisNode.get(), asyncObservationInterval);

    // first observation --> no measurement and no trigger async job
    assertNull(mock.runObservation());
    mock.verifyNoAsyncJobTriggered();

    // second observation --> no measurement, but no trigger async job
    assertNull(mock.runObservation());
    mock.verifyNoAsyncJobTriggered();

    awaitNextAsyncObservationInterval(asyncObservationInterval);

    // third observation --> no measurement and no trigger async job
    assertNull(mock.runObservation());
    mock.verifyNoAsyncJobTriggered();

    activeOnThisNode.set(Boolean.TRUE);

    // forth observation --> no measurement, but trigger async job
    assertNull(mock.runObservation());
    mock.verifyAsyncJobTriggered();
    mock.verifyAsyncJobFinished();

    // fifth observation --> first measurement, but no trigger async job
    assertEquals(Long.valueOf(111L), mock.runObservation());
    mock.verifyNoAsyncJobTriggered();
  }

  private Set<IFuture<?>> getRunningAsyncJobs() {
    return BEANS.get(IJobManager.class).getFutures(newAsyncJobFilter().toFilter());
  }

  private Set<IFuture<?>> getRunningAsyncJobs(String name) {
    return BEANS.get(IJobManager.class).getFutures(newAsyncJobFilter(name).toFilter());
  }

  private FutureFilterBuilder newAsyncJobFilter() {
    return Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(PeriodicAsyncMeasurement.ASYNC_JOB_EXECUTION_HINT);
  }

  private FutureFilterBuilder newAsyncJobFilter(String name) {
    return newAsyncJobFilter()
        .andMatchName(MessageFormatter.arrayFormat(PeriodicAsyncMeasurement.ASYNC_JOB_NAME_PATTERN, new String[]{name}).getMessage());
  }

  private void awaitNextAsyncObservationInterval(long asyncObservationIntervalMillis) throws InterruptedException {
    Thread.sleep(asyncObservationIntervalMillis + 50);
  }

  private Callable<Long> mockCallable() {
    //noinspection unchecked
    Callable<Long> callable = Mockito.mock(Callable.class);
    return callable;
  }

  private static void awaitCondition(BooleanSupplier condition, long timeoutMillis) throws Exception {
    long start = System.currentTimeMillis();
    while (!condition.getAsBoolean()) {
      if (System.currentTimeMillis() - start > timeoutMillis) {
        fail(String.format("Condition not met within %s ms", timeoutMillis));
      }
      //noinspection BusyWait
      Thread.sleep(10);
    }
  }

  private class PeriodicAsyncMeasurementMock {

    private final String m_name;
    private final Callable<Long> m_callable;
    private final CyclicBarrier m_callableStarted = new CyclicBarrier(2);
    private final CyclicBarrier m_callableFinished = new CyclicBarrier(2);
    private final CyclicBarrier m_asyncJobDoneHandlersFinished = new CyclicBarrier(2);
    private final PeriodicAsyncMeasurement<Long> m_asyncMeasurement;
    private int m_expectedAsyncJobTriggerCount = 0;

    public PeriodicAsyncMeasurementMock(String name, LongSupplier valueSupplier, long asyncObservationInterval) throws Exception {
      this(name, valueSupplier, () -> Boolean.TRUE, asyncObservationInterval);
    }

    public PeriodicAsyncMeasurementMock(String name, LongSupplier valueSupplier, Supplier<Boolean> activeOnThisNodeSupplier, long asyncObservationInterval) throws Exception {
      m_name = name;
      m_callable = mockCallable();
      Mockito.when(m_callable.call()).then(invocation -> {
        m_callableStarted.await();
        try {
          return valueSupplier.getAsLong();
        }
        finally {
          m_callableFinished.await();
        }
      });
      m_asyncMeasurement = new PeriodicAsyncMeasurement<>(name, m_callable, () -> RunContexts.empty(), activeOnThisNodeSupplier, asyncObservationInterval);
    }

    public Long runObservation() {
      return m_asyncMeasurement.getAndNext();
    }

    public void verifyAsyncJobTriggered() throws Exception {
      m_expectedAsyncJobTriggerCount++;
      assertCallableStarted();
      assertAsyncJobTrigger(m_expectedAsyncJobTriggerCount);
    }

    public void verifyNoAsyncJobTriggered() throws Exception {
      assertAsyncJobTrigger(m_expectedAsyncJobTriggerCount);
    }

    public void verifyAsyncJobFinished() throws Exception {
      assertCallableFinished();
      awaitAsyncJobFinished();
      assertAsyncJobDoneHandlersFinished();
    }

    private void assertAsyncJobTrigger(int expectedTriggerCount) throws Exception {
      Mockito.verify(m_callable, Mockito.timeout(100).times(expectedTriggerCount)).call();
    }

    private void assertCallableStarted() throws Exception {
      m_callableStarted.await(1, TimeUnit.SECONDS);
      Set<IFuture<?>> runningAsyncJobs = getRunningAsyncJobs(m_name);
      assertEquals(1, runningAsyncJobs.size());
      IFuture<?> asyncJob = runningAsyncJobs.iterator().next();
      asyncJob.addListener(event -> event.getType() == JobEventType.JOB_STATE_CHANGED && asyncJob.isCancelled(), event -> resetCallableBarriers());
      // register done handler to be run at the very end of all done handlers, especially the one registered by PeriodicAsyncMeasurement.triggerAsyncMeasurement()
      // (this required to be able to verify/test the observation value is set correctly)
      asyncJob.whenDone(event -> {
        try {
          m_asyncJobDoneHandlersFinished.await();
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      }, RunContexts.empty());
    }

    private void resetCallableBarriers() {
      m_callableStarted.reset();
      m_callableFinished.reset();
    }

    private void assertCallableFinished() throws Exception {
      m_callableFinished.await(1, TimeUnit.SECONDS);
    }

    private void assertAsyncJobDoneHandlersFinished() throws Exception {
      m_asyncJobDoneHandlersFinished.await(1, TimeUnit.SECONDS);
    }

    private void awaitAsyncJobFinished() {
      BEANS.get(IJobManager.class).awaitFinished(newAsyncJobFilter(m_name).toFilter(), 1, TimeUnit.SECONDS);
    }
  }
}
