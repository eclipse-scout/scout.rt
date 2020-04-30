/*
 * Copyright (c) 2014-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package com.bsiag.scout.rt.ui.html.json.basic.chart;

import java.util.Map;

import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.MainJsonObjectFactory;
import org.json.JSONObject;

import com.bsiag.scout.rt.client.ui.basic.chart.IChart;
import com.bsiag.scout.rt.shared.data.basic.chart.IChartData;

/**
 * @since 5.2
 */
public class JsonChart<CHART extends IChart> extends AbstractJsonWidget<CHART> {

  public static final String EVENT_VALUE_CLICK = "valueClick";

  public JsonChart(CHART model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Chart";
  }

  @Override
  protected void initJsonProperties(CHART model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IChart>(IChart.PROP_DATA, model) {
      @Override
      protected IChartData modelValue() {
        return getModel().getData();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return MainJsonObjectFactory.get().createJsonObject(value).toJson();
      }
    });
    putJsonProperty(new JsonProperty<IChart>(IChart.PROP_CONFIG, model) {
      @Override
      protected Map<String, Object> modelValue() {
        return getModel().getConfig() != null ? getModel().getConfig().getProperties() : null;
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return MainJsonObjectFactory.get().createJsonObject(value).toJson();
      }
    });
    putJsonProperty(new JsonProperty<IChart>(IChart.PROP_VISIBLE, model) {
      @Override
      protected Object modelValue() {
        return getModel().isVisible();
      }
    });
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_VALUE_CLICK.equals(event.getType())) {
      handleUiValueClick(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiValueClick(JsonEvent event) {
    JSONObject data = event.getData();
    int axisIndex = data.getInt("axisIndex");
    int valueIndex = data.getInt("valueIndex");
    int groupIndex = data.getInt("groupIndex");
    getModel().getUIFacade().fireValueClickFromUI(axisIndex, valueIndex, groupIndex);
  }
}
