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

/**
 * Interface for a service initializer.
 *
 * @see IServiceInitializerFactory
 */
public interface IServiceInitializer {

  enum ServiceInitializerResult {
    /**
     * lets the following initializer do their job
     */
    CONTINUE,
    /**
     * breaks up and ignores the following initializers
     */
    STOP
  }

  /**
   * @return one of the ServiceInitializerResult enums.
   *         ServiceInitializerResult.CONINUE lets the following initializer do their job
   *         ServiceInitializerResult.STOP breaks up and ignores the following initializers.
   */
  ServiceInitializerResult initializeService(IService service);

}
