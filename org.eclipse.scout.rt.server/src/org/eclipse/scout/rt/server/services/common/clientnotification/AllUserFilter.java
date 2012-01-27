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

/**
 * Filter to broadcast a notification to all users
 */
public class AllUserFilter implements IClientNotificationFilter {
  private static final long serialVersionUID = 1L;
  private long m_validUntil;

  public AllUserFilter(long timeout) {
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

  @Override
  public boolean accept() {
    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    return obj.getClass() == this.getClass();
  }

  @Override
  public String toString() {
    StringBuffer b = new StringBuffer(getClass().getSimpleName());
    b.append("[");
    b.append("validUntil=" + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(m_validUntil)));
    b.append("]");
    return b.toString();
  }

}
