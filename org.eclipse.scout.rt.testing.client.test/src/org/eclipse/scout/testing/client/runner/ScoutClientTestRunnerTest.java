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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;

import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.testing.client.DefaultTestClientSessionProvider;
import org.eclipse.scout.testing.client.ITestClientSessionProvider;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner.ClientTest;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner.NullTestClientSessionProvider;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.model.InitializationError;

/**
 * JUnit test for {@link ScoutClientTestRunner}
 */
public class ScoutClientTestRunnerTest {

  private ScoutClientTestRunnerEx m_runner;
  private ClientTest m_allValuesClientTest;
  private ClientTest m_defaultValuesClientTest;

  @BeforeClass
  public static void beforeClassCheck() {
    assertTrue("This check ensures the before class runner from scout testing works. @See bug405846", true);
  }

  @AfterClass
  public static void afterClassCheck() {
    assertTrue("This check ensures the after class runner from scout testing works. @See bug405846", true);
  }

  @Before
  public void setUp() throws Exception {
    m_runner = new ScoutClientTestRunnerEx(TestCase.class);
    m_allValuesClientTest = new ClientTest() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return ClientTest.class;
      }

      @Override
      public Class<? extends ITestClientSessionProvider> sessionProvider() {
        return TestClientSessionProvider.class;
      }

      @Override
      public String runAs() {
        return "otherUser";
      }

      @Override
      public boolean forceNewSession() {
        return true;
      }

      @Override
      public Class<? extends IClientSession> clientSessionClass() {
        return TestClientSession.class;
      }
    };
    m_defaultValuesClientTest = new ClientTest() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return ClientTest.class;
      }

      @Override
      public Class<? extends ITestClientSessionProvider> sessionProvider() {
        return NullTestClientSessionProvider.class;
      }

      @Override
      public String runAs() {
        return "";
      }

      @Override
      public boolean forceNewSession() {
        return false;
      }

      @Override
      public Class<? extends IClientSession> clientSessionClass() {
        return IClientSession.class;
      }
    };
  }

  @Test
  public void testExtractClientSessionClass() throws Exception {
    // null values
    assertNull(m_runner.extractClientSessionClass(null, null));
    assertEquals(IClientSession.class, m_runner.extractClientSessionClass(null, IClientSession.class));

    // default values
    assertNull(m_runner.extractClientSessionClass(m_defaultValuesClientTest, null));
    assertEquals(IClientSession.class, m_runner.extractClientSessionClass(m_defaultValuesClientTest, IClientSession.class));

    // overridden values
    assertEquals(TestClientSession.class, m_runner.extractClientSessionClass(m_allValuesClientTest, null));
    assertEquals(TestClientSession.class, m_runner.extractClientSessionClass(m_allValuesClientTest, IClientSession.class));
  }

  @Test
  public void testExtractSessionProvider() throws Exception {
    DefaultTestClientSessionProvider defaultValue = new DefaultTestClientSessionProvider();

    // null values
    assertNull(m_runner.extractSessionProvider(null, null));
    assertEquals(defaultValue, m_runner.extractSessionProvider(null, defaultValue));

    // default values
    assertNull(m_runner.extractSessionProvider(m_defaultValuesClientTest, null));
    assertEquals(defaultValue, m_runner.extractSessionProvider(m_defaultValuesClientTest, defaultValue));

    // overridden values
    assertTrue(m_runner.extractSessionProvider(m_allValuesClientTest, null) instanceof TestClientSessionProvider);
    assertTrue(m_runner.extractSessionProvider(m_allValuesClientTest, defaultValue) instanceof TestClientSessionProvider);
  }

  @Test
  public void testExtractRunAs() throws Exception {
    String defaultValue = "defaultValue";

    // null values
    assertNull(m_runner.extractRunAs(null, null));
    assertEquals(defaultValue, m_runner.extractRunAs(null, defaultValue));

    // default values
    assertNull(m_runner.extractRunAs(m_defaultValuesClientTest, null));
    assertEquals(defaultValue, m_runner.extractRunAs(m_defaultValuesClientTest, defaultValue));

    // overridden values
    assertEquals("otherUser", m_runner.extractRunAs(m_allValuesClientTest, null));
    assertEquals("otherUser", m_runner.extractRunAs(m_allValuesClientTest, defaultValue));
  }

  @Test
  public void testExtractForceNewSession() throws Exception {

    // null values
    assertFalse(m_runner.extractForceNewSession(null, false));
    assertTrue(m_runner.extractForceNewSession(null, true));

    // default values
    assertFalse(m_runner.extractForceNewSession(m_defaultValuesClientTest, true));
    assertFalse(m_runner.extractForceNewSession(m_defaultValuesClientTest, false));

    // overridden values
    assertTrue(m_runner.extractForceNewSession(m_allValuesClientTest, false));
    assertTrue(m_runner.extractForceNewSession(m_allValuesClientTest, true));
  }

  private static class ScoutClientTestRunnerEx extends ScoutClientTestRunner {

    /**
     * @param klass
     * @throws InitializationError
     */
    public ScoutClientTestRunnerEx(Class<?> klass) throws InitializationError {
      super(klass);
    }

    @Override
    protected IClientSession getOrCreateClientSession(ClientTest classLevelClientTest, ClientTest methodLevelClientTest) throws Exception {
      return null;
    }

    @Override
    public Class<? extends IClientSession> extractClientSessionClass(ClientTest methodLevelClientTest, Class<? extends IClientSession> defaultValue) {
      return super.extractClientSessionClass(methodLevelClientTest, defaultValue);
    }

    @Override
    public ITestClientSessionProvider extractSessionProvider(ClientTest methodLevelClientTest, ITestClientSessionProvider defaultValue) throws Exception {
      return super.extractSessionProvider(methodLevelClientTest, defaultValue);
    }

    @Override
    public String extractRunAs(ClientTest methodLevelClientTest, String defaultValue) {
      return super.extractRunAs(methodLevelClientTest, defaultValue);
    }

    @Override
    public boolean extractForceNewSession(ClientTest clientTest, boolean defaultValue) {
      return super.extractForceNewSession(clientTest, defaultValue);
    }
  }

  public static class TestCase {
    @Test
    public void test() {

    }
  }

  public static class TestClientSession extends AbstractClientSession {

    public TestClientSession() {
      super(true);
    }
  }

  public static class TestClientSessionProvider implements ITestClientSessionProvider {

    @Override
    public <T extends IClientSession> T getOrCreateClientSession(Class<T> clazz, String user, boolean forceNewSession) {
      return null;
    }
  }
}
