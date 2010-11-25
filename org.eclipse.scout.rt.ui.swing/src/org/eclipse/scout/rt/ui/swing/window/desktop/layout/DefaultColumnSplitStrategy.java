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
package org.eclipse.scout.rt.ui.swing.window.desktop.layout;

/**
 * Column split of a 3x3 matrix
 * 4 splits per row
 */
public class DefaultColumnSplitStrategy implements IMultiSplitStrategy {
  private int m_span;
  private int[][] m_location;
  private boolean m_initialValues;

  public DefaultColumnSplitStrategy() {
    m_span = 1000;
    m_location = new int[][]{new int[]{0, 250, 750, 1000}, new int[]{0, 250, 750, 1000}, new int[]{0, 250, 750, 1000}};
    m_initialValues = true;
  }

  @Override
  public void updateSpan(int newSpan) {
    int oldSpan = m_span;
    if (oldSpan != newSpan) {
      m_span = newSpan;
      for (int r = 0; r < 3; r++) {
        m_location[r][3] = m_span;
        if (m_initialValues) {
          m_location[r][1] = 250;
          m_location[r][2] = m_span - 250;
          if (m_location[r][1] + 20 > m_location[r][2]) {
            m_location[r][1] = m_span / 2 - 10;
            m_location[r][2] = m_span / 2 + 10;
          }
        }
        else {
          if (newSpan > oldSpan) {
            m_location[r][2] += (newSpan - oldSpan);
          }
          else {
            int dSpan = oldSpan - newSpan;
            dSpan = Math.min(dSpan, m_location[r][2] - m_location[r][1]);
            dSpan = Math.max(dSpan, 0);
            m_location[r][2] -= dSpan;
          }
        }
      }
    }
  }

  @Override
  public int getSplitLocation(int row, int col) {
    return m_location[row][col];
  }

  @Override
  public void setSplitLocation(int row, int col, int newLocation) {
    if (col == 0 || col == 3) return;
    //
    if (m_location[row][col - 1] + 20 < newLocation && newLocation + 20 < m_location[row][col + 1]) {
      m_location[row][col] = newLocation;
      m_initialValues = false;
    }
  }
}
