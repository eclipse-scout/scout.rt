/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.scout.rt.ui.html.json.basic.chart;

import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.MainJsonObjectFactory;

import com.bsiag.scout.rt.client.ui.basic.chart.IChart;
import com.bsiag.scout.rt.shared.data.basic.chart.IChartBean;

/**
 * @since 5.2
 */
public class JsonChart<CHART extends IChart> extends AbstractJsonPropertyObserver<CHART> {

  public static final String EVENT_VALUE_CLICKED = "valueClicked";

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
    putJsonProperty(new JsonProperty<IChart>(IChart.PROP_CHART_DATA, model) {
      @Override
      protected IChartBean modelValue() {
        return getModel().getChartData();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return MainJsonObjectFactory.get().createJsonObject(value).toJson();
      }
    });
    putJsonProperty(new JsonProperty<IChart>(IChart.PROP_AUTO_COLOR, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isAutoColor();
      }
    });
    putJsonProperty(new JsonProperty<IChart>(IChart.PROP_CHART_TYPE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getChartType();
      }
    });
    putJsonProperty(new JsonProperty<IChart>(IChart.PROP_ENABLED, model) {
      @Override
      protected Object modelValue() {
        return getModel().isEnabled();
      }
    });
    putJsonProperty(new JsonProperty<IChart>(IChart.PROP_VISIBLE, model) {
      @Override
      protected Object modelValue() {
        return getModel().isVisible();
      }
    });
    putJsonProperty(new JsonProperty<IChart>(IChart.PROP_MAX_SEGMENTS, model) {
      @Override
      protected Object modelValue() {
        return getModel().getMaxSegments();
      }
    });
    putJsonProperty(new JsonProperty<IChart>(IChart.PROP_CLICKABLE, model) {
      @Override
      protected Object modelValue() {
        return getModel().isClickable();
      }
    });
    putJsonProperty(new JsonProperty<IChart>(IChart.PROP_MODEL_HANDLES_CLICK, model) {
      @Override
      protected Object modelValue() {
        return getModel().isModelHandlesClick();
      }
    });
    putJsonProperty(new JsonProperty<IChart>(IChart.PROP_ANIMATED, model) {
      @Override
      protected Object modelValue() {
        return getModel().isAnimated();
      }
    });
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_VALUE_CLICKED.equals(event.getType())) {
      handleUiValueClicked(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiValueClicked(JsonEvent event) {
    // TODO nbu
    getModel().getUIFacade().fireValueClickedFromUI(new int[0], null);
  }
}
