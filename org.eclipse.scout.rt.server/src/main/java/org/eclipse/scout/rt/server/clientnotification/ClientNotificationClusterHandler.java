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

import java.util.Collection;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.notification.INotificationHandler;

/**
 * Publishes {@link ClientNotificationClusterNotification}s received via cluster synchronization into local registry.
 *
 * @author jgu
 */
public class ClientNotificationClusterHandler implements INotificationHandler<ClientNotificationClusterNotification> {

  @Override
  public void handleNotification(ClientNotificationClusterNotification notification) {
    Collection<? extends ClientNotificationMessage> messages = notification.getClientNotificationMessages();
    BEANS.get(ClientNotificationRegistry.class).publishWithoutClusterNotification(messages);
  }
}
