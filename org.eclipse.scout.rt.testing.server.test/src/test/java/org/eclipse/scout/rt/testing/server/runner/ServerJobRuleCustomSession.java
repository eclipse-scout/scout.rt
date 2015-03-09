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
package org.eclipse.scout.rt.testing.server.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.platform.PlatformTestRunner;
import org.eclipse.scout.rt.testing.server.junit.rule.RunAs;
import org.eclipse.scout.rt.testing.server.junit.rule.ServerJobRule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
@RunAs("Anna")
@Ignore
// TODO [dwi] REMOVE ignore once nOSGi is done
public class ServerJobRuleCustomSession {

  private static Set<ISession> m_serverSessions;

  @Rule
  public TestRule serverJobRule = new ServerJobRule(JUnitServerSession.class);

  @BeforeClass
  public static void beforeClass() {
    m_serverSessions = new HashSet<>();
  }

  @Test
  public void test1() {
    ISession serverSession = IServerSession.CURRENT.get();
    assertNotNull(serverSession);
    assertSame(JUnitServerSession.class, serverSession.getClass());
    assertEquals("Anna", serverSession.getUserId());
    m_serverSessions.add(serverSession);

  }

  @Test
  public void test2() {
    ISession serverSession = IServerSession.CURRENT.get();
    assertNotNull(serverSession);
    assertSame(JUnitServerSession.class, serverSession.getClass());
    assertEquals("Anna", serverSession.getUserId());
    m_serverSessions.add(serverSession);
  }

  @AfterClass
  public static void afterClass() {
    assertEquals(1, m_serverSessions.size());
  }

  private static class JUnitServerSession extends AbstractServerSession {

    private static final long serialVersionUID = 1L;

    public JUnitServerSession() {
      super(true);
    }
  }
}
