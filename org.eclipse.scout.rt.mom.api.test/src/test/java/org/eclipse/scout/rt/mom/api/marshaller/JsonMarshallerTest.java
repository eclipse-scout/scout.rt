/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.api.marshaller;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

public class JsonMarshallerTest {

  @Test
  public void test() {
    Map<String, String> context = new HashMap<>();

    assertEquals("ABC", marshallAndUnmarshallAsText("ABC", context));
    assertEquals("ABC", marshallAndUnmarshallAsBytes("ABC", context));

    context = new HashMap<>();
    MatcherAssert.assertThat(marshallAndUnmarshallAsText(new TestObject(), context), instanceOf(TestObject.class));
    MatcherAssert.assertThat(marshallAndUnmarshallAsBytes(new TestObject(), context), instanceOf(TestObject.class));

    context = new HashMap<>();
    TestObject testee = new TestObject();
    MatcherAssert.assertThat(marshallAndUnmarshallAsText(testee, context), is(equalTo(testee)));
    MatcherAssert.assertThat(marshallAndUnmarshallAsBytes(testee, context), is(equalTo(testee)));
  }

  @Test
  public void testEmpty() {
    Map<String, String> context = new HashMap<>();

    String jsonText = (String) BEANS.get(JsonMarshaller.class).marshall("", context);
    assertEquals("", BEANS.get(JsonMarshaller.class).unmarshall(jsonText, context));

    byte[] jsonBytes = (byte[]) BEANS.get(JsonAsBytesMarshaller.class).marshall("", context);
    assertEquals("", BEANS.get(JsonAsBytesMarshaller.class).unmarshall(jsonBytes, context));
  }

  @Test
  public void testNull() {
    Map<String, String> context = new HashMap<>();

    String jsonText = (String) BEANS.get(JsonMarshaller.class).marshall(null, context);
    assertNull(BEANS.get(JsonMarshaller.class).unmarshall(jsonText, context));

    byte[] jsonBytes = (byte[]) BEANS.get(JsonAsBytesMarshaller.class).marshall(null, context);
    assertNull(BEANS.get(JsonAsBytesMarshaller.class).unmarshall(jsonBytes, context));
  }

  private static Object marshallAndUnmarshallAsText(Object object, Map<String, String> context) {
    JsonMarshaller marshaller = BEANS.get(JsonMarshaller.class);
    return marshaller.unmarshall(marshaller.marshall(object, context), context);
  }

  private static Object marshallAndUnmarshallAsBytes(Object object, Map<String, String> context) {
    JsonAsBytesMarshaller marshaller = BEANS.get(JsonAsBytesMarshaller.class);
    return marshaller.unmarshall(marshaller.marshall(object, context), context);
  }

  public static class TestObject {

    public String m_field1;
    public int m_field2;

    public TestObject withField1(String field1) {
      m_field1 = field1;
      return this;
    }

    public TestObject withField2(int field2) {
      m_field2 = field2;
      return this;
    }

    @Override
    public String toString() {
      return "TestObject.toString()";
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }

      if (obj == null) {
        return false;
      }

      if (obj.getClass() != TestObject.class) {
        return false;
      }

      TestObject o = (TestObject) obj;

      return ObjectUtility.equals(o.m_field1, m_field1) && o.m_field2 == m_field2;
    }
  }
}
