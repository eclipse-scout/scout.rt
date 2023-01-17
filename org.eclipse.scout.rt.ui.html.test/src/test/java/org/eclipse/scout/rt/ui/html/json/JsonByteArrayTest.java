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

import static org.junit.Assert.assertNull;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;

public class JsonByteArrayTest {

  @Test
  public void testConversion() {
    assertNull(new JsonByteArray((byte[]) null).getBytes());

    byte[] bytes = new byte[0];
    Assert.assertArrayEquals(bytes, new JsonByteArray(bytes).getBytes());

    bytes = new byte[]{0, 1, 2, 3};
    Assert.assertArrayEquals(bytes, new JsonByteArray(bytes).getBytes());
  }

  @Test
  public void testJsonObjectUtility() {
    JSONArray jsonArray = new JSONArray();
    jsonArray.put(new JsonByteArray((byte[]) null).toJson());
    byte[] bytes1 = new byte[0];
    jsonArray.put(new JsonByteArray(bytes1).toJson());
    byte[] bytes2 = new byte[]{0, 1, 2, 3};
    jsonArray.put(new JsonByteArray(bytes2).toJson());

    assertNull(JsonObjectUtility.jsonArrayElementToJava(jsonArray, 0, byte[].class, true));
    Assert.assertArrayEquals(bytes1, JsonObjectUtility.jsonArrayElementToJava(jsonArray, 1, byte[].class, true));
    Assert.assertArrayEquals(bytes2, JsonObjectUtility.jsonArrayElementToJava(jsonArray, 2, byte[].class, true));
  }
}
