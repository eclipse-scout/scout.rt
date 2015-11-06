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
  private final TypeParameterBeanRegistry<INotificationHandler> m_registry = new TypeParameterBeanRegistry<>();

  @PostConstruct
  protected void buildHandlerLinking() {
    m_registry.registerBeans(INotificationHandler.class, BEANS.all(INotificationHandler.class));
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
        LOG.error(String.format("Handler '%s' notification with notification '%s' failed.", handler, notification), e);
      }
    }
  }

}
