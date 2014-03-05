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
package org.eclipse.scout.rt.ui.swt.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 *
 */
public class SwtScoutMenuItem extends AbstractSwtScoutMenu {

  private List<SwtScoutMenuItem> m_childMenus = new ArrayList<SwtScoutMenuItem>();
  private Menu m_childMenu;
  private boolean m_handleSelectionPending;
  private Listener m_swtSelectionListener;

  /**
   * @param scoutMenu
   * @param parentMenu
   * @param environment
   */
  public SwtScoutMenuItem(IMenu scoutMenu, Menu parentMenu, ISwtEnvironment environment) {
    super(scoutMenu, parentMenu, environment, false);
    createMenu(scoutMenu, parentMenu, environment);
  }

  @Override
  protected void initializeUi(MenuItem swtMenuItem) {
    super.initializeUi(swtMenuItem);
    m_swtSelectionListener = new P_SwtMenuListener();
    swtMenuItem.addListener(SWT.Selection, m_swtSelectionListener);
  }

  @Override
  protected void attachScout() {
    super.attachScout();

  }

  @Override
  protected void handleSwtMenuItemDispose() {
    getSwtMenuItem().removeListener(SWT.Selection, m_swtSelectionListener);
    m_swtSelectionListener = null;
    super.handleSwtMenuItemDispose();
  }

  protected void handleSwtMenuSelection() {
    if (!m_handleSelectionPending) {
      m_handleSelectionPending = true;
      //notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          try {
            getScoutMenu().getUIFacade().fireActionFromUI();
          }
          finally {
            m_handleSelectionPending = false;
          }
        }
      };
      getEnvironment().invokeScoutLater(t, 0);
      //end notify
    }
  }

  private class P_SwtMenuListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Selection:
          handleSwtMenuSelection();
          break;
      }
    }

  }

}
