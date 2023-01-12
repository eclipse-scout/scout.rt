/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.ui.html.json.basic.table.userfilter;

import static java.util.Arrays.asList;
import static org.eclipse.scout.rt.ui.html.json.JsonObjectUtility.unwrap;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.IUserFilterStateFactory;
import org.json.JSONArray;
import org.json.JSONObject;

@Order(5450)
public class UserFilterStateFactory implements IUserFilterStateFactory {

  @Override
  public IUserFilterState createUserFilterState(JsonTable<? extends ITable> table, JSONObject data) {
    String filterType = data.getString("filterType");
    if (ChartTableUserFilterState.TYPE.equals(filterType)) {
      ChartTableUserFilterState filterState = new ChartTableUserFilterState();
      filterState.setText(data.getString(JsonChartTableUserFilter.ATTRIBUTE_TEXT));
      filterState.setFilters(createArrayValuesFromJson(data, JsonChartTableUserFilter.ATTRIBUTE_FILTERS));
      filterState.setColumnX(extractColumn(table, data, JsonChartTableUserFilter.ATTRIBUTE_COLUMN_ID_X));
      filterState.setColumnY(extractColumn(table, data, JsonChartTableUserFilter.ATTRIBUTE_COLUMN_ID_Y));
      return filterState;
    }
    return null;
  }

  protected List<Object> createArrayValuesFromJson(JSONObject json, String propName) {
    JSONArray jsonArr = json.optJSONArray(propName);
    return asList(unwrap(jsonArr));
  }

  protected IColumn extractColumn(JsonTable<? extends ITable> table, JSONObject json, String propName) {
    String columnId = json.optString(propName, null);
    if (columnId == null) {
      return null;
    }
    return table.optColumn(columnId);
  }
}
