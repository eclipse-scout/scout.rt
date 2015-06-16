package org.eclipse.scout.rt.shared.notification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;

/**
 * Registry for {@link INotificationHandler}s.
 */
@ApplicationScoped
public class NotificationHandlerRegistry {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(NotificationHandlerRegistry.class);

  private final Map<Class<? extends Serializable> /*notification class*/, List<INotificationHandler<? extends Serializable>>> m_notificationClassToHandler = new HashMap<>();
  protected final ReadWriteLock m_classToHandlerLock = new ReentrantReadWriteLock();

  private final Map<Class<? extends Serializable> /*notification class*/, List<INotificationHandler<? extends Serializable>>> m_cachedHandlers = new HashMap<>();
  protected final ReadWriteLock m_cachedHandlerLock = new ReentrantReadWriteLock();

  /**
   * builds a linking of all handlers generic types to handlers. This is used to find the corresponding handler of a
   * notification.
   */
  @SuppressWarnings("unchecked")
  @PostConstruct
  protected void buildHandlerLinking() {
    List<INotificationHandler> notificationHandlers = BEANS.all(INotificationHandler.class);
    m_classToHandlerLock.writeLock().lock();
    try {

      for (INotificationHandler<?> notificationHandler : notificationHandlers) {
        Class notificationClass = TypeCastUtility.getGenericsParameterClass(notificationHandler.getClass(), INotificationHandler.class);
        List<INotificationHandler<?>> handlerList = m_notificationClassToHandler.get(notificationClass);
        if (handlerList == null) {
          handlerList = new LinkedList<>();
          m_notificationClassToHandler.put(notificationClass, handlerList);
        }
        handlerList.add(notificationHandler);
      }
    }
    finally {
      m_classToHandlerLock.writeLock().unlock();
    }
  }

  /**
   * Notify all {@link INotificationHandler}s with the message, if the message type matches the handler type.
   *
   * @param notification
   *          notification message
   */
  @SuppressWarnings("unchecked")
  public void notifyHandlers(Serializable notification) {
    List<INotificationHandler<? extends Serializable>> handlers = getHandlers(notification.getClass());
    for (INotificationHandler handler : handlers) {
      try {
        handler.handleNotification(notification);
      }
      catch (Exception e) {
        LOG.error(String.format("Handler '%s' notification with notification '%s' failed.", handler, notification), e);
      }
    }
  }

  protected List<INotificationHandler<? extends Serializable>> getHandlers(Class<? extends Serializable> notificationClass) {
    List<INotificationHandler<? extends Serializable>> handlers = getCachedHandlers(notificationClass);
    if (handlers != null) {
      return new ArrayList<INotificationHandler<? extends Serializable>>(handlers);
    }
    else {
      handlers = findHandlers(notificationClass);
      cacheHandlers(notificationClass, handlers);
      return new ArrayList<INotificationHandler<? extends Serializable>>(handlers);
    }
  }

  private List<INotificationHandler<? extends Serializable>> getCachedHandlers(Class<? extends Serializable> notificationClass) {
    m_cachedHandlerLock.readLock().lock();
    try {
      return m_cachedHandlers.get(notificationClass);
    }
    finally {
      m_cachedHandlerLock.readLock().unlock();
    }
  }

  private void cacheHandlers(Class<? extends Serializable> notificationClass, List<INotificationHandler<? extends Serializable>> notificationHandlers) {
    m_cachedHandlerLock.writeLock().lock();
    try {
      m_cachedHandlers.put(notificationClass, notificationHandlers);
    }
    finally {
      m_cachedHandlerLock.writeLock().unlock();
    }
  }

  protected List<INotificationHandler<? extends Serializable>> findHandlers(Class<? extends Serializable> notificationClass) {
    List<INotificationHandler<? extends Serializable>> handlers = new LinkedList<>();
    m_classToHandlerLock.readLock().lock();
    try {
      for (Entry<Class<? extends Serializable> /*notification class*/, List<INotificationHandler<? extends Serializable>>> e : m_notificationClassToHandler.entrySet()) {
        if (e.getKey().isAssignableFrom(notificationClass)) {
          handlers.addAll(e.getValue());
        }
      }
    }
    finally {
      m_classToHandlerLock.readLock().unlock();
    }
    return handlers;
  }

}
