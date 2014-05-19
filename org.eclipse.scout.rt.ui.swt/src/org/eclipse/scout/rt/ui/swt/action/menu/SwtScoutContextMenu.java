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
package org.eclipse.scout.rt.ui.swt.action.menu;

import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IContextMenu;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.SwtMenuUtility;
import org.eclipse.scout.rt.ui.swt.action.menu.text.ITextAccess;
import org.eclipse.scout.rt.ui.swt.action.menu.text.SwtCopyMenuItem;
import org.eclipse.scout.rt.ui.swt.action.menu.text.SwtCutMenuItem;
import org.eclipse.scout.rt.ui.swt.action.menu.text.SwtPasteMenuItem;
import org.eclipse.scout.rt.ui.swt.action.menu.text.SwtRedoMenuItem;
import org.eclipse.scout.rt.ui.swt.action.menu.text.SwtUndoMenuItem;
import org.eclipse.scout.rt.ui.swt.internal.StyledTextFieldUndoRedoSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 *
 */
public class SwtScoutContextMenu implements ISwtScoutMenu {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutContextMenu.class);
  public static final String DATA_SYSTEM_MENU = "dataSystemMenu";

  protected final BasicPropertySupport m_propertySupport;
  private final ISwtEnvironment m_environment;
  private final Shell m_parentShell;
  private Menu m_swtMenu;

  // children
//  private List<IMenu> m_scoutChildMenus;
  private IContextMenu m_scoutContextMenu;
  private Listener m_uiMenuListener;
  private Boolean m_childrenCreated = Boolean.valueOf(false);

  // all children
//  private final PropertyChangeListener m_scoutMenuVisibilityListener;
//  private List<IMenu> m_visibilityObservees;

  private final ITextAccess m_systemMenuOwner;
  private final StyledText m_undoRedoMenuOwner;

  public SwtScoutContextMenu(Shell parentShell, IContextMenu scoutContextMenu, ISwtEnvironment environment) {
    this(parentShell, scoutContextMenu, environment, null, null, true);
  }

  public SwtScoutContextMenu(Shell parentShell, IContextMenu scoutContextMenu, ISwtEnvironment environment, ITextAccess systemMenuOwner) {
    this(parentShell, scoutContextMenu, environment, systemMenuOwner, null, true);
  }

  public SwtScoutContextMenu(Shell parentShell, IContextMenu scoutContextMenu, ISwtEnvironment environment, ITextAccess systemMenuOwner, StyledText undoRedoMenuOwner) {
    this(parentShell, scoutContextMenu, environment, systemMenuOwner, undoRedoMenuOwner, true);
  }

  public SwtScoutContextMenu(Shell parentShell, IContextMenu scoutContextMenu, ISwtEnvironment environment, ITextAccess systemMenuOwner, StyledText undoRedoMenuOwner, boolean callInitializer) {
    m_parentShell = parentShell;
    m_scoutContextMenu = scoutContextMenu;
    m_systemMenuOwner = systemMenuOwner;
    m_undoRedoMenuOwner = undoRedoMenuOwner;
    m_environment = environment;
    m_propertySupport = new BasicPropertySupport(this);
//    m_scoutMenuVisibilityListener = new P_VisibilityOfMenuItemChangedListener();
//    m_visibilityObservees = new ArrayList<IMenu>();
    if (callInitializer) {
      initMenu();
    }
  }

  protected void initMenu() {
    m_swtMenu = new Menu(getParentShell().getShell(), SWT.POP_UP);
    // scout menus
    createSystemMenuItems(m_swtMenu);
//    addScoutMenuVisibilityListenerToAllChildren(getScoutContextMenu());
    // listeners
    m_uiMenuListener = new P_UiMenuListener();
    m_swtMenu.addListener(SWT.Show, m_uiMenuListener);
    m_swtMenu.addListener(SWT.Hide, m_uiMenuListener);
    m_swtMenu.addListener(SWT.Dispose, m_uiMenuListener);
  }

//  private void addVisiblePropertyListenerRec(IMenu menu) {
//    for (IMenu m : m_visibilityObservees) {
//      m.removePropertyChangeListener(IMenu.PROP_VISIBLE, m_scoutMenuVisibilityListener);
//    }
//    m_visibilityObservees.clear();
//    for (IMenu m : menu.getChildActions()) {
//      m.addPropertyChangeListener(IMenu.PROP_VISIBLE, m_scoutMenuVisibilityListener);
//      m_visibilityObservees.add(m);
//      addVisiblePropertyListenerRec(m);
//    }
//  }

  /**
   * @param swtMenu
   */
  protected void createSystemMenuItems(Menu swtMenu) {
    if (getSystemMenuOwner() != null) {
      new SwtCutMenuItem(swtMenu, getSystemMenuOwner());
      new SwtCopyMenuItem(swtMenu, getSystemMenuOwner());
      new SwtPasteMenuItem(swtMenu, getSystemMenuOwner());
    }
    if (getUndoRedoMenuOwner() != null) {
      StyledTextFieldUndoRedoSupport undoRedoSupport = new StyledTextFieldUndoRedoSupport(getUndoRedoMenuOwner());
      new SwtUndoMenuItem(swtMenu, undoRedoSupport);
      new SwtRedoMenuItem(swtMenu, undoRedoSupport);
    }
  }

  protected void updateUiMenu() {
    for (MenuItem item : getSwtMenu().getItems()) {
      if (item.getData(DATA_SYSTEM_MENU) == null) {
        item.dispose();
      }
    }
    SwtMenuUtility.fillMenu(getSwtMenu(), getScoutContextMenu().getChildActions(), ActionUtility.createMenuFilterVisibleAvailable(), getEnvironment(), getSwtMenu().getItemCount() > 0);
  }

  protected void ensureChildrenLoaded() {
    synchronized (m_childrenCreated) {
      if (!m_childrenCreated) {
        try {
        }
        finally {
          m_childrenCreated = Boolean.valueOf(true);
        }
      }
    }
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  public Shell getParentShell() {
    return m_parentShell;
  }

  public IContextMenu getScoutContextMenu() {
    return m_scoutContextMenu;
  }

  public ITextAccess getSystemMenuOwner() {
    return m_systemMenuOwner;
  }

  public StyledText getUndoRedoMenuOwner() {
    return m_undoRedoMenuOwner;
  }

  public Menu getSwtMenu() {
    return m_swtMenu;
  }

  protected void handleSwtMenuHide() {
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

  protected void handleSwtMenuDispose() {
    m_swtMenu.removeListener(SWT.Show, m_uiMenuListener);
    m_swtMenu.removeListener(SWT.Hide, m_uiMenuListener);
    m_swtMenu.removeListener(SWT.Dispose, m_uiMenuListener);
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

  } // end P_UiMenuListener

}
