/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.runner;

import static org.junit.Assert.assertNotNull;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class MockBeanTest {

  @BeanMock
  private ITestA testBean;

  @Test
  public void testInitializeBeanMock() {
    assertNotNull(testBean);
    assertNotNull(BEANS.opt(ITestA.class));
  }

  public static interface ITestA {
  }
}
