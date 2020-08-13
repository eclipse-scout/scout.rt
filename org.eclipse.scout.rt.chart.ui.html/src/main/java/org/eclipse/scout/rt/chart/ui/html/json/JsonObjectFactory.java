/*
 * Copyright (c) 2014-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.chart.ui.html.json;

import org.eclipse.scout.rt.chart.client.ui.basic.chart.IChart;
import org.eclipse.scout.rt.chart.client.ui.basic.table.controls.IChartTableControl;
import org.eclipse.scout.rt.chart.client.ui.form.fields.chartfield.IChartField;
import org.eclipse.scout.rt.chart.client.ui.form.fields.tile.AbstractChartTile;
import org.eclipse.scout.rt.chart.ui.html.json.basic.chart.JsonChart;
import org.eclipse.scout.rt.chart.ui.html.json.basic.table.controls.JsonChartTableControl;
import org.eclipse.scout.rt.chart.ui.html.json.basic.table.userfilter.ChartTableUserFilterState;
import org.eclipse.scout.rt.chart.ui.html.json.basic.table.userfilter.JsonChartTableUserFilter;
import org.eclipse.scout.rt.chart.ui.html.json.form.fields.chartfield.JsonChartField;
import org.eclipse.scout.rt.chart.ui.html.json.tile.JsonChartFieldTile;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonObjectFactory;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;

@Bean
@Order(5400)
public class JsonObjectFactory extends AbstractJsonObjectFactory {

  @Override
  public IJsonAdapter<?> createJsonAdapter(Object model, IUiSession session, String id, IJsonAdapter<?> parent) {
    if (model instanceof IChart) {
      return new JsonChart<>((IChart) model, session, id, parent);
    }
    if (model instanceof IChartField) {
      return new JsonChartField<IChartField<? extends IChart>>((IChartField<?>) model, session, id, parent);
    }
    if (model instanceof IChartTableControl) {
      return new JsonChartTableControl<>((IChartTableControl) model, session, id, parent);
    }
    if (model instanceof AbstractChartTile) {
      return new JsonChartFieldTile<>((AbstractChartTile) model, session, id, parent);
    }
    return null;
  }

  @Override
  public IJsonObject createJsonObject(Object object) {
    if (object instanceof ChartTableUserFilterState) {
      return new JsonChartTableUserFilter<>((ChartTableUserFilterState) object);
    }
    return null;
  }

  protected boolean isTabItem(Object widget) {
    return widget instanceof IGroupBox && ((IGroupBox) widget).getParent() instanceof ITabBox;
  }
}
