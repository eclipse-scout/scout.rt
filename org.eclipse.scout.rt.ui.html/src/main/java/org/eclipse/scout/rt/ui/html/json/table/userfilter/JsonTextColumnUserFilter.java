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

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TextColumnUserFilterState;
import org.json.JSONObject;

public class JsonTextColumnUserFilter extends JsonColumnUserFilter<TextColumnUserFilterState> {

  public JsonTextColumnUserFilter(TextColumnUserFilterState filterState) {
    super(filterState);
  }

  @Override
  public String getObjectType() {
    return "TextColumnUserFilter";
  }

  @Override
  public ColumnUserFilterState createFilterStateFromJson(IColumn<?> column, JSONObject json) {
    TextColumnUserFilterState filterState = new TextColumnUserFilterState(column);
    filterState.setSelectedValues(createSelectedValuesFromJson(json));
    filterState.setFreeText(json.optString("freeText"));
    return filterState;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("freeText", getFilterState().getFreeText());
    return json;
  }
}
