/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.scout.rt.client.ui.form.fields.chartfield;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

import com.bsiag.scout.rt.client.ui.basic.chart.AbstractChart;
import com.bsiag.scout.rt.client.ui.basic.chart.IChart;

/**
 * @since 5.2
 */
@ClassId("cee88505-5685-438d-a87d-591c54efe8d7")
public abstract class AbstractChartField<T extends IChart> extends AbstractFormField implements IChartField<T> {

  private T m_chart;

  public AbstractChartField() {
    super(true);
  }

  public AbstractChartField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setChartInternal(createChart());
    // local enabled listener
    addPropertyChangeListener(PROP_ENABLED_COMPUTED, e -> {
      if (m_chart == null) {
        return;
      }
      boolean newEnabled = ((Boolean) e.getNewValue()).booleanValue();
      m_chart.setEnabled(newEnabled);
    });
  }

  protected Class<? extends IChart> getConfiguredChart() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IChart>> fc = ConfigurationUtility.filterClasses(dca, IChart.class);
    if (fc.size() == 1) {
      return CollectionUtility.firstElement(fc);
    }
    else {
      for (Class<? extends IChart> c : fc) {
        if (c.getDeclaringClass() != AbstractChartField.class) {
          return c;
        }
      }
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  protected T createChart() {
    List<IChart> contributedCharts = m_contributionHolder.getContributionsByClass(IChart.class);
    IChart result = CollectionUtility.firstElement(contributedCharts);
    if (result != null) {
      return (T) result;
    }
    Class<? extends IChart> configuredChart = getConfiguredChart();
    if (configuredChart != null) {
      return (T) ConfigurationUtility.newInnerInstance(this, configuredChart);
    }
    return null;
  }

  @Override
  public final T getChart() {
    return m_chart;
  }

  @Override
  public void setChart(T chart) {
    setChartInternal(chart);
  }

  protected void setChartInternal(T chart) {
    if (m_chart == chart) {
      return;
    }
    if (m_chart != null) {
      m_chart.setContainerInternal(null);
    }
    m_chart = chart;
    if (m_chart != null) {
      m_chart.setContainerInternal(this);
      m_chart.setEnabled(isEnabled());
    }
    boolean changed = propertySupport.setProperty(PROP_CHART, m_chart);
    if (changed) {
      if (getForm() != null) {
        getForm().structureChanged(this);
      }
      updateKeyStrokes();
    }
  }

  // TODO [15.4] bsh: export import, interception

  @ClassId("a6e76b7c-f42c-43b2-bcb8-55bbbc534b80")
  public class Chart extends AbstractChart {

  }
}
