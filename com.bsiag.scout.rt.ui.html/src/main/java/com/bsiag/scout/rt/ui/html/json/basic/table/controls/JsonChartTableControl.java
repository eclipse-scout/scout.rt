/*
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package com.bsiag.scout.rt.ui.html.json.basic.table.controls;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
import org.eclipse.scout.rt.ui.html.json.table.control.JsonTableControl;
import org.json.JSONObject;

import com.bsiag.scout.rt.client.ui.basic.table.controls.ChartColumnParam;
import com.bsiag.scout.rt.client.ui.basic.table.controls.IChartColumnParam;
import com.bsiag.scout.rt.client.ui.basic.table.controls.IChartTableControl;

public class JsonChartTableControl<CHART_TABLE_CONTROL extends IChartTableControl> extends JsonTableControl<CHART_TABLE_CONTROL> {

  public JsonChartTableControl(CHART_TABLE_CONTROL model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ChartTableControl";
  }

  @Override
  protected void initJsonProperties(CHART_TABLE_CONTROL model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<CHART_TABLE_CONTROL>(IChartTableControl.PROP_CHART_TYPE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getChartType();
      }
    });
    putJsonProperty(new JsonProperty<CHART_TABLE_CONTROL>(IChartTableControl.PROP_CHART_AGGREGATION, model) {
      @Override
      protected IChartColumnParam modelValue() {
        return getModel().getAggregation();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return createJsonObject((IChartColumnParam) value);
      }
    });
    putJsonProperty(new JsonProperty<CHART_TABLE_CONTROL>(IChartTableControl.PROP_CHART_GROUP_1, model) {
      @Override
      protected IChartColumnParam modelValue() {
        return getModel().getGroup1();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return createJsonObject((IChartColumnParam) value);
      }
    });
    putJsonProperty(new JsonProperty<CHART_TABLE_CONTROL>(IChartTableControl.PROP_CHART_GROUP_2, model) {
      @Override
      protected IChartColumnParam modelValue() {
        return getModel().getGroup2();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return createJsonObject((IChartColumnParam) value);
      }
    });
  }

  protected JSONObject createJsonObject(IChartColumnParam columnParam) {
    JSONObject json = new JSONObject();
    if (columnParam != null) {
      IColumn<?> column = columnParam.getColumn();
      String columnId = column != null ? getTableAdapter().getColumnId(column) : null;
      int columnModifier = columnParam.getColumnModifier();
      json.put("id", columnId);
      json.put("modifier", columnModifier);
      return json;
    }
    return null;
  }

  protected JsonTable<?> getTableAdapter() {
    return Assertions.assertInstance(getParent(), JsonTable.class, "Unexpected parent adapter: {}", getParent());
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (IChartTableControl.PROP_CHART_TYPE.equals(propertyName)) {
      handleUiChartTypeChange(propertyName, data);
    }
    else if (IChartTableControl.PROP_CHART_AGGREGATION.equals(propertyName)) {
      handleUiChartAggregationChange(propertyName, data);
    }
    else if (IChartTableControl.PROP_CHART_GROUP_1.equals(propertyName)) {
      handleUiChartGroup1Change(propertyName, data);
    }
    else if (IChartTableControl.PROP_CHART_GROUP_2.equals(propertyName)) {
      handleUiChartGroup2Change(propertyName, data);
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }

  protected void handleUiChartTypeChange(String propertyName, JSONObject data) {
    int chartType = data.getInt(propertyName);
    addPropertyEventFilterCondition(IChartTableControl.PROP_CHART_TYPE, chartType);
    getModel().setChartType(chartType);
  }

  protected void handleUiChartAggregationChange(String propertyName, JSONObject data) {
    JSONObject chartAggregation = data.getJSONObject(propertyName);
    addPropertyEventFilterCondition(IChartTableControl.PROP_CHART_AGGREGATION, createColumnParam(chartAggregation));
    getModel().setAggregation(createColumnParam(chartAggregation));
  }

  protected void handleUiChartGroup1Change(String propertyName, JSONObject data) {
    JSONObject chartGroup1 = data.optJSONObject(propertyName);
    addPropertyEventFilterCondition(IChartTableControl.PROP_CHART_GROUP_1, createColumnParam(chartGroup1));
    getModel().setGroup1(createColumnParam(chartGroup1));
  }

  protected void handleUiChartGroup2Change(String propertyName, JSONObject data) {
    JSONObject chartGroup2 = data.optJSONObject(propertyName);
    addPropertyEventFilterCondition(IChartTableControl.PROP_CHART_GROUP_2, createColumnParam(chartGroup2));
    getModel().setGroup2(createColumnParam(chartGroup2));
  }

  protected ChartColumnParam createColumnParam(JSONObject jsonValue) {
    if (jsonValue == null) {
      return null;
    }
    String id = jsonValue.optString("id");
    int modifier = jsonValue.optInt("modifier");
    IColumn<?> column = getTableAdapter().optColumn(id);
    return new ChartColumnParam(column, modifier);
  }
}
