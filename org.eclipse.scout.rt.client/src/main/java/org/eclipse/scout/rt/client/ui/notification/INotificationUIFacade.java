/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.notification;

public interface INotificationUIFacade {

  /**
   * Notification that the Notification was closed in the UI
   */
  void fireClosedFromUI();

  /**
   * Notification that the AppLink of the Notification was clicked
   */
  void fireAppLinkActionFromUI(String ref);
}
