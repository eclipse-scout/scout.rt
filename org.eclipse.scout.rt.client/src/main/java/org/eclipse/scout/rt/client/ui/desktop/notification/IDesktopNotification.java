/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.notification;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.platform.status.IStatus;

/**
 * A notification is used to display a short information on the Desktop. The notification disappears automatically after
 * the given duration has passed.
 *
 * @since 5.2
 */
public interface IDesktopNotification extends IPropertyObserver {

  /**
   * Default duration a notification is displayed is 5 seconds.
   */
  long DEFAULT_DURATION = TimeUnit.SECONDS.toMillis(5);

  /**
   * Duration is infinite which means notification is not automatically removed.
   */
  long INFINITE_DURATION = -1;

  IStatus getStatus();

  /**
   * Duration in milliseconds while the notification is displayed.
   */
  long getDuration();

  /**
   * Whether or not the notification can be closed by the user.
   */
  boolean isClosable();

  IDesktopNotificationUIFacade getUIFacade();

}
