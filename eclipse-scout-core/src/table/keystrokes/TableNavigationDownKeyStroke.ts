/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractTableNavigationKeyStroke, arrays, keys, scout, Table, TableRow} from '../../index';

export class TableNavigationDownKeyStroke extends AbstractTableNavigationKeyStroke {

  constructor(table: Table) {
    super(table);
    this.which = [keys.DOWN];
    this.renderingHints.text = '↓';
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      return this.firstRowAfterSelection()?.$row;
    };
  }

  override handle(event: JQuery.KeyboardEventBase) {
    let table = this.field,
      rows = table.visibleRows,
      selectedRows = table.selectedRows,
      focusedRow = this._getFocusedRow(),
      focusedRowIndex = -1,
      newFocusedRowIndex = -1,
      newFocusedRow: TableRow,
      newSelectedRows: TableRow[];

    if (focusedRow) {
      focusedRowIndex = rows.indexOf(focusedRow);
    }

    if (rows.length > 1 && (selectedRows.length > 0 || focusedRowIndex > -1)) {
      // focused row index maybe < 0 if row got invisible (e.g. due to filtering), or if the user has not made a selection before
      if (focusedRowIndex < 0) {
        if (rows.length === selectedRows.length) {
          focusedRow = arrays.first(rows);
        } else {
          focusedRow = arrays.last(selectedRows);
        }
        focusedRowIndex = rows.indexOf(focusedRow);
      }
      if (focusedRowIndex === rows.length - 1) {
        return;
      }

      newFocusedRowIndex = focusedRowIndex + 1;
      newFocusedRow = rows[newFocusedRowIndex];
      newSelectedRows = [newFocusedRow];

      if (event.shiftKey) {
        if (table.isRowSelected(newFocusedRow)) {
          // if new action row already is selected, remove focused row from selection
          // use case: rows 2,3,4 are selected, focused row is 2. User presses shift-down -> rows 3,4 need to be the new selection
          newSelectedRows = [];
          arrays.pushAll(newSelectedRows, selectedRows);
          // only unselect when first or last row (but not in the middle of the selection, see #172929)
          let selectionIndizes = table.selectionHandler.getMinMaxSelectionIndizes();
          if (scout.isOneOf(focusedRowIndex, selectionIndizes[0], selectionIndizes[1])) {
            arrays.remove(newSelectedRows, focusedRow);
          }
        } else {
          newSelectedRows = arrays.union(selectedRows, newSelectedRows);
          newFocusedRow = this._findLastSelectedRowAfter(table, newFocusedRowIndex);
        }
      }
    } else {
      newSelectedRows = [arrays.first(rows)];
      newFocusedRow = newSelectedRows[0];
    }

    table.setFocusedRow(newFocusedRow);
    table.selectRows(newSelectedRows, true);
    table.scrollTo(newFocusedRow);
    if (!table.isFocused()) {
      table.focus();
    }
  }
}
