/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.server.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.TestTimedOutException;

/**
 * @since 5.1
 */
public class ServerTestRunnerTimeoutTest {

  @Rule
  public TestName m_name = new TestName();

  @Test
  public void testTimeoutNotExeeded() throws Exception {
    runServerTestRunner(ServerTestRunnerTimeoutTestFixture.class, m_name.getMethodName(), 0);
  }

  @Test
  public void testTimeoutNotExceededButExceptionExpected() throws Exception {
    runServerTestRunner(ServerTestRunnerTimeoutTestFixture.class, m_name.getMethodName(), 0);
  }

  @Test
  public void testTimeoutExceeded() throws Exception {
    Result result = runServerTestRunner(ServerTestRunnerTimeoutTestFixture.class, m_name.getMethodName(), 1);
    Failure f = result.getFailures().get(0);
    assertNotNull(f);
    assertTrue(f.getException() instanceof TestTimedOutException);
  }

  protected static Result runServerTestRunner(Class<?> testClass, String testMethod, int expectedFailureCount) throws Exception {
    JUnitCore jUnitCore = new JUnitCore();
    Request req = Request.runner(new ServerTestRunner(testClass));
    req = req.filterWith(Filter.matchMethodDescription(Description.createTestDescription(testClass, testMethod)));
    Result result = jUnitCore.run(req);
    assertEquals(0, result.getIgnoreCount());
    assertEquals(1, result.getRunCount());
    if (expectedFailureCount != result.getFailureCount()) {
      StringBuilder sb = new StringBuilder();
      sb.append("expected ");
      sb.append(expectedFailureCount);
      sb.append(" but caught ");
      sb.append(result.getFailureCount());
      sb.append(":");
      for (Failure f : result.getFailures()) {
        sb.append("\n  ");
        sb.append(f.getException());
        sb.append(Arrays.asList(f.getException().getStackTrace()));
        if (f.getException() != null && f.getException().getCause() != null) {
          sb.append("Cause:");
          sb.append(f.getException().getCause());
        }
      }
      Assert.fail(sb.toString());
    }
    return result;
  }
}
