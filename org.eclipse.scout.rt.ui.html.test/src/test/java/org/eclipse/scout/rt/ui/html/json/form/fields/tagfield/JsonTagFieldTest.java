/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.tagfield;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.form.fields.tagfield.ITagField;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.json.JSONArray;
import org.junit.Test;
import org.mockito.Mockito;

public class JsonTagFieldTest {

  /**
   * Verifies the jsonToValue returns a sorted set (retain order of values in JSON array).
   */
  @Test
  public void testJsonToValue_Sorted() {
    JSONArray jsonArray = new JSONArray();
    jsonArray.put("000");
    jsonArray.put("bar");
    jsonArray.put("foo");
    jsonArray.put("baz");
    jsonArray.put("999");
    var jsonTagField = new JsonTagField(Mockito.mock(ITagField.class), new UiSessionMock(), null, null);
    Set<String> valueSet = jsonTagField.jsonToValue(jsonArray);
    List<String> valueList = new ArrayList<>(valueSet);
    for (int i = 0; i < valueList.size(); i++) {
      assertEquals(jsonArray.get(i), valueList.get(i));
    }
  }
}
