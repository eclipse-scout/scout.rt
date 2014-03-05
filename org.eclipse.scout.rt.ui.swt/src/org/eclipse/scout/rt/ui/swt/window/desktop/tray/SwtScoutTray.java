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
package org.eclipse.scout.rt.ui.swt.window.desktop.tray;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.swt.Activator;
import org.eclipse.scout.rt.ui.swt.SwtIcons;
import org.eclipse.scout.rt.ui.swt.SwtMenuUtility;
import org.eclipse.scout.rt.ui.swt.basic.SwtScoutComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

public class SwtScoutTray extends SwtScoutComposite<IDesktop> implements ISwtScoutTray {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutTray.class);

  private TrayItem m_trayItem;
  private Menu m_popupMenu;

  public SwtScoutTray() {
    super();
  }

  @Override
  protected void initializeSwt(Composite parent) {
    super.initializeSwt(parent);
    m_trayItem = createTrayItem();
    if (m_trayItem != null) {
      m_trayItem.addMenuDetectListener(new MenuDetectListener() {
        @Override
        public void menuDetected(MenuDetectEvent e) {
          if (m_popupMenu != null) {
            m_popupMenu.setVisible(true);
          }
        }
      });
      //
      m_popupMenu = createPopupMenu();
      updatePopupMenus();
    }
  }

  protected TrayItem createTrayItem() {
    Tray tray = getEnvironment().getDisplay().getSystemTray();
    if (tray != null) {
      TrayItem trayItem = new TrayItem(tray, SWT.NONE);
      trayItem.setImage(Activator.getIcon(SwtIcons.Window));
      return trayItem;
    }
    return null;
  }

  protected Menu createPopupMenu() {
    Shell shell = new Shell(getEnvironment().getDisplay());
    return new Menu(shell, SWT.POP_UP);
  }

  @Override
  public boolean isDisposed() {
    return m_trayItem == null || m_trayItem.isDisposed();
  }

  protected void updatePopupMenus() {
    if (m_popupMenu == null) {
      return;
    }
    //
    for (MenuItem mi : m_popupMenu.getItems()) {
      if (!mi.isDisposed()) {
        mi.dispose();
      }
    }
    //
    if (getScoutObject() != null) {
      final AtomicReference<List<IMenu>> scoutMenusRef = new AtomicReference<List<IMenu>>();
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          scoutMenusRef.set(getScoutObject().getUIFacade().fireTrayPopupFromUI());
        }
      };
      try {
        getEnvironment().invokeScoutLater(t, 5678).join(5678);
      }
      catch (InterruptedException e) {
        //nop
      }
      // end notify
      SwtMenuUtility.fillContextMenu(scoutMenusRef.get(), m_popupMenu, getEnvironment());
    }
  }

  @Override
  public TrayItem getSwtTrayItem() {
    return m_trayItem;
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    IDesktop desktop = getScoutObject();
    setTooltipFromScout(desktop.getTitle());
  }

  /*
   * properties
   */
  protected void setTooltipFromScout(String tooltip) {
    if (m_trayItem != null) {
      m_trayItem.setToolTipText(tooltip);
    }
  }

  /*
   * extended property observer
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (IDesktop.PROP_TITLE.equals(name)) {
      setTooltipFromScout((String) newValue);
    }
  }
}
