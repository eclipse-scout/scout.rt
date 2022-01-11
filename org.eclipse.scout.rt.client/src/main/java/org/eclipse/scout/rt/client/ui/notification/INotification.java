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
package org.eclipse.scout.rt.client.ui.notification;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.platform.status.IStatus;

/**
 * A notification is used to show short information on a widget.
 *
 * @since 8.0
 */
public interface INotification extends IWidget {

  Notification withStatus(IStatus status);

  IStatus getStatus();

  Notification withClosable(boolean closable);

  /**
   * Whether the notification can be closed by the user.
   */
  boolean isClosable();

  Notification withHtmlEnabled(boolean htmlEnabled);

  /**
   * @return true, if the notification may contain html that needs to be rendered. false otherwise.
   */
  boolean isHtmlEnabled();

  void addNotificationListener(NotificationListener listener);

  void removeNotificationListener(NotificationListener listener);

  INotificationUIFacade getUIFacade();
}
