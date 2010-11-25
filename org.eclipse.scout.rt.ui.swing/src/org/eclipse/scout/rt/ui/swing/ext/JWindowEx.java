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
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Window;

import javax.swing.JRootPane;
import javax.swing.JWindow;

/**
 * JDialog with support of wait cursor property as a second layer over normal
 * cursor concept
 */
public class JWindowEx extends JWindow implements IWaitSupport {
  private static final long serialVersionUID = 1L;

  private boolean m_waitCursor;

  public JWindowEx() {
    super();
  }

  public JWindowEx(Window w) {
    super(w);
  }

  @Override
  protected JRootPane createRootPane() {
    return new JRootPaneEx() {
      private static final long serialVersionUID = 1L;
    };
  }

  public boolean isWaitCursor() {
    return m_waitCursor;
  }

  public void setWaitCursor(boolean b) {
    if (b != m_waitCursor) {
      m_waitCursor = b;
      Component comp = getContentPane();
      if (m_waitCursor) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      }
      else {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    }
  }

}
