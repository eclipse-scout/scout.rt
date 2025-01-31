/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.client.ui.basic.table.controls;

import org.eclipse.scout.rt.chart.shared.data.basic.chart.IChartType;
import org.eclipse.scout.rt.client.ui.basic.table.controls.AbstractTableControl;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.shared.AbstractIcons;

@ClassId("c097daeb-8105-4e11-bd02-ba37e461e033")
@Order(400)
public class ChartTableControl extends AbstractTableControl implements IChartTableControl {

  public ChartTableControl() {
    this(true);
  }

  public ChartTableControl(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setTooltipText(TEXTS.get("ui.Chart"));
    setIconId(AbstractIcons.Chart);
    setChartType(getConfiguredChartType());
    setAggregation(getConfiguredAggregation());
    setGroup1(getConfiguredGroup1());
    setGroup2(getConfiguredGroup2());
  }

  /**
   * Supported chart types:
   * <ul>
   * <li>{@link IChartType#PIE}</li>
   * <li>{@link IChartType#LINE}</li>
   * <li>{@link IChartType#BAR}</li>
   * <li>{@link IChartType#BAR_HORIZONTAL}</li>
   * <li>{@link IChartType#BUBBLE}</li>
   * </ul>
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(100)
  protected String getConfiguredChartType() {
    return IChartType.BAR;
  }

  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(200)
  protected IChartColumnParam getConfiguredAggregation() {
    return new ChartColumnParam(IChartColumnParam.AGGREGATION_COUNT);
  }

  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(200)
  protected IChartColumnParam getConfiguredGroup1() {
    return null;
  }

  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(200)
  protected IChartColumnParam getConfiguredGroup2() {
    return null;
  }

  @Override
  public void setChartType(String chartType) {
    propertySupport.setPropertyString(PROP_CHART_TYPE, chartType);
  }

  @Override
  public String getChartType() {
    return propertySupport.getPropertyString(PROP_CHART_TYPE);
  }

  @Override
  public void setGroup1(IChartColumnParam param) {
    propertySupport.setProperty(PROP_CHART_GROUP_1, param);
  }

  @Override
  public IChartColumnParam getGroup1() {
    return (IChartColumnParam) propertySupport.getProperty(PROP_CHART_GROUP_1);
  }

  @Override
  public void setGroup2(IChartColumnParam param) {
    propertySupport.setProperty(PROP_CHART_GROUP_2, param);
  }

  @Override
  public IChartColumnParam getGroup2() {
    return (IChartColumnParam) propertySupport.getProperty(PROP_CHART_GROUP_2);
  }

  @Override
  public void setAggregation(IChartColumnParam param) {
    propertySupport.setProperty(PROP_CHART_AGGREGATION, param);
  }

  @Override
  public IChartColumnParam getAggregation() {
    return (IChartColumnParam) propertySupport.getProperty(PROP_CHART_AGGREGATION);
  }
}
