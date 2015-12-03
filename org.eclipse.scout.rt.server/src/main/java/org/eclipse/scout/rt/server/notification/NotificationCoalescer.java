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
 * <p>
 * All beans implementing ICoalescer&ltT&gt
 * </p>
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
    if (notificationsIn.size() < 2) {
      return notificationsIn;
    }
    else {
      int i = 0;
      List<? extends Serializable> res = notificationsIn;
      while (i < res.size()) {
        final List<ICoalescer> coalescers = m_registry.getBeans(res.get(i).getClass());
        if (coalescers.size() > 0) {
          ICoalescer c = coalescers.get(0);
          int j = getCoalesceCount(c, res.subList(i, res.size()));
          res = coalesce(i, i + j, c, res);
        }
        i++;
      }
      return res;
    }
  }

  private <T extends Serializable> List<? extends Serializable> coalesce(int from, int to, ICoalescer<T> c, List<T> notifications) {
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
