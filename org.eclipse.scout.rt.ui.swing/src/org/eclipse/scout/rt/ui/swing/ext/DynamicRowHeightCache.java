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
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SizeSequence;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;

/**
 * Support for table with dynamic vairable row heights
 */
public class DynamicRowHeightCache {
  private static final long serialVersionUID = 1L;

  private final JTable m_table;
  private final SizeSequence m_sizes;
  private boolean m_sizesValid;
  private OptimisticLock m_validationLock;

  public DynamicRowHeightCache(JTable table) {
    this(table, table.getRowHeight());
  }

  public DynamicRowHeightCache(JTable table, int rowHeight) {
    m_table = table;
    m_sizes = new SizeSequence(table.getRowCount(), rowHeight);
    m_sizesValid = false;
    m_validationLock = new OptimisticLock();
  }

  public int getRowHeight(int row) {
    validateSizes();
    return m_sizes.getSize(row);
  }

  public int getRowAtPoint(Point p) {
    validateSizes();
    int index = m_sizes.getIndex(p.y);
    if (index < 0) {
      return -1;
    }
    if (index >= m_table.getRowCount()) {
      return -1;
    }
    return index;
  }

  public int getPointForRow(int row) {
    validateSizes();
    if (row < 0) {
      return 0;
    }
    if (row >= m_table.getRowCount()) {
      return m_table.getHeight();
    }
    return m_sizes.getPosition(row);
  }

  protected void validateSizes() {
    if (!m_sizesValid) {
      m_sizesValid = true;
      try {
        if (!m_validationLock.acquire()) {
          return;
        }
        //
        int rowCount = m_table.getRowCount();
        TableColumnModel cm = m_table.getColumnModel();
        int defaultHeight = m_table.getRowHeight();
        for (int i = 0; i < rowCount; i++) {
          int h = determinePreferredRowHeight(i, cm, defaultHeight);
          m_sizes.setSize(i, h);
        }
      }
      finally {
        m_validationLock.release();
      }
    }
  }

  protected int determinePreferredRowHeight(int r, TableColumnModel cm, int defaultHeight) {
    int colCount = cm.getColumnCount();
    int maxHeight = defaultHeight;
    for (int c = 0; c < colCount; c++) {
      TableCellRenderer renderer = m_table.getCellRenderer(r, c);
      JComponent comp = (JComponent) m_table.prepareRenderer(renderer, r, c);
      int cellW = cm.getColumn(c).getWidth() - cm.getColumnMargin();
      int h = SwingLayoutUtility.getPreferredLabelSize((JLabel) comp, cellW).height;
      maxHeight = Math.max(maxHeight, h);
    }
    return maxHeight;
  }

}
