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
package org.eclipse.scout.rt.ui.swing.window;

import java.awt.Dialog;
import java.awt.Window;
import java.util.ArrayList;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public final class SwingWindowManager {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingWindowManager.class);

  /*
   * Singleton
   */
  private static SwingWindowManager instance = new SwingWindowManager();

  public static SwingWindowManager getInstance() {
    return instance;
  }

  /*
   * Instance
   */
  private Window m_activeWindow;
  private ArrayList<Dialog> m_modalDialogStack = new ArrayList<Dialog>();

  private SwingWindowManager() {
  }

  public void setActiveWindow(Window w) {
    m_activeWindow = w;
  }

  public Window getActiveWindow() {
    return m_activeWindow;
  }

  public void pushModalDialog(Dialog modalDialog) {
    m_modalDialogStack.add(modalDialog);
  }

  public void popModalDialog(Dialog verifyModalDialog) {
    if (verifyModalDialog != null) {
      int index = m_modalDialogStack.indexOf(verifyModalDialog);
      if (index >= 0) {
        if (index != m_modalDialogStack.size() - 1) {
          LOG.warn("modal dialog stack is out of sync. Expected " + verifyModalDialog + " on top, but stack is: " + m_modalDialogStack);
        }
        m_modalDialogStack.remove(index);
      }
      else {
        LOG.warn("modal dialog stack is out of sync. Expected " + verifyModalDialog + " on top, but stack is empty");
      }
    }
  }

  public Dialog getActiveModalDialog() {
    if (m_modalDialogStack.isEmpty()) return null;
    else return m_modalDialogStack.get(m_modalDialogStack.size() - 1);
  }
}
