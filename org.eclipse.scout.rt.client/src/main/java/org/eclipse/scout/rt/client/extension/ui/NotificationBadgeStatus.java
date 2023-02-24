/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.extension.ui;

import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;

public class NotificationBadgeStatus extends Status {
  private static final long serialVersionUID = 1L;

  public NotificationBadgeStatus(String notificationBadgeText) {
    super(notificationBadgeText, IStatus.INFO);
  }

  @Override
  public int compareTo(IStatus o) {
    if (o instanceof NotificationBadgeStatus) {
      return super.compareTo(o);
    }
    return -1;
  }
}
