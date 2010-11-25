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
package org.eclipse.scout.rt.client.ui.form.fields.chartbox;

import java.util.EventListener;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;

public abstract class AbstractChartBox extends AbstractFormField implements IChartBox {
  private IChartBoxUIFacade m_uiFacade;
  private EventListenerList m_listenerList = new EventListenerList();

  public AbstractChartBox() {
  }

  /*
   * Configuration
   */
  /**
   * Fully qualified class name of a ISwingChartProvider class with bundle
   * symbolic name prefix<br>
   * Example: <code>com.bsiag.crm.ui.swing / com.bsiag.crm.ui.swing.chart.ForecastChart</code>
   */
  @ConfigProperty(ConfigProperty.CHART_QNAME)
  @Order(190)
  @ConfigPropertyValue("null")
  protected String getConfiguredChartQName() {
    return null;
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    setChartQName(getConfiguredChartQName());
  }

  /*
   * Runtime
   */

  public String getChartQName() {
    return propertySupport.getPropertyString(PROP_CHART_QNAME);
  }

  public void setChartQName(String className) {
    propertySupport.setPropertyString(PROP_CHART_QNAME, className);
  }

  public IChartBoxUIFacade getUIFacade() {
    return m_uiFacade;
  }

  public void addChartBoxListener(ChartBoxListener listener) {
    m_listenerList.add(ChartBoxListener.class, listener);
  }

  public void removeChartBoxListener(ChartBoxListener listener) {
    m_listenerList.remove(ChartBoxListener.class, listener);
  }

  public void refreshChart() {
    fireDataChanged();
  }

  private void fireDataChanged() {
    fireChartBoxEvent(new ChartBoxEvent(this, ChartBoxEvent.TYPE_DATA_CHANGED));
  }

  // main handler
  private void fireChartBoxEvent(ChartBoxEvent e) {
    EventListener[] listeners = m_listenerList.getListeners(ChartBoxListener.class);
    if (listeners != null && listeners.length > 0) {
      for (int i = 0; i < listeners.length; i++) {
        ((ChartBoxListener) listeners[i]).chartBoxChanged(e);
      }
    }
  }

  private class P_UIFacade implements IChartBoxUIFacade {
    // empty so far

  }

}
