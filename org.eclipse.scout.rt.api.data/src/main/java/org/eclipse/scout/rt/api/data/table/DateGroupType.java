/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.table;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.Date;

import org.eclipse.scout.rt.dataobject.enumeration.EnumName;
import org.eclipse.scout.rt.dataobject.enumeration.IEnum;
import org.eclipse.scout.rt.platform.nls.NlsLocale;

@EnumName("scout.DateGroupType")
public enum DateGroupType implements IEnum {
  YEAR("year") {
    @Override
    protected long toKey(LocalDate date) {
      return date.getYear();
    }
  },
  MONTH("month") {
    @Override
    protected long toKey(LocalDate date) {
      return date.getMonthValue();
    }
  },
  MONTH_AND_YEAR("month-and-year") {
    @Override
    protected long toKey(LocalDate date) {
      return (date.getYear() * 100L) + date.getMonthValue(); // 2026-03-27 -> 202603
    }
  },
  CALENDAR_WEEK("calendar-week") {
    @Override
    protected long toKey(LocalDate date) {
      return date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }
  },
  WEEKDAY("weekday") {
    @Override
    protected long toKey(LocalDate date) {
      return date.get(WeekFields.of(NlsLocale.get()).dayOfWeek());
    }
  },
  DATE("date") {
    @Override
    protected long toKey(LocalDate date) {
      return date.toEpochDay();
    }
  };

  private final String m_stringValue;

  DateGroupType(String stringValue) {
    m_stringValue = stringValue;
  }

  @Override
  public String stringValue() {
    return m_stringValue;
  }

  /**
   * Returns a numeric value that can be used to order dates according to this group type.
   */
  protected long toKey(Date date) {
    LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    return toKey(localDate);
  }

  /**
   * Returns a numeric value that can be used to order dates according to this group type.
   */
  protected abstract long toKey(LocalDate date);

  /**
   * Compares the given dates with respect to this group type. For example, {@link #MONTH},
   * would sort 2017-08-01 _after_ 2026-03-27, because August comes after March.
   */
  public int compare(Date d1, Date d2) {
    if (d1 == null && d2 == null) {
      return 0;
    }
    if (d1 == null) {
      return -1;
    }
    if (d2 == null) {
      return 1;
    }
    return Long.compare(toKey(d1), toKey(d2));
  }
}
