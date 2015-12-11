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
import org.eclipse.scout.rt.platform.util.Range;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
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

  @Override
  public JSONObject toJson() {
    // FIXME AWE: (filter) refactor this method when we have Columns per type
    JSONObject json = super.toJson();
    JsonTable jsonTable = getJsonTable();
    ColumnUserFilterState filterState = getFilterState();
    IColumn modelColumn = filterState.getColumn();
    json.put("column", jsonTable.getColumnId(modelColumn));
    json.put("selectedValues", new JSONArray(filterState.getSelectedValues()));
    String colummType = jsonTable.getColumnType(modelColumn);
    if ("text".equals(colummType)) {
      json.put("freeText", getFilterState().getFreeText());
    }
    else if ("number".equals(colummType)) {
      json.put("numberRange", numberRangeToJson(getFilterState().getNumberRange()));
    }
    else if ("date".equals(colummType)) {
      json.put("dateRange", dateRangeToJson(getFilterState().getDateRange()));
    }
    return json;
  }

  protected JSONObject numberRangeToJson(Range<Number> range) {
    if (range == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    json.put("from", range.getFrom());
    json.put("to", range.getTo());
    return json;
  }

  protected JSONObject dateRangeToJson(Range<Date> range) {
    if (range == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    json.put("from", formatDate(range.getFrom()));
    json.put("to", formatDate(range.getTo()));
    return json;
  }

  protected String formatDate(Date date) { // FIXME AWE: (filter) user JsonDate (see DateField)
    return DateUtility.format(date, "y-M-dd");
  }

  @Override
  public String toString() {
    return getObjectType() + ", " + getFilterState().getColumn();
  }
}
