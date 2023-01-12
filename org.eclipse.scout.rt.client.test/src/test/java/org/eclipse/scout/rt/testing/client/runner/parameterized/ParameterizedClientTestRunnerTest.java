/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.client.runner.parameterized;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class ParameterizedClientTestRunnerTest {

  @Test
  public void testTestRunner() {
    Result result = JUnitCore.runClasses(SampleParameterizedClientTest.class);
    assertTrue(result.wasSuccessful());
    assertTrue(result.getRunCount() > 0);
  }
}
