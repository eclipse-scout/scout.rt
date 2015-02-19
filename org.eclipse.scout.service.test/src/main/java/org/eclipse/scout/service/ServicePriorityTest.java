/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.service;

import org.junit.Test;

/**
 *
 */
public class ServicePriorityTest {

  @Test
  public void testCuntomInitializer() throws Exception {
//    ServiceReference ref = new ServiceReference();
//    ref.setRanking(10);
//    ref.setService(TestLowPrioService.class);
//    SERVICES.registerService(ref);
//
//    ref = new ServiceReference();
//    ref.setRanking(20);
//    ref.setService(TestHighPrioService.class);
//    SERVICES.registerService(ref);
//
//    Assert.assertTrue(SERVICES.getService(ITestService.class) instanceof TestHighPrioService);
//
//    ITestService[] services = SERVICES.getServices(ITestService.class);
//    Assert.assertTrue(services[0] instanceof TestHighPrioService);
//    Assert.assertTrue(services[1] instanceof TestLowPrioService);

  }

  private static interface ITestService extends IService {

  }

  private static class TestLowPrioService extends AbstractService implements ITestService {

  }

  private static class TestHighPrioService extends AbstractService implements ITestService {

  }

}
