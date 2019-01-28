/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.dataobject.value;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.scout.rt.jackson.testing.DataObjectSerializationTestHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.platform.dataobject.IPrettyPrintDataObjectMapper;
import org.eclipse.scout.rt.platform.dataobject.value.BigDecimalValueDo;
import org.eclipse.scout.rt.platform.dataobject.value.BooleanValueDo;
import org.eclipse.scout.rt.platform.dataobject.value.DateTimeValueDo;
import org.eclipse.scout.rt.platform.dataobject.value.DateValueDo;
import org.eclipse.scout.rt.platform.dataobject.value.IValueDo;
import org.eclipse.scout.rt.platform.dataobject.value.StringValueDo;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Various low-level tests for {@link IValueDo}
 */
public class IValueDoTest {

  protected static Date s_testDate;
  protected static String s_testString;
  protected static BigDecimal s_testNumber;

  protected static DataObjectSerializationTestHelper s_testHelper;
  protected static IDataObjectMapper s_dataObjectMapper;

  @BeforeClass
  public static void before() {
    s_testHelper = BEANS.get(DataObjectSerializationTestHelper.class);
    s_dataObjectMapper = BEANS.get(IPrettyPrintDataObjectMapper.class);

    Calendar c = Calendar.getInstance();
    c.clear();
    c.set(2018, 7, 23, 13, 30, 25);
    s_testDate = c.getTime();
    s_testString = "aaabbbccc";
    s_testNumber = new BigDecimal("0.1");
  }

  @Test
  public void testTypedValueDoSerialization() {
    ValueDoFixtureDo fixture = new ValueDoFixtureDo();
    fixture.withDate(s_testDate);
    fixture.withDateTime(s_testDate);
    fixture.withTypedDate(DateValueDo.of(s_testDate));
    fixture.withTypedDateTime(DateTimeValueDo.of(s_testDate));
    fixture.withPartlyTypedDate(DateTimeValueDo.of(s_testDate));
    fixture.withTypedList(DateValueDo.of(s_testDate), DateTimeValueDo.of(s_testDate), StringValueDo.of(s_testString), BooleanValueDo.of(Boolean.TRUE), BigDecimalValueDo.of(s_testNumber));
    fixture.put("dynamicDate", s_testDate);
    fixture.put("dynamicTypedDate", DateValueDo.of(s_testDate));
    fixture.put("dynamicTypedDateTime", DateTimeValueDo.of(s_testDate));

    String json = s_dataObjectMapper.writeValue(fixture);
    assertJsonEquals("TestValueDoFixture.json", json);
  }

  @Test
  public void testTypedValueDoDeserialization() throws Exception {
    String json = readResourceAsString("TestValueDoFixture.json");

    Date dateWithTime = s_testDate;
    Calendar c = Calendar.getInstance();
    c.setTime(dateWithTime);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    Date dateOnly = c.getTime();

    ValueDoFixtureDo fixture = s_dataObjectMapper.readValue(json, ValueDoFixtureDo.class);
    assertEquals(dateOnly, fixture.getDate());
    assertEquals(dateWithTime, fixture.getDateTime());
    assertEquals(dateOnly, fixture.getTypedDate().getValue());
    assertEquals(dateWithTime, fixture.getTypedDateTime().getValue());
    assertEquals(dateWithTime, fixture.getPartlyTypedDate().unwrap());
    assertEquals(5, fixture.getTypedList().size());
    assertEquals(dateOnly, fixture.getTypedList().get(0).unwrap());
    assertEquals(dateWithTime, fixture.getTypedList().get(1).unwrap());
    assertEquals(s_testString, fixture.getTypedList().get(2).unwrap());
    assertEquals(Boolean.TRUE, fixture.getTypedList().get(3).unwrap());
    assertEquals(s_testNumber, fixture.getTypedList().get(4).unwrap());
  }

  protected void assertJsonEquals(String expectedResourceName, String actual) {
    s_testHelper.assertJsonEquals(getResource(expectedResourceName), actual);
  }

  protected String readResourceAsString(String resourceName) throws IOException {
    return s_testHelper.readResourceAsString(getResource(resourceName));
  }

  protected URL getResource(String expectedResourceName) {
    return getClass().getResource(expectedResourceName);
  }
}
