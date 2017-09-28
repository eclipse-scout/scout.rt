/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.datefield;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Basic string field test
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractDateFieldTest {

  private AbstractDateField m_field;
  private Date m_date;

  @Before
  public void reset() {
    m_field = null;
    m_date = null;
  }

  @Test
  public void testGetConfiguredFormatPatterns() {
    givenField(new TestingDateTimeField("yyyy-MM-dd", "HH:mm:ss"));

    // 8 - 1 because months are 0-indexed, so this is August.
    givenDate(2017, 8 - 1, 22, 10, 11, 12);

    thenFormattedValueIs("2017-08-22\n10:11:12");
  }

  @Test
  public void testGetConfiguredDateFormatPatternOnly() {
    givenField(new TestingDateTimeField("yyyy-MM-dd"));

    // 8 - 1 because months are 0-indexed, so this is August.
    givenDate(2017, 8 - 1, 22, 10, 11, 12);

    thenFormattedValueIs("2017-08-22");
  }

  @Test
  public void testGetConfiguredFormat() {
    givenField(new TestingDateTimeSingleFormatField("yyyy-MM-dd HH:mm:ss"));

    // 8 - 1 because months are 0-indexed, so this is August.
    givenDate(2017, 8 - 1, 22, 10, 11, 12);

    thenFormattedValueIs("2017-08-22\n10:11:12");
  }

  private void givenField(AbstractDateField field) {
    m_field = field;
  }

  private void givenDate(int year, int month, int day, int hour, int minute, int second) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(year, month, day, hour, minute, second);

    m_date = DateUtility.convertCalendar(calendar);
  }

  private void thenFormattedValueIs(String expected) {
    String actual = m_field.formatValueInternal(m_date);
    assertEquals(expected, actual);
  }

  private class TestingDateTimeSingleFormatField extends AbstractDateField {
    private final String m_configuredFormat;

    public TestingDateTimeSingleFormatField(String format) {
      super(false);
      m_configuredFormat = format;
      callInitializer();
    }

    @Override
    protected boolean getConfiguredHasTime() {
      return true;
    }

    @Override
    protected String getConfiguredFormat() {
      return m_configuredFormat;
    }
  }

  private class TestingDateTimeField extends AbstractDateField {
    private final String m_configuredDateFormatPattern;
    private final String m_configuredTimeFormatPattern;

    public TestingDateTimeField(String configuredDateFormatPattern) {
      this(configuredDateFormatPattern, null);
    }

    public TestingDateTimeField(String configuredDateFormatPattern, String configuredTimeFormatPattern) {
      super(false);
      m_configuredDateFormatPattern = configuredDateFormatPattern;
      m_configuredTimeFormatPattern = configuredTimeFormatPattern;
      callInitializer();
    }

    @Override
    protected boolean getConfiguredHasTime() {
      return m_configuredTimeFormatPattern != null;
    }

    @Override
    protected String getConfiguredDateFormatPattern() {
      return m_configuredDateFormatPattern;
    }

    @Override
    protected String getConfiguredTimeFormatPattern() {
      return m_configuredTimeFormatPattern;
    }
  }
}
