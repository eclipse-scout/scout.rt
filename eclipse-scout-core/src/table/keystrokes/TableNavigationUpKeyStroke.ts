/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractTableNavigationKeyStroke, aria, arrays, keys, scout, Table, TableRow} from '../../index';

export class TableNavigationUpKeyStroke extends AbstractTableNavigationKeyStroke {

  constructor(table: Table) {
    super(table);
    this.which = [keys.UP];
    this.renderingHints.text = '↑';
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      let row = this.firstRowBeforeSelection();
      if (row) {
        return row.$row;
      }
    };
  }

  override handle(event: JQuery.KeyboardEventBase) {
    let table = this.field,
      rows = table.visibleRows,
      selectedRows = table.selectedRows,
      lastActionRow = table.selectionHandler.lastActionRow,
      lastActionRowIndex = -1,
      newActionRowIndex = -1,
      newSelectedRows: TableRow[],
      newActionRow: TableRow;

    if (lastActionRow) {
      lastActionRowIndex = rows.indexOf(lastActionRow);
    }

    if (rows.length > 1 && (selectedRows.length > 0 || lastActionRowIndex > -1)) {
      // last action row index maybe < 0 if row got invisible (e.g. due to filtering), or if the user has not made a selection before
      if (lastActionRowIndex < 0) {
        if (rows.length === selectedRows.length) {
          lastActionRow = arrays.last(rows);
        } else {
          lastActionRow = arrays.first(selectedRows);
        }
        lastActionRowIndex = rows.indexOf(lastActionRow);
      }
      if (lastActionRowIndex === 0) {
        return;
      }

      newActionRowIndex = lastActionRowIndex - 1;
      newActionRow = rows[newActionRowIndex];
      newSelectedRows = [newActionRow];

      if (event.shiftKey) {
        if (table.isRowSelected(newActionRow)) {
          // if new action row already is selected, remove last action row from selection
          // use case: rows 2,3,4 are selected, last action row is 4. User presses shift-up -> rows 2,3 need to be the new selection
          newSelectedRows = [];
          arrays.pushAll(newSelectedRows, selectedRows);
          // only unselect when first or last row (but not in the middle of the selection, see #172929)
          let selectionIndizes = table.selectionHandler.getMinMaxSelectionIndizes();
          if (scout.isOneOf(lastActionRowIndex, selectionIndizes[0], selectionIndizes[1])) {
            arrays.remove(newSelectedRows, lastActionRow);
          }
        } else {
          newSelectedRows = arrays.union(newSelectedRows, selectedRows);
          newActionRow = this._findLastSelectedRowBefore(table, newActionRowIndex);
        }
      }
    } else {
      newSelectedRows = [arrays.last(rows)];
      newActionRow = newSelectedRows[0];
    }

    table.selectionHandler.lastActionRow = newActionRow;
    table.selectRows(newSelectedRows, true);
    table.scrollTo(newActionRow);
    if (!table.isFocused()) {
      table.focus();
    }

    // Set active descendant to the new row. This should be done last so selection state/focus/etc. is
    // all set correctly before the change of active descendant triggers the screen readers announcement.
    aria.linkElementWithActiveDescendant(this.field.$container, newActionRow.$row);
  }
}
