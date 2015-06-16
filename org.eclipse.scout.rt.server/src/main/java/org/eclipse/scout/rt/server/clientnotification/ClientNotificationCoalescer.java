/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.clientnotification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.notification.NotificationCoalescer;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotficationAddress;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

/**
 *
 */
@ApplicationScoped
public class ClientNotificationCoalescer {

  public List<ClientNotificationMessage> coalesce(List<ClientNotificationMessage> inNotifications) {
    List<ClientNotificationMessage> result = new ArrayList<>();
    // sort by address
    Map<ClientNotficationAddress, List<Serializable>> notificationsPerAddress = new HashMap<>();
    for (ClientNotificationMessage message : inNotifications) {
      List<Serializable> notifications = notificationsPerAddress.get(message.getAddress());
      if (notifications == null) {
        notifications = new ArrayList<>();
        notificationsPerAddress.put(message.getAddress(), notifications);
      }
      notifications.add(message.getNotification());
    }
    for (Entry<ClientNotficationAddress, List<Serializable>> e : notificationsPerAddress.entrySet()) {
      result.addAll(coalesce(e.getKey(), e.getValue()));
    }
    return result;
  }

  protected List<ClientNotificationMessage> coalesce(ClientNotficationAddress address, List<Serializable> notificationsIn) {
    if (notificationsIn.isEmpty()) {
      return new ArrayList<>();
    }
    else if (notificationsIn.size() == 1) {
      // no coalesce needed
      return CollectionUtility.arrayList(new ClientNotificationMessage(address, CollectionUtility.firstElement(notificationsIn)));
    }
    else {
      List<? extends Serializable> outNotifications = BEANS.get(NotificationCoalescer.class).coalesce(notificationsIn);
      List<ClientNotificationMessage> result = new ArrayList<>();
      for (Serializable n : outNotifications) {
        result.add(new ClientNotificationMessage(address, n));
      }
      return result;
    }
  }
}
