/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.scout.rt.client.ui.basic.chart;

import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;

import com.bsiag.scout.rt.client.ui.basic.chart.ChartChains.ChartValueClickChain;
import com.bsiag.scout.rt.shared.data.basic.chart.ChartBean;
import com.bsiag.scout.rt.shared.data.basic.chart.ChartValueGroupBean;
import com.bsiag.scout.rt.shared.data.basic.chart.IChartBean;
import com.bsiag.scout.rt.shared.data.basic.chart.IChartType;

/**
 * <h1>Chart types</h1>
 * <h2>Pie and Donut Chart</h2> ({@link IChartType.PIE}, {@link IChartType.DONUT})
 * <p>
 * Sectors on pie or donut represent a single {@link ChartValueGroupBean} on the {@link ChartBean}. For each
 * {@link ChartValueGroupBean} there can be only one value. On the {@link ChartBean} there are no axes set.
 * <h2>Line Chart</h2> ({@link IChartType.LINE})
 * <p>
 * Each line represents a single {@link ChartValueGroupBean} on the {@link ChartBean}. All added
 * {@link ChartValueGroupBean} must have the same value count. The labels of the x-axis are read out of
 * {@link ChartBean#getAxes()}<code>[0]</code>. Each label in this list corresponds to the index in the value list in
 * the {@link ChartValueGroupBean}.
 * <h2>Bar Chart</h2> ({@link IChartType.BAR})
 * <p>
 * Each bar group in the same color represents a single {@link ChartValueGroupBean} on the {@link ChartBean}. All added
 * {@link ChartValueGroupBean} must have the same value count. The labels of the x-axis are read out of
 * {@link ChartBean#getAxes()}<code>[0]</code>. Each label in this list corresponds to the index in the value list in
 * the {@link ChartValueGroupBean}.
 * <p>
 * <h2>Fulfillment Cart</h2> ({@link IChartType.FULFILLMENT})
 * <p>
 * There have to be exactly two {@link ChartValueGroupBean} on the {@link ChartBean}, with just one value each. The
 * first group contains the degree of fulfillment and the second group contains the value to fulfill. This type has no
 * legendbox.
 * <h2>Sales Funnel Chart</h2> ({@link IChartType.SALESFUNNEL})
 * <p>
 * A salesfunnel consists of n {@link ChartValueGroupBean}. Each represents a bar in the chart. Every group must have at
 * least one value and at most two values. This type has no legendbox.
 * <p>
 * There are custom attributes for the sales funnel chart:
 * <ul>
 * <li>{@link IChartType#PROP_SALESFUNNEL_NORMALIZED} defines if the bars should be rendered smaller from top to bottom
 * (true) or if they get a size according to their values (false).
 * <li>{@link IChartType.PROP_SALESFUNNEL_CALC_CONVERSION_RATE} specifies whether the conversion rate should be rendered
 * (true or false).
 * </ul>
 * <h2>Speedo Chart</h2> ({@link IChartType.SPEEDO})
 * <p>
 * For a speedo chart there have to be just one {@link ChartValueGroupBean} on the {@link ChartBean}. The first value on
 * the group is the min value of the range, the second the actual value is the actual value for the pointer and the
 * third value is the max value for the range. The {@link IChartType#PROP_SPEEDO_CHART_GREEN_AREA_POSITION} is a custom
 * property which defines where the green area is located on the speedo. This type has no legendbox.
 * <h2>Venn Chart</h2> ({@link IChartType.VENN})
 * <p>
 * A Venn-chart consist of one to seven {@link ChartValueGroupBean} with exactly one value. First there are all values
 * for the own circle followed by all combinations. The venn chart is limited to three circles.
 * {@link IChartType.PROP_VENN_NUMBER_OF_CIRCLES} specifies the number of circles.
 * <p>
 * Examples:
 * <ul>
 * <li>Three circles: {"Set A", "Set B", "Set C", "A-B", "A-C", "B-C", "A-B-C"}
 * <li>Two circles:{"Set A", "Set B", "A-B"}
 * </ul>
 * <h1>Other attributes</h1>
 * <h2>Legend</h2> Write the text for the legend into groupName of {@link ChartValueGroupBean}.
 * <p>
 *
 * @since 5.2
 */
@ClassId("c31e0b6e-77bd-4752-ab1a-bda7560230b2")
public abstract class AbstractChart extends AbstractWidget implements IChart, IExtensibleObject {

  private IChartUIFacade m_uiFacade;
  private final FastListenerList<ChartListener> m_listenerList = new FastListenerList<>();
  private final ObjectExtensions<AbstractChart, IChartExtension<? extends AbstractChart>> m_objectExtensions;

  public AbstractChart() {
    this(true);
  }

  public AbstractChart(boolean callInitializer) {
    super(false);
    m_objectExtensions = new ObjectExtensions<>(this, false);
    if (callInitializer) {
      callInitializer();
    }
  }

  /*
   * Configuration
   */
  @Override
  protected void initConfig() {
    super.initConfig();
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    setChartType(getConfiguredChartType());
    setAutoColor(getConfiguredAutoColor());
    setVisible(getConfiguredVisible());
    setMaxSegments(getConfiguredMaxSegments());
    setClickable(getConfiguredClickable());
    setAnimated(getConfiguredAnimated());
    setLegendPosition(getConfiguredLegendPosition());
    setLegendVisible(getConfiguredLegendVisible());
    setInteractiveLegendVisible(getConfiguredInteractiveLegendVisible());
  }

  @Override
  protected void initConfigInternal() {
    m_objectExtensions.initConfigAndBackupExtensionContext(createLocalExtension(), this::initConfig);
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(10)
  protected int getConfiguredChartType() {
    return IChartType.PIE;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(20)
  protected boolean getConfiguredAutoColor() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(20)
  protected boolean getConfiguredVisible() {
    return true;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(30)
  protected int getConfiguredMaxSegments() {
    return DEFAULT_MAX_SEGMENTS_PIE;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(40)
  protected boolean getConfiguredModelHandelsClick() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(50)
  protected boolean getConfiguredClickable() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(60)
  protected boolean getConfiguredAnimated() {
    return true;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(70)
  protected int getConfiguredLegendPosition() {
    return IChart.LEGEND_POSITION_RIGHT;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(80)
  protected boolean getConfiguredLegendVisible() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(90)
  protected boolean getConfiguredInteractiveLegendVisible() {
    return true;
  }

  @Override
  public IChartUIFacade getUIFacade() {
    return m_uiFacade;
  }

  /**
   * @deprecated Will be removed in Scout 11. Use {@link #getParent()} or {@link #getParentOfType(Class)} instead.
   */
  @Override
  @Deprecated
  @SuppressWarnings("deprecation")
  public ITypeWithClassId getContainer() {
    return getParent();
  }

  @Override
  public IFastListenerList<ChartListener> chartListeners() {
    return m_listenerList;
  }

  protected final void interceptValueClick(int axisIndex, int valueIndex, int groupIndex) {
    List<? extends IChartExtension<? extends AbstractChart>> extensions = getAllExtensions();
    ChartValueClickChain chain = new ChartValueClickChain(extensions);
    chain.execValueClick(axisIndex, valueIndex, groupIndex);
  }

  @Override
  public final List<? extends IChartExtension<? extends AbstractChart>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  protected IChartExtension<? extends AbstractChart> createLocalExtension() {
    return new LocalChartExtension<>(this);
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> clazz) {
    return m_objectExtensions.getExtension(clazz);
  }

  @ConfigOperation
  @Order(10)
  protected void execValueClick(int axisIndex, int valueIndex, int groupIndex) {
    ChartEvent event = new ChartEvent(this, ChartEvent.TYPE_VALUE_CLICK);
    event.setAxisIndex(axisIndex);
    event.setValueIndex(valueIndex);
    event.setGroupIndex(groupIndex);
    fireChartEventInternal(event);
  }

  protected void fireChartEventInternal(ChartEvent event) {
    chartListeners().list().forEach(listener -> listener.chartChanged(event));
  }

  @Override
  public void fireChartDataChanged() {
    propertySupport.setPropertyAlwaysFire(PROP_CHART_DATA, getChartData());
  }

  @Override
  public void setChartType(int chartType) {
    propertySupport.setProperty(PROP_CHART_TYPE, chartType);
  }

  @Override
  public int getChartType() {
    return propertySupport.getPropertyInt(PROP_CHART_TYPE);
  }

  @Override
  public void setAutoColor(boolean autoColor) {
    propertySupport.setProperty(PROP_AUTO_COLOR, autoColor);
  }

  @Override
  public boolean isAutoColor() {
    return propertySupport.getPropertyBool(PROP_AUTO_COLOR);
  }

  @Override
  public void setChartData(IChartBean chartData) {
    propertySupport.setProperty(PROP_CHART_DATA, chartData);
  }

  @Override
  public IChartBean getChartData() {
    return (IChartBean) propertySupport.getProperty(PROP_CHART_DATA);
  }

  @Override
  public void setVisible(boolean visible) {
    propertySupport.setPropertyBool(PROP_VISIBLE, visible);
  }

  @Override
  public boolean isVisible() {
    return propertySupport.getPropertyBool(PROP_VISIBLE);
  }

  @Override
  public int getMaxSegments() {
    return propertySupport.getPropertyInt(PROP_MAX_SEGMENTS);
  }

  @Override
  public void setMaxSegments(int maxSegments) {
    propertySupport.setPropertyInt(PROP_MAX_SEGMENTS, maxSegments);
  }

  @Override
  public boolean isClickable() {
    return propertySupport.getPropertyBool(PROP_CLICKABLE);
  }

  @Override
  public void setClickable(boolean clickable) {
    propertySupport.setPropertyBool(PROP_CLICKABLE, clickable);
  }

  @Override
  public boolean isAnimated() {
    return propertySupport.getPropertyBool(PROP_ANIMATED);
  }

  @Override
  public void setAnimated(boolean animated) {
    propertySupport.setPropertyBool(PROP_ANIMATED, animated);
  }

  @Override
  public void setLegendPosition(int position) {
    propertySupport.setPropertyInt(PROP_LEGEND_POSITION, position);
  }

  @Override
  public int getLegendPosition() {
    return propertySupport.getPropertyInt(PROP_LEGEND_POSITION);
  }

  @Override
  public boolean isLegendVisible() {
    return propertySupport.getPropertyBool(PROP_LEGEND_VISIBLE);
  }

  @Override
  public void setLegendVisible(boolean visible) {
    propertySupport.setPropertyBool(PROP_LEGEND_VISIBLE, visible);
  }

  @Override
  public boolean isInteractiveLegendVisible() {
    return propertySupport.getPropertyBool(PROP_INTERACTIVE_LEGEND_VISIBLE);
  }

  @Override
  public void setInteractiveLegendVisible(boolean visible) {
    propertySupport.setPropertyBool(PROP_INTERACTIVE_LEGEND_VISIBLE, visible);
  }

  protected class P_UIFacade implements IChartUIFacade {

    @Override
    public void fireValueClickFromUI(int axisIndex, int valueIndex, int groupIndex) {
      try {
        if (isEnabled() && isVisible()) {
          interceptValueClick(axisIndex, valueIndex, groupIndex);
        }
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
  }

  protected static class LocalChartExtension<CHART extends AbstractChart> extends AbstractExtension<CHART> implements IChartExtension<CHART> {

    public LocalChartExtension(CHART owner) {
      super(owner);
    }

    @Override
    public void execValueClick(ChartValueClickChain chain, int axisIndex, int valueIndex, int groupIndex) {
      getOwner().execValueClick(axisIndex, valueIndex, groupIndex);
    }
  }

}
