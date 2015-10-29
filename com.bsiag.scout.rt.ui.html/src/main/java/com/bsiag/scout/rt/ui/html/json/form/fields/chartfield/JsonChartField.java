/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.scout.rt.ui.html.json.form.fields.chartfield;

import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

import com.bsiag.scout.rt.client.ui.basic.chart.IChart;
import com.bsiag.scout.rt.client.ui.form.fields.chartfield.IChartField;

/**
 * @since 5.2
 */
public class JsonChartField<CHART_FIELD extends IChartField<? extends IChart>> extends JsonFormField<CHART_FIELD> {

  public JsonChartField(CHART_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ChartField";
  }

  @Override
  protected void initJsonProperties(CHART_FIELD model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<IChartField<? extends IChart>>(IChartField.PROP_CHART, model, getUiSession()) {
      @Override
      protected IChart modelValue() {
        return getModel().getChart();
      }
    });
  }
}
