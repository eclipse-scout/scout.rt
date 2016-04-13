/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.scout.rt.ui.html.json.basic.table.controls;

import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
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
    putJsonProperty(new JsonProperty<CHART_TABLE_CONTROL>(IChartTableControl.PROP_CHART_AGGRAGATION, model) {
      @Override
      protected IChartColumnParam modelValue() {
        return getModel().getAggregation();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return JsonChartColumnParam.toJson((IChartColumnParam) value);
      }
    });
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (IChartTableControl.PROP_CHART_TYPE.equals(propertyName)) {
      int chartType = data.getInt(propertyName);
      addPropertyEventFilterCondition(IChartTableControl.PROP_CHART_TYPE, chartType);
      getModel().setChartType(chartType);
    }
    else if (IChartTableControl.PROP_CHART_AGGRAGATION.equals(propertyName)) {
      JSONObject chartAggregation = data.getJSONObject(propertyName);
      int index = chartAggregation.getInt("index");
      int modifier = chartAggregation.getInt("modifier");
      addPropertyEventFilterCondition(IChartTableControl.PROP_CHART_AGGRAGATION, chartAggregation);
      getModel().setAggregation(new ChartColumnParam(index, modifier));
    }
  }

}
