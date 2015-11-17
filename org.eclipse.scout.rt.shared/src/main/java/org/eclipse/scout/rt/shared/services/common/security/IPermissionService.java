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
package org.eclipse.scout.rt.shared.services.common.security;

import java.security.Permission;
import java.util.Set;

import org.eclipse.scout.rt.platform.service.IService;

/**
 * Support service for querying available Permission types.
 */
public interface IPermissionService extends IService {

  /**
   * @return Returns all permissions classes that are necessary for this scout application. The actual strategy to find
   *         these permissions is up to the implementation.
   */
  Set<Class<? extends Permission>> getAllPermissionClasses();

}
