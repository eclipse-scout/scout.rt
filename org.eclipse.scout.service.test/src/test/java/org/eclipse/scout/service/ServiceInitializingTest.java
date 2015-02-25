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

import org.eclipse.scout.commons.annotations.Priority;
import org.junit.Test;

/**
 *
 */
public class ServiceInitializingTest {

  @Test
  public void testCustomInitializer() throws Exception {
    // TODO NOOSGI
//    OBJ.register(P_TestService.class);
//    ServiceInitializerReference initializerRef = new ServiceInitializerReference();
//    initializerRef.setServiceInitializerFactoryClass(P_TestServiceInitializerFactory.class);
//    initializerRef.setRanking(20);
//    SERVICES.registerServiceInitializerFactory(initializerRef);
//
//    Assert.assertTrue(SERVICES.getService(P_TestService.class).isCustomInitialized());

  }

  @Priority(10)
  private static class P_TestService extends AbstractService {
    private boolean m_custumInitialized;

    public void setCustumInitialized(boolean custumInitialized) {
      m_custumInitialized = custumInitialized;
    }

    public boolean isCustomInitialized() {
      return m_custumInitialized;
    }

  }

  @Priority(20)
  private static class P_TestServiceInitializerFactory implements IServiceInitializerFactory {

    @Override
    public IServiceInitializer createInstance(IService service) {
      return new P_TestServiceInitializer();
    }

  }

  @Priority(10l)
  private static class P_TestServiceInitializer implements IServiceInitializer {

    @Override
    public ServiceInitializerResult initializeService(IService service) {
      if (service instanceof P_TestService) {
        ((P_TestService) service).setCustumInitialized(true);
      }
      return ServiceInitializerResult.STOP;
    }
  }

}
