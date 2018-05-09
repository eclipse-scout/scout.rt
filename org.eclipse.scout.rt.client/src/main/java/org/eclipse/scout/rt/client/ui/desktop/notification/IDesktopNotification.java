package org.eclipse.scout.rt.client.ui.desktop.notification;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.status.IStatus;

/**
 * A notification is used to display a short information on the Desktop. If the given duration is >= 0, the notification
 * disappears automatically after the duration has passed.
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
   * Duration in milliseconds while the notification is displayed.
   * <p>
   * A value <= 0 indicates an infinite duration, i.e. the notification is never closed automatically.
   */
  long getDuration();

  /**
   * Whether or not the notification can be closed by the user.
   */
  boolean isClosable();
}
