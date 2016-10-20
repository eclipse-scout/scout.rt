package org.eclipse.scout.rt.client.ui.desktop.notification;

import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;

public class DesktopNotification implements IDesktopNotification {

  private final IStatus m_status;
  private final long m_duration;
  private final boolean m_closable;

  /**
   * Creates a closable, simple info notification with a text and the default duration.
   */
  public DesktopNotification(String text) {
    this(new Status(text, IStatus.INFO));
  }

  /**
   * Creates a closable, notification with a status and the default duration.
   */
  public DesktopNotification(IStatus status) {
    m_status = status;
    m_duration = DEFAULT_DURATION;
    m_closable = true;
  }

  public DesktopNotification(IStatus status, long duration, boolean closable) {
    m_status = status;
    m_duration = duration;
    m_closable = closable;
  }

  @Override
  public IStatus getStatus() {
    return m_status;
  }

  @Override
  public long getDuration() {
    return m_duration;
  }

  @Override
  public boolean isClosable() {
    return m_closable;
  }

}
