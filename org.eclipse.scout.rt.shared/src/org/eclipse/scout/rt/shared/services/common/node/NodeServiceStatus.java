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
package org.eclipse.scout.rt.shared.services.common.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class NodeServiceStatus implements Serializable {

  private static final long serialVersionUID = 1L;

  private String m_originNodeId;
  private Date m_statusDate;
  private String m_serviceName;
  private String m_description;
  private String m_reloadClusterCacheMenuText;
  private Date m_lastChangeddDate;
  private String m_lastChangedUserId;
  private String m_lastChangedClusterNodeId;
  private List<String> m_infoLines = new ArrayList<String>();

  public String getOriginNodeId() {
    return m_originNodeId;
  }

  public void setOriginNodeId(String originNodeName) {
    m_originNodeId = originNodeName;
  }

  public Date getStatusDate() {
    return m_statusDate;
  }

  public void setStatusDate(Date statusDate) {
    m_statusDate = statusDate;
  }

  public String getServiceName() {
    return m_serviceName;
  }

  public void setServiceName(String serviceName) {
    m_serviceName = serviceName;
  }

  public String getDescription() {
    return m_description;
  }

  public void setDescription(String description) {
    m_description = description;
  }

  public String getReloadClusterCacheMenuText() {
    return m_reloadClusterCacheMenuText;
  }

  public void setReloadClusterCacheMenuText(String reloadClusterCacheMenuText) {
    m_reloadClusterCacheMenuText = reloadClusterCacheMenuText;
  }

  public String getLastChangedClusterNodeId() {
    return m_lastChangedClusterNodeId;
  }

  public void setLastChangedClusterNodeId(String lastChangedClusterNodeId) {
    m_lastChangedClusterNodeId = lastChangedClusterNodeId;
  }

  public String getLastChangedUserId() {
    return m_lastChangedUserId;
  }

  public void setLastChangedUserId(String lastChangedUserId) {
    if (lastChangedUserId != null && lastChangedUserId.equals("ors")) {
      m_lastChangedUserId = "System";
      return;
    }
    m_lastChangedUserId = lastChangedUserId;
  }

  public Date getLastChangedDate() {
    return m_lastChangeddDate;
  }

  public void setLastChangedDate(Date lastChangedDate) {
    m_lastChangeddDate = lastChangedDate;
  }

  public void addInfoLine(String line) {
    m_infoLines.add(line);
  }

  public void addInfoLine(String label, String value) {
    addInfoLine(label + ": <b>" + value + "</b>");
  }

  public void addInfoLine(String label, int value) {
    addInfoLine(label, String.valueOf(value));
  }

  public List<String> getInfoLines() {
    return m_infoLines;
  }
}
