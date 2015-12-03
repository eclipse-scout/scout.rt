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
package org.eclipse.scout.rt.testing.platform.runner.parameterized;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.testing.platform.runner.parameterized.ParameterizedTestRunnerExtensionTest.ParameterizedTestClassParameter;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

@Ignore("This class is not supposed to be executed as test. Its class structure is used by ParameterizedTestRunnerExtensionTest.")
public class SampleParameterizedTestClass {
  static final String TEST_PARAMETER_NAME_1 = "Scenario 1";
  private final ParameterizedTestClassParameter m_testParameter;

  public SampleParameterizedTestClass(ParameterizedTestClassParameter testParameter) {
    m_testParameter = testParameter;
  }

  public ParameterizedTestClassParameter getTestParameter() {
    return m_testParameter;
  }

  @Parameters
  public static List<IScoutTestParameter> getParameters() {
    List<IScoutTestParameter> parametersList = new LinkedList<IScoutTestParameter>();
    parametersList.add(new ParameterizedTestClassParameter(TEST_PARAMETER_NAME_1));
    parametersList.add(new ParameterizedTestClassParameter("Scenario 2"));
    return parametersList;
  }

  @Test
  public void testSomething() {
  }

  @Test
  @NonParameterized
  public void testSomethingNonParameterized() {

  }
}
