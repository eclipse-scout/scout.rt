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

import java.awt.Component;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;
import org.eclipse.scout.rt.ui.swing.ext.JTableEx;

class SwingTable extends JTableEx {
  private static final long serialVersionUID = 1L;

  public SwingTable() {
  }

  /**
   * Stores the optimial width of each column in the property "preferredWidth"
   */
  public void setOptimalColumnWidths() {
    int dw = getIntercellSpacing().width;
    Insets is = new Insets(0, 0, 0, 0);
    TableColumnModel tcm = getColumnModel();
    for (int c = 0, nc = getColumnCount(); c < nc; c++) {
      TableColumn tc = tcm.getColumn(c);
      Component hcomp = getTableHeader().getDefaultRenderer().getTableCellRendererComponent(this, tc.getHeaderValue(), false, false, 0, c);
      int wmax = SwingLayoutUtility.getSize(hcomp, SwingLayoutUtility.PREF).width;
      if (hcomp instanceof JComponent) {
        ((JComponent) hcomp).getInsets(is);
        wmax += is.left + is.right;
      }
      if (isDynamicRowHeight() && isColumnWrapped(tc)) {
        int w = getConfiguredWidth(tc);
        if (w > wmax) {
          wmax = w;
        }
      }
      for (int r = 0, nr = getRowCount(); r < nr; r++) {
        TableCellRenderer renderer = getCellRenderer(r, c);
        if (renderer != null) {
          JComponent comp = (JComponent) prepareRenderer(renderer, r, c);
          int w = SwingLayoutUtility.getSize(comp, SwingLayoutUtility.PREF).width;
          if (comp instanceof JComponent) {
            (comp).getInsets(is);
            w += is.left + is.right;
          }
          if (w > wmax) {
            wmax = w;
          }
        }
      }
      // resize column
      getColumnModel().getColumn(c).setPreferredWidth(wmax + dw);
    }
  }

  private boolean isColumnWrapped(TableColumn tc) {
    if (!(tc instanceof SwingTableColumn)) {
      return false;
    }
    IColumn scoutColumn = ((SwingTableColumn) tc).getScoutColumn();
    if (!(scoutColumn instanceof AbstractStringColumn)) {
      return false;
    }
    return ((AbstractStringColumn) scoutColumn).isTextWrap();
  }

  private int getConfiguredWidth(TableColumn tc) {
    if (!(tc instanceof SwingTableColumn)) {
      return 0;
    }
    return ((SwingTableColumn) tc).getScoutColumn().getWidth();
  }

}
