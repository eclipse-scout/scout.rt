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

import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotification;

/**
 * Cluster notification for new client notification
 */
public class ClientNotificationClusterNotification implements IClusterNotification {

  private static final long serialVersionUID = -8513131031858145786L;
  private final IClientNotificationQueueElement m_queueElement;

  public ClientNotificationClusterNotification(IClientNotificationQueueElement queueElement) {
    m_queueElement = queueElement;
  }

  public IClientNotificationQueueElement getQueueElement() {
    return m_queueElement;
  }

  @Override
  public String toString() {
    return "ClientNotificationClusterNotification [m_queueElement=" + m_queueElement + "]";
  }
}
