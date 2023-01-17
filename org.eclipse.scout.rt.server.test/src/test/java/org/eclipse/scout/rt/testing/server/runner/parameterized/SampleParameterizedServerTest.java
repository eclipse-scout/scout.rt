/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.server.runner.parameterized;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.testing.platform.runner.parameterized.AbstractScoutTestParameter;
import org.eclipse.scout.rt.testing.platform.runner.parameterized.IScoutTestParameter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

/**
 * This test illustrates the use of the {@link ParameterizedServerTestRunner}. <br/>
 * The test runner executes the test case {@link #testIsGreaterZero()} for each parameter which is returned from
 * {@link #getParameters()}. It creates an instance of the test class for each parameter (and test case), hands the
 * parameter over as constructor argument and executes the test case.
 *
 * @see ParameterizedServerTestRunner
 */
@RunWith(ParameterizedServerTestRunner.class)
public class SampleParameterizedServerTest {

  @Parameters
  public static List<IScoutTestParameter> getParameters() {
    List<IScoutTestParameter> parametersList = new LinkedList<>();
    parametersList.add(new MathTestParameter("Scenario 1", 2));
    parametersList.add(new MathTestParameter("Scenario 2", 5));
    return parametersList;
  }

  private final MathTestParameter m_testParameter;

  public SampleParameterizedServerTest(MathTestParameter testParameter) {
    m_testParameter = testParameter;
  }

  @Test
  public void testIsGreaterZero() {
    assertTrue(m_testParameter.getX() > 0);
  }

  static class MathTestParameter extends AbstractScoutTestParameter {
    private int m_x;

    public MathTestParameter(String name, int x) {
      super(name);
      m_x = x;
    }

    public int getX() {
      return m_x;
    }
  }
}
