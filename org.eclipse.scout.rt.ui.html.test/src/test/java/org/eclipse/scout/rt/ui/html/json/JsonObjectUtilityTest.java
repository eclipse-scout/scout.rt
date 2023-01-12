/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import static java.util.Collections.singletonMap;
import static org.eclipse.scout.rt.ui.html.json.JsonObjectUtility.unwrap;
import static org.junit.Assert.*;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class JsonObjectUtilityTest {

  @Test
  public void testJsonWithHtmlCharacters() {
    assertEquals("foo", toJson("foo"));
    assertEquals("<foo>", toJson("<foo>"));
    assertEquals("foo & bar", toJson("foo & bar"));
    assertEquals("<!-- foo -->", toJson("<!-- foo -->"));
    assertEquals("foo < bar", toJson("foo < bar"));
  }

  private String toJson(String s) {
    return MainJsonObjectFactory.get().createJsonObject(s).toJson().toString();
  }

  @Test
  public void testUnwrap() {
    String input = "abc";
    assertNull(unwrap((Object) null));
    assertNull(unwrap(JSONObject.NULL));
    assertSame(input, unwrap(input));

    Map<String, Object> expanded = unwrap(new JSONObject("{\"a\": \"1\", \"b\": [{\"c\":\"2\"}, {\"d\": \"3\"}]}"));
    assertEquals(2, expanded.size());
    assertEquals("1", expanded.get("a"));
    Object[] second = (Object[]) expanded.get("b");
    assertArrayEquals(second, new Object[]{singletonMap("c", "2"), singletonMap("d", "3")});
  }

  @Test
  public void testUnwrapWithNullValues() {
    Map<String, Object> expanded = unwrap(new JSONObject("{\"a\": null, \"b\": [null,, {\"d\": \"3\"}]}"));
    assertEquals(2, expanded.size());
    assertNull(expanded.get("1"));
    Object[] secondArr = (Object[]) expanded.get("b");
    assertArrayEquals(new Object[]{null, null, singletonMap("d", "3")}, secondArr);
  }

  @Test
  public void testPutProperties() {
    JsonObjectUtility.putProperties(null, null); // should not throw exception
    JsonObjectUtility.putProperties(new JSONObject(), null); // should not throw exception
    JsonObjectUtility.putProperties(null, new JSONObject()); // should not throw exception

    JSONObject a = new JSONObject();
    a.put("test", "Test-String");
    a.put("date", false);
    a.put("value", 12345);

    JSONObject b = new JSONObject();
    b.put("existing", true);
    b.put("test", false);

    JsonObjectUtility.putProperties(b, a);

    // check a
    assertEquals("Test-String", a.opt("test"));
    assertEquals(Boolean.FALSE, a.opt("date"));
    assertEquals(12345, a.opt("value"));
    assertNull(a.opt("existing"));
    assertNull(a.opt("undefined"));
    // check b
    assertEquals("Test-String", b.opt("test"));
    assertEquals(Boolean.FALSE, b.opt("date"));
    assertEquals(12345, b.opt("value"));
    assertEquals(Boolean.TRUE, b.opt("existing"));
    assertNull(b.opt("undefined"));
  }

  @Test
  public void testOptLong() {
    JSONObject json = new JSONObject();
    json.put("test", "Test-String");
    json.put("value", 12345);
    json.put("zero", 0);
    json.put("neg", -1);
    json.put("fracValue", 1.9);
    json.put("existing", true);

    // should not throw exception
    assertNull(JsonObjectUtility.optLong(null, null));
    assertNull(JsonObjectUtility.optLong(json, null));
    assertNull(JsonObjectUtility.optLong(null, "test"));

    assertNull(JsonObjectUtility.optLong(json, "test"));
    assertNull(JsonObjectUtility.optLong(json, "xyz"));
    assertNull(JsonObjectUtility.optLong(json, "existing"));
    assertEquals((Long) 12345L, JsonObjectUtility.optLong(json, "value"));
    assertEquals((Long) 0L, JsonObjectUtility.optLong(json, "zero"));
    assertEquals((Long) (-1L), JsonObjectUtility.optLong(json, "neg"));
    assertEquals((Long) 1L, JsonObjectUtility.optLong(json, "fracValue")); // cut off by cast
  }

  @Test
  public void testOptDouble() {
    JSONObject json = new JSONObject();
    json.put("test", "Test-String");
    json.put("value", 12345);
    json.put("zero", 0);
    json.put("neg", -1);
    json.put("fracValue", 1.9);
    json.put("existing", true);

    // should not throw exception
    assertNull(JsonObjectUtility.optDouble(null, null));
    assertNull(JsonObjectUtility.optDouble(json, null));
    assertNull(JsonObjectUtility.optDouble(null, "test"));

    assertNull(JsonObjectUtility.optDouble(json, "test"));
    assertNull(JsonObjectUtility.optDouble(json, "xyz"));
    assertNull(JsonObjectUtility.optDouble(json, "existing"));
    assertEquals(12345.0, JsonObjectUtility.optDouble(json, "value"), 0.0);
    assertEquals(0.0, JsonObjectUtility.optDouble(json, "zero"), 0.0);
    assertEquals(-1.0, JsonObjectUtility.optDouble(json, "neg"), 0.0);
    assertEquals(1.9, JsonObjectUtility.optDouble(json, "fracValue"), 0.0);
  }

  @Test
  public void testOptInt() {
    JSONObject json = new JSONObject();
    json.put("test", "Test-String");
    json.put("value", 12345);
    json.put("zero", 0);
    json.put("neg", -1);
    json.put("fracValue", 1.9);
    json.put("existing", true);

    // should not throw exception
    assertNull(JsonObjectUtility.optInt(null, null));
    assertNull(JsonObjectUtility.optInt(json, null));
    assertNull(JsonObjectUtility.optInt(null, "test"));

    assertNull(JsonObjectUtility.optInt(json, "test"));
    assertNull(JsonObjectUtility.optInt(json, "xyz"));
    assertNull(JsonObjectUtility.optInt(json, "existing"));
    assertEquals((Integer) 12345, JsonObjectUtility.optInt(json, "value"));
    assertEquals((Integer) 0, JsonObjectUtility.optInt(json, "zero"));
    assertEquals((Integer) (-1), JsonObjectUtility.optInt(json, "neg"));
    assertEquals((Integer) 1, JsonObjectUtility.optInt(json, "fracValue")); // cut off by cast
  }

  @Test
  public void testPutIfNotNull() {
    // Default behavior
    JSONArray array = new JSONArray();
    array.put("element");
    array.put(null);
    array.put(JSONObject.NULL);
    array.put(123);
    assertEquals(4, array.length());
    assertEquals("[\"element\",null,null,123]", array.toString());

    // "putIfNotNull" behavior
    JSONArray array2 = new JSONArray();
    JsonObjectUtility.putIfNotNull(array2, "element");
    JsonObjectUtility.putIfNotNull(array2, null);
    JsonObjectUtility.putIfNotNull(array2, JSONObject.NULL);
    JsonObjectUtility.putIfNotNull(array2, 123);
    assertEquals(3, array2.length());
    assertEquals("[\"element\",null,123]", array2.toString());
  }

  @Test
  public void testBeanWithOptionalJsonProperty() {
    JSONArray a = new JSONArray("[{\"filename\": \"foo\", \"content\":\"\"}]");// intentionally not setting 'available'
    FixtureDataBean b = JsonObjectUtility.jsonArrayElementToJava(a, 0, FixtureDataBean.class, false);
    assertEquals("foo", b.getFilename());
    assertArrayEquals(new byte[0], b.getContent());
    assertFalse(b.isAvailable());
  }

  public static class FixtureDataBean {
    private String m_filename;
    private byte[] m_content;
    private boolean m_available;

    public String getFilename() {
      return m_filename;
    }

    public void setFilename(String filename) {
      m_filename = filename;
    }

    public byte[] getContent() {
      return m_content;
    }

    public void setContent(byte[] content) {
      m_content = content;
    }

    public boolean isAvailable() {
      return m_available;
    }

    public void setAvailable(boolean b) {
      m_available = b;
    }
  }
}
