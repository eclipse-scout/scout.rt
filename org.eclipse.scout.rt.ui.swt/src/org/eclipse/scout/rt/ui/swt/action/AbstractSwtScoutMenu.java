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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
public abstract class AbstractSwtScoutMenu {
  private IMenu m_scoutMenu;
  private Menu m_parentMenu;
  private ISwtEnvironment m_environment;

  private MenuItem m_swtMenuItem;

  private PropertyChangeListener m_scoutPropertyChangeListener;
  private Listener m_swtMenuItemListener;

  public AbstractSwtScoutMenu() {
  }

  public AbstractSwtScoutMenu(IMenu scoutMenu, Menu parentMenu, ISwtEnvironment environment) {
    this(scoutMenu, parentMenu, environment, true);
  }

  public AbstractSwtScoutMenu(IMenu scoutMenu, Menu parentMenu, ISwtEnvironment environment, boolean callInitializer) {
    if (callInitializer) {
      createMenu(scoutMenu, parentMenu, environment);
    }
  }

  protected void createMenu(IMenu scoutMenu, Menu parentMenu, ISwtEnvironment environment) {
    m_scoutMenu = scoutMenu;
    m_environment = environment;
    m_parentMenu = parentMenu;

    m_swtMenuItem = createSwtMenu(parentMenu, scoutMenu);
    m_swtMenuItem.setData(getScoutMenu());
    initializeUi(m_swtMenuItem);
    attachScout();
  }

  /**
   *
   */
  protected void attachScout() {
    m_scoutPropertyChangeListener = new P_ScoutPropertyChangeListener();
    m_scoutMenu.addPropertyChangeListener(m_scoutPropertyChangeListener);
    // init
    updateEnabledFromScout();
    updateIconFromScout();
    updateKeyStrokeFromScout();
    updateTextWithMnemonicFromScout();
    updateTooltipTextFromScout();
  }

  protected void dettachScout() {
    getScoutMenu().removePropertyChangeListener(m_scoutPropertyChangeListener);
  }

  /**
   * @param parentMenu
   * @return
   */
  protected MenuItem createSwtMenu(Menu parentMenu, IMenu scoutMenu) {
    int flags = SWT.PUSH;
    if (scoutMenu.isSeparator()) {
      flags = SWT.SEPARATOR;
    }
    if (scoutMenu.hasChildActions()) {
      flags = SWT.CASCADE;
    }
    else if (getScoutMenu().isToggleAction()) {
      flags = SWT.CHECK;
    }
    MenuItem swtMenuItem = new MenuItem(parentMenu, flags);
    m_swtMenuItemListener = new P_UiMenuItemListener();
    swtMenuItem.addListener(SWT.Dispose, m_swtMenuItemListener);
    return swtMenuItem;
  }

  protected void initializeUi(MenuItem swtMenuItem) {
  }

  public IMenu getScoutMenu() {
    return m_scoutMenu;
  }

  public Menu getParentMenu() {
    return m_parentMenu;
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  public MenuItem getSwtMenuItem() {
    return m_swtMenuItem;
  }

  private boolean isHandleScoutPropertyChange(String propertyName, Object newValue) {
    return true;
  }

  /**
   * in swt thread
   */
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (name.equals(IMenu.PROP_ENABLED)) {
      updateEnabledFromScout();
    }
    else if (name.equals(IMenu.PROP_TEXT_WITH_MNEMONIC)) {
      updateTextWithMnemonicFromScout();
    }
    else if (name.equals(IMenu.PROP_TOOLTIP_TEXT)) {
      updateTooltipTextFromScout();
    }
    else if (name.equals(IMenu.PROP_ICON_ID)) {
      updateIconFromScout();
    }
    else if (name.equals(IMenu.PROP_KEYSTROKE)) {
      updateKeyStrokeFromScout();
    }

  }

  protected void updateKeyStrokeFromScout() {
    if (getSwtMenuItem() != null && !getSwtMenuItem().isDisposed()) {
      // void see settext mnemonic
    }
  }

  protected void updateIconFromScout() {
    if (getSwtMenuItem() != null && !getSwtMenuItem().isDisposed()) {
      getSwtMenuItem().setImage(getEnvironment().getIcon(getScoutMenu().getIconId()));
    }
  }

  protected void updateTooltipTextFromScout() {
    if (getSwtMenuItem() != null && !getSwtMenuItem().isDisposed()) {
      // not supported in swt
    }
  }

  protected void updateTextWithMnemonicFromScout() {
    if (getSwtMenuItem() != null && !getSwtMenuItem().isDisposed()) {
      String text = getScoutMenu().getTextWithMnemonic();
      if (text == null) {
        text = "";
      }
      getSwtMenuItem().setText(text);
    }
  }

  protected void updateEnabledFromScout() {
    if (getSwtMenuItem() != null && !getSwtMenuItem().isDisposed()) {
      getSwtMenuItem().setEnabled(getScoutMenu().isEnabled());
    }
  }

  /**
  *
  */
  protected void handleSwtMenuItemDispose() {
    if (getSwtMenuItem() != null && !getSwtMenuItem().isDisposed()) {
      getSwtMenuItem().removeListener(SWT.Dispose, m_swtMenuItemListener);
      m_swtMenuItemListener = null;
    }
    dettachScout();
  }

  private class P_ScoutPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent e) {
      if (isHandleScoutPropertyChange(e.getPropertyName(), e.getNewValue())) {
        Runnable t = new Runnable() {
          @Override
          public void run() {
            handleScoutPropertyChange(e.getPropertyName(), e.getNewValue());
          }

        };
        getEnvironment().invokeSwtLater(t);
      }
    }
  }

  private class P_UiMenuItemListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Dispose:
          handleSwtMenuItemDispose();
          break;
      }
    }
  }

}
