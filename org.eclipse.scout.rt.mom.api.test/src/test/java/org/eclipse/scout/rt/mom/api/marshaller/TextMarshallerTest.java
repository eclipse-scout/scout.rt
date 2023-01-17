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
import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

public class TextMarshallerTest {

  @Test
  public void test() {
    Map<String, String> context = new HashMap<>();

    assertEquals("ABC", BEANS.get(TextMarshaller.class).marshall("ABC", context));
    assertEquals("TestObject.toString()", BEANS.get(TextMarshaller.class).marshall(new TestObject(), context));
    assertEquals("ABC", BEANS.get(TextMarshaller.class).unmarshall("ABC", context));

    byte[] testee = (byte[]) BEANS.get(TextAsBytesMarshaller.class).marshall("ABC", context);
    MatcherAssert.assertThat(testee, is(equalTo(toBytes("ABC"))));

    testee = (byte[]) BEANS.get(TextAsBytesMarshaller.class).marshall(new TestObject(), context);
    MatcherAssert.assertThat(testee, is(equalTo(toBytes("TestObject.toString()"))));
    assertEquals("ABC", BEANS.get(TextAsBytesMarshaller.class).unmarshall(toBytes("ABC"), context));
  }

  @Test
  public void testNull() {
    Map<String, String> context = new HashMap<>();

    assertNull(BEANS.get(TextMarshaller.class).marshall(null, context));
    assertNull(BEANS.get(TextMarshaller.class).unmarshall(null, context));

    assertNull(BEANS.get(TextAsBytesMarshaller.class).marshall(null, context));
    assertNull(BEANS.get(TextAsBytesMarshaller.class).unmarshall(null, context));
  }

  @Test
  public void testEmpty() {
    Map<String, String> context = new HashMap<>();

    assertEquals("", BEANS.get(TextMarshaller.class).marshall("", context));
    assertEquals("", BEANS.get(TextMarshaller.class).unmarshall("", context));

    byte[] testee = (byte[]) BEANS.get(TextAsBytesMarshaller.class).marshall("", context);
    MatcherAssert.assertThat(testee, is(equalTo(toBytes(""))));
    assertEquals("", BEANS.get(TextAsBytesMarshaller.class).unmarshall(toBytes(""), context));
  }

  private static byte[] toBytes(String value) {
    return value.getBytes(StandardCharsets.UTF_8);
  }

  public class TestObject {

    @Override
    public String toString() {
      return "TestObject.toString()";
    }
  }
}
