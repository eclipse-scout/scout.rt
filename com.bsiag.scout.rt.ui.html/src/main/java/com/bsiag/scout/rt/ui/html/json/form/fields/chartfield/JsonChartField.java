/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.scout.rt.ui.html.json.form.fields.chartfield;

import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

import com.bsiag.scout.rt.client.ui.form.fields.chartfield.IChart;
import com.bsiag.scout.rt.client.ui.form.fields.chartfield.IChartField;

/**
 *
 */
public class JsonChartField<CHART_FIELD extends IChartField<? extends IChart>> extends JsonFormField<CHART_FIELD> {

  /**
   * @param model
   * @param uiSession
   * @param id
   * @param parent
   */
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
