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
package org.eclipse.scout.rt.ui.rap.action.menu;

import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IContextMenu;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 *
 */
public class RwtScoutContextMenu implements IRwtScoutMenu {

  public static final String DATA_SYSTEM_MENU = "dataSystemMenu";

  protected final BasicPropertySupport m_propertySupport;
  private final IRwtEnvironment m_environment;
  private final Shell m_parentShell;
  private Menu m_uiMenu;

  private IContextMenu m_scoutContextMenu;
  private Listener m_uiMenuListener;
  private Boolean m_childrenCreated = Boolean.valueOf(false);

  private final IRwtContextMenuMarker m_menuMarker;

  public RwtScoutContextMenu(Shell parentShell, IContextMenu scoutContextMenu, IRwtContextMenuMarker menuMarker, IRwtEnvironment environment) {
    this(parentShell, scoutContextMenu, menuMarker, environment, true);
  }

  public RwtScoutContextMenu(Shell parentShell, IContextMenu scoutContextMenu, IRwtContextMenuMarker menuMarker, IRwtEnvironment environment, boolean callInitializer) {
    m_parentShell = parentShell;
    m_scoutContextMenu = scoutContextMenu;
    m_menuMarker = menuMarker;
    if (m_menuMarker != null) {
      m_menuMarker.addSelectionListener(new SelectionAdapter() {
        private static final long serialVersionUID = 1L;

        @Override
        public void widgetSelected(SelectionEvent e) {
          if (e.widget instanceof Control) {
            Point loc = ((Control) e.widget).toDisplay(e.x, e.y);
            m_uiMenu.setLocation(RwtMenuUtility.getMenuLocation(getScoutContextMenu().getChildActions(), m_uiMenu, loc, getEnvironment()));
          }
          m_uiMenu.setVisible(true);
        }
      });
    }
    m_environment = environment;
    m_propertySupport = new BasicPropertySupport(this);
    if (callInitializer) {
      initMenu();
    }
  }

  protected void initMenu() {
    m_uiMenu = new Menu(getParentShell(), SWT.POP_UP);
    // listeners
    m_uiMenuListener = new P_UiMenuListener();
    m_uiMenu.addListener(SWT.Show, m_uiMenuListener);
    m_uiMenu.addListener(SWT.Hide, m_uiMenuListener);
    m_uiMenu.addListener(SWT.Dispose, m_uiMenuListener);
  }

  protected void disposeChildren() {
    synchronized (m_childrenCreated) {
      if (m_childrenCreated) {
        try {
          for (MenuItem item : getUiMenu().getItems()) {
            if (item.getData(DATA_SYSTEM_MENU) == null) {
              item.dispose();
            }
          }
        }
        finally {
          m_childrenCreated = Boolean.valueOf(false);
        }
      }
    }
  }

  protected void ensureChildrenLoaded() {
    synchronized (m_childrenCreated) {
      if (!m_childrenCreated) {
        try {
          RwtMenuUtility.fillMenu(getUiMenu(), getScoutContextMenu().getChildActions(), ActionUtility.createMenuFilterVisibleAvailable(), getEnvironment());

        }
        finally {
          m_childrenCreated = Boolean.valueOf(true);
        }
      }
    }
  }

  @Override
  public IRwtEnvironment getEnvironment() {
    return m_environment;
  }

  @Override
  public IRwtContextMenuMarker getMenuMarker() {
    return m_menuMarker;
  }

  public Shell getParentShell() {
    return m_parentShell;
  }

  @Override
  public IContextMenu getScoutContextMenu() {
    return m_scoutContextMenu;
  }

  @Override
  public Menu getUiMenu() {
    return m_uiMenu;
  }

  protected void handleSwtMenuHide() {
  }

  /**
  *
  */
  protected void handleSwtMenuShow() {
    ensureChildrenLoaded();
    Runnable t = new Runnable() {
      @SuppressWarnings("deprecation")
      @Override
      public void run() {
        getScoutContextMenu().prepareAction();
      }
    };
    getEnvironment().invokeScoutLater(t, 0);
    //end notify
  }

  protected void handleSwtMenuDispose() {
    m_uiMenu.removeListener(SWT.Show, m_uiMenuListener);
    m_uiMenu.removeListener(SWT.Hide, m_uiMenuListener);
    m_uiMenu.removeListener(SWT.Dispose, m_uiMenuListener);
    m_uiMenuListener = null;

  }

  private class P_UiMenuListener implements Listener {
    private static final long serialVersionUID = 1L;

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

  } // end P_UiMenuListener
}
