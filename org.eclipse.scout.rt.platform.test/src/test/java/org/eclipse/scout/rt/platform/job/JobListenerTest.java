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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.commons.filter.NotFilter;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobListenerTest {

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
  public void testEvents() throws Exception {
    P_JobChangeListener jobListener = new P_JobChangeListener();
    IJobListenerRegistration jobListenerRegistration = m_jobManager.addListener(jobListener);

    P_ShutdownListener shutdownListener = new P_ShutdownListener();
    IJobListenerRegistration shutdownListenerRegistration = m_jobManager.addListener(Jobs.newEventFilter().andMatchEventType(JobEventType.SHUTDOWN), shutdownListener);

    IFuture<Void> future = null;
    future = m_jobManager.schedule(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        return null;
      }
    }, Jobs.newInput(RunContexts.empty()));
    m_jobManager.awaitDone(Jobs.newFutureFilter().andMatchFuture(future), 1, TimeUnit.MINUTES);
    sleep();
    jobListenerRegistration.dispose();
    m_jobManager.shutdown();
    shutdownListenerRegistration.dispose();

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
    P_JobChangeListener jobListener = new P_JobChangeListener();
    IJobListenerRegistration jobListenerRegistration = m_jobManager.addListener(jobListener);

    P_ShutdownListener shutdownListener = new P_ShutdownListener();
    IJobListenerRegistration shutdownListenerRegistration = m_jobManager.addListener(Jobs.newEventFilter().andMatchEventType(JobEventType.SHUTDOWN), shutdownListener);

    final BooleanHolder hasStarted = new BooleanHolder(Boolean.FALSE);
    IFuture<Void> future = m_jobManager.schedule(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        hasStarted.setValue(Boolean.TRUE);
        return null;
      }
    }, 200, TimeUnit.MILLISECONDS, Jobs.newInput(RunContexts.empty()));
    future.cancel(true);
    m_jobManager.awaitDone(Jobs.newFutureFilter().andMatchFuture(future), 1, TimeUnit.MINUTES);
    jobListenerRegistration.dispose();
    m_jobManager.shutdown();
    shutdownListenerRegistration.dispose();

    Assert.assertFalse(hasStarted.getValue().booleanValue());
    assertEquals(Arrays.asList(JobEventType.SCHEDULED, JobEventType.DONE), jobListener.m_eventTypes);
    assertEquals(Arrays.asList(future, future), jobListener.m_futures);
    assertTrue(future.isCancelled());

    assertTrue(shutdownListener.m_shutdown.get());
  }

  @Test
  public void testGlobalListener1() {
    P_JobChangeListener listener = new P_JobChangeListener();
    m_jobManager.addListener(listener);

    IFuture<Void> future = runJob(0, TimeUnit.SECONDS);
    future.awaitDone();
    sleep();

    // verify
    assertEquals(Arrays.asList(JobEventType.SCHEDULED, JobEventType.ABOUT_TO_RUN, JobEventType.DONE), listener.m_eventTypes);
    assertEquals(Arrays.asList(future, future, future), listener.m_futures);
  }

  @Test
  public void testGlobalListener2() throws InterruptedException {
    P_JobChangeListener listener = new P_JobChangeListener();
    m_jobManager.addListener(Jobs.newEventFilter().andMatchEventType(JobEventType.SCHEDULED, JobEventType.DONE), listener);

    IFuture<Void> future = runJob(0, TimeUnit.SECONDS);
    future.awaitDone();
    sleep();

    // verify
    assertEquals(Arrays.asList(JobEventType.SCHEDULED, JobEventType.DONE), listener.m_eventTypes);
    assertEquals(Arrays.asList(future, future), listener.m_futures);
  }

  @Test
  public void testLocalListener1() {
    IFuture<Void> future = runJob(500, TimeUnit.MILLISECONDS);

    P_JobChangeListener listener = new P_JobChangeListener();
    m_jobManager.addListener(Jobs.newEventFilter().andMatchEventType(JobEventType.ABOUT_TO_RUN).andMatchFuture(future), listener);

    // verify
    future.awaitDone();
    sleep();

    assertEquals(Arrays.asList(JobEventType.ABOUT_TO_RUN), listener.m_eventTypes);
    assertEquals(Arrays.asList(future), listener.m_futures);
  }

  @Test
  public void testLocalListener2a() {
    IFuture<Void> future1 = runJob(0, TimeUnit.MILLISECONDS);
    future1.awaitDone();

    IFuture<Void> future2 = runJob(500, TimeUnit.MILLISECONDS);
    P_JobChangeListener listener = new P_JobChangeListener();
    m_jobManager.addListener(Jobs.newEventFilter().andMatchFuture(future1, future2), listener);
    future2.awaitDone();
    sleep();

    // verify
    assertEquals(Arrays.asList(JobEventType.ABOUT_TO_RUN, JobEventType.DONE), listener.m_eventTypes);
    assertEquals(Arrays.asList(future2, future2), listener.m_futures);
  }

  @Test
  public void testLocalListener2b() {
    IFuture<Void> future1 = runJob(0, TimeUnit.MILLISECONDS);
    future1.awaitDone();

    IFuture<Void> future2 = runJob(500, TimeUnit.MILLISECONDS);
    P_JobChangeListener listener = new P_JobChangeListener();
    m_jobManager.addListener(Jobs.newEventFilter().andMatchFuture(future2).andMatchFuture(future1), listener);
    future2.awaitDone();
    sleep();

    // verify
    assertEquals(Collections.emptyList(), listener.m_eventTypes);
    assertEquals(Collections.emptyList(), listener.m_futures);
  }

  @Test
  public void testLocalListener3() {
    IFuture<Void> future1 = runJob(0, TimeUnit.MILLISECONDS);
    future1.awaitDone();

    IFuture<Void> future2 = runJob(500, TimeUnit.MILLISECONDS);
    P_JobChangeListener listener = new P_JobChangeListener();
    m_jobManager.addListener(Jobs.newEventFilter().andMatchFuture(future2).andMatch(new NotFilter<>(Jobs.newEventFilter().andMatchFuture(future2))), listener);
    future2.awaitDone();
    sleep();

    // verify
    assertEquals(Collections.emptyList(), listener.m_eventTypes);
    assertEquals(Collections.emptyList(), listener.m_futures);
  }

  @Test
  public void testLocalListener4() {
    IFuture<Void> future = runJob(500, TimeUnit.MILLISECONDS);

    P_JobChangeListener listener = new P_JobChangeListener();
    future.addListener(listener);

    // verify
    future.awaitDone();
    sleep();

    assertEquals(Arrays.asList(JobEventType.ABOUT_TO_RUN, JobEventType.DONE), listener.m_eventTypes);
    assertEquals(Arrays.asList(future, future), listener.m_futures);
  }

  @Test
  public void testLocalListener5() {
    IFuture<Void> future = runJob(500, TimeUnit.MILLISECONDS);

    P_JobChangeListener listener = new P_JobChangeListener();
    future.addListener(Jobs.newEventFilter().andMatchEventType(JobEventType.ABOUT_TO_RUN), listener);

    // verify
    future.awaitDone();
    sleep();

    assertEquals(Arrays.asList(JobEventType.ABOUT_TO_RUN), listener.m_eventTypes);
    assertEquals(Arrays.asList(future), listener.m_futures);
  }

  private static final class P_JobChangeListener implements IJobListener {

    private final List<JobEventType> m_eventTypes = Collections.synchronizedList(new ArrayList<JobEventType>());
    private final List<IFuture<?>> m_futures = Collections.synchronizedList(new ArrayList<IFuture<?>>());

    @Override
    public void changed(JobEvent event) {
      m_eventTypes.add(event.getType());
      m_futures.add(event.getFuture());
    }
  }

  private static final class P_ShutdownListener implements IJobListener {

    private final AtomicBoolean m_shutdown = new AtomicBoolean();

    @Override
    public void changed(JobEvent event) {
      m_shutdown.set(JobEventType.SHUTDOWN == event.getType());
    }
  }

  private IFuture<Void> runJob(long delay, TimeUnit unit) {
    IFuture<Void> future2 = m_jobManager.schedule(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        return null;
      }
    }, delay, unit, Jobs.newInput(null));
    return future2;
  }

  /**
   * Wait until the 'done-event' is fired. That is because {@link FutureTask} releases any waiting thread before
   * {@link FutureTask#done()} is invoked.
   */
  private void sleep() {
    try {
      Thread.sleep(500);
    }
    catch (InterruptedException e) {
      // NOOP
    }
  }

}
