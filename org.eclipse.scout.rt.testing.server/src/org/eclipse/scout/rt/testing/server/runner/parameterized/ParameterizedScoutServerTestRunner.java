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
package org.eclipse.scout.rt.testing.server.runner.parameterized;

import java.util.List;

import org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner;
import org.eclipse.scout.rt.testing.shared.runner.parameterized.IScoutTestParameter;
import org.eclipse.scout.rt.testing.shared.runner.parameterized.ParameterizedFrameworkMethod;
import org.eclipse.scout.rt.testing.shared.runner.parameterized.ParameterizedTestRunnerExtension;
import org.junit.runner.Description;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Parameterized form of {@link ScoutServerTestRunner}. <br/>
 * <b>Example:</b>
 *
 * <pre>
 * &#064;RunWith(ParameterizedScoutServerTestRunner.class)
 * public class SampleParameterizedServerTest {
 * 
 *   &#064;Parameters
 *   public static List&lt;IScoutTestParameter&gt; getParameters() {
 *     List&lt;IScoutTestParameter&gt; parametersList = new LinkedList&lt;IScoutTestParameter&gt;();
 *     parametersList.add(new MathTestParameter(&quot;Scenario 1&quot;, 2));
 *     parametersList.add(new MathTestParameter(&quot;Scenario 2&quot;, 5));
 *     return parametersList;
 *   }
 * 
 *   private final MathTestParameter m_testParameter;
 * 
 *   public SampleParameterizedServerTest(MathTestParameter testParameter) {
 *     m_testParameter = testParameter;
 *   }
 * 
 *   &#064;Test
 *   public void testIsGreaterZero() {
 *     assertTrue(m_testParameter.getX() &gt; 0);
 *   }
 * 
 *   static class MathTestParameter extends AbstractScoutTestParameter {
 *     private int m_x;
 * 
 *     public MathTestParameter(String name, int x) {
 *       super(name);
 *       m_x = x;
 *     }
 * 
 *     public int getX() {
 *       return m_x;
 *     }
 *   }
 * }
 * </pre>
 *
 * @see ParameterizedScoutClientTestRunner
 * @see Parameterized
 * @see Parameters
 */
public class ParameterizedScoutServerTestRunner extends ScoutServerTestRunner {

  /** Parameters returned by the <code>@</code>{@link Parameters} annotated method in the test class. */
  private List<IScoutTestParameter> m_parameterList;
  /** Parameter for the current test method being executed. */
  private IScoutTestParameter m_currentTestParameter = null;

  public ParameterizedScoutServerTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  protected List<FrameworkMethod> getChildren() {
    m_parameterList = ParameterizedTestRunnerExtension.loadParameterList(getTestClass());
    return ParameterizedTestRunnerExtension.createParameterizedTestMethods(super.getChildren(), m_parameterList.size());
  }

  @Override
  protected final Object createTest() throws Exception {
    return createTestInternal(m_currentTestParameter);
  }

  protected Object createTestInternal(IScoutTestParameter testParameter) throws Exception {
    return ParameterizedTestRunnerExtension.createTest(getTestClass(), testParameter);
  }

  @Override
  protected Statement methodBlock(FrameworkMethod method) {
    int paramsIndex = ((ParameterizedFrameworkMethod) method).getParamIndex();
    m_currentTestParameter = m_parameterList.get(paramsIndex);

    return super.methodBlock(method);
  }

  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    super.collectInitializationErrors(errors);
    validateParametersMethod(errors);
  }

  @Override
  protected void validateConstructor(List<Throwable> errors) {
    validateOnlyOneConstructor(errors);
  }

  /** Validate the method which specifies the test parameters. */
  protected void validateParametersMethod(List<Throwable> errors) {
    ParameterizedTestRunnerExtension.validateOneParametersMethod(getTestClass(), errors);
  }

  @Override
  protected Description describeChild(FrameworkMethod method) {
    return ParameterizedTestRunnerExtension.describeChild(getTestClass(), (ParameterizedFrameworkMethod) method, testName(method), m_parameterList);
  }
}
