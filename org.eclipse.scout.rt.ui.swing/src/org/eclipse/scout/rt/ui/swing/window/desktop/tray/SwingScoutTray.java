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
package org.eclipse.scout.rt.ui.swing.window.desktop.tray;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.swing.Activator;
import org.eclipse.scout.rt.ui.swing.SwingIcons;
import org.eclipse.scout.rt.ui.swing.action.SwingScoutAction;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;

public class SwingScoutTray extends SwingScoutComposite<IDesktop> implements ISwingScoutTray {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutTray.class);

  private TrayIcon m_trayIcon;

  public SwingScoutTray() {
    super();
  }

  @Override
  protected void initializeSwing() {
    m_trayIcon = createTrayIcon();
    if (m_trayIcon != null) {
      PopupMenu pm = createPopupMenu();
      if (pm != null) {
        updatePopupMenus(pm);
        m_trayIcon.setPopupMenu(pm);
      }
      try {
        SystemTray.getSystemTray().add(m_trayIcon);
      }
      catch (AWTException e) {
        LOG.warn("Failed attaching tray icon", e);
      }
    }
  }

  protected TrayIcon createTrayIcon() {
    Image icon = Activator.getImage(SwingIcons.Window); // legacy
    if (icon == null) {
      icon = Activator.getImage(SwingIcons.Tray); // different from window icon (should be a GIF for Win XP)
    }
    return new TrayIcon(icon);
  }

  protected PopupMenu createPopupMenu() {
    return new PopupMenu();
  }

  protected void updatePopupMenus(PopupMenu pm) {
    pm.removeAll();
    if (getScoutObject() != null) {
      final AtomicReference<IMenu[]> scoutMenusRef = new AtomicReference<IMenu[]>();
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          scoutMenusRef.set(getScoutObject().getUIFacade().fireTrayPopupFromUI());
        }
      };
      try {
        getSwingEnvironment().invokeScoutLater(t, 5678).join(5678);
      }
      catch (InterruptedException e) {
        //nop
      }
      // end notify
      if (scoutMenusRef.get() != null && scoutMenusRef.get().length > 0) {
        //create awt menu wrappers
        for (IAction a : scoutMenusRef.get()) {
          if (a.isSeparator()) {
            pm.addSeparator();
          }
          else if (a instanceof IMenu) {
            IMenu menu = (IMenu) a;
            SwingScoutAction<IAction> actionComposite = new SwingScoutAction<IAction>();
            actionComposite.createField(a, getSwingEnvironment());
            if (menu.getChildActionCount() > 0) {
              Menu mi = new Menu();
              mi.setLabel(menu.getText());
              mi.setEnabled(menu.isEnabled());
              mi.addActionListener(actionComposite.getSwingAction());
              pm.add(mi);
            }
            else {
              MenuItem mi = new MenuItem();
              mi.setLabel(menu.getText());
              mi.setEnabled(menu.isEnabled());
              mi.addActionListener(actionComposite.getSwingAction());
              pm.add(mi);
            }
          }
        }
      }
    }
  }

  @Override
  public TrayIcon getSwingTrayIcon() {
    return m_trayIcon;
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
    if (m_trayIcon != null) {
      m_trayIcon.setToolTip(tooltip);
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
