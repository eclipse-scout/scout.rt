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
package org.eclipse.scout.rt.ui.swt.form.radiobuttongroup.layout;

import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup;
import org.eclipse.scout.rt.ui.swt.util.SwtLayoutUtility;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public class RadioButtonGroupLayout extends Layout {

  private IRadioButtonGroup<?> m_scoutField;
  private int m_hgap;
  private int m_vgap;
  // cache
  private Control[][] m_buttons;
  private Point[][] m_sizes;

  public RadioButtonGroupLayout(IRadioButtonGroup<?> scoutField, int hgap, int vgap) {
    m_scoutField = scoutField;
    m_hgap = hgap;
    m_vgap = vgap;
  }

  @Override
  protected Point computeSize(Composite composite, int hint, int hint2, boolean flushCache) {
    validateLayout(composite);
    Point result = new Point(0, 0);
    for (int r = 0; r < m_buttons.length; r++) {
      int w = 0;
      int h = 0;
      int colCount = m_buttons[r].length;
      for (int c = 0; c < colCount; c++) {
        if (m_buttons[r][c] != null) {
          Point d = m_sizes[r][c];
          w += d.x;
          h = Math.max(h, d.y);
        }
      }
      result.x = Math.max(result.x, w);
      result.y += h;
    }
    //add gaps
    if (m_buttons.length > 0) {
      result.x += Math.max(0, m_buttons[0].length - 1) * m_hgap;
      result.y += Math.max(0, m_buttons.length - 1) * m_vgap;
    }
    return result;
  }

  protected void validateLayout(Composite parent) {
    int rowCount = Math.max(1, m_scoutField.getGridRowCount());
    int colCount = Math.max(1, m_scoutField.getGridColumnCount());
    m_buttons = new Control[rowCount][colCount];
    m_sizes = new Point[rowCount][colCount];
    int index = 0;
    for (Control c : parent.getChildren()) {
      if (c.getVisible()) {
        m_buttons[index / colCount][index % colCount] = c;
        Point d = new Point(SwtLayoutUtility.computeMinimumWidth(c, false), 0);
        m_sizes[index / colCount][index % colCount] = d;
        index++;
      }
    }
  }

  @Override
  protected void layout(Composite composite, boolean flushCache) {
    if (flushCache) {
      validateLayout(composite);
    }
    //
    Rectangle clientArea = composite.getClientArea();
    int w = clientArea.width;
    int h = clientArea.height;
    int rowCount = m_buttons.length;
    int colCount = rowCount > 0 ? m_buttons[0].length : 0;
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
            minWidths[c] = Math.max(minWidths[c], m_sizes[r][c].x);
          }
        }
        y[r] = r * hWithoutGaps / rowCount + r * m_vgap;
      }
      int excess = 0;
      for (int c = 0; c < colCount; c++) {
        int candidateWidth = (c + 1) * w / colCount - c * w / colCount;
        if (minWidths[c] > candidateWidth) {
          excess += minWidths[c] - candidateWidth;
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
        x[i + 1] = x[i] + widths[i];
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
