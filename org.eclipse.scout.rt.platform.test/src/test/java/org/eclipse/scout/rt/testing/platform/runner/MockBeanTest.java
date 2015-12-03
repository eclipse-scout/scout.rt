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
