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

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SpringLayout;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.navigation.INavigationHistoryService;
import org.eclipse.scout.rt.client.ui.desktop.navigation.NavigationHistoryEvent;
import org.eclipse.scout.rt.client.ui.desktop.navigation.NavigationHistoryListener;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.ui.swing.SwingPopupWorker;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.focus.SwingScoutFocusTraversalPolicy;
import org.eclipse.scout.rt.ui.swing.window.desktop.ProgressHandler;
import org.eclipse.scout.service.SERVICES;

public class SwingScoutToolBar extends SwingScoutComposite<IDesktop> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutToolBar.class);
  private static final long serialVersionUID = 1L;
  private JNavigationWidget m_navigationWidget;
  private JLabel m_windowIcons;
  private static final int HEIGHT = 85;

  private int m_topLevelMenuCount;
  private JViewTabsBar m_viewTabPanel;
  private JToolTabsBar m_toolTabsPanel;
  //
  private NavigationHistoryListener m_scoutNavListener;
  private ProgressHandler m_progressHandler;

  public SwingScoutToolBar() {
  }

  @Override
  protected void initializeSwing() {
    SpringLayout layout = new SpringLayout();

    m_topLevelMenuCount = getScoutObject().getMenus().length;
    JPanelEx toolBar = new JPanelEx(layout);
    SwingUtility.installFocusCycleRoot(toolBar, new SwingScoutFocusTraversalPolicy());

    m_navigationWidget = new JNavigationWidget(getSwingEnvironment());
    toolBar.add(m_navigationWidget);
    layout.putConstraint(SpringLayout.WEST, m_navigationWidget, 9, SpringLayout.WEST, toolBar);
    layout.putConstraint(SpringLayout.NORTH, m_navigationWidget, 11, SpringLayout.NORTH, toolBar);

    // back
    m_navigationWidget.getBackButton().setPrimaryAction(new P_BackAction());

    // foward & history menu
    m_navigationWidget.getForwardButton().setPrimaryAction(new P_ForwardAction());
    m_navigationWidget.getForwardButton().setSecondaryAction(new P_HistoryAction());

    // refresh & stop
    m_progressHandler = new ProgressHandler(getSwingEnvironment());
    m_navigationWidget.getStopRefreshButton().setPrimaryAction(new P_RefreshAction());
    m_navigationWidget.getStopRefreshButton().setSecondaryAction(m_progressHandler.createStopAction());

    JComponent logo = getSwingEnvironment().createLogo();
    toolBar.add(logo);
    layout.putConstraint(SpringLayout.NORTH, logo, 0, SpringLayout.NORTH, toolBar);
    layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, logo, 0, SpringLayout.HORIZONTAL_CENTER, toolBar);

    m_viewTabPanel = new JViewTabsBar(getSwingEnvironment());
    toolBar.add(m_viewTabPanel);
    layout.putConstraint(SpringLayout.SOUTH, m_viewTabPanel, 0, SpringLayout.SOUTH, toolBar);
    layout.putConstraint(SpringLayout.WEST, m_viewTabPanel, 0, SpringLayout.WEST, toolBar);

    m_toolTabsPanel = new JToolTabsBar(getSwingEnvironment());
    toolBar.add(m_toolTabsPanel);
    layout.putConstraint(SpringLayout.EAST, m_toolTabsPanel, 0, SpringLayout.EAST, toolBar);
    layout.putConstraint(SpringLayout.SOUTH, m_toolTabsPanel, 0, SpringLayout.SOUTH, toolBar);
    layout.putConstraint(SpringLayout.EAST, m_viewTabPanel, 0, SpringLayout.WEST, m_toolTabsPanel);

    toolBar.setOpaque(true);
    toolBar.setBackground(new Color(207, 226, 239));//XXX
    toolBar.setPreferredSize(new Dimension(-1, HEIGHT));
    toolBar.setBounds(0, 0, toolBar.getWidth(), HEIGHT);

    setSwingField(toolBar);
    rebuildViewTabs();
    rebuildToolTabs();
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
        new SwingPopupWorker(getSwingEnvironment(), m_navigationWidget, m_navigationWidget.getHistoryMenuLocation(), scoutMenus).enqueue();
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

  public JComponent getSwingToolBar() {
    return getSwingField();
  }

  public JComponent getSwingToolTabsPanel() {
    return m_toolTabsPanel;
  }

  private void updateNavigationWidget(boolean backEnabled, boolean forwardEnabled, boolean menuEnabled) {
    m_navigationWidget.getBackButton().setEnabled(backEnabled);
    m_navigationWidget.getForwardButton().setEnabled(forwardEnabled);
    m_navigationWidget.getForwardButton().setHistoryEnabled(menuEnabled);
  }

  private void rebuildViewTabs() {
    m_viewTabPanel.rebuild(getScoutObject());
  }

  private void rebuildToolTabs() {
    m_toolTabsPanel.rebuild(getScoutObject());
  }

  public JNavigationWidget getSwingNavigationWidget() {
    return m_navigationWidget;
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

    public void actionPerformed(ActionEvent e) {
      handleNavigationBackFromSwing();
    }
  }// end class
}
