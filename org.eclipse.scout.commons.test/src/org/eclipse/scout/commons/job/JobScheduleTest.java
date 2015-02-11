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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.holders.IntegerHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * JUnit-test to test {@link Job#schedule()}
 */
public class JobScheduleTest {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JobScheduleTest.class);

  private JobManager m_jobManager;

  @Before
  public void before() {
    m_jobManager = new JobManager();
  }

  @After
  public void after() {
    m_jobManager.shutdown();
  }

  @Test
  public void testResult() throws ProcessingException {
    IJob<String> job = new Job_<String>("job") {
      @Override
      protected String onRun(IProgressMonitor monitor) throws ProcessingException {
        return "RUNNING_WITH_RESULT";
      }
    };

    @SuppressWarnings("unchecked")
    IAsyncFuture<String> asyncFutureMock = mock(IAsyncFuture.class);

    String actualResult = job.schedule(asyncFutureMock).get();
    assertEquals("RUNNING_WITH_RESULT", actualResult);

    verify(asyncFutureMock, times(1)).onDone(eq("RUNNING_WITH_RESULT"), isNull(ProcessingException.class));
    verify(asyncFutureMock, times(1)).onSuccess(eq("RUNNING_WITH_RESULT"));
    verify(asyncFutureMock, never()).onError(any(ProcessingException.class));
  }

  @Test
  public void testVoidResult() throws ProcessingException {
    final Holder<String> holder = new Holder<>();

    IJob<Void> job = new Job_<Void>("job") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        _sleep(500);
        holder.setValue("RUNNING_VOID");
      }
    };

    @SuppressWarnings("unchecked")
    IAsyncFuture<Void> asyncFutureMock = mock(IAsyncFuture.class);

    IFuture<Void> future = job.schedule(asyncFutureMock);
    assertFalse(future.isDone());
    Void actualResult = future.get();
    assertTrue(future.isDone());

    assertNull(actualResult);
    assertEquals("RUNNING_VOID", holder.getValue());

    verify(asyncFutureMock, times(1)).onDone(isNull(Void.class), isNull(ProcessingException.class));
    verify(asyncFutureMock, times(1)).onSuccess(isNull(Void.class));
    verify(asyncFutureMock, never()).onError(any(ProcessingException.class));
  }

  @Test
  public void testProcessingException() throws ProcessingException {
    final ProcessingException expectedException = new ProcessingException();

    IJob<String> job = new Job_<String>("job") {
      @Override
      protected String onRun(IProgressMonitor monitor) throws ProcessingException {
        _sleep(500);
        throw expectedException;
      }
    };

    @SuppressWarnings("unchecked")
    IAsyncFuture<String> asyncFutureMock = mock(IAsyncFuture.class);

    IFuture<String> future = job.schedule(asyncFutureMock);
    try {
      assertFalse(future.isDone());
      future.get();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertSame(expectedException, e);
    }
    finally {
      assertTrue(future.isDone());
    }

    assertTrue(future.isDone());

    verify(asyncFutureMock, times(1)).onDone(isNull(String.class), same(expectedException));
    verify(asyncFutureMock, never()).onSuccess(anyString());
    verify(asyncFutureMock, times(1)).onError(same(expectedException));
  }

  @Test
  public void testRuntimeException() throws ProcessingException {
    final RuntimeException expectedException = new RuntimeException();

    IJob<String> job = new Job_<String>("job") {
      @Override
      protected String onRun(IProgressMonitor monitor) throws ProcessingException {
        _sleep(500);
        throw expectedException;
      }
    };

    // Mock AsyncFuture and prepare to capture the exception
    ArgumentCaptor<ProcessingException> actualExceptionCaptor = ArgumentCaptor.forClass(ProcessingException.class);
    @SuppressWarnings("unchecked")
    IAsyncFuture<String> asyncFutureMock = mock(IAsyncFuture.class);
    doNothing().when(asyncFutureMock).onError(actualExceptionCaptor.capture());

    IFuture<String> future = job.schedule(asyncFutureMock);
    try {
      assertFalse(future.isDone());
      future.get();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessingException);
      assertSame(expectedException, e.getCause());
    }
    finally {
      assertTrue(future.isDone());
    }

    assertTrue(future.isDone());

    verify(asyncFutureMock, times(1)).onDone(isNull(String.class), any(ProcessingException.class));
    verify(asyncFutureMock, never()).onSuccess(anyString());
    verify(asyncFutureMock, times(1)).onError(any(ProcessingException.class));
    assertSame(expectedException, actualExceptionCaptor.getValue().getCause());
  }

  @Test
  public void testWorkerThread() throws ProcessingException {
    final Set<Thread> workerThreads = new HashSet<Thread>();

    new Job_<Void>("job-1") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor1) throws ProcessingException {
        workerThreads.add(Thread.currentThread());

        new Job_<Void>("job-2") {

          @Override
          protected void onRunVoid(IProgressMonitor monitor2) throws ProcessingException {
            workerThreads.add(Thread.currentThread());
          }
        }.schedule().get();
      }
    }.schedule().get();

    assertEquals(2, workerThreads.size());
    assertFalse(workerThreads.contains(Thread.currentThread()));
  }

  @Test
  public void testThreadName() throws ProcessingException {
    Thread.currentThread().setName("main");

    final Holder<String> actualThreadName1 = new Holder<>();
    final Holder<String> actualThreadName2 = new Holder<>();

    new Job_<Void>("ABC") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor1) throws ProcessingException {
        actualThreadName1.setValue(Thread.currentThread().getName());

        new Job_<Void>("XYZ") {

          @Override
          protected void onRunVoid(IProgressMonitor monitor2) throws ProcessingException {
            actualThreadName2.setValue(Thread.currentThread().getName());
          }
        }.schedule().get();
      }
    }.schedule().get();

    assertTrue(actualThreadName1.getValue().matches("thread:scout-(\\d)+;job:ABC"));
    assertTrue(actualThreadName2.getValue().matches("thread:scout-(\\d)+;job:XYZ"));
    assertEquals("main", Thread.currentThread().getName());
  }

  @Test
  public void testCurrentJob() throws ProcessingException {
    final Holder<IJob<?>> job1 = new Holder<>();
    final Holder<IJob<?>> job2 = new Holder<>();

    final Holder<IJob<?>> actualJob1 = new Holder<>();
    final Holder<IJob<?>> actualJob2 = new Holder<>();

    new Job_<Void>("job-1") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor1) throws ProcessingException {
        job1.setValue(this);
        actualJob1.setValue(Job.get());

        new Job_<Void>("job-2") {

          @Override
          protected void onRunVoid(IProgressMonitor monitor2) throws ProcessingException {
            job2.setValue(this);
            actualJob2.setValue(Job.get());
          }
        }.schedule().get();
      }
    }.schedule().get();

    assertNotNull(job1.getValue());
    assertNotNull(job2.getValue());

    assertSame(job1.getValue(), actualJob1.getValue());
    assertSame(job2.getValue(), actualJob2.getValue());

    assertNull(IJob.CURRENT.get());
  }

  @Test
  public void testScheduleAndGet() throws ProcessingException {
    final List<Integer> actualProtocol = new ArrayList<>();
    new Job_<Void>("job") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        _sleep(100);
        actualProtocol.add(1);
      }
    }.schedule().get();
    actualProtocol.add(2);

    assertEquals(Arrays.asList(1, 2), actualProtocol);
  }

  @Test
  public void testParallelExecution() throws ProcessingException, InterruptedException {
    final CountDownLatch testLatch = new CountDownLatch(3);
    final CountDownLatch parallelRunningLatch = new CountDownLatch(1);

    new Job_<Void>("job-1") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        _sleep(100);
        testLatch.countDown();
        try {
          parallelRunningLatch.await();
        }
        catch (InterruptedException e) {
          // NOOP
        }
      }
    }.schedule();
    new Job_<Void>("job-2") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        _sleep(100);
        testLatch.countDown();
        try {
          parallelRunningLatch.await();
        }
        catch (InterruptedException e) {
          // NOOP
        }
      }
    }.schedule();
    new Job_<Void>("job-3") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        _sleep(100);
        testLatch.countDown();
        try {
          parallelRunningLatch.await();
        }
        catch (InterruptedException e) {
          // NOOP
        }
      }
    }.schedule();

    assertEquals(3, testLatch.getCount());

    // Wait for the jobs to wait in parallel.
    try {
      if (!testLatch.await(5, TimeUnit.SECONDS)) {
        fail("Jobs not running in parallel");
      }
    }
    finally {
      parallelRunningLatch.countDown();
    }
  }

  @Test
  public void testReusable() throws ProcessingException {
    final IntegerHolder holder = new IntegerHolder();
    IJob<String> job = new Job_<String>("job") {
      @Override
      protected String onRun(IProgressMonitor monitor) throws ProcessingException {
        return "RUN_" + holder.getValue();
      }
    };

    holder.setValue(1);
    assertEquals("RUN_1", job.schedule().get());
    holder.setValue(2);
    assertEquals("RUN_2", job.schedule().get());
  }

  private static void _sleep(long millis) {
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException e) {
      LOG.warn("Interrupted while sleeping", e);
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
      return JobScheduleTest.this.m_jobManager;
    }
  }
}
