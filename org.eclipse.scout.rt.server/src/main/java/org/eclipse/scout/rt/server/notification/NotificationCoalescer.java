/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.notification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.TypeParameterBeanRegistry;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * The {@link NotificationCoalescer} is used to coalesce notifications.
 * <p>
 * All beans implementing ICoalescer&ltT&gt
 * </p>
 */
@ApplicationScoped
@CreateImmediately
public class NotificationCoalescer {
  private final TypeParameterBeanRegistry<ICoalescer> m_registry = new TypeParameterBeanRegistry<>(ICoalescer.class);

  @PostConstruct
  protected void buildCoalescerLinking() {
    m_registry.registerBeans(BEANS.all(ICoalescer.class));
  }

  public <T extends Serializable> List<T> coalesce(List<T> notificationsIn) {
    if (notificationsIn.size() < 2) {
      return notificationsIn;
    }
    else {
      int i = 0;
      List<T> res = notificationsIn;
      while (i < res.size()) {
        final List<ICoalescer> coalescers = m_registry.getBeans(res.get(i).getClass());
        @SuppressWarnings("unchecked")
        ICoalescer<T> c = CollectionUtility.firstElement(coalescers);
        if (c != null) {
          int j = getCoalesceCount(c, res.subList(i, res.size()));
          res = coalesce(i, i + j, c, res);
        }
        i++;
      }
      return res;
    }
  }

  private <T extends Serializable> List<T> coalesce(int from, int to, ICoalescer<T> c, List<T> notifications) {
    List<T> res = new ArrayList<>();
    res.addAll(notifications.subList(0, from));
    res.addAll(c.coalesce(notifications.subList(from, to)));
    res.addAll(notifications.subList(to, notifications.size()));
    return res;
  }

  private int getCoalesceCount(ICoalescer<?> c, List<? extends Serializable> notifications) {
    int i = 0;
    while (i < notifications.size() && isApplicable(c, notifications, i)) {
      i++;
    }
    return i;
  }

  /**
   * @return <code>true</code>, if the {@link ICoalescer} is applicable to the message.
   */
  private boolean isApplicable(ICoalescer<?> c, List<? extends Serializable> notifications, int i) {
    Serializable n = notifications.get(i);
    return m_registry.getBeans(n.getClass()).contains(c);
  }
}
