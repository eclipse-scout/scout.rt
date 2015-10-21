/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.scout.rt.client.ui.form.fields.chartfield;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;

import com.bsiag.scout.rt.client.ui.basic.chart.IChart;

/**
 * @since 5.2
 */
@ClassId("cee88505-5685-438d-a87d-591c54efe8d7")
public class AbstractChartField<T extends IChart> extends AbstractFormField implements IChartField<T> {

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
    addPropertyChangeListener(PROP_ENABLED, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        if (m_chart != null) {
          m_chart.setEnabled(isEnabled());
        }
      }
    });
  }

  protected Class<? extends IChart> getConfiguredChart() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IChart>> f = ConfigurationUtility.filterClasses(dca, IChart.class);
    if (f.size() == 1) {
      return CollectionUtility.firstElement(f);
    }
    else {
      for (Class<? extends IChart> c : f) {
        if (c.getDeclaringClass() != AbstractChartField.class) {
          return c;
        }
      }
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  protected T createChart() {
    List<IChart> contributedFields = m_contributionHolder.getContributionsByClass(IChart.class);
    IChart result = CollectionUtility.firstElement(contributedFields);
    if (result != null) {
      return (T) result;
    }

    Class<? extends IChart> configuredChart = getConfiguredChart();
    if (configuredChart != null) {
      try {
        return (T) ConfigurationUtility.newInnerInstance(this, configuredChart);
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException(
            "error creating instance of class '" + configuredChart.getName() + "'.", e));
      }
    }
    return null;
  }

  @Override
  public final T getChart() {
    return m_chart;
  }

  @Override
  public void setChart(T newChart) {
    setChartInternal(newChart);
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

  // TODO export import
  // TODO interception
}
