/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.notification;

import java.io.Serializable;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.TypeParameterBeanRegistry;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationAddress;
import org.eclipse.scout.rt.shared.clientnotification.IDispatchingNotificationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for {@link INotificationHandler}s.
 */
@ApplicationScoped
public class NotificationHandlerRegistry {
  private static final Logger LOG = LoggerFactory.getLogger(NotificationHandlerRegistry.class);
  private final TypeParameterBeanRegistry<INotificationHandler> m_notificationHandlerRegistry = new TypeParameterBeanRegistry<>(INotificationHandler.class);
  private final TypeParameterBeanRegistry<IDispatchingNotificationHandler> m_dispatchingNotificationHandlerRegistry = new TypeParameterBeanRegistry<>(IDispatchingNotificationHandler.class);

  @PostConstruct
  protected void buildHandlerLinking() {
    m_notificationHandlerRegistry.registerBeans(BEANS.all(INotificationHandler.class));
    m_dispatchingNotificationHandlerRegistry.registerBeans(BEANS.all(IDispatchingNotificationHandler.class));
  }

  public void notifyHandlers(Serializable notification, IClientNotificationAddress address) {
    notifyNotificationHandlers(notification);
    notifyDispatchingNotificationHandlers(notification, address);
  }

  @SuppressWarnings("unchecked")
  public void notifyDispatchingNotificationHandlers(Serializable notification, IClientNotificationAddress address) {
    final List<IDispatchingNotificationHandler> dispatchingHandlers = m_dispatchingNotificationHandlerRegistry.getBeans(notification.getClass());
    for (IDispatchingNotificationHandler handler : dispatchingHandlers) {
      try {
        handler.handleNotification(notification, address);
      }
      catch (RuntimeException e) {
        LOG.error("Handler '{}' notification with notification '{}' failed.", handler, notification, e);
      }
    }
  }

  /**
   * Notify all {@link INotificationHandler}s with the message, if the message type matches the handler type.
   *
   * @param notification
   *          notification message
   */
  @SuppressWarnings("unchecked")
  public void notifyNotificationHandlers(Serializable notification) {
    final List<INotificationHandler> handlers = m_notificationHandlerRegistry.getBeans(notification.getClass());
    for (INotificationHandler handler : handlers) {
      try {
        handler.handleNotification(notification);
      }
      catch (RuntimeException e) {
        LOG.error("Handler '{}' notification with notification '{}' failed.", handler, notification, e);
      }
    }
  }

}
