/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.calendar;

import java.util.Random;

public class CalendarDescriptor implements ICalendarDescriptor {

  private long m_calendarId;
  private String m_name;
  private String m_cssClass;

  public CalendarDescriptor(String name) {
    this(new Random().nextLong(), name);
  }

  public CalendarDescriptor(long calendarId, String name) {
    m_calendarId = calendarId;
    m_name = name;
  }

  public CalendarDescriptor(long calendarId, String name, String cssClass) {
    m_calendarId = calendarId;
    m_name = name;
    m_cssClass = cssClass;
  }

  @Override
  public long getCalendarId() {
    return m_calendarId;
  }

  @Override
  public void setCalendarId(long calendarId) {
    m_calendarId = calendarId;
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public void setName(String name) {
    m_name = name;
  }

  @Override
  public String getCssClass() {
    return m_cssClass;
  }

  @Override
  public void setCssClass(String cssClass) {
    m_cssClass = cssClass;
  }
}
