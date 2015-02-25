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
package org.eclipse.scout.rt.server.services.common.clientnotification;

import java.security.AccessController;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.security.auth.Subject;

/**
 * <p>
 * Filter used to send notifications to users owning any of the principal names (case insensitive) in their subject
 * </p>
 * <p>
 * This is a value object.
 * </p>
 */
public class PrincipalNameFilter implements IClientNotificationFilter {
  private static final long serialVersionUID = 1L;
  private final String m_principalName;
  private final long m_validUntil;

  /**
   * @param principalName
   *          case insensitive
   * @param timeout
   */
  public PrincipalNameFilter(String principalName, long timeout) {
    if (principalName != null) {
      m_principalName = principalName.toLowerCase();
    }
    else {
      m_principalName = null;
    }

    m_validUntil = System.currentTimeMillis() + timeout;
  }

  @Override
  public boolean isActive() {
    return System.currentTimeMillis() <= m_validUntil;
  }

  @Override
  public boolean isMulticast() {
    return true;
  }

  public String getPrincipalName() {
    return m_principalName;
  }

  @Override
  public boolean accept() {
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject != null) {
      for (Principal principal : subject.getPrincipals()) {
        if (principal != null && m_principalName.equalsIgnoreCase(principal.getName())) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return m_principalName != null ? m_principalName.hashCode() : 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj.getClass() == this.getClass()) {
      PrincipalNameFilter o = (PrincipalNameFilter) obj;
      return o.m_principalName == this.m_principalName || (o.m_principalName != null && o.m_principalName.equals(this.m_principalName));
    }
    return false;
  }

  @Override
  public String toString() {
    StringBuffer b = new StringBuffer(getClass().getSimpleName());
    b.append("[");
    b.append(m_principalName);
    b.append(", validUntil=" + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(m_validUntil)));
    b.append("]");
    return b.toString();
  }
}
