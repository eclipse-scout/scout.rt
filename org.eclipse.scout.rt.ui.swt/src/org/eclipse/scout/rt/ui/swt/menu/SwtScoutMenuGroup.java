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
package org.eclipse.scout.rt.ui.swt.menu;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.SwtMenuUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 *
 */
public class SwtScoutMenuGroup extends AbstractSwtScoutMenu {

  private Menu m_childMenu;
  private List<AbstractSwtScoutMenu> m_childMenus = new ArrayList<AbstractSwtScoutMenu>();
  private Listener m_uiMenuListener;

  public SwtScoutMenuGroup(IMenu scoutMenu, Menu parentMenu, ISwtEnvironment environment) {
    super(scoutMenu, parentMenu, environment, false);
    createMenu(scoutMenu, parentMenu, environment);
  }

  @Override
  protected void initializeUi(MenuItem swtMenuItem) {
    super.initializeUi(swtMenuItem);
    IMenu scoutMenu = getScoutMenu();
    if (scoutMenu.hasChildActions()) {
      m_childMenu = new Menu(swtMenuItem);
      for (IMenu subMenu : SwtMenuUtility.consolidateMenus(scoutMenu.getChildActions())) {
        AbstractSwtScoutMenu menuItem = SwtMenuUtility.createMenuItem(subMenu, m_childMenu, getEnvironment());
        if (menuItem != null) {
          m_childMenus.add(menuItem);
        }
      }
      swtMenuItem.setMenu(m_childMenu);
      m_uiMenuListener = new P_UiMenuListener();
      m_childMenu.addListener(SWT.Show, m_uiMenuListener);
      m_childMenu.addListener(SWT.Hide, m_uiMenuListener);
      m_childMenu.addListener(SWT.Dispose, m_uiMenuListener);
    }
  }

  protected void handleSwtMenuHide() {
  }

  /**
  *
  */
  protected void handleSwtMenuShow() {
    Runnable t = new Runnable() {
      @Override
      public void run() {
        for (AbstractSwtScoutMenu menuItem : m_childMenus) {
          menuItem.getScoutMenu().prepareAction();
        }
      }
    };
    getEnvironment().invokeScoutLater(t, 0);
    //end notify
  }

  protected void handleSwtMenuDispose() {
    m_childMenu.removeListener(SWT.Show, m_uiMenuListener);
    m_childMenu.removeListener(SWT.Hide, m_uiMenuListener);
    m_childMenu.removeListener(SWT.Dispose, m_uiMenuListener);
    m_uiMenuListener = null;

  }

  private class P_UiMenuListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Show:
          handleSwtMenuShow();
          break;
        case SWT.Hide:
          handleSwtMenuHide();
          break;
        case SWT.Dispose:
          handleSwtMenuDispose();
          break;
      }
    }

  }

}
