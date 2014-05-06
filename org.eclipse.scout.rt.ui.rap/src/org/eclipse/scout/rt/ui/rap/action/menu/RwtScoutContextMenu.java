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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
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

  // children
  private List<IMenu> m_scoutChildMenus;
  private IContextMenu m_scoutContextMenu;
  private Listener m_uiMenuListener;
  private Boolean m_childrenCreated = Boolean.valueOf(false);

  // all children
  private final PropertyChangeListener m_scoutMenuVisibilityListener;
  private List<IMenu> m_visibilityObservees;

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
    m_scoutMenuVisibilityListener = new P_VisibilityOfMenuItemChangedListener();
    if (callInitializer) {
      initMenu();
    }
  }

  protected void initMenu() {
    m_uiMenu = new Menu(getParentShell(), SWT.POP_UP);
//    getMenuOwner().setMenu(m_swtMenu);

    m_scoutChildMenus = getScoutContextMenu().getChildActions();
    updateMenusFromScout();
    addScoutMenuVisibilityListenerToAllChildren(getScoutContextMenu());
    // listeners
    m_uiMenuListener = new P_UiMenuListener();
    m_uiMenu.addListener(SWT.Show, m_uiMenuListener);
    m_uiMenu.addListener(SWT.Hide, m_uiMenuListener);
    m_uiMenu.addListener(SWT.Dispose, m_uiMenuListener);
  }

  protected void removeScoutMenuVisibilityListenerToAllChildren(IMenu menuContainer) {
    if (m_visibilityObservees != null) {
      for (IMenu m : m_visibilityObservees) {
        m.removePropertyChangeListener(IMenu.PROP_VISIBLE, m_scoutMenuVisibilityListener);
        removeScoutMenuVisibilityListenerToAllChildren(m);
      }
      m_visibilityObservees = null;
    }
  }

  protected void addScoutMenuVisibilityListenerToAllChildren(IMenu menuContainer) {
    if (m_visibilityObservees == null) {
      m_visibilityObservees = new ArrayList<IMenu>();
    }
    for (IMenu m : menuContainer.getChildActions()) {
      m_visibilityObservees.add(m);
      m.addPropertyChangeListener(IMenu.PROP_VISIBLE, m_scoutMenuVisibilityListener);
      addScoutMenuVisibilityListenerToAllChildren(m);
    }
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
          RwtMenuUtility.fillMenu(getUiMenu(), m_scoutChildMenus, getEnvironment());

        }
        finally {
          m_childrenCreated = Boolean.valueOf(true);
        }
      }
    }
  }

  protected void updateMenusFromScout() {

//    removeScoutMenuVisibilityListenerToAllChildren(getScoutMenuContainer());
//    addScoutMenuVisibilityListenerToAllChildren(getScoutMenuContainer());
//    m_scoutChildMenus = getScoutMenuContainer().getMenus();
    // copy past menus

    // scout menus
    List<IMenu> visibleChildMenus = ActionUtility.visibleNormalizedActions(m_scoutChildMenus);
    if (getMenuMarker() != null) {
      getMenuMarker().setMarkerVisible(!visibleChildMenus.isEmpty());
    }
    disposeChildren();
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
        for (IMenu menuItem : m_scoutChildMenus) {
          menuItem.prepareAction();
        }
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

  private class P_VisibilityOfMenuItemChangedListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
      if (IMenu.PROP_VISIBLE.equals(evt.getPropertyName())) {
        // sync to ui
        Runnable r = new Runnable() {

          @Override
          public void run() {
            updateMenusFromScout();
          }

        };
        getEnvironment().invokeUiLater(r);
      }
    }
  }
}
