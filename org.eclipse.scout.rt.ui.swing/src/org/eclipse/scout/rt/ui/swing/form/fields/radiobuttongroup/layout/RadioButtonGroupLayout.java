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
package org.eclipse.scout.rt.ui.swing.form.fields.radiobuttongroup.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;

import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.form.fields.AbstractLayoutManager2;

public class RadioButtonGroupLayout extends AbstractLayoutManager2 {

  private IRadioButtonGroup<?> m_scoutField;
  private int m_hgap;
  private int m_vgap;
  // cache
  private Component[][] m_buttons;
  private Dimension[][] m_sizes;

  public RadioButtonGroupLayout(IRadioButtonGroup<?> scoutField, int hgap, int vgap) {
    m_scoutField = scoutField;
    m_hgap = hgap;
    m_vgap = vgap;
  }

  @Override
  protected Dimension getLayoutSize(Container parent, int sizeflag) {
    Dimension result = new Dimension();
    for (int r = 0; r < m_buttons.length; r++) {
      int w = 0;
      int h = 0;
      int colCount = m_buttons[r].length;
      if (colCount > 0) {
        for (int c = 0; c < colCount; c++) {
          if (m_buttons[r][c] != null) {
            Dimension d = m_sizes[r][c];
            w += d.width;
            h = Math.max(h, d.height);
          }
        }
      }
      result.width = Math.max(result.width, w);
      result.height += h;
    }
    //add gaps
    if (m_buttons.length > 0) {
      result.width += Math.max(0, m_buttons[0].length - 1) * m_hgap;
      result.height += Math.max(0, m_buttons.length - 1) * m_vgap;
    }
    return result;
  }

  @Override
  protected void validateLayout(Container parent) {
    int rowCount = Math.max(1, m_scoutField.getGridRowCount());
    int colCount = Math.max(1, m_scoutField.getGridColumnCount());
    m_buttons = new Component[rowCount][colCount];
    m_sizes = new Dimension[rowCount][colCount];
    int index = 0;
    for (int i = 0, ni = parent.getComponentCount(); i < ni; i++) {
      if (parent.getComponent(i).isVisible()) {
        m_buttons[index / colCount][index % colCount] = parent.getComponent(i);
        Dimension d = SwingLayoutUtility.getSize(parent.getComponent(i), MIN_SIZE);
        m_sizes[index / colCount][index % colCount] = d;
        index++;
      }
    }
  }

  @Override
  public void layoutContainer(Container parent) {
    verifyLayout(parent);
    synchronized (parent.getTreeLock()) {
      /*
       * necessary as workaround for awt bug: when component does not change
       * size, its reported minimumSize, preferredSize and maximumSize are
       * cached instead of beeing calculated using layout manager
       */
      if (!SwingUtility.IS_JAVA_7_OR_GREATER && SwingUtility.DO_RESET_COMPONENT_BOUNDS) {
        SwingUtility.setZeroBounds(parent.getComponents());
      }
      //
      int w = parent.getWidth();
      int h = parent.getHeight();
      int rowCount = m_buttons.length;
      int colCount = (rowCount > 0 ? m_buttons[0].length : 0);
      int hWithoutGaps = h - Math.max(0, rowCount - 1) * m_vgap;
      //
      if (colCount > 0) {
        // check if some radio columns need more space
        int[] widths = new int[colCount];
        int[] minWidths = new int[colCount];
        int[] y = new int[rowCount + 1];
        y[y.length - 1] = h + m_vgap;
        for (int r = 0; r < rowCount; r++) {
          for (int c = 0; c < colCount; c++) {
            if (m_sizes[r][c] != null) {
              minWidths[c] = Math.max(minWidths[c], m_sizes[r][c].width);
            }
          }
          y[r] = r * hWithoutGaps / rowCount + r * m_vgap;
        }
        int excess = 0;
        for (int c = 0; c < colCount; c++) {
          int candidateWidth = (c + 1) * w / colCount - (c) * w / colCount;
          if (minWidths[c] > candidateWidth) {
            excess += (minWidths[c] - candidateWidth);
          }
          widths[c] = Math.max(candidateWidth, minWidths[c]);
        }
        while (excess > 0) {
          int shrinkableCount = 0;
          int gcd = Integer.MAX_VALUE;// greatest common delta of all still
          // shrinkable columns
          for (int c = 0; c < colCount; c++) {
            if (widths[c] > minWidths[c]) {
              shrinkableCount++;
              gcd = Math.min(gcd, widths[c] - minWidths[c]);
            }
          }
          if (shrinkableCount == 0) {
            break;
          }
          int delta = (Math.min(excess, gcd) + shrinkableCount - 1) / shrinkableCount;
          for (int c = 0; c < colCount && excess > 0; c++) {
            if (widths[c] > minWidths[c]) {
              widths[c] -= delta;
              excess -= delta;
            }
          }
        }
        int[] x = new int[colCount + 1];
        for (int i = 0; i < widths.length; i++) {
          x[i + 1] = x[i] + ((widths[i]));
        }
        // set bounds
        for (int r = 0; r < rowCount; r++) {
          for (int c = 0; c < colCount; c++) {
            Rectangle bounds = new Rectangle(x[c], y[r], x[c + 1] - x[c], y[r + 1] - y[r] - m_vgap);
            if (m_buttons[r][c] != null) {
              m_buttons[r][c].setBounds(bounds);
            }
          }
        }
      }
    }
  }

}
