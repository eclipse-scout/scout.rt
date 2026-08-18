/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.util.date;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.util.AbstractScoutTestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Test rule to use an {@link IDateProvider} providing a fixed date.
 */
public class FixedDateRule extends AbstractScoutTestRule {

  private final FixedDateProvider m_dateProvider;

  public FixedDateRule() {
    this(new Date());
  }

  public FixedDateRule(Date date) {
    m_dateProvider = new FixedDateProvider(date);
  }

  public FixedDateProvider getDateProvider() {
    return m_dateProvider;
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {

      @Override
      public void evaluate() throws Throwable {
        // NOTE: register IDateProvider and NOT DateProvider -> there should be no BEANS.get(DateProvider.class) calls
        IBean<Object> bean = BeanTestingHelper.get().registerBean(new BeanMetaData(IDateProvider.class, getDateProvider()));
        try {
          base.evaluate();
        }
        finally {
          BeanTestingHelper.get().unregisterBean(bean);
        }
      }
    };
  }

  // ------------ delegate methods ------------

  /**
   * Change the date and time returned by the provider
   *
   * @param newTimeMillis
   *     new date and time to return as provider value
   */
  public void setTimeMillis(long newTimeMillis) {
    m_dateProvider.setTimeMillis(newTimeMillis);
  }

  /**
   * Change the date and time returned by the provider
   *
   * @param newDate
   *     new date and time to return as provider value
   */
  public void setDate(Date newDate) {
    m_dateProvider.setDate(newDate);
  }

  public Date getDate() {
    return m_dateProvider.getDate();
  }

  public Calendar currentCalendar() {
    return m_dateProvider.currentCalendar();
  }

  public long currentUTCMillis() {
    return m_dateProvider.currentUTCMillis();
  }

  public Date currentMillis() {
    return m_dateProvider.currentMillis();
  }

  public Date currentSeconds() {
    return m_dateProvider.currentSeconds();
  }

  public Date currentDay() {
    return m_dateProvider.currentDay();
  }

  public Date currentMonth() {
    return m_dateProvider.currentMonth();
  }

  public Date currentWeek() {
    return m_dateProvider.currentWeek();
  }

  public Date currentYear() {
    return m_dateProvider.currentYear();
  }

  public TimeZone currentTimeZone() {
    return m_dateProvider.currentTimeZone();
  }
}
