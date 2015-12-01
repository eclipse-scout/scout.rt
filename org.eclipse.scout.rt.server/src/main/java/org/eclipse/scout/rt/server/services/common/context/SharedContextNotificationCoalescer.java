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
