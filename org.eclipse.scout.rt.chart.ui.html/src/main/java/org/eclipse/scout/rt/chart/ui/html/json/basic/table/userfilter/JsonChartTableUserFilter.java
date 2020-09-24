/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.chart.ui.html.json.basic.table.userfilter;

import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonTableUserFilter;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonChartTableUserFilter<T extends ChartTableUserFilterState> extends JsonTableUserFilter<T> {

  public JsonChartTableUserFilter(T filter) {
    super(filter);
  }

  @Override
  public String getObjectType() {
    return "ChartTableUserFilter";
  }

  @Override
  public boolean isValid() {
    return getJsonTable().getColumnId(getFilterState().getColumnX()) != null;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("text", getFilterState().getText());
    json.put("filters", new JSONArray(getFilterState().getFilters()));
    json.put("columnIdX", getJsonTable().getColumnId(getFilterState().getColumnX()));
    json.put("columnIdY", getJsonTable().getColumnId(getFilterState().getColumnY()));
    return json;
  }

}
