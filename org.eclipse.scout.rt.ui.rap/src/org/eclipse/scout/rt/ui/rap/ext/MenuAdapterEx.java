/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.ext;

import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;

/**
 * @author sle
 * @since 3.8.0
 */
public abstract class MenuAdapterEx extends MenuAdapter {
  private static final long serialVersionUID = 1L;

  private final Control m_menuControl;
  private final Control m_keyStrokeWidget;
  private P_EscKeyStroke m_escKeyStroke;

  public MenuAdapterEx(Control menuControl, Control keyStrokeWidget) {
    m_menuControl = menuControl;
    m_keyStrokeWidget = keyStrokeWidget;
  }

  public Control getMenuControl() {
    return m_menuControl;
  }

  public Control getKeyStrokeWidget() {
    return m_keyStrokeWidget;
  }

  @Override
  public void menuShown(MenuEvent e) {
    Menu menu = ((Menu) e.getSource());
    final IRwtEnvironment uiEnvironment = RwtUtility.getUiEnvironment(e.display);
    m_escKeyStroke = new P_EscKeyStroke(menu);

    uiEnvironment.addKeyStroke(getKeyStrokeWidget(), m_escKeyStroke, true);
    menu.addDisposeListener(new DisposeListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetDisposed(DisposeEvent event) {
        if (m_escKeyStroke != null) {
          uiEnvironment.removeKeyStroke(getKeyStrokeWidget(), m_escKeyStroke);
          m_escKeyStroke = null;
        }
      }
    });
  }

  @Override
  public void menuHidden(MenuEvent e) {
    if (m_escKeyStroke != null) {
      IRwtEnvironment uiEnvironment = RwtUtility.getUiEnvironment(e.display);
      uiEnvironment.removeKeyStroke(getKeyStrokeWidget(), m_escKeyStroke);
      m_escKeyStroke = null;
    }
  }

  //add escape-keystroke to close the contextmenu with esc, resp. prevent closing the surrounding dialog when a contextmenu is open.
  private class P_EscKeyStroke extends RwtKeyStroke {
    private Menu m_menu;

    public P_EscKeyStroke(Menu menu) {
      super(RwtUtility.scoutToRwtKey("escape"));
      m_menu = menu;
    }

    @Override
    public void handleUiAction(Event keyEvent) {
      final IRwtEnvironment uiEnvironment = RwtUtility.getUiEnvironment(keyEvent.display);
      if (m_menu != null) {
        m_menu.dispose();
      }
      uiEnvironment.removeKeyStroke(getKeyStrokeWidget(), this);
      keyEvent.doit = false;
    }
  }
}
