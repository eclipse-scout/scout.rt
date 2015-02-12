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
package org.eclipse.scout.commons.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class JobCancelTest {

  private JobManager m_jobManager;
  private static ScheduledExecutorService s_executor;

  @BeforeClass
  public static void beforeClass() {
    s_executor = Executors.newScheduledThreadPool(5);
  }

  @AfterClass
  public static void afterClass() {
    s_executor.shutdown();
  }

  @Before
  public void before() {
    m_jobManager = new JobManager();
  }

  @After
  public void after() {
    m_jobManager.shutdown();
  }

  @Test
  public void testRunNowCancelSoft() throws ProcessingException {
    final List<Boolean> actualProtocol = new ArrayList<>();

    final BooleanHolder actualInterrupted = new BooleanHolder(false);
    final Job<Void> job = new Job_<Void>("job") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        actualProtocol.add(monitor.isCancelled());
        try {
          Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        }
        catch (InterruptedException e) {
          actualInterrupted.setValue(true);
        }
        actualProtocol.add(monitor.isCancelled());
      }
    };

    s_executor.schedule(new Runnable() {

      @Override
      public void run() {
        job.cancel(false /* soft */);
      }
    }, 1, TimeUnit.SECONDS);

    job.runNow();

    assertFalse(actualInterrupted.getValue());
    assertEquals(Arrays.asList(Boolean.FALSE, Boolean.TRUE), actualProtocol);
    assertNull(m_jobManager.getFuture(job));
  }

  @Test
  public void testRunNowCancelForce() throws ProcessingException {
    final List<Boolean> actualProtocol = new ArrayList<>();

    final BooleanHolder actualInterrupted = new BooleanHolder(false);
    final Job<Void> job = new Job_<Void>("job") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        actualProtocol.add(monitor.isCancelled());
        try {
          Thread.sleep(TimeUnit.SECONDS.toMillis(20));
        }
        catch (InterruptedException e) {
          actualInterrupted.setValue(true);
        }
        actualProtocol.add(monitor.isCancelled());
      }
    };

    s_executor.schedule(new Runnable() {

      @Override
      public void run() {
        job.cancel(true /* force */);
      }
    }, 1, TimeUnit.SECONDS);

    job.runNow();

    assertTrue(actualInterrupted.getValue());
    assertEquals(Arrays.asList(Boolean.FALSE, Boolean.TRUE), actualProtocol);
    assertNull(m_jobManager.getFuture(job));
  }

  @Test
  public void testScheduleCancelSoft() throws ProcessingException {
    final List<Boolean> actualProtocol = new ArrayList<>();

    final BooleanHolder actualInterrupted = new BooleanHolder(false);
    final Job<String> job = new Job_<String>("job") {

      @Override
      protected String onRun(IProgressMonitor monitor) throws ProcessingException {
        actualProtocol.add(monitor.isCancelled());
        try {
          Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        }
        catch (InterruptedException e) {
          actualInterrupted.setValue(true);
        }
        actualProtocol.add(monitor.isCancelled());

        return "result";
      }
    };

    @SuppressWarnings("unchecked")
    IAsyncFuture<String> asyncFutureMock = Mockito.mock(IAsyncFuture.class);

    IFuture<String> future = job.schedule(asyncFutureMock);
    _sleep_re(TimeUnit.SECONDS.toMillis(1));
    future.cancel(false /* soft */);
    _sleep_re(TimeUnit.SECONDS.toMillis(3));

    try {
      future.get(1, TimeUnit.SECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      assertFalse(actualInterrupted.getValue());
      assertEquals(Arrays.asList(Boolean.FALSE, Boolean.TRUE), actualProtocol);
      assertNull(m_jobManager.getFuture(job)); // Future not available anymore
      assertFalse(m_jobManager.isCanceled(job)); // cancel=false because Future not available anymore

      assertNotNull(e);
      assertFalse("Future did not return", e.isTimeout());
      assertTrue("Future should be canceled", e.isCancellation());
      assertFalse("Job execution should be interrupted", e.isInterruption());

      verify(asyncFutureMock, times(1)).onDone(eq("result"), isNull(ProcessingException.class));
      verify(asyncFutureMock, times(1)).onSuccess(eq("result"));
      verify(asyncFutureMock, never()).onError(any(ProcessingException.class));
    }
  }

  @Test
  public void testScheduleCancelForce() throws ProcessingException {
    final List<Boolean> actualProtocol = new ArrayList<>();

    final BooleanHolder actualInterrupted = new BooleanHolder(false);
    final Job<String> job = new Job_<String>("job") {

      @Override
      protected String onRun(IProgressMonitor monitor) throws ProcessingException {
        actualProtocol.add(monitor.isCancelled());
        try {
          Thread.sleep(TimeUnit.SECONDS.toMillis(20));
        }
        catch (InterruptedException e) {
          actualInterrupted.setValue(true);
        }
        actualProtocol.add(monitor.isCancelled());

        return "result";
      }
    };

    @SuppressWarnings("unchecked")
    IAsyncFuture<String> asyncFutureMock = Mockito.mock(IAsyncFuture.class);

    IFuture<String> future = job.schedule(asyncFutureMock);
    _sleep_re(TimeUnit.SECONDS.toMillis(2));
    future.cancel(true /* force */);
    _sleep_re(TimeUnit.SECONDS.toMillis(2));

    try {
      future.get(5, TimeUnit.SECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(actualInterrupted.getValue());
      assertEquals(Arrays.asList(Boolean.FALSE, Boolean.TRUE), actualProtocol);
      assertNull(m_jobManager.getFuture(job)); // Future not available anymore
      assertFalse(m_jobManager.isCanceled(job)); // cancel=false because Future not available anymore

      assertNotNull(e);
      assertFalse("Future did not return", e.isTimeout());
      assertTrue("Future should be canceled", e.isCancellation());
      assertFalse("Job execution should be interrupted", e.isInterruption());

      verify(asyncFutureMock, times(1)).onDone(eq("result"), isNull(ProcessingException.class));
      verify(asyncFutureMock, times(1)).onSuccess(eq("result"));
      verify(asyncFutureMock, never()).onError(any(ProcessingException.class));
    }
  }

  @Test
  public void testScheduleCancelBeforeRunning() throws ProcessingException {
    final List<Boolean> actualProtocol = new ArrayList<>();
    final Job<String> job = new Job_<String>("job") {

      @Override
      protected String onRun(IProgressMonitor monitor) throws ProcessingException {
        actualProtocol.add(monitor.isCancelled());
        return "result";
      }
    };

    @SuppressWarnings("unchecked")
    IAsyncFuture<String> asyncFutureMock = Mockito.mock(IAsyncFuture.class);

    IFuture<String> future = job.schedule(200, TimeUnit.MILLISECONDS, asyncFutureMock); // delay execution
    future.cancel(true);
    _sleep_re(500);
    assertEquals(Collections.emptyList(), actualProtocol);

    assertNull(m_jobManager.getFuture(job));

    verify(asyncFutureMock, never()).onDone(anyString(), any(ProcessingException.class));
    verify(asyncFutureMock, never()).onSuccess(anyString());
    verify(asyncFutureMock, never()).onError(any(ProcessingException.class));
  }

  @Test
  public void testCancelPeriodicAction() throws ProcessingException {
    final List<Future<?>> actualProtocol = new ArrayList<>();
    final Job<String> job = new Job_<String>("job") {

      @Override
      protected String onRun(IProgressMonitor monitor) throws ProcessingException {
        actualProtocol.add(m_jobManager.getFuture(this));

        if (actualProtocol.size() == 3) {
          cancel(false);
        }
        return "result";
      }
    };

    IFuture<String> future = job.scheduleAtFixedRate(1, 1, TimeUnit.SECONDS);
    Future<?> f = m_jobManager.getFuture(job);

    try {
      future.get(10, TimeUnit.SECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      assertNotNull(e);
      assertFalse(e.isTimeout());
      assertTrue(e.isCancellation());
      assertEquals(CollectionUtility.arrayList(f, f, f), actualProtocol);
      assertNull(m_jobManager.getFuture(job));
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testShutdownJobManagerAndSchedule() throws ProcessingException {
    final List<String> protocol = new ArrayList<>();

    IJob<Void> job1 = new Job_<Void>("job-1") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("running-1");
        _sleep_pe(Long.MAX_VALUE);
      }
    };
    IJob<Void> job2 = new Job_<Void>("job-2") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("running-2");
        _sleep_pe(Long.MAX_VALUE);
      }
    };
    IJob<Void> job3 = new Job_<Void>("job-3") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        protocol.add("running-3");
        _sleep_pe(Long.MAX_VALUE);
      }
    };

    IAsyncFuture<Void> asyncFutureMock1 = mock(IAsyncFuture.class);
    IAsyncFuture<Void> asyncFutureMock2 = mock(IAsyncFuture.class);
    IAsyncFuture<Void> asyncFutureMock3 = mock(IAsyncFuture.class);

    ArgumentCaptor<ProcessingException> pe1Captor = ArgumentCaptor.forClass(ProcessingException.class);
    doNothing().when(asyncFutureMock1).onError(pe1Captor.capture());
    ArgumentCaptor<ProcessingException> pe2Captor = ArgumentCaptor.forClass(ProcessingException.class);
    doNothing().when(asyncFutureMock2).onError(pe2Captor.capture());
    ArgumentCaptor<ProcessingException> pe3Captor = ArgumentCaptor.forClass(ProcessingException.class);
    doNothing().when(asyncFutureMock3).onError(pe3Captor.capture());

    IFuture<Void> future1 = job1.schedule(asyncFutureMock1);
    IFuture<Void> future2 = job2.schedule(asyncFutureMock2);

    _sleep_re(1000);
    m_jobManager.shutdown();

    JobExecutionException je1 = null;
    try {
      future1.get(5, TimeUnit.SECONDS);
    }
    catch (JobExecutionException e) {
      je1 = e;
    }
    JobExecutionException je2 = null;
    try {
      future2.get(5, TimeUnit.SECONDS);
    }
    catch (JobExecutionException e) {
      je2 = e;
    }
    JobExecutionException je3 = null;
    try {
      job3.schedule();
    }
    catch (JobExecutionException e) {
      je3 = e;
    }

    _sleep_pe(1000);

    assertEquals(Arrays.asList("running-1", "running-2"), protocol);
    assertNull(m_jobManager.getFuture(job1));
    assertNull(m_jobManager.getFuture(job2));
    assertNull(m_jobManager.getFuture(job3));

    assertNotNull(je1);
    assertTrue(je1.isCancellation());
    assertFalse(je1.isInterruption());
    assertFalse(je1.isTimeout());

    verify(asyncFutureMock1, times(1)).onDone(isNull(Void.class), any(ProcessingException.class));
    verify(asyncFutureMock1, never()).onSuccess(any(Void.class));
    verify(asyncFutureMock1, times(1)).onError(any(ProcessingException.class));
    assertNotNull(pe1Captor.getValue());
    assertTrue(pe1Captor.getValue().isInterruption());

    assertNotNull(je2);
    assertTrue(je2.isCancellation());
    assertFalse(je2.isInterruption());
    assertFalse(je2.isTimeout());

    verify(asyncFutureMock2, times(1)).onDone(isNull(Void.class), any(ProcessingException.class));
    verify(asyncFutureMock2, never()).onSuccess(any(Void.class));
    verify(asyncFutureMock2, times(1)).onError(any(ProcessingException.class));
    assertNotNull(pe2Captor.getValue());
    assertTrue(pe2Captor.getValue().isInterruption());

    assertNotNull(je3);
    assertTrue(pe3Captor.getAllValues().isEmpty()); // job not run at all
    assertTrue(je3.isRejection());
    assertFalse(je3.isTimeout());
    assertFalse(je3.isInterruption());
    assertFalse(je3.isCancellation());
    verify(asyncFutureMock3, never()).onDone(any(Void.class), any(ProcessingException.class)); // job not run at all
    verify(asyncFutureMock3, never()).onSuccess(any(Void.class)); // job not run at all
    verify(asyncFutureMock3, never()).onError(any(ProcessingException.class)); // job not run at all
  }

  @Test
  public void testShutdownJobManagerAndRunNow() throws ProcessingException {
    final BooleanHolder executed = new BooleanHolder(false);

    m_jobManager.shutdown();
    Job_<Void> job = new Job_<Void>("job") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        executed.setValue(true);
      }
    };

    job.runNow();

    assertNull(m_jobManager.getFuture(job));
    assertTrue("Job must not be executed if the job-manager is shutdown", executed.getValue());
  }

  private static void _sleep_pe(long millis) throws ProcessingException {
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException e) {
      throw new ProcessingException("interrupted", e);
    }
  }

  private static void _sleep_re(long millis) {
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Job with a dedicated {@link JobManager} per test-case.
   */
  public class Job_<R> extends Job<R> {

    public Job_(String name) {
      super(name);
    }

    @Override
    protected JobManager createJobManager() {
      return JobCancelTest.this.m_jobManager;
    }
  }
}
