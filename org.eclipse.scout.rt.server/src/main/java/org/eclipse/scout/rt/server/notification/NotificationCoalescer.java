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
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.shared.notification.TypeParameterBeanRegistry;

/**
 * The {@link NotificationCoalescer} is used to coalesce notifications.
 */
@ApplicationScoped
@CreateImmediately
public class NotificationCoalescer {
  private final TypeParameterBeanRegistry<ICoalescer> m_registry = new TypeParameterBeanRegistry<>();

  @PostConstruct
  protected void buildCoalescerLinking() {
    m_registry.registerBeans(ICoalescer.class, BEANS.all(ICoalescer.class));
  }

  @SuppressWarnings("unchecked")
  public List<? extends Serializable> coalesce(List<? extends Serializable> notificationsIn) {
    final List<ICoalescer> coalescers = m_registry.getBeans(getClasses(notificationsIn));

    List<? extends Serializable> notifications = new ArrayList<>(notificationsIn);
    for (ICoalescer notificationCoalescer : coalescers) {
      notifications = notificationCoalescer.coalesce(notifications);
    }

    return notifications;
  }

  private List<Class<?>> getClasses(List<? extends Serializable> objects) {
    List<Class<?>> classList = new ArrayList<>();
    for (Serializable n : objects) {
      classList.add(n.getClass());
    }
    return classList;
  }
}
