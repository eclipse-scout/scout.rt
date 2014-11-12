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
package org.eclipse.scout.rt.testing.server.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner.LoginInfo;
import org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner.ServerTest;
import org.eclipse.scout.rt.testing.server.test.TestServerSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

/**
 * JUnit test for {@link ScoutServerTestRunner}
 */
public class ScoutServerTestRunnerTest {

  private Class<? extends IServerSession> m_globalServerSessionClass;
  private String m_globalTestPrincipal;
  private static final String TEST_PRINCIPAL = "TEST_PRINCIPAL";
  private static final String TEST_CLASS_PRINCIPAL = "TEST_CLASS_PRINCIPAL";
  private static final String TEST_METHOD_PRINCIPAL = "TEST_METHOD_PRINCIPAL";

  @Before
  public void setup() {
    m_globalServerSessionClass = ScoutServerTestRunner.getDefaultServerSessionClass();
    m_globalTestPrincipal = ScoutServerTestRunner.getDefaultPrincipalName();
  }

  @After
  public void tearDown() {
    ScoutServerTestRunner.setDefaultServerSessionClass(m_globalServerSessionClass);
    ScoutServerTestRunner.setDefaultPrincipalName(m_globalTestPrincipal);
  }

  /**
   * Test for
   * {@link ScoutServerTestRunner#getOrCreateServerSession(org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner.ServerTest, org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner.ServerTest)}
   * without annotations.
   */
  @Test
  public void testCreateServerSession() throws Exception {
    ScoutServerTestRunner.setDefaultServerSessionClass(TestServerSession.class);
    ScoutServerTestRunner.setDefaultPrincipalName(TEST_PRINCIPAL);

    final ScoutServerTestRunner runner = new ScoutServerTestRunner(DummyClassUnderTest.class);
    final LoginInfo loginInfo = runner.getOrCreateServerSession(null, null);
    assertEquals(TestServerSession.class, loginInfo.getServerSession().getClass());
    assertTrue(containsPrincipal(TEST_PRINCIPAL, loginInfo.getSubject()));
  }

  /**
   * Test for
   * {@link ScoutServerTestRunner#getOrCreateServerSession(org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner.ServerTest, org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner.ServerTest)}
   * with class annotations.
   */
  @Test
  public void testCreateServerSessionClassAnnotations() throws Exception {
    final ScoutServerTestRunner runner = new ScoutServerTestRunner(DummyTestWithClassAnnotation.class);
    final LoginInfo loginInfo = runner.getOrCreateServerSession(getClassAnnotation(runner), null);
    assertEquals(TestServerSession.class, loginInfo.getServerSession().getClass());
    assertTrue(containsPrincipal(TEST_CLASS_PRINCIPAL, loginInfo.getSubject()));
  }

  /**
   * Test for
   * {@link ScoutServerTestRunner#getOrCreateServerSession(org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner.ServerTest, org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner.ServerTest)}
   * with method annotations.
   */
  @Test
  public void testCreateServerSessionMethodAnnotations() throws Exception {
    final ScoutServerTestRunner runner = new ScoutServerTestRunner(DummyTestWithMethodAnnotation.class);
    final ServerTest methodLevelAnnotation = DummyTestWithMethodAnnotation.class.getMethod("test").getAnnotation(ServerTest.class);
    final LoginInfo loginInfo = runner.getOrCreateServerSession(getClassAnnotation(runner), methodLevelAnnotation);
    assertEquals(TestServerSession.class, loginInfo.getServerSession().getClass());
    assertTrue(containsPrincipal(TEST_METHOD_PRINCIPAL, loginInfo.getSubject()));
  }

  @Test(expected = InitializationError.class)
  public void testCreateNoServerSession() throws Exception {
    ScoutServerTestRunner.setDefaultServerSessionClass(null);
    ScoutServerTestRunner.setDefaultPrincipalName(null);
    new ScoutServerTestRunner(DummyClassUnderTest.class);
  }

  private ServerTest getClassAnnotation(final ScoutServerTestRunner runner) {
    return runner.getTestClass().getJavaClass().getAnnotation(ServerTest.class);
  }

  private boolean containsPrincipal(String principalName, Subject subject) {
    final Set<Principal> principals = subject.getPrincipals();
    for (Principal p : principals) {
      if (principalName.equals(p.getName())) {
        return true;
      }
    }
    return false;
  }

  //fixtures

  @RunWith(ScoutServerTestRunner.class)
  public static class DummyClassUnderTest {

    @Test
    public void test() {
      assertTrue(true);
    }

  }

  @RunWith(ScoutServerTestRunner.class)
  @ServerTest(serverSessionClass = TestServerSession.class, runAs = TEST_CLASS_PRINCIPAL)
  public static class DummyTestWithClassAnnotation {
    @Test
    public void test() {
      assertTrue(true);
    }
  }

  @RunWith(ScoutServerTestRunner.class)
  @ServerTest(serverSessionClass = TestServerSession.class, runAs = TEST_CLASS_PRINCIPAL)
  public static class DummyTestWithMethodAnnotation {

    @Test
    @ServerTest(serverSessionClass = TestServerSession.class, runAs = TEST_METHOD_PRINCIPAL)
    public void test() {
      assertTrue(true);
    }
  }

}
