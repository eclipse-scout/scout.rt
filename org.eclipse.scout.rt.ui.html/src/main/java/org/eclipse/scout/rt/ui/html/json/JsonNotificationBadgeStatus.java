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
package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.rt.client.extension.ui.NotificationBadgeStatus;

public class JsonNotificationBadgeStatus extends JsonStatus {

  public JsonNotificationBadgeStatus(NotificationBadgeStatus status) {
    super(status);
  }

  @Override
  public String getObjectType() {
    return "NotificationBadgeStatus";
  }
}