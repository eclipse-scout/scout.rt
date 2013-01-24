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

import java.awt.Font;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;

import org.eclipse.scout.rt.ui.swing.SwingUtility;

/**
 * fixed default focus keys
 */
public class JCheckBoxEx extends JCheckBox implements FocusListener {
  private static final long serialVersionUID = 1L;

  public JCheckBoxEx() {
    // focus
    addFocusListener(this);
    SwingUtility.installDefaultFocusHandling(this);
  }

  @Override
  public void focusGained(FocusEvent e) {
    repaint();
  }

  @Override
  public void focusLost(FocusEvent e) {
    repaint();
  }

  public void setMandatory(boolean mandatory) {
    // Note: (bsh 2010-09-29)
    // Currently, this seems to have no effect, as the font is overridden
    // later in SwingScoutFieldComposite.attachScout() by calling
    // "setFontFromScout(scoutField.getFont());". I'm not sure how to
    // fix this, but because currently Rayo is the main GUI, it does not
    // matter. TODO Someone should fix this for Rayo _and_ Orson.

    // change label font to bold
    Font f = getFont();
    if (f != null && (f.getStyle() == Font.BOLD) != mandatory) {
      f = new Font(f.getName(), mandatory ? Font.BOLD : Font.PLAIN, f.getSize());
    }
    setFont(f);
  }

  @Override
  public Point getToolTipLocation(MouseEvent e) {
    return SwingUtility.getAdjustedToolTipLocation(e, this, getTopLevelAncestor());
  }
}
