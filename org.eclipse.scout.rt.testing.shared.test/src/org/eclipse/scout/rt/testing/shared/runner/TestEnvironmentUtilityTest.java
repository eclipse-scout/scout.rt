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
package org.eclipse.scout.rt.testing.shared.runner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.eclipse.scout.rt.testing.shared.runner.fixture.CustomTestingTestEnvironment;
import org.eclipse.scout.rt.testing.shared.runner.fixture.DefaultTestingTestEnvironment;
import org.eclipse.scout.rt.testing.shared.runner.fixture.ITestingTestEnvironment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 4.2.x
 */
public class TestEnvironmentUtilityTest {

  private static final String CUSTOM_TESTING_ENV_PROPERTY_NAME = "customTestingTestEnvironmentPropertyName";
  private static final String DEFAULT_TESTING_ENV_CLASS_NAME = DefaultTestingTestEnvironment.class.getName();

  @Before
  @After
  public void cleanup() throws Exception {
    System.clearProperty(CUSTOM_TESTING_ENV_PROPERTY_NAME);
  }

  @Test
  public void noTestEnvironment() throws Exception {
    ITestingTestEnvironment env = TestEnvironmentUtility.createTestEnvironment(
        ITestingTestEnvironment.class,
        "notImportant",
        "NonExistingDefaultTestingTestEnvironment");
    assertNull(env);
  }

  @Test
  public void defaultTestEnvironment() throws Exception {
    ITestingTestEnvironment env = TestEnvironmentUtility.createTestEnvironment(
        ITestingTestEnvironment.class,
        "notImportant",
        DEFAULT_TESTING_ENV_CLASS_NAME);
    assertNotNull(env);
    assertSame(DefaultTestingTestEnvironment.class, env.getClass());
  }

  @Test
  public void defaultTestEnvironmentNotImplementingExpectedInterface() throws Exception {
    ITestingTestEnvironment env = TestEnvironmentUtility.createTestEnvironment(
        ITestingTestEnvironment.class,
        "notImportant",
        Object.class.getName()); // Object is not an ITestingTestEnvironment
    assertNull(env);
  }

  @Test
  public void customTestEnvironment() throws Exception {
    System.setProperty(CUSTOM_TESTING_ENV_PROPERTY_NAME, CustomTestingTestEnvironment.class.getName());
    ITestingTestEnvironment env = TestEnvironmentUtility.createTestEnvironment(
        ITestingTestEnvironment.class,
        CUSTOM_TESTING_ENV_PROPERTY_NAME,
        DEFAULT_TESTING_ENV_CLASS_NAME);
    assertNotNull(env);
    assertSame(CustomTestingTestEnvironment.class, env.getClass());
  }

  @Test(expected = IllegalStateException.class)
  public void customTestEnvironmentReferencingNonExistingClass() throws Exception {
    System.setProperty(CUSTOM_TESTING_ENV_PROPERTY_NAME, "NonExistingCustomTestEnvironment");
    TestEnvironmentUtility.createTestEnvironment(
        ITestingTestEnvironment.class,
        CUSTOM_TESTING_ENV_PROPERTY_NAME,
        DEFAULT_TESTING_ENV_CLASS_NAME);
  }

  @Test(expected = IllegalStateException.class)
  public void customTestEnvironmentNotImplementingExpectedInterface() throws Exception {
    System.setProperty(CUSTOM_TESTING_ENV_PROPERTY_NAME, Object.class.getName()); // Object is not an ITestingTestEnvironment
    TestEnvironmentUtility.createTestEnvironment(
        ITestingTestEnvironment.class,
        CUSTOM_TESTING_ENV_PROPERTY_NAME,
        DEFAULT_TESTING_ENV_CLASS_NAME);
  }
}
