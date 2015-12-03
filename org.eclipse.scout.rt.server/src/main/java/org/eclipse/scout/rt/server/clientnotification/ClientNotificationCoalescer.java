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
package org.eclipse.scout.rt.server.clientnotification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.notification.NotificationCoalescer;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationAddress;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

@ApplicationScoped
public class ClientNotificationCoalescer {

  public List<ClientNotificationMessage> coalesce(List<ClientNotificationMessage> inNotifications) {
    LinkedHashSet<ClientNotificationMessage> notificationsNoDuplicates = new LinkedHashSet<ClientNotificationMessage>(inNotifications);
    List<ClientNotificationMessage> result = new ArrayList<>();
    // sort by address
    Map<ClientNotificationAddress, List<ClientNotificationMessage>> messagesPerAddress = new HashMap<>();
    for (ClientNotificationMessage message : notificationsNoDuplicates) {
      List<ClientNotificationMessage> messages = messagesPerAddress.get(message.getAddress());
      if (messages == null) {
        messages = new ArrayList<ClientNotificationMessage>();
        messagesPerAddress.put(message.getAddress(), messages);
      }
      messages.add(message);
    }
    for (Entry<ClientNotificationAddress, List<ClientNotificationMessage>> e : messagesPerAddress.entrySet()) {
      result.addAll(coalesce(e.getKey(), e.getValue()));
    }
    return result;
  }

  protected List<ClientNotificationMessage> coalesce(ClientNotificationAddress address, List<ClientNotificationMessage> messagesIn) {
    if (messagesIn.isEmpty()) {
      return new ArrayList<>();
    }
    else if (messagesIn.size() == 1) {
      // no coalesce needed
      ClientNotificationMessage singleMessage = CollectionUtility.firstElement(messagesIn);
      return CollectionUtility.arrayList(new ClientNotificationMessage(address, singleMessage.getNotification(), singleMessage.isDistributeOverCluster()));
    }
    else {
      Map<Serializable, Boolean> notificationsIn = new LinkedHashMap<Serializable, Boolean>();
      for (ClientNotificationMessage singleMessage : messagesIn) {
        notificationsIn.put(singleMessage.getNotification(), singleMessage.isDistributeOverCluster());
      }
      List<? extends Serializable> outNotifications = BEANS.get(NotificationCoalescer.class).coalesce(new ArrayList<Serializable>(notificationsIn.keySet()));
      List<ClientNotificationMessage> result = new ArrayList<ClientNotificationMessage>();
      for (Serializable n : outNotifications) {
        result.add(new ClientNotificationMessage(address, n, notificationsIn.get(n)));
      }
      return result;
    }
  }

}
