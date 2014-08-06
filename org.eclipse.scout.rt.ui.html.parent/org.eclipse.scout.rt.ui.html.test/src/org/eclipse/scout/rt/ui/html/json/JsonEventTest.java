/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.json.JSONObject;
import org.junit.Test;

public class JsonEventTest {

  @Test
  public void testCtor_NullData() {
    JsonEvent event = new JsonEvent("foo", "bar", null);
    assertEquals("foo", event.getId());
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
    new JsonEvent("id", null, null);
  }

  @Test
  public void testToJson() throws Exception {
    JSONObject data = JsonObjectUtility.putProperty(new JSONObject(), "myProp", "myValue");
    JsonEvent event = new JsonEvent("foo", "bar", data);
    JSONObject json = event.toJson();
    assertEquals("foo", json.optString("id"));
    assertEquals("bar", json.optString("type"));
    assertEquals("myValue", json.optString("myProp"));
  }

  @Test
  public void testFromJson() throws Exception {
    JSONObject json = new JSONObject();
    JsonObjectUtility.putProperty(json, "id", "foo");
    JsonObjectUtility.putProperty(json, "type", "bar");
    JsonObjectUtility.putProperty(json, "myProp", "myValue");
    JsonEvent event = JsonEvent.fromJson(json);
    assertEquals("foo", event.getId());
    assertEquals("bar", event.getType());
    assertEquals("myValue", event.getData().optString("myProp"));
  }

}
