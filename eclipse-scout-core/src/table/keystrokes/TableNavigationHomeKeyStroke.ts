/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractTableNavigationKeyStroke, arrays, keys, Table, TableRow} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export class TableNavigationHomeKeyStroke extends AbstractTableNavigationKeyStroke {

  constructor(table: Table) {
    super(table);
    this.which = [keys.HOME];
    this.renderingHints.text = 'Home';
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      let viewport = this._viewportInfo();
      if (viewport.firstRow) {
        return viewport.firstRow.$row;
      }
    };
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    let table = this.field,
      rows = table.visibleRows,
      firstRow = arrays.first(rows),
      selectedRows = table.selectedRows,
      newSelectedRows: TableRow[] = [],
      lastActionRow = table.selectionHandler.lastActionRow,
      lastActionRowIndex = -1;

    if (event.shiftKey && selectedRows.length > 0) {
      if (lastActionRow) {
        lastActionRowIndex = rows.indexOf(lastActionRow);
      }
      // last action row index maybe < 0 if row got invisible (e.g. due to filtering), or if the user has not made a selection before
      if (lastActionRowIndex < 0) {
        lastActionRow = arrays.first(selectedRows);
        lastActionRowIndex = rows.indexOf(lastActionRow);
      }
      newSelectedRows = rows.slice(0, lastActionRowIndex);
      newSelectedRows = arrays.union(newSelectedRows, selectedRows);
    } else {
      newSelectedRows = [firstRow];
    }
    table.selectionHandler.lastActionRow = firstRow;
    table.selectRows(newSelectedRows);
    table.scrollTo(firstRow);
    if (!table.isFocused()) {
      table.focus();
    }
  }
}
