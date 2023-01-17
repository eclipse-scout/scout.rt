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

import java.util.Date;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.DateColumnUserFilterState;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.json.JSONObject;

public class JsonDateColumnUserFilter extends JsonColumnUserFilter<DateColumnUserFilterState> {

  public JsonDateColumnUserFilter(DateColumnUserFilterState filterState) {
    super(filterState);
  }

  @Override
  public String getObjectType() {
    return "DateColumnUserFilter";
  }

  protected String dateToJson(Date date) {
    return JsonDate.format(date, JsonDate.JSON_PATTERN_DATE_ONLY, false);
  }

  protected Date toDate(String dateString) {
    return JsonDate.parse(dateString, JsonDate.JSON_PATTERN_DATE_ONLY);
  }

  @Override
  public ColumnUserFilterState createFilterStateFromJson(IColumn<?> column, JSONObject json) {
    DateColumnUserFilterState filterState = new DateColumnUserFilterState(column);
    filterState.setSelectedValues(createSelectedValuesFromJson(json));
    filterState.setDateFrom(toDate(json.optString("dateFrom")));
    filterState.setDateTo(toDate(json.optString("dateTo")));
    return filterState;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("dateFrom", dateToJson(getFilterState().getDateFrom()));
    json.put("dateTo", dateToJson(getFilterState().getDateTo()));
    return json;
  }

}
