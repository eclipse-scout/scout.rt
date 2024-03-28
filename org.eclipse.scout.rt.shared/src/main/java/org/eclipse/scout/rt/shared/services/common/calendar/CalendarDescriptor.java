/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.calendar;

import java.util.Objects;
import java.util.UUID;

public class CalendarDescriptor implements ICalendarDescriptor {

  private final String m_calendarId;
  private String m_name;
  private String m_parentId;
  private boolean m_visible;
  private boolean m_selectable;
  private String m_cssClass;
  private long m_order;

  public CalendarDescriptor(String calendarId) {
    m_calendarId = calendarId;
    m_visible = true;
    m_selectable = true;
  }

  public CalendarDescriptor() {
    this(UUID.randomUUID().toString());
  }

  @Override
  public String getCalendarId() {
    return m_calendarId;
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public ICalendarDescriptor withName(String name) {
    m_name = name;
    return this;
  }

  @Override
  public String getParentId() {
    return m_parentId;
  }

  @Override
  public CalendarDescriptor withParentId(String parentId) {
    m_parentId = parentId;
    return this;
  }

  @Override
  public boolean isVisible() {
    return m_visible;
  }

  @Override
  public CalendarDescriptor withVisible(boolean visible) {
    m_visible = visible;
    return this;
  }

  @Override
  public boolean isSelectable() {
    return m_selectable;
  }

  @Override
  public CalendarDescriptor withSelectable(boolean selectable) {
    m_selectable = selectable;
    return this;
  }

  @Override
  public String getCssClass() {
    return m_cssClass;
  }

  @Override
  public CalendarDescriptor withCssClass(String cssClass) {
    m_cssClass = cssClass;
    return this;
  }

  @Override
  public long getOrder() {
    return m_order;
  }

  @Override
  public CalendarDescriptor withOrder(long order) {
    m_order = order;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CalendarDescriptor that = (CalendarDescriptor) o;

    if (m_visible != that.m_visible) {
      return false;
    }
    if (m_selectable != that.m_selectable) {
      return false;
    }
    if (m_order != that.m_order) {
      return false;
    }
    if (!m_calendarId.equals(that.m_calendarId)) {
      return false;
    }
    if (!Objects.equals(m_name, that.m_name)) {
      return false;
    }
    if (!Objects.equals(m_parentId, that.m_parentId)) {
      return false;
    }
    if (!Objects.equals(m_cssClass, that.m_cssClass)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = m_calendarId.hashCode();
    result = 31 * result + (m_name != null ? m_name.hashCode() : 0);
    result = 31 * result + (m_parentId != null ? m_parentId.hashCode() : 0);
    result = 31 * result + (m_visible ? 1 : 0);
    result = 31 * result + (m_selectable ? 1 : 0);
    result = 31 * result + (m_cssClass != null ? m_cssClass.hashCode() : 0);
    result = 31 * result + (int) (m_order ^ (m_order >>> 32));
    return result;
  }
}
