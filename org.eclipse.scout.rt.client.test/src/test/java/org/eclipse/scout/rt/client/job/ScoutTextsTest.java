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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.internal.ClientJobManager;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.testing.platform.ScoutPlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutPlatformTestRunner.class)
public class ScoutTextsTest {

  private IClientJobManager m_jobManager;

  @Before
  public void before() {
    m_jobManager = ClientJobManager.DEFAULT;
  }

  @After
  public void after() {
    m_jobManager.shutdown();
  }

  /**
   * Session not available; ThreadLocal null;
   */
  @Test
  public void testScoutTexts1() throws ProcessingException {
    ScoutTexts.CURRENT.remove();

    m_jobManager.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertNull(ScoutTexts.CURRENT.get());
      }
    }, ClientJobInput.defaults().session(null).sessionRequired(false));
  }

  /**
   * Session not available; ThreadLocal set;
   */
  @Test
  public void testScoutTexts2() throws ProcessingException {
    final ScoutTexts scoutTextsThreadLocal = new ScoutTexts();
    ScoutTexts.CURRENT.set(scoutTextsThreadLocal);

    m_jobManager.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertSame(scoutTextsThreadLocal, ScoutTexts.CURRENT.get());
      }
    }, ClientJobInput.defaults().session(null).sessionRequired(false));
  }

  /**
   * Session available with TEXTS; ThreadLocal set;
   */
  @Test
  public void testScoutTexts3() throws ProcessingException {
    final ScoutTexts scoutTextsThreadLocal = new ScoutTexts();
    final ScoutTexts scoutTextsSession = new ScoutTexts();
    ScoutTexts.CURRENT.set(scoutTextsThreadLocal);

    IClientSession session = mock(IClientSession.class);
    when(session.getTexts()).thenReturn(scoutTextsSession);
    ISession.CURRENT.set(session);

    m_jobManager.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertSame(scoutTextsSession, ScoutTexts.CURRENT.get());
      }
    }, ClientJobInput.defaults().sessionRequired(false));
  }

  /**
   * Session available without TEXTS; ThreadLocal set;
   */
  @Test
  public void testScoutTexts4() throws ProcessingException {
    final ScoutTexts scoutTextsThreadLocal = new ScoutTexts();
    ScoutTexts.CURRENT.set(scoutTextsThreadLocal);

    IClientSession session = mock(IClientSession.class);
    when(session.getTexts()).thenReturn(null); // no texts available on session
    ISession.CURRENT.set(session);

    m_jobManager.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertNull(ScoutTexts.CURRENT.get());
      }
    }, ClientJobInput.defaults().sessionRequired(false));
  }
}
