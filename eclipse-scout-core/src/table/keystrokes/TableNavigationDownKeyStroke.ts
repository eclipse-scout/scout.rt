/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractTableNavigationKeyStroke, arrays, keys, scout, Table, TableRow} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export class TableNavigationDownKeyStroke extends AbstractTableNavigationKeyStroke {

  constructor(table: Table) {
    super(table);
    this.which = [keys.DOWN];
    this.renderingHints.text = '↓';
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      let row = this.firstRowAfterSelection();
      if (row) {
        return row.$row;
      }
    };
  }

  override handle(event: KeyboardEventBase) {
    let table = this.field,
      rows = table.visibleRows,
      selectedRows = table.selectedRows,
      lastActionRow = table.selectionHandler.lastActionRow,
      lastActionRowIndex = -1,
      newActionRowIndex = -1,
      newActionRow: TableRow,
      newSelectedRows: TableRow[];

    if (lastActionRow) {
      lastActionRowIndex = rows.indexOf(lastActionRow);
    }

    if (rows.length > 1 && (selectedRows.length > 0 || lastActionRowIndex > -1)) {
      // last action row index maybe < 0 if row got invisible (e.g. due to filtering), or if the user has not made a selection before
      if (lastActionRowIndex < 0) {
        if (rows.length === selectedRows.length) {
          lastActionRow = arrays.first(rows);
        } else {
          lastActionRow = arrays.last(selectedRows);
        }
        lastActionRowIndex = rows.indexOf(lastActionRow);
      }
      if (lastActionRowIndex === rows.length - 1) {
        return;
      }

      newActionRowIndex = lastActionRowIndex + 1;
      newActionRow = rows[newActionRowIndex];
      newSelectedRows = [newActionRow];

      if (event.shiftKey) {
        if (table.isRowSelected(newActionRow)) {
          // if new action row already is selected, remove last action row from selection
          // use case: rows 2,3,4 are selected, last action row is 2. User presses shift-down -> rows 3,4 need to be the new selection
          newSelectedRows = [];
          arrays.pushAll(newSelectedRows, selectedRows);
          // only unselect when first or last row (but not in the middle of the selection, see #172929)
          let selectionIndizes = table.selectionHandler.getMinMaxSelectionIndizes();
          if (scout.isOneOf(lastActionRowIndex, selectionIndizes[0], selectionIndizes[1])) {
            arrays.remove(newSelectedRows, lastActionRow);
          }
        } else {
          newSelectedRows = arrays.union(selectedRows, newSelectedRows);
          newActionRow = this._findLastSelectedRowAfter(table, newActionRowIndex);
        }
      }
    } else {
      newSelectedRows = [arrays.first(rows)];
      newActionRow = newSelectedRows[0];
    }

    table.selectionHandler.lastActionRow = newActionRow;
    table.selectRows(newSelectedRows, true);
    table.scrollTo(newActionRow);
    if (!table.isFocused()) {
      table.focus();
    }
  }
}
