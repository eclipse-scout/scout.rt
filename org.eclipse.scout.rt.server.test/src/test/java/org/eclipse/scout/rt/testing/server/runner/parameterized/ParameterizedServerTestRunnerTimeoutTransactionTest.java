/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.server.runner.parameterized;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.runner.parameterized.AbstractScoutTestParameter;
import org.eclipse.scout.rt.testing.platform.runner.parameterized.IScoutTestParameter;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunnerSameSessionTest.JUnitServerSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

/**
 * @since 5.1
 */
@RunWith(ParameterizedServerTestRunner.class)
@RunWithServerSession(JUnitServerSession.class)
@RunWithSubject("anna")
public class ParameterizedServerTestRunnerTimeoutTransactionTest {

  private Map<String, Set<ITransaction>> s_protocolByTestMethod;
  private static Map<String, Integer> s_expectedTransactionCountByTestMethod;

  @Rule
  public TestName m_name = new TestName();

  private ParameterizedServerTestRunnerTestParameter m_param;

  public ParameterizedServerTestRunnerTimeoutTransactionTest(ParameterizedServerTestRunnerTestParameter param) {
    m_param = param;

    s_protocolByTestMethod = new HashMap<>();
    s_expectedTransactionCountByTestMethod = new HashMap<>();
    s_expectedTransactionCountByTestMethod.put("testDefault", 1);
    s_expectedTransactionCountByTestMethod.put("testTimeout", 2);
    s_expectedTransactionCountByTestMethod.put("testDefaultExpectException", 1);
    s_expectedTransactionCountByTestMethod.put("testTimeoutExpectException", 2);
  }

  @Parameters
  public static List<IScoutTestParameter> getParameters() {
    List<IScoutTestParameter> parametersList = new LinkedList<>();
    parametersList.add(new ParameterizedServerTestRunnerTestParameter("Scenario 1"));
    parametersList.add(new ParameterizedServerTestRunnerTestParameter("Scenario 2"));
    return parametersList;
  }

  protected void updateProtocol() {
    String testName = getMethodName();
    Set<ITransaction> p = s_protocolByTestMethod.get(testName);
    if (p == null) {
      p = new HashSet<>(3);
      s_protocolByTestMethod.put(testName, p);
    }
    p.add(ITransaction.CURRENT.get());
  }

  protected void verifyProtocol() {
    String testName = getMethodName();
    Integer expectedTransactionCount = s_expectedTransactionCountByTestMethod.get(testName);
    assertNotNull(expectedTransactionCount);

    Set<ITransaction> transactions = s_protocolByTestMethod.get(testName);
    int actualTransactionCount = transactions == null ? 0 : transactions.size();

    assertEquals(expectedTransactionCount.intValue(), actualTransactionCount);
  }

  protected String getMethodName() {
    String baseName = m_name.getMethodName();
    return baseName.substring(0, baseName.length() - (m_param.getName().length() + 3));
  }

  @Before
  public void before() {
    updateProtocol();
  }

  @After
  public void after() {
    updateProtocol();
    verifyProtocol();
  }

  @Test
  public void testDefault() {
    updateProtocol();
  }

  @Test(timeout = 1000)
  public void testTimeout() {
    updateProtocol();
  }

  @Test(expected = PlatformException.class)
  public void testDefaultExpectException() {
    updateProtocol();
    throw new PlatformException("throwed intentionally");
  }

  @Test(timeout = 1000, expected = PlatformException.class)
  public void testTimeoutExpectException() {
    updateProtocol();
    throw new PlatformException("throwed intentionally");
  }

  public static class ParameterizedServerTestRunnerTestParameter extends AbstractScoutTestParameter {

    public ParameterizedServerTestRunnerTestParameter(String name) {
      super(name);
    }
  }
}
