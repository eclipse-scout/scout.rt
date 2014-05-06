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
package org.eclipse.scout.rt.ui.swing.ext.decoration;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseEvent;

/**
 *
 */
public class DecorationMouseEvent extends MouseEvent {
  private static final long serialVersionUID = 1L;

  private Cursor m_currentCursor;

  private Cursor m_cursorToApply;
  private boolean m_repaintNeeded;

  public DecorationMouseEvent(MouseEvent e) {
    super((e.getSource() instanceof Component) ? ((Component) e.getSource()) : (null), e.getID(), e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(),
        e.isPopupTrigger(), e.getButton());
  }

  public Cursor getCursorToApply() {
    return m_cursorToApply;
  }

  public void setCursorToApply(Cursor cursorToApply) {
    m_cursorToApply = cursorToApply;
  }

  public Cursor getCurrentCursor() {
    return m_currentCursor;
  }

  public void setCurrentCursor(Cursor currentCursor) {
    m_currentCursor = currentCursor;
  }

  public boolean isRepaintNeeded() {
    return m_repaintNeeded;
  }

  public void setRepaintNeeded(boolean repaintNeeded) {
    m_repaintNeeded = repaintNeeded;
  }
}
