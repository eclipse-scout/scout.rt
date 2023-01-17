/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.calendar;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.basic.calendar.provider.ICalendarItemProvider;

/**
 * Two or more items of different {@link ICalendarItemProvider}s that have same subject and overlapping dates.
 */
public class CalendarItemConflict {
  private final Map<Class<? extends ICalendarItemProvider>, Collection<CalendarComponent>> m_componentsByProvider;
  private final List<CalendarComponent> m_components;
  private final double m_match;

  public CalendarItemConflict(Map<Class<? extends ICalendarItemProvider>, Collection<CalendarComponent>> componentsByProvider, List<CalendarComponent> components, double match) {
    m_componentsByProvider = componentsByProvider;
    m_components = components;
    m_match = match;
  }

  /**
   * Average match in percent. 1.0 = full match (appointment[subject, start, end], task[subject, start, due, complete])
   * ... 0.5 = date ranges overlap in 50% ... 0.0 = date ranges just touch
   */
  public double getMatch() {
    return m_match;
  }

  public List<CalendarComponent> getComponents() {
    return m_components;
  }

  /**
   * Accept only the component of this provider, all other items are removed from the componentsByProvider map.
   */
  public void acceptComponentOf(Class<? extends ICalendarItemProvider> providerType) {
    for (CalendarComponent comp : m_components) {
      Class<? extends ICalendarItemProvider> pcls = comp.getProvider().getClass();
      if (!providerType.isAssignableFrom(pcls)) {
        m_componentsByProvider.get(pcls).remove(comp);
      }
    }
  }
}
