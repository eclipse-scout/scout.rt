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

import org.osgi.framework.ServiceRegistration;

/**
 * Convenience {@link IService} implementation with support for config.ini
 * variable injection. see {@link ServiceUtility#injectConfigProperties(IService)}
 */
public abstract class AbstractService implements IService2 {

  public AbstractService() {
  }

  /**
   * This default implementation calls {@link
   * ServiceUtility#injectConfigParams(this)}
   * 
   * @deprecated use {@link AbstractService#initializeService(ServiceRegistration)} instead.
   */
  @Deprecated
  public void initializeService() {
    ServiceUtility.injectConfigProperties(this);
  }

  /**
   * calls the "old" initialization method to ensure properties getting initialized. This method can be overwritten by
   * implementers. Implementers should aware the property injection is only done if the super call is made.
   * 
   * @see AbstractService#initializeService()
   */
  public void initializeService(ServiceRegistration registration) {
    initializeService();
  }

  /**
   * This default implementation does nothing
   */
  public void disposeServices() {
  }

}
