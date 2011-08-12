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
import javax.swing.UIManager;

public class CheckboxIcon implements Icon {
  protected static final int WIDTH = 9;
  protected static final int HEIGHT = 9;

  private boolean m_selected = false;
  private boolean m_enabled = true;

  public boolean isSelecetd() {
    return m_selected;
  }

  public void setSelected(boolean on) {
    m_selected = on;
  }

  public boolean isEnabled() {
    return m_enabled;
  }

  public void setEnabled(boolean on) {
    m_enabled = on;
  }

  @Override
  public int getIconWidth() {
    return WIDTH;
  }

  @Override
  public int getIconHeight() {
    return HEIGHT;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    g.setColor(Color.lightGray);
    if (m_selected) {
      g.drawLine(0, 0, 4, 0);
      g.drawLine(0, 0, 0, 8);
      g.drawLine(0, 8, 8, 8);
      g.drawLine(8, 5, 8, 8);
      if (isEnabled()) {
        g.setColor(Color.black);
      }
      else {
        g.setColor(UIManager.getColor("textInactiveText"));
      }
      g.drawLine(4, 4, 8, 0);
      g.drawLine(4, 5, 8, 1);
      g.drawLine(4, 6, 8, 2);
      g.drawLine(2, 2, 2, 4);
      g.drawLine(3, 3, 3, 5);
    }
    else {
      g.drawRect(0, 0, 8, 8);
    }
  }
}
