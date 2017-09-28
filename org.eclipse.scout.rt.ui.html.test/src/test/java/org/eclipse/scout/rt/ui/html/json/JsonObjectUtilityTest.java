/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class JsonObjectUtilityTest {

  @Test
  public void testJsonWithHtmlCharacters() throws Exception {
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
    assertEquals(null, a.opt("existing"));
    assertEquals(null, a.opt("undefined"));
    // check b
    assertEquals("Test-String", b.opt("test"));
    assertEquals(Boolean.FALSE, b.opt("date"));
    assertEquals(12345, b.opt("value"));
    assertEquals(Boolean.TRUE, b.opt("existing"));
    assertEquals(null, b.opt("undefined"));
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
}
