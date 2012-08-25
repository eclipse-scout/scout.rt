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

import org.osgi.framework.BundleContext;

/**
 * Additional service constants for use with osgi service, see
 * {@link BundleContext#registerService(String, Object, java.util.Dictionary)}
 */
public interface ServiceConstants {
  /**
   * osgi service init parameter that defines the scope of a service.
   * Only valid together with a service factory.
   * The value is of type String (may be a classname) and is used by the corresponding service factory to filter the
   * service usage based on the scope.
   */
  String SERVICE_SCOPE = "service.scope";

  /**
   * osgi service init parameter that acts as hint for the service factory to start the service automatically (as soon
   * as
   * possible).
   * Only makes sense together with a service factory.
   * The value is of type Boolean with default value false.
   */
  String SERVICE_CREATE_IMMEDIATELY = "service.createImmediately";

}
