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
package org.eclipse.scout.rt.ui.rap;

import java.util.LinkedList;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Validate layout changes (add/remove, invisible components).
 * <p>
 * Components that act as validate roots use {@link Control#setData(VALIDATE_ROOT_DATA, ValidateRoot)}
 */
public class LayoutValidateManager {
  private static final String RUNNABLE_DISPLAY_DATA = "LayoutValidateManager.runnable";

  private final LinkedList<IValidateRoot> m_dirtyList;

  public LayoutValidateManager() {
    m_dirtyList = new LinkedList<IValidateRoot>();
  }

  public void invalidate(Control c) {
    if (c == null) {
      return;
    }
    //collect all IValidateRoot on the path to root
    boolean changed = false;
    Control tmp = c;
    while (tmp != null) {
      IValidateRoot v = (IValidateRoot) tmp.getData(IValidateRoot.VALIDATE_ROOT_DATA);
      if (v != null) {
        changed = true;
        m_dirtyList.remove(v);
        m_dirtyList.add(0, v);
      }
      tmp = tmp.getParent();
    }
    if (changed) {
      //runnable per display
      Display display = c.getDisplay();
      Runnable runnable = (Runnable) display.getData(RUNNABLE_DISPLAY_DATA);
      if (runnable == null && display != null && !display.isDisposed()) {
        runnable = new P_ValidateRunnable();
        display.setData(RUNNABLE_DISPLAY_DATA, null);
        display.asyncExec(runnable);
      }
    }
  }

  private class P_ValidateRunnable implements Runnable {
    @Override
    public void run() {
      try {
        for (IValidateRoot v : m_dirtyList) {
          v.validate();
        }
      }
      finally {
        m_dirtyList.clear();
        Display.getCurrent().setData(RUNNABLE_DISPLAY_DATA, null);
      }
    }
  } // end class P_SizeCheckRunnable
}
