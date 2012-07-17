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

/**
 * By default, properties in config.ini bean properties are injected when a service is initialized.
 * 
 * @see ServiceUtility#injectConfigProperties
 */
public class DefaultServiceInitializer implements IServiceInitializer {

  @Override
  public double getRunOrder() {
    return 50;
  }

  /**
   * {@inheritDoc} injects properties in config.ini lets the following initializers do their job
   */
  @Override
  public ServiceInitializerResult initializeService(IService service) {
    ServiceUtility.injectConfigProperties(service);
    return ServiceInitializerResult.CONTINUE;
  }

}
