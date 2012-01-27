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

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.keystroke.ISwtKeyStroke;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class AbstractSwtMenuAction {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSwtMenuAction.class);

  private final ISwtEnvironment m_environment;
  private final IAction m_scoutAction;
  private boolean m_initialized;
  private boolean m_connectedToScout;
  private P_ScoutPropertyChangeListener m_scoutPropertyListener;
  private MenuItem m_swtMenuItem;
  private final Menu m_swtMenu;
  // cache
  private ISwtKeyStroke[] m_swtKeyStrokes;

  private SelectionListener m_menuSelectionListener;

  public AbstractSwtMenuAction(Menu swtMenu, IAction action, boolean createInitial, ISwtEnvironment environment) {
    m_swtMenu = swtMenu;
    m_scoutAction = action;
    m_environment = environment;
    if (createInitial) {
      callInitializers(m_swtMenu);
    }
  }

  protected final void callInitializers(Menu swtMenu) {
    if (m_initialized) {
      return;
    }
    else {
      m_initialized = true;
      //
      initializeSwt(swtMenu);
      connectToScout();
    }
  }

  protected final void connectToScout() {
    if (!m_connectedToScout) {
      attachScoutListeners();
      applyScoutProperties();
      m_connectedToScout = true;
    }
  }

  protected final void disconnectFromScout() {
    if (m_connectedToScout) {
      detachScoutListeners();
      m_connectedToScout = false;
    }
  }

  protected void attachScoutListeners() {
    if (m_scoutPropertyListener == null) {
      m_scoutPropertyListener = new P_ScoutPropertyChangeListener();
      m_scoutAction.addPropertyChangeListener(m_scoutPropertyListener);
    }
  }

  protected void applyScoutProperties() {
    IAction scoutAction = getScoutAction();
    setEnabledFromScout(scoutAction.isEnabled());
    setTextFromScout(scoutAction.getText());
    setTooltipTextFromScout(scoutAction.getTooltipText());
    // setMnemonicFromScout(scoutAction.getMnemonic());
    setIconFromScout(scoutAction.getIconId());
    updateKeyStrokeFromScout();
  }

  protected void setIconFromScout(String iconId) {
    if (!getSwtMenuItem().isDisposed()) {
      getSwtMenuItem().setImage(getEnvironment().getIcon(iconId));
    }
  }

  protected void setTooltipTextFromScout(String tooltipText) {
    if (!StringUtility.isNullOrEmpty(tooltipText)) {
      LOG.warn("unsuported method on swt");
    }
  }

  protected void setTextFromScout(String text) {
    if (!getSwtMenuItem().isDisposed()) {
      getSwtMenuItem().setText(text);
    }
  }

  protected void setEnabledFromScout(boolean enabled) {
    if (!getSwtMenuItem().isDisposed()) {
      getSwtMenuItem().setEnabled(enabled);
    }
  }

  protected void updateKeyStrokeFromScout() {
    // remove old
    if (m_swtKeyStrokes != null) {
      for (ISwtKeyStroke swtStroke : m_swtKeyStrokes) {
        getEnvironment().removeGlobalKeyStroke(swtStroke);
      }
    }
    m_swtKeyStrokes = null;
    if (getScoutAction().getKeyStroke() != null) {
      IKeyStroke scoutKeyStroke = new KeyStroke(getScoutAction().getKeyStroke());
      m_swtKeyStrokes = SwtUtility.getKeyStrokes(scoutKeyStroke, getEnvironment());
      for (ISwtKeyStroke swtStroke : m_swtKeyStrokes) {
        getEnvironment().addGlobalKeyStroke(swtStroke);
      }
    }
  }

  protected void initializeSwt(Menu swtMenu) {
  }

  protected void detachScoutListeners() {
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  public IAction getScoutAction() {
    return m_scoutAction;
  }

  public MenuItem getSwtMenuItem() {
    return m_swtMenuItem;
  }

  public void setSwtMenuItem(MenuItem swtMenuItem) {
    if (m_swtMenuItem != null) {
      m_swtMenuItem.removeSelectionListener(m_menuSelectionListener);
    }
    m_swtMenuItem = swtMenuItem;
    if (m_menuSelectionListener == null) {
      m_menuSelectionListener = new P_MenuItemSelectionListener();
    }

    m_swtMenuItem.addSelectionListener(m_menuSelectionListener);
  }

  protected Menu getSwtMenu() {
    return m_swtMenu;
  }

  private void handleSwtAction() {
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutAction().getUIFacade().fireActionFromUI();
      }
    };
    getEnvironment().invokeScoutLater(t, 0);
  }

  private boolean isHandleScoutPropertyChange(String propertyName, Object newValue) {
    return true;
  }

  /**
   * in swt thread
   */
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (name.equals(IAction.PROP_ENABLED)) {
      setEnabledFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IAction.PROP_TEXT)) {
      setTextFromScout((String) newValue);
    }
    else if (name.equals(IAction.PROP_TOOLTIP_TEXT)) {
      setTooltipTextFromScout((String) newValue);
    }
    else if (name.equals(IAction.PROP_ICON_ID)) {
      setIconFromScout((String) newValue);
    }
    else if (name.equals(IAction.PROP_KEYSTROKE)) {
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

  private class P_MenuItemSelectionListener extends SelectionAdapter {
    @Override
    public void widgetSelected(SelectionEvent e) {
      handleSwtAction();
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
      // TODO Auto-generated method stub
      super.widgetDefaultSelected(e);
    }
  }
}
