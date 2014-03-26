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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JTextPane;

import org.eclipse.scout.rt.ui.swing.SwingUtility;

public class JTextPaneEx extends JTextPane {

  private static final long serialVersionUID = 1L;

  /**
   * This property is set from the Scout Model. It will determine the foreground color of the JTextPane. If
   * <code>true</code> the default text color will be shown, if <code>false</code> the disabled text color will be
   * shown.
   */
  private boolean m_enabledFromScout;

  public JTextPaneEx() {
    super();
    setEnabled(true);
  }

  @Override
  public Color getForeground() {
    if (m_enabledFromScout) {
      return super.getForeground();
    }
    else {
      return getDisabledTextColor();
    }
  }

  /**
   * Swing sets the cursor of a non-editable text pane to DEFAULT, instead of
   * setting it to null to inherit from the parent.
   */
  @Override
  public void setCursor(Cursor cursor) {
    if (cursor != null && cursor.getType() == Cursor.DEFAULT_CURSOR) {
      cursor = null;
    }
    super.setCursor(cursor);
  }

  @Override
  public void setEnabled(boolean enabled) {
    m_enabledFromScout = enabled;
  }

  /**
   * Always return <code>true</code> so that the JTextPane stays selectable. If we would return <code>false</code> the
   * JTextPane's content would not be selectable anymore
   */
  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public Point getToolTipLocation(MouseEvent e) {
    return SwingUtility.getAdjustedToolTipLocation(e, this, getTopLevelAncestor());
  }
}
