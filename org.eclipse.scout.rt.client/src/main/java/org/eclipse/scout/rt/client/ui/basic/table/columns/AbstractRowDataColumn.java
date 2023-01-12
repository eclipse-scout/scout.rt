/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.rt.client.dto.ColumnData;
import org.eclipse.scout.rt.client.dto.ColumnData.SdkColumnCommand;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableListener;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

/**
 * This class is intended to be used if some other (or all) columns of a table depend on values represented by
 * this one. All columns depending on this row data column should be annotated with {@link ColumnData} using
 * {@link SdkColumnCommand#IGNORE}.
 * <p/>
 * By default, this column is not displayable (i.e. {@link #getConfiguredDisplayable()} returns <code>false</code>).
 * <p/>
 * Use this regular expression for searching potential refactoring candidates
 *
 * <pre>
 * extends\s+AbstractColumn\s*<\s*(?!\w+Key\b|\bUid\b|\bRemoteFile\b|\bbyte\b)
 * </pre>
 * <p>
 */
@ClassId("3ffdbb40-21be-4bca-93b6-eea592cf030c")
public abstract class AbstractRowDataColumn<T> extends AbstractColumn<T> {

  private TableListener m_updateTableRowListener;

  @Override
  public void initColumn() {
    super.initColumn();
    if (getTable() != null) {
      if (m_updateTableRowListener != null) {
        getTable().removeTableListener(m_updateTableRowListener);
      }
      AbstractRowDataColumn<T> self = this;
      m_updateTableRowListener = e -> {
        try {
          getTable().setTableChanging(true);
          for (ITableRow row : e.getRows()) {
            // Trigger "updateTableColumn" when a row was inserted, or the value of this column is changed.
            // Do _not_ trigger the method when other columns change their values (this might lead to loops).
            if (e.getType() == TableEvent.TYPE_ROWS_INSERTED || e.getUpdatedColumns(row).contains(self)) {
              //updating other columns should not change the row status
              final int origStatus = row.getStatus();
              updateTableColumns(row, getValue(row));
              row.setStatus(origStatus);
            }
          }
        }
        catch (RuntimeException ex) {
          BEANS.get(ExceptionHandler.class).handle(ex);
        }
        finally {
          getTable().setTableChanging(false);
        }
      };
      getTable().addTableListener(
          m_updateTableRowListener,
          TableEvent.TYPE_ROWS_INSERTED,
          TableEvent.TYPE_ROWS_UPDATED);
    }
  }

  @Override
  public void disposeColumn() {
    super.disposeColumn();
    if (getTable() != null && m_updateTableRowListener != null) {
      getTable().removeTableListener(m_updateTableRowListener);
    }
    m_updateTableRowListener = null;
  }

  @Override
  protected boolean getConfiguredDisplayable() {
    return false;
  }

  @Override
  public void setValue(ITableRow r, T rawValue) {
    final int oldRowState = r.getStatus();
    final T oldValue = getValue(r);
    super.setValue(r, rawValue);

    //In case the same bean value (by equals) is set again, the super.setValue method will not do anything
    //Therefore, updateTableColumns would not be triggered and the other columns might be in an
    //inconsistent state.
    //The following ensures that the observer below is always triggered.

    if (r.getCell(this).getObserver() != null) {
      r.getCell(this).getObserver().cellChanged(r.getCell(this), ICell.VALUE_BIT);
    }

    // preserve row-state if the value did not change
    // due to decoration of other columns, a cell-changed-notification was fired anyway
    if (!hasValueChanged(oldValue, rawValue)) {
      r.setStatus(oldRowState);
    }
  }

  protected boolean hasValueChanged(T oldValue, T newValue) {
    return !ObjectUtility.equals(oldValue, newValue);
  }

  /**
   * Updates all other columns based on this column's value.
   */
  protected abstract void updateTableColumns(ITableRow r, T newValue);
}
