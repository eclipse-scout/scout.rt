/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.clientnotification;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.notification.INotificationHandler;

public class ClientNotificationRegistryClusterHandler implements INotificationHandler<ClientNotificationRegistryClusterNotification> {

  @Override
  public void handleNotification(ClientNotificationRegistryClusterNotification notification) {
    switch (notification.getEvent()) {
      case NODE_REGISTERED -> BEANS.get(ClientNotificationRegistry.class).registerNode(notification.getClientNodeId(), false);
      case NODE_UNREGISTERED -> BEANS.get(ClientNotificationRegistry.class).unregisterNode(notification.getClientNodeId(), false);
    }
  }
}
