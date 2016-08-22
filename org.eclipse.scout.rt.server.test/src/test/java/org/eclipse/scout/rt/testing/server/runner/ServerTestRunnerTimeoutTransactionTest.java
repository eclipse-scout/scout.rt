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
package org.eclipse.scout.rt.testing.server.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunnerSameSessionTest.JUnitServerSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

/**
 * @since 5.1
 */
@RunWith(ServerTestRunner.class)
@RunWithServerSession(JUnitServerSession.class)
@RunWithSubject("anna")
public class ServerTestRunnerTimeoutTransactionTest {

  private static Map<String, Set<ITransaction>> s_protocolByTestMethod;
  private static Map<String, Integer> s_expectedTransactionCountByTestMethod;

  @Rule
  public TestName m_name = new TestName();

  @BeforeClass
  public static void beforeClass() {
    s_protocolByTestMethod = new HashMap<String, Set<ITransaction>>();
    s_expectedTransactionCountByTestMethod = new HashMap<>();
    s_expectedTransactionCountByTestMethod.put("testDefault", 1);
    s_expectedTransactionCountByTestMethod.put("testTimeout", 2);
    s_expectedTransactionCountByTestMethod.put("testDefaultExpectException", 1);
    s_expectedTransactionCountByTestMethod.put("testTimeoutExpectException", 2);
  }

  @AfterClass
  public static void afterClass() {
    assertEquals(0, s_expectedTransactionCountByTestMethod.size());
  }

  protected void updateProtocol() {
    String testName = m_name.getMethodName();
    Set<ITransaction> p = s_protocolByTestMethod.get(testName);
    if (p == null) {
      p = new HashSet<>(3);
      s_protocolByTestMethod.put(testName, p);
    }
    p.add(ITransaction.CURRENT.get());
  }

  protected void verifyProtocol() {
    String testName = m_name.getMethodName();
    Integer expectedTransactionCount = s_expectedTransactionCountByTestMethod.remove(testName);
    assertNotNull(expectedTransactionCount);

    Set<ITransaction> transactions = s_protocolByTestMethod.get(testName);
    int actualTransactionCount = transactions == null ? 0 : transactions.size();

    assertEquals(expectedTransactionCount.intValue(), actualTransactionCount);
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
}
