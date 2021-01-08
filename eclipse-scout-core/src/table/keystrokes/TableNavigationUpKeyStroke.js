/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractTableNavigationKeyStroke, arrays, keys, scout} from '../../index';

export default class TableNavigationUpKeyStroke extends AbstractTableNavigationKeyStroke {

  constructor(table) {
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

  handle(event) {
    let table = this.field,
      rows = table.visibleRows,
      selectedRows = table.selectedRows,
      lastActionRow = table.selectionHandler.lastActionRow,
      lastActionRowIndex = -1,
      newActionRowIndex = -1,
      newSelectedRows, newActionRow;

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
  }
}
