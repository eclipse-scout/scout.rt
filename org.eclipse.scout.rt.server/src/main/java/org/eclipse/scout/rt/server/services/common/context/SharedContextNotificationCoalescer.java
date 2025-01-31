/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.common.context;

import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.notification.ICoalescer;
import org.eclipse.scout.rt.shared.services.common.context.SharedContextChangedNotification;

public class SharedContextNotificationCoalescer implements ICoalescer<SharedContextChangedNotification> {

  /**
   * Reduce to last notification
   */
  @Override
  public List<SharedContextChangedNotification> coalesce(List<SharedContextChangedNotification> notifications) {
    return CollectionUtility.arrayList(CollectionUtility.lastElement(notifications));
  }
}
