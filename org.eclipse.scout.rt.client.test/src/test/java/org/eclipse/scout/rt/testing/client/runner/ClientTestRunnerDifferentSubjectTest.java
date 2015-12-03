/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.client.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.AccessController;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunnerDifferentSubjectTest.JUnitClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithClientSession(JUnitClientSession.class)
@RunWithSubject("anna")
public class ClientTestRunnerDifferentSubjectTest {

  private static Set<ISession> m_clientSessions;

  @BeforeClass
  public static void beforeClass() {
    m_clientSessions = new HashSet<>();
    ISession clientSession = IClientSession.CURRENT.get();
    assertTrue(clientSession instanceof JUnitClientSession);
    assertEquals("anna", getCurrentUser());
    m_clientSessions.add(clientSession);
  }

  @Test
  public void test1() {
    ISession clientSession = IClientSession.CURRENT.get();
    assertTrue(clientSession instanceof JUnitClientSession);
    assertEquals("anna", getCurrentUser());
    m_clientSessions.add(clientSession);
  }

  @Test
  @RunWithSubject("john")
  public void test2() {
    ISession clientSession = IClientSession.CURRENT.get();
    assertTrue(clientSession instanceof JUnitClientSession);
    assertEquals("john", getCurrentUser());
    m_clientSessions.add(clientSession);
  }

  @AfterClass
  public static void afterClass() {
    ISession clientSession = IClientSession.CURRENT.get();
    assertTrue(clientSession instanceof JUnitClientSession);
    assertEquals("anna", getCurrentUser());
    m_clientSessions.add(clientSession);

    assertEquals(2, m_clientSessions.size());
  }

  private static String getCurrentUser() {
    return Subject.getSubject(AccessController.getContext()).getPrincipals().iterator().next().toString();
  }

  public static class JUnitClientSession extends AbstractClientSession {

    public JUnitClientSession() {
      super(true);
    }
  }
}
