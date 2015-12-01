/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.ui.html.ResourceBase;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class DefaultValuesFilterTest {

  protected JSONObject readJsonFile(String filename) {
    String content = IOUtility.getContentUtf8(ResourceBase.class.getResourceAsStream(filename));
    return new JSONObject(JsonUtility.stripCommentsFromJson(content));
  }

  @Test
  public void testDefaultValueFilter_nullSafe() {
    DefaultValuesFilter filter = new DefaultValuesFilter();
    filter.filter(null);
  }

  @Test
  public void testDefaultValueFilter_simple() throws Exception {
    // Load defaults and build filter
    JSONObject jsonDefaultValueConfiguration = readJsonFile("json/DefaultValuesFilterTest_defaults_simple.json");
    DefaultValuesFilter filter = new DefaultValuesFilter();
    filter.importConfiguration(jsonDefaultValueConfiguration);

    // Load test data and apply filter
    JSONObject jsonToFilter = readJsonFile("json/DefaultValuesFilterTest_test_simple.json");
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

    assertEquals(Boolean.FALSE, ((JSONObject) adapterData.get(6)).opt("enabled"));
    JSONObject chartData = ((JSONObject) adapterData.get(6)).optJSONObject("chartData");
    assertEquals(2, chartData.opt("value"));

    assertEquals(Boolean.TRUE, ((JSONObject) adapterData.get(7)).opt("enabled"));
    assertEquals(null, ((JSONObject) adapterData.get(7)).opt("chartData"));

    assertEquals(Boolean.TRUE, ((JSONObject) adapterData.get(8)).opt("enabled"));
    assertEquals("none", ((JSONObject) adapterData.get(8)).opt("chartData"));

    assertEquals(null, ((JSONObject) adapterData.get(9)).opt("enabled"));
    assertEquals(null, ((JSONObject) adapterData.get(9)).opt("chartData"));
    JSONObject axisData = ((JSONObject) adapterData.get(9)).optJSONObject("axisData");
    assertEquals(0, axisData.optJSONObject("xAxis").length());
    JSONObject yAxis = axisData.optJSONObject("yAxis");
    assertEquals("non-default", yAxis.opt("label"));

    JSONArray axisDataArray = ((JSONObject) adapterData.get(10)).optJSONArray("axisData");
    assertEquals(3, axisDataArray.length());
    assertEquals(0, axisDataArray.getJSONObject(0).optJSONObject("xAxis").length());
    yAxis = axisDataArray.getJSONObject(0).optJSONObject("yAxis");
    assertEquals("non-default", yAxis.opt("label"));
    assertEquals(1, axisDataArray.getJSONObject(1).length());
    assertEquals(2, axisDataArray.getJSONObject(2).length());

    JSONObject format = adapterData.getJSONObject(11).getJSONObject("format");
    assertEquals(null, format.opt("lang"));
    assertEquals(3, format.getJSONArray("localeData").length());
    assertEquals(null, format.getJSONArray("localeData").getJSONObject(0).opt("location"));
    assertEquals("here", format.getJSONArray("localeData").getJSONObject(1).opt("location"));
    assertEquals(null, format.getJSONArray("localeData").getJSONObject(2).opt("location"));
  }

  @Test
  public void testDefaultValueFilter_variant() throws Exception {
    // Load defaults and build filter
    JSONObject jsonDefaultValueConfiguration = readJsonFile("json/DefaultValuesFilterTest_defaults_variant.json");
    DefaultValuesFilter filter = new DefaultValuesFilter();
    filter.importConfiguration(jsonDefaultValueConfiguration);

    // Load test data and apply filter
    JSONObject jsonToFilter = readJsonFile("json/DefaultValuesFilterTest_test_variant.json");
    JSONArray adapterData = jsonToFilter.getJSONArray("adapterData");
    for (int i = 0; i < adapterData.length(); i++) {
      filter.filter(adapterData.getJSONObject(i));
    }

    // Checks
    assertEquals(null, ((JSONObject) adapterData.get(0)).opt("enabled"));
    assertEquals(Boolean.TRUE, ((JSONObject) adapterData.get(0)).opt("visible"));
    assertEquals("auto", ((JSONObject) adapterData.get(0)).opt("borderDecoration"));

    assertEquals(null, ((JSONObject) adapterData.get(1)).opt("enabled"));
    assertEquals(Boolean.TRUE, ((JSONObject) adapterData.get(0)).opt("visible"));
    assertEquals("auto", ((JSONObject) adapterData.get(1)).opt("borderDecoration"));

    assertEquals(null, ((JSONObject) adapterData.get(2)).opt("enabled"));
    assertEquals(Boolean.TRUE, ((JSONObject) adapterData.get(0)).opt("visible"));
    assertEquals("auto", ((JSONObject) adapterData.get(2)).opt("borderDecoration"));

    assertEquals(Boolean.TRUE, ((JSONObject) adapterData.get(3)).opt("enabled"));
    assertEquals(Boolean.TRUE, ((JSONObject) adapterData.get(0)).opt("visible"));
    assertEquals(null, ((JSONObject) adapterData.get(3)).opt("borderDecoration"));
  }

  @Test
  public void testDefaultValueFilter_lists() throws Exception {
    // Load defaults and build filter
    JSONObject jsonDefaultValueConfiguration = readJsonFile("json/DefaultValuesFilterTest_defaults_lists.json");
    DefaultValuesFilter filter = new DefaultValuesFilter();
    filter.importConfiguration(jsonDefaultValueConfiguration);

    // Load test data and apply filter
    JSONObject jsonToFilter = readJsonFile("json/DefaultValuesFilterTest_test_lists.json");
    JSONArray adapterData = jsonToFilter.getJSONArray("adapterData");
    for (int i = 0; i < adapterData.length(); i++) {
      filter.filter(adapterData.getJSONObject(i));
    }

    JSONObject menu1 = (JSONObject) adapterData.get(0);
    JSONObject menu2 = (JSONObject) adapterData.get(1);
    JSONObject menu3 = (JSONObject) adapterData.get(2);

    // Checks
    assertEquals(Boolean.FALSE, menu1.opt("visible"));
    assertNull(menu1.opt("enabled"));
    assertNull(menu1.optJSONArray("menuTypes"));

    assertNull(menu2.opt("visible"));
    assertNull(menu2.opt("enabled"));
    assertNull(menu2.optJSONArray("menuTypes"));

    assertNull(menu3.opt("visible"));
    assertNull(menu3.opt("enabled"));
    assertNotNull(menu3.optJSONArray("menuTypes"));
    assertEquals(3, menu3.optJSONArray("menuTypes").length());
  }

  @Test(expected = IllegalStateException.class)
  public void testDefaultValueFilter_illegalHierarchy() throws Exception {
    // Load defaults and build filter
    JSONObject jsonDefaultValueConfiguration = readJsonFile("json/DefaultValuesFilterTest_defaults_illegalHierarchy.json");
    DefaultValuesFilter filter = new DefaultValuesFilter();
    filter.importConfiguration(jsonDefaultValueConfiguration);
  }

  @Test(expected = IllegalStateException.class)
  public void testDefaultValueFilter_loopHierarchy() throws Exception {
    // Load defaults and build filter
    JSONObject jsonDefaultValueConfiguration = readJsonFile("json/DefaultValuesFilterTest_defaults_loopHierarchy.json");
    DefaultValuesFilter filter = new DefaultValuesFilter();
    filter.importConfiguration(jsonDefaultValueConfiguration);
  }
}
