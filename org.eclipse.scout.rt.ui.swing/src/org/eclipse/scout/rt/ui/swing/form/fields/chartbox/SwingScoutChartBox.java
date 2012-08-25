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
package org.eclipse.scout.rt.ui.swing.form.fields.chartbox;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.WeakEventListener;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.chartbox.ChartBoxEvent;
import org.eclipse.scout.rt.client.ui.form.fields.chartbox.ChartBoxListener;
import org.eclipse.scout.rt.client.ui.form.fields.chartbox.IChartBox;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SingleLayout;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFieldComposite;

public class SwingScoutChartBox extends SwingScoutFieldComposite<IChartBox> implements ISwingScoutChartBox {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutChartBox.class);

  private P_ScoutChartBoxListener m_scoutChartBoxListener;
  // cached
  private ISwingChartProvider m_chartProvider;
  private JComponent m_chartComponent;

  @Override
  protected void initializeSwing() {
    super.initializeSwing();
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    //
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
    container.add(label);
    //
    JPanel chartPanel = new JPanelEx(new SingleLayout());
    chartPanel.setOpaque(false);
    container.add(chartPanel);
    //
    setSwingContainer(container);
    setSwingLabel(label);
    setSwingField(chartPanel);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  @Override
  public JPanel getSwingChartPanel() {
    return (JPanel) getSwingField();
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    if (m_scoutChartBoxListener != null) {
      getScoutObject().removeChartBoxListener(m_scoutChartBoxListener);
      m_scoutChartBoxListener = null;
    }
  }

  /*
   * scout properties
   */
  @Override
  protected void attachScout() {
    super.attachScout();
    if (m_scoutChartBoxListener == null) {
      m_scoutChartBoxListener = new P_ScoutChartBoxListener();
      getScoutObject().addChartBoxListener(m_scoutChartBoxListener);
    }
    IChartBox f = getScoutObject();
    setChartQNameFromScout(f.getChartQName());
  }

  protected void setChartQNameFromScout(String qname) {
    JPanel chartPanel = getSwingChartPanel();
    chartPanel.removeAll();
    m_chartProvider = null;
    m_chartComponent = null;
    if (qname != null) {
      // create chart panel and attach to layout
      try {
        String[] parts = qname.split("/");
        if (parts.length == 2) {
          m_chartProvider = (ISwingChartProvider) Platform.getBundle(parts[0]).loadClass(parts[1]).newInstance();
          m_chartComponent = m_chartProvider.createChart(this);
          chartPanel.add(m_chartComponent);
        }
        else {
          LOG.error("chart qname must be of form: bundle-symbolic-name/class-name");
        }
      }
      catch (Exception e) {
        LOG.error("chart-qname: " + qname, e);
      }
    }
  }

  /**
   * scout property handler override
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IChartBox.PROP_CHART_QNAME)) {
      setChartQNameFromScout((String) newValue);
    }
  }

  private void handleChartBoxChangedInSwing(ChartBoxEvent e) {
    try {
      switch (e.getType()) {
        case ChartBoxEvent.TYPE_DATA_CHANGED: {
          JPanel chartPanel = getSwingChartPanel();
          JComponent newComp = m_chartProvider.refreshChart(m_chartComponent);
          if (newComp != null && newComp != m_chartComponent) {
            m_chartComponent = newComp;
            chartPanel.removeAll();
            chartPanel.add(m_chartComponent);
          }
          chartPanel.repaint();
          break;
        }
      }
    }
    catch (Exception ex) {
      LOG.error("chartBoxEvent=" + e, ex);
    }
  }

  private class P_ScoutChartBoxListener implements ChartBoxListener, WeakEventListener {
    @Override
    public void chartBoxChanged(final ChartBoxEvent e) {
      Runnable t = new Runnable() {
        @Override
        public void run() {
          handleChartBoxChangedInSwing(e);
        }
      };
      getSwingEnvironment().invokeSwingLater(t);
    }
  }// end private class

}
