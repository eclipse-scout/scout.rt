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

import java.util.AbstractCollection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.platform.util.date.DateUtility;

public class CalendarAppointment extends AbstractCalendarItem implements ICalendarAppointment {
  private static final long serialVersionUID = 1L;
  //
  private Object m_person;
  private Date m_start;
  private Date m_end;
  private boolean m_fullDay;
  private String m_location;
  private int m_busyStatus;
  private final AbstractCollection<String> m_recipientEmail = new HashSet<>();
  private String m_mailboxName;

  public CalendarAppointment() {
    super();
  }

  /**
   * @param itemId
   * @param person
   * @param startDate
   * @param endDate
   * @param fullDay
   * @param subject
   * @param body
   * @param cssClass
   */
  @SuppressWarnings("squid:S00107")
  public CalendarAppointment(Object itemId, Object person, Date startDate, Date endDate, boolean fullDay, String location, String subject, String body, String cssClass) {
    setItemId(itemId);
    setPerson(person);
    setStart(startDate);
    setEnd(endDate);
    setFullDay(fullDay);
    setLocation(location);
    setSubject(subject);
    setBody(body);
    setCssClass(cssClass);
  }

  /**
   * @param itemId
   * @param person
   * @param startDate
   * @param endDate
   * @param fullDay
   * @param location
   * @param subject
   * @param body
   * @param cssClass
   */
  public CalendarAppointment(Object[] data) {
    if (data != null) {
      for (int i = 0; i < data.length; i++) {
        if (data[i] != null) {
          switch (i) {
            case 0: {
              setItemId(data[i]);
              break;
            }
            case 1: {
              setPerson(data[i]);
              break;
            }
            case 2: {
              setStart((Date) data[i]);
              break;
            }
            case 3: {
              setEnd((Date) data[i]);
              break;
            }
            case 4: {
              setFullDay(TypeCastUtility.castValue(data[i], Boolean.class));
              break;
            }
            case 5: {
              setLocation((String) data[i]);
              break;
            }
            case 6: {
              setSubject((String) data[i]);
              break;
            }
            case 7: {
              setBody((String) data[i]);
              break;
            }
            case 8: {
              setCssClass((String) data[i]);
              break;
            }
          }
        }
      }
    }
  }

  @Override
  public boolean isIntersecting(Date minDate, Date maxDate) {
    return DateUtility.intersects(m_start, m_end, minDate, maxDate);
  }

  @Override
  public Object getPerson() {
    return m_person;
  }

  @Override
  public void setPerson(Object person) {
    m_person = person;
  }

  @Override
  public Date getStart() {
    return m_start;
  }

  @Override
  public void setStart(Date a) {
    m_start = a;
  }

  @Override
  public Date getEnd() {
    return m_end;
  }

  @Override
  public void setEnd(Date a) {
    m_end = a;
  }

  @Override
  public boolean isFullDay() {
    return m_fullDay;
  }

  @Override
  public void setFullDay(boolean a) {
    m_fullDay = a;
  }

  @Override
  public String getLocation() {
    return m_location;
  }

  @Override
  public void setLocation(String a) {
    m_location = a;
  }

  @Override
  public int getBusyStatus() {
    return m_busyStatus;
  }

  @Override
  public void setBusyStatus(int a) {
    m_busyStatus = a;
  }

  @Override
  public String getMailboxName() {
    return m_mailboxName;
  }

  @Override
  public void setMailboxName(String mailboxName) {
    m_mailboxName = mailboxName;
  }

  @Override
  public String[] getRecipientEmail() {
    return m_recipientEmail.toArray(new String[0]);
  }

  public void addRecipientEmail(String recipientEmail) {
    m_recipientEmail.add(recipientEmail);
  }

  @Override
  public void removeRecipientEmail(String recipientEmail) {
    m_recipientEmail.remove(recipientEmail);
  }

  @Override
  public void removeAllRecipientEmail() {
    m_recipientEmail.clear();
  }

  @Override
  public ICalendarItem copy() {
    CalendarAppointment a = (CalendarAppointment) super.copy();
    a.m_person = this.m_person;
    a.m_start = this.m_start;
    a.m_end = this.m_end;
    a.m_fullDay = this.m_fullDay;
    a.m_location = this.m_location;
    a.m_busyStatus = this.m_busyStatus;
    return a;
  }

  @Override
  protected void dumpState(Map<String, Object> attributes) {
    super.dumpState(attributes);
    if (m_start != null) {
      attributes.put("start", getDumpDateFormat().format(m_start));
    }
    if (m_end != null) {
      attributes.put("end", getDumpDateFormat().format(m_end));
    }
    attributes.put("fullDay", m_fullDay);
    attributes.put("location", m_location);
    attributes.put("busyStatus", m_busyStatus);
    attributes.put("recipientEmail", m_recipientEmail);
    attributes.put("mailboxName", m_mailboxName);
  }

  @Override
  public String getDescription() {
    StringBuilder sb = new StringBuilder();
    if (!StringUtility.isNullOrEmpty(m_mailboxName)) {
      sb.append(m_mailboxName);
    }
    if (!StringUtility.isNullOrEmpty(m_location)) {
      if (sb.length() > 0) {
        sb.append("\n");
      }
      sb.append(m_location);
    }
    if (!StringUtility.isNullOrEmpty(getBody())) {
      if (sb.length() > 0) {
        sb.append("\n");
      }
      sb.append(getBody());
    }
    return sb.toString();
  }
}
