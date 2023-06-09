/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.uinotification;

import java.util.EventObject;

import org.eclipse.scout.rt.api.data.uinotification.UiNotificationDo;

public class UiNotificationAddedEvent extends EventObject {
  private static final long serialVersionUID = 1L;
  private UiNotificationDo m_notification;

  public UiNotificationAddedEvent(Object source, UiNotificationDo notification) {
    super(source);
    m_notification = notification;
  }

  public UiNotificationDo getNotification() {
    return m_notification;
  }
}
