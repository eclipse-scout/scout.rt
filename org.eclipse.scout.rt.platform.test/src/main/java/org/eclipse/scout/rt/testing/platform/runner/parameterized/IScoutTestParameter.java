/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform.runner.parameterized;

/**
 * This interface must be implemented by the test parameter which is used by the {@link ParameterizedServerTestRunner}
 * and the {@link ParameterizedClientTestRunner}. <br/>
 * The test runner will provide this class as input for a test case of a test class.
 */
@FunctionalInterface
public interface IScoutTestParameter {

  /** Get the name of the parameter. It is used in the test result. */
  String getName();
}
