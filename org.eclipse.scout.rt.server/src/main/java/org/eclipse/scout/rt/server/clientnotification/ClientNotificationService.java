/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.clientnotification;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationProperties.MaxNotificationBlockingTimeOut;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationProperties.MaxNotificationMessages;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;

public class ClientNotificationService implements IClientNotificationService {
  private final int m_blockingTimeout;
  private final int m_maxNotifications;

  public ClientNotificationService() {
    m_blockingTimeout = Assertions.assertNotNull(CONFIG.getPropertyValue(MaxNotificationBlockingTimeOut.class));
    m_maxNotifications = Assertions.assertNotNull(CONFIG.getPropertyValue(MaxNotificationMessages.class));
  }

  @Override
  public void registerNode(NodeId nodeId) {
    BEANS.get(ClientNotificationRegistry.class).registerNode(nodeId);
  }

  @Override
  public void unregisterNode(NodeId nodeId) {
    BEANS.get(ClientNotificationRegistry.class).unregisterNode(nodeId);
  }

  @Override
  public List<ClientNotificationMessage> getNotifications(NodeId nodeId) {
    return BEANS.get(ClientNotificationRegistry.class).consume(nodeId, m_maxNotifications, m_blockingTimeout, TimeUnit.MILLISECONDS);
  }
}
