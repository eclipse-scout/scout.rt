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
package org.eclipse.scout.testing.client.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Collections;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.testing.client.runner.fixture.LoginTestClientSessionProvider;
import org.eclipse.scout.testing.client.runner.fixture.LoginTestClientSessionProvider.LoginTestClientSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit test for {@link ClientTestRunner} with login.
 */
@RunWith(ClientTestRunner.class)
@RunWithClientSession(value = LoginTestClientSession.class, provider = LoginTestClientSessionProvider.class)
@RunWithSubject(ScoutClientTestRunnerLoginTest.TEST_SUBJECT)
public class ScoutClientTestRunnerLoginTest {

  static final String TEST_SUBJECT = "ScoutClientTestRunnerLoginTest";
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
      assertSame(LoginTestClientSessionProvider.getCurrentSession(), TestEnvironmentClientSession.get());
      assertEquals(Collections.singletonList(TEST_SUBJECT), LoginTestClientSessionProvider.getBeforeStartRunAs());
      assertEquals(Collections.singletonList(TEST_SUBJECT), LoginTestClientSessionProvider.getAfterStartRunAs());
      s_beforeClassClientSession = TestEnvironmentClientSession.get();
      LoginTestClientSessionProvider.clearProtocol();
    }
    else {
      assertEquals(Collections.emptyList(), LoginTestClientSessionProvider.getBeforeStartRunAs());
      assertEquals(Collections.emptyList(), LoginTestClientSessionProvider.getAfterStartRunAs());
      assertSame(s_beforeClassClientSession, TestEnvironmentClientSession.get());
    }
  }

  private static void checkAfterClass() {
    assertSame(s_beforeClassClientSession, TestEnvironmentClientSession.get());
    assertEquals(Collections.emptyList(), LoginTestClientSessionProvider.getBeforeStartRunAs());
    assertEquals(Collections.emptyList(), LoginTestClientSessionProvider.getAfterStartRunAs());
  }

  private void checkBeforeAfterAndTest() {
    assertNotNull(TestEnvironmentClientSession.get());
    assertSame(s_beforeClassClientSession, TestEnvironmentClientSession.get());
    assertEquals(Collections.emptyList(), LoginTestClientSessionProvider.getBeforeStartRunAs());
    assertEquals(Collections.emptyList(), LoginTestClientSessionProvider.getAfterStartRunAs());
  }
}
