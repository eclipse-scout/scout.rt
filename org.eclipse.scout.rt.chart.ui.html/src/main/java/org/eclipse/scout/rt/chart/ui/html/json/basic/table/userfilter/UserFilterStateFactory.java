/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.chart.ui.html.json.basic.table.userfilter;

import java.util.ArrayList;
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
    if ("CHART".equals(filterType)) {
      ChartTableUserFilterState filterState = new ChartTableUserFilterState();
      filterState.setText(data.getString("text"));
      filterState.setFilters(createArrayValuesFromJson(data, "filters"));
      filterState.setColumnX(extractColumn(table, data, "columnIdX"));
      filterState.setColumnY(extractColumn(table, data, "columnIdY"));
      return filterState;
    }
    return null;
  }

  protected List<Object> createArrayValuesFromJson(JSONObject json, String propName) {
    JSONArray jsonValues = json.optJSONArray(propName);
    if (jsonValues == null) {
      return null;
    }
    List<Object> values = new ArrayList<>();
    for (int i = 0; i < jsonValues.length(); i++) {
      if (jsonValues.isNull(i)) {
        values.add(null);
      }
      else {
        values.add(jsonValues.get(i));
      }
    }
    return values;
  }

  protected IColumn extractColumn(JsonTable<? extends ITable> table, JSONObject json, String propName) {
    String columnId = json.optString(propName, null);
    if (columnId == null) {
      return null;
    }
    return table.optColumn(columnId);
  }

}
