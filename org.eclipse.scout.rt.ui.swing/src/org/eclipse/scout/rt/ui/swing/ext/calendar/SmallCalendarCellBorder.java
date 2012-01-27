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
package org.eclipse.scout.rt.ui.swing.ext.calendar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class SmallCalendarCellBorder extends AbstractBorder {
  private static final long serialVersionUID = 1L;
  private Border m_defaultBorder;
  private Border m_selectBorder;
  private Border m_focusBorder;
  private Border m_selectAndFocusBorder;

  public SmallCalendarCellBorder(Color foreground) {
    m_defaultBorder = new EmptyBorder(1, 1, 1, 1);
    m_selectBorder = new LineBorder(foreground, 1);
    m_focusBorder = new LineBorder(Color.lightGray, 1);
    m_selectAndFocusBorder = new CompoundBorder(
        new LineBorder(foreground, 1),
        new LineBorder(Color.lightGray, 1)
        );
  }

  private Border getInternalBorder(Component c) {
    AbstractCalendarCell cell = (AbstractCalendarCell) c;
    if (cell.isSelected() && cell.isFocusOwner()) {
      return m_selectAndFocusBorder;
    }
    else if (cell.isSelected()) {
      return m_selectBorder;
    }
    else if (cell.isFocusOwner()) {
      return m_focusBorder;
    }
    else {
      return m_defaultBorder;
    }
  }

  @Override
  public Insets getBorderInsets(Component c) {
    Insets in = new Insets(0, 0, 0, 0);
    return getBorderInsets(c, in);
  }

  @Override
  public Insets getBorderInsets(Component c, Insets in) {
    Border border = getInternalBorder(c);
    if (border instanceof AbstractBorder) {
      return ((AbstractBorder) border).getBorderInsets(c, in);
    }
    else {
      Insets t = border.getBorderInsets(c);
      in.top = t.top;
      in.left = t.left;
      in.bottom = t.bottom;
      in.right = t.right;
      return in;
    }
  }

  @Override
  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Border border = getInternalBorder(c);
    border.paintBorder(c, g, x, y, width, height);
  }
}
