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
import static org.junit.Assert.assertNotSame;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.holders.IntegerHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * JUnit-test to test {@link Job#schedule()}
 */
public class JobScheduleTest {

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
  public void testResult() throws ProcessingException {
    IJob<String> job = new _Job<String>("job") {
      @Override
      protected String call() throws Exception {
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

    IJob<Void> job = new _Job<Void>("job") {

      @Override
      protected Void call() throws Exception {
        Thread.sleep(500);
        holder.setValue("RUNNING_VOID");
        return null;
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

    IJob<String> job = new _Job<String>("job") {
      @Override
      protected String call() throws Exception {
        Thread.sleep(500);
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

    IJob<String> job = new _Job<String>("job") {
      @Override
      protected String call() throws Exception {
        Thread.sleep(500);
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
    final Set<Thread> workerThreads = Collections.synchronizedSet(new HashSet<Thread>()); // synchronized because modified/read by different threads.

    new _Job<Void>("job-1") {

      @Override
      protected Void call() throws Exception {
        workerThreads.add(Thread.currentThread());

        new _Job<Void>("job-2") {

          @Override
          protected Void call() throws Exception {
            workerThreads.add(Thread.currentThread());
            return null;
          }
        }.schedule().get();
        return null;
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

    new _Job<Void>("ABC") {

      @Override
      protected Void call() throws Exception {
        actualThreadName1.setValue(Thread.currentThread().getName());

        new _Job<Void>("XYZ") {

          @Override
          protected Void call() throws Exception {
            actualThreadName2.setValue(Thread.currentThread().getName());
            return null;
          }
        }.schedule().get();
        return null;
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

    IJob.CURRENT.set(null);

    new _Job<Void>("job-1") {

      @Override
      protected Void call() throws Exception {
        job1.setValue(this);
        actualJob1.setValue(IJob.CURRENT.get());

        new _Job<Void>("job-2") {

          @Override
          protected Void call() throws Exception {
            job2.setValue(this);
            actualJob2.setValue(IJob.CURRENT.get());
            return null;
          }
        }.schedule().get();
        return null;
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
    final List<Integer> actualProtocol = Collections.synchronizedList(new ArrayList<Integer>()); // synchronized because modified/read by different threads.
    new _Job<Void>("job") {

      @Override
      protected Void call() throws Exception {
        Thread.sleep(100);
        actualProtocol.add(1);
        return null;
      }
    }.schedule().get();
    actualProtocol.add(2);

    assertEquals(Arrays.asList(1, 2), actualProtocol);
  }

  @Test
  public void testParallelExecution() throws ProcessingException, InterruptedException {
    final CountDownLatch testLatch = new CountDownLatch(3);
    final CountDownLatch parallelRunningLatch = new CountDownLatch(1);

    new _Job<Void>("job-1") {

      @Override
      protected Void call() throws Exception {
        Thread.sleep(100);
        testLatch.countDown();
        parallelRunningLatch.await();
        return null;
      }
    }.schedule();
    new _Job<Void>("job-2") {

      @Override
      protected Void call() throws Exception {
        Thread.sleep(100);
        testLatch.countDown();
        parallelRunningLatch.await();
        return null;
      }
    }.schedule();
    new _Job<Void>("job-3") {

      @Override
      protected Void call() throws Exception {
        Thread.sleep(100);
        testLatch.countDown();
        parallelRunningLatch.await();
        return null;
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
    IJob<String> job = new _Job<String>("job") {
      @Override
      protected String call() throws Exception {
        return "RUN_" + holder.getValue();
      }
    };

    holder.setValue(1);
    assertEquals("RUN_1", job.schedule().get());
    holder.setValue(2);
    assertEquals("RUN_2", job.schedule().get());
  }

  @Test
  public void testJobContext() throws ProcessingException {
    Thread.currentThread().setName("main");

    final Holder<JobContext> actualJobContext1 = new Holder<>();
    final Holder<JobContext> actualJobContext2 = new Holder<>();

    new _Job<Void>("job-1") {

      @Override
      protected Void call() throws Exception {
        JobContext ctx1 = JobContext.CURRENT.get();
        ctx1.set("PROP_JOB1", "J1");
        ctx1.set("PROP_JOB1+JOB2", "SHARED-1");
        actualJobContext1.setValue(ctx1);

        new _Job<Void>("job-2") {

          @Override
          protected Void call() throws Exception {
            JobContext ctx2 = JobContext.CURRENT.get();
            ctx2.set("PROP_JOB2", "J2");
            ctx2.set("PROP_JOB1+JOB2", "SHARED-2");
            actualJobContext2.setValue(ctx2);
            return null;
          }
        }.schedule().get();
        return null;
      }
    }.schedule().get();

    assertNotNull(actualJobContext1.getValue());
    assertNotNull(actualJobContext2.getValue());
    assertNotSame("JobContext should be a copy", actualJobContext1.getValue(), actualJobContext2.getValue());

    assertEquals("J1", actualJobContext1.getValue().get("PROP_JOB1"));
    assertEquals("SHARED-1", actualJobContext1.getValue().get("PROP_JOB1+JOB2"));
    assertNull(actualJobContext1.getValue().get("PROP_JOB2"));

    assertEquals("J1", actualJobContext2.getValue().get("PROP_JOB1"));
    assertEquals("J2", actualJobContext2.getValue().get("PROP_JOB2"));
    assertEquals("SHARED-2", actualJobContext2.getValue().get("PROP_JOB1+JOB2"));
    assertNull(actualJobContext1.getValue().get("JOB2"));

    assertNull(JobContext.CURRENT.get());
  }

  /**
   * Job with a dedicated {@link JobManager} per test-case.
   */
  public abstract class _Job<R> extends Job<R> {

    public _Job(String name) {
      super(name);
    }

    @Override
    protected IJobManager createJobManager() {
      return JobScheduleTest.this.m_jobManager;
    }
  }
}
