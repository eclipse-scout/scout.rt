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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TextsThreadLocal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit-test to test {@link ModelJob#runNow()}
 */
public class ModelJobRunNowTest {

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

  @Test(expected = JobExecutionException.class)
  public void testRunNowNotModelThread() throws ProcessingException {
    new ModelJob<Void>("job", m_clientSession).runNow();
  }

  @Test
  public void testResult() throws ProcessingException {
    simulateToRunInModelThread();

    ModelJob<String> job = new ModelJob<String>("job", m_clientSession) {
      @Override
      protected String onRun(IProgressMonitor monitor) throws ProcessingException {
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

    ModelJob<Void> job = new ModelJob<Void>("job", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
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

    ModelJob<String> job = new ModelJob<String>("job", m_clientSession) {
      @Override
      protected String onRun(IProgressMonitor monitor) throws ProcessingException {
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

    ModelJob<String> job = new ModelJob<String>("job", m_clientSession) {
      @Override
      protected String onRun(IProgressMonitor monitor) throws ProcessingException {
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

    final Set<Thread> threads = new HashSet<Thread>();

    new ModelJob<Void>("job-1", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor1) throws ProcessingException {
        threads.add(Thread.currentThread());

        new ModelJob<Void>("job-2", m_clientSession) {

          @Override
          protected void onRunVoid(IProgressMonitor monitor2) throws ProcessingException {
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

    new ModelJob<Void>("ABC", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor1) throws ProcessingException {
        actualThreadName1.setValue(Thread.currentThread().getName());

        new ModelJob<Void>("XYZ", m_clientSession) {

          @Override
          protected void onRunVoid(IProgressMonitor monitor2) throws ProcessingException {
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

    final Holder<ModelJob<?>> job1 = new Holder<>();
    final Holder<ModelJob<?>> job2 = new Holder<>();

    final Holder<ModelJob<?>> actualJob1 = new Holder<>();
    final Holder<ModelJob<?>> actualJob2 = new Holder<>();

    new ModelJob<Void>("job-1", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor1) throws ProcessingException {
        job1.setValue(this);
        actualJob1.setValue(ModelJob.get());

        new ModelJob<Void>("job-2", m_clientSession) {

          @Override
          protected void onRunVoid(IProgressMonitor monitor2) throws ProcessingException {
            job2.setValue(this);
            actualJob2.setValue(ModelJob.get());
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

    final List<Integer> actualProtocol = new ArrayList<>();

    new ModelJob<Void>("job", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        actualProtocol.add(1);
      }
    }.runNow();
    actualProtocol.add(2);

    assertEquals(Arrays.asList(1, 2), actualProtocol);
  }

  @Test
  public void testClientContext() throws ProcessingException {
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

    new ModelJob<Void>("job-1", m_clientSession) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor1) throws ProcessingException {
        actualClientSession1.setValue(IClientSession.CURRENT.get());
        actualLocale1.setValue(LocaleThreadLocal.get());
        actualTexts1.setValue(TextsThreadLocal.get());

        JobContext ctx1 = JobContext.CURRENT.get();
        ctx1.set("PROP_JOB1", "J1");
        ctx1.set("PROP_JOB1+JOB2", "SHARED-1");
        actualJobContext1.setValue(ctx1);

        new ModelJob<Void>("job-2", m_clientSession) {

          @Override
          protected void onRunVoid(IProgressMonitor monitor2) throws ProcessingException {
            actualClientSession2.setValue(IClientSession.CURRENT.get());
            actualLocale2.setValue(LocaleThreadLocal.get());
            actualTexts2.setValue(TextsThreadLocal.get());

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
    assertSame(Locale.getDefault(), LocaleThreadLocal.get());

    assertSame(m_clientSession.getTexts(), actualTexts1.getValue());
    assertSame(m_clientSession.getTexts(), actualTexts2.getValue());
    assertNull(TextsThreadLocal.get());

    assertNotNull(actualJobContext1.getValue());
    assertNotNull(actualJobContext2.getValue());
    assertNotSame("ClientJobContex should be a copy", actualJobContext1.getValue(), actualJobContext2.getValue());

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
    m_clientSession.getModelJobManager().m_mutexSemaphore.registerAsModelThread();
  }
}
