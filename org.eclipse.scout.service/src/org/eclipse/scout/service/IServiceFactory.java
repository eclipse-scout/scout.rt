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

import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * Service factory interface
 * 
 * @since 3.5.0
 */
public interface IServiceFactory extends ServiceFactory {

  /**
   * This method is called by the services extension manager just after registering this service factory.
   */
  void serviceRegistered(ServiceRegistration reg) throws Throwable;

}
