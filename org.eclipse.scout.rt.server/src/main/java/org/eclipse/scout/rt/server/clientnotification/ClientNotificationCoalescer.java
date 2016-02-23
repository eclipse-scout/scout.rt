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
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationAddress;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

@ApplicationScoped
public class ClientNotificationCoalescer {

  public List<ClientNotificationMessage> coalesce(List<ClientNotificationMessage> inNotifications) {
    LinkedHashSet<ClientNotificationMessage> notificationsNoDuplicates = new LinkedHashSet<ClientNotificationMessage>(inNotifications);
    // sort by distribute & address property
    Map<Boolean, Map<ClientNotificationAddress, List<ClientNotificationMessage>>> messagesPerDistributeAndAddress = new HashMap<>();
    messagesPerDistributeAndAddress.put(true, new HashMap<ClientNotificationAddress, List<ClientNotificationMessage>>());
    messagesPerDistributeAndAddress.put(false, new HashMap<ClientNotificationAddress, List<ClientNotificationMessage>>());
    for (ClientNotificationMessage message : notificationsNoDuplicates) {
      Map<ClientNotificationAddress, List<ClientNotificationMessage>> messagesPerAddress = messagesPerDistributeAndAddress.get(message.isDistributeOverCluster());
      List<ClientNotificationMessage> messages = messagesPerAddress.get(message.getAddress());
      if (messages == null) {
        messages = new ArrayList<ClientNotificationMessage>();
        messagesPerAddress.put(message.getAddress(), messages);
      }
      messages.add(message);
    }
    List<ClientNotificationMessage> result = new ArrayList<>();
    for (Entry<Boolean, Map<ClientNotificationAddress, List<ClientNotificationMessage>>> distributeEntry : messagesPerDistributeAndAddress.entrySet()) {
      boolean distribute = distributeEntry.getKey();
      for (Entry<ClientNotificationAddress, List<ClientNotificationMessage>> e : distributeEntry.getValue().entrySet()) {
        result.addAll(coalesce(distribute, e.getKey(), e.getValue()));
      }
    }
    return result;
  }

  protected List<ClientNotificationMessage> coalesce(final boolean distributeOverCluster, final ClientNotificationAddress address, final List<ClientNotificationMessage> messages) {
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
