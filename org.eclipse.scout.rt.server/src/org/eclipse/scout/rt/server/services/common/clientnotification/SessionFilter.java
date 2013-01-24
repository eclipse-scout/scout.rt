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

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ThreadContext;

/**
 * Filter to send a notification to a specific session
 */
public class SessionFilter implements IClientNotificationFilter {
  private static final long serialVersionUID = 1L;
  private transient WeakReference<IServerSession> m_sessionRef;
  private long m_validUntil;

  public SessionFilter(IServerSession session, long timeout) {
    m_sessionRef = new WeakReference<IServerSession>(session);
    m_validUntil = System.currentTimeMillis() + timeout;
  }

  @Override
  public boolean isActive() {
    return m_sessionRef != null && m_sessionRef.get() != null && System.currentTimeMillis() <= m_validUntil;
  }

  @Override
  public boolean isMulticast() {
    return false;
  }

  @Override
  public boolean accept() {
    return m_sessionRef != null && ThreadContext.getServerSession() == m_sessionRef.get();
  }

  @Override
  public int hashCode() {
    IServerSession session = m_sessionRef.get();
    return session != null ? session.hashCode() : 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj.getClass() == this.getClass()) {
      SessionFilter o = (SessionFilter) obj;
      return o.m_sessionRef.get() == this.m_sessionRef.get();
    }
    return false;
  }

  @Override
  public String toString() {
    StringBuffer b = new StringBuffer(getClass().getSimpleName());
    b.append("[");
    if (m_sessionRef != null) {
      b.append(m_sessionRef.get());
    }
    b.append(", validUntil=" + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(m_validUntil)));
    b.append("]");
    return b.toString();
  }

}
