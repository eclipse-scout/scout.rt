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
package org.eclipse.scout.rt.ui.html.json.table.userfilter;

import java.util.Date;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.DateColumnUserFilterState;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.json.JSONObject;

public class JsonDateColumnUserFilter extends JsonColumnUserFilter<DateColumnUserFilterState> {

  public JsonDateColumnUserFilter(DateColumnUserFilterState filterState) {
    super(filterState);
  }

  @Override
  public String getObjectType() {
    return "DateColumnUserFilter";
  }

  // FIXME AWE: (filter) user JsonDate (see DateField)
  protected String dateToJson(Date date) {
    if (date == null) {
      return null;
    }
    return DateUtility.format(date, "yyyy-MM-dd");
  }

  // FIXME AWE: (filter) fix this poor-mans solution and use JsonDate, property date-format
  private Date toDate(String dateString) {
    if (StringUtility.isNullOrEmpty(dateString)) {
      return null;
    }
    return DateUtility.parse(dateString, "yyyy-MM-dd");
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
