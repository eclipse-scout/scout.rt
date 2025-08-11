/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.dataobject.BigDecimalDataObjectValue;
import org.eclipse.scout.rt.dataobject.BooleanDataObjectValue;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper;
import org.eclipse.scout.rt.dataobject.LongDataObjectValue;
import org.eclipse.scout.rt.dataobject.StringDataObjectValue;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JsonDataObjectValuesRawSerializationTest {

  protected static IDataObjectMapper s_dataObjectMapper;

  @BeforeClass
  public static void beforeClass() {
    s_dataObjectMapper = BEANS.get(IPrettyPrintDataObjectMapper.class);
  }

  @Test
  public void testDataObjectValue() {
    testRawDataObjectMapper("\"StringTest\"");
    testRawDataObjectMapper("12345");
    testRawDataObjectMapper("1.2345");
    testRawDataObjectMapper("true");
    testRawDataObjectMapper("false");
  }

  protected IDataObject testRawDataObjectMapper(String json) {
    IDataObject object = s_dataObjectMapper.readValueRaw(json);
    assertType(object);
    return object;
  }

  protected void assertType(IDataObject entity) {
    assertTrue("Expected type IDataObjectValue, was " + entity.getClass(),
        entity.getClass().equals(BigDecimalDataObjectValue.class) ||
            entity.getClass().equals(BooleanDataObjectValue.class) ||
            entity.getClass().equals(LongDataObjectValue.class) ||
            entity.getClass().equals(StringDataObjectValue.class));
  }
}
