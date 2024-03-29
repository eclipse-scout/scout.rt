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

import java.io.Serializable;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;

@Bean
public class CalendarItemDescriptionElement implements ICalendarItemDescriptionElement, Serializable {
  private static final long serialVersionUID = 1L;

  private String m_text;
  private String m_iconId;
  private String m_appLink;

  @Override
  public String getText() {
    return m_text;
  }

  public CalendarItemDescriptionElement withText(String text) {
    m_text = text;
    return this;
  }

  @Override
  public String getIconId() {
    return m_iconId;
  }

  public CalendarItemDescriptionElement withIconId(String iconId) {
    m_iconId = iconId;
    return this;
  }

  @Override
  public String getAppLink() {
    return m_appLink;
  }

  public CalendarItemDescriptionElement withAppLink(String appLink) {
    m_appLink = appLink;
    return this;
  }

  @Override
  public ICalendarItemDescriptionElement copy() {
    return BEANS.get(CalendarItemDescriptionElement.class)
        .withText(getText())
        .withIconId(getIconId())
        .withAppLink(getAppLink());
  }

  @Override
  public String toString() {
    return "CalendarItemDescriptionElement [text=" + m_text + ", iconId=" + m_iconId + ", appLink=" + m_appLink + "]";
  }
}
