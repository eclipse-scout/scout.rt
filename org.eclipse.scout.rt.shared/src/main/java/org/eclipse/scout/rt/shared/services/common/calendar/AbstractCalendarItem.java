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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractCalendarItem implements ICalendarItem, Serializable {
  private static final long serialVersionUID = 1L;

  public static DateFormat getDumpDateFormat() {
    return new SimpleDateFormat("dd.MM.yy HH:mm:ss");
  }

  public static DateFormat getExchangeableDateFormat() {
    return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
  }

  private boolean m_exists = true;
  private long m_lastModified;
  private Object m_itemId;
  private String m_owner;
  private long m_calendarId;
  private String m_subject;
  private String m_subjectLabel;
  private String m_subjectAppLink;
  private String m_subjectIconId;
  private String m_body;
  private String m_cssClass;
  private RecurrencePattern m_recurrencyPattern;
  private List<ICalendarItemDescriptionElement> m_descriptionElements = new ArrayList<>();

  /**
   * External key is intentionally not copied in the copy() method.
   */
  private Serializable m_externalKey;

  public AbstractCalendarItem() {
    this(0L);
  }

  public AbstractCalendarItem(Object id) {
    m_itemId = id;
  }

  public ICalendarItem copy() {
    Class<? extends ICalendarItem> c = getClass();
    try {
      AbstractCalendarItem a = (AbstractCalendarItem) c.getConstructor().newInstance();
      a.m_exists = this.m_exists;
      a.m_lastModified = this.m_lastModified;
      a.m_itemId = this.m_itemId;
      a.m_owner = this.m_owner;
      a.m_subject = this.m_subject;
      a.m_subjectLabel = this.m_subjectLabel;
      a.m_subjectAppLink = this.m_subjectAppLink;
      a.m_subjectIconId = this.m_subjectIconId;
      a.m_body = this.m_body;
      a.m_cssClass = this.m_cssClass;
      a.m_recurrencyPattern = this.m_recurrencyPattern;
      if (this.m_descriptionElements != null) {
        for (ICalendarItemDescriptionElement descriptionElement : this.m_descriptionElements) {
          a.m_descriptionElements.add(descriptionElement.copy());
        }
      }
      return a;
    }
    catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
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
  public long getCalendarId() {
    return m_calendarId;
  }

  @Override
  public void setCalendarId(long id) {
    m_calendarId = id;
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
  public String getSubjectLabel() {
    return m_subjectLabel;
  }

  @Override
  public void setSubjectLabel(String subjectLabel) {
    m_subjectLabel = subjectLabel;
  }

  @Override
  public String getSubjectAppLink() {
    return m_subjectAppLink;
  }

  @Override
  public void setSubjectAppLink(String subjectAppLink) {
    m_subjectAppLink = subjectAppLink;
  }

  @Override
  public String getSubjectIconId() {
    return m_subjectIconId;
  }

  @Override
  public void setSubjectIconId(String subjectIconId) {
    m_subjectIconId = subjectIconId;
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

  @Override
  public List<ICalendarItemDescriptionElement> getDescriptionElements() {
    return m_descriptionElements;
  }

  public void setDescriptionElements(List<ICalendarItemDescriptionElement> descriptionElements) {
    m_descriptionElements = descriptionElements;
  }

  protected void dumpState(Map<String, Object> attributes) {
    attributes.put("exists", m_exists);
    attributes.put("lastModified", getDumpDateFormat().format(m_lastModified));
    attributes.put("id", String.valueOf(m_itemId));
    attributes.put("owner", m_owner);
    attributes.put("subject", m_subject);
    attributes.put("subjectLabel", m_subjectLabel);
    attributes.put("subjectAppLink", m_subjectAppLink);
    attributes.put("subjectIconId", m_subjectIconId);
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
    Map<String, Object> attributes = new HashMap<>();
    dumpState(attributes);
    int count = 0;
    for (Entry<String, Object> e : attributes.entrySet()) {
      if (e.getValue() != null) {
        if (count > 0) {
          b.append(", ");
        }
        b.append(e.getKey()).append("=").append(e.getValue());
        count++;
      }
    }
    b.append(", descriptionElements={");
    for (ICalendarItemDescriptionElement descriptionElement : getDescriptionElements()) {
      b.append(descriptionElement.toString());
    }
    b.append("}");
    b.append("]");
    return b.toString();
  }
}
