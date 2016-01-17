/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.JUnitExceptionHandler;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SimpleScheduleBuilder;

@RunWith(PlatformTestRunner.class)
public class ScheduleAtFixedRateTest {

  @Test
  public void testFiveRunsAndCancel() {
    final List<Long> protocol = Collections.synchronizedList(new ArrayList<Long>());

    final AtomicInteger counter = new AtomicInteger();

    final int nRuns = 3;
    long initialDelayMillis = 300;
    long periodMillis = 500;
    long tStartMillis = System.currentTimeMillis();

    // Schedule a job which runs 'nRuns' times and cancels itself afterwards.
    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (counter.incrementAndGet() == nRuns) {
          IFuture.CURRENT.get().cancel(false);
        }
        else {
          protocol.add(System.currentTimeMillis());
        }
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(initialDelayMillis, TimeUnit.MILLISECONDS)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(TimeUnit.MILLISECONDS.toMillis(periodMillis))
                .repeatForever())));

    // verify
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 30, TimeUnit.SECONDS);
    assertEquals(nRuns, counter.get());
    for (int i = 0; i < protocol.size(); i++) {
      Long actualExecutionTime = protocol.get(i);
      long expectedExecutionTime = tStartMillis + initialDelayMillis + i * periodMillis;
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
    long initialDelayMillis = 300;
    long periodMillis = 500;
    long tStartMillis = System.currentTimeMillis();

    // Schedule a job which runs 'nRuns' times and cancels itself afterwards.
    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (counter.incrementAndGet() == nRuns) {
          throw new Exception("expected JUnit test exception");
        }
        else {
          protocol.add(System.currentTimeMillis());
        }
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withExceptionHandling(null, false)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(initialDelayMillis, TimeUnit.MILLISECONDS)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(TimeUnit.MILLISECONDS.toMillis(periodMillis))
                .repeatForever())));

    // verify
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 30, TimeUnit.SECONDS);
    assertEquals(nRuns, counter.get());
    for (int i = 0; i < protocol.size(); i++) {
      Long actualExecutionTime = protocol.get(i);
      long expectedExecutionTime = tStartMillis + initialDelayMillis + i * periodMillis;
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
    final long sleepTimeMillis = 300;
    long initialDelayMillis = 300;
    long periodMillis = 500;
    long tStartMillis = System.currentTimeMillis();

    // Schedule a job which runs 'nRuns' times and cancels itself afterwards.
    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (counter.incrementAndGet() == nRuns) {
          throw new Exception("expected JUnit test exception");
        }
        else {
          protocol.add(System.currentTimeMillis());
          Thread.sleep(TimeUnit.MILLISECONDS.toMillis(sleepTimeMillis));
        }
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withExceptionHandling(null, false)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(initialDelayMillis, TimeUnit.MILLISECONDS)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(TimeUnit.MILLISECONDS.toMillis(periodMillis))
                .repeatForever())));

    // verify
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 30, TimeUnit.SECONDS);
    assertEquals(nRuns, counter.get());
    for (int i = 0; i < protocol.size(); i++) {
      Long actualExecutionTime = protocol.get(i);
      long expectedExecutionTime = tStartMillis + initialDelayMillis + i * periodMillis;
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
    final long sleepTimeMillis = 1500;
    long initialDelayMillis = 300;
    long periodMillis = 500;
    long tStartMillis = System.currentTimeMillis();

    // Schedule a job which runs 'nRuns' times and cancels itself afterwards.
    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (counter.incrementAndGet() == nRuns) {
          throw new Exception("expected JUnit test exception");
        }
        else {
          protocol.add(System.currentTimeMillis());
          Thread.sleep(TimeUnit.MILLISECONDS.toMillis(sleepTimeMillis));
        }
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withExceptionHandling(null, false)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(initialDelayMillis, TimeUnit.MILLISECONDS)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(TimeUnit.MILLISECONDS.toMillis(periodMillis))
                .repeatForever())));

    // verify
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 30, TimeUnit.SECONDS);
    assertEquals(nRuns, counter.get());
    for (int i = 0; i < protocol.size(); i++) {
      Long actualExecutionTime = protocol.get(i);
      long expectedExecutionTime = tStartMillis + initialDelayMillis + i * sleepTimeMillis;
      long expectedExecutionTimeMin = expectedExecutionTime;

      if (actualExecutionTime < expectedExecutionTimeMin) {
        fail(String.format("run=%s, actualExecutionTime=%s, expectedExecutionTime=%s", i, actualExecutionTime, expectedExecutionTimeMin));
      }
    }
  }

  @Test
  public void testSwallowException() {
    final AtomicInteger counter = new AtomicInteger();
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (counter.incrementAndGet() == 2) {
          RunMonitor.CURRENT.get().cancel(false);
        }
        else {
          throw new Exception("expected JUnit test exception");
        }
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withExceptionHandling(null, true/* swallow */ )
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(1)
                .repeatForever())))
        .awaitDone(10, TimeUnit.SECONDS);
    assertEquals(2, counter.get());
  }

  @Test
  public void testPropagatedException() {
    final AtomicInteger counter = new AtomicInteger();
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (counter.incrementAndGet() == 2) {
          RunMonitor.CURRENT.get().cancel(false);
        }
        else {
          throw new Exception("expected JUnit test exception");
        }
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withExceptionHandling(null, false /* propagated */ )
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(1)
                .repeatForever())))
        .awaitDone(10, TimeUnit.SECONDS);
    assertEquals(1, counter.get());
  }

  @Test
  public void testDefaultExceptionHandling() {
    // Unregister JUnit exception handler
    BEANS.getBeanManager().unregisterBean(BEANS.getBeanManager().getBean(JUnitExceptionHandler.class));

    final AtomicInteger counter = new AtomicInteger();
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (counter.incrementAndGet() == 2) {
          RunMonitor.CURRENT.get().cancel(false);
        }
        else {
          throw new Exception("expected JUnit test exception");
        }
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(1)
                .repeatForever())))
        .awaitDone(10, TimeUnit.SECONDS);
    assertEquals(1, counter.get());
  }

  @Test
  public void testRepetiveWithTotalCount() {
    final AtomicInteger counter = new AtomicInteger();
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        counter.incrementAndGet();
      }
    }, Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(1) // with 1s, no consolidation should be occur.
                .withRepeatCount(3))))
        .awaitDone(10, TimeUnit.SECONDS);
    assertEquals(4, counter.get());
  }

  @Test
  public void testRepetiveWithEndTime() {
    final AtomicInteger counter = new AtomicInteger();
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        counter.incrementAndGet();
      }
    }, Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withEndIn(1, TimeUnit.SECONDS)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(1)
                .repeatForever())))
        .awaitDone(10, TimeUnit.SECONDS);

    assertTrue(counter.get() > 10);
  }
}
