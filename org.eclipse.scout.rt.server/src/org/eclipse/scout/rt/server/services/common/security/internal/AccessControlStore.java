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
package org.eclipse.scout.rt.server.services.common.security.internal;

import java.security.Permissions;
import java.util.Collection;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.TTLCache;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.server.services.common.clientnotification.SingleUserFilter;
import org.eclipse.scout.rt.shared.services.common.security.AccessControlChangedNotification;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.ResetAccessControlChangedNotification;
import org.eclipse.scout.service.SERVICES;

/**
 * <p>
 * {@link Permissions} store per userId
 * </p>
 * <p>
 * Maintains a map of one {@link Permissions} object per userId (derived from their Subject, see
 * {@link IAccessControlService#getUserIdOfCurrentSubject()}).
 * </p>
 * <p>
 * The userId is case insensitive, case does not matter.
 * </p>
 */
public class AccessControlStore {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AccessControlStore.class);

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
    String userId = SERVICES.getService(IAccessControlService.class).getUserIdOfCurrentSubject();
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
    String userId = SERVICES.getService(IAccessControlService.class).getUserIdOfCurrentSubject();
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
    // notify clients
    SERVICES.getService(IClientNotificationService.class).putNotification(new AccessControlChangedNotification(p), new SingleUserFilter(userId, 120000L));
  }

  /**
   * clears the cache
   */
  public void clearCache() {
    // notify with a filter, that will be accepted nowhere
    SERVICES.getService(IClientNotificationService.class).putNotification(new ResetAccessControlChangedNotification(), new SingleUserFilter(null, 0L));
    clearCacheOfUserIds(CollectionUtility.hashSet(m_store.keySet()));
  }

  /**
   * clears the cache
   */
  public void clearCacheNoFire() {
    clearCacheOfUserIdsNoFire(CollectionUtility.hashSet(m_store.keySet()));
  }

  /**
   * Clears the cache for a set of userIds and sends a notification for these users.
   * 
   * @param userIds
   *          derived from the Subject, see{@link IAccessControlService#getUserIdOfCurrentSubject()}
   */
  public void clearCacheOfUserIds(Collection<String> userIds0) {
    clearCacheOfUserIdsNoFire(userIds0);
    //notify clients
    for (String userId : userIds0) {
      if (userId != null) {
        SERVICES.getService(IClientNotificationService.class).putNotification(new AccessControlChangedNotification(null), new SingleUserFilter(userId, 120000L));
      }
    }
  }

  /**
   * Clears the cache for a set of userIds and sends a notification for these users.
   * 
   * @param userIds
   *          derived from the Subject, see{@link IAccessControlService#getUserIdOfCurrentSubject()}
   */
  public void clearCacheOfUserIdsNoFire(Collection<String> userIds0) {
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

  public String[] getUserIds() {
    synchronized (m_storeLock) {
      return m_store.keySet().toArray(new String[m_store.keySet().size()]);
    }
  }
}
