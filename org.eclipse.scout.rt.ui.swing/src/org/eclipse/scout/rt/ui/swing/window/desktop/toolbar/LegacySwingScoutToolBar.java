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

import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.navigation.INavigationHistoryService;
import org.eclipse.scout.rt.client.ui.desktop.navigation.NavigationHistoryEvent;
import org.eclipse.scout.rt.client.ui.desktop.navigation.NavigationHistoryListener;
import org.eclipse.scout.rt.ui.swing.Activator;
import org.eclipse.scout.rt.ui.swing.SwingIcons;
import org.eclipse.scout.rt.ui.swing.SwingPopupWorker;
import org.eclipse.scout.rt.ui.swing.action.ISwingScoutAction;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.ext.BorderLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.FlowLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.JButtonEx;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.service.SERVICES;

public class LegacySwingScoutToolBar extends SwingScoutComposite<IDesktop> {

  private int m_topLevelMenuCount;
  private JButton m_navBackButton;
  private JButton m_navFwdButton;
  private JButton m_navMenuButton;
  private JPanelEx m_viewTabPanel;
  private JPanelEx m_toolTabPanel;
  //
  private NavigationHistoryListener m_scoutNavListener;

  public LegacySwingScoutToolBar() {
  }

  @Override
  protected void initializeSwing() {
    m_topLevelMenuCount = getScoutObject().getMenus().length;
    JToolBar toolBar = new JToolBar();
    toolBar.setLayout(new BorderLayoutEx(0, 0));
    toolBar.setMargin(new Insets(4, 4, 4, 4));
    toolBar.setFloatable(false);
    toolBar.setRollover(true);
    //
    JPanelEx navigationPanel = new JPanelEx(new FlowLayoutEx(FlowLayoutEx.LEFT, 0, 0));
    navigationPanel.setOpaque(false);
    navigationPanel.add(m_navBackButton = createNavigationButton(SwingIcons.NavigationBackward, new AbstractAction() {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        handleNavigationBackFromSwing();
      }
    }));
    navigationPanel.add(m_navFwdButton = createNavigationButton(SwingIcons.NavigationForward, new AbstractAction() {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        handleNavigationForwardFromSwing();
      }
    }));
    navigationPanel.add(m_navMenuButton = createNavigationButton(SwingIcons.NavigationMenu, new AbstractAction() {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        handleNavigationMenuFromSwing();
      }
    }));
    //
    m_viewTabPanel = new JPanelEx(new FlowLayoutEx(FlowLayoutEx.LEFT, 0, 0));
    m_viewTabPanel.setOpaque(false);
    //
    m_toolTabPanel = new JPanelEx(new FlowLayoutEx(FlowLayoutEx.LEFT, 0, 0));
    m_toolTabPanel.setOpaque(false);
    //
    toolBar.add(navigationPanel, BorderLayoutEx.LINE_START);
    toolBar.add(m_toolTabPanel, BorderLayoutEx.CENTER);
    toolBar.add(m_viewTabPanel, BorderLayoutEx.LINE_END);
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
        new SwingPopupWorker(getSwingEnvironment(), m_navMenuButton, new Point(0, 0), scoutMenus).enqueue();
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
        updateNavigationTab(backEnabled, forewardEnabled, menuEnabled);
      }
    };
    getSwingEnvironment().invokeSwingLater(t);
    // end notify
  }

  public boolean isEmpty() {
    return m_topLevelMenuCount == 0;
  }

  public JToolBar getSwingToolBar() {
    return (JToolBar) getSwingField();
  }

  public JPanel getSwingToolsPanel() {
    return m_toolTabPanel;
  }

  private void updateNavigationTab(boolean backEnabled, boolean forewardEnabled, boolean menuEnabled) {
    m_navBackButton.setEnabled(backEnabled);
    m_navFwdButton.setEnabled(forewardEnabled);
    m_navMenuButton.setEnabled(menuEnabled);
  }

  private void rebuildViewTabs() {
    m_viewTabPanel.removeAll();
    for (IAction a : getScoutObject().getViewButtons()) {
      ISwingScoutAction button = getSwingEnvironment().createAction(m_viewTabPanel, a);
      if (button != null) {
        m_viewTabPanel.add(button.getSwingField());
      }
    }
  }

  private void rebuildToolTabs() {
    m_toolTabPanel.removeAll();
    for (IAction a : getScoutObject().getToolButtons()) {
      ISwingScoutAction button = getSwingEnvironment().createAction(m_toolTabPanel, a);
      if (button != null) {
        m_toolTabPanel.add(button.getSwingField());
      }
    }
  }

  private JButton createNavigationButton(String iconId, Action a) {
    JButtonEx b = new JButtonEx(a);
    b.setIcon(Activator.getIcon(iconId));
    b.setDisabledIcon(Activator.getIcon(iconId + "_disabled"));
    b.setPressedIcon(Activator.getIcon(iconId + "_pressed"));
    b.setRolloverIcon(Activator.getIcon(iconId + "_rollover"));
    b.setOpaque(true);
    b.setContentAreaFilled(false);
    b.setBorder(null);
    // turn off native hover effect
    b.setBorderPainted(false);
    b.setFocusPainted(false);
    return b;
  }
}
