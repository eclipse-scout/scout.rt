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

  public JTextPaneEx() {
    super();
  }

  /**
   * Deep inside swing the text field editor View.class is only checking for
   * isEnable() This is not changeable by a LookAndFeel. This is handled in the
   * corresponding ...Ex sub classes of JTextComponent
   */
  @Override
  public Color getForeground() {
    if (isEditable()) {
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
  public Point getToolTipLocation(MouseEvent e) {
    return SwingUtility.getAdjustedToolTipLocation(e, this, getTopLevelAncestor());
  }
}
