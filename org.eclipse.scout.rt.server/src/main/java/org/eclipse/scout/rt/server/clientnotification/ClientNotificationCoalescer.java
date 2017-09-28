/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.notification.NotificationCoalescer;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationAddress;

@ApplicationScoped
public class ClientNotificationCoalescer {

  public List<ClientNotificationMessage> coalesce(List<ClientNotificationMessage> inNotifications) {
    Iterable<ClientNotificationMessage> notificationsNoDuplicates = new LinkedHashSet<>(inNotifications);
    // sort by distribute & address property
    Map<Boolean, Map<IClientNotificationAddress, List<ClientNotificationMessage>>> messagesPerDistributeAndAddress = new HashMap<>();
    messagesPerDistributeAndAddress.put(true, new HashMap<>());
    messagesPerDistributeAndAddress.put(false, new HashMap<>());
    for (ClientNotificationMessage message : notificationsNoDuplicates) {
      Map<IClientNotificationAddress, List<ClientNotificationMessage>> messagesPerAddress = messagesPerDistributeAndAddress.get(message.isDistributeOverCluster());
      List<ClientNotificationMessage> messages = messagesPerAddress.computeIfAbsent(message.getAddress(), k -> new ArrayList<>());
      messages.add(message);
    }
    List<ClientNotificationMessage> result = new ArrayList<>();
    for (Entry<Boolean, Map<IClientNotificationAddress, List<ClientNotificationMessage>>> distributeEntry : messagesPerDistributeAndAddress.entrySet()) {
      boolean distribute = distributeEntry.getKey();
      for (Entry<IClientNotificationAddress, List<ClientNotificationMessage>> e : distributeEntry.getValue().entrySet()) {
        result.addAll(coalesce(distribute, e.getKey(), e.getValue()));
      }
    }
    return result;
  }

  protected List<ClientNotificationMessage> coalesce(final boolean distributeOverCluster, final IClientNotificationAddress address, final List<ClientNotificationMessage> messages) {
    if (messages.isEmpty()) {
      return Collections.emptyList();
    }
    else if (messages.size() == 1) {
      final ClientNotificationMessage message = CollectionUtility.firstElement(messages);
      return CollectionUtility.arrayList(new ClientNotificationMessage(address, message.getNotification(), message.isDistributeOverCluster(), message.getCorrelationId()));
    }
    else {
      final Map<Serializable, String> messageMap = new LinkedHashMap<>(messages.size()); // respect insertion order
      for (final ClientNotificationMessage message : messages) {
        messageMap.put(message.getNotification(), message.getCorrelationId());
      }

      final List<? extends Serializable> coalescedNotifications = BEANS.get(NotificationCoalescer.class).coalesce(new ArrayList<>(messageMap.keySet()));

      final List<ClientNotificationMessage> coalescedMessages = new ArrayList<>(coalescedNotifications.size());
      for (final Serializable coalescedNotification : coalescedNotifications) {
        final String cid = messageMap.get(coalescedNotification) != null ? messageMap.get(coalescedNotification) : BEANS.get(CorrelationId.class).newCorrelationId();
        coalescedMessages.add(new ClientNotificationMessage(address, coalescedNotification, distributeOverCluster, cid));
      }
      return coalescedMessages;
    }
  }
}
