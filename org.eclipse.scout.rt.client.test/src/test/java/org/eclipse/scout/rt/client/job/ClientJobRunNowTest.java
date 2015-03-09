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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.AccessController;
import java.util.Locale;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.ICallable;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.internal.JobManager;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.internal.ClientJobManager;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

/**
 * Tests the additional functionality of {@link ClientJobManager} compared with {@link JobManager}.
 */
@RunWith(PlatformTestRunner.class)
public class ClientJobRunNowTest {

  private IClientJobManager m_jobManager;

  private Subject m_subject1 = new Subject();
  private Subject m_subject2 = new Subject();

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);

    m_jobManager = new ClientJobManager();
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
  public void testClientContext() throws ProcessingException {
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    ScoutTexts.CURRENT.remove();
    UserAgent.CURRENT.remove();
    JobContext.CURRENT.remove();

    final IClientSession clientSession1 = mock(IClientSession.class);
    when(clientSession1.getLocale()).thenReturn(new Locale("de", "DE"));
    when(clientSession1.getTexts()).thenReturn(new ScoutTexts());
    when(clientSession1.getUserAgent()).thenReturn(newUserAgent());

    final IClientSession clientSession2 = mock(IClientSession.class);
    when(clientSession2.getLocale()).thenReturn(new Locale("de", "CH"));
    when(clientSession2.getTexts()).thenReturn(new ScoutTexts());
    when(clientSession2.getUserAgent()).thenReturn(newUserAgent());

    final Holder<ISession> actualClientSession1 = new Holder<>();
    final Holder<ISession> actualClientSession2 = new Holder<>();

    final Holder<Locale> actualLocale1 = new Holder<>();
    final Holder<Locale> actualLocale2 = new Holder<>();

    final Holder<UserAgent> actualUserAgent1 = new Holder<>();
    final Holder<UserAgent> actualUserAgent2 = new Holder<>();

    final Holder<ScoutTexts> actualTexts1 = new Holder<>();
    final Holder<ScoutTexts> actualTexts2 = new Holder<>();

    final Holder<Subject> actualSubject1 = new Holder<>();
    final Holder<Subject> actualSubject2 = new Holder<>();

    m_jobManager.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualClientSession1.setValue(IClientSession.CURRENT.get());
        actualLocale1.setValue(NlsLocale.CURRENT.get());
        actualTexts1.setValue(ScoutTexts.CURRENT.get());
        actualSubject1.setValue(Subject.getSubject(AccessController.getContext()));
        actualUserAgent1.setValue(UserAgent.CURRENT.get());

        m_jobManager.runNow(new IRunnable() {

          @Override
          public void run() throws Exception {
            actualClientSession2.setValue(IClientSession.CURRENT.get());
            actualLocale2.setValue(NlsLocale.CURRENT.get());
            actualTexts2.setValue(ScoutTexts.CURRENT.get());
            actualSubject2.setValue(Subject.getSubject(AccessController.getContext()));
            actualUserAgent2.setValue(UserAgent.CURRENT.get());
          }
        }, ClientJobInput.defaults().session(clientSession2).subject(m_subject2));
      }
    }, ClientJobInput.defaults().session(clientSession1).subject(m_subject1));

    assertSame(clientSession1, actualClientSession1.getValue());
    assertSame(clientSession2, actualClientSession2.getValue());
    assertNull(ISession.CURRENT.get());

    assertSame(clientSession1.getLocale(), actualLocale1.getValue());
    assertSame(clientSession2.getLocale(), actualLocale2.getValue());
    assertNull(NlsLocale.CURRENT.get());

    assertSame(clientSession1.getUserAgent(), actualUserAgent1.getValue());
    assertSame(clientSession2.getUserAgent(), actualUserAgent2.getValue());
    assertNull(UserAgent.CURRENT.get());

    assertSame(clientSession1.getTexts(), actualTexts1.getValue());
    assertSame(clientSession2.getTexts(), actualTexts2.getValue());
    assertNull(ScoutTexts.CURRENT.get());

    assertSame(m_subject1, actualSubject1.getValue());
    assertSame(m_subject2, actualSubject2.getValue());
    assertNull(Subject.getSubject(AccessController.getContext()));
  }

  private static UserAgent newUserAgent() {
    return UserAgent.create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN, "n/a");
  }
}
