/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
