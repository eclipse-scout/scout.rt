/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.clientnotification;

import java.util.Collection;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.notification.INotificationHandler;

/**
 * Publishes {@link ClientNotificationClusterNotification}s received via cluster synchronization into local registry.
 * <h3>{@link ClientNotificationClusterHandler}</h3>
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
