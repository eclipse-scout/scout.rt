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

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.osgi.BundleClassDescriptor;
import org.eclipse.scout.service.IService;

/**
 * Support service for querying all Permission types available in any Plug-Ins class set.
 */
@Priority(-3)
public interface IPermissionService extends IService {

  /**
   * @return all permissions in the package <bundle-name>.security and its sub packages of any loaded bundle
   */
  BundleClassDescriptor[] getAllPermissionClasses();

}
