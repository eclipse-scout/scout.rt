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
package org.eclipse.scout.rt.ui.swing.basic.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.Icon;

public class SortIcon implements Icon {
  private static int W = 7;
  private static int H = 4;
  private static Insets insets = new Insets(3, 1, 3, 1);
  private boolean m_asc;

  public SortIcon(boolean asc) {
    m_asc = asc;
  }

  public int getIconWidth() {
    return W + insets.left + insets.right;
  }

  public int getIconHeight() {
    return H + insets.top + insets.bottom;
  }

  public void paintIcon(Component c, Graphics g, int x0, int y0) {
    g.setColor(Color.gray);
    g.translate(x0 + insets.left, y0 + insets.top);

    int x = W / 2;
    int w = 1;
    int y = (m_asc ? 0 : H - 1);
    int dy = (m_asc ? 1 : -1);
    while (y >= 0 && y < H) {
      g.drawLine(x, y, x + w - 1, y);
      y += dy;
      x -= 1;
      w += 2;
    }

    g.translate(-x0 - insets.left, -y0 - insets.top);
  }
}
