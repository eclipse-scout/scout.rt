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

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.checkbox.ICheckBoxMenu;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 *
 */
public class SwtScoutMenuContributionItem extends ContributionItem {

  private final IMenu m_scoutMenu;
  private final ISwtEnvironment m_environment;
  private MenuItem m_swtMenuItem;
  private boolean m_handleSelectionPending;

  private PropertyChangeListener m_scoutPropertyListener;
  Listener m_uiListener;

  public SwtScoutMenuContributionItem(IMenu scoutMenu, ISwtEnvironment environment) {
    m_scoutMenu = scoutMenu;
    m_environment = environment;
    // add scout prop listener
    m_scoutPropertyListener = new P_ScoutPropertyChangeListener();
    m_scoutMenu.addPropertyChangeListener(m_scoutPropertyListener);
  }

  @Override
  public void dispose() {

    getScoutMenu().removePropertyChangeListener(m_scoutPropertyListener);
    System.out.println("do dispose....");
    super.dispose();
  }

  public IMenu getScoutMenu() {
    return m_scoutMenu;
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  public MenuItem getSwtMenuItem() {
    return m_swtMenuItem;
  }

  @Override
  public void fill(Menu menu, int index) {
    if (m_swtMenuItem == null && menu != null) {

      int flags = SWT.PUSH;
      if (getScoutMenu().hasChildActions()) {
        flags = SWT.CASCADE;
      }
      else if (getScoutMenu() instanceof ICheckBoxMenu) {
        flags = SWT.CHECK;
      }
      MenuItem mi = null;
      if (index >= 0) {
        mi = new MenuItem(menu, flags, index);
      }
      else {
        mi = new MenuItem(menu, flags);
      }
      m_swtMenuItem = mi;
      // listeners
      m_uiListener = new P_SwtMenuListener();
      menu.addListener(SWT.Show, m_uiListener);
      m_swtMenuItem.addListener(SWT.Selection, m_uiListener);
      m_swtMenuItem.addListener(SWT.Dispose, m_uiListener);

      m_swtMenuItem.setData(getScoutMenu());
      updateEnabledFromScout();
      updateIconFromScout();
      updateKeyStrokeFromScout();
      updateTooltipTextFromScout();
      updateTextFromScout();

//      // child menus
//      if(flags == SWT.CASCADE){
//
////        just create a proxy for now, if the user shows it then
//     // fill it in
//     Menu subMenu = new Menu(parent);
//     subMenu.addListener(SWT.Show, getMenuCreatorListener());
//     subMenu.addListener(SWT.Hide, getMenuCreatorListener());
//     mi.setMenu(subMenu);
//     }
//      mi.addListener(SWT.Dispose, getMenuItemListener());
//      mi.addListener(SWT.Selection, getMenuItemListener());
//      if (action.getHelpListener() != null) {
//        mi.addHelpListener(action.getHelpListener());
//      }
//
//      if (flags == SWT.CASCADE) {
//        // just create a proxy for now, if the user shows it then
//        // fill it in
//        Menu subMenu = new Menu(parent);
//        subMenu.addListener(SWT.Show, getMenuCreatorListener());
//        subMenu.addListener(SWT.Hide, getMenuCreatorListener());
//        mi.setMenu(subMenu);
//      }
//
//      update(null);
//
//      // Attach some extra listeners.
//      action.addPropertyChangeListener(propertyListener);
//      if (action != null) {
//        String commandId = action.getActionDefinitionId();
//        ExternalActionManager.ICallback callback = ExternalActionManager
//            .getInstance().getCallback();
//
//        if ((callback != null) && (commandId != null)) {
//          callback.addPropertyChangeListener(commandId,
//              actionTextListener);
//        }
//      }
    }
  }

  @Override
  public boolean isDirty() {
    return true;
  }

  @Override
  public void update() {

  }

  @Override
  public void update(String id) {

  }

  protected void updateKeyStrokeFromScout() {
    if (getSwtMenuItem() != null) {
      // void see settext mnemonic
    }
  }

  protected void updateIconFromScout() {
    if (getSwtMenuItem() != null) {
      getSwtMenuItem().setImage(getEnvironment().getIcon(getScoutMenu().getIconId()));
    }
  }

  protected void updateTooltipTextFromScout() {
    if (getSwtMenuItem() != null) {
      // not supported in swt
    }
  }

  protected void updateTextFromScout() {
    if (getSwtMenuItem() != null) {
      getSwtMenuItem().setText(getScoutMenu().getTextWithMnemonic());
    }
  }

  protected void updateEnabledFromScout() {
    if (getSwtMenuItem() != null) {
      getSwtMenuItem().setEnabled(getScoutMenu().isEnabled());
    }
  }

  protected void handleSwtMenuShow() {
    //notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutMenu().prepareAction();
      }
    };
    getEnvironment().invokeScoutLater(t, 0);
    //end notify
  }

  private void handleSwtMenuDispose() {
  }

  private void handleSwtMenuSelection() {
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
      System.out.println("text[m] changed to: " + getScoutMenu().getText());
      updateTextFromScout();
    }
    else if (name.equals(IMenu.PROP_TEXT)) {
      System.out.println("text changed to: " + getScoutMenu().getText());
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

  }// end private class

  private class P_SwtMenuListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Show:
          handleSwtMenuShow();
          break;
        case SWT.Selection:
          handleSwtMenuSelection();
          break;
        case SWT.Dispose:
          handleSwtMenuDispose();
          break;

        default:
          break;
      }
    }

  }
}
