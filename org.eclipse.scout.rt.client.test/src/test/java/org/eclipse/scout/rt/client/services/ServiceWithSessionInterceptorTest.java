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
package org.eclipse.scout.rt.client.services;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ServiceWithSessionInterceptorTest {

  private static IBean<TestService> m_bean01;

  @BeforeClass
  public static void setUp() {
    m_bean01 = Platform.get().getBeanManager().registerClass(TestService.class);
  }

  @AfterClass
  public static void tearDown() {
    Platform.get().getBeanManager().unregisterBean(m_bean01);
  }

  @Test
  public void testService() throws Exception {
    BEANS.get(ITestService.class).doit();
  }

  private static interface ITestService extends IService {
    void doit();

  }

  @ApplicationScoped
  private static class TestService implements ITestService {
    @Override
    public void doit() {
    }
  }
}
