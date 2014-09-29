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
package org.eclipse.scout.rt.server.services.common.security;

import java.util.Collection;

import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.servicetunnel.RemoteServiceAccessDenied;

/**
 * @deprecated will be removed in N Release
 */
@Deprecated
public interface IClusterSyncAccessControlService extends IAccessControlService {

  /**
   * Clear all caches without firing notifications. This can be useful when some permissions and/or user-role mappings
   * have changed.
   */
  @RemoteServiceAccessDenied
  void clearCacheOfUserIdsNoFire(Collection<String> userIds);

  @RemoteServiceAccessDenied
  void clearCacheNoFire();

}
