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
package com.bsiag.scout.rt.client.ui.basic.chart;

import java.math.BigDecimal;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;

import com.bsiag.scout.rt.shared.data.basic.chart.IChartBean;
import com.bsiag.scout.rt.shared.data.basic.chart.IChartType;

/**
 * @since 5.2
 */
@ClassId("c31e0b6e-77bd-4752-ab1a-bda7560230b2")
public abstract class AbstractChart extends AbstractPropertyObserver implements IChart {
	// TODO make extensible

	private IChartUIFacade m_uiFacade;
	private final EventListenerList m_listenerList = new EventListenerList();

	public AbstractChart() {
		this(true);
	}

	public AbstractChart(boolean callInitializer) {
		if (callInitializer) {
			callInitializer();
		}
	}

	@Override
	public String classId() {
		String simpleClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
		if (getContainer() != null) {
			return simpleClassId + ID_CONCAT_SYMBOL + getContainer().classId();
		}
		return simpleClassId;
	}

	protected void callInitializer() {
		initConfig();
	}

	/*
	 * Configuration
	 */
	protected void initConfig() {
		m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
		setChartType(getConfiguredChartType());
		setAutoColor(getConfiguredAutoColor());
		setEnabled(getConfiguredEnabled());
		setVisible(getConfiguredVisible());
		setMaxSegments(getConfiguredMaxSegments());
		setClickable(getConfiguredClickable());
		setModelHandlesClick(getConfiguredModelHandelsClick());
		setAnimated(getConfiguredAnimated());
		setLegendPosition(getConfiguredLegendPosition());
		setLegendVisible(getConfiguredLegendVisible());
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
	protected boolean getConfiguredEnabled() {
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

	@Override
	public IChartUIFacade getUIFacade() {
		return m_uiFacade;
	}

	/**
	 * do not use this internal method unless you are implementing a container
	 * that holds and controls an {@link IChart}
	 */
	@Override
	public void setContainerInternal(ITypeWithClassId container) {
		propertySupport.setProperty(PROP_CONTAINER, container);
	}

	@Override
	public ITypeWithClassId getContainer() {
		return (ITypeWithClassId) propertySupport.getProperty(PROP_CONTAINER);
	}

	@Override
	public void addChartListener(ChartListener listener) {
		m_listenerList.add(ChartListener.class, listener);
	}

	@Override
	public void removeChartListener(ChartListener listener) {
		m_listenerList.remove(ChartListener.class, listener);
	}

	public void fireValueClicked(int[] axesPosition, BigDecimal value) {
		ChartEvent event = new ChartEvent(this, ChartEvent.TYPE_VALUE_CLICKED);
		event.setAxesPosition(axesPosition);
		event.setValue(value);
		ChartListener[] listeners = m_listenerList.getListeners(ChartListener.class);
		for (ChartListener listener : listeners) {
			listener.chartChanged(event);
		}
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
	public void setEnabled(boolean enabled) {
		propertySupport.setPropertyBool(PROP_ENABLED, enabled);
	}

	@Override
	public boolean isEnabled() {
		return propertySupport.getPropertyBool(PROP_ENABLED);
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
	public boolean isModelHandlesClick() {
		return propertySupport.getPropertyBool(PROP_MODEL_HANDLES_CLICK);
	}

	@Override
	public void setModelHandlesClick(boolean modelHandlesClick) {
		propertySupport.setPropertyBool(PROP_MODEL_HANDLES_CLICK, modelHandlesClick);
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

	protected class P_UIFacade implements IChartUIFacade {

		@Override
		public void fireValueClickedFromUI(int[] axesPosition, BigDecimal value) {
			try {
				if (isEnabled() && isVisible()) {
					fireValueClicked(axesPosition, value);
				}
			} catch (Exception e) {
				BEANS.get(ExceptionHandler.class).handle(e);
			}
		}
	}

}
