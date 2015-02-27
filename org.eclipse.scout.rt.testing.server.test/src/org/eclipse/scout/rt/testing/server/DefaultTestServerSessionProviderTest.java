/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.server;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.cdi.internal.BeanInstanceCreator;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.platform.ScoutPlatformTestRunner;
import org.eclipse.scout.rt.testing.server.test.TestServerSession;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * Test for {@link DefaultTestServerSessionProvider}
 */
@RunWith(ScoutPlatformTestRunner.class)
public class DefaultTestServerSessionProviderTest {

  //TODO aho this test fails in NOSGI because bundle cannot be resolved when loading server session.
  @Ignore
  @Test
  public void testCreateServerSession() throws ProcessingException {
    DefaultTestServerSessionProvider serverSessionProvider = BeanInstanceCreator.create(DefaultTestServerSessionProvider.class);
    final DefaultTestServerSessionProvider testee = Mockito.spy(serverSessionProvider);
    final Subject subject = new Subject();

    final TestServerSession session = testee.newServerSession(TestServerSession.class, subject, UserAgent.create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN, "n/a"));

    assertTrue(session.isActive());
    verify(testee, times(1)).afterStartSession(session, subject);
    verify(testee, times(1)).beforeStartSession(session, subject);
  }
}
