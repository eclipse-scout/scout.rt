/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.notification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.CreateImmediately;

/**
 * The {@link NotificationCoalescer} is used to coalesce notifications.
 */
@ApplicationScoped
@CreateImmediately
public class NotificationCoalescer {

  /**
   * static bindings of available {@link ICoalescer}
   */
  private final Map<Class<? extends Serializable> /*notification class*/, Set<ICoalescer<? extends Serializable>>> m_notificationClassToCoalescer = new HashMap<>();

  /**
   * dynamic bindings of notifications to {@link ICoalescer}
   */
  private final Map<Class<? extends Serializable> /*notification class*/, Set<ICoalescer<? extends Serializable>>> m_cachedNotificationCoalescers = new HashMap<>();
  private final Object m_cacheLock = new Object();

  private boolean m_useCachedNotificationCoalescerLookup = true;

  /**
   * builds a linking of all handlers generic types to handlers. This is used to find the corresponding handler of a
   * notification.
   */
  @SuppressWarnings("unchecked")
  @PostConstruct
  protected void buildCoalescerLinking() {
    List<ICoalescer> notificationCoalescers = BEANS.all(ICoalescer.class);
    for (ICoalescer<?> notificationCoalescer : notificationCoalescers) {
      Class notificationClass = TypeCastUtility.getGenericsParameterClass(notificationCoalescer.getClass(), ICoalescer.class);
      if (Serializable.class.isAssignableFrom(notificationClass)) {

        Set<ICoalescer<? extends Serializable>> coalescerSet = m_notificationClassToCoalescer.get(notificationClass);
        if (coalescerSet == null) {
          coalescerSet = new HashSet<>();
          m_notificationClassToCoalescer.put(notificationClass, coalescerSet);
        }
        coalescerSet.add((ICoalescer<? extends Serializable>) notificationCoalescer);
      }
    }
  }

  protected Set<ICoalescer<? extends Serializable>> getNotificationCoalescers(Class<? extends Serializable> notificationClass) {
    if (m_useCachedNotificationCoalescerLookup) {
      synchronized (m_cacheLock) {
        Set<ICoalescer<? extends Serializable>> notificationCoalescers = m_cachedNotificationCoalescers.get(notificationClass);
        if (notificationCoalescers == null) {
          notificationCoalescers = findNotificationCoalescers(notificationClass);
          m_cachedNotificationCoalescers.put(notificationClass, notificationCoalescers);
        }
        return new HashSet<ICoalescer<? extends Serializable>>(notificationCoalescers);
      }
    }
    else {
      return findNotificationCoalescers(notificationClass);
    }
  }

  private Set<ICoalescer<? extends Serializable>> findNotificationCoalescers(Class<? extends Serializable> notificationClass) {
    Set<ICoalescer<? extends Serializable>> coalescers = new HashSet<>();
    synchronized (m_cacheLock) {
      for (Entry<Class<? extends Serializable> /*notification class*/, Set<ICoalescer<? extends Serializable>>> e : m_notificationClassToCoalescer.entrySet()) {
        if (e.getKey().isAssignableFrom(notificationClass)) {
          coalescers.addAll(e.getValue());
        }
      }
    }
    return coalescers;
  }

  public List<? extends Serializable> coalesce(List<? extends Serializable> notificationsIn) {
    List<? extends Serializable> notifications = new ArrayList<>(notificationsIn);
    List<ICoalescer> notificationCoalescers = BEANS.all(ICoalescer.class);
    for (ICoalescer notificationCoalescer : notificationCoalescers) {
      notifications = coalesce(notificationCoalescer, notifications);
    }

    return notifications;
  }

  @SuppressWarnings("unchecked")
  protected List<? extends Serializable> coalesce(ICoalescer coalescer, List<? extends Serializable> notifications) {
    List<Serializable> toCoalesceNotificaitons = new ArrayList<>();
    Iterator<? extends Serializable> notificationIt = notifications.iterator();
    while (notificationIt.hasNext()) {
      Serializable notification = notificationIt.next();
      if (getNotificationCoalescers(notification.getClass()).contains(coalescer)) {
        toCoalesceNotificaitons.add(notification);
        notificationIt.remove();
      }
    }
    if (!toCoalesceNotificaitons.isEmpty()) {
      notifications.addAll(coalescer.coalesce(toCoalesceNotificaitons));
    }
    return notifications;
  }

}
