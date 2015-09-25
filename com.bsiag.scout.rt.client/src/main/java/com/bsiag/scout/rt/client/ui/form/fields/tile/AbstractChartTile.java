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
package com.bsiag.scout.rt.client.ui.form.fields.tile;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;

import com.bsiag.scout.rt.client.ui.form.fields.chartfield.AbstractChart;
import com.bsiag.scout.rt.client.ui.form.fields.chartfield.AbstractChartField;
import com.bsiag.scout.rt.client.ui.form.fields.tile.AbstractChartTile.ChartField;
import com.bsiag.scout.rt.client.ui.form.fields.tile.AbstractChartTile.ChartField.Chart;

/**
 * @since 5.2
 */
public abstract class AbstractChartTile extends AbstractTile<ChartField> {

  public AbstractChartTile() {
    this(true);
  }

  public AbstractChartTile(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * If set, this value is applied to the tile field's "autoColor" property.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(70)
  protected Boolean getConfiguredAutoColor() {
    return null;
  }

  /**
   * If set, this value is applied to the tile field's "chartType" property.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(70)
  protected Integer getConfiguredChartType() {
    return null;
  }

  @Override
  protected void initFileFieldInternal() {
    super.initFileFieldInternal();

    if (getConfiguredAutoColor() != null) {
      getTileField().getChart().setAutoColor(getConfiguredAutoColor());
    }
    if (getConfiguredChartType() != null) {
      getTileField().getChart().setChartType(getConfiguredChartType());
    }
  }

  public Chart getChart() {
    return getTileField().getChart();
  }

  public class ChartField extends AbstractChartField<ChartField.Chart> {

    public class Chart extends AbstractChart {
    }
  }
}
