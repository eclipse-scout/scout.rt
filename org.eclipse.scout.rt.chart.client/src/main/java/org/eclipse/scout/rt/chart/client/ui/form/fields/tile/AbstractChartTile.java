/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.chart.client.ui.form.fields.tile;

import java.math.BigDecimal;

import org.eclipse.scout.rt.chart.client.ui.basic.chart.AbstractChart;
import org.eclipse.scout.rt.chart.client.ui.form.fields.chartfield.AbstractChartField;
import org.eclipse.scout.rt.chart.client.ui.form.fields.tile.AbstractChartTile.ChartField;
import org.eclipse.scout.rt.chart.client.ui.form.fields.tile.AbstractChartTile.ChartField.Chart;
import org.eclipse.scout.rt.chart.shared.data.basic.chart.IChartConfig;
import org.eclipse.scout.rt.client.ui.tile.fields.AbstractFormFieldTile;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;

/**
 * @since 5.2
 */
@ClassId("e482fd82-fb1b-4dd5-8a42-ade0a0fa5eaf")
public abstract class AbstractChartTile extends AbstractFormFieldTile<ChartField> {

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
  protected String getConfiguredChartType() {
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
   * If set, this value is applied to the tile field chart's "legendClickable" property.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  @SuppressWarnings("findbugs:NP_BOOLEAN_RETURN_NULL")
  protected Boolean getConfiguredLegendClickable() {
    return null;
  }

  /**
   * If set, this value is applied to the tile field chart's "legend position" property.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(70)
  protected String getConfiguredLegendPosition() {
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
  protected Boolean getConfiguredTooltipsEnabled() {
    return true;
  }

  /**
   * If set, this value is applied to the tile field chart's "datalabels visible" property.
   */
  @SuppressWarnings("findbugs:NP_BOOLEAN_RETURN_NULL")
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  protected Boolean getConfiguredDatalabelsVisible() {
    return null;
  }

  @Override
  protected void initTileWidgetConfig() {
    super.initTileWidgetConfig();

    IChartConfig config = getTileWidget().getChart().getConfig();
    if (getConfiguredChartType() != null) {
      config.withType(getConfiguredChartType());
    }
    if (getConfiguredAutoColor() != null) {
      config.withAutoColor(getConfiguredAutoColor());
    }
    if (getConfiguredClickable() != null) {
      config.withClickable(getConfiguredClickable());
    }
    if (getConfiguredAnimated() != null) {
      config.withAnimated(getConfiguredAnimated());
    }
    if (getConfiguredLegendClickable() != null) {
      config.withLegendClickable(getConfiguredLegendClickable());
    }
    if (getConfiguredLegendPosition() != null) {
      config.withLegendPosition(getConfiguredLegendPosition());
    }
    if (getConfiguredLegendVisible() != null) {
      config.withLegendDisplay(getConfiguredLegendVisible());
    }
    if (getConfiguredTooltipsEnabled() != null) {
      config.withTooltipsEnabled(getConfiguredTooltipsEnabled());
    }
    if (getConfiguredDatalabelsVisible() != null) {
      config.withDatalabelsDisplay(getConfiguredDatalabelsVisible());
    }
    getTileWidget().getChart().setConfig(config);
  }

  /**
   * Delegated exec-method of the Chart widget. Override to handle click events on the chart.
   */
  protected void execValueClick(BigDecimal xIndex, BigDecimal yIndex, Integer datasetIndex) {
    // NOP
  }

  public Chart getChart() {
    return getTileWidget().getChart();
  }

  @Order(10)
  @ClassId("fb72a598-b9ca-44b7-b1be-0ca1a33bca6b")
  public class ChartField extends AbstractChartField<Chart> {

    @Override
    public String classId() {
      return AbstractChartTile.this.classId() + ID_CONCAT_SYMBOL + ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass(), true);
    }

    @ClassId("7c238257-fea0-4de1-8154-7db7f763ae85")
    public class Chart extends AbstractChart {

      @Override
      protected void execValueClick(BigDecimal xIndex, BigDecimal yIndex, Integer datasetIndex) {
        AbstractChartTile.this.execValueClick(xIndex, yIndex, datasetIndex);
      }
    }
  }
}
