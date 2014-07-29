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
package org.eclipse.scout.rt.testing.shared.runner.parameterized;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

public class ParameterizedTestRunnerExtensionTest {

  private TestClass m_testClass;
  private FrameworkMethod m_testMethod;

  @Before
  public void before() throws NoSuchMethodException, SecurityException {
    m_testClass = new TestClass(SampleParameterizedTestClass.class);
    m_testMethod = new FrameworkMethod(SampleParameterizedTestClass.class.getMethod("testSomething"));
  }

  @Test
  public void testCreateTest() throws Exception {
    IScoutTestParameter testParameter = new ParameterizedTestClassParameter("p");

    Object createdTestInstance = ParameterizedTestRunnerExtension.createTest(m_testClass, testParameter);
    assertNotNull(createdTestInstance);
    assertEquals(SampleParameterizedTestClass.class, createdTestInstance.getClass());
    assertEquals(testParameter, ((SampleParameterizedTestClass) createdTestInstance).getTestParameter());
  }

  @Test
  public void testLoadParameterList() {
    List<IScoutTestParameter> parameterList = ParameterizedTestRunnerExtension.loadParameterList(m_testClass);
    assertNotNull(parameterList);
    assertEquals(2, parameterList.size());
    assertTrue(parameterList.get(0) instanceof IScoutTestParameter);
  }

  @Test
  public void testCreateParameterizedTestMethods() {
    List<FrameworkMethod> testMethodLists = new LinkedList<FrameworkMethod>();
    testMethodLists.add(m_testMethod);

    List<FrameworkMethod> createdParameterizedTestMethods = ParameterizedTestRunnerExtension.createParameterizedTestMethods(testMethodLists, 2);
    assertNotNull(createdParameterizedTestMethods);
    assertEquals(2, createdParameterizedTestMethods.size());
  }

  @Test
  public void testValidateOneParametersMethod() throws NoSuchMethodException, SecurityException {
    List<Throwable> errors;

    errors = new LinkedList<Throwable>();
    ParameterizedTestRunnerExtension.validateOneParametersMethod(m_testClass, errors);
    assertTrue(errors.isEmpty());

    errors = new LinkedList<Throwable>();
    ParameterizedTestRunnerExtension.validateOneParametersMethod(new TestClass(ParameterizedTestClassWithMissingParametersMethod.class), errors);
    assertFalse(errors.isEmpty());

    errors = new LinkedList<Throwable>();
    ParameterizedTestRunnerExtension.validateOneParametersMethod(new TestClass(ParameterizedTestClassWithIncorrectParametersMethod1.class), errors);
    assertFalse(errors.isEmpty());

    errors = new LinkedList<Throwable>();
    ParameterizedTestRunnerExtension.validateOneParametersMethod(new TestClass(ParameterizedTestClassWithIncorrectParametersMethod2.class), errors);
    assertFalse(errors.isEmpty());
  }

  @Test
  public void testDescribeChild() {
    final String testName = SampleParameterizedTestClass.class.getSimpleName();
    final List<IScoutTestParameter> parameterList = SampleParameterizedTestClass.getParameters();
    ParameterizedFrameworkMethod parameterizedTestMethod = new ParameterizedFrameworkMethod(m_testMethod, 0);

    Description testDescription = ParameterizedTestRunnerExtension.describeChild(m_testClass, parameterizedTestMethod, testName, parameterList);
    String expectedTestDescription = testName + " [" + SampleParameterizedTestClass.TEST_PARAMETER_NAME_1 + "]";
    assertTrue(testDescription.getDisplayName().startsWith(expectedTestDescription));
  }

  public static class ParameterizedTestClassWithMissingParametersMethod {
  }

  /** Parameters method with wrong access modifier. */
  public static class ParameterizedTestClassWithIncorrectParametersMethod1 {
    @Parameters
    private static List<IScoutTestParameter> getParameters() {
      return new LinkedList<IScoutTestParameter>();
    }
  }

  /** Parameters method with wrong return type. */
  public static class ParameterizedTestClassWithIncorrectParametersMethod2 {
    @Parameters
    private static Set<IScoutTestParameter> getParameters() {
      return new HashSet<IScoutTestParameter>();
    }
  }

  public static class ParameterizedTestClassParameter extends AbstractScoutTestParameter {
    public ParameterizedTestClassParameter(String name) {
      super(name);
    }
  }
}
