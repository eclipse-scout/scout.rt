/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractTableNavigationKeyStroke, arrays, keys, Table, TableRow} from '../../index';

export class TableNavigationHomeKeyStroke extends AbstractTableNavigationKeyStroke {

  constructor(table: Table) {
    super(table);
    this.which = [keys.HOME];
    this.renderingHints.text = 'Home';
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      return this._viewportInfo().firstRow?.$row;
    };
  }

  override handle(event: JQuery.KeyboardEventBase) {
    let table = this.field,
      rows = table.visibleRows,
      firstRow = arrays.first(rows),
      selectedRows = table.selectedRows,
      newSelectedRows: TableRow[] = [],
      focusedRow = table.focusedRow,
      focusedRowIndex = -1;

    if (event.shiftKey && selectedRows.length > 0) {
      if (focusedRow) {
        focusedRowIndex = rows.indexOf(focusedRow);
      }
      // focused row index maybe < 0 if row got invisible (e.g. due to filtering), or if the user has not made a selection before
      if (focusedRowIndex < 0) {
        focusedRow = arrays.first(selectedRows);
        focusedRowIndex = rows.indexOf(focusedRow);
      }
      newSelectedRows = rows.slice(0, focusedRowIndex);
      newSelectedRows = arrays.union(newSelectedRows, selectedRows);
    } else {
      newSelectedRows = [firstRow];
    }

    table.setFocusedRow(firstRow);
    table.selectRows(newSelectedRows);
    table.scrollTo(firstRow);
    if (!table.isFocused()) {
      table.focus();
    }
  }
}
