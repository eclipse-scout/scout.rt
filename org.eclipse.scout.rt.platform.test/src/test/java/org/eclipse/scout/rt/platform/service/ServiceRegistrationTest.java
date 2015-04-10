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
package org.eclipse.scout.rt.platform.service;

import org.junit.Test;

/**
 *
 */
public class ServiceRegistrationTest {

  @Test
  public void testCustomInitializer() throws Exception {
    // TODO NOOSGI
//    ServiceReference ref = new ServiceReference();
//    ref.setRanking(10);
//    ref.setService(TestLowPrioService.class);
//    SERVICES.registerService(ref);
//
//    Assert.assertTrue(BEANS.get(ITestService.class) instanceof TestLowPrioService);
//
//    SERVICES.unregister(ref);
//
//    ITestService[] services = BEANS.all(ITestService.class);
//    Assert.assertEquals(0, services.length);

  }

  private static interface ITestService extends IService {

  }

  public static class TestLowPrioService extends AbstractService implements ITestService {

  }

}
