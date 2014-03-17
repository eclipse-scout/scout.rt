/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.notification;

import org.eclipse.scout.rt.server.services.common.notification.DistributedNotification.DistributedNotificationType;

/**
 *
 */
public class CreateNotificationFactory {
  public static IDistributedNotification createNewNotification(INotification n) {
    IDistributedNotification notification = new DistributedNotification(n, DistributedNotificationType.NEW);
    return notification;
  }

  public static IDistributedNotification createUpdateNotification(INotification n) {
    IDistributedNotification notification = new DistributedNotification(n, DistributedNotificationType.UPDATE);
    return notification;
  }

  public static IDistributedNotification createRemoveNotification(INotification n) {
    IDistributedNotification notification = new DistributedNotification(n, DistributedNotificationType.REMOVE);
    return notification;
  }
}
