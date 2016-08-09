/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.scout.rt.client.ui.basic.table.controls;

import org.eclipse.scout.rt.client.ui.basic.table.controls.AbstractTableControl;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.TEXTS;

import com.bsiag.scout.rt.shared.data.basic.chart.IChartType;

@ClassId("c097daeb-8105-4e11-bd02-ba37e461e033")
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
   * <li>{@link IChartType#BAR_VERTICAL}</li>
   * <li>{@link IChartType#BAR_HORIZONTAL}</li>
   * <li>{@link IChartType#SCATTER}</li>
   * </ul>
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(100)
  protected int getConfiguredChartType() {
    return IChartType.BAR_VERTICAL;
  }

  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(200)
  protected IChartColumnParam getConfiguredAggregation() {
    return new ChartColumnParam(-1, IChartColumnParam.AGGREGATION_COUNT);
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
  public void setChartType(int chartType) {
    propertySupport.setPropertyInt(PROP_CHART_TYPE, chartType);
  }

  @Override
  public int getChartType() {
    return propertySupport.getPropertyInt(PROP_CHART_TYPE);
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
