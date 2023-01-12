/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
