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
 * Factory interface to create a service initializer. Classes implementing this interface
 * may be registered using the extension point serviceInitializerFactory
 * 
 * @see AbstractService#initializeService(org.osgi.framework.ServiceRegistration)
 */
public interface IServiceInitializerFactory {

  /**
   * @return provide a new {@link IServiceInitializer} for the given service or null, if no initializing needs to
   *         be done
   */
  IServiceInitializer createInstance(IService service);

}
