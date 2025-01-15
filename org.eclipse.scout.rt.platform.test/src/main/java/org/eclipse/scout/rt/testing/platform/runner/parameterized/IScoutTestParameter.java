/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.runner.parameterized;

/**
 * This interface must be implemented by the test parameter which is used by the {@link ParameterizedServerTestRunner}
 * and the {@link ParameterizedClientTestRunner}. <br/>
 * The test runner will provide this class as input for a test case of a test class.
 */
@FunctionalInterface
public interface IScoutTestParameter {

  /**
   * Get the name of the parameter. It is used in the test result.
   */
  String getName();
}
