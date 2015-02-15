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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.commons.job.IAsyncFuture;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ModelJobCancelTest {

  private static ExecutorService s_executor;
  private IClientSession m_clientSession;

  @BeforeClass
  public static void beforeClass() {
    s_executor = Executors.newCachedThreadPool();
  }

  @AfterClass
  public static void afterClass() {
    s_executor.shutdown();
  }

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
  public void testRunNowCancelSoft() throws ProcessingException {
    final List<Boolean> actualProtocol = new ArrayList<>();

    final BooleanHolder actualInterrupted = new BooleanHolder(false);

    final CountDownLatch latch = new CountDownLatch(1);

    final ModelJob<Void> job = new ModelJob<Void>("job", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        actualProtocol.add(monitor.isCancelled());
        latch.countDown();

        try {
          Thread.sleep(1000);
        }
        catch (InterruptedException e) {
          actualInterrupted.setValue(true);
        }
        actualProtocol.add(monitor.isCancelled());
      }
    };

    s_executor.execute(new Runnable() {

      @Override
      public void run() {
        try {
          latch.await();
          Thread.sleep(100);
          job.cancel(false /* soft */);
        }
        catch (InterruptedException e) {
          // NOOP
        }
      }
    });

    simulateToRunInModelThread();
    job.runNow();

    assertFalse(actualInterrupted.getValue());
    assertEquals(Arrays.asList(Boolean.FALSE, Boolean.TRUE), actualProtocol);
    assertNull(job.m_jobManager.getFuture(job));
  }

  @Test
  public void testRunNowCancelForce() throws ProcessingException {
    final List<Boolean> actualProtocol = new ArrayList<>();

    final BooleanHolder actualInterrupted = new BooleanHolder(false);

    final CountDownLatch latch = new CountDownLatch(1);

    final ModelJob<Void> job = new ModelJob<Void>("job", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        actualProtocol.add(monitor.isCancelled());
        latch.countDown();

        try {
          Thread.sleep(TimeUnit.SECONDS.toMillis(30));
        }
        catch (InterruptedException e) {
          actualInterrupted.setValue(true);
        }
        actualProtocol.add(monitor.isCancelled());
      }
    };

    s_executor.execute(new Runnable() {

      @Override
      public void run() {
        try {
          latch.await();
          Thread.sleep(100);
          job.cancel(true /* force */);
        }
        catch (InterruptedException e) {
          // NOOP
        }
      }
    });

    simulateToRunInModelThread();
    job.runNow();

    assertTrue(actualInterrupted.getValue());
    assertEquals(Arrays.asList(Boolean.FALSE, Boolean.TRUE), actualProtocol);
    assertNull(job.m_jobManager.getFuture(job));
  }

  @Test
  public void testScheduleCancelSoft() throws ProcessingException, InterruptedException {
    final List<Boolean> actualProtocol = new ArrayList<>();

    final BooleanHolder actualInterrupted = new BooleanHolder(false);

    final CountDownLatch latch = new CountDownLatch(1);

    final ModelJob<String> job = new ModelJob<String>("job", m_clientSession) {

      @Override
      protected String onRun(IProgressMonitor monitor) throws Exception {
        actualProtocol.add(monitor.isCancelled());

        latch.countDown();

        try {
          Thread.sleep(1000);
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
    latch.await();

    Thread.sleep(100);
    future.cancel(false /* soft */);
    Thread.sleep(100);

    JobExecutionException je = null;
    try {
      future.get(5, TimeUnit.SECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      je = e;
    }

    assertTrue(m_clientSession.getModelJobManager().waitForIdle(10, TimeUnit.SECONDS));

    assertFalse(actualInterrupted.getValue());
    assertEquals(Arrays.asList(Boolean.FALSE, Boolean.TRUE), actualProtocol);
    assertNull(job.m_jobManager.getFuture(job)); // Future not available anymore
    assertFalse(job.m_jobManager.isCanceled(job)); // cancel=false because Future not available anymore

    assertNotNull(je);
    assertFalse("Future did not return", je.isTimeout());
    assertTrue("Future should be canceled", je.isCancellation());
    assertFalse("Job execution should be interrupted", je.isInterruption());

    verify(asyncFutureMock, times(1)).onDone(eq("result"), isNull(ProcessingException.class));
    verify(asyncFutureMock, times(1)).onSuccess(eq("result"));
    verify(asyncFutureMock, never()).onError(any(ProcessingException.class));
  }

  @Test
  public void testScheduleCancelForce() throws ProcessingException, InterruptedException {
    final List<Boolean> actualProtocol = new ArrayList<>();

    final BooleanHolder actualInterrupted = new BooleanHolder(false);

    final CountDownLatch latch = new CountDownLatch(1);

    final ModelJob<String> job = new ModelJob<String>("job", m_clientSession) {

      @Override
      protected String onRun(IProgressMonitor monitor) throws Exception {
        actualProtocol.add(monitor.isCancelled());

        latch.countDown();
        try {
          Thread.sleep(TimeUnit.SECONDS.toMillis(30));
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
    latch.await();

    future.cancel(true /* soft */);
    Thread.sleep(100);
    JobExecutionException je = null;
    try {
      future.get(5, TimeUnit.SECONDS);
      fail();
    }
    catch (JobExecutionException e) {
      je = e;
    }

    assertTrue(actualInterrupted.getValue());
    assertEquals(Arrays.asList(Boolean.FALSE, Boolean.TRUE), actualProtocol);
    assertNull(job.m_jobManager.getFuture(job)); // Future not available anymore
    assertFalse(job.m_jobManager.isCanceled(job)); // cancel=false because Future not available anymore

    assertNotNull(je);
    assertFalse("Future did not return", je.isTimeout());
    assertTrue("Future should be canceled", je.isCancellation());
    assertFalse("Job execution should be interrupted", je.isInterruption());

    verify(asyncFutureMock, times(1)).onDone(eq("result"), isNull(ProcessingException.class));
    verify(asyncFutureMock, times(1)).onSuccess(eq("result"));
    verify(asyncFutureMock, never()).onError(any(ProcessingException.class));
  }

  @Test
  public void testScheduleAndCancelBeforeRunning() throws ProcessingException, InterruptedException {
    // Let this job block the mutex.
    ModelJob<Void> job1 = new ModelJob<Void>("job-1", m_clientSession) {
      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws Exception {
        Thread.sleep(Long.MAX_VALUE);
      }
    };
    IFuture<Void> future1 = job1.schedule();

    final BooleanHolder actualOnRun = new BooleanHolder(false);
    final ModelJob<Void> job2 = new ModelJob<Void>("job", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws Exception {
        actualOnRun.setValue(true);
      }
    };

    @SuppressWarnings("unchecked")
    IAsyncFuture<Void> asyncFutureMock = Mockito.mock(IAsyncFuture.class);

    IFuture<Void> future2 = job2.schedule(asyncFutureMock); // schedule job2 --> will not start running because the mutex-holder is job1.

    assertEquals(future1, m_clientSession.getModelJobManager().getFuture(job1)); // registered
    assertEquals(future2, m_clientSession.getModelJobManager().getFuture(job2)); // registered

    future2.cancel(true); // cancel the future of job2 before running so that it will not be executed (silent rejection by the FutureTask)
    Thread.sleep(100);
    job1.cancel(true); // cancel job1 so that job2 is being scheduled.

    assertTrue(m_clientSession.getModelJobManager().waitForIdle(10, TimeUnit.SECONDS));
    assertTrue(m_clientSession.getModelJobManager().isIdle()); // no pending mutex-jobs anymore
    assertFalse(actualOnRun.getValue()); // not executed
    assertNull(m_clientSession.getModelJobManager().getFuture(job2)); // not registered anymore
    assertNull(m_clientSession.getModelJobManager().getFuture(job2)); // not registered anymore

    verifyZeroInteractions(asyncFutureMock);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testShutdownJobManagerAndSchedule() throws ProcessingException, InterruptedException {
    final List<String> protocol = new ArrayList<>();

    ModelJobManager jobManager = m_clientSession.getModelJobManager();

    ModelJob<Void> job1 = new ModelJob<Void>("job-1", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws Exception {
        protocol.add("running-1");
        Thread.sleep(TimeUnit.SECONDS.toMillis(30));
      }
    };
    ModelJob<Void> job2 = new ModelJob<Void>("job-2", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws Exception {
        protocol.add("running-2");
        Thread.sleep(TimeUnit.SECONDS.toMillis(30));
      }
    };
    ModelJob<Void> job3 = new ModelJob<Void>("job-3", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws Exception {
        protocol.add("running-3");
        Thread.sleep(TimeUnit.SECONDS.toMillis(30));
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
    Thread.sleep(500);
    jobManager.shutdown();
    IFuture<Void> future3 = job3.schedule();

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
      future3.get(5, TimeUnit.SECONDS);
    }
    catch (JobExecutionException e) {
      je3 = e;
    }

    Thread.sleep(TimeUnit.SECONDS.toMillis(1));

    assertEquals(Arrays.asList("running-1"), protocol);

    assertTrue(jobManager.m_mutexSemaphore.isIdle());

    assertNull(jobManager.getFuture(job1));
    assertNull(jobManager.getFuture(job2));
    assertNull(jobManager.getFuture(job3));

    assertNotNull(je1);
    assertNotNull(pe1Captor.getValue());
    assertFalse(je1.isTimeout());
    assertFalse(je1.isInterruption());
    assertTrue(je1.isCancellation());
    assertTrue(pe1Captor.getValue().isInterruption());
    verify(asyncFutureMock1, times(1)).onDone(isNull(Void.class), any(ProcessingException.class));
    verify(asyncFutureMock1, never()).onSuccess(any(Void.class));
    verify(asyncFutureMock1, times(1)).onError(any(ProcessingException.class));

    assertNotNull(je2);
    assertTrue(pe2Captor.getAllValues().isEmpty()); // job not run at all
    assertFalse(je2.isTimeout());
    assertFalse(je2.isInterruption());
    assertTrue(je2.isCancellation());
    verify(asyncFutureMock2, never()).onDone(any(Void.class), any(ProcessingException.class)); // job not run at all
    verify(asyncFutureMock2, never()).onSuccess(any(Void.class)); // job not run at all
    verify(asyncFutureMock2, never()).onError(any(ProcessingException.class)); // job not run at all

    // verify job that was scheduled after shutdown.
    assertNotNull(je3);
    assertTrue(pe3Captor.getAllValues().isEmpty()); // job not run at all
    assertFalse(je3.isTimeout());
    assertFalse(je3.isInterruption());
    assertTrue(je3.isCancellation());
    verify(asyncFutureMock3, never()).onDone(any(Void.class), any(ProcessingException.class)); // job not run at all
    verify(asyncFutureMock3, never()).onSuccess(any(Void.class)); // job not run at all
    verify(asyncFutureMock3, never()).onError(any(ProcessingException.class)); // job not run at all
  }

  @Test
  public void testShutdownJobManagerAndRunNow() throws ProcessingException {
    final BooleanHolder executed = new BooleanHolder(false);

    m_clientSession.getModelJobManager().shutdown();
    ModelJob<Void> job = new ModelJob<Void>("job", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws Exception {
        executed.setValue(true);
      }
    };

    simulateToRunInModelThread();
    job.runNow();

    assertTrue("Job must not be executed if the job-manager is shutdown", executed.getValue());
  }

  private void simulateToRunInModelThread() {
    m_clientSession.getModelJobManager().m_mutexSemaphore.registerAsModelThread();
  }
}
