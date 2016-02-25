/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.notification;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for {@link INotificationHandler}s.
 */
@ApplicationScoped
public class NotificationHandlerRegistry {
  private static final Logger LOG = LoggerFactory.getLogger(NotificationHandlerRegistry.class);
  private final TypeParameterBeanRegistry<INotificationHandler> m_registry = new TypeParameterBeanRegistry<>(INotificationHandler.class);

  @PostConstruct
  protected void buildHandlerLinking() {
    m_registry.registerBeans(BEANS.all(INotificationHandler.class));
  }

  /**
   * Notify all {@link INotificationHandler}s with the message, if the message type matches the handler type.
   *
   * @param notification
   *          notification message
   */
  @SuppressWarnings("unchecked")
  public void notifyHandlers(Serializable notification) {
    final List<INotificationHandler> handlers = m_registry.getBeans(notification.getClass());
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
