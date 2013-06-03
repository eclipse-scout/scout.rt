/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
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
import static org.junit.Assert.fail;

import java.util.Locale;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.DateAndTimeFieldParseTest.TestForm.MainBox.TestsGroupBox.DateOnlyField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.DateAndTimeFieldParseTest.TestForm.MainBox.TestsGroupBox.DateOnlyWithFormatField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.DateAndTimeFieldParseTest.TestForm.MainBox.TestsGroupBox.DateWithTimeField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.DateAndTimeFieldParseTest.TestForm.MainBox.TestsGroupBox.DateWithTimeWithFormatField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.DateAndTimeFieldParseTest.TestForm.MainBox.TestsGroupBox.TimeOnlyField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.DateAndTimeFieldParseTest.TestForm.MainBox.TestsGroupBox.TimeOnlyWithFormatField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.testing.client.form.FormHandler;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for parsing in AbstractDateFields and AbstractTimeFields.
 */
@RunWith(ScoutClientTestRunner.class)
public class DateAndTimeFieldParseTest {

  public static class TestForm extends AbstractForm {

    public TestForm() throws ProcessingException {
      super();
    }

    @Override
    protected String getConfiguredTitle() {
      return "Test Form";
    }

    @Override
    protected void execInitForm() throws ProcessingException {
      for (IFormField f : getAllFields()) {
        if (f instanceof IDateField) {
          ((IDateField) f).setAutoTimeMillis(0, 0, 0);
        }
      }
    }

    public void startForm() throws ProcessingException {
      startInternal(new FormHandler());
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    public DateOnlyField getDateOnlyField() {
      return getFieldByClass(DateOnlyField.class);
    }

    public DateWithTimeField getDateWithTimeField() {
      return getFieldByClass(DateWithTimeField.class);
    }

    public TimeOnlyField getTimeOnlyField() {
      return getFieldByClass(TimeOnlyField.class);
    }

    public DateOnlyWithFormatField getDateOnlyWithFormatField() {
      return getFieldByClass(DateOnlyWithFormatField.class);
    }

    public DateWithTimeWithFormatField getDateWithTimeWithFormatField() {
      return getFieldByClass(DateWithTimeWithFormatField.class);
    }

    public TimeOnlyWithFormatField getTimeOnlyWithFormatField() {
      return getFieldByClass(TimeOnlyWithFormatField.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class TestsGroupBox extends AbstractGroupBox {

        @Order(10)
        public class DateOnlyField extends AbstractDateField {
          @Override
          protected String getConfiguredLabel() {
            return getClass().getSimpleName();
          }

          @Override
          protected boolean getConfiguredHasTime() {
            return false;
          }

        }

        @Order(20)
        public class DateWithTimeField extends AbstractDateField {
          @Override
          protected String getConfiguredLabel() {
            return getClass().getSimpleName();
          }

          @Override
          protected boolean getConfiguredHasTime() {
            return true;
          }
        }

        @Order(30)
        public class TimeOnlyField extends AbstractTimeField {
          @Override
          protected String getConfiguredLabel() {
            return getClass().getSimpleName();
          }
        }

        @Order(40)
        public class DateOnlyWithFormatField extends AbstractDateField {
          @Override
          protected String getConfiguredLabel() {
            return getClass().getSimpleName();
          }

          @Override
          protected boolean getConfiguredHasTime() {
            return false;
          }

          @Override
          protected String getConfiguredFormat() {
            return "yyyy-MM-dd";
          }
        }

        @Order(50)
        public class DateWithTimeWithFormatField extends AbstractDateField {
          @Override
          protected String getConfiguredLabel() {
            return getClass().getSimpleName();
          }

          @Override
          protected boolean getConfiguredHasTime() {
            return true;
          }

          @Override
          protected String getConfiguredFormat() {
            return "yyyy-MM-dd@HH.mm.ss";
          }
        }

        @Order(60)
        public class TimeOnlyWithFormatField extends AbstractTimeField {
          @Override
          protected String getConfiguredLabel() {
            return getClass().getSimpleName();
          }

          @Override
          protected String getConfiguredFormat() {
            return "HH.mm.ss";
          }
        }
      }

      @Order(999999)
      public class CloseButton extends AbstractCloseButton {
        @Override
        protected String getConfiguredLabel() {
          return "Close";
        }
      }
    }
  }

  private TestForm m_form;
  private Locale m_oldLocale;

  @Before
  public void setUp() throws Throwable {
    m_oldLocale = LocaleThreadLocal.get();
    m_form = new TestForm();
    m_form.startForm();
  }

  @After
  public void tearDown() throws Throwable {
    LocaleThreadLocal.set(m_oldLocale);
    m_form.doClose();
  }

  @Test
  public void testDateOnly() throws Throwable {
    IDateField testField = m_form.getDateOnlyField();
    for (Locale locale : new Locale[]{new Locale("de", "CH"), new Locale("de", "DE")}) {
      LocaleThreadLocal.set(locale);
      expectError(testField, "gugus");
      expectError(testField, "2210");
      expectError(testField, "22.10.");
      expectError(testField, "2017.10.22");
      expectError(testField, "20171022");
      expectSuccess(testField, "22.10.2017", "22.10.2017");
      expectSuccess(testField, "22102017", "22.10.2017");
      expectSuccess(testField, "221017", "22.10.2017");
      expectSuccess(testField, "22,10,2017", "22.10.2017");
    }
    LocaleThreadLocal.set(new Locale("en", "US"));
    expectError(testField, "gugus");
    expectError(testField, "2210");
    expectError(testField, "22.10.");
    expectError(testField, "2017.10.22");
    expectError(testField, "22102017");
    expectError(testField, "10,22,2017");
    expectSuccess(testField, "10/22/2017", "Oct 22, 2017");

  }

  @Test
  public void testDateOnlyWithFormat() throws Throwable {
    IDateField testField = m_form.getDateOnlyWithFormatField();
    for (Locale locale : new Locale[]{new Locale("de", "CH"), new Locale("de", "DE")}) {
      LocaleThreadLocal.set(locale);
      expectError(testField, "gugus");
      expectError(testField, "2210");
      expectError(testField, "22.10.");
      expectError(testField, "2017.10.22");
      expectError(testField, "20171022");
      expectSuccess(testField, "22.10.2017", "2017-10-22");
      expectSuccess(testField, "22102017", "2017-10-22");
      expectSuccess(testField, "221017", "2017-10-22");
      expectSuccess(testField, "22,10,2017", "2017-10-22");
      expectSuccess(testField, "2017-10-22", "2017-10-22");
    }
    LocaleThreadLocal.set(new Locale("en", "US"));
    expectError(testField, "gugus");
    expectError(testField, "2210");
    expectError(testField, "22.10.");
    expectError(testField, "2017.10.22");
    expectError(testField, "22102017");
    expectError(testField, "10,22,2017");
    expectSuccess(testField, "10/22/2017", "2017-10-22");
    expectSuccess(testField, "2017-10-22", "2017-10-22");
  }

  @Test
  public void testDateWithTime() throws Throwable {
    IDateField testField = m_form.getDateWithTimeField();
    for (Locale locale : new Locale[]{new Locale("de", "CH"), new Locale("de", "DE")}) {
      LocaleThreadLocal.set(locale);
      expectError(testField, "gugus");
      expectError(testField, "2210");
      expectError(testField, "22.10.");
      expectError(testField, "2017.10.22");
      expectError(testField, "20171022");
      expectSuccess(testField, "22.10.2017", "22.10.17 00:00");
      expectSuccess(testField, "22102017", "22.10.17 00:00");
      expectSuccess(testField, "221017", "22.10.17 00:00");
      expectSuccess(testField, "22,10,2017", "22.10.17 00:00");
      expectSuccess(testField, "22.10.2017 1314", "22.10.17 13:14");
      expectSuccess(testField, "22.10.2017 13:14", "22.10.17 13:14");
      expectSuccess(testField, "22.10.17 13:14", "22.10.17 13:14");
      expectError(testField, "22102017 1314");
      expectSuccess(testField, "22,10,2017 13:14", "22.10.17 13:14");
    }
    LocaleThreadLocal.set(new Locale("en", "US"));
    expectError(testField, "gugus");
    expectError(testField, "2210");
    expectError(testField, "22.10.");
    expectError(testField, "2017.10.22");
    expectError(testField, "22102017");
    expectError(testField, "10,22,2017");
    expectSuccess(testField, "10/22/2017", "10/22/17 12:00 AM");
    expectSuccess(testField, "10/22/2017 13:14", "10/22/17 1:14 PM");
    expectSuccess(testField, "10/22/2017 1:14 PM", "10/22/17 1:14 PM");
  }

  @Test
  public void testDateWithTimeWithFormat() throws Throwable {
    IDateField testField = m_form.getDateWithTimeWithFormatField();
    for (Locale locale : new Locale[]{new Locale("de", "CH"), new Locale("de", "DE")}) {
      LocaleThreadLocal.set(locale);
      expectError(testField, "gugus");
      expectError(testField, "2210");
      expectError(testField, "22.10.");
      expectError(testField, "2017.10.22");
      expectError(testField, "20171022");
      expectSuccess(testField, "22.10.2017", "2017-10-22@00.00.00");
      expectSuccess(testField, "22102017", "2017-10-22@00.00.00");
      expectSuccess(testField, "221017", "2017-10-22@00.00.00");
      expectSuccess(testField, "22,10,2017", "2017-10-22@00.00.00");
      expectSuccess(testField, "2017-10-22@00.00.00", "2017-10-22@00.00.00");
    }
    LocaleThreadLocal.set(new Locale("en", "US"));
    expectError(testField, "gugus");
    expectError(testField, "2210");
    expectError(testField, "22.10.");
    expectError(testField, "2017.10.22");
    expectError(testField, "22102017");
    expectError(testField, "10,22,2017");
    expectSuccess(testField, "10/22/2017", "2017-10-22@00.00.00");
    expectSuccess(testField, "2017-10-22@00.00.00", "2017-10-22@00.00.00");
  }

  @Test
  public void testTimeOnly() throws Throwable {
    IDateField testField = m_form.getTimeOnlyField();
    for (Locale locale : new Locale[]{new Locale("de", "CH"), new Locale("de", "DE")}) {
      LocaleThreadLocal.set(locale);
      expectError(testField, "gugus");
      expectSuccess(testField, "0930", "09:30");
      expectSuccess(testField, "11", "11:00");
      expectSuccess(testField, "59", "00:59");
      expectSuccess(testField, "730", "07:30");
      expectSuccess(testField, "1015", "10:15");
      expectSuccess(testField, "27", "00:27");
      expectSuccess(testField, "200", "02:00");
      expectSuccess(testField, "2000", "20:00");
      expectSuccess(testField, "201", "02:01");
    }
    LocaleThreadLocal.set(new Locale("en", "US"));
    expectError(testField, "gugus");
    expectSuccess(testField, "0930", "9:30 AM");
    expectSuccess(testField, "11", "11:00 AM");
    expectSuccess(testField, "59", "12:59 AM");
    expectSuccess(testField, "730", "7:30 AM");
    expectSuccess(testField, "1015", "10:15 AM");
    expectSuccess(testField, "200", "2:00 AM");
    expectSuccess(testField, "2000", "8:00 PM");
    expectSuccess(testField, "201", "2:01 AM");
  }

  @Test
  public void testTimeOnlyWithFormat() throws Throwable {
    IDateField testField = m_form.getTimeOnlyWithFormatField();
    for (Locale locale : new Locale[]{new Locale("de", "CH"), new Locale("de", "DE"), new Locale("en", "US")}) {
      LocaleThreadLocal.set(locale);
      expectError(testField, "gugus");
      expectSuccess(testField, "59", "00.59.00");
      expectSuccess(testField, "0930", "09.30.00");
      expectSuccess(testField, "1015", "10.15.00");
      expectSuccess(testField, "200", "02.00.00");
      expectSuccess(testField, "201", "02.01.00");
      expectSuccess(testField, "2000", "20.00.00");
      expectSuccess(testField, "00.00.00", "00.00.00");
      expectSuccess(testField, "00.00.13", "00.00.13");
      expectSuccess(testField, "20.00.00", "20.00.00");
    }
  }

  private void expectSuccess(IDateField field, String input, String expectedDisplayText) {
    field.getUIFacade().setDateTimeTextFromUI(input);
    if (field.getErrorStatus() != null) {
      fail(field.getClass().getSimpleName() + ": Validation error: " + field.getErrorStatus().getMessage());
    }
    assertEquals(field.getClass().getSimpleName() + ": Validation error for input " + input, expectedDisplayText, field.getDisplayText());
    // reset
    field.getUIFacade().setDateTimeTextFromUI(null);
  }

  private void expectError(IDateField field, String input) {
    field.getUIFacade().setDateTimeTextFromUI(input);
    if (field.getErrorStatus() == null) {
      fail(field.getClass().getSimpleName() + ": Validation did _not_ fail for input '" + input + "'! (Display text is: ' " + field.getDisplayText() + "', current locale is: " + Locale.getDefault().toString() + ")");
    }
    // reset
    field.getUIFacade().setDateTimeTextFromUI(null);
  }
}
