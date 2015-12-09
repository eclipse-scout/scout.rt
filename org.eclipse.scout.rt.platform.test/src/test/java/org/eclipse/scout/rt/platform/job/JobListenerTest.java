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
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.holders.BooleanHolder;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobListenerTest {

  private IBean<IJobManager> m_jobManagerBean;

  @Before
  public void before() {
    // Use dedicated job manager because job manager is shutdown in tests.
    m_jobManagerBean = JobTestUtil.replaceCurrentJobManager(new JobManager() {
      // must be a subclass in order to replace JobManager
    });
  }

  @After
  public void after() {
    JobTestUtil.unregisterAndShutdownJobManager(m_jobManagerBean);
  }

  @Test
  public void testEvents() throws Exception {
    JobListenerEventCollector jobListener = new JobListenerEventCollector();
    IJobListenerRegistration jobListenerRegistration = Jobs.getJobManager().addListener(jobListener);

    ShutdownListenerEventCollector shutdownListener = new ShutdownListenerEventCollector();
    Jobs.getJobManager().addListener(Jobs.newEventFilterBuilder()
        .andMatchEventType(JobEventType.SHUTDOWN)
        .toFilter(), shutdownListener);

    IFuture<Void> future = Jobs.getJobManager().schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(RunContexts.empty()));
    future.awaitDone(10, TimeUnit.SECONDS);
    jobListenerRegistration.dispose();

    Jobs.getJobManager().shutdown();

    List<JobEventType> expectedStati = new ArrayList<>();
    expectedStati.add(JobEventType.SCHEDULED);
    expectedStati.add(JobEventType.ABOUT_TO_RUN);
    expectedStati.add(JobEventType.DONE);
    assertEquals(expectedStati, jobListener.m_eventTypes);

    List<IFuture<Void>> expectedFutures = new ArrayList<>();
    expectedFutures.add(future); // scheduled
    expectedFutures.add(future); // about to run
    expectedFutures.add(future); // done
    assertEquals(expectedFutures, jobListener.m_futures);

    assertTrue(shutdownListener.m_shutdown.get());
  }

  @Test
  public void testCancel() throws Exception {
    JobListenerEventCollector jobListener = new JobListenerEventCollector();
    IJobListenerRegistration jobListenerRegistration = Jobs.getJobManager().addListener(jobListener);

    ShutdownListenerEventCollector shutdownListener = new ShutdownListenerEventCollector();
    Jobs.getJobManager().addListener(Jobs.newEventFilterBuilder()
        .andMatchEventType(JobEventType.SHUTDOWN)
        .toFilter(), shutdownListener);

    final BooleanHolder hasStarted = new BooleanHolder(Boolean.FALSE);
    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        hasStarted.setValue(Boolean.TRUE);
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withSchedulingDelay(1, TimeUnit.HOURS));

    future.cancel(true);
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 10, TimeUnit.SECONDS);
    jobListenerRegistration.dispose();
    Jobs.getJobManager().shutdown();

    Assert.assertFalse(hasStarted.getValue());
    assertEquals(Arrays.asList(JobEventType.SCHEDULED, JobEventType.DONE), jobListener.m_eventTypes);
    assertEquals(Arrays.asList(future, future), jobListener.m_futures);
    assertTrue(future.isCancelled());

    assertTrue(shutdownListener.m_shutdown.get());
  }

  @Test
  public void testGlobalListener1() {
    JobListenerEventCollector listener = new JobListenerEventCollector();
    Jobs.getJobManager().addListener(listener);

    IFuture<Void> future = Jobs.getJobManager().schedule(mock(IRunnable.class), Jobs.newInput());
    future.awaitDone();

    // verify
    assertEquals(Arrays.asList(JobEventType.SCHEDULED, JobEventType.ABOUT_TO_RUN, JobEventType.DONE), listener.m_eventTypes);
    assertEquals(Arrays.asList(future, future, future), listener.m_futures);
  }

  @Test
  public void testGlobalListener2() {
    JobListenerEventCollector listener = new JobListenerEventCollector();
    Jobs.getJobManager().addListener(Jobs.newEventFilterBuilder()
        .andMatchEventType(
            JobEventType.SCHEDULED,
            JobEventType.DONE)
        .toFilter(), listener);

    IFuture<Void> future = Jobs.getJobManager().schedule(mock(IRunnable.class), Jobs.newInput());
    future.awaitDone();

    // verify
    assertEquals(Arrays.asList(JobEventType.SCHEDULED, JobEventType.DONE), listener.m_eventTypes);
    assertEquals(Arrays.asList(future, future), listener.m_futures);
  }

  @Test
  public void testLocalListener1() throws InterruptedException {
    JobListenerEventCollector listener = new JobListenerEventCollector();

    // schedule job, and install listener once started running
    final BlockingCountDownLatch jobRunningLatch = new BlockingCountDownLatch(1);
    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        jobRunningLatch.countDownAndBlock();
      }
    }, Jobs.newInput());
    jobRunningLatch.await();

    Jobs.getJobManager().addListener(Jobs.newEventFilterBuilder()
        .andMatchEventType(JobEventType.DONE)
        .andMatchFuture(future)
        .toFilter(), listener);
    jobRunningLatch.unblock();
    future.awaitDone();

    // verify
    assertEquals(Arrays.asList(JobEventType.DONE), listener.m_eventTypes);
    assertEquals(Arrays.asList(future), listener.m_futures);
  }

  @Test
  public void testLocalListener2a() throws InterruptedException {
    JobListenerEventCollector listener = new JobListenerEventCollector();

    // Schedule job-1
    IFuture<Void> future1 = Jobs.getJobManager().schedule(mock(IRunnable.class), Jobs.newInput());
    future1.awaitDone();

    // schedule job-2, and install listener once started running
    final BlockingCountDownLatch job2RunningLatch = new BlockingCountDownLatch(1);
    IFuture<Void> future2 = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        job2RunningLatch.countDownAndBlock();
      }
    }, Jobs.newInput());
    job2RunningLatch.await();

    // install listener
    Jobs.getJobManager().addListener(Jobs.newEventFilterBuilder()
        .andMatchFuture(future1, future2)
        .toFilter(), listener);

    job2RunningLatch.unblock();
    future2.awaitDone();

    // verify
    assertEquals(Arrays.asList(JobEventType.DONE), listener.m_eventTypes);
    assertEquals(Arrays.asList(future2), listener.m_futures);
  }

  @Test
  public void testLocalListener2b() throws InterruptedException {
    IFuture<Void> future1 = Jobs.getJobManager().schedule(mock(IRunnable.class), Jobs.newInput());
    future1.awaitDone();

    final BlockingCountDownLatch job2RunningLatch = new BlockingCountDownLatch(1);
    IFuture<Void> future2 = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        job2RunningLatch.countDownAndBlock();
      }
    }, Jobs.newInput());
    job2RunningLatch.await();

    JobListenerEventCollector listener = new JobListenerEventCollector();
    Jobs.getJobManager().addListener(Jobs.newEventFilterBuilder()
        .andMatchFuture(future2)
        .andMatchFuture(future1)
        .toFilter(), listener);

    job2RunningLatch.unblock();
    future2.awaitDone();

    // verify
    assertEquals(Collections.emptyList(), listener.m_eventTypes);
    assertEquals(Collections.emptyList(), listener.m_futures);
  }

  @Test
  public void testLocalListener3() throws InterruptedException {
    IFuture<Void> future1 = Jobs.getJobManager().schedule(mock(IRunnable.class), Jobs.newInput());
    future1.awaitDone();

    final BlockingCountDownLatch job2RunningLatch = new BlockingCountDownLatch(1);
    IFuture<Void> future2 = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        job2RunningLatch.countDownAndBlock();
      }
    }, Jobs.newInput());
    job2RunningLatch.await();

    JobListenerEventCollector listener = new JobListenerEventCollector();
    Jobs.getJobManager().addListener(Jobs.newEventFilterBuilder()
        .andMatchNotFuture(future2)
        .toFilter(), listener);

    job2RunningLatch.unblock();
    future2.awaitDone();

    // verify
    assertEquals(Collections.emptyList(), listener.m_eventTypes);
    assertEquals(Collections.emptyList(), listener.m_futures);
  }

  @Test
  public void testLocalListener4() throws InterruptedException {
    final BlockingCountDownLatch jobRunningLatch = new BlockingCountDownLatch(1);
    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        jobRunningLatch.countDownAndBlock();
      }
    }, Jobs.newInput());
    jobRunningLatch.await();

    JobListenerEventCollector listener = new JobListenerEventCollector();
    future.addListener(listener);

    jobRunningLatch.unblock();
    future.awaitDone();

    // verify
    assertEquals(Arrays.asList(JobEventType.DONE), listener.m_eventTypes);
    assertEquals(Arrays.asList(future), listener.m_futures);
  }

  @Test
  public void testLocalListener5() throws InterruptedException {
    final BlockingCountDownLatch jobRunningLatch = new BlockingCountDownLatch(1);
    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        jobRunningLatch.countDownAndBlock();
      }
    }, Jobs.newInput());
    jobRunningLatch.await();

    JobListenerEventCollector listener = new JobListenerEventCollector();
    future.addListener(Jobs.newEventFilterBuilder()
        .andMatchEventType(JobEventType.DONE)
        .toFilter(), listener);

    jobRunningLatch.unblock();
    future.awaitDone();

    // verify
    assertEquals(Arrays.asList(JobEventType.DONE), listener.m_eventTypes);
    assertEquals(Arrays.asList(future), listener.m_futures);
  }

  private static final class JobListenerEventCollector implements IJobListener {

    private final List<JobEventType> m_eventTypes = Collections.synchronizedList(new ArrayList<JobEventType>());
    private final List<IFuture<?>> m_futures = Collections.synchronizedList(new ArrayList<IFuture<?>>());

    @Override
    public void changed(JobEvent event) {
      m_eventTypes.add(event.getType());
      m_futures.add(event.getFuture());
    }
  }

  private static final class ShutdownListenerEventCollector implements IJobListener {

    private final AtomicBoolean m_shutdown = new AtomicBoolean();

    @Override
    public void changed(JobEvent event) {
      m_shutdown.set(JobEventType.SHUTDOWN == event.getType());
    }
  }
}
