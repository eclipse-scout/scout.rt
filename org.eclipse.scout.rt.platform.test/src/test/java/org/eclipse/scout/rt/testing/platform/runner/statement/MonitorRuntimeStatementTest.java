/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.runner.statement;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runners.MethodSorters;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class MonitorRuntimeStatementTest {

  @Test
  public void testStatement() throws InitializationError {
    Result result = new JUnitCore().run(createRunner());

    assertEquals(3, result.getRunCount()); // all tests must be run
    assertEquals(1, result.getFailureCount()); // only testFirst may fail
    assertEquals(0, result.getIgnoreCount()); // nothing should be ignored
    assertTrue(result.getRunTime() > 1000L); // two sleeps below, must run at least 1 second
  }

  public PlatformTestRunner createRunner() throws InitializationError {
    return new PlatformTestRunner(TestCase.class) { // customized runner just for our test case below
      @Override
      protected Statement withMaximumRuntime(FrameworkMethod method, Statement next) {
        return new MonitorRuntimeStatement(method, next) {
          @Override
          protected long getConfiguredDefaultMaximumRuntime() {
            return 250; // shorter runtime for testing
          }
        };
      }
    };
  }

  // no actual test cases; should just be run/used within other test cases
  @SuppressWarnings({"JUnitMalformedDeclaration", "JUnit3StyleTestMethodInJUnit4Class"})
  @FixMethodOrder(MethodSorters.NAME_ASCENDING)
  public static class TestCase {

    @Test
    public void testFirst() throws InterruptedException {
      Thread.sleep(500);
    }

    @Test(timeout = 5000)
    public void testSecond() throws InterruptedException {
      Thread.sleep(500);
    }

    @Test
    public void testThird() {
      // nop: just an ordinary test method which should be executed (not influenced by previous failures)
    }
  }
}
