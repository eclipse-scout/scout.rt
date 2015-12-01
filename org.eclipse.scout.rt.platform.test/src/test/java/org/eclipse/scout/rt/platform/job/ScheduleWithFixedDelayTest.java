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

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ScheduleWithFixedDelayTest {

  private IBean<IJobManager> m_jobManagerBean;

  @Before
  public void before() {
    m_jobManagerBean = JobTestUtil.registerJobManager();
  }

  @After
  public void after() {
    JobTestUtil.unregisterJobManager(m_jobManagerBean);
  }

  @Test
  public void testFiveRunsAndCancel() {
    final List<Long> protocol = Collections.synchronizedList(new ArrayList<Long>());

    final AtomicInteger counter = new AtomicInteger();

    final int nRuns = 3;
    long initialDelayNanos = TimeUnit.MILLISECONDS.toNanos(300);
    long delayNanos = TimeUnit.MILLISECONDS.toNanos(500);
    long tStartNano = System.nanoTime();

    // Schedule a job which runs 'nRuns' times and cancels itself afterwards.
    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (counter.incrementAndGet() == nRuns) {
          IFuture.CURRENT.get().cancel(false);
        }
        else {
          protocol.add(System.nanoTime());
        }
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withSchedulingDelay(initialDelayNanos, TimeUnit.NANOSECONDS)
        .withPeriodicExecutionWithFixedDelay(delayNanos, TimeUnit.NANOSECONDS));

    // verify
    assertTrue(Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 30, TimeUnit.SECONDS));
    assertEquals(nRuns, counter.get());
    for (int i = 0; i < protocol.size(); i++) {
      Long actualExecutionTime = protocol.get(i);
      long expectedExecutionTime = tStartNano + initialDelayNanos + i * delayNanos;
      long expectedExecutionTimeMin = expectedExecutionTime;

      if (actualExecutionTime < expectedExecutionTimeMin) {
        fail(String.format("run=%s, actualExecutionTime=%s, expectedExecutionTime=%s", i, actualExecutionTime, expectedExecutionTimeMin));
      }
    }
  }

  @Test
  public void testFiveRunsAndException() {
    final List<Long> protocol = Collections.synchronizedList(new ArrayList<Long>());

    final AtomicInteger counter = new AtomicInteger();

    final int nRuns = 3;
    long initialDelayNanos = TimeUnit.MILLISECONDS.toNanos(300);
    long delayNanos = TimeUnit.MILLISECONDS.toNanos(500);
    long tStartNano = System.nanoTime();

    // Schedule a job which runs 'nRuns' times and cancels itself afterwards.
    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (counter.incrementAndGet() == nRuns) {
          throw new Exception("blubber");
        }
        else {
          protocol.add(System.nanoTime());
        }
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withSchedulingDelay(initialDelayNanos, TimeUnit.NANOSECONDS)
        .withPeriodicExecutionWithFixedDelay(delayNanos, TimeUnit.NANOSECONDS)
        .withLogOnError(false));

    // verify
    assertTrue(Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 30, TimeUnit.SECONDS));
    assertEquals(nRuns, counter.get());
    for (int i = 0; i < protocol.size(); i++) {
      Long actualExecutionTime = protocol.get(i);
      long expectedExecutionTime = tStartNano + initialDelayNanos + i * delayNanos;
      long expectedExecutionTimeMin = expectedExecutionTime;

      if (actualExecutionTime < expectedExecutionTimeMin) {
        fail(String.format("run=%s, actualExecutionTime=%s, expectedExecutionTime=%s", i, actualExecutionTime, expectedExecutionTimeMin));
      }
    }
  }

  @Test
  public void testFiveShortRunsAndException() {
    final List<Long> protocol = Collections.synchronizedList(new ArrayList<Long>());

    final AtomicInteger counter = new AtomicInteger();

    final int nRuns = 3;
    final long sleepTimeNano = TimeUnit.MILLISECONDS.toNanos(300);
    long initialDelayNanos = TimeUnit.MILLISECONDS.toNanos(300);
    long delayNanos = TimeUnit.MILLISECONDS.toNanos(500);
    long tStartNano = System.nanoTime();

    // Schedule a job which runs 'nRuns' times and cancels itself afterwards.
    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

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
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withSchedulingDelay(initialDelayNanos, TimeUnit.NANOSECONDS)
        .withPeriodicExecutionWithFixedDelay(delayNanos, TimeUnit.NANOSECONDS)
        .withLogOnError(false));

    // verify
    assertTrue(Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 30, TimeUnit.SECONDS));
    assertEquals(nRuns, counter.get());
    for (int i = 0; i < protocol.size(); i++) {
      Long actualExecutionTime = protocol.get(i);
      long expectedExecutionTime = tStartNano + initialDelayNanos + i * (sleepTimeNano + delayNanos);
      long expectedExecutionTimeMin = expectedExecutionTime;

      if (actualExecutionTime < expectedExecutionTimeMin) {
        fail(String.format("run=%s, actualExecutionTime=%s, expectedExecutionTime=%s", i, actualExecutionTime, expectedExecutionTimeMin));
      }
    }
  }

  @Test
  public void testFiveLongRunsAndException() {
    final List<Long> protocol = Collections.synchronizedList(new ArrayList<Long>());

    final AtomicInteger counter = new AtomicInteger();

    final int nRuns = 3;
    final long sleepTimeNano = TimeUnit.MILLISECONDS.toNanos(1500);
    long initialDelayNanos = TimeUnit.MILLISECONDS.toNanos(300);
    long delayNanos = TimeUnit.MILLISECONDS.toNanos(500);
    long tStartNano = System.nanoTime();

    // Schedule a job which runs 5 times and cancels itself afterwards.
    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

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
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withSchedulingDelay(initialDelayNanos, TimeUnit.NANOSECONDS)
        .withPeriodicExecutionWithFixedDelay(delayNanos, TimeUnit.NANOSECONDS)
        .withLogOnError(false));

    // verify
    assertTrue(Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 30, TimeUnit.SECONDS));
    assertEquals(nRuns, counter.get());
    for (int i = 0; i < protocol.size(); i++) {
      Long actualExecutionTime = protocol.get(i);
      long expectedExecutionTime = tStartNano + initialDelayNanos + i * (sleepTimeNano + delayNanos);
      long expectedExecutionTimeMin = expectedExecutionTime;

      if (actualExecutionTime < expectedExecutionTimeMin) {
        fail(String.format("run=%s, actualExecutionTime=%s, expectedExecutionTime=%s", i, actualExecutionTime, expectedExecutionTimeMin));
      }
    }
  }
}
