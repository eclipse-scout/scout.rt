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

import java.security.AccessController;
import java.security.Permissions;
import java.security.Principal;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.TTLCache;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.server.services.common.clientnotification.PrincipalNameFilter;
import org.eclipse.scout.rt.shared.services.common.security.AccessControlChangedNotification;
import org.eclipse.scout.rt.shared.services.common.security.ResetAccessControlChangedNotification;
import org.eclipse.scout.service.SERVICES;

/**
 * <p>
 * {@link Permissions} store per principal name
 * </p>
 * <p>
 * Maintains a map of one {@link Permissions} object per principal (name).
 * </p>
 * <p>
 * The principal name is case insensitive, case does not matter.
 * </p>
 */
public class AccessControlStore {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AccessControlStore.class);

  /**
   * the internal store, the {@link String} used as key is always lower case
   */
  private TTLCache<String/* principalName */, Permissions> m_store;
  private Object m_storeLock;

  public AccessControlStore() {
    m_storeLock = new Object();
    m_store = new TTLCache<String, Permissions>(3600000L);
  }

  /**
   * @return the first permission collection that is associated with a principal
   *         name of the current subject
   */
  public Permissions getPermissionsOfCurrentSubject() {
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject != null) {
      for (Principal principal : subject.getPrincipals()) {
        if (principal != null) {
          String principalName = principal.getName();
          if (principalName != null) {
            principalName = principalName.toLowerCase();
          }
          Permissions permissions = getPermissions(principalName);
          if (permissions != null) {
            return permissions;
          }
        }
      }
    }
    return null;
  }

  /**
   * sets permission collection that is associated with a principal
   * name of the current subject
   * 
   * @param p
   *          permission collection
   */
  public void setPermissionsOfCurrentSubject(Permissions p) {
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null) throw new SecurityException("subject is null");
    for (Principal principal : subject.getPrincipals()) {
      if (principal != null) {
        String principalName = principal.getName();
        if (principalName != null) {
          principalName = principalName.toLowerCase();
        }
        setPermissions(principalName, p);
        return;
      }
    }
    throw new SecurityException("subject contains no principals");
  }

  /**
   * @param principalName
   *          name of principal, case-insensitive
   * @return the permission collection that is associated with this principal
   *         name, <code>null</code> if the parameter is <code>null</code>
   */
  public Permissions getPermissions(String principalName) {
    if (principalName == null) {
      return null;
    }
    synchronized (m_storeLock) {
      return m_store.get(principalName.toLowerCase());
    }
  }

  /**
   * associate a permission collection with this principal name
   * 
   * @param principalName
   *          name of principal, case-insensitive, if <code>null</code> method
   *          does nothing
   */
  public void setPermissions(String principalName, Permissions p) {
    if (principalName != null) {
      synchronized (m_storeLock) {
        if (p == null) {
          p = new Permissions();
          p.setReadOnly();
        }
        m_store.put(principalName.toLowerCase(), p);
      }
      // notify clients
      SERVICES.getService(IClientNotificationService.class).putNotification(new AccessControlChangedNotification(p), new PrincipalNameFilter(principalName, 120000L));
    }
  }

  /**
   * clears the cache for a set of principal names
   * 
   * @param principalNames
   *          names of principals, case insensitive
   */
  public void clearCache() {
    String[] principalNames;
    synchronized (m_storeLock) {
      principalNames = m_store.keySet().toArray(new String[m_store.size()]);
    }
    clearCacheOfPrincipals(principalNames);
  }

  /**
   * clears the cache for a set of principal names
   * 
   * @param principalNames
   *          names of principals, case insensitive
   */
  public void clearCacheOfPrincipals(String... principalNames) {
    synchronized (m_storeLock) {
      for (String principalName : principalNames) {
        if (principalName != null) {
          m_store.remove(principalName.toLowerCase());
        }
      }
    }
    //notify clients
    for (String principalName : principalNames) {
      if (principalName != null) {
        SERVICES.getService(IClientNotificationService.class).putNotification(new AccessControlChangedNotification(null), new PrincipalNameFilter(principalName, 120000L));
        SERVICES.getService(IClientNotificationService.class).putNotification(new ResetAccessControlChangedNotification(), new PrincipalNameFilter(principalName, 120000L));
      }
    }
  }

  public String[] getPrincipalNames() {
    synchronized (m_storeLock) {
      return m_store.keySet().toArray(new String[m_store.keySet().size()]);
    }
  }
}
