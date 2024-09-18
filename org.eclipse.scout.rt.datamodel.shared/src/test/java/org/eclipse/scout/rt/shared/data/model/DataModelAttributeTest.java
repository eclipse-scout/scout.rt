/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.model;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.eclipse.scout.rt.shared.services.common.code.ICodeRow;
import org.eclipse.scout.rt.shared.services.common.code.MutableCode;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 3.8.0
 */
@SuppressWarnings("deprecation")
@RunWith(PlatformTestRunner.class)
public class DataModelAttributeTest {

  private Locale m_oldLocale;

  @Before
  public void before() {
    m_oldLocale = NlsLocale.getOrElse(null);
  }

  @After
  public void after() {
    NlsLocale.set(m_oldLocale);
  }

  @Test
  public void testFormatAttributeTypeNone() {
    IDataModelAttribute att = new DynamicDataModelAttribute(DataModelConstants.TYPE_NONE);
    assertNull(att.formatValue(null));
    assertEquals("", att.formatValue(""));
    assertEquals("123456", att.formatValue(123456L));
    assertEquals("Hello World!", att.formatValue(new AttributeTestObject()));
  }

  @Test
  public void testFormatAttributeTypeList() {
    IDataModelAttribute att = new DynamicDataModelAttribute(DataModelConstants.TYPE_LIST);
    att.setLookupCall(new AttributeTestLookupCall());
    assertNull(att.formatValue(null));
    assertEquals("a", att.formatValue(1L));
    assertEquals("c", att.formatValue(3L));
  }

  @Test
  public void testFormatAttributeTypeTree() {
    IDataModelAttribute att = new DynamicDataModelAttribute(DataModelConstants.TYPE_TREE);
    att.setLookupCall(new AttributeTestLookupCall());
    assertNull(att.formatValue(null));
    assertEquals("b", att.formatValue(2L));
    assertEquals("d", att.formatValue(4L));
  }

  @Test
  public void testFormatAttributeTypeDate() {
    IDataModelAttribute att = new DynamicDataModelAttribute(DataModelConstants.TYPE_DATE);
    assertNull(att.formatValue(null));

    NlsLocale.set(new Locale("de", "CH"));
    assertEquals("27.04.2012", att.formatValue(DateUtility.parse("27.04.2012 14:03:45", "dd.MM.yyyy HH:mm:ss")));

    NlsLocale.set(new Locale("en", "US"));
    assertEquals("Apr 27, 2012", att.formatValue(DateUtility.parse("27.04.2012 14:03:45", "dd.MM.yyyy HH:mm:ss")));
  }

  @Test
  public void testFormatAttributeTypeTime() {
    IDataModelAttribute att = new DynamicDataModelAttribute(DataModelConstants.TYPE_TIME);
    assertNull(att.formatValue(null));

    NlsLocale.set(new Locale("de", "CH"));
    String formattedDe = att.formatValue(DateUtility.parse("27.04.2012 14:03:45", "dd.MM.yyyy HH:mm:ss"));
    assertEquals("14:03", formattedDe);

    NlsLocale.set(new Locale("en", "US"));
    String formattedEn = att.formatValue(DateUtility.parse("27.04.2012 14:03:45", "dd.MM.yyyy HH:mm:ss"));
    assertTrue("2:03 PM".equals(formattedEn) // JDK < 21 uses normal whitespace
        || "2:03\u202fPM".equals(formattedEn) // JDK >= 21 uses 'NARROW NO-BREAK SPACE' (U+202F)
    );
  }

  @Test
  public void testFormatAttributeTypeDateTime() {
    IDataModelAttribute att = new DynamicDataModelAttribute(DataModelConstants.TYPE_DATE_TIME);
    assertNull(att.formatValue(null));

    NlsLocale.set(new Locale("de", "CH"));
    Date date = DateUtility.parse("27.04.2012 14:03:45", "dd.MM.yyyy HH:mm:ss");
    String expected = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, NlsLocale.get()).format(date);
    assertEquals(expected, att.formatValue(date));

    NlsLocale.set(new Locale("en", "US"));
    expected = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, NlsLocale.get()).format(date);
    assertEquals(expected, att.formatValue(date));
  }

  @Test
  public void testFormatAttributeTypeInteger() {
    IDataModelAttribute att = new DynamicDataModelAttribute(DataModelConstants.TYPE_INTEGER);
    assertNull(att.formatValue(null));

    NlsLocale.set(new Locale("de", "DE"));
    assertEquals("10", att.formatValue(Integer.valueOf(10)));
    assertEquals("-1", att.formatValue(Integer.valueOf(-1)));
    assertEquals("1.000", att.formatValue(Integer.valueOf(1000)));
    assertEquals("-1.000", att.formatValue(Integer.valueOf(-1000)));

    NlsLocale.set(new Locale("en", "US"));
    assertEquals("10", att.formatValue(Integer.valueOf(10)));
    assertEquals("-1", att.formatValue(Integer.valueOf(-1)));
    assertEquals("1,000", att.formatValue(Integer.valueOf(1000)));
    assertEquals("-1,000", att.formatValue(Integer.valueOf(-1000)));
  }

  @Test
  public void testFormatAttributeTypeLong() {
    IDataModelAttribute att = new DynamicDataModelAttribute(DataModelConstants.TYPE_LONG);
    assertNull(att.formatValue(null));

    NlsLocale.set(new Locale("de", "DE"));
    assertEquals("10", att.formatValue(Long.valueOf(10)));
    assertEquals("-1", att.formatValue(Long.valueOf(-1)));
    assertEquals("1.000", att.formatValue(Long.valueOf(1000)));
    assertEquals("-1.000", att.formatValue(Long.valueOf(-1000)));

    NlsLocale.set(new Locale("en", "US"));
    assertEquals("10", att.formatValue(Long.valueOf(10)));
    assertEquals("-1", att.formatValue(Long.valueOf(-1)));
    assertEquals("1,000", att.formatValue(Long.valueOf(1000)));
    assertEquals("-1,000", att.formatValue(Long.valueOf(-1000)));
  }

  @Test
  public void testFormatAttributeTypeBigDecimal() {
    IDataModelAttribute att = new DynamicDataModelAttribute(DataModelConstants.TYPE_BIG_DECIMAL);
    assertNull(att.formatValue(null));

    NlsLocale.set(new Locale("de", "DE"));
    assertEquals("10,00", att.formatValue(BigDecimal.valueOf(10)));
    assertEquals("-1,00", att.formatValue(BigDecimal.valueOf(-1)));
    assertEquals("1.000,00", att.formatValue(BigDecimal.valueOf(1000)));
    assertEquals("-1.000,00", att.formatValue(BigDecimal.valueOf(-1000)));
    assertEquals("1.000,35", att.formatValue(BigDecimal.valueOf(1000.35)));
    assertEquals("-1.000,46", att.formatValue(BigDecimal.valueOf(-1000.46)));
    assertEquals("1.000,50", att.formatValue(BigDecimal.valueOf(1000.495)));
    assertEquals("-1.000,50", att.formatValue(BigDecimal.valueOf(-1000.495)));

    NlsLocale.set(new Locale("en", "US"));
    assertEquals("10.00", att.formatValue(BigDecimal.valueOf(10)));
    assertEquals("-1.00", att.formatValue(BigDecimal.valueOf(-1)));
    assertEquals("1,000.00", att.formatValue(BigDecimal.valueOf(1000)));
    assertEquals("-1,000.00", att.formatValue(BigDecimal.valueOf(-1000)));
    assertEquals("1,000.35", att.formatValue(BigDecimal.valueOf(1000.35)));
    assertEquals("-1,000.46", att.formatValue(BigDecimal.valueOf(-1000.46)));
    assertEquals("1,000.50", att.formatValue(BigDecimal.valueOf(1000.495)));
    assertEquals("-1,000.50", att.formatValue(BigDecimal.valueOf(-1000.495)));

    // same results expected when called with Double values
    NlsLocale.set(new Locale("de", "DE"));
    assertEquals("10,00", att.formatValue(Double.valueOf(10)));
    assertEquals("-1,00", att.formatValue(Double.valueOf(-1)));
    assertEquals("1.000,00", att.formatValue(Double.valueOf(1000)));
    assertEquals("-1.000,00", att.formatValue(Double.valueOf(-1000)));
    assertEquals("1.000,35", att.formatValue(Double.valueOf(1000.35)));
    assertEquals("-1.000,46", att.formatValue(Double.valueOf(-1000.46)));
    assertEquals("1.000,50", att.formatValue(Double.valueOf(1000.495)));
    assertEquals("-1.000,50", att.formatValue(Double.valueOf(-1000.495)));

    NlsLocale.set(new Locale("en", "US"));
    assertEquals("10.00", att.formatValue(Double.valueOf(10)));
    assertEquals("-1.00", att.formatValue(Double.valueOf(-1)));
    assertEquals("1,000.00", att.formatValue(Double.valueOf(1000)));
    assertEquals("-1,000.00", att.formatValue(Double.valueOf(-1000)));
    assertEquals("1,000.35", att.formatValue(Double.valueOf(1000.35)));
    assertEquals("-1,000.46", att.formatValue(Double.valueOf(-1000.46)));
    assertEquals("1,000.50", att.formatValue(Double.valueOf(1000.495)));
    assertEquals("-1,000.50", att.formatValue(Double.valueOf(-1000.495)));

  }

  @Test
  public void testFormatAttributeTypePlainInteger() {
    IDataModelAttribute att = new DynamicDataModelAttribute(DataModelConstants.TYPE_PLAIN_INTEGER);
    assertNull(att.formatValue(null));

    NlsLocale.set(new Locale("de", "CH"));
    assertEquals("10", att.formatValue(Integer.valueOf(10)));
    assertEquals("-1", att.formatValue(Integer.valueOf(-1)));
    assertEquals("1000", att.formatValue(Integer.valueOf(1000)));
    assertEquals("-1000", att.formatValue(Integer.valueOf(-1000)));

    NlsLocale.set(new Locale("de", "DE"));
    assertEquals("10", att.formatValue(Integer.valueOf(10)));
    assertEquals("-1", att.formatValue(Integer.valueOf(-1)));
    assertEquals("1000", att.formatValue(Integer.valueOf(1000)));
    assertEquals("-1000", att.formatValue(Integer.valueOf(-1000)));

    NlsLocale.set(new Locale("en", "US"));
    assertEquals("10", att.formatValue(Integer.valueOf(10)));
    assertEquals("-1", att.formatValue(Integer.valueOf(-1)));
    assertEquals("1000", att.formatValue(Integer.valueOf(1000)));
    assertEquals("-1000", att.formatValue(Integer.valueOf(-1000)));
  }

  @Test
  public void testFormatAttributeTypePlainLong() {
    IDataModelAttribute att = new DynamicDataModelAttribute(DataModelConstants.TYPE_PLAIN_LONG);
    assertNull(att.formatValue(null));

    NlsLocale.set(new Locale("de", "CH"));
    assertEquals("10", att.formatValue(Long.valueOf(10)));
    assertEquals("-1", att.formatValue(Long.valueOf(-1)));
    assertEquals("1000", att.formatValue(Long.valueOf(1000)));
    assertEquals("-1000", att.formatValue(Long.valueOf(-1000)));

    NlsLocale.set(new Locale("de", "DE"));
    assertEquals("10", att.formatValue(Long.valueOf(10)));
    assertEquals("-1", att.formatValue(Long.valueOf(-1)));
    assertEquals("1000", att.formatValue(Long.valueOf(1000)));
    assertEquals("-1000", att.formatValue(Long.valueOf(-1000)));

    NlsLocale.set(new Locale("en", "US"));
    assertEquals("10", att.formatValue(Long.valueOf(10)));
    assertEquals("-1", att.formatValue(Long.valueOf(-1)));
    assertEquals("1000", att.formatValue(Long.valueOf(1000)));
    assertEquals("-1000", att.formatValue(Long.valueOf(-1000)));
  }

  @Test
  public void testFormatAttributeTypePlainBigDecimal() {
    IDataModelAttribute att = new DynamicDataModelAttribute(DataModelConstants.TYPE_PLAIN_BIG_DECIMAL);
    assertNull(att.formatValue(null));

    NlsLocale.set(new Locale("de", "CH"));
    assertEquals("10.00", att.formatValue(BigDecimal.valueOf(10)));
    assertEquals("-1.00", att.formatValue(BigDecimal.valueOf(-1)));
    assertEquals("1000.00", att.formatValue(BigDecimal.valueOf(1000)));
    assertEquals("-1000.00", att.formatValue(BigDecimal.valueOf(-1000)));
    assertEquals("1000.35", att.formatValue(BigDecimal.valueOf(1000.35)));
    assertEquals("-1000.46", att.formatValue(BigDecimal.valueOf(-1000.46)));
    assertEquals("1000.50", att.formatValue(BigDecimal.valueOf(1000.495)));
    assertEquals("-1000.50", att.formatValue(BigDecimal.valueOf(-1000.495)));

    NlsLocale.set(new Locale("de", "DE"));
    assertEquals("10,00", att.formatValue(BigDecimal.valueOf(10)));
    assertEquals("-1,00", att.formatValue(BigDecimal.valueOf(-1)));
    assertEquals("1000,00", att.formatValue(BigDecimal.valueOf(1000)));
    assertEquals("-1000,00", att.formatValue(BigDecimal.valueOf(-1000)));
    assertEquals("1000,35", att.formatValue(BigDecimal.valueOf(1000.35)));
    assertEquals("-1000,46", att.formatValue(BigDecimal.valueOf(-1000.46)));
    assertEquals("1000,50", att.formatValue(BigDecimal.valueOf(1000.495)));
    assertEquals("-1000,50", att.formatValue(BigDecimal.valueOf(-1000.495)));

    NlsLocale.set(new Locale("en", "US"));
    assertEquals("10.00", att.formatValue(BigDecimal.valueOf(10)));
    assertEquals("-1.00", att.formatValue(BigDecimal.valueOf(-1)));
    assertEquals("1000.00", att.formatValue(BigDecimal.valueOf(1000)));
    assertEquals("-1000.00", att.formatValue(BigDecimal.valueOf(-1000)));
    assertEquals("1000.35", att.formatValue(BigDecimal.valueOf(1000.35)));
    assertEquals("-1000.46", att.formatValue(BigDecimal.valueOf(-1000.46)));
    assertEquals("1000.50", att.formatValue(BigDecimal.valueOf(1000.495)));
    assertEquals("-1000.50", att.formatValue(BigDecimal.valueOf(-1000.495)));

    // same results expected when called with Double values
    NlsLocale.set(new Locale("de", "CH"));
    assertEquals("10.00", att.formatValue(Double.valueOf(10)));
    assertEquals("-1.00", att.formatValue(Double.valueOf(-1)));
    assertEquals("1000.00", att.formatValue(Double.valueOf(1000)));
    assertEquals("-1000.00", att.formatValue(Double.valueOf(-1000)));
    assertEquals("1000.35", att.formatValue(Double.valueOf(1000.35)));
    assertEquals("-1000.46", att.formatValue(Double.valueOf(-1000.46)));
    assertEquals("1000.50", att.formatValue(Double.valueOf(1000.495)));
    assertEquals("-1000.50", att.formatValue(Double.valueOf(-1000.495)));

    NlsLocale.set(new Locale("de", "DE"));
    assertEquals("10,00", att.formatValue(Double.valueOf(10)));
    assertEquals("-1,00", att.formatValue(Double.valueOf(-1)));
    assertEquals("1000,00", att.formatValue(Double.valueOf(1000)));
    assertEquals("-1000,00", att.formatValue(Double.valueOf(-1000)));
    assertEquals("1000,35", att.formatValue(Double.valueOf(1000.35)));
    assertEquals("-1000,46", att.formatValue(Double.valueOf(-1000.46)));
    assertEquals("1000,50", att.formatValue(Double.valueOf(1000.495)));
    assertEquals("-1000,50", att.formatValue(Double.valueOf(-1000.495)));

    NlsLocale.set(new Locale("en", "US"));
    assertEquals("10.00", att.formatValue(Double.valueOf(10)));
    assertEquals("-1.00", att.formatValue(Double.valueOf(-1)));
    assertEquals("1000.00", att.formatValue(Double.valueOf(1000)));
    assertEquals("-1000.00", att.formatValue(Double.valueOf(-1000)));
    assertEquals("1000.35", att.formatValue(Double.valueOf(1000.35)));
    assertEquals("-1000.46", att.formatValue(Double.valueOf(-1000.46)));
    assertEquals("1000.50", att.formatValue(Double.valueOf(1000.495)));
    assertEquals("-1000.50", att.formatValue(Double.valueOf(-1000.495)));
  }

  @Test
  public void testFormatAttributeTypePercent() {
    IDataModelAttribute att = new DynamicDataModelAttribute(DataModelConstants.TYPE_PERCENT);
    assertNull(att.formatValue(null));

    NlsLocale.set(new Locale("en", "US"));
    assertEquals("10.00%", att.formatValue(Double.valueOf(10)));
    assertEquals("35.46%", att.formatValue(Double.valueOf(35.456)));
    assertEquals("-36.00%", att.formatValue(Double.valueOf(-36)));
    assertEquals("120.00%", att.formatValue(Double.valueOf(120)));
  }

  @Test
  public void testFormatAttributeTypeString() {
    IDataModelAttribute att = new DynamicDataModelAttribute(DataModelConstants.TYPE_STRING);
    assertNull(att.formatValue(null));
    assertEquals("Test", att.formatValue("Test"));
    assertEquals("   ", att.formatValue("   "));
  }

  @Test
  public void testFormatAttributeTypeSmart() {
    IDataModelAttribute att = new DynamicDataModelAttribute(DataModelConstants.TYPE_SMART);
    att.setCodeTypeClass(AttributeTestCodeType.class);
    assertNull(att.formatValue(null));
    assertEquals("First", att.formatValue(1L));
    assertEquals("Second", att.formatValue(2L));

    att = new DynamicDataModelAttribute(DataModelConstants.TYPE_LIST);
    att.setLookupCall(new AttributeTestLookupCall());
    assertNull(att.formatValue(null));
    assertEquals("a", att.formatValue(1L));
    assertEquals("c", att.formatValue(3L));

    // code type wins if both code type class and lookup call are set
    att.setCodeTypeClass(AttributeTestCodeType.class);
    assertEquals("First", att.formatValue(1L));
    assertEquals("Second", att.formatValue(2L));
  }

  @Test
  public void testDataModelAttributeSerializable() throws Exception {
    File tmpFile = IOUtility.createTempFile("DynamicDataModelAttribute", "tmp", null);
    IDataModelAttribute dataModelAttribute = new DynamicDataModelAttribute(DataModelConstants.TYPE_NONE);
    writeObjectToFile(dataModelAttribute, tmpFile);
    Object readObject = readObjectFromFile(tmpFile);
    //noinspection ResultOfMethodCallIgnored
    tmpFile.delete();
    assertTrue(readObject instanceof IDataModelAttribute);
    assertEquals(((IDataModelAttribute) readObject).getType(), DataModelConstants.TYPE_NONE);
  }

  private void writeObjectToFile(Object input, File file) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(file)) {
      SerializationUtility.createObjectSerializer().serialize(fos, input);
    }
  }

  private Object readObjectFromFile(File file) throws IOException, ClassNotFoundException {
    try (FileInputStream fis = new FileInputStream(file)) {
      return SerializationUtility.createObjectSerializer().deserialize(fis, Object.class);
    }
  }

  public static class DynamicDataModelAttribute extends AbstractDataModelAttribute {
    private static final long serialVersionUID = 1L;

    public DynamicDataModelAttribute(int type) {
      super();
      setType(type);
    }
  }

  public static class AttributeTestLookupCall extends LocalLookupCall<Long> {
    private static final long serialVersionUID = 1L;

    @Override
    protected List<ILookupRow<Long>> execCreateLookupRows() {
      List<ILookupRow<Long>> result = new ArrayList<>();
      result.add(new LookupRow<>(1L, "a"));
      result.add(new LookupRow<>(2L, "b"));
      result.add(new LookupRow<>(3L, "c"));
      result.add(new LookupRow<>(4L, "d"));
      return result;
    }
  }

  public static class AttributeTestCodeType extends AbstractCodeType<Long, Long> {
    private static final long serialVersionUID = 1L;

    @Override
    public Long getId() {
      return 42L;
    }

    @Override
    protected AbstractCode<Long> execCreateCode(ICodeRow<Long> newRow) {
      return new MutableCode<>(newRow);
    }

    @Order(10)
    public class FirstCode extends AbstractCode<Long> {
      private static final long serialVersionUID = 1L;

      @Override
      public Long getId() {
        return 1L;
      }

      @Override
      public String getText() {
        return "First";
      }
    }

    @Order(20)
    public class SecondCode extends AbstractCode<Long> {
      private static final long serialVersionUID = 1L;

      @Override
      public Long getId() {
        return 2L;
      }

      @Override
      public String getText() {
        return "Second";
      }
    }
  }

  public static class AttributeTestObject {
    @Override
    public String toString() {
      return "Hello World!";
    }
  }
}
