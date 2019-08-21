/*******************************************************************************
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.security;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.service.IService;

/**
 * Service providing current users permission collection
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
   * Returns the {@link IPermissionCollection} for the current user.
   * <p>
   * If no permission collection can be determined for the current subject or there is no current subject this method
   * should return either {@link NonePermissionCollection} or {@link AllPermissionCollection} but not {@code null}. An
   * implementor of this interface should document this decision.
   *
   * @return permission collection, never null
   */
  IPermissionCollection getPermissions();

  /**
   * Invalidates the cached {@link IPermissionCollection} of the current user.
   */
  void clearCacheOfCurrentUser();

  /**
   * Invalidates any cached {@link IPermissionCollection}s.
   */
  void clearCache();
}
