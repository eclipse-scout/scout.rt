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

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.rt.ui.html.ResourceBase;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class DefaultValuesFilterTest {

  @Test
  public void testDefaultValueFilter_nullSafe() {
    DefaultValuesFilter filter = new DefaultValuesFilter(null, null);
    filter.filter(null);
  }

  @Test
  public void testDefaultValueFilter_simple() throws Exception {
    // Load defaults and build filter
    JSONObject jsonDefaultValueConfiguration = new JSONObject(IOUtility.getContentUtf8(ResourceBase.class.getResourceAsStream("json/DefaultValuesFilterTest_defaults_simple.json")));
    JSONObject jsonDefaults = new JSONObject(jsonDefaultValueConfiguration.optString("defaults"));
    JSONObject jsonObjectTypeHierarchy = new JSONObject(jsonDefaultValueConfiguration.optString("objectTypeHierarchy"));
    DefaultValuesFilter filter = new DefaultValuesFilter(jsonDefaults, jsonObjectTypeHierarchy);

    // Load test data and apply filter
    JSONObject jsonToFilter = new JSONObject(IOUtility.getContentUtf8(ResourceBase.class.getResourceAsStream("json/DefaultValuesFilterTest_test_simple.json")));
    JSONArray adapterData = jsonToFilter.getJSONArray("adapterData");
    for (int i = 0; i < adapterData.length(); i++) {
      filter.filter(adapterData.getJSONObject(i));
    }

    // Checks
    assertEquals(Boolean.FALSE, ((JSONObject) adapterData.get(0)).opt("multiline"));
    assertEquals(null, ((JSONObject) adapterData.get(1)).opt("multiline"));
    assertEquals(null, ((JSONObject) adapterData.get(2)).opt("multiline"));
    assertEquals(Boolean.TRUE, ((JSONObject) adapterData.get(3)).opt("multiline"));

    assertEquals(Boolean.TRUE, ((JSONObject) adapterData.get(0)).opt("enabled"));
    assertEquals(null, ((JSONObject) adapterData.get(1)).opt("enabled"));
    assertEquals(null, ((JSONObject) adapterData.get(2)).opt("enabled"));
    assertEquals(Boolean.FALSE, ((JSONObject) adapterData.get(3)).opt("enabled"));

    // Special case "default object"
    assertEquals(null, ((JSONObject) adapterData.get(0)).optJSONObject("gridData"));
    assertEquals(null, ((JSONObject) adapterData.get(1)).optJSONObject("gridData"));
    JSONObject gd = ((JSONObject) adapterData.get(2)).optJSONObject("gridData");
    assertEquals(10, gd.opt("x"));
    assertEquals(null, gd.opt("y"));
    gd = ((JSONObject) adapterData.get(3)).optJSONObject("gridData");
    assertEquals(null, gd.opt("x"));
    assertEquals(2, gd.opt("y"));

    JSONObject table = (JSONObject) adapterData.get(4);
    assertEquals(Boolean.FALSE, table.opt("enabled"));
    assertEquals(Boolean.FALSE, table.optJSONObject("rows").opt("enabled"));
    JSONArray cells = table.optJSONObject("rows").optJSONArray("cells");
    for (int i = 0; i < cells.length(); i++) {
      filter.filter(cells.getJSONObject(i), "Cell"); // Custom type
    }
    assertEquals(Boolean.TRUE, ((JSONObject) cells.get(0)).opt("editable"));
    assertEquals(null, ((JSONObject) cells.get(1)).opt("editable"));

    assertEquals(Boolean.TRUE, ((JSONObject) adapterData.get(5)).opt("multiline"));
    assertEquals(Boolean.FALSE, ((JSONObject) adapterData.get(5)).opt("enabled"));
  }

  @Test(expected = IllegalStateException.class)
  public void testDefaultValueFilter_illegalHierarchy() throws Exception {
    // Load defaults and build filter
    JSONObject jsonDefaultValueConfiguration = new JSONObject(IOUtility.getContentUtf8(ResourceBase.class.getResourceAsStream("json/DefaultValuesFilterTest_defaults_illegalHierarchy.json")));
    JSONObject jsonDefaults = new JSONObject(jsonDefaultValueConfiguration.optString("defaults"));
    JSONObject jsonObjectTypeHierarchy = new JSONObject(jsonDefaultValueConfiguration.optString("objectTypeHierarchy"));
    new DefaultValuesFilter(jsonDefaults, jsonObjectTypeHierarchy);
  }

  @Test(expected = IllegalStateException.class)
  public void testDefaultValueFilter_loopHierarchy() throws Exception {
    // Load defaults and build filter
    JSONObject jsonDefaultValueConfiguration = new JSONObject(IOUtility.getContentUtf8(ResourceBase.class.getResourceAsStream("json/DefaultValuesFilterTest_defaults_loopHierarchy.json")));
    JSONObject jsonDefaults = new JSONObject(jsonDefaultValueConfiguration.optString("defaults"));
    JSONObject jsonObjectTypeHierarchy = new JSONObject(jsonDefaultValueConfiguration.optString("objectTypeHierarchy"));
    new DefaultValuesFilter(jsonDefaults, jsonObjectTypeHierarchy);
  }
}
