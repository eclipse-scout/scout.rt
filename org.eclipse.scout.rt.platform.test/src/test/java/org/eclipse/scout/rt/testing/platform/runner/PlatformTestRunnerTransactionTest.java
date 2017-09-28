/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.testing.platform.runner.statement.TransactionAddFailureOnAnyExceptionStatement;
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

/**
 * Verifies that the Scout transaction (i.e. {@link ITransaction}) is rolled-back, if any unhandled exceptions occur
 * during test execution (e.g. an expected exception is caught by JUnit within the Scout transaction, but the
 * transaction must be rolled-back anyway).
 *
 * @see SafeStatementInvoker
 * @see TransactionAddFailureOnAnyExceptionStatement
 * @since 6.0
 */
public class PlatformTestRunnerTransactionTest {

  @Rule
  public TestName m_name = new TestName();

  @Test
  public void testOk() throws Exception {
    runServerTestRunner(m_name.getMethodName(), false, true);
  }

  @Test
  public void testHandledException() throws Exception {
    runServerTestRunner(m_name.getMethodName(), false, true);
  }

  @Test
  public void testThrowingExpectedRuntimeException() throws Exception {
    runServerTestRunner(m_name.getMethodName(), false, false);
  }

  @Test
  public void testThrowingExpectedAssertionError() throws Exception {
    runServerTestRunner(m_name.getMethodName(), false, false);
  }

  @Test
  public void testThrowingUnexpectedRuntimeException() throws Exception {
    runServerTestRunner(m_name.getMethodName(), true, false);
  }

  @Test
  public void testThrowingUnexpectedAssertionError() throws Exception {
    runServerTestRunner(m_name.getMethodName(), true, false);
  }

  @Test
  public void testExpectingRuntimeExceptionThatIsNotThrown() throws Exception {
    runServerTestRunner(m_name.getMethodName(), true, false);
  }

  @Test
  public void testExpectingAssertionErrorThatIsNotThrown() throws Exception {
    runServerTestRunner(m_name.getMethodName(), true, false);
  }

  @Test
  public void testExpectingPlatformExceptionButIllegalArgumentExceptionIsThrown() throws Exception {
    runServerTestRunner(m_name.getMethodName(), true, false);
  }

  @Test
  public void testExpectedExceptionHandledByExceptionHandler() throws Exception {
    runServerTestRunner(m_name.getMethodName(), false, false);
  }

  @Test
  public void testUnexpectedExceptionHandledByExceptionHandler() throws Exception {
    runServerTestRunner(m_name.getMethodName(), true, false);
  }

  protected static Result runServerTestRunner(String testMethod, boolean expectingTestFails, boolean expectingCommit) throws Exception {
    final Class<?> testClass = PlatformTestRunnerTransactionTestFixture.class;
    final FinalValue<PlatformTestRunnerTransactionTestFixture> testInstance = new FinalValue<PlatformTestRunnerTransactionTestFixture>();
    final PlatformTestRunner serverTestRunner = new PlatformTestRunner(PlatformTestRunnerTransactionTestFixture.class) {
      @Override
      protected Object createTest() throws Exception {
        Object test = super.createTest();
        testInstance.set((PlatformTestRunnerTransactionTestFixture) test);
        return test;
      }
    };

    JUnitCore jUnitCore = new JUnitCore();
    Request req = Request
        .runner(serverTestRunner)
        .filterWith(Filter.matchMethodDescription(Description.createTestDescription(testClass, testMethod)));
    Result result = jUnitCore.run(req);
    assertEquals(0, result.getIgnoreCount());
    assertEquals(1, result.getRunCount());
    int expectedFailureCount = expectingTestFails ? 1 : 0;
    if (result.getFailureCount() != expectedFailureCount) {
      StringBuilder sb = new StringBuilder();
      sb.append("expected ");
      sb.append(expectedFailureCount);
      sb.append(" but caught ");
      sb.append(result.getFailureCount());
      sb.append(":");
      for (Failure f : result.getFailures()) {
        sb.append("\n  ");
        sb.append("Description: '" + f.getDescription() + "'");
        sb.append("\n  ");
        sb.append(f.getException());
        sb.append(Arrays.asList(f.getException().getStackTrace()));
        if (f.getException() != null && f.getException().getCause() != null) {
          sb.append("Cause:");
          sb.append(f.getException().getCause());
          sb.append(Arrays.asList(f.getException().getCause().getStackTrace()));
        }
      }
      Assert.fail(sb.toString());
    }

    final PlatformTestRunnerTransactionTestFixture fixture = testInstance.get();
    assertNotNull(fixture);
    fixture.verifyTransaction(expectingCommit);
    return result;
  }
}
