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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.Job;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the additional functionality of {@link ClientJob} compared with {@link Job}.
 */
public class ClientJobScheduleTest {

  private JobManager m_jobManager;
  private IClientSession m_clientSession1;
  private IClientSession m_clientSession2;

  @Before
  public void before() {
    m_jobManager = new JobManager();
    m_clientSession1 = mock(IClientSession.class);
    when(m_clientSession1.getLocale()).thenReturn(new Locale("de", "DE"));
    when(m_clientSession1.getTexts()).thenReturn(new ScoutTexts());

    m_clientSession2 = mock(IClientSession.class);
    when(m_clientSession2.getLocale()).thenReturn(new Locale("de", "CH"));
    when(m_clientSession2.getTexts()).thenReturn(new ScoutTexts());
  }

  @After
  public void after() {
    m_jobManager.shutdown();
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

    new ClientJob_<Void>("job-1", m_clientSession1) {

      @Override
      protected void onRunVoid(IProgressMonitor monitor1) throws ProcessingException {
        actualClientSession1.setValue(IClientSession.CURRENT.get());
        actualLocale1.setValue(LocaleThreadLocal.CURRENT.get());
        actualTexts1.setValue(ScoutTexts.CURRENT.get());

        JobContext ctx1 = JobContext.CURRENT.get();
        ctx1.set("PROP_JOB1", "J1");
        ctx1.set("PROP_JOB1+JOB2", "SHARED-1");
        actualJobContext1.setValue(ctx1);

        new ClientJob_<Void>("job-2", m_clientSession2) {

          @Override
          protected void onRunVoid(IProgressMonitor monitor2) throws ProcessingException {
            actualClientSession2.setValue(IClientSession.CURRENT.get());
            actualLocale2.setValue(LocaleThreadLocal.CURRENT.get());
            actualTexts2.setValue(ScoutTexts.CURRENT.get());

            JobContext ctx2 = JobContext.CURRENT.get();
            ctx2.set("PROP_JOB2", "J2");
            ctx2.set("PROP_JOB1+JOB2", "SHARED-2");
            actualJobContext2.setValue(ctx2);
          }
        }.schedule().get();
      }
    }.schedule().get();

    assertSame(m_clientSession1, actualClientSession1.getValue());
    assertSame(m_clientSession2, actualClientSession2.getValue());
    assertNull(IClientSession.CURRENT.get());

    assertSame(m_clientSession1.getLocale(), actualLocale1.getValue());
    assertSame(m_clientSession2.getLocale(), actualLocale2.getValue());
    assertNull(LocaleThreadLocal.CURRENT.get());

    assertSame(m_clientSession1.getTexts(), actualTexts1.getValue());
    assertSame(m_clientSession2.getTexts(), actualTexts2.getValue());
    assertNull(ScoutTexts.CURRENT.get());

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

  /**
   * Job with a dedicated {@link JobManager} per test-case.
   */
  public class ClientJob_<R> extends ClientJob<R> {

    public ClientJob_(String name, IClientSession clientSession) {
      super(name, clientSession);
    }

    @Override
    protected JobManager createJobManager() {
      return ClientJobScheduleTest.this.m_jobManager;
    }
  }
}
