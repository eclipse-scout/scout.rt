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
package org.eclipse.scout.rt.testing.client.runner.parameterized;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.runner.parameterized.AbstractScoutTestParameter;
import org.eclipse.scout.rt.testing.platform.runner.parameterized.IScoutTestParameter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

/**
 * This test illustrates the use of the {@link ParameterizedClientTestRunner}. <br/>
 * The test runner executes the test case {@link #testIsGreaterZero()} for each parameter which is returned from
 * {@link #getParameters()}. It creates an instance of the test class for each parameter (and test case), hands the
 * parameter over as constructor argument and executes the test case.
 *
 * @see ParameterizedClientTestRunner
 */
@RunWith(ParameterizedClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class SampleParameterizedClientTest {

  @Parameters
  public static List<IScoutTestParameter> getParameters() {
    List<IScoutTestParameter> parametersList = new LinkedList<IScoutTestParameter>();
    parametersList.add(new MathTestParameter("Scenario 1", 2));
    parametersList.add(new MathTestParameter("Scenario 2", 5));
    return parametersList;
  }

  private final MathTestParameter m_testParameter;

  public SampleParameterizedClientTest(MathTestParameter testParameter) {
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
