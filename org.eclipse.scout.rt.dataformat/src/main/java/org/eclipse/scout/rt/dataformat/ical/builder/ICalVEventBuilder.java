/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataformat.ical.builder;

import static org.eclipse.scout.rt.platform.util.date.DateUtility.nextDay;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.scout.rt.dataformat.ical.ICalBean;
import org.eclipse.scout.rt.dataformat.ical.ICalProperties;
import org.eclipse.scout.rt.dataformat.ical.model.ICalVCardHelper;
import org.eclipse.scout.rt.dataformat.ical.model.Property;
import org.eclipse.scout.rt.dataformat.ical.model.PropertyParameter;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;

@Bean
public class ICalVEventBuilder {
  protected ICalBean m_cal = BEANS.get(ICalBean.class);
  protected ICalVCardHelper m_helper = BEANS.get(ICalVCardHelper.class);

  public ICalVEventBuilder() {
    begin();
  }

  protected ICalVEventBuilder begin() {
    m_cal.addProperty(ICalProperties.PROP_BEGIN_VEVENT);
    m_cal.addProperty(new Property(ICalProperties.PROP_NAME_DTSTAMP, m_helper.createDateTime(BEANS.get(IDateProvider.class).currentMillis())));
    return this;
  }

  public ICalVEventBuilder withUid(String uid) {
    m_cal.addProperty(new Property(ICalProperties.PROP_NAME_UID, uid));
    return this;
  }

  public ICalVEventBuilder withDescription(String description) {
    if (StringUtility.isNullOrEmpty(description)) {
      return this;
    }
    m_cal.addProperty(new Property(ICalProperties.PROP_NAME_DESCRIPTION, description));
    return this;
  }

  /**
   * Adds start and (optionally) end date.
   *
   * @param startDate
   *     The start date.
   * @param endDate
   *     The optional end date.
   * @return this instance
   */
  public ICalVEventBuilder withScheduling(LocalDate startDate, LocalDate endDate) {
    if (startDate == null) {
      return this;
    }

    ZoneId utc = ZoneId.of("UTC");
    Date start = Date.from(startDate.atStartOfDay().atZone(utc).toInstant());
    Date end = null;
    if (endDate != null) {
      end = Date.from(endDate.atStartOfDay().atZone(utc).toInstant());
    }
    return withScheduling(start, end, true, TimeZone.getTimeZone(utc));
  }

  /**
   * Adds start and (optionally) end date.
   *
   * @param startDate
   *     The start date. The UTC time zone is used to interpret the {@link Date}.
   * @param endDate
   *     The optional end date. The UTC time zone is used to interpret the {@link Date}.
   * @param allDay
   *     Specifies if it is an all-day event. If {@code true}, the time is ignored.
   * @return this instance
   */
  public ICalVEventBuilder withScheduling(Date startDate, Date endDate, Boolean allDay) {
    return withScheduling(startDate, endDate, allDay, TimeZone.getDefault());
  }

  /**
   * Adds start and (optionally) end date.
   *
   * @param startDate
   *     The start date. The UTC time zone is used to interpret the {@link Date}.
   * @param endDate
   *     The optional end date. The UTC time zone is used to interpret the {@link Date}.
   * @param allDay
   *     Specifies if it is an all-day event. If {@code true}, the time is ignored.
   * @param zone
   *     The {@link TimeZone} of the given {@link Date} arguments. Specifies in which timezone the given dates
   *     should be interpreted and therefore which date the iCal event will have. Only used for allDay events.
   * @return this instance
   */
  public ICalVEventBuilder withScheduling(Date startDate, Date endDate, Boolean allDay, TimeZone zone) {
    if (startDate == null) {
      return this;
    }
    if (BooleanUtility.nvl(allDay)) {
      m_cal.addProperty(new Property(ICalProperties.PROP_NAME_DTSTART, m_helper.createDate(m_helper.removeTimeZoneOffset(startDate, zone))));
      if (endDate != null) {
        m_cal.addProperty(new Property(ICalProperties.PROP_NAME_DTEND, m_helper.createDate(nextDay(m_helper.removeTimeZoneOffset(endDate, zone)))));
      }
    }
    else {
      m_cal.addProperty(new Property(ICalProperties.PROP_NAME_DTSTART, m_helper.createDateTime(startDate)));
      if (endDate != null) {
        m_cal.addProperty(new Property(ICalProperties.PROP_NAME_DTEND, m_helper.createDateTime(endDate)));
      }
    }
    return this;
  }

  public ICalVEventBuilder withLocation(String location) {
    if (StringUtility.isNullOrEmpty(location)) {
      return this;
    }
    m_cal.addProperty(new Property(ICalProperties.PROP_NAME_LOCATION, location));
    return this;
  }

  public ICalVEventBuilder withSummary(String summary) {
    if (StringUtility.isNullOrEmpty(summary)) {
      return this;
    }
    m_cal.addProperty(new Property(ICalProperties.PROP_NAME_SUMMARY, summary));
    return this;
  }

  public ICalVEventBuilder withOrganizer(String name, String email) {
    m_cal.addProperty(new Property(ICalProperties.PROP_NAME_ORGANIZER, Arrays.asList(new PropertyParameter(ICalProperties.PARAM_NAME_CN, name == null ? null : "\"" + name + "\"")),
        email == null ? null : "mailto:" + email));
    return this;
  }

  /**
   * Creates an alarm component for this VEVENT, if and only if: - triggerOffset is not null triggerOffset will be
   * negated (e. g. a passed Duration of 30min means 30 minutes before VEVENT)
   */
  public ICalVEventBuilder withAlarm(Duration triggerOffset, String description) {
    if (triggerOffset == null) {
      return this;
    }
    m_cal.addProperty(ICalProperties.PROP_BEGIN_VALARM);
    m_cal.addProperty(new Property(ICalProperties.PROP_NAME_ACTION, ICalProperties.PROP_VALUE_DISPLAY));
    m_cal.addProperty(new Property(ICalProperties.PROP_NAME_TRIGGER, m_helper.formatDurationAsNegative(triggerOffset.abs())));
    m_cal.addProperty(new Property(ICalProperties.PROP_NAME_DESCRIPTION, description));
    m_cal.addProperty(ICalProperties.PROP_END_VALARM);
    return this;
  }

  protected ICalVEventBuilder end() {
    m_cal.addProperty(ICalProperties.PROP_END_VEVENT);
    return this;
  }

  public ICalBean build() {
    end();
    return m_cal;
  }
}
