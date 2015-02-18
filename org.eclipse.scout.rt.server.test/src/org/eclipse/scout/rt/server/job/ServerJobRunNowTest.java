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
package org.eclipse.scout.rt.server.job;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.AccessController;
import java.util.Locale;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.IJobManager;
import org.eclipse.scout.commons.job.Job;
import org.eclipse.scout.commons.job.JobManager;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.servletfilter.HttpServletRoundtrip;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests the additional functionality of {@link ServerJobWithResult} compared with {@link Job}.
 */
public class ServerJobRunNowTest {

  private IJobManager m_jobManager;
  private IServerSession m_serverSession1;
  private IServerSession m_serverSession2;
  private Subject m_subject1 = new Subject();
  private Subject m_subject2 = new Subject();
  private long m_transactionId1 = 1;
  private long m_transactionId2 = 2;

  @Mock
  private HttpServletRequest m_httpServletRequest;
  @Mock
  private HttpServletResponse m_httpServletResponse;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);

    m_jobManager = new JobManager();
    m_serverSession1 = mock(IServerSession.class);
    when(m_serverSession1.getLocale()).thenReturn(new Locale("de", "DE"));
    when(m_serverSession1.getTexts()).thenReturn(new ScoutTexts());

    m_serverSession2 = mock(IServerSession.class);
    when(m_serverSession2.getLocale()).thenReturn(new Locale("de", "CH"));
    when(m_serverSession2.getTexts()).thenReturn(new ScoutTexts());

    HttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.set(m_httpServletRequest);
    HttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.set(m_httpServletResponse);
  }

  @After
  public void after() {
    m_jobManager.shutdown();
  }

  @Test
  public void testServerContext() throws ProcessingException {
    Thread.currentThread().setName("main");

    final Holder<ISession> actualServerSession1 = new Holder<>();
    final Holder<ISession> actualServerSession2 = new Holder<>();

    final Holder<Locale> actualLocale1 = new Holder<>();
    final Holder<Locale> actualLocale2 = new Holder<>();

    final Holder<ScoutTexts> actualTexts1 = new Holder<>();
    final Holder<ScoutTexts> actualTexts2 = new Holder<>();

    final Holder<ITransaction> actualTransaction1 = new Holder<>();
    final Holder<ITransaction> actualTransaction2 = new Holder<>();

    final Holder<Subject> actualSubject1 = new Holder<>();
    final Holder<Subject> actualSubject2 = new Holder<>();

    final Holder<HttpServletRequest> actualHttpServletRequest1 = new Holder<>();
    final Holder<HttpServletRequest> actualHttpServletRequest2 = new Holder<>();

    final Holder<HttpServletResponse> actualHttpServletResponse1 = new Holder<>();
    final Holder<HttpServletResponse> actualHttpServletResponse2 = new Holder<>();

    new _ServerJob("job-1", m_serverSession1, m_subject1, m_transactionId1) {

      @Override
      protected void run() throws Exception {
        actualServerSession1.setValue(IServerSession.CURRENT.get());
        actualLocale1.setValue(NlsLocale.CURRENT.get());
        actualTexts1.setValue(ScoutTexts.CURRENT.get());
        actualTransaction1.setValue(ITransaction.CURRENT.get());
        actualSubject1.setValue(Subject.getSubject(AccessController.getContext()));
        actualHttpServletRequest1.setValue(HttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get());
        actualHttpServletResponse1.setValue(HttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get());

        new _ServerJob("job-2", m_serverSession2, m_subject2, m_transactionId2) {

          @Override
          protected void run() throws Exception {
            actualServerSession2.setValue(IServerSession.CURRENT.get());
            actualLocale2.setValue(NlsLocale.CURRENT.get());
            actualTexts2.setValue(ScoutTexts.CURRENT.get());
            actualTransaction2.setValue(ITransaction.CURRENT.get());
            actualSubject2.setValue(Subject.getSubject(AccessController.getContext()));
            actualHttpServletRequest2.setValue(HttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get());
            actualHttpServletResponse2.setValue(HttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get());
          }
        }.runNow();
      }
    }.runNow();

    assertSame(m_serverSession1, actualServerSession1.getValue());
    assertSame(m_serverSession2, actualServerSession2.getValue());
    assertNull(IServerSession.CURRENT.get());

    assertSame(m_serverSession1.getLocale(), actualLocale1.getValue());
    assertSame(m_serverSession2.getLocale(), actualLocale2.getValue());
    assertNull(NlsLocale.CURRENT.get());

    assertSame(m_serverSession1.getTexts(), actualTexts1.getValue());
    assertSame(m_serverSession2.getTexts(), actualTexts2.getValue());
    assertNull(ScoutTexts.CURRENT.get());

    assertSame(m_transactionId1, actualTransaction1.getValue().getId());
    assertSame(m_transactionId2, actualTransaction2.getValue().getId());
    assertNull(ITransaction.CURRENT.get());

    assertSame(m_subject1, actualSubject1.getValue());
    assertSame(m_subject2, actualSubject2.getValue());
    assertNull(Subject.getSubject(AccessController.getContext()));

    assertSame(m_httpServletRequest, actualHttpServletRequest1.getValue());
    assertSame(m_httpServletRequest, actualHttpServletRequest2.getValue());
    assertSame(m_httpServletRequest, HttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get());

    assertSame(m_httpServletResponse, actualHttpServletResponse1.getValue());
    assertSame(m_httpServletResponse, actualHttpServletResponse2.getValue());
    assertSame(m_httpServletResponse, HttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get());
  }

  /**
   * Job with a dedicated {@link JobManager} per test-case.
   */
  public abstract class _ServerJob extends ServerJob {

    public _ServerJob(String name, IServerSession serverSession, Subject subject, long transactionId) {
      super(name, serverSession, subject, transactionId);
    }

    @Override
    protected IJobManager createJobManager() {
      return ServerJobRunNowTest.this.m_jobManager;
    }
  }
}
