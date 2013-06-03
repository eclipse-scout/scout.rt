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
 * JUnit test for {@link ScoutClientTestRunner} with login.
 */
@RunWith(ScoutClientTestRunner.class)
@ClientTest(runAs = "test", sessionProvider = LoginTestSessionProvider.class, forceNewSession = true)
public class ScoutClientTestRunnerLoginTest {

  private static IClientSession s_beforeClassClientSession;

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
    checkBeforeAfterAndTest();
  }

  @Before
  public void before2() throws Exception {
    checkBeforeAfterAndTest();
  }

  @After
  public void after1() throws Exception {
    checkBeforeAfterAndTest();
  }

  @After
  public void after2() throws Exception {
    checkBeforeAfterAndTest();
  }

  @Test
  public void testWithAdditionalLogin1() throws Exception {
    checkBeforeAfterAndTest();
  }

  @Test
  public void testWithAdditionalLogin2() throws Exception {
    checkBeforeAfterAndTest();
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

  private void checkBeforeAfterAndTest() {
    assertNotNull(TestEnvironmentClientSession.get());
    assertSame(s_beforeClassClientSession, TestEnvironmentClientSession.get());
    assertEquals(Collections.emptyList(), LoginTestSessionProvider.getBeforeStartRunAs());
    assertEquals(Collections.emptyList(), LoginTestSessionProvider.getAfterStartRunAs());
  }
}
