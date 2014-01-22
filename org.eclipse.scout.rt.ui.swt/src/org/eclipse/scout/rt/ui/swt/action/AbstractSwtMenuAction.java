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

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.keystroke.ISwtKeyStroke;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Common code for the SWT widgets (sub classes of {@link MenuItem}) rendering {@link IAction}.
 */
public class AbstractSwtMenuAction extends AbstractSwtScoutAction {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSwtMenuAction.class);

  private boolean m_initialized;
  private MenuItem m_swtMenuItem;
  private final Menu m_swtMenu;
  // cache
  private ISwtKeyStroke[] m_swtKeyStrokes;

  private SelectionListener m_menuSelectionListener;

  public AbstractSwtMenuAction(Menu swtMenu, IAction action, boolean createInitial, ISwtEnvironment environment) {
    super(action, environment);
    m_swtMenu = swtMenu;
    if (createInitial) {
      callInitializers(m_swtMenu);
    }

    m_swtMenu.addDisposeListener(new DisposeListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetDisposed(DisposeEvent event) {
        disconnectFromScout();
      }
    });
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

  @Override
  protected void setIconFromScout(String iconId) {
    if (!getSwtMenuItem().isDisposed()) {
      getSwtMenuItem().setImage(getEnvironment().getIcon(iconId));
    }
  }

  @Override
  protected void setTooltipTextFromScout(String tooltipText) {
    if (!StringUtility.isNullOrEmpty(tooltipText)) {
      LOG.warn("unsuported method on swt");
    }
  }

  /**
   * @deprecated Use {@link #setTextWithMnemonicFromScout(String)} instead.
   *             Will be removed with the M-Release.
   */
  @Deprecated
  protected void setTextFromScout(String text) {
    setTextWithMnemonicFromScout(text);
  }

  @Override
  protected void setTextWithMnemonicFromScout(String textWithMnemonic) {
    if (!getSwtMenuItem().isDisposed()) {
      IAction action = getScoutAction();
      if (action != null && StringUtility.hasText(action.getKeyStroke())) {
        textWithMnemonic += "\t" + SwtUtility.getKeyStrokePrettyPrinted(action);
      }
      getSwtMenuItem().setText(textWithMnemonic);
    }
  }

  @Override
  protected void setEnabledFromScout(boolean enabled) {
    if (!getSwtMenuItem().isDisposed()) {
      getSwtMenuItem().setEnabled(enabled);
    }
  }

  @Override
  protected void setKeyStrokeFromScout(String keyStroke) {
    // remove old
    if (m_swtKeyStrokes != null) {
      for (ISwtKeyStroke swtStroke : m_swtKeyStrokes) {
        getEnvironment().removeGlobalKeyStroke(swtStroke);
      }
    }
    m_swtKeyStrokes = null;
    if (keyStroke != null) {
      IKeyStroke scoutKeyStroke = new KeyStroke(getScoutAction().getKeyStroke());
      m_swtKeyStrokes = SwtUtility.getKeyStrokes(scoutKeyStroke, getEnvironment());
      for (ISwtKeyStroke swtStroke : m_swtKeyStrokes) {
        getEnvironment().addGlobalKeyStroke(swtStroke);
      }
    }
  }

  protected void initializeSwt(Menu swtMenu) {
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
    if (SwtUtility.runSwtInputVerifier()) {
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutAction().getUIFacade().fireActionFromUI();
        }
      };
      getEnvironment().invokeScoutLater(t, 0);
    }
  }

  @Override
  protected boolean isHandleScoutPropertyChangeSwtThread() {
    return !getSwtMenu().isDisposed();
  }

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
