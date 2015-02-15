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
package org.eclipse.scout.rt.client.job;

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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.holders.IntegerHolder;
import org.eclipse.scout.commons.job.IAsyncFuture;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * JUnit-test to test {@link ModelJob#schedule()}
 */
public class ModelJobScheduleTest {

  private IClientSession m_clientSession;

  @Before
  public void before() {
    m_clientSession = mock(IClientSession.class);
    when(m_clientSession.getLocale()).thenReturn(new Locale("de", "DE"));
    when(m_clientSession.getTexts()).thenReturn(new ScoutTexts());
    when(m_clientSession.getModelJobManager()).thenReturn(new ModelJobManager());
  }

  @After
  public void after() {
    m_clientSession.getModelJobManager().shutdown();
  }

  @Test
  public void testResult() throws ProcessingException {
    ModelJob<String> job = new ModelJob<String>("job", m_clientSession) {
      @Override
      protected String onRun(IProgressMonitor monitor) throws Exception {
        return "RUNNING_WITH_RESULT";
      }
    };

    @SuppressWarnings("unchecked")
    IAsyncFuture<String> asyncFutureMock = Mockito.mock(IAsyncFuture.class);

    String actualResult = job.schedule(asyncFutureMock).get();
    assertEquals("RUNNING_WITH_RESULT", actualResult);

    verify(asyncFutureMock, times(1)).onDone(eq("RUNNING_WITH_RESULT"), isNull(ProcessingException.class));
    verify(asyncFutureMock, times(1)).onSuccess(eq("RUNNING_WITH_RESULT"));
    verify(asyncFutureMock, never()).onError(any(ProcessingException.class));
  }

  @Test
  public void testVoidResult() throws ProcessingException {
    final Holder<String> holder = new Holder<>();

    ModelJob<Void> job = new ModelJob<Void>("job", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws Exception {
        Thread.sleep(500);
        holder.setValue("RUNNING_VOID");
      }
    };

    @SuppressWarnings("unchecked")
    IAsyncFuture<Void> asyncFutureMock = Mockito.mock(IAsyncFuture.class);

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

    ModelJob<String> job = new ModelJob<String>("job", m_clientSession) {
      @Override
      protected String onRun(IProgressMonitor monitor) throws Exception {
        Thread.sleep(500);
        throw expectedException;
      }
    };

    @SuppressWarnings("unchecked")
    IAsyncFuture<String> asyncFutureMock = Mockito.mock(IAsyncFuture.class);

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

    ModelJob<String> job = new ModelJob<String>("job", m_clientSession) {
      @Override
      protected String onRun(IProgressMonitor monitor) throws Exception {
        Thread.sleep(500);
        throw expectedException;
      }
    };

    // Mock AsyncFuture and prepare to capture the exception
    ArgumentCaptor<ProcessingException> actualExceptionCaptor = ArgumentCaptor.forClass(ProcessingException.class);
    @SuppressWarnings("unchecked")
    IAsyncFuture<String> asyncFutureMock = Mockito.mock(IAsyncFuture.class);
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
  public void testWorkerThread() throws ProcessingException, InterruptedException {
    final Set<Thread> workerThreads = new HashSet<Thread>();

    final CountDownLatch latch = new CountDownLatch(2);

    new ModelJob<Void>("job-1", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor1) throws Exception {
        workerThreads.add(Thread.currentThread());
        latch.countDown();

        new ModelJob<Void>("job-2", m_clientSession) {

          @Override
          protected void onRunVoid(IProgressMonitor monitor2) throws Exception {
            workerThreads.add(Thread.currentThread());
            latch.countDown();
          }
        }.schedule();
      }
    }.schedule();

    latch.await();

    assertEquals(2, workerThreads.size());
    assertFalse(workerThreads.contains(Thread.currentThread()));
  }

  @Test
  public void testThreadName() throws ProcessingException, InterruptedException {
    Thread.currentThread().setName("main");

    final Holder<String> actualThreadName1 = new Holder<>();
    final Holder<String> actualThreadName2 = new Holder<>();

    final CountDownLatch latch = new CountDownLatch(2);

    new ModelJob<Void>("ABC", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor1) throws Exception {
        actualThreadName1.setValue(Thread.currentThread().getName());
        latch.countDown();

        new ModelJob<Void>("XYZ", m_clientSession) {

          @Override
          protected void onRunVoid(IProgressMonitor monitor2) throws Exception {
            actualThreadName2.setValue(Thread.currentThread().getName());
            latch.countDown();
          }
        }.schedule();
      }
    }.schedule();

    latch.await();

    assertTrue(actualThreadName1.getValue().matches("thread:scout-model-(\\d)+;job:ABC"));
    assertTrue(actualThreadName2.getValue().matches("thread:scout-model-(\\d)+;job:XYZ"));
    assertEquals("main", Thread.currentThread().getName());
  }

  @Test
  public void testCurrentJob() throws ProcessingException, InterruptedException {
    final Holder<ModelJob<?>> job1 = new Holder<>();
    final Holder<ModelJob<?>> job2 = new Holder<>();

    final Holder<ModelJob<?>> actualJob1 = new Holder<>();
    final Holder<ModelJob<?>> actualJob2 = new Holder<>();

    final CountDownLatch latch = new CountDownLatch(2);

    ModelJob.CURRENT.set(null);

    new ModelJob<Void>("job-1", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor1) throws Exception {
        job1.setValue(this);
        actualJob1.setValue(ModelJob.get());
        latch.countDown();

        new ModelJob<Void>("job-2", m_clientSession) {

          @Override
          protected void onRunVoid(IProgressMonitor monitor2) throws Exception {
            job2.setValue(this);
            actualJob2.setValue(ModelJob.get());
            latch.countDown();
          }
        }.schedule();
      }
    }.schedule();

    latch.await();

    assertNotNull(job1.getValue());
    assertNotNull(job2.getValue());

    assertSame(job1.getValue(), actualJob1.getValue());
    assertSame(job2.getValue(), actualJob2.getValue());

    assertNull(ModelJob.CURRENT.get());
  }

  @Test
  public void testScheduleAndGet() throws ProcessingException {
    final List<Integer> actualProtocol = new ArrayList<>();
    new ModelJob<Void>("job", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws Exception {
        Thread.sleep(100);
        actualProtocol.add(1);
      }
    }.schedule().get();
    actualProtocol.add(2);

    assertEquals(Arrays.asList(1, 2), actualProtocol);
  }

  @Test
  public void testReusable() throws ProcessingException {
    final IntegerHolder holder = new IntegerHolder();
    ModelJob<String> job = new ModelJob<String>("job", m_clientSession) {
      @Override
      protected String onRun(IProgressMonitor monitor) throws Exception {
        return "RUN_" + holder.getValue();
      }
    };

    holder.setValue(1);
    assertEquals("RUN_1", job.schedule().get());
    holder.setValue(2);
    assertEquals("RUN_2", job.schedule().get());
  }

  @Test
  public void testJobContext() throws ProcessingException, InterruptedException {
    Thread.currentThread().setName("main");

    final Holder<ISession> actualClientSession1 = new Holder<>();
    final Holder<ISession> actualClientSession2 = new Holder<>();

    final Holder<Locale> actualLocale1 = new Holder<>();
    final Holder<Locale> actualLocale2 = new Holder<>();

    final Holder<ScoutTexts> actualTexts1 = new Holder<>();
    final Holder<ScoutTexts> actualTexts2 = new Holder<>();

    final Holder<JobContext> actualJobContext1 = new Holder<>();
    final Holder<JobContext> actualJobContext2 = new Holder<>();

    new ModelJob<Void>("job-1", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor1) throws Exception {
        actualClientSession1.setValue(IClientSession.CURRENT.get());
        actualLocale1.setValue(NlsLocale.CURRENT.get());
        actualTexts1.setValue(ScoutTexts.CURRENT.get());

        JobContext ctx1 = JobContext.CURRENT.get();
        ctx1.set("PROP_JOB1", "J1");
        ctx1.set("PROP_JOB1+JOB2", "SHARED-1");
        actualJobContext1.setValue(ctx1);

        new ModelJob<Void>("job-2", m_clientSession) {

          @Override
          protected void onRunVoid(IProgressMonitor monitor2) throws Exception {
            actualClientSession2.setValue(IClientSession.CURRENT.get());
            actualLocale2.setValue(NlsLocale.CURRENT.get());
            actualTexts2.setValue(ScoutTexts.CURRENT.get());

            JobContext ctx2 = JobContext.CURRENT.get();
            ctx2.set("PROP_JOB2", "J2");
            ctx2.set("PROP_JOB1+JOB2", "SHARED-2");
            actualJobContext2.setValue(ctx2);
          }
        }.schedule();
      }
    }.schedule().get();

    assertTrue(m_clientSession.getModelJobManager().waitForIdle(10, TimeUnit.SECONDS));

    assertSame(m_clientSession, actualClientSession1.getValue());
    assertSame(m_clientSession, actualClientSession2.getValue());
    assertNull(IClientSession.CURRENT.get());

    assertSame(m_clientSession.getLocale(), actualLocale1.getValue());
    assertSame(m_clientSession.getLocale(), actualLocale2.getValue());
    assertNull(NlsLocale.CURRENT.get());

    assertSame(m_clientSession.getTexts(), actualTexts1.getValue());
    assertSame(m_clientSession.getTexts(), actualTexts2.getValue());
    assertNull(ScoutTexts.CURRENT.get());

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
}
