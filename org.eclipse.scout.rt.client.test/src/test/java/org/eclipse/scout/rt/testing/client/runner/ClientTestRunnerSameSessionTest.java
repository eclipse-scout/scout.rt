/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.client.runner;

import static org.junit.Assert.*;

import java.security.AccessController;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunnerSameSessionTest.JUnitClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithClientSession(JUnitClientSession.class)
@RunWithSubject("anna")
public class ClientTestRunnerSameSessionTest {

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
  public void test2() {
    ISession clientSession = IClientSession.CURRENT.get();
    assertTrue(clientSession instanceof JUnitClientSession);
    assertEquals("anna", getCurrentUser());
    m_clientSessions.add(clientSession);
  }

  @AfterClass
  public static void afterClass() {
    ISession clientSession = IClientSession.CURRENT.get();
    assertTrue(clientSession instanceof JUnitClientSession);
    assertEquals("anna", getCurrentUser());
    m_clientSessions.add(clientSession);

    assertEquals(1, m_clientSessions.size());
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
