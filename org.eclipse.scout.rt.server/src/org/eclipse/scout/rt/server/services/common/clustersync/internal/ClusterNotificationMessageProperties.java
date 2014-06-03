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
package org.eclipse.scout.rt.server.services.common.clustersync.internal;

import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationMessageProperties;

/**
 *
 */
public class ClusterNotificationMessageProperties implements IClusterNotificationMessageProperties {
  private String m_originNode;
  private String m_originUser;

  /**
   *
   */
  public ClusterNotificationMessageProperties(String originNode, String originUser) {
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

}
