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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;

public class SwingTableColumnModel extends DefaultTableColumnModel implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private ISwingEnvironment m_env;
  private SwingScoutTable m_swingScoutTable;
  private MouseListener m_swingTableHeaderMouseListener;
  private boolean m_mousePressedInTableHeader;

  public SwingTableColumnModel(ISwingEnvironment env, SwingScoutTable swingScoutTable) {
    m_env = env;
    m_swingScoutTable = swingScoutTable;
    m_swingTableHeaderMouseListener = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        m_mousePressedInTableHeader = true;
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        m_mousePressedInTableHeader = false;
      }

      @Override
      public void mouseExited(MouseEvent e) {
        m_mousePressedInTableHeader = false;
      }
    };

    // init columnMargin as defined by the JTable (which is set by the look and feel)
    // otherwise the new column model instance would reset the column margin to the default margin (1)
    // used by DefaultTableColumnModel when we set the column model on the JTable.
    columnMargin = m_swingScoutTable.getSwingTable().getIntercellSpacing().width;

    initializeColumns();
  }

  public void notifyScoutModelChanged() {
    initializeColumns();
  }

  //avoid negative indices
  @Override
  public TableColumn getColumn(int columnIndex) {
    if (columnIndex < 0) {
      columnIndex = 0;
    }
    return super.getColumn(columnIndex);
  }

  public void initializeColumns() {
    if (m_swingScoutTable.getSwingTable() != null) {
      if (m_swingScoutTable.getSwingTable().getTableHeader() != null) {
        m_swingScoutTable.getSwingTable().getTableHeader().removeMouseListener(m_swingTableHeaderMouseListener);
      }
    }
    while (getColumnCount() > 0) {
      removeColumn(getColumn(0));
    }
    //
    if (m_swingScoutTable.getScoutObject() != null) {
      if (m_swingScoutTable.getSwingTable().getTableHeader() != null) {
        m_swingScoutTable.getSwingTable().getTableHeader().addMouseListener(m_swingTableHeaderMouseListener);
      }
      IColumn[] scoutCols = m_swingScoutTable.getScoutObject().getColumnSet().getVisibleColumns();
      for (int i = 0; i < scoutCols.length; i++) {
        SwingTableColumn swingColumn = m_env.createColumn(i, scoutCols[i]);
        // add column
        addColumn(swingColumn);
      }
    }
  }

  public IColumn swingToScoutColumn(int index) {
    if (index >= 0 && index < getColumnCount()) {
      return ((SwingTableColumn) getColumn(index)).getScoutColumn();
    }
    return null;
  }

  /**
   * override handler for storing column gui index
   */
  @Override
  protected void fireColumnMoved(TableColumnModelEvent e) {
    super.fireColumnMoved(e);
    if (m_swingScoutTable.getUpdateSwingFromScoutLock().isAcquired()) {
      return;
    }
    //
    if (m_swingScoutTable.getScoutObject() != null) {
      // store all columns because the move of one column also changes the
      // index of all other columns
      if (e.getFromIndex() != e.getToIndex()) {
        final IColumn col = ((SwingTableColumn) getColumn(e.getToIndex())).getScoutColumn();
        final int toIndex = e.getToIndex();
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            try {
              m_swingScoutTable.addIgnoredScoutEvent(TableEvent.class, "" + TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED);
              m_swingScoutTable.addIgnoredScoutEvent(TableEvent.class, "" + TableEvent.TYPE_COLUMN_ORDER_CHANGED);
              //
              m_swingScoutTable.getScoutObject().getUIFacade().fireColumnMovedFromUI(col, toIndex);
            }
            finally {
              m_swingScoutTable.removeIgnoredScoutEvent(TableEvent.class, "" + TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED);
              m_swingScoutTable.removeIgnoredScoutEvent(TableEvent.class, "" + TableEvent.TYPE_COLUMN_ORDER_CHANGED);
            }
          }
        };
        m_env.invokeScoutLater(t, 0);
        // end notify
      }
    }
  }

  /**
   * override handler for storing column width
   */
  @Override
  public void propertyChange(PropertyChangeEvent e) {
    super.propertyChange(e);
    if (m_mousePressedInTableHeader) {
      if (e.getPropertyName().equals("preferredWidth") || e.getPropertyName().equals("width")) {
        TableColumn column = (TableColumn) e.getSource();
        if (m_swingScoutTable != null) {
          ArrayList<TableColumn> list = new ArrayList<TableColumn>(1);
          list.add(column);
          m_swingScoutTable.storeColumnWidthsFromSwing(list);
        }
      }
    }
  }

  @Override
  public void moveColumn(int columnIndex, int newIndex) {
    if (isMoveColumnAllowed(columnIndex, newIndex)) {
      super.moveColumn(columnIndex, newIndex);
    }
  }

  /**
   * Ensure the first column to retain a non-editable column. Otherwise, the checkable and editable column would
   * be the very same column which is confusing.
   * 
   * @param oldIndex
   * @param newIndex
   * @return
   */
  private boolean isMoveColumnAllowed(int oldIndex, int newIndex) {
    ITable table = m_swingScoutTable.getScoutObject();
    if (!table.isCheckable() || oldIndex == newIndex) {
      return true;
    }

    // do not allow an editable column to be moved as first column
    if (newIndex == 0) {
      IColumn columnToMove = table.getColumnSet().getVisibleColumn(oldIndex);
      if (columnToMove.isEditable()) {
        return false;
      }
    }
    // do not allow the first column to be moved if the succeeding column is an editable column
    if (oldIndex == 0 && table.getColumnSet().getVisibleColumnCount() > 1) {
      IColumn succeedingColumn = table.getColumnSet().getVisibleColumn(1);
      if (succeedingColumn.isEditable()) {
        return false;
      }
    }
    return true;
  }
}
