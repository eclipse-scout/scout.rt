/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.ui.html.json.form.fields.chartfield;

import org.eclipse.scout.rt.chart.client.ui.basic.chart.IChart;
import org.eclipse.scout.rt.chart.client.ui.form.fields.chartfield.IChartField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

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
