package org.eclipse.scout.rt.client.ui.desktop.notification;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;

public class DesktopNotification extends AbstractPropertyObserver implements IDesktopNotification {

  private final IStatus m_status;
  private final long m_duration;
  private final boolean m_closable;
  private IDesktopNotificationUIFacade m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());

  /**
   * Creates a closable, simple info notification with a text and the default duration.
   */
  public DesktopNotification(String text) {
    this(new Status(text, IStatus.INFO));
  }

  /**
   * Creates a closable notification with a status and the default duration.
   */
  public DesktopNotification(IStatus status) {
    m_status = status;
    m_duration = DEFAULT_DURATION;
    m_closable = true;
  }

  /**
   * Creates a notification.
   *
   * @param status
   * @param duration
   *          in milliseconds
   * @param closable
   */
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

  @Override
  public IDesktopNotificationUIFacade getUIFacade() {
    return m_uiFacade;
  }

  protected class P_UIFacade implements IDesktopNotificationUIFacade {

    @Override
    public void fireClosedFromUI() {
      IDesktop.CURRENT.get().getUIFacade().removedNotificationFromUI(DesktopNotification.this);
    }
  }
}
