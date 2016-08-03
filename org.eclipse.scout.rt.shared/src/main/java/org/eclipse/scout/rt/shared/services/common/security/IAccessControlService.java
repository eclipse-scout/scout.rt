/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.security.PermissionCollection;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;

/**
 * Access control facility. You may use {@link AbstractAccessControlService} as a base for the implementation which uses
 * internally an {@link ICache}.
 */
public interface IAccessControlService extends IService {

  /**
   * @return current UserId extracted from current {@link Subject}
   */
  String getUserIdOfCurrentSubject();

  /**
   * @return current UserId extracted from the provided {@link Subject}
   */
  String getUserId(Subject subject);

  /**
   * Checks the given permission against the current users {@link PermissionCollection}
   *
   * @param p
   * @return true if the permission <tt>p</tt> is granted to the current user
   */
  boolean checkPermission(Permission p);

  /**
   * @param p
   * @return permission level of permission <tt>p</tt> that is granted to the current user. For permissions not
   *         extending {@link BasicHierarchyPermission} this is either ALL or NONE depending if the permission granted
   *         to the user or not.
   */
  int getPermissionLevel(Permission p);

  /**
   * Only use this method to transfer permissions, don't use it for access control or adding permissions see also
   * {@link #checkPermission(Permission)} and {@link #getPermissionLevel(BasicHierarchyPermission)}
   *
   * @return PermissionCollection for the current user
   */
  PermissionCollection getPermissions();

  /**
   * Clear all caches. This can be useful when some permissions and/or user-role mappings have changed.
   */
  void clearCache();

  /**
   * Invalidates the cached {@link PermissionCollection} of the current user.
   */
  void clearCacheOfCurrentUser();
}
