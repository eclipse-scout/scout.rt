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

import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserFilterState;
import org.eclipse.scout.rt.platform.util.Range;
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
    JSONObject json = super.toJson();
    json.put("column", getJsonTable().getColumnId(getFilterState().getColumn()));
    json.put("selectedValues", new JSONArray(getFilterState().getSelectedValues()));
    ColumnUserFilterState state = getFilterState();
    if ("text".equals(state.getType())) {
      json.put("freeText", getFilterState().getFreeText());
    }
    else if ("number".equals(state.getType())) {
      json.put("numberRange", toJson(getFilterState().getNumberRange()));
    }
    else if ("date".equals(state.getType())) {
      json.put("dateRange", toJson(getFilterState().getDateRange()));
    }
    return json;
  }

  protected JSONObject toJson(Range<?> range) {
    if (range == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    json.put("from", range.getFrom());
    json.put("to", range.getTo());
    return json;
  }

  @Override
  public String toString() {
    return getObjectType() + ", " + getFilterState().getColumn();
  }
}
