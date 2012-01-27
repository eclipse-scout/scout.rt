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
package org.eclipse.scout.rt.ui.swing.ext.activitymap;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Arrays;

import javax.swing.JComponent;

/**
 * Range selection over the cell grid
 */
public class JSelector extends JComponent {
  private static final long serialVersionUID = 1L;

  private JActivityMap m_map;
  private boolean m_mousePressed;

  public JSelector(JActivityMap map) {
    super();
    setOpaque(false);
    m_map = map;
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        checkCursorInternal(e.getPoint());
      }

      @Override
      public void mouseExited(MouseEvent e) {
        checkCursorInternal(new Point(-100, -100));
      }

      @Override
      public void mousePressed(MouseEvent e) {
        m_mousePressed = true;
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        m_mousePressed = false;
        checkCursorInternal(e.getPoint());
      }
    });
    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        checkCursorInternal(e.getPoint());
      }
    });
  }

  private void checkCursorInternal(Point p) {
    if (!m_mousePressed) {
      Cursor c = null;
      if (Math.abs(p.x - 0) <= 4) {
        c = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
      }
      else if (Math.abs(p.x - getWidth()) <= 4) {
        c = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
      }
      setCursor(c);
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    int w = getWidth();
    int[] indexes = m_map.getSelectedRows();
    double[] range = m_map.getSelectedRange();
    if (indexes.length > 0 && range != null) {
      int modelRowCount = m_map.getModel().getRowCount();
      for (int i = 0; i < indexes.length; i++) {
        if (indexes[i] >= modelRowCount) {
          indexes[i] = -1;
        }
      }
      if (indexes.length > 0 && range[1] - range[0] > 0) {
        // build row sets of successive row indexes
        int[] sortedIndexesFrom = new int[indexes.length];
        int[] sortedIndexesTo = new int[indexes.length];
        System.arraycopy(indexes, 0, sortedIndexesFrom, 0, indexes.length);
        Arrays.sort(sortedIndexesFrom);
        System.arraycopy(sortedIndexesFrom, 0, sortedIndexesTo, 0, sortedIndexesFrom.length);
        for (int i = sortedIndexesFrom.length - 1; i > 0; i--) {
          if (sortedIndexesFrom[i] == sortedIndexesTo[i - 1] + 1) {
            sortedIndexesTo[i - 1] = sortedIndexesTo[i];
            sortedIndexesFrom[i] = -1;
            sortedIndexesTo[i] = -1;
          }
        }
        // process sections
        for (int i = 0; i < sortedIndexesFrom.length; i++) {
          if (sortedIndexesFrom[i] >= 0) {
            Rectangle r = m_map.getCellRect(sortedIndexesFrom[i], sortedIndexesTo[i], range);
            // start
            g.setColor(new Color(0x55ff55));
            g.fillRect(1, r.y, 3, r.height);
            // end
            g.setColor(new Color(0xff5555));
            g.fillRect(w - 3 - 1, r.y, 3, r.height);
            // box
            g.setColor(Color.black);
            g.drawRect(0, r.y, w - 1, r.height);
          }
        }
      }
    }
  }

}
