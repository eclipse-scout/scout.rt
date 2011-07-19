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
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * This widget may be used instead of the JTextFieldWithDropDownButton when you have not enough space to display
 * text and icon, which is the case for date from/to fields where you have very limited space for displaying the date
 * text (i.e. "21.12.2010") and the date icon on the right. {@link LegacyJTextFieldWithTransparentIcon} solves this
 * problem as it allows the text to overlay the icon. Whenever this
 * happens,
 * the icon becomes transparent for better readability.
 */
public class LegacyJTextFieldWithTransparentIcon extends JTextFieldWithTransparentIcon {

  private static final long serialVersionUID = 1L;

  @Override
  protected Icon createTransparentIcon() {
    return new Icon() {
      @Override
      public int getIconHeight() {
        return 0;
      }

      @Override
      public int getIconWidth() {
        return 0;
      }

      @Override
      public void paintIcon(Component c, Graphics g, int x, int y) {
        Icon icon = getIconForCurrentState();
        if (icon != null) {
          x = getWidth() - icon.getIconWidth() - getInsetsRight();
          y = (getHeight() - icon.getIconHeight()) / 2;
          icon.paintIcon(c, g, x, Math.max(y, getInsets().top));
        }
      }
    };
  }

  @Override
  protected int getInsetsRight() {
    return getInsets().right;
  }
}
