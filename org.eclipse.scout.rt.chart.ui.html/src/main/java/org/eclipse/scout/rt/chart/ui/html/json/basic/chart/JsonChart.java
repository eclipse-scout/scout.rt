/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.ui.html.json.basic.chart;

import java.math.BigDecimal;
import java.util.Map;

import org.eclipse.scout.rt.chart.client.ui.basic.chart.IChart;
import org.eclipse.scout.rt.chart.shared.data.basic.chart.IChartData;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.MainJsonObjectFactory;
import org.json.JSONObject;

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
    BigDecimal xIndex = !data.isNull("xIndex") ? NumberUtility.getBigDecimalValue(data.get("xIndex")) : null;
    BigDecimal yIndex = !data.isNull("yIndex") ? NumberUtility.getBigDecimalValue(data.get("yIndex")) : null;
    Integer datasetIndex = !data.isNull("datasetIndex") ? (Integer) data.get("datasetIndex") : null;
    getModel().getUIFacade().fireValueClickFromUI(xIndex, yIndex, datasetIndex);
  }
}
