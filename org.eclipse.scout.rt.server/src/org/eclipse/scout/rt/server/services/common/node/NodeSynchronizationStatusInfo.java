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

import java.util.Date;

public class NodeSynchronizationStatusInfo {

  private Date m_lastChangedDate;
  private String m_lastChangedUserId;
  private String m_lastChangedClusterNodeId;

  public Date getLastChangedDate() {
    return m_lastChangedDate;
  }

  public void setLastChangedDate(Date lastChangedDate) {
    m_lastChangedDate = lastChangedDate;
  }

  public String getLastChangedUserId() {
    return m_lastChangedUserId;
  }

  public void setLastChangedUserId(String lastChangedUserId) {
    m_lastChangedUserId = lastChangedUserId;
  }

  public String getLastChangedClusterNodeId() {
    return m_lastChangedClusterNodeId;
  }

  public void setLastChangedClusterNodeId(String lastChangedClusterNodeId) {
    m_lastChangedClusterNodeId = lastChangedClusterNodeId;
  }

}
