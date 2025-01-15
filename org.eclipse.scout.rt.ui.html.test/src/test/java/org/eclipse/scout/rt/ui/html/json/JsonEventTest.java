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

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Test;

public class JsonEventTest {

  @Test
  public void testCtor_NullData() {
    JsonEvent event = new JsonEvent("foo", "bar", null);
    assertEquals("foo", event.getTarget());
    assertEquals("bar", event.getType());
    assertNotNull(event.getData());
  }

  @Test
  public void testCtor_Data() {
    JSONObject data = new JSONObject();
    JsonEvent event = new JsonEvent("foo", "bar", data);
    assertSame(data, event.getData());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCtor_MandatoryParamId() {
    new JsonEvent(null, "type", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCtor_MandatoryParamType() {
    new JsonEvent("target", null, null);
  }

  @Test
  public void testToJson() {
    JSONObject data = new JSONObject().put("myProp", "myValue");
    JsonEvent event = new JsonEvent("foo", "bar", data);
    JSONObject json = event.toJson();
    assertEquals("foo", json.getString("target"));
    assertEquals("bar", json.getString("type"));
    assertEquals("myValue", json.getString("myProp"));
  }

  @Test
  public void testFromJson() {
    JSONObject json = new JSONObject();
    json.put("target", "foo");
    json.put("type", "bar");
    json.put("myProp", "myValue");
    JsonEvent event = JsonEvent.fromJson(json);
    assertEquals("foo", event.getTarget());
    assertEquals("bar", event.getType());
    assertEquals("myValue", event.getData().getString("myProp"));
  }
}
