/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.table.userfilter;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserFilterState;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonColumnUserFilter<T extends ColumnUserFilterState> extends JsonTableUserFilter<T> {

  public JsonColumnUserFilter(T filter) {
    super(filter);
  }

  @Override
  public String getObjectType() {
    return "ColumnUserFilter";
  }

  @Override
  public boolean isValid() {
    return getJsonTable().getColumnId(getFilterState().getColumn()) != null;
  }

  /**
   * This method creates a type specific filter-state model for the given column and JSON data. Sub-classes may
   * implement this method to return a different type. The default impl. returns a {@link ColumnUserFilterState}.
   *
   * @return
   */
  public ColumnUserFilterState createFilterStateFromJson(IColumn<?> column, JSONObject json) {
    ColumnUserFilterState filterState = new ColumnUserFilterState(column);
    filterState.setSelectedValues(createSelectedValuesFromJson(json));
    return filterState;
  }

  protected Set<Object> createSelectedValuesFromJson(JSONObject json) {
    JSONArray jsonSelectedValues = json.getJSONArray("selectedValues");
    Set<Object> selectedValues = new HashSet<>();
    for (int i = 0; i < jsonSelectedValues.length(); i++) {
      if (jsonSelectedValues.isNull(i)) {
        selectedValues.add(null);
      }
      else {
        selectedValues.add(jsonSelectedValues.get(i));
      }
    }
    return selectedValues;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    ColumnUserFilterState filterState = getFilterState();
    IColumn modelColumn = filterState.getColumn();
    json.put("column", getJsonTable().getColumnId(modelColumn));
    json.put("selectedValues", new JSONArray(filterState.getSelectedValues()));
    return json;
  }

  @Override
  public String toString() {
    return getObjectType() + ", " + getFilterState().getColumn();
  }

}
