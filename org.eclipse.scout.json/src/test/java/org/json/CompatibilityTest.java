/*******************************************************************************
 * Copyright (C) 2005-2010 The Android Open Source Project
 * Copyright (c) 2015 BSI Business Systems Integration AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     The Android Open Source Project - initial implementation
 *     BSI Business Systems Integration AG - changes and improvements
 ******************************************************************************/
package org.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Tests to ensure compatibility of org.eclipse.scout.json with the original "org.json" implementation.
 */
public class CompatibilityTest {

  @Test
  public void testNullPropertyValue() {
    String input = "{\"test\": null}";
    JSONObject json = new JSONObject(input);

    assertTrue(json.isNull("test"));

    assertEquals(JSONObject.NULL, json.opt("test"));
    assertEquals(JSONObject.NULL, json.get("test"));

    assertEquals(false, json.optBoolean("test"));
    try {
      json.getBoolean("test");
      fail();
    }
    catch (Exception e) {
      // expected
    }

    assertEquals(Double.NaN, json.optDouble("test"), 0.0);
    try {
      json.getDouble("test");
      fail();
    }
    catch (Exception e) {
      // expected
    }

    assertEquals(0, json.optInt("test"));
    try {
      json.getInt("test");
      fail();
    }
    catch (Exception e) {
      // expected
    }

    assertEquals(null, json.optJSONArray("test"));
    try {
      json.getJSONArray("test");
      fail();
    }
    catch (Exception e) {
      // expected
    }

    assertEquals(null, json.optJSONObject("test"));
    try {
      json.getJSONObject("test");
      fail();
    }
    catch (Exception e) {
      // expected
    }

    assertEquals(0, json.optLong("test"));
    try {
      json.getLong("test");
      fail();
    }
    catch (Exception e) {
      // expected
    }

    assertEquals("", json.optString("test"));
    assertEquals(null, json.optString("test", null));
    try {
      json.getString("test");
      fail();
    }
    catch (Exception e) {
      // expected
    }
  }

  @Test
  public void testUndefinedPropertyValue() {
    String input = "{\"myvalue\": null}";
    JSONObject json = new JSONObject(input);

    assertTrue(json.isNull("test"));

    assertEquals(JSONObject.NULL, json.opt("test"));
    try {
      json.get("test");
      fail();
    }
    catch (Exception e) {
      // expected
    }

    assertEquals(false, json.optBoolean("test"));
    try {
      json.getBoolean("test");
      fail();
    }
    catch (Exception e) {
      // expected
    }

    assertEquals(Double.NaN, json.optDouble("test"), 0.0);
    try {
      json.getDouble("test");
      fail();
    }
    catch (Exception e) {
      // expected
    }

    assertEquals(0, json.optInt("test"));
    try {
      json.getInt("test");
      fail();
    }
    catch (Exception e) {
      // expected
    }

    assertEquals(null, json.optJSONArray("test"));
    try {
      json.getJSONArray("test");
      fail();
    }
    catch (Exception e) {
      // expected
    }

    assertEquals(null, json.optJSONObject("test"));
    try {
      json.getJSONObject("test");
      fail();
    }
    catch (Exception e) {
      // expected
    }

    assertEquals(0, json.optLong("test"));
    try {
      json.getLong("test");
      fail();
    }
    catch (Exception e) {
      // expected
    }

    assertEquals("", json.optString("test"));
    assertEquals(null, json.optString("test", null));
    try {
      json.getString("test");
      fail();
    }
    catch (Exception e) {
      // expected
    }
  }
}
