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
package org.eclipse.scout.rt.server.services.common.node;

import java.util.List;

import org.eclipse.scout.rt.server.services.common.notification.INotification;
import org.eclipse.scout.rt.shared.services.common.node.NodeServiceStatus;

/**
 *
 */
public class RequestServerStatusNotification implements INotification {

  private static final long serialVersionUID = 4969413547888550116L;
  private List<NodeServiceStatus> m_statusList;

  RequestServerStatusNotification(List<NodeServiceStatus> statusList) {
    m_statusList = statusList;
  }

  public List<NodeServiceStatus> getStatusList() {
    return m_statusList;
  }

  public void setStatusList(List<NodeServiceStatus> statusList) {
    m_statusList = statusList;
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append(getClass().toString());
    if (getStatusList() != null) {
      s.append(" {").append(getStatusList().toString()).append("}");
    }
    return s.toString();
  }

}
