/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security;

import java.util.List;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.cache.ICache;
import org.eclipse.scout.rt.platform.cache.ICacheEntryFilter;
import org.eclipse.scout.rt.platform.cache.ICacheInvalidationListener;
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

  /**
   * Adds a new listener to be notified when access control cache entries are invalidated. The listener is fired after
   * the entries have already been removed.
   *
   * @param listener
   *          The listener to add. The {@link ICacheEntryFilter} given to the listener is the filter passed to
   *          {@link ICache#invalidate(ICacheEntryFilter, boolean)}.
   */
  void addInvalidationListener(ICacheInvalidationListener<Object, IPermissionCollection> listener);

  /**
   * Removes the given listener.
   */
  void removeInvalidationListener(ICacheInvalidationListener<Object, IPermissionCollection> listener);

  /**
   * @return All registered invalidation listeners.
   */
  List<ICacheInvalidationListener<Object, IPermissionCollection>> getInvalidationListeners();

  /**
   * @param cacheKey
   *          A cacheKey used by the internal cache of this service.
   * @return The userId (username) of the given cacheKey.
   */
  String getUserIdForCacheKey(Object cacheKey);
}
