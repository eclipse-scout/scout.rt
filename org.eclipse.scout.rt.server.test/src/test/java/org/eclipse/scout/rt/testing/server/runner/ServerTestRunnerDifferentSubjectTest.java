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
package org.eclipse.scout.rt.testing.server.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunnerDifferentSubjectTest.JUnitServerSession;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ServerTestRunner.class)
@RunWithServerSession(JUnitServerSession.class)
@RunWithSubject("anna")
public class ServerTestRunnerDifferentSubjectTest {

  private static Set<ISession> m_serverSessions;
  private static Set<ITransaction> m_transactions;

  @BeforeClass
  public static void beforeClass() {
    m_serverSessions = new HashSet<>();
    ISession serverSession = IServerSession.CURRENT.get();
    assertTrue(serverSession instanceof JUnitServerSession);
    assertEquals("anna", serverSession.getUserId());
    m_serverSessions.add(serverSession);

    m_transactions = new HashSet<>();
    ITransaction transaction = ITransaction.CURRENT.get();
    assertNotNull(transaction);
    m_transactions.add(transaction);
  }

  @Test
  public void test1() {
    ISession serverSession = IServerSession.CURRENT.get();
    assertTrue(serverSession instanceof JUnitServerSession);
    assertEquals("anna", serverSession.getUserId());
    m_serverSessions.add(serverSession);

    ITransaction transaction = ITransaction.CURRENT.get();
    assertNotNull(transaction);
    m_transactions.add(transaction);
  }

  @Test
  @RunWithSubject("john")
  public void test2() {
    ISession serverSession = IServerSession.CURRENT.get();
    assertTrue(serverSession instanceof JUnitServerSession);
    assertEquals("john", serverSession.getUserId());
    m_serverSessions.add(serverSession);

    ITransaction transaction = ITransaction.CURRENT.get();
    assertNotNull(transaction);
    m_transactions.add(transaction);
  }

  @AfterClass
  public static void afterClass() {
    ISession serverSession = IServerSession.CURRENT.get();
    assertTrue(serverSession instanceof JUnitServerSession);
    assertEquals("anna", serverSession.getUserId());
    m_serverSessions.add(serverSession);

    ITransaction transaction = ITransaction.CURRENT.get();
    assertNotNull(transaction);
    m_transactions.add(transaction);

    assertEquals(2, m_serverSessions.size());
    assertEquals(4, m_transactions.size()); // (beforeClass), (before,test1,after), (before,test2,after), (afterClass)
  }

  public static class JUnitServerSession extends AbstractServerSession {

    private static final long serialVersionUID = 1L;

    public JUnitServerSession() {
      super(true);
    }
  }
}
