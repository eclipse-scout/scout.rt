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
package org.eclipse.scout.rt.server.services.common.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.cache.InvalidateCacheNotification;

/**
 * Cluster notification for access control changes for a set of users.
 *
 * @deprecated replaced with {@link InvalidateCacheNotification}. Will be removed in Scout 6.1. See {@link ICache}
 */
@Deprecated
public class AccessControlClusterNotification implements Serializable {

  private static final long serialVersionUID = 128460814967537176L;

  private final Set<String> m_userIds;

  public AccessControlClusterNotification() {
    this(null);
  }

  public AccessControlClusterNotification(Collection<String> userIds) {
    m_userIds = CollectionUtility.hashSetWithoutNullElements(userIds);
  }

  public Set<String> getUserIds() {
    return m_userIds;
  }

  @Override
  public String toString() {
    return "AccessControlClusterNotification [m_userIds=" + m_userIds + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_userIds == null) ? 0 : m_userIds.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    AccessControlClusterNotification other = (AccessControlClusterNotification) obj;
    if (m_userIds == null) {
      if (other.m_userIds != null) {
        return false;
      }
    }
    else if (!m_userIds.equals(other.m_userIds)) {
      return false;
    }
    return true;
  }

}
