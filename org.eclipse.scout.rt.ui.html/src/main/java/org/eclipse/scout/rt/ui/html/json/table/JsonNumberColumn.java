/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserFilterState;
import org.eclipse.scout.rt.ui.html.json.MainJsonObjectFactory;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonNumberColumnUserFilter;
import org.json.JSONObject;

public class JsonNumberColumn<T extends INumberColumn<?>> extends JsonColumn<T> {

  public JsonNumberColumn(T model) {
    super(model);
  }

  @Override
  public String getObjectType() {
    return "NumberColumn";
  }

  @Override
  protected ColumnUserFilterState createFilterStateFromJson(JSONObject json) {
    return new JsonNumberColumnUserFilter(null).createFilterStateFromJson(getColumn(), json);
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("decimalFormat", MainJsonObjectFactory.get().createJsonObject(getColumn().getFormat()).toJson());
    json.put("aggregationFunction", getColumn().getAggregationFunction());
    json.put("allowedAggregationFunctions", MainJsonObjectFactory.get().createJsonObject(getColumn().getAllowedAggregationFunctions()).toJson());
    json.put("backgroundEffect", getColumn().getBackgroundEffect());
    return json;
  }

  @Override
  public boolean isValueRequired() {
    return true;
  }

}
