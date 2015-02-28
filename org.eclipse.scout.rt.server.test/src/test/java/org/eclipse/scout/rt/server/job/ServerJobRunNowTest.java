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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.ICallable;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.job.internal.JobManager;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.servletfilter.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.job.internal.ServerJobManager;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.platform.ScoutPlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests the additional functionality of {@link ServerJobManager} compared with {@link JobManager}.
 */
@RunWith(ScoutPlatformTestRunner.class)
public class ServerJobRunNowTest {

  private IServerJobManager m_jobManager;

  private Subject m_subject1 = new Subject();
  private Subject m_subject2 = new Subject();

  @Mock
  private HttpServletRequest m_httpServletRequest;
  @Mock
  private HttpServletResponse m_httpServletResponse;

  private List<ITransaction> m_transactions;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);

    m_transactions = new ArrayList<>();
    m_jobManager = new ServerJobManager() {

      @Override
      protected ITransaction createTransaction() {
        ITransaction tx = mock(ITransaction.class);
        m_transactions.add(tx);
        return tx;
      }
    };
  }

  @After
  public void after() {
    m_jobManager.shutdown();
  }

  @Test
  public void testMissingSession() throws ProcessingException {
    ISession.CURRENT.remove(); // ensure no session installed.

    String result = null;
    try {
      result = m_jobManager.runNow(new ICallable<String>() {

        @Override
        public String call() throws Exception {
          return "executed";
        }
      });
      fail();
    }
    catch (AssertionException e) {
      assertNull(result);
    }
  }

  @Test
  public void testMissingJobInput() throws ProcessingException {
    String result = null;
    try {
      result = m_jobManager.runNow(new ICallable<String>() {

        @Override
        public String call() throws Exception {
          return "executed";
        }
      }, null);
      fail();
    }
    catch (AssertionException e) {
      assertNull(result);
    }
  }

  @Test
  public void testServerContext() throws ProcessingException {
    final IServerSession serverSession1 = mock(IServerSession.class);
    when(serverSession1.getTexts()).thenReturn(new ScoutTexts());
    final IServerSession serverSession2 = mock(IServerSession.class);
    when(serverSession2.getTexts()).thenReturn(new ScoutTexts());

    final UserAgent userAgent1 = newUserAgent();
    final UserAgent userAgent2 = newUserAgent();

    ISession.CURRENT.remove();
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.remove();
    IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.remove();
    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    UserAgent.CURRENT.set(userAgent1);
    ScoutTexts.CURRENT.remove();
    ITransaction.CURRENT.remove();

    final Holder<ISession> actualServerSession1 = new Holder<>();
    final Holder<ISession> actualServerSession2 = new Holder<>();

    final Holder<Locale> actualLocale1 = new Holder<>();
    final Holder<Locale> actualLocale2 = new Holder<>();

    final Holder<UserAgent> actualUserAgent1 = new Holder<>();
    final Holder<UserAgent> actualUserAgent2 = new Holder<>();

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

    m_jobManager.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualServerSession1.setValue(IServerSession.CURRENT.get());
        actualLocale1.setValue(NlsLocale.CURRENT.get());
        actualTexts1.setValue(ScoutTexts.CURRENT.get());
        actualTransaction1.setValue(ITransaction.CURRENT.get());
        actualSubject1.setValue(Subject.getSubject(AccessController.getContext()));
        actualHttpServletRequest1.setValue(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get());
        actualHttpServletResponse1.setValue(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get());
        actualUserAgent1.setValue(UserAgent.CURRENT.get());

        m_jobManager.runNow(new IRunnable() {

          @Override
          public void run() throws Exception {
            actualServerSession2.setValue(IServerSession.CURRENT.get());
            actualLocale2.setValue(NlsLocale.CURRENT.get());
            actualTexts2.setValue(ScoutTexts.CURRENT.get());
            actualTransaction2.setValue(ITransaction.CURRENT.get());
            actualSubject2.setValue(Subject.getSubject(AccessController.getContext()));
            actualHttpServletRequest2.setValue(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get());
            actualHttpServletResponse2.setValue(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get());
            actualUserAgent2.setValue(UserAgent.CURRENT.get());
          }
        }, ServerJobInput.defaults().session(serverSession2).locale(null).userAgent(userAgent2).subject(m_subject2));
      }
    }, ServerJobInput.defaults().session(serverSession1).subject(m_subject1).servletRequest(m_httpServletRequest).servletResponse(m_httpServletResponse));

    assertEquals(2, m_transactions.size());

    assertSame(serverSession1, actualServerSession1.getValue());
    assertSame(serverSession2, actualServerSession2.getValue());
    assertNull(ISession.CURRENT.get());

    assertEquals(Locale.CANADA_FRENCH, actualLocale1.getValue());
    assertNull(actualLocale2.getValue());
    assertEquals(Locale.CANADA_FRENCH, NlsLocale.CURRENT.get());

    assertSame(serverSession1.getTexts(), actualTexts1.getValue());
    assertSame(serverSession2.getTexts(), actualTexts2.getValue());
    assertNull(ScoutTexts.CURRENT.get());

    assertSame(m_transactions.get(0), actualTransaction1.getValue());
    assertSame(m_transactions.get(1), actualTransaction2.getValue());
    assertNull(ITransaction.CURRENT.get());

    assertSame(m_subject1, actualSubject1.getValue());
    assertSame(m_subject2, actualSubject2.getValue());
    assertNull(Subject.getSubject(AccessController.getContext()));

    assertSame(m_httpServletRequest, actualHttpServletRequest1.getValue());
    assertSame(m_httpServletRequest, actualHttpServletRequest2.getValue());
    assertNull(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get());

    assertSame(m_httpServletResponse, actualHttpServletResponse1.getValue());
    assertSame(m_httpServletResponse, actualHttpServletResponse2.getValue());
    assertNull(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get());

    assertSame(userAgent1, actualUserAgent1.getValue());
    assertSame(userAgent2, actualUserAgent2.getValue());
    assertSame(userAgent1, UserAgent.CURRENT.get());
  }

  private static UserAgent newUserAgent() {
    return UserAgent.create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN, "n/a");
  }
}
