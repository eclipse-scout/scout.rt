/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.testing.client.runner;

import java.util.Collections;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner.ClientTest;
import org.eclipse.scout.testing.client.runner.fixture.LoginTestSessionProvider;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit test for {@link ScoutClientTestRunner} with additional login.
 */
@RunWith(ScoutClientTestRunner.class)
@ClientTest(runAs = "test", sessionProvider = LoginTestSessionProvider.class, forceNewSession = true)
public class ScoutClientTestRunnerAdditionalLoginTest {

  private static IClientSession s_beforeClassClientSession;
  private static TestEnvironmentClientSession s_beforeClientSession;
  private static TestEnvironmentClientSession s_afterClientSession;

  @BeforeClass
  public static void beforeClass1() throws Exception {
    checkBeforeClass();
  }

  @BeforeClass
  public static void beforeClass2() throws Exception {
    checkBeforeClass();
  }

  @AfterClass
  public static void afterClass1() throws Exception {
    checkAfterClass();
  }

  @AfterClass
  public static void afterClass2() throws Exception {
    checkAfterClass();
  }

  @Before
  public void before1() throws Exception {
    checkBefore();
  }

  @Before
  public void before2() throws Exception {
    checkBefore();
  }

  @After
  public void after1() throws Exception {
    checkAfter();
  }

  @After
  public void after2() throws Exception {
    checkAfter();
  }

  @Test
  @ClientTest(runAs = "otherUser", sessionProvider = LoginTestSessionProvider.class)
  public void testWithAdditionalLogin1() throws Exception {
    checkTest();
  }

  @Test
  @ClientTest(runAs = "thirdUser", sessionProvider = LoginTestSessionProvider.class)
  public void testWithAdditionalLogin2() throws Exception {
    checkTest();
  }

  private static void checkBeforeClass() {
    assertNotNull(TestEnvironmentClientSession.get());
    if (s_beforeClassClientSession == null) {
      assertSame(LoginTestSessionProvider.getCurrentSession(), TestEnvironmentClientSession.get());
      assertEquals(Collections.singletonList("test"), LoginTestSessionProvider.getBeforeStartRunAs());
      assertEquals(Collections.singletonList("test"), LoginTestSessionProvider.getAfterStartRunAs());
      s_beforeClassClientSession = TestEnvironmentClientSession.get();
      LoginTestSessionProvider.clearProtocol();
    }
    else {
      assertEquals(Collections.emptyList(), LoginTestSessionProvider.getBeforeStartRunAs());
      assertEquals(Collections.emptyList(), LoginTestSessionProvider.getAfterStartRunAs());
      assertSame(s_beforeClassClientSession, TestEnvironmentClientSession.get());
    }
  }

  private static void checkAfterClass() {
    assertSame(s_beforeClassClientSession, TestEnvironmentClientSession.get());
    assertEquals(Collections.emptyList(), LoginTestSessionProvider.getBeforeStartRunAs());
    assertEquals(Collections.emptyList(), LoginTestSessionProvider.getAfterStartRunAs());
  }

  private void checkBefore() {
    assertNotNull(TestEnvironmentClientSession.get());
    if (s_beforeClientSession == null) {
      assertSame(LoginTestSessionProvider.getCurrentSession(), TestEnvironmentClientSession.get());
      assertEquals(1, LoginTestSessionProvider.getBeforeStartRunAs().size());
      assertEquals(1, LoginTestSessionProvider.getAfterStartRunAs().size());
      assertTrue("otherUser".equals(LoginTestSessionProvider.getBeforeStartRunAs().get(0)) || "thirdUser".equals(LoginTestSessionProvider.getBeforeStartRunAs().get(0)));
      assertTrue("otherUser".equals(LoginTestSessionProvider.getAfterStartRunAs().get(0)) || "thirdUser".equals(LoginTestSessionProvider.getAfterStartRunAs().get(0)));
      LoginTestSessionProvider.clearProtocol();
    }
    else {
      assertEquals(Collections.emptyList(), LoginTestSessionProvider.getBeforeStartRunAs());
      assertEquals(Collections.emptyList(), LoginTestSessionProvider.getAfterStartRunAs());
      assertSame(s_beforeClientSession, TestEnvironmentClientSession.get());
    }
    s_beforeClientSession = TestEnvironmentClientSession.get();
  }

  private void checkAfter() {
    assertEquals(Collections.emptyList(), LoginTestSessionProvider.getBeforeStartRunAs());
    assertEquals(Collections.emptyList(), LoginTestSessionProvider.getAfterStartRunAs());
    if (s_beforeClientSession != null) {
      assertSame(s_beforeClassClientSession, TestEnvironmentClientSession.get());
    }
  }

  private void checkTest() {
    try {
      assertEquals(s_beforeClientSession, TestEnvironmentClientSession.get());
    }
    finally {
      s_afterClientSession = s_beforeClientSession;
      s_beforeClientSession = null;
    }
  }
}
