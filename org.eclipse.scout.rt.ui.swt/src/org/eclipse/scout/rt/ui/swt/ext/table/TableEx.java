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
import org.eclipse.scout.rt.ui.swt.ext.table.internal.EditableTableMarkerSupport;
import org.eclipse.scout.rt.ui.swt.ext.table.internal.TableMultilineListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class TableEx extends Table {

  private static final int TEXT_MARGIN_Y = 1;
  private static final int TEXT_MARGIN_X = 6;

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
      new EditableTableMarkerSupport(this);
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

}
