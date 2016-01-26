package org.eclipse.scout.rt.client.ui.desktop.notification;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.status.IStatus;

/**
 * A notification is used to display a short information on the Desktop. The notification disappears automatically after
 * the given duration has passed.
 *
 * @since 5.2
 */
public interface IDesktopNotification {

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
   * Duration while the notification is displayed.
   */
  long getDuration();

  /**
   * Whether or not the notification can be closed by the user.
   */
  boolean isCloseable();

}
