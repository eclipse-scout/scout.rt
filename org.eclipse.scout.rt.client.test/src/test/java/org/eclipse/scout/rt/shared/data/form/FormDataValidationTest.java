/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.form;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests if hacking attack can compromise form field data values (invalid type and range) see {@link AbstractFormData}
 */
@SuppressWarnings("unchecked")
@RunWith(PlatformTestRunner.class)
public class FormDataValidationTest {

  @Test
  public void testValueFields() throws Exception {
    assertIOSuccess("StringValue", null);
    assertIOSuccess("StringValue", "ABC");
    assertIOFailure("StringValue", 1234L);
    //
    assertIOSuccess("IntegerValue", null);
    assertIOSuccess("IntegerValue", 12345);
    assertIOFailure("IntegerValue", "abcd");
    assertIOFailure("IntegerValue", 1234L);
    //
    assertIOSuccess("LongValue", null);
    assertIOSuccess("LongValue", 123456L);
    assertIOFailure("LongValue", "abcd");
    assertIOFailure("LongValue", 1234);
    //
    assertIOSuccess("DoubleValue", null);
    assertIOSuccess("DoubleValue", 1.234);
    assertIOFailure("DoubleValue", "abcd");
    assertIOFailure("DoubleValue", 1234);
    //
    assertIOSuccess("BigDecimalValue", null);
    assertIOSuccess("BigDecimalValue", BigDecimal.valueOf(123.456));
    assertIOFailure("BigDecimalValue", "abcd");
    assertIOFailure("BigDecimalValue", 1234.56);
    //
    assertIOSuccess("BooleanValue", null);
    assertIOSuccess("BooleanValue", true);
    assertIOFailure("BooleanValue", "true");
    //
    assertIOSuccess("DateValue", null);
    assertIOSuccess("DateValue", new Date());
    assertIOSuccess("DateValue", new java.sql.Date(System.currentTimeMillis()));
    assertIOFailure("DateValue", "abcd");
    assertIOFailure("DateValue", new GregorianCalendar());
    //
    assertIOSuccess("CharArrayValue", null);
    assertIOSuccess("CharArrayValue", "ABC".toCharArray());
    assertIOFailure("CharArrayValue", new byte[]{0, 1, 2, 3});
    //
    assertIOSuccess("IntArrayValue", null);
    assertIOSuccess("IntArrayValue", new int[]{0, 1, 2, 3});
    assertIOFailure("IntArrayValue", new char[]{0, 1, 2, 3});
    //
    assertIOSuccess("LongArrayValue", null);
    assertIOSuccess("LongArrayValue", new Long[]{1L, 2L, 3L});
    assertIOFailure("LongArrayValue", new Object[]{1L, 2L, 3L});
  }

  private static void assertIOSuccess(String fieldId, Object expectedValue) throws Exception {
    MyFormData d1 = new MyFormData();
    AbstractValueFieldData<Object> v1 = (AbstractValueFieldData<Object>) d1.getFieldById(fieldId);
    v1.setValue(expectedValue);
    byte[] serial = SerializationUtility.createObjectSerializer().serialize(d1);
    //
    MyFormData d2 = SerializationUtility.createObjectSerializer().deserialize(serial, MyFormData.class);
    AbstractValueFieldData<Object> v2 = (AbstractValueFieldData<Object>) d2.getFieldById(fieldId);
    if (expectedValue != null && expectedValue.getClass().isArray()) {
      //nop
    }
    else {
      assertEquals(expectedValue, v2.getValue());
    }
  }

  private static void assertIOFailure(String fieldId, Object expectedValue) throws Exception {
    MyFormData d1 = new MyFormData();
    AbstractValueFieldData<Object> v1 = (AbstractValueFieldData<Object>) d1.getFieldById(fieldId);
    v1.setValue(expectedValue);
    byte[] serial = SerializationUtility.createObjectSerializer().serialize(d1);
    //
    try {
      SerializationUtility.createObjectSerializer().deserialize(serial, MyFormData.class);
    }
    catch (SecurityException e) {
      //ok
      return;
    }
    fail("should have failed");
  }

  static class MyFormData extends AbstractFormData {
    private static final long serialVersionUID = 1L;

    public MyFormData() {
    }

    public class StringValue extends AbstractValueFieldData<String> {
      private static final long serialVersionUID = 1L;
    }

    public class IntegerValue extends AbstractValueFieldData<Integer> {
      private static final long serialVersionUID = 1L;
    }

    public class LongValue extends AbstractValueFieldData<Long> {
      private static final long serialVersionUID = 1L;
    }

    public class DoubleValue extends AbstractValueFieldData<Double> {
      private static final long serialVersionUID = 1L;
    }

    public class BigDecimalValue extends AbstractValueFieldData<BigDecimal> {
      private static final long serialVersionUID = 1L;
    }

    public class BooleanValue extends AbstractValueFieldData<Boolean> {
      private static final long serialVersionUID = 1L;
    }

    public class DateValue extends AbstractValueFieldData<Date> {
      private static final long serialVersionUID = 1L;
    }

    public class CharArrayValue extends AbstractValueFieldData<char[]> {
      private static final long serialVersionUID = 1L;
    }

    public class IntArrayValue extends AbstractValueFieldData<int[]> {
      private static final long serialVersionUID = 1L;
    }

    public class LongArrayValue extends AbstractValueFieldData<Long[]> {
      private static final long serialVersionUID = 1L;
    }
  }

}
