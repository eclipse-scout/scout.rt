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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit-test to test {@link ModelJob#runNow()}
 */
public class ModelJobRunNowTest {

  private IClientSession m_clientSession;
  private ModelJobManager m_jobManager;

  @Before
  public void before() {
    m_jobManager = new ModelJobManager();
    m_clientSession = mock(IClientSession.class);
    when(m_clientSession.getLocale()).thenReturn(new Locale("de", "DE"));
    when(m_clientSession.getTexts()).thenReturn(new ScoutTexts());
    when(m_clientSession.getModelJobManager()).thenReturn(new ModelJobManager());
    when(m_clientSession.getModelJobManager()).thenReturn(m_jobManager);

  }

  @After
  public void after() {
    m_jobManager.shutdown();
  }

  @Test(expected = JobExecutionException.class)
  public void testRunNowNotModelThread() throws ProcessingException {
    new ModelJob("job", m_clientSession) {

      @Override
      protected void run() throws Exception {
        // NOOP
      }
    }.runNow();
  }

  @Test
  public void testResult() throws ProcessingException {
    simulateToRunInModelThread();

    IModelJob<String> job = new ModelJobWithResult<String>("job", m_clientSession) {

      @Override
      protected String call() throws Exception {
        return "RUNNING_WITH_RESULT";
      }
    };
    String actualResult = job.runNow();

    assertEquals("RUNNING_WITH_RESULT", actualResult);
  }

  @Test
  public void testVoidResult() throws ProcessingException {
    simulateToRunInModelThread();

    final Holder<String> holder = new Holder<>();

    ModelJob job = new ModelJob("job", m_clientSession) {

      @Override
      protected void run() throws Exception {
        holder.setValue("RUNNING_VOID");
      }
    };
    Void actualResult = job.runNow();

    assertNull(actualResult);
    assertEquals("RUNNING_VOID", holder.getValue());
  }

  @Test
  public void testProcessingException() throws ProcessingException {
    simulateToRunInModelThread();

    final ProcessingException expectedException = new ProcessingException();

    ModelJob job = new ModelJob("job", m_clientSession) {
      @Override
      protected void run() throws Exception {
        throw expectedException;
      }
    };

    try {
      job.runNow();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertSame(expectedException, e);
    }
  }

  @Test
  public void testRuntimeException() throws ProcessingException {
    simulateToRunInModelThread();

    final RuntimeException expectedException = new RuntimeException();

    ModelJob job = new ModelJob("job", m_clientSession) {
      @Override
      protected void run() throws Exception {
        throw expectedException;
      }
    };

    try {
      job.runNow();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessingException);
      assertSame(expectedException, e.getCause());
    }
  }

  @Test
  public void testSameThread() throws ProcessingException {
    simulateToRunInModelThread();

    final Set<Thread> threads = Collections.synchronizedSet(new HashSet<Thread>()); // synchronized because modified/read by different threads.

    new ModelJob("job-1", m_clientSession) {

      @Override
      protected void run() throws Exception {
        threads.add(Thread.currentThread());

        new ModelJob("job-2", m_clientSession) {

          @Override
          protected void run() throws Exception {
            threads.add(Thread.currentThread());
          }
        }.runNow();
      }
    }.runNow();

    assertEquals(1, threads.size());
    assertTrue(threads.contains(Thread.currentThread()));
  }

  @Test
  public void testThreadName() throws ProcessingException {
    simulateToRunInModelThread();
    Thread.currentThread().setName("scout-model");

    final Holder<String> actualThreadName1 = new Holder<>();
    final Holder<String> actualThreadName2 = new Holder<>();

    new ModelJob("ABC", m_clientSession) {

      @Override
      protected void run() throws Exception {
        actualThreadName1.setValue(Thread.currentThread().getName());

        new ModelJob("XYZ", m_clientSession) {

          @Override
          protected void run() throws Exception {
            actualThreadName2.setValue(Thread.currentThread().getName());
          }
        }.runNow();
      }
    }.runNow();

    assertEquals("thread:scout-model;job:ABC", actualThreadName1.getValue());
    assertEquals("thread:scout-model;job:XYZ", actualThreadName2.getValue());
    assertEquals("scout-model", Thread.currentThread().getName());
  }

  @Test
  public void testCurrentJob() throws ProcessingException {
    simulateToRunInModelThread();
    final Holder<IJob<?>> job1 = new Holder<>();
    final Holder<IJob<?>> job2 = new Holder<>();

    final Holder<IJob<?>> actualJob1 = new Holder<>();
    final Holder<IJob<?>> actualJob2 = new Holder<>();

    IJob.CURRENT.set(null);

    new ModelJob("job-1", m_clientSession) {

      @Override
      protected void run() throws Exception {
        job1.setValue(this);
        actualJob1.setValue(IJob.CURRENT.get());

        new ModelJob("job-2", m_clientSession) {

          @Override
          protected void run() throws Exception {
            job2.setValue(this);
            actualJob2.setValue(IJob.CURRENT.get());
          }
        }.runNow();
      }
    }.runNow();

    assertNotNull(job1.getValue());
    assertNotNull(job2.getValue());

    assertSame(job1.getValue(), actualJob1.getValue());
    assertSame(job2.getValue(), actualJob2.getValue());

    assertNull(ModelJob.CURRENT.get());
  }

  @Test
  public void testBlocking() throws ProcessingException {
    simulateToRunInModelThread();

    final List<Integer> actualProtocol = Collections.synchronizedList(new ArrayList<Integer>()); // synchronized because modified/read by different threads.

    new ModelJob("job", m_clientSession) {

      @Override
      protected void run() throws Exception {
        actualProtocol.add(1);
      }
    }.runNow();
    actualProtocol.add(2);

    assertEquals(Arrays.asList(1, 2), actualProtocol);
  }

  @Test
  public void testJobContext() throws ProcessingException {
    Thread.currentThread().setName("main");

    final Holder<ISession> actualClientSession1 = new Holder<>();
    final Holder<ISession> actualClientSession2 = new Holder<>();

    final Holder<Locale> actualLocale1 = new Holder<>();
    final Holder<Locale> actualLocale2 = new Holder<>();

    final Holder<ScoutTexts> actualTexts1 = new Holder<>();
    final Holder<ScoutTexts> actualTexts2 = new Holder<>();

    final Holder<JobContext> actualJobContext1 = new Holder<>();
    final Holder<JobContext> actualJobContext2 = new Holder<>();

    simulateToRunInModelThread();

    new ModelJob("job-1", m_clientSession) {

      @Override
      protected void run() throws Exception {
        actualClientSession1.setValue(IClientSession.CURRENT.get());
        actualLocale1.setValue(NlsLocale.CURRENT.get());
        actualTexts1.setValue(ScoutTexts.CURRENT.get());

        JobContext ctx1 = JobContext.CURRENT.get();
        ctx1.set("PROP_JOB1", "J1");
        ctx1.set("PROP_JOB1+JOB2", "SHARED-1");
        actualJobContext1.setValue(ctx1);

        new ModelJob("job-2", m_clientSession) {

          @Override
          protected void run() throws Exception {
            actualClientSession2.setValue(IClientSession.CURRENT.get());
            actualLocale2.setValue(NlsLocale.CURRENT.get());
            actualTexts2.setValue(ScoutTexts.CURRENT.get());

            JobContext ctx2 = JobContext.CURRENT.get();
            ctx2.set("PROP_JOB2", "J2");
            ctx2.set("PROP_JOB1+JOB2", "SHARED-2");
            actualJobContext2.setValue(ctx2);
          }
        }.runNow();
      }
    }.runNow();

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

  private void simulateToRunInModelThread() {
    m_jobManager.m_mutexSemaphore.registerAsModelThread();
  }
}
