/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.basic.table.celleditor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.scout.commons.BooleanUtility;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.basic.table.ISwtScoutTable;
import org.eclipse.scout.rt.ui.swt.basic.table.SwtScoutTable;
import org.eclipse.swt.widgets.TableColumn;

/**
 * <p>
 * Editing Support for {@link SwtScoutTable}.
 * </p>
 * Each editable column has its own {@link EditingSupport} instance.
 */
public class TableEditingSupport extends EditingSupport implements ICellEditorListener {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TableEditingSupport.class);

  private ISwtEnvironment m_environment;

  private ITable m_scoutTable;
  private IColumn<?> m_scoutColumn;
  private TableColumn m_swtColumn;

  public TableEditingSupport(TableViewer viewer, TableColumn swtColumn, ISwtEnvironment environment) {
    super(viewer);

    m_scoutColumn = (IColumn<?>) swtColumn.getData(ISwtScoutTable.KEY_SCOUT_COLUMN);
    m_swtColumn = swtColumn;
    m_scoutTable = m_scoutColumn.getTable();

    m_environment = environment;
  }

  @Override
  public TableViewer getViewer() {
    return (TableViewer) super.getViewer();
  }

  @Override
  protected boolean canEdit(Object element) {
    final ITableRow row = (ITableRow) element;

    final BooleanHolder editable = new BooleanHolder();
    try {
      m_environment.invokeScoutLater(new Runnable() {
        @Override
        public void run() {
          editable.setValue(m_scoutTable.isCellEditable(row, m_scoutColumn));
        }
      }, 2345).join(2345);
    }
    catch (InterruptedException e) {
      LOG.warn("Interrupted while waiting for the model to determine the cell's editability.", e);
    }
    return BooleanUtility.nvl(editable.getValue(), false);
  }

  @Override
  protected CellEditor getCellEditor(Object element) {
    CellEditor cellEditor = new TableCellEditor(getViewer(), m_swtColumn, (ITableRow) element, m_environment);
    cellEditor.addListener(this);
    return cellEditor;
  }

  @Override
  protected Object getValue(Object element) {
    // NOOP: The value is set into the cell-editor when it is created.
    return null;
  }

  @Override
  protected void setValue(Object element, Object value) {
    // NOOP: Only notify the model about completing the editing mode.
    //       The value itself is written back into the model by the widget's verify event.
    m_environment.invokeScoutLater(new Runnable() {
      @Override
      public void run() {
        m_scoutTable.getUIFacade().completeCellEditFromUI();
      }
    }, 0);
  }

  // == ICellEditorListener ==
  // To notify Scout about canceling editing.

  @Override
  public void cancelEditor() {
    m_environment.invokeScoutLater(new Runnable() {
      @Override
      public void run() {
        m_scoutTable.getUIFacade().cancelCellEditFromUI();
      }
    }, 0);
  }

  @Override
  public void applyEditorValue() {
    // NOOP: is done in #setValue.
  }

  @Override
  public void editorValueChanged(boolean oldValidState, boolean newValidState) {
    // NOOP
  }
}
