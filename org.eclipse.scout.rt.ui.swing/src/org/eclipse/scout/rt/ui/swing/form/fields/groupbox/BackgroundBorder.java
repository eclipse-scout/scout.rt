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
package org.eclipse.scout.rt.ui.swing.form.fields.groupbox;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;

public class BackgroundBorder extends AbstractBorder {
  private static final long serialVersionUID = 1L;

  private Icon m_icon;
  private int m_halign;
  private int m_valign;

  public BackgroundBorder(Icon icon, int halign, int valign) {
    m_icon = icon;
    m_halign = halign;
    m_valign = valign;
  }

  @Override
  public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
    if (m_icon != null) {
      int iconW = m_icon.getIconWidth();
      int iconH = m_icon.getIconHeight();
      int anchorX = x;
      int anchorY = y;
      switch (m_halign) {
        case SwingConstants.LEFT: {
          anchorX = x;
          break;
        }
        case SwingConstants.CENTER: {
          anchorX = x + (w - iconW) / 2;
          break;
        }
        case SwingConstants.RIGHT: {
          anchorX = x + (w - iconW);
          break;
        }
      }
      switch (m_valign) {
        case SwingConstants.TOP: {
          anchorY = y;
          break;
        }
        case SwingConstants.CENTER: {
          anchorY = y + (h - iconH) / 2;
          break;
        }
        case SwingConstants.BOTTOM: {
          anchorY = y + (h - iconH);
          break;
        }
      }
      m_icon.paintIcon(c, g, anchorX, anchorY);
    }
  }
}
