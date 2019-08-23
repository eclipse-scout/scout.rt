package org.eclipse.scout.rt.dataformat.ical.builder;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;

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
   * If start date is null, no dates will be written. If end date is null, no end date will be written (means all-day
   * event)
   */
  public ICalVEventBuilder withScheduling(Date startDate, Date endDate, Boolean allDay) {
    if (startDate == null) {
      return this;
    }
    if (BooleanUtility.nvl(allDay)) {
      m_cal.addProperty(new Property(ICalProperties.PROP_NAME_DTSTART, m_helper.createDate(startDate)));
      m_cal.addProperty(new Property(ICalProperties.PROP_NAME_DTEND, m_helper.createDate(endDate)));
    }
    else {
      m_cal.addProperty(new Property(ICalProperties.PROP_NAME_DTSTART, m_helper.createDateTime(startDate)));
      m_cal.addProperty(new Property(ICalProperties.PROP_NAME_DTEND, m_helper.createDateTime(endDate)));
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
