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
package org.eclipse.scout.rt.ui.swing.basic.activitymap;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Shape;

import javax.swing.UIManager;

import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityCell;
import org.eclipse.scout.rt.ui.swing.ext.LookAndFeelUtility;
import org.eclipse.scout.rt.ui.swing.ext.activitymap.DefaultActivityComponent;

public class SwingActivityComponent extends DefaultActivityComponent {
  private static final long serialVersionUID = 1L;

  private ActivityCell m_cell;

  public SwingActivityComponent(ActivityCell cell, int rowIndex) {
    super(rowIndex);
    m_cell = cell;
    setText(cell.getText());
    setToolTipText(cell.getTooltipText());
  }

  public ActivityCell getScoutActivityCell() {
    return m_cell;
  }

  @Override
  protected void paintComponent(Graphics g) {
    int w = getWidth();
    int h = getHeight();
    Shape oldShape = g.getClip();
    try {
      if (m_cell.getMajorColor() != null) {
        // major fill
        float f = m_cell.getMajorValue();
        if (f < 0) {
          f = 0;
        }
        if (f > 1) {
          f = 1;
        }
        int dy = (int) (f * (h - 2 - 2));
        // clear bg
        g.setColor(Color.white);
        g.fillRect(2, 2, w - 2 - 2, h - 2 - 2);
        g.setColor(Color.decode("0x" + m_cell.getMajorColor()));
        if (dy > 0) {
          g.fillRect(2, h - 2 - dy + 1, w - 2 - 2 + 1, dy);
        }
        // major border
        g.drawRect(2, 2, w - 2 - 2, h - 2 - 2);
        if (m_cell.getMinorColor() != null) {
          // minor fill
          f = m_cell.getMinorValue();
          if (f < 0) {
            f = 0;
          }
          if (f > 1) {
            f = 1;
          }
          dy = (int) (f * (h - 2 - 4 - 4));
          // clear bg
          g.setColor(Color.white);
          g.fillRect(2 + 4, 2 + 4, w - 2 - 2 - 4 - 4, h - 2 - 2 - 4 - 4);
          g.setColor(Color.decode("0x" + m_cell.getMinorColor()));
          if (dy > 0) {
            g.fillRect(2 + 4, h - 2 - 4 - dy + 1, w - 2 - 2 - 4 - 4 + 1, dy);
          }
          // minor border
          g.drawRect(2 + 4, 2 + 4, w - 2 - 2 - 4 - 4, h - 2 - 2 - 4 - 4);
        }
      }
      else {
        // fill
        g.setColor(getBackground());
        g.fillRect(2, 2, getWidth() - 3, getHeight() - 3);
      }
      // focus
      if (isFocusOwner()) {
        g.setColor(UIManager.getColor("Table.focusCellForeground"));
        LookAndFeelUtility.drawFocus(g, 1, 1, getWidth() - 2, getHeight() - 2);
      }
      // text
      g.setColor(getForeground());
      g.setFont(getFont());
      String s = getText();
      if (s != null) {
        FontMetrics fm = g.getFontMetrics();
        int sWidth = fm.stringWidth(s);
        int dx = Math.max(0, getWidth() - 4 - sWidth);
        // centered
        g.clipRect(2, 2, getWidth() - 4, getHeight() - 4);
        g.drawString(s, 2 + dx / 2, getHeight() - 2 - fm.getDescent());
      }
    }
    finally {
      g.setClip(oldShape);
    }
  }

}
