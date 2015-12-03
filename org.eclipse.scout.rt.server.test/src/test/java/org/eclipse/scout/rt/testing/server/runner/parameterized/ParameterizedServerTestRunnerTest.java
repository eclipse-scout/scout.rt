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
package org.eclipse.scout.rt.testing.server.runner.parameterized;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class ParameterizedServerTestRunnerTest {

  @Test
  public void testTestRunner() {
    Result result = JUnitCore.runClasses(SampleParameterizedServerTest.class);
    assertTrue(result.wasSuccessful());
    assertTrue(result.getRunCount() > 0);
  }
}
