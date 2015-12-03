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
      List<Serializable> messages = notificationsPerProps.get(message.getProperties());
      if (messages == null) {
        messages = new ArrayList<>();
        notificationsPerProps.put(message.getProperties(), messages);
      }
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
