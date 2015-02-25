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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.AccessController;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.internal.ClientJobManager;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.platform.ScoutPlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(ScoutPlatformTestRunner.class)
public class ClientJobScheduleTest {

  private IClientJobManager m_jobManager;

  private Subject m_subject1 = new Subject();
  private Subject m_subject2 = new Subject();

  @Mock
  private IClientSession m_clientSession1;
  @Mock
  private IClientSession m_clientSession2;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);

    m_jobManager = new ClientJobManager();

    // initialize ClientSession1
    when(m_clientSession1.getLocale()).thenReturn(new Locale("de", "DE"));
    when(m_clientSession1.getTexts()).thenReturn(new ScoutTexts());

    // initialize ClientSession2
    m_clientSession2 = mock(IClientSession.class);
    when(m_clientSession2.getLocale()).thenReturn(new Locale("de", "CH"));
    when(m_clientSession2.getTexts()).thenReturn(new ScoutTexts());
  }

  @After
  public void after() {
    m_jobManager.shutdown();
  }

  @Test
  public void testMissingSession() throws JobExecutionException {
    ISession.CURRENT.remove(); // ensure no session installed.

    final AtomicReference<Boolean> running = new AtomicReference<Boolean>(false);

    IFuture<Void> future = null;
    try {
      future = m_jobManager.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          running.set(true);
        }
      });
      fail();
    }
    catch (AssertionException e) {
      assertFalse(running.get());
      assertNull(future);
    }
  }

  @Test
  public void testMissingJobInput() throws JobExecutionException {
    final AtomicReference<Boolean> running = new AtomicReference<Boolean>(false);

    IFuture<Void> future = null;
    try {
      future = m_jobManager.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          running.set(true);
        }
      }, null);
      fail();
    }
    catch (AssertionException e) {
      assertFalse(running.get());
      assertNull(future);
    }
  }

  @Test
  public void testClientContext() throws Exception {
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    ScoutTexts.CURRENT.remove();

    final Holder<ISession> actualClientSession1 = new Holder<>();
    final Holder<ISession> actualClientSession2 = new Holder<>();

    final Holder<Locale> actualLocale1 = new Holder<>();
    final Holder<Locale> actualLocale2 = new Holder<>();

    final Holder<ScoutTexts> actualTexts1 = new Holder<>();
    final Holder<ScoutTexts> actualTexts2 = new Holder<>();

    final Holder<Subject> actualSubject1 = new Holder<>();
    final Holder<Subject> actualSubject2 = new Holder<>();

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(2);

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualClientSession1.setValue(IClientSession.CURRENT.get());
        actualLocale1.setValue(NlsLocale.CURRENT.get());
        actualTexts1.setValue(ScoutTexts.CURRENT.get());
        actualSubject1.setValue(Subject.getSubject(AccessController.getContext()));

        setupLatch.countDown();

        m_jobManager.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            actualClientSession2.setValue(IClientSession.CURRENT.get());
            actualLocale2.setValue(NlsLocale.CURRENT.get());
            actualTexts2.setValue(ScoutTexts.CURRENT.get());
            actualSubject2.setValue(Subject.getSubject(AccessController.getContext()));

            setupLatch.countDown();
          }
        }, ClientJobInput.defaults().session(m_clientSession2).subject(m_subject2));
      }
    }, ClientJobInput.defaults().session(m_clientSession1).subject(m_subject1));

    assertTrue(setupLatch.await());

    assertSame(m_clientSession1, actualClientSession1.getValue());
    assertSame(m_clientSession2, actualClientSession2.getValue());
    assertNull(ISession.CURRENT.get());

    assertSame(m_clientSession1.getLocale(), actualLocale1.getValue());
    assertSame(m_clientSession2.getLocale(), actualLocale2.getValue());
    assertNull(NlsLocale.CURRENT.get());

    assertSame(m_clientSession1.getTexts(), actualTexts1.getValue());
    assertSame(m_clientSession2.getTexts(), actualTexts2.getValue());
    assertNull(ScoutTexts.CURRENT.get());

    assertSame(m_subject1, actualSubject1.getValue());
    assertSame(m_subject2, actualSubject2.getValue());
    assertNull(Subject.getSubject(AccessController.getContext()));
  }
}
