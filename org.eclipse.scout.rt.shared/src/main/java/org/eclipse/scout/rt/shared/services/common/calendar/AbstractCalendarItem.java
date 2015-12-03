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
package org.eclipse.scout.rt.shared.services.common.calendar;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCalendarItem implements ICalendarItem, Serializable {
  private static final long serialVersionUID = 1L;

  public static final DateFormat getDumpDateFormat() {
    return new SimpleDateFormat("dd.MM.yy HH:mm:ss");
  }

  public static final DateFormat getExchangeableDateFormat() {
    return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
  }

  private boolean m_exists = true;
  private long m_lastModified;
  private Object m_itemId;
  private String m_owner;
  private String m_subject;
  private String m_body;
  private String m_color;
  private String m_cssClass;
  private RecurrencePattern m_recurrencyPattern;

  /**
   * External key is intentionally not copied in the copy() method.
   */
  private Serializable m_externalKey;

  public AbstractCalendarItem() {
    this(Long.valueOf(0L));
  }

  public AbstractCalendarItem(Object id) {
    m_itemId = id;
  }

  public ICalendarItem copy() {
    Class<? extends ICalendarItem> c = getClass();
    try {
      AbstractCalendarItem a = (AbstractCalendarItem) c.newInstance();
      a.m_exists = this.m_exists;
      a.m_lastModified = this.m_lastModified;
      a.m_itemId = this.m_itemId;
      a.m_owner = this.m_owner;
      a.m_subject = this.m_subject;
      a.m_body = this.m_body;
      a.m_color = this.m_color;
      a.m_cssClass = this.m_cssClass;
      a.m_recurrencyPattern = this.m_recurrencyPattern;
      return a;
    }
    catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  @SuppressWarnings("deprecation")
  public String getColor() {
    return m_color;
  }

  @Override
  @SuppressWarnings("deprecation")
  public void setColor(String hex) {
    m_color = hex;
  }

  @Override
  public String getCssClass() {
    return m_cssClass;
  }

  @Override
  public void setCssClass(String cssClass) {
    m_cssClass = cssClass;
  }

  @Override
  public boolean exists() {
    return m_exists;
  }

  @Override
  public void delete() {
    m_exists = false;
  }

  @Override
  public long getLastModified() {
    return m_lastModified;
  }

  @Override
  public void setLastModified(long b) {
    m_lastModified = b;
  }

  @Override
  public Object getItemId() {
    return m_itemId;
  }

  @Override
  public void setItemId(Object itemId) {
    m_itemId = itemId;
  }

  @Override
  public String getOwner() {
    return m_owner;
  }

  @Override
  public void setOwner(String a) {
    m_owner = a;
  }

  @Override
  public String getSubject() {
    return m_subject;
  }

  @Override
  public void setSubject(String a) {
    m_subject = a;
  }

  @Override
  public String getBody() {
    return m_body;
  }

  @Override
  public void setBody(String a) {
    m_body = a;
  }

  @Override
  public RecurrencePattern getRecurrencePattern() {
    return m_recurrencyPattern;
  }

  @Override
  public void setRecurrencePattern(RecurrencePattern p) {
    m_recurrencyPattern = p;
  }

  protected void dumpState(Map<String, Object> attributes) {
    attributes.put("exists", m_exists);
    attributes.put("lastModified", getDumpDateFormat().format(m_lastModified));
    attributes.put("id", String.valueOf(m_itemId));
    attributes.put("owner", m_owner);
    attributes.put("subject", m_subject);
    if (m_body != null) {
      attributes.put("body", m_body.replace('\n', ' ').replace('\r', ' ').substring(0, Math.min(200, m_body.length())));
    }
    attributes.put("recurrencyPattern", m_recurrencyPattern);
  }

  @Override
  public Serializable getExternalKey() {
    return m_externalKey;
  }

  @Override
  public void setExternalKey(Serializable externalKey) {
    m_externalKey = externalKey;
  }

  @Override
  public String getDescription() {
    return m_body;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append(getClass().getSimpleName());
    b.append("[");
    HashMap<String, Object> attributes = new HashMap<String, Object>();
    dumpState(attributes);
    int count = 0;
    for (Map.Entry<String, Object> e : attributes.entrySet()) {
      if (e.getValue() != null) {
        if (count > 0) {
          b.append(", ");
        }
        b.append(e.getKey() + "=" + e.getValue());
        count++;
      }
    }
    b.append("]");
    return b.toString();
  }
}
