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
package org.eclipse.scout.rt.ui.swing.window.desktop.toolbar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.UIManager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.navigation.INavigationHistoryService;
import org.eclipse.scout.rt.client.ui.desktop.navigation.NavigationHistoryEvent;
import org.eclipse.scout.rt.client.ui.desktop.navigation.NavigationHistoryListener;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.ui.swing.SwingPopupWorker;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.job.SwingProgressHandler;
import org.eclipse.scout.rt.ui.swing.window.desktop.toolbar.internal.JNavigationPanel;
import org.eclipse.scout.rt.ui.swing.window.desktop.toolbar.internal.JToolTabsBar;
import org.eclipse.scout.rt.ui.swing.window.desktop.toolbar.internal.JViewTabsBar;
import org.eclipse.scout.service.SERVICES;

public class SwingScoutHeaderPanel extends SwingScoutComposite<IDesktop> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutHeaderPanel.class);
  private static final long serialVersionUID = 1L;
  private static final int DISTANCE_NAVIGATION_TABS = 2;
  protected AbstractJNavigationPanel m_navigationPanel;
  private JLabel m_windowIcons;

  private int m_topLevelMenuCount;
  protected AbstractJViewTabsBar m_viewTabsPanel;
  protected AbstractJToolTabsBar m_toolTabsPanel;
  protected JComponent m_logo;
  private NavigationHistoryListener m_scoutNavListener;

  protected final SpringLayout m_layout;

  public SwingScoutHeaderPanel() {
    m_layout = new SpringLayout();
  }

  @Override
  protected void initializeSwing() {
    m_topLevelMenuCount = getScoutObject().getMenus().length;
    final JPanelEx container = new JPanelEx(m_layout);

    m_navigationPanel = createNavigationPanel();
    container.add(m_navigationPanel);

    // navigation buttons and tool buttons (only buttons that are not of the type @{link AbstractFormToolButton} are considered)
    m_navigationPanel.setBackAction(new P_BackAction());
    m_navigationPanel.setForwardAction(new P_ForwardAction());
    m_navigationPanel.setHistoryAction(new P_HistoryAction());
    m_navigationPanel.setRefreshAction(new P_RefreshAction());
    m_navigationPanel.setStopAction(new SwingProgressHandler.CancelJobsAction());
    m_layout.putConstraint(SpringLayout.WEST, m_navigationPanel, 0, SpringLayout.WEST, container);
    m_layout.putConstraint(SpringLayout.NORTH, m_navigationPanel, 0, SpringLayout.NORTH, container);

    // view tabs
    m_viewTabsPanel = createViewTabsBar();
    container.add(m_viewTabsPanel);
    m_layout.putConstraint(SpringLayout.NORTH, m_viewTabsPanel, DISTANCE_NAVIGATION_TABS, SpringLayout.SOUTH, m_navigationPanel);
    m_layout.putConstraint(SpringLayout.SOUTH, m_viewTabsPanel, 0, SpringLayout.SOUTH, container);
    m_layout.putConstraint(SpringLayout.WEST, m_viewTabsPanel, 0, SpringLayout.WEST, container);
    m_layout.putConstraint(SpringLayout.EAST, m_viewTabsPanel, 0, SpringLayout.EAST, container);

    // tool buttons (only buttons of the type @{link AbstractFormToolButton} are considered)
    m_toolTabsPanel = createToolTabsBar();
    m_toolTabsPanel.setSwingScoutHeaderPanel(this);
    container.add(m_toolTabsPanel);
    m_layout.putConstraint(SpringLayout.NORTH, m_toolTabsPanel, 0, SpringLayout.NORTH, m_viewTabsPanel);
    m_layout.putConstraint(SpringLayout.SOUTH, m_toolTabsPanel, 0, SpringLayout.SOUTH, container);
    m_layout.putConstraint(SpringLayout.EAST, m_toolTabsPanel, 0, SpringLayout.EAST, container);

    // logo
    m_logo = getSwingEnvironment().createLogo();
    if (m_logo != null) {
      container.add(m_logo);

      // vertical alignment
      int vAlignment = UIManager.getInt("HeaderPanel.logoVerticalAlignment");
      switch (vAlignment) {
        case 0: { // center
          m_layout.putConstraint(SpringLayout.NORTH, m_logo, m_viewTabsPanel.getPreferredSize().height * -1, SpringLayout.VERTICAL_CENTER, container);
          break;
        }
        case 1: { // bottom
          m_layout.putConstraint(SpringLayout.SOUTH, m_logo, 0, SpringLayout.NORTH, m_viewTabsPanel);
          break;
        }
        default: { // top
          m_layout.putConstraint(SpringLayout.NORTH, m_logo, 0, SpringLayout.NORTH, container);
          break;
        }
      }

      // horizontal alignment
      int hAlignment = UIManager.getInt("HeaderPanel.logoHorizontalAlignment");
      if (hAlignment == 1) { // east
        m_layout.putConstraint(SpringLayout.EAST, m_logo, 0, SpringLayout.EAST, container);
      }
      else { // center
        m_layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, m_logo, 0, SpringLayout.HORIZONTAL_CENTER, container);
      }

      // register listener to hide logo if overlapping navigation panel
      container.addComponentListener(new ComponentAdapter() {

        private final OptimisticLock m_syncLock = new OptimisticLock();

        @Override
        public void componentResized(ComponentEvent e) {
          try {
            if (m_syncLock.acquire()) {
              // calculate whether logo overlaps navigation panel
              boolean overlapping = m_navigationPanel.getBounds().x + m_navigationPanel.getBounds().width > m_logo.getBounds().x;

              if (overlapping && m_logo.isVisible()) {
                m_logo.setVisible(false);
              }
              else if (!overlapping && !m_logo.isVisible()) {
                m_logo.setVisible(true);
              }
            }
          }
          finally {
            m_syncLock.release();
          }
        }
      });
    }

    Color color = UIManager.getColor("HeaderPanel.background");
    if (color != null) {
      container.setOpaque(true);
      container.setBackground(color);
    }

    int height = UIManager.getInt("HeaderPanel.height");
    if (height > 0) {
      container.setPreferredSize(new Dimension(-1, height));
    }
    else {
      // register listener to calculate panel height
      container.addComponentListener(new ComponentAdapter() {

        private int m_height;
        private final OptimisticLock m_syncLock = new OptimisticLock();

        @Override
        public void componentResized(ComponentEvent e) {
          try {
            if (m_syncLock.acquire()) {
              int newHeight = calculatePanelHeight();
              if (m_height != newHeight) {
                m_height = newHeight;
                container.setPreferredSize(new Dimension(-1, m_height));
              }
            }
          }
          finally {
            m_syncLock.release();
          }
        }

        private int calculatePanelHeight() {
          double heightNavigationPanel = m_navigationPanel.getPreferredSize().getHeight();
          double heightViewTabPanel = m_viewTabsPanel.getPreferredSize().getHeight();
          double heightToolTabsPanel = m_toolTabsPanel.getPreferredSize().getHeight();
          double heightLogoPanel = 0;
          if (m_logo != null) {
            heightLogoPanel = m_logo.getPreferredSize().getHeight();
          }

          double heightTopPanel = Math.max(heightNavigationPanel, heightLogoPanel);
          double heightBottomPanel = Math.max(heightViewTabPanel, heightToolTabsPanel);
          return (int) (heightTopPanel + DISTANCE_NAVIGATION_TABS + heightBottomPanel);
        }

      });
    }

    setSwingField(container);
    rebuildViewTabs();
    rebuildToolTabs();
    rebuildNavigationWidget();
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    //add listener and init values
    new ClientSyncJob("add navigation listener", getSwingEnvironment().getScoutSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        if (m_scoutNavListener == null) {
          m_scoutNavListener = new NavigationHistoryListener() {
            @Override
            public void navigationChanged(NavigationHistoryEvent e) {
              handleNavigationChangedFromScout();
            }
          };
          SERVICES.getService(INavigationHistoryService.class).addNavigationHistoryListener(m_scoutNavListener);
          handleNavigationChangedFromScout();
        }
      }
    }.schedule();
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    //add listener and init values
    new ClientSyncJob("remove navigation listener", getSwingEnvironment().getScoutSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        if (m_scoutNavListener != null) {
          INavigationHistoryService nav = SERVICES.getService(INavigationHistoryService.class);
          if (nav != null) {
            nav.removeNavigationHistoryListener(m_scoutNavListener);
          }
          m_scoutNavListener = null;
        }
      }
    }.schedule();
  }

  @Override
  protected boolean isHandleScoutPropertyChange(String name, Object newValue) {
    return false;
  }

  /**
   * runs in ui thread
   */
  protected void handleNavigationBackFromSwing() {
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        try {
          SERVICES.getService(INavigationHistoryService.class).stepBackward();
        }
        catch (ProcessingException e) {
          //nop
        }
      }
    };

    getSwingEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

  /**
   * runs in ui thread
   */
  protected void handleNavigationForwardFromSwing() {
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        try {
          SERVICES.getService(INavigationHistoryService.class).stepForward();
        }
        catch (ProcessingException e) {
          //nop
        }
      }
    };

    getSwingEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

  /**
   * runs in ui thread
   */
  protected void handleNavigationMenuFromSwing() {
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        IMenu[] scoutMenus = SERVICES.getService(INavigationHistoryService.class).getMenus();
        // call swing menu
        new SwingPopupWorker(getSwingEnvironment(), m_navigationPanel, m_navigationPanel.getHistoryMenuLocation(), scoutMenus).enqueue();
      }
    };
    getSwingEnvironment().invokeScoutLater(t, 5678);
    // end notify
  }

  /**
   * runs in scout job (thread)
   */
  protected void handleNavigationChangedFromScout() {
    INavigationHistoryService service = SERVICES.getService(INavigationHistoryService.class);
    final boolean backEnabled = service.hasBackwardBookmarks();
    final boolean forewardEnabled = service.hasForwardBookmarks();
    final boolean menuEnabled = backEnabled || forewardEnabled;
    // notify UI
    Runnable t = new Runnable() {
      @Override
      public void run() {
        updateNavigationWidget(backEnabled, forewardEnabled, menuEnabled);
      }
    };
    getSwingEnvironment().invokeSwingLater(t);
    // end notify
  }

  public boolean isEmpty() {
    return m_topLevelMenuCount == 0;
  }

  public AbstractJToolTabsBar getSwingToolTabsPanel() {
    return m_toolTabsPanel;
  }

  /**
   * Update layout of {@link RayoToolTabsBar} tool button bar to have the very same size as the tool bar itself
   * 
   * @param width
   *          the new width to be set. Thereby, the with is only set if is different to the old width.
   * @param force
   *          to force the panel width to be updated also if the given value is the same
   */
  public void adjustToolButtonPanelWidth(int width, boolean force) {
    // it is crucial to compare the new value against the current value hold by the layout manager and not the size of the toolTabsPanel itself.
    // This is because the toolTabsPanel might have the correct size, but the layout manager was never told about.
    Spring constraintLeft = m_layout.getConstraint(SpringLayout.WEST, m_toolTabsPanel);
    Spring constraintRigth = m_layout.getConstraint(SpringLayout.EAST, m_toolTabsPanel);
    int currentWidth = (constraintRigth.getValue() - constraintLeft.getValue());

    if (force || width != currentWidth) {
      // adjust width of tool button bar to be equals to the tool bar width
      m_layout.putConstraint(SpringLayout.WEST, m_toolTabsPanel, width * -1, SpringLayout.EAST, getSwingField());

      // set maximum width to outline tabs in order to not obscure tool buttons
      m_layout.putConstraint(SpringLayout.EAST, m_viewTabsPanel, width * -1, SpringLayout.EAST, getSwingField());

      // revalidate layout to immediately reflect changed layout
      getSwingField().revalidate();
    }
  }

  private void updateNavigationWidget(boolean backEnabled, boolean forwardEnabled, boolean menuEnabled) {
    m_navigationPanel.getBackAction().setEnabled(backEnabled);
    m_navigationPanel.getForwardAction().setEnabled(forwardEnabled);
    m_navigationPanel.getHistoryAction().setEnabled(menuEnabled);
  }

  private void rebuildViewTabs() {
    m_viewTabsPanel.rebuild(getScoutObject());
  }

  private void rebuildToolTabs() {
    m_toolTabsPanel.rebuild(getScoutObject());
  }

  private void rebuildNavigationWidget() {
    m_navigationPanel.rebuild(getScoutObject());
  }

  public AbstractJNavigationPanel getSwingNavigationWidget() {
    return m_navigationPanel;
  }

  /**
   * <p>
   * To be overwritten to install a custom navigation panel.
   * </p>
   * This panel holds navigation buttons like 'back', 'forward', 'stop' and 'refresh'.
   * 
   * @return
   */
  protected AbstractJNavigationPanel createNavigationPanel() {
    return new JNavigationPanel(getSwingEnvironment());
  }

  /**
   * <p>
   * To be overwritten to install a custom tool tab bar.
   * </p>
   * This bar holds the {@link IToolButton} configured on desktop.
   * 
   * @param swingScoutToolBar
   * @return
   */
  protected AbstractJToolTabsBar createToolTabsBar() {
    return new JToolTabsBar(getSwingEnvironment());
  }

  /**
   * <p>
   * To be overwritten to install a custom view tab bar.
   * </p>
   * This bar holds the {@link IViewButton} configured on desktop.
   * 
   * @return
   */
  protected AbstractJViewTabsBar createViewTabsBar() {
    return new JViewTabsBar(getSwingEnvironment());
  }

  private class P_RefreshAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    @Override
    public void actionPerformed(ActionEvent evt) {
      Runnable r = new Runnable() {
        @Override
        public void run() {
          IOutline outline = getScoutObject().getOutline();
          if (outline != null) {
            IPage page = outline.getActivePage();
            if (page != null) {
              try {
                page.reloadPage();
              }
              catch (ProcessingException e) {
                LOG.error("reloading page " + page, e);
              }
            }
          }
        }
      };
      getSwingEnvironment().invokeScoutLater(r, 0);
    }
  } // end class

  private class P_ForwardAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    @Override
    public void actionPerformed(ActionEvent e) {
      handleNavigationForwardFromSwing();
    }
  } // end class

  private class P_HistoryAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    @Override
    public void actionPerformed(ActionEvent e) {
      handleNavigationMenuFromSwing();
    }
  } // end class

  private class P_BackAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    @Override
    public void actionPerformed(ActionEvent e) {
      handleNavigationBackFromSwing();
    }
  }// end class
}
