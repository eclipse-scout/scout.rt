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
package org.eclipse.scout.rt.ui.swing.icons;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * paints a composite of multiple icons with optional separators inbetween
 */
public class CompositeIcon implements Icon {
  private int m_gap;
  private Icon[] m_icons;
  private int m_paintedX;
  private int w, h;

  public CompositeIcon(int gap, Icon... icons) {
    m_gap = gap;
    m_icons = icons;
    w = 0;
    h = 0;
    int index = 0;
    for (Icon icon : m_icons) {
      if (icon != null) {
        if (index > 0) {
          w += m_gap;
        }
        w = w + icon.getIconWidth();
        h = Math.max(h, icon.getIconHeight());
        index++;
      }
    }
  }

  @Override
  public int getIconWidth() {
    return w;
  }

  @Override
  public int getIconHeight() {
    return h;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    m_paintedX = x;
    int offsetX = 0;
    int index = 0;
    for (Icon icon : m_icons) {
      if (icon != null) {
        if (index > 0) {
          if (m_gap > 0) {
            g.setColor(Color.lightGray);
            g.drawLine(x + offsetX + m_gap / 2, y, x + offsetX + m_gap / 2, w + h);
          }
          offsetX += m_gap;
        }
        int offsetY = Math.max(0, (h - icon.getIconHeight()) / 2);
        icon.paintIcon(c, g, x + offsetX, offsetY + y);
        offsetX += icon.getIconWidth();
        index++;
      }
    }
  }

  public int getIconIndexFor(int x) {
    x = x - m_paintedX;
    int offsetX = 0;
    // before first
    if (x < 0) {
      return m_icons.length > 0 ? 0 : -1;
    }
    // inside
    for (int i = 0; i < m_icons.length; i++) {
      if (m_icons[i] != null) {
        if (x >= offsetX && x <= offsetX + m_icons[i].getIconWidth()) {
          return i;
        }
        offsetX += m_icons[i].getIconWidth();
        offsetX += m_gap;
      }
    }
    // after last
    return m_icons.length - 1;
  }

}
