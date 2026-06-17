/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
  public void registerNode(NodeId clientNodeId) {
    BEANS.get(ClientNotificationRegistry.class).registerNode(clientNodeId, true);
  }

  @Override
  public void unregisterNode(NodeId clientNodeId) {
    BEANS.get(ClientNotificationRegistry.class).unregisterNode(clientNodeId, true);
  }

  @Override
  public List<ClientNotificationMessage> getNotifications(NodeId clientNodeId) {
    return BEANS.get(ClientNotificationRegistry.class).consume(clientNodeId, m_maxNotifications, m_blockingTimeout, TimeUnit.MILLISECONDS);
  }
}
