/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.notification;

import java.util.function.Consumer;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;

@ClassId("759627bb-02e5-4db2-812c-aac00b80cdb6")
public class Notification extends AbstractWidget implements INotification {

  private final IStatus m_status;
  private final boolean m_closable;
  private final boolean m_htmlEnabled;
  private final Consumer<String> m_appLinkConsumer;
  private final FastListenerList<NotificationListener> m_listenerList = new FastListenerList<>();
  private final INotificationUIFacade m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());

  /**
   * Creates a simple info notification with a text; not closable.
   */
  public Notification(String text) {
    this(new Status(text, IStatus.INFO));
  }

  /**
   * Creates a notification with a status.
   */
  public Notification(IStatus status) {
    this(status, false, false, null);
  }

  /**
   * Creates a notification with the given attributes.
   *
   * @param status
   * @param closable
   *          see {@link #isClosable()}
   */
  public Notification(IStatus status, boolean closable) {
    this(status, closable, false, null);
  }

  /**
   * Creates a notification with the given attributes.
   *
   * @param status
   * @param closable
   *          see {@link #isClosable()}
   * @param htmlEnabled
   *          see {@link #isHtmlEnabled()}
   */
  public Notification(IStatus status, boolean closable, boolean htmlEnabled) {
    this(status, closable, htmlEnabled, null);
  }

  /**
   * Creates a notification with the given attributes.
   *
   * @param status
   * @param closable
   *          see {@link #isClosable()}
   * @param htmlEnabled
   *          see {@link #isHtmlEnabled()}
   * @param appLinkConsumer
   */
  public Notification(IStatus status, boolean closable, boolean htmlEnabled, Consumer<String> appLinkConsumer) {
    m_status = status;
    m_closable = closable;
    m_htmlEnabled = htmlEnabled;
    m_appLinkConsumer = appLinkConsumer;
  }

  @Override
  public IStatus getStatus() {
    return m_status;
  }

  @Override
  public boolean isClosable() {
    return m_closable;
  }

  @Override
  public boolean isHtmlEnabled() {
    return m_htmlEnabled;
  }

  protected Consumer<String> getAppLinkConsumer() {
    return m_appLinkConsumer;
  }

  protected IFastListenerList<NotificationListener> notificationListeners() {
    return m_listenerList;
  }

  @Override
  public void addNotificationListener(NotificationListener listener) {
    notificationListeners().add(listener);
  }

  public void removeNotificationListener(NotificationListener listener) {
    notificationListeners().remove(listener);
  }

  protected void fireClosed() {
    fireNotificationEvent(new NotificationEvent(this, NotificationEvent.TYPE_CLOSED));
  }

  protected void fireNotificationEvent(NotificationEvent event) {
    notificationListeners().list().forEach(listener -> listener.notificationChanged(event));
  }

  @Override
  public INotificationUIFacade getUIFacade() {
    return m_uiFacade;
  }

  protected class P_UIFacade implements INotificationUIFacade {

    @Override
    public void fireClosedFromUI() {
      fireClosed();
    }

    @Override
    public void fireAppLinkActionFromUI(String ref) {
      if (m_appLinkConsumer != null) {
        m_appLinkConsumer.accept(ref);
      }
    }
  }
}
