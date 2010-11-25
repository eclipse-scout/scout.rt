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
package org.eclipse.scout.rt.ui.swt.util;

import org.eclipse.swt.widgets.Display;

public abstract class AbstractShellPackHandler {
  private final Display m_display;
  private Runnable m_runnable;
  private final Object m_lock = new Object();

  public AbstractShellPackHandler(Display display) {
    m_display = display;
  }

  public void enqueueSizeCheck() {
    synchronized (m_lock) {
      if (m_runnable == null && m_display != null && !m_display.isDisposed()) {
        m_runnable = new P_SizeCheckRunnable();
        m_display.asyncExec(m_runnable);
      }
    }
  }

  protected abstract void execSizeCheck();

  private class P_SizeCheckRunnable implements Runnable {
    public void run() {
      synchronized (m_lock) {
        execSizeCheck();
        m_runnable = null;
      }
    }
  } // end class P_SizeCheckRunnable
}
