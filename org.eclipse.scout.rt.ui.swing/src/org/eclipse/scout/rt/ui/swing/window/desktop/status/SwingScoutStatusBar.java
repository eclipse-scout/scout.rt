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
package org.eclipse.scout.rt.ui.swing.window.desktop.status;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.services.common.perf.IPerformanceAnalyzerService;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.ui.swing.Activator;
import org.eclipse.scout.rt.ui.swing.SwingIcons;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.ext.BorderLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.FlowLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.busy.SwingBusyIndicator;
import org.eclipse.scout.rt.ui.swing.ext.job.SwingProgressHandler;
import org.eclipse.scout.service.SERVICES;

public class SwingScoutStatusBar extends SwingScoutComposite<IDesktop> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutStatusBar.class);

  public static final int VISIBLE_ALWAYS = 0;
  public static final int VISIBLE_WHEN_BUSY = 1;
  public static final int VISIBLE_WHEN_TEXT_OR_BUSY = 2;

  private static final String CARD_LOGO = "logo";
  private static final String CARD_ACTIVITY = "activity";

  private int m_visibilityPolicy;
  private boolean m_jobRunning;
  // items
  private JLabel m_swingStatusLabel;
  private JPanel m_swingCardPanel;
  private JProgressBar m_swingProgressBar;
  private JButton m_swingStopButton;
  private JLabel m_swingNetworkLatency;
  private Icon m_iconNetworkLatencyGreen;
  private Icon m_iconNetworkLatencyYellow;
  private Icon m_iconNetworkLatencyRed;
  //listeners
  private SwingProgressHandler.IStateChangeListener m_progressListener;
  private PropertyChangeListener m_perfListener;

  public SwingScoutStatusBar(int visibilityPolicy) {
    setVisibilityPolicy(visibilityPolicy);
  }

  @Override
  protected void initializeSwing() {
    super.initializeSwing();
    m_iconNetworkLatencyGreen = Activator.getIcon(SwingIcons.NetworkLatencyGreen);
    m_iconNetworkLatencyYellow = Activator.getIcon(SwingIcons.NetworkLatencyYellow);
    m_iconNetworkLatencyRed = Activator.getIcon(SwingIcons.NetworkLatencyRed);
    //
    Icon icon = UIManager.getIcon("StatusBar.StopButton.icon");
    // create button and add notifiers for starting progress listeners
    m_swingStopButton = new JButton(icon);
    m_swingStopButton.putClientProperty(SwingBusyIndicator.BUSY_CLICKABLE_CLIENT_PROPERTY, true);
    m_swingStopButton.setFocusable(false);
    m_swingStopButton.setHorizontalAlignment(SwingConstants.CENTER);
    m_swingStopButton.setMargin(new Insets(0, 4, 0, 3));
    m_swingStopButton.addActionListener(new SwingProgressHandler.CancelJobsAction());
    m_swingStopButton.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    //
    m_swingProgressBar = new JProgressBar(0, 100);
    m_swingProgressBar.setOpaque(false);
    m_swingProgressBar.setBorder(new EmptyBorder(1, 2, 1, 2));
    m_swingProgressBar.setStringPainted(true);
    m_swingProgressBar.setString("");
    m_swingProgressBar.setIndeterminate(true);
    //
    m_swingStatusLabel = new JLabel("Ok");
    m_swingStatusLabel.setOpaque(false);
    // create layout
    // logo card
    JPanel cardLogo = new JPanelEx(new FlowLayoutEx(FlowLayoutEx.RIGHT, 0, 0));
    cardLogo.setOpaque(false);
    icon = UIManager.getIcon("StatusBar.icon");
    if (icon != null) {
      JLabel logo = new JLabel();
      logo.setIcon(icon);
      cardLogo.add(logo);
    }
    m_swingNetworkLatency = new JLabel();
    m_swingNetworkLatency.setPreferredSize(new Dimension(16, 22));
    cardLogo.add(m_swingNetworkLatency);
    // activity card
    JPanel cardActivity = new JPanelEx(new FlowLayoutEx(FlowLayoutEx.RIGHT, 4, 0));
    cardActivity.add(m_swingProgressBar);
    cardActivity.add(m_swingStopButton);
    m_swingCardPanel = new JPanelEx(new CardLayout());
    m_swingCardPanel.add(cardLogo, CARD_LOGO);
    m_swingCardPanel.add(cardActivity, CARD_ACTIVITY);
    JPanel bar = new P_SwingStatusBarPanel();
    bar.setLayout(new BorderLayoutEx());
    bar.add(m_swingStatusLabel, BorderLayoutEx.CENTER);
    bar.add(m_swingCardPanel, BorderLayoutEx.EAST);
    showCard(CARD_LOGO);
    // adjust sizes
    int h = UIManager.getInt("StatusBar.height");
    if (h <= 0) {
      h = 29;
    }
    bar.setMinimumSize(new Dimension(0, h));
    bar.setPreferredSize(new Dimension(800, h));
    bar.setMaximumSize(new Dimension(2048, h));
    setSwingField(bar);
  }

  public JPanel getSwingStatusBar() {
    return (JPanel) getSwingField();
  }

  public void setVisibilityPolicy(int p) {
    switch (p) {
      case VISIBLE_ALWAYS:
      case VISIBLE_WHEN_BUSY:
      case VISIBLE_WHEN_TEXT_OR_BUSY:
        break;
      default:
        throw new IllegalArgumentException("Invalid visibilityPolicy " + p);
    }
    m_visibilityPolicy = p;
  }

  public int getVisibilityPolicy() {
    return m_visibilityPolicy;
  }

  private void showCard(String card) {
    if (m_swingCardPanel != null) {
      CardLayout l = (CardLayout) m_swingCardPanel.getLayout();
      l.show(m_swingCardPanel, card);
    }
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    setStatusFromScout();
    updateFieldVisibilities();
    //add listeners
    if (m_progressListener == null) {
      m_progressListener = new SwingProgressHandler.IStateChangeListener() {
        @Override
        public void stateChanged(SwingProgressHandler handler) {
          handleProgressChangeInSwingThread(handler);
        }
      };
      SwingProgressHandler progressHandler = SwingProgressHandler.getInstance();
      if (progressHandler != null) {
        progressHandler.addStateChangeListener(m_progressListener);
      }
    }
    if (m_perfListener == null) {
      m_perfListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
          if (IPerformanceAnalyzerService.PROP_NETWORK_LATENCY.equals(e.getPropertyName())) {
            handleNetworkLatencyChanged((Long) e.getNewValue());
          }
        }
      };
      IPerformanceAnalyzerService perf = SERVICES.getService(IPerformanceAnalyzerService.class);
      if (perf != null) {
        perf.addPropertyChangeListener(m_perfListener);
      }
    }
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    //remove listeners
    if (m_progressListener != null) {
      SwingProgressHandler progressHandler = SwingProgressHandler.getInstance();
      if (progressHandler != null) {
        progressHandler.removeStateChangeListener(m_progressListener);
      }
      m_progressListener = null;
    }
    if (m_perfListener != null) {
      IPerformanceAnalyzerService perf = SERVICES.getService(IPerformanceAnalyzerService.class);
      if (perf != null) {
        perf.removePropertyChangeListener(m_perfListener);
      }
      m_perfListener = null;
    }
  }

  /*
   * extended property observer
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IDesktop.PROP_STATUS)) {
      setStatusFromScout();
    }
  }

  protected void handleNetworkLatencyChanged(final Long value) {
    if (m_swingNetworkLatency != null) {
      Icon icon = null;
      if (value <= 70) {
        icon = m_iconNetworkLatencyGreen;
      }
      else if (value <= 200) {
        icon = m_iconNetworkLatencyYellow;
      }
      else {
        icon = m_iconNetworkLatencyRed;
      }
      final Icon iconf = icon;
      SwingUtilities.invokeLater(
          new Runnable() {
            @Override
            public void run() {
              m_swingNetworkLatency.setToolTipText(SwingUtility.getNlsText("NetworkLatency") + " " + value + "ms");
              if (iconf != m_swingNetworkLatency.getIcon()) {
                m_swingNetworkLatency.setIcon(iconf);
              }
            }
          }
          );
    }
  }

  private void handleProgressChangeInSwingThread(SwingProgressHandler h) {
    m_jobRunning = h.isJobRunning();
    if (m_swingProgressBar != null) {
      Integer newValue = null;
      String newText = null;
      if (h.isJobRunning()) {
        // percent
        double f = h.getJobWorked();
        if (f <= 0) {
          newValue = null;
        }
        else {
          newValue = (int) (f * 100);
        }
        // text
        newText = h.getJobTaskName();
        if (newText == null || newText.length() == 0) {
          if (m_swingProgressBar.getValue() > 0) {
            newText = null;// auto-display of '%'
          }
          else {
            newText = "";
          }
        }
        else {
          m_swingProgressBar.setStringPainted(true);
        }
      }
      else {
        newText = "";
        newValue = null;
      }
      //
      m_swingProgressBar.setString(newText);
      m_swingProgressBar.setIndeterminate(newValue == null);
      m_swingProgressBar.setValue(newValue != null ? newValue.intValue() : 0);
    }
    updateFieldVisibilities();
  }

  protected void setStatusFromScout() {
    setSwingStatus(getScoutObject().getStatus());
  }

  public void setSwingStatus(IProcessingStatus newStatus) {
    String newText = null;
    Icon newIcon = null;
    if (newStatus != null && !StringUtility.isNullOrEmpty(newStatus.getMessage())) {
      // text
      newText = newStatus.getMessage();
      // icon
      switch (newStatus.getSeverity()) {
        case IProcessingStatus.ERROR:
        case IProcessingStatus.FATAL: {
          newIcon = getSwingEnvironment().getIcon(AbstractIcons.StatusError);
          break;
        }
        case IProcessingStatus.WARNING: {
          newIcon = getSwingEnvironment().getIcon(AbstractIcons.StatusWarning);
          break;
        }
        case IProcessingStatus.INFO: {
          newIcon = getSwingEnvironment().getIcon(AbstractIcons.StatusInfo);
          break;
        }
      }
    }
    m_swingStatusLabel.setText(newText);
    m_swingStatusLabel.setIcon(newIcon);
    updateFieldVisibilities();
  }

  protected void updateFieldVisibilities() {
    boolean hasJob = m_jobRunning;
    boolean hasStatus = (m_swingStatusLabel.getText() != null && m_swingStatusLabel.getText().length() > 0);
    boolean hasAny = (hasJob || hasStatus);
    //
    boolean panelVisible = true;
    switch (m_visibilityPolicy) {
      case VISIBLE_ALWAYS: {
        panelVisible = true;
        break;
      }
      case VISIBLE_WHEN_BUSY:
      case VISIBLE_WHEN_TEXT_OR_BUSY: {
        panelVisible = hasAny;
        break;
      }
    }
    // bar
    getSwingStatusBar().setVisible(panelVisible);
    showCard(hasAny ? CARD_ACTIVITY : CARD_LOGO);
    // status
    m_swingStatusLabel.setVisible(hasStatus);
    // progress
    m_swingProgressBar.setVisible(hasJob);
    // stop button
    m_swingStopButton.setVisible(hasJob);
    m_swingStopButton.setEnabled(true);
  }

  /**
   * Paints the background of the status bar. Rootpanel for all statusbar
   * components.
   */
  private class P_SwingStatusBarPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public P_SwingStatusBarPanel() {
      setOpaque(false);
      setBorder(new EmptyBorder(4, 9, 0, 9));
    }

    @Override
    public void paint(Graphics g) {
      // paint gradient
      Graphics2D g2d = (Graphics2D) g;
      g2d.setPaint(new GradientPaint(new Point(0, 0), new Color(0xB7B7B7),
          new Point(0, 4), new Color(0xF4F4F4)));
      g2d.fillRect(0, 0, getWidth(), 4);
      super.paint(g);
    }
  } // end of private class
}
