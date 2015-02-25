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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.rt.server.ThreadContext;

/**
 * Filter to send a notification to a specific user
 */
public class SingleUserFilter implements IClientNotificationFilter {
  private static final long serialVersionUID = 1L;
  private final String m_userId;
  private final long m_validUntil;

  public SingleUserFilter(String userId, long timeout) {
    m_userId = userId;
    m_validUntil = System.currentTimeMillis() + timeout;
  }

  public String getUserId() {
    return m_userId;
  }

  @Override
  public boolean isActive() {
    return System.currentTimeMillis() <= m_validUntil;
  }

  @Override
  public boolean isMulticast() {
    return true;
  }

  @Override
  public boolean accept() {
    return m_userId != null && m_userId.equalsIgnoreCase(ThreadContext.getServerSession().getUserId());
  }

  @Override
  public int hashCode() {
    return m_userId != null ? m_userId.hashCode() : 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj.getClass() == this.getClass()) {
      SingleUserFilter o = (SingleUserFilter) obj;
      return o.m_userId == this.m_userId || (o.m_userId != null && o.m_userId.equals(this.m_userId));
    }
    return false;
  }

  @Override
  public String toString() {
    StringBuffer b = new StringBuffer(getClass().getSimpleName());
    b.append("[");
    b.append(m_userId);
    b.append(", validUntil=" + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(m_validUntil)));
    b.append("]");
    return b.toString();
  }
}
