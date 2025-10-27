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

export class TableNavigationPageDownKeyStroke extends AbstractTableNavigationKeyStroke {

  constructor(table: Table) {
    super(table);
    this.which = [keys.PAGE_DOWN];
    this.renderingHints.text = 'PgDn';
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      return this._viewportInfo().lastRow?.$row;
    };
  }

  override handle(event: JQuery.KeyboardEventBase) {
    let table = this.field,
      viewport = this._viewportInfo(),
      rows = table.visibleRows,
      selectedRows = table.selectedRows,
      lastSelectedRow = arrays.last(selectedRows),
      focusedRow = table.focusedRow,
      focusedRowIndex = -1,
      newSelectedRows: TableRow[];

    // Last row may be undefined if there is only one row visible in the viewport and this row is bigger than the viewport. In that case just scroll down.
    // If it already is at the bottom nothing will happen
    if (!viewport.lastRow) {
      table.scrollPageDown();
      viewport = this._viewportInfo();
      if (!viewport.lastRow) {
        return;
      }
    }

    if (focusedRow) {
      focusedRowIndex = rows.indexOf(focusedRow);
    }
    // focused row index maybe < 0 if row got invisible (e.g. due to filtering), or if the user has not made a selection before
    if (focusedRowIndex < 0) {
      focusedRow = lastSelectedRow;
      focusedRowIndex = rows.indexOf(focusedRow);
    }

    // If last row in viewport already is selected -> scroll a page down
    // Don't do it if multiple rows are selected and user only presses page down without shift
    if (selectedRows.length > 0 && focusedRow === viewport.lastRow && !(selectedRows.length > 1 && !event.shiftKey)) {
      table.scrollPageDown();
      viewport = this._viewportInfo();
      if (!viewport.lastRow) {
        // May happen due to same reason as above -> Row will fill the whole viewport after scrolling
        return;
      }
    }

    if (event.shiftKey && selectedRows.length > 0) {
      // Using focusedRow instead of lastSelectedRow is essential if the user does a multi selection using ctrl and presses shift-pagedown afterward
      newSelectedRows = rows.slice(focusedRowIndex + 1, rows.indexOf(viewport.lastRow) + 1);
      newSelectedRows = arrays.union(selectedRows, newSelectedRows);
    } else {
      newSelectedRows = [viewport.lastRow];
    }

    table.setFocusedRow(viewport.lastRow);
    table.selectRows(newSelectedRows, true);
    if (!table.isFocused()) {
      table.focus();
    }
  }
}
