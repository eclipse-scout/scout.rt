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

import java.math.BigDecimal;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.NumberColumnUserFilterState;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.json.JSONObject;

public class JsonNumberColumnUserFilter extends JsonColumnUserFilter<NumberColumnUserFilterState> {

  public JsonNumberColumnUserFilter(NumberColumnUserFilterState filterState) {
    super(filterState);
  }

  @Override
  public String getObjectType() {
    return "NumberColumnUserFilter";
  }

  protected String bigDecimalToJson(BigDecimal number) {
    if (number == null) {
      return null;
    }
    return number.toString();
  }

  protected BigDecimal toBigDecimal(String numberString) {
    if (StringUtility.isNullOrEmpty(numberString)) {
      return null;
    }
    return new BigDecimal(numberString);
  }

  @Override
  public ColumnUserFilterState createFilterStateFromJson(IColumn<?> column, JSONObject json) {
    NumberColumnUserFilterState filterState = new NumberColumnUserFilterState(column);
    filterState.setSelectedValues(createSelectedValuesFromJson(json));
    filterState.setNumberFrom(toBigDecimal(json.optString("numberFrom")));
    filterState.setNumberTo(toBigDecimal(json.optString("numberTo")));
    return filterState;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("numberFrom", bigDecimalToJson(getFilterState().getNumberFrom()));
    json.put("numberTo", bigDecimalToJson(getFilterState().getNumberTo()));
    return json;
  }
}
