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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.IJobManager;
import org.eclipse.scout.commons.job.Job;
import org.eclipse.scout.commons.job.JobManager;
import org.eclipse.scout.commons.nls.NlsLocale;
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

  private IJobManager m_jobManager;
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

    new _ClientJob("job-1", m_clientSession1) {

      @Override
      protected void run() throws Exception {
        actualClientSession1.setValue(IClientSession.CURRENT.get());
        actualLocale1.setValue(NlsLocale.CURRENT.get());
        actualTexts1.setValue(ScoutTexts.CURRENT.get());

        new _ClientJob("job-2", m_clientSession2) {

          @Override
          protected void run() throws Exception {
            actualClientSession2.setValue(IClientSession.CURRENT.get());
            actualLocale2.setValue(NlsLocale.CURRENT.get());
            actualTexts2.setValue(ScoutTexts.CURRENT.get());
          }
        }.schedule().get();
      }
    }.schedule().get();

    assertSame(m_clientSession1, actualClientSession1.getValue());
    assertSame(m_clientSession2, actualClientSession2.getValue());
    assertNull(IClientSession.CURRENT.get());

    assertSame(m_clientSession1.getLocale(), actualLocale1.getValue());
    assertSame(m_clientSession2.getLocale(), actualLocale2.getValue());
    assertNull(NlsLocale.CURRENT.get());

    assertSame(m_clientSession1.getTexts(), actualTexts1.getValue());
    assertSame(m_clientSession2.getTexts(), actualTexts2.getValue());
    assertNull(ScoutTexts.CURRENT.get());
  }

  /**
   * Job with a dedicated {@link JobManager} per test-case.
   */
  public abstract class _ClientJob extends ClientJob {

    public _ClientJob(String name, IClientSession clientSession) {
      super(name, clientSession);
    }

    @Override
    protected IJobManager createJobManager() {
      return ClientJobScheduleTest.this.m_jobManager;
    }
  }
}
