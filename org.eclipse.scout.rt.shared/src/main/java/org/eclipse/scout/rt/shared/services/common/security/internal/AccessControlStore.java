/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.security.internal;

import java.security.Permissions;
import java.util.Collection;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.collection.TTLCache;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;

/**
 * <p>
 * {@link Permissions} store per userId without any notifications.
 * </p>
 * <p>
 * Maintains a map of one {@link Permissions} object per userId (derived from their Subject, see
 * {@link IAccessControlService#getUserIdOfCurrentSubject()}).
 * </p>
 * <p>
 * The userId is case insensitive, case does not matter.
 * </p>
 *
 * @since 4.3.0 (Mars-M5)
 * @deprecated replaced with {@link ICache}. Will be removed in Scout 6.1.
 */
@Deprecated
public class AccessControlStore {
  /**
   * the internal store, the {@link String} used as key is always lower case
   */
  private TTLCache<String/* userId */, Permissions> m_store;
  private Object m_storeLock;

  public AccessControlStore() {
    m_storeLock = new Object();
    m_store = new TTLCache<String, Permissions>(3600000L);
  }

  /**
   * @return the permission collection that is associated with the current subject
   */
  public Permissions getPermissionsOfCurrentSubject() {
    String userId = BEANS.get(IAccessControlService.class).getUserIdOfCurrentSubject();
    if (userId == null) {
      return null;
    }
    return getPermissions(userId);
  }

  /**
   * sets permission collection that is associated with the current subject
   *
   * @param p
   *          permission collection
   */
  public void setPermissionsOfCurrentSubject(Permissions p) {
    String userId = BEANS.get(IAccessControlService.class).getUserIdOfCurrentSubject();
    if (userId == null) {
      throw new SecurityException("userId is null");
    }
    setPermissions(userId, p);
  }

  /**
   * @param userId
   *          of Subject
   * @return the permission collection that is associated with this userId, <code>null</code> if the parameter is
   *         <code>null</code>
   */
  public Permissions getPermissions(String userId) {
    if (userId == null) {
      return null;
    }
    synchronized (m_storeLock) {
      return m_store.get(userId.toLowerCase());
    }
  }

  /**
   * associate a permission collection with this userId
   *
   * @param userId
   *          if userId is <code>null</code> the method does nothing
   */
  public void setPermissions(String userId, Permissions p) {
    if (userId == null) {
      return;
    }
    synchronized (m_storeLock) {
      if (p == null) {
        p = new Permissions();
        p.setReadOnly();
      }
      m_store.put(userId.toLowerCase(), p);
    }
  }

  /**
   * clears the cache
   */
  public void clearCache() {
    clearCacheOfUserIds(getUserIds());
  }

  /**
   * Clears the cache for a set of userIds and sends a notification for these users.
   *
   * @param userIds
   *          derived from the Subject, see{@link IAccessControlService#getUserIdOfCurrentSubject()}
   */
  public void clearCacheOfUserIds(Collection<String> userIds0) {
    Set<String> userIds = CollectionUtility.hashSetWithoutNullElements(userIds0);
    if (userIds.isEmpty()) {
      return;
    }
    synchronized (m_storeLock) {
      for (String userId : userIds) {
        if (userId != null) {
          m_store.remove(userId.toLowerCase());
        }
      }
    }
  }

  public Collection<String> getUserIds() {
    synchronized (m_storeLock) {
      return CollectionUtility.hashSet(m_store.keySet());
    }
  }
}
