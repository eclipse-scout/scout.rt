/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.client.runner;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.context.PropertyMap;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunnerDifferentSubjectTest.JUnitClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
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
@RunWith(ClientTestRunner.class)
@RunWithClientSession(JUnitClientSession.class)
@RunWithSubject("anna")
public class ClientTestRunnerTimeoutPropertyMapTest {

  private static Map<String, Set<PropertyMap>> s_protocolByTestMethod;
  private static Map<String, Integer> s_expectedPropertyMapCountByTestMethod;

  @Rule
  public TestName m_name = new TestName();

  @BeforeClass
  public static void beforeClass() {
    s_protocolByTestMethod = new HashMap<>();
    s_expectedPropertyMapCountByTestMethod = new HashMap<>();
    s_expectedPropertyMapCountByTestMethod.put("testDefault", 1);
    s_expectedPropertyMapCountByTestMethod.put("testTimeout", 3);
    s_expectedPropertyMapCountByTestMethod.put("testDefaultExpectException", 1);
    s_expectedPropertyMapCountByTestMethod.put("testTimeoutExpectException", 3);
  }

  @AfterClass
  public static void afterClass() {
    assertEquals(0, s_expectedPropertyMapCountByTestMethod.size());
  }

  protected void updateProtocol() {
    String testName = m_name.getMethodName();
    Set<PropertyMap> p = s_protocolByTestMethod.get(testName);
    if (p == null) {
      p = new HashSet<>(3);
      s_protocolByTestMethod.put(testName, p);
    }
    p.add(PropertyMap.CURRENT.get());
  }

  protected void verifyProtocol() {
    String testName = m_name.getMethodName();
    Integer expectedTransactionCount = s_expectedPropertyMapCountByTestMethod.remove(testName);
    assertNotNull(expectedTransactionCount);

    Set<PropertyMap> transactions = s_protocolByTestMethod.get(testName);
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
