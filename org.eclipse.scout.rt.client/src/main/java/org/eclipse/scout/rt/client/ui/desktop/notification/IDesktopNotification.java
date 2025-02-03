/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.notification;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.ui.notification.INotification;
import org.eclipse.scout.rt.platform.status.IStatus;

/**
 * A notification is used to display short information on the Desktop. If the given duration is >= 0, the notification
 * disappears automatically after the duration has passed.
 *
 * @since 5.2
 */
public interface IDesktopNotification extends INotification {

  /**
   * Default duration a notification is displayed is 5 seconds.
   */
  long DEFAULT_DURATION = TimeUnit.SECONDS.toMillis(5);

  /**
   * Duration is infinite which means notification is not automatically removed.
   */
  long INFINITE_DURATION = -1;

  /**
   * No native notification is shown.
   */
  String NATIVE_NOTIFICATION_VISIBILITY_NONE = "none";

  /**
   * The native notification is only shown if the application is in background.
   */
  String NATIVE_NOTIFICATION_VISIBILITY_BACKGROUND = "background";

  /**
   * The native notification is always shown.
   */
  String NATIVE_NOTIFICATION_VISIBILITY_ALWAYS = "always";

  String PROP_NATIVE_ONLY = "nativeOnly";

  String PROP_NATIVE_NOTIFICATION_VISIBILITY = "nativeNotificationVisibility";

  String PROP_NATIVE_NOTIFICATION_TITLE = "nativeNotificationTitle";

  String PROP_NATIVE_NOTIFICATION_STATUS = "nativeNotificationStatus";

  String PROP_NATIVE_NOTIFICATION_SHOWN = "nativeNotificationShown";

  DesktopNotification withDuration(long duration);

  /**
   * Duration in milliseconds while the notification is displayed.
   * <p>
   * A value <= 0 indicates an infinite duration, i.e. the notification is never closed automatically.
   */
  long getDuration();

  /**
   * If true, only the native notification will be shown. The desktop notification will be kept open invisibly until the
   * native notification is closed. If you use an infinite duration, consider to close the desktop notification
   * automatically when you don't need it anymore to help the user keep its notification center clean, and to clean up
   * the references on the desktop. This may be necessary in case the user does not close it manually or the
   * notification won't be shown at all due to missing permissions.
   */
  DesktopNotification withNativeOnly(boolean nativeOnly);

  /**
   * @return True, to only show the native desktop notification. False, to show both, the regular and the native
   * notification. If and when a native desktop notification is shown is controlled by
   * {@link #getNativeNotificationVisibility()}.
   */
  boolean isNativeOnly();

  DesktopNotification withNativeNotificationVisibility(String nativeNotificationVisibility);

  /**
   * see {@link #NATIVE_NOTIFICATION_VISIBILITY_NONE}, {@link #NATIVE_NOTIFICATION_VISIBILITY_ALWAYS} and
   * {@link #NATIVE_NOTIFICATION_VISIBILITY_BACKGROUND}
   */
  String getNativeNotificationVisibility();

  DesktopNotification withNativeNotificationTitle(String nativeNotificationTitle);

  /**
   * The title displayed on the native notification.
   */
  String getNativeNotificationTitle();

  DesktopNotification withNativeNotificationStatus(IStatus nativeNotificationStatus);

  /**
   * The {@link IStatus} used for the native notification (holds message and icon).
   */
  IStatus getNativeNotificationStatus();

  /**
   * Indicates, whether the native notification is shown. Once it is closed, the property will be set to false.
   *
   * @return true, if it is shown, false if not.
   */
  boolean isNativeNotificationShown();

  @Override
  IDesktopNotificationUIFacade getUIFacade();
}
