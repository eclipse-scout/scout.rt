/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.scout.rt.client.ui.form.fields.tile;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;

import com.bsiag.scout.rt.client.ui.basic.chart.AbstractChart;
import com.bsiag.scout.rt.client.ui.form.fields.chartfield.AbstractChartField;
import com.bsiag.scout.rt.client.ui.form.fields.tile.AbstractChartTile.ChartField;
import com.bsiag.scout.rt.client.ui.form.fields.tile.AbstractChartTile.ChartField.Chart;

/**
 * @since 5.2
 */
@ClassId("e482fd82-fb1b-4dd5-8a42-ade0a0fa5eaf")
public abstract class AbstractChartTile extends AbstractTile<ChartField> {

  public AbstractChartTile() {
    this(true);
  }

  public AbstractChartTile(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * If set, this value is applied to the tile field chart's "chartType" property.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(70)
  protected Integer getConfiguredChartType() {
    return null;
  }

  /**
   * If set, this value is applied to the tile field chart's "autoColor" property.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  @SuppressWarnings("findbugs:NP_BOOLEAN_RETURN_NULL")
  protected Boolean getConfiguredAutoColor() {
    return null;
  }

  /**
   * If set, this value is applied to the tile field chart's "clickable" property.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  @SuppressWarnings("findbugs:NP_BOOLEAN_RETURN_NULL")
  protected Boolean getConfiguredClickable() {
    return null;
  }

  /**
   * If set, this value is applied to the tile field chart's "animated" property.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  @SuppressWarnings("findbugs:NP_BOOLEAN_RETURN_NULL")
  protected Boolean getConfiguredAnimated() {
    return null;
  }

  /**
   * If set, this value is applied to the tile field chart's "legend position" property.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(70)
  protected Integer getConfiguredLegendPosition() {
    return null;
  }

  /**
   * If set, this value is applied to the tile field chart's "legend visible" property.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(80)
  protected Boolean getConfiguredLegendVisible() {
    return true;
  }

  /**
   * If set, this value is applied to the tile field chart's "legend visible" property.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(90)
  protected Boolean getConfiguredInteractiveLegendVisible() {
    return true;
  }

  @Override
  protected void initTileFieldInternal() {
    super.initTileFieldInternal();

    if (getConfiguredChartType() != null) {
      getTileField().getChart().setChartType(getConfiguredChartType());
    }
    if (getConfiguredAutoColor() != null) {
      getTileField().getChart().setAutoColor(getConfiguredAutoColor());
    }
    if (getConfiguredClickable() != null) {
      getTileField().getChart().setClickable(getConfiguredClickable());
    }
    if (getConfiguredAnimated() != null) {
      getTileField().getChart().setAnimated(getConfiguredAnimated());
    }
    if (getConfiguredLegendPosition() != null) {
      getTileField().getChart().setLegendPosition(getConfiguredLegendPosition());
    }
    if (getConfiguredLegendVisible() != null) {
      getTileField().getChart().setLegendVisible(getConfiguredLegendVisible());
    }
    if (getConfiguredInteractiveLegendVisible() != null) {
      getTileField().getChart().setInteractiveLegendVisible(getConfiguredInteractiveLegendVisible());
    }
  }

  public Chart getChart() {
    return getTileField().getChart();
  }

  @Order(10)
  @ClassId("fb72a598-b9ca-44b7-b1be-0ca1a33bca6b")
  public class ChartField extends AbstractChartField<ChartField.Chart> {

    @Override
    public String classId() {
      return AbstractChartTile.this.classId() + ID_CONCAT_SYMBOL + ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass(), true);
    }

    @ClassId("7c238257-fea0-4de1-8154-7db7f763ae85")
    public class Chart extends AbstractChart {
    }
  }
}
