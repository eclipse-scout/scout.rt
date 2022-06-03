/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.extension.ui;

import org.eclipse.scout.rt.client.ui.CssClasses;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;

public class NotificationBadgeStatus extends Status {
  private static final long serialVersionUID = 1L;

  public static final int NOTIFICATION_BADGE_STATUS_CODE = 197821;

  public NotificationBadgeStatus(String notificationBadgeText) {
    super(notificationBadgeText, IStatus.INFO, NOTIFICATION_BADGE_STATUS_CODE);
    withCssClass(CssClasses.NOTIFICATION_BADGE_STATUS);
  }
}
