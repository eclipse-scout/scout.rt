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
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.swt.SWT;
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
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutContextMenu.class);

  protected final BasicPropertySupport m_propertySupport;
  private final IRwtEnvironment m_environment;
  private final Shell m_parentShell;
  private Menu m_uiMenu;

  private IContextMenu m_scoutContextMenu;
  private Listener m_uiMenuListener;
  private Boolean m_childrenCreated = Boolean.valueOf(false);


  public RwtScoutContextMenu(Shell parentShell, IContextMenu scoutContextMenu, IRwtEnvironment environment) {
    this(parentShell, scoutContextMenu, environment, true);
  }

  public RwtScoutContextMenu(Shell parentShell, IContextMenu scoutContextMenu, IRwtEnvironment environment, boolean callInitializer) {
    m_parentShell = parentShell;
    m_scoutContextMenu = scoutContextMenu;
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

  public void dispose() {
    if (getUiMenu() != null) {
      getUiMenu().dispose();
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

  protected void updateUiMenu() {
    for (MenuItem item : getUiMenu().getItems()) {
      if (item.getData(DATA_SYSTEM_MENU) == null) {
        item.dispose();
      }
    }
    RwtMenuUtility.fillMenu(getUiMenu(), getScoutContextMenu().getChildActions(), getScoutContextMenu().getActiveFilter(), getEnvironment());
  }

  @Override
  public IRwtEnvironment getEnvironment() {
    return m_environment;
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

  /**
  *
  */
  protected void handleSwtMenuShow() {
    Runnable t = new Runnable() {
      @SuppressWarnings("deprecation")
      @Override
      public void run() {
        getScoutContextMenu().prepareAction();
        getScoutContextMenu().aboutToShow();
      }
    };
    JobEx prepareJob = getEnvironment().invokeScoutLater(t, 0);
    try {
      prepareJob.join(1200);
    }
    catch (InterruptedException e) {
      LOG.error("error during prepare menus.", e);
    }
    updateUiMenu();
    //end notify
  }

  protected void handleSwtMenuHide() {
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
