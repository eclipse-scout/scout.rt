/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.common.clustersync;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.notification.NotificationCoalescer;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationMessage;

/**
 * Delegates to {@link NotificationCoalescer} for {@link IClusterNotificationMessage} with the same properties.
 */
@ApplicationScoped
public class ClusterNotificationMessageCoalescer {

  public List<IClusterNotificationMessage> coalesce(List<IClusterNotificationMessage> inNotifications) {
    List<IClusterNotificationMessage> result = new ArrayList<>();
    // sort by properties
    Map<IClusterNotificationProperties, List<Serializable>> notificationsPerProps = new HashMap<>();
    for (IClusterNotificationMessage message : inNotifications) {
      List<Serializable> messages = notificationsPerProps.computeIfAbsent(message.getProperties(), k -> new ArrayList<>());
      messages.add(message.getNotification());
    }

    for (Entry<IClusterNotificationProperties, List<Serializable>> e : notificationsPerProps.entrySet()) {
      result.addAll(coalesce(e.getKey(), e.getValue()));
    }
    return result;
  }

  protected List<IClusterNotificationMessage> coalesce(IClusterNotificationProperties props, List<Serializable> notificationsIn) {
    if (notificationsIn.isEmpty()) {
      return new ArrayList<>();
    }
    else if (notificationsIn.size() == 1) {
      // no coalesce needed
      IClusterNotificationMessage message = new ClusterNotificationMessage(CollectionUtility.firstElement(notificationsIn), props);
      return CollectionUtility.arrayList(message);
    }
    else {
      List<? extends Serializable> outNotifications = BEANS.get(NotificationCoalescer.class).coalesce(notificationsIn);
      List<IClusterNotificationMessage> result = new ArrayList<>();
      for (Serializable n : outNotifications) {
        result.add(new ClusterNotificationMessage(n, props));
      }
      return result;
    }
  }
}
