/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.dataformat.ical;

import org.eclipse.scout.rt.dataformat.ical.model.Property;

public final class ICalProperties {

  public static final String PROP_NAME_UNKNOWN = "UNKNOWN";
  public static final String PROP_NAME_VERSION = "VERSION";
  public static final String PROP_NAME_PRODID = "PRODID";
  public static final String PROP_NAME_REV = "REV";
  public static final String PROP_NAME_BEGIN = "BEGIN";
  public static final String PROP_NAME_END = "END";
  public static final String PROP_NAME_METHOD = "METHOD";
  public static final String PROP_NAME_DESCRIPTION = "DESCRIPTION";
  public static final String PROP_NAME_LAST_MODIFIED = "LAST-MODIFIED";
  public static final String PROP_NAME_RRULE = "RRULE";
  public static final String PROP_NAME_SEQUENCE = "SEQUENCE";
  public static final String PROP_NAME_SUMMARY = "SUMMARY";
  public static final String PROP_NAME_UID = "UID";
  public static final String PROP_NAME_DTSTAMP = "DTSTAMP";
  public static final String PROP_NAME_DTSTART = "DTSTART";
  public static final String PROP_NAME_DTEND = "DTEND";
  public static final String PROP_NAME_DUE = "DUE";
  public static final String PROP_NAME_LOCATION = "LOCATION";
  public static final String PROP_NAME_ORGANIZER = "ORGANIZER";
  public static final String PROP_NAME_ATTENDEE = "ATTENDEE";
  public static final String PROP_NAME_CREATED = "CREATED";
  public static final String PROP_NAME_CLASS = "CLASS";
  public static final String PROP_NAME_TZID = "TZID";
  public static final String PROP_NAME_TZOFFSETTO = "TZOFFSETTO";
  public static final String PROP_NAME_ACTION = "ACTION";
  public static final String PROP_NAME_TRIGGER = "TRIGGER";
  public static final String PROP_NAME_KIND = "KIND";

  public static final String PROP_VALUE_ATTACH = "ATTACH";
  public static final String PROP_VALUE_ICALENDAR = "VCALENDAR";
  public static final String PROP_VALUE_VEVENT = "VEVENT";
  public static final String PROP_VALUE_VTODO = "VTODO";
  public static final String PROP_VALUE_VALARM = "VALARM";
  public static final String PROP_VALUE_VTIMEZONE = "VTIMEZONE";
  public static final String PROP_VALUE_STANDARD = "STANDARD";
  public static final String PROP_VALUE_PUBLISH = "PUBLISH";
  public static final String PROP_VALUE_PUBLIC = "PUBLIC";
  public static final String PROP_VALUE_DISPLAY = "DISPLAY";
  public static final String PROP_VALUE_INDIVIDUAL = "INDIVIDUAL";
  public static final String PROP_VALUE_VERSION_2_1 = "2.1";

  public static final Property PROP_UNKNOWN = new Property(PROP_NAME_UNKNOWN);
  public static final Property PROP_BEGIN_ICALENDAR = new Property(PROP_NAME_BEGIN, PROP_VALUE_ICALENDAR);
  public static final Property PROP_END_ICALENDAR = new Property(PROP_NAME_END, PROP_VALUE_ICALENDAR);
  public static final Property PROP_VERSION_2_1 = new Property(PROP_NAME_VERSION, PROP_VALUE_VERSION_2_1);
  public static final Property PROP_BEGIN_VEVENT = new Property(PROP_NAME_BEGIN, PROP_VALUE_VEVENT);
  public static final Property PROP_END_VEVENT = new Property(PROP_NAME_END, PROP_VALUE_VEVENT);
  public static final Property PROP_BEGIN_VTODO = new Property(PROP_NAME_BEGIN, PROP_VALUE_VTODO);
  public static final Property PROP_END_VTODO = new Property(PROP_NAME_END, PROP_VALUE_VTODO);
  public static final Property PROP_BEGIN_VTIMEZONE = new Property(PROP_NAME_BEGIN, PROP_VALUE_VTIMEZONE);
  public static final Property PROP_END_VTIMEZONE = new Property(PROP_NAME_END, PROP_VALUE_VTIMEZONE);
  public static final Property PROP_METHOD_PUBLISH = new Property(PROP_NAME_METHOD, PROP_VALUE_PUBLISH);
  public static final Property PROP_BEGIN_STANDARD = new Property(PROP_NAME_BEGIN, PROP_VALUE_STANDARD);
  public static final Property PROP_END_STANDARD = new Property(PROP_NAME_END, PROP_VALUE_STANDARD);
  public static final Property PROP_BEGIN_VALARM = new Property(PROP_NAME_BEGIN, PROP_VALUE_VALARM);
  public static final Property PROP_END_VALARM = new Property(PROP_NAME_END, PROP_VALUE_VALARM);

  public static final String PARAM_NAME_CN = "CN";
  public static final String PARAM_NAME_RSVP = "RSVP";
  public static final String PARAM_NAME_X_FILENAME = "X-FILENAME";

  private ICalProperties() {
  }
}
