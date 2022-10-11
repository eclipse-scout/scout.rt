/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.services.common.calendar;

import java.io.Serializable;
import java.util.Map;

public class CalendarItemDescriptionElement implements ICalendarItemDescriptionElement, Serializable {
  private static final long serialVersionUID = 1L;

  private String m_text;
  private String m_iconId;
  private String m_appLink;

  public CalendarItemDescriptionElement() {
    this(null, null, null);
  }

  public CalendarItemDescriptionElement(String text, String iconId, String appLink) {
    super();
    m_text = text;
    m_iconId = iconId;
    m_appLink = appLink;
  }

  @Override
  public String getText() {
    return m_text;
  }

  public void setText(String text) {
    m_text = text;
  }

  @Override
  public String getIconId() {
    return m_iconId;
  }

  public void setIconId(String iconId) {
    m_iconId = iconId;
  }

  @Override
  public String getAppLink() {
    return m_appLink;
  }

  public void setAppLink(String appLink) {
    m_appLink = appLink;
  }

  @Override
  public ICalendarItemDescriptionElement copy() {
    Class<? extends ICalendarItemDescriptionElement> c = getClass();
    try {
      CalendarItemDescriptionElement a = (CalendarItemDescriptionElement) c.getConstructor().newInstance();
      a.m_text = this.m_text;
      a.m_iconId = this.m_iconId;
      a.m_appLink = this.m_appLink;
      return a;
    }
    catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected void dumpState(Map<String, Object> attributes) {
    attributes.put("text", m_text);
    attributes.put("iconId", m_iconId);
    attributes.put("appLink", m_appLink);
  }

  @Override
  public String toString() {
    return "CalendarItemDescriptionElement [text=" + m_text + ", iconId=" + m_iconId + ", appLink=" + m_appLink + "]";
  }
}
