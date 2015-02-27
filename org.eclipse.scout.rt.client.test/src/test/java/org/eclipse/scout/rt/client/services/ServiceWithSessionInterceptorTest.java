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
package org.eclipse.scout.rt.client.services;

import org.eclipse.scout.rt.platform.cdi.ApplicationScoped;
import org.eclipse.scout.rt.platform.cdi.IBean;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.IService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(ClientTestRunner.class)
public class ServiceWithSessionInterceptorTest {

  private static IBean<TestService> m_bean01;

  @BeforeClass
  public static void setUp() {
    m_bean01 = OBJ.registerClass(TestService.class);
  }

  @AfterClass
  public static void tearDown() {
    OBJ.unregisterBean(m_bean01);
  }

  @Test
  public void testService() throws Exception {
    OBJ.one(ITestService.class).doit();
  }

  private static interface ITestService extends IService {
    void doit();

  }

  @ApplicationScoped
  private static class TestService extends AbstractService implements ITestService {
    @Override
    public void doit() {
    }
  }
}
