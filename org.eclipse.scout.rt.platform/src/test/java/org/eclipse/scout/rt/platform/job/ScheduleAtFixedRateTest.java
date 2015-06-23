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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ScheduleAtFixedRateTest {

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
  public void testFiveRunsAndCancel() throws ProcessingException {
    final List<Long> protocol = Collections.synchronizedList(new ArrayList<Long>());

    final AtomicInteger counter = new AtomicInteger();

    final int nRuns = 3;
    long initialDelayNano = TimeUnit.MILLISECONDS.toNanos(300);
    long periodNano = TimeUnit.MILLISECONDS.toNanos(500);
    long tStartNano = System.nanoTime();
    long assertToleranceNano = TimeUnit.MILLISECONDS.toNanos(200);

    // Schedule a job which runs 'nRuns' times and cancels itself afterwards.
    IFuture<Void> future = m_jobManager.scheduleAtFixedRate(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (counter.incrementAndGet() == nRuns) {
          IFuture.CURRENT.get().cancel(false);
        }
        else {
          protocol.add(System.nanoTime());
        }
      }
    }, initialDelayNano, periodNano, TimeUnit.NANOSECONDS, Jobs.newInput(RunContexts.empty()));

    // verify
    assertTrue(m_jobManager.awaitDone(Jobs.newFutureFilter().andMatchFutures(future), 30, TimeUnit.SECONDS));
    assertEquals(nRuns, counter.get());
    for (int i = 0; i < protocol.size(); i++) {
      Long actualExecutionTime = protocol.get(i);
      long expectedExecutionTime = tStartNano + initialDelayNano + i * periodNano;
      long expectedExecutionTimeMin = expectedExecutionTime;
      long expectedExecutionTimeMax = expectedExecutionTime + assertToleranceNano;

      if (actualExecutionTime < expectedExecutionTimeMin || actualExecutionTime > expectedExecutionTimeMax) {
        fail(String.format("run=%s, actualExecutionTime=%s, expectedExecutionTime=[%s;%s]", i, actualExecutionTime, expectedExecutionTimeMin, expectedExecutionTimeMax));
      }
    }
  }

  @Test
  public void testFiveRunsAndException() throws ProcessingException {
    final List<Long> protocol = Collections.synchronizedList(new ArrayList<Long>());

    final AtomicInteger counter = new AtomicInteger();

    final int nRuns = 3;
    long initialDelayNano = TimeUnit.MILLISECONDS.toNanos(300);
    long periodNano = TimeUnit.MILLISECONDS.toNanos(500);
    long assertToleranceNano = TimeUnit.MILLISECONDS.toNanos(200);
    long tStartNano = System.nanoTime();

    // Schedule a job which runs 'nRuns' times and cancels itself afterwards.
    IFuture<Void> future = m_jobManager.scheduleAtFixedRate(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (counter.incrementAndGet() == nRuns) {
          throw new Exception("blubber");
        }
        else {
          protocol.add(System.nanoTime());
        }
      }
    }, initialDelayNano, periodNano, TimeUnit.NANOSECONDS, Jobs.newInput(RunContexts.empty()).logOnError(false));

    // verify
    assertTrue(m_jobManager.awaitDone(Jobs.newFutureFilter().andMatchFutures(future), 30, TimeUnit.SECONDS));
    assertEquals(nRuns, counter.get());
    for (int i = 0; i < protocol.size(); i++) {
      Long actualExecutionTime = protocol.get(i);
      long expectedExecutionTime = tStartNano + initialDelayNano + i * periodNano;
      long expectedExecutionTimeMin = expectedExecutionTime;
      long expectedExecutionTimeMax = expectedExecutionTime + assertToleranceNano;

      if (actualExecutionTime < expectedExecutionTimeMin || actualExecutionTime > expectedExecutionTimeMax) {
        fail(String.format("run=%s, actualExecutionTime=%s, expectedExecutionTime=[%s;%s]", i, actualExecutionTime, expectedExecutionTimeMin, expectedExecutionTimeMax));
      }
    }
  }

  @Test
  public void testFiveShortRunsAndException() throws ProcessingException {
    final List<Long> protocol = Collections.synchronizedList(new ArrayList<Long>());

    final AtomicInteger counter = new AtomicInteger();

    final int nRuns = 3;
    final long sleepTimeNano = TimeUnit.MILLISECONDS.toNanos(300);
    long initialDelayNano = TimeUnit.MILLISECONDS.toNanos(300);
    long periodNano = TimeUnit.MILLISECONDS.toNanos(500);
    long assertToleranceNano = TimeUnit.MILLISECONDS.toNanos(200);
    long tStartNano = System.nanoTime();

    // Schedule a job which runs 'nRuns' times and cancels itself afterwards.
    IFuture<Void> future = m_jobManager.scheduleAtFixedRate(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (counter.incrementAndGet() == nRuns) {
          throw new Exception("blubber");
        }
        else {
          protocol.add(System.nanoTime());
          Thread.sleep(TimeUnit.NANOSECONDS.toMillis(sleepTimeNano));
        }
      }
    }, initialDelayNano, periodNano, TimeUnit.NANOSECONDS, Jobs.newInput(RunContexts.empty()).logOnError(false));

    // verify
    assertTrue(m_jobManager.awaitDone(Jobs.newFutureFilter().andMatchFutures(future), 30, TimeUnit.SECONDS));
    assertEquals(nRuns, counter.get());
    for (int i = 0; i < protocol.size(); i++) {
      Long actualExecutionTime = protocol.get(i);
      long expectedExecutionTime = tStartNano + initialDelayNano + i * periodNano;
      long expectedExecutionTimeMin = expectedExecutionTime;
      long expectedExecutionTimeMax = expectedExecutionTime + assertToleranceNano;

      if (actualExecutionTime < expectedExecutionTimeMin || actualExecutionTime > expectedExecutionTimeMax) {
        fail(String.format("run=%s, actualExecutionTime=%s, expectedExecutionTime=[%s;%s]", i, actualExecutionTime, expectedExecutionTimeMin, expectedExecutionTimeMax));
      }
    }
  }

  @Test
  public void testFiveLongRunsAndException() throws ProcessingException {
    final List<Long> protocol = Collections.synchronizedList(new ArrayList<Long>());

    final AtomicInteger counter = new AtomicInteger();

    final int nRuns = 3;
    final long sleepTimeNano = TimeUnit.MILLISECONDS.toNanos(1500);
    long initialDelayNano = TimeUnit.MILLISECONDS.toNanos(300);
    long periodNano = TimeUnit.MILLISECONDS.toNanos(500);
    long assertToleranceNano = TimeUnit.MILLISECONDS.toNanos(200);
    long tStartNano = System.nanoTime();

    // Schedule a job which runs 'nRuns' times and cancels itself afterwards.
    IFuture<Void> future = m_jobManager.scheduleAtFixedRate(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (counter.incrementAndGet() == nRuns) {
          throw new Exception("blubber");
        }
        else {
          protocol.add(System.nanoTime());
          Thread.sleep(TimeUnit.NANOSECONDS.toMillis(sleepTimeNano));
        }
      }
    }, initialDelayNano, periodNano, TimeUnit.NANOSECONDS, Jobs.newInput(RunContexts.empty()).logOnError(false));

    // verify
    assertTrue(m_jobManager.awaitDone(Jobs.newFutureFilter().andMatchFutures(future), 30, TimeUnit.SECONDS));
    assertEquals(nRuns, counter.get());
    for (int i = 0; i < protocol.size(); i++) {
      Long actualExecutionTime = protocol.get(i);
      long expectedExecutionTime = tStartNano + initialDelayNano + i * sleepTimeNano;
      long expectedExecutionTimeMin = expectedExecutionTime;
      long expectedExecutionTimeMax = expectedExecutionTime + assertToleranceNano;

      if (actualExecutionTime < expectedExecutionTimeMin || actualExecutionTime > expectedExecutionTimeMax) {
        fail(String.format("run=%s, actualExecutionTime=%s, expectedExecutionTime=[%s;%s]", i, actualExecutionTime, expectedExecutionTimeMin, expectedExecutionTimeMax));
      }
    }
  }
}
