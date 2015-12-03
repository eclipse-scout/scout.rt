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
package org.eclipse.scout.rt.server.services.common.clustersync.internal;

import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationProperties;

public class ClusterNotificationProperties implements IClusterNotificationProperties {
  private static final long serialVersionUID = 245680805887844037L;
  private final String m_originNode;
  private final String m_originUser;

  public ClusterNotificationProperties(String originNode, String originUser) {
    m_originNode = originNode;
    m_originUser = originUser;
  }

  @Override
  public String getOriginNode() {
    return m_originNode;
  }

  @Override
  public String getOriginUser() {
    return m_originUser;
  }

  @Override
  public String toString() {
    return "ClusterNotificationMessageProperties [m_originNode=" + m_originNode + ", m_originUser=" + m_originUser + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_originNode == null) ? 0 : m_originNode.hashCode());
    result = prime * result + ((m_originUser == null) ? 0 : m_originUser.hashCode());
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
    ClusterNotificationProperties other = (ClusterNotificationProperties) obj;
    if (m_originNode == null) {
      if (other.m_originNode != null) {
        return false;
      }
    }
    else if (!m_originNode.equals(other.m_originNode)) {
      return false;
    }
    if (m_originUser == null) {
      if (other.m_originUser != null) {
        return false;
      }
    }
    else if (!m_originUser.equals(other.m_originUser)) {
      return false;
    }
    return true;
  }
}
