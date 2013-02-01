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
package org.eclipse.scout.rt.ui.swt.ext.table;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;
import org.eclipse.scout.rt.ui.swt.ext.table.internal.TableMultilineListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TableEx extends Table {

  private static final int TEXT_MARGIN_Y = 1;
  private static final int TEXT_MARGIN_X = 6;

  private P_MouseHoverListener m_mouseHoverListener = new P_MouseHoverListener();
  private boolean m_readOnly;

  public TableEx(Composite parent, int style, ITable table) {
    super(parent, style);
    if (table != null) {
      int rowHeight = table.getRowHeightHint() - 2 * TEXT_MARGIN_Y;
      boolean multiline = table.isMultilineText();
      Set<Integer> wrapTextColumns = getWrapTextColumnIdxs(table);
      if (multiline || wrapTextColumns.size() > 0) {
        Listener multilineListener = new TableMultilineListener(multiline, rowHeight, wrapTextColumns, TEXT_MARGIN_X, TEXT_MARGIN_Y);
        addListener(SWT.MeasureItem, multilineListener);
        addListener(SWT.EraseItem, multilineListener);
        addListener(SWT.PaintItem, multilineListener);
      }
    }
  }

  private Set<Integer> getWrapTextColumnIdxs(ITable table) {
    Set<Integer> res = new HashSet<Integer>();
    for (IColumn column : table.getColumns()) {
      if (column instanceof IStringColumn && ((IStringColumn) column).isTextWrap()) {
        res.add(column.getColumnIndex());
      }
    }
    return res;
  }

  @Override
  protected void checkSubclass() {
  }

  private int m_insideSetTopIndex;

  /**
   * bug fix of swt in win32 when using MeasureListener due to scrollfix in setTopIndex
   * <p>
   * adding loop detection to avoid stack overflow
   * 
   * <pre>
   * ...
   *   at org.eclipse.swt.widgets.Table.setTopIndex(Table.java:5223)
   *   at org.eclipse.swt.widgets.Table.setItemHeight(Table.java:4646)
   *   at org.eclipse.swt.widgets.Table.setItemHeight(Table.java:4700)
   *   at org.eclipse.swt.widgets.Table.sendMeasureItemEvent(Table.java:3761)
   *   at org.eclipse.swt.widgets.Table.hitTestSelection(Table.java:2877)
   *   at org.eclipse.swt.widgets.Table.setTopIndex(Table.java:5223)
   *   at org.eclipse.swt.widgets.Table.setItemHeight(Table.java:4646)
   *   at org.eclipse.swt.widgets.Table.setItemHeight(Table.java:4700)
   *   at org.eclipse.swt.widgets.Table.sendMeasureItemEvent(Table.java:3761)
   *   at org.eclipse.swt.widgets.Table.hitTestSelection(Table.java:2877)
   *   at org.eclipse.swt.widgets.Table.setTopIndex(Table.java:5223)
   * </pre>
   */
  @Override
  public void setTopIndex(int index) {
    if (m_insideSetTopIndex > 0) {
      return;
    }
    try {
      m_insideSetTopIndex++;
      super.setTopIndex(index);
    }
    finally {
      m_insideSetTopIndex--;
    }
  }

  @Override
  public Point computeSize(int hint, int hint2, boolean changed) {
    /*
     * workaround since compute size on the table returns the sum of all columns
     * plus 20px.
     */
    Point size = super.computeSize(hint, hint2, changed);
    TableColumn[] columns = getColumns();
    if (columns != null) {
      int x = 0;
      for (TableColumn col : columns) {
        x += col.getWidth();
      }
      x += columns.length * getGridLineWidth();
      size.x = x;
    }
    return size;
  }

  public void setReadOnly(boolean readOnly) {
    if (m_readOnly != readOnly) {
      m_readOnly = readOnly;
    }
  }

  public boolean isReadOnly() {
    return m_readOnly;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (enabled) {
      setForeground(null);
    }
    else {
      setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
    }
  }

  private class P_MouseHoverListener extends MouseTrackAdapter implements MouseMoveListener {
    private TableItem m_mouseHoverItem;

    // case 1: mouse hover an item
    @Override
    public void mouseMove(MouseEvent e) {
      TableItem item = getItem(new Point(e.x, e.y));
      // another item under the mouse?
      if (item != m_mouseHoverItem && item != null) {
        // old mouse over item valid?
        if (m_mouseHoverItem != null && !m_mouseHoverItem.isDisposed()) {
          // change color and redraw old item
          m_mouseHoverItem.setBackground(null);
          final Rectangle b = m_mouseHoverItem.getBounds();
          redrawInternal(b);
        }
        // store, change color and redraw new item
        m_mouseHoverItem = item;
        m_mouseHoverItem.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        Rectangle b = m_mouseHoverItem.getBounds();
        redrawInternal(b);
      }
    }

    // case 2: exit from the table
    @Override
    public void mouseExit(MouseEvent e) {
      if (m_mouseHoverItem != null && !m_mouseHoverItem.isDisposed()) {
        m_mouseHoverItem.setBackground(null);
        Rectangle b = m_mouseHoverItem.getBounds();
        redrawInternal(b);
      }
    }

    // asynchronous redraw
    private void redrawInternal(final Rectangle b) {
      getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          if (!isDisposed()) {
            redraw(b.x, b.y, b.width, b.height, false);
          }
        }
      });
    }
  } // end class P_MouseHoverListener

}
