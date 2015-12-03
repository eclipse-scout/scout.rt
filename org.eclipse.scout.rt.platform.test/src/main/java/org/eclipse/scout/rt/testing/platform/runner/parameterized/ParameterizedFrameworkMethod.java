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

import org.junit.runners.model.FrameworkMethod;

/**
 * This class represents a method annotated with &#064;Test on a parameterized test class.
 */
public class ParameterizedFrameworkMethod extends FrameworkMethod {
  private final int m_paramsIndex;

  /**
   * @param frameworkMethod
   *          test method
   * @param paramsIndex
   *          index of the test parameter used to execute the test
   */
  public ParameterizedFrameworkMethod(FrameworkMethod frameworkMethod, int paramsIndex) {
    super(frameworkMethod.getMethod());
    this.m_paramsIndex = paramsIndex;
  }

  /**
   * Get the index of the used test parameter.
   */
  public int getParamIndex() {
    return m_paramsIndex;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ParameterizedFrameworkMethod other = (ParameterizedFrameworkMethod) obj;
    if (m_paramsIndex != other.m_paramsIndex) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + m_paramsIndex;
    return result;
  }
}
