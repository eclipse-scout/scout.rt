/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.server.session.runner;

import static org.junit.Assert.*;

import java.io.Serial;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.security.User;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.server.session.AbstractServerSession;
import org.eclipse.scout.rt.server.session.IServerSession;
import org.eclipse.scout.rt.shared.session.ISession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.session.runner.ServerTestRunnerSameSessionTest.JUnitServerSession;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ServerSessionTestRunner.class)
@RunWithServerSession(JUnitServerSession.class)
@RunWithSubject("anna")
public class ServerTestRunnerSameSessionTest {

  private static Set<ISession> s_serverSessions;
  private static Set<ITransaction> s_transactions;

  @BeforeClass
  public static void beforeClass() {
    s_serverSessions = new HashSet<>();
    ISession serverSession = IServerSession.CURRENT.get();
    assertTrue(serverSession instanceof JUnitServerSession);
    assertEquals("anna", User.currentUserId());
    s_serverSessions.add(serverSession);

    s_transactions = new HashSet<>();
    ITransaction transaction = ITransaction.CURRENT.get();
    assertNotNull(transaction);
    s_transactions.add(transaction);
  }

  @Test
  public void test1() {
    ISession serverSession = IServerSession.CURRENT.get();
    assertTrue(serverSession instanceof JUnitServerSession);
    assertEquals("anna", User.currentUserId());
    s_serverSessions.add(serverSession);

    ITransaction transaction = ITransaction.CURRENT.get();
    assertNotNull(transaction);
    s_transactions.add(transaction);
  }

  @Test
  public void test2() {
    ISession serverSession = IServerSession.CURRENT.get();
    assertTrue(serverSession instanceof JUnitServerSession);
    assertEquals("anna", User.currentUserId());
    s_serverSessions.add(serverSession);

    ITransaction transaction = ITransaction.CURRENT.get();
    assertNotNull(transaction);
    s_transactions.add(transaction);
  }

  @AfterClass
  public static void afterClass() {
    ISession serverSession = IServerSession.CURRENT.get();
    assertTrue(serverSession instanceof JUnitServerSession);
    assertEquals("anna", User.currentUserId());
    s_serverSessions.add(serverSession);

    ITransaction transaction = ITransaction.CURRENT.get();
    assertNotNull(transaction);
    s_transactions.add(transaction);

    assertEquals(1, s_serverSessions.size());
    assertEquals(4, s_transactions.size()); // (beforeClass), (before,test1,after), (before,test2,after), (afterClass)
  }

  @IgnoreBean
  public static class JUnitServerSession extends AbstractServerSession {

    @Serial
    private static final long serialVersionUID = 1L;

    public JUnitServerSession() {
      super(true);
    }
  }
}
