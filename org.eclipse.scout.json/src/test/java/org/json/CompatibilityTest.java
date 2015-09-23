/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
