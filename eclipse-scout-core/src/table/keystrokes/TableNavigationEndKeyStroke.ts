/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractTableNavigationKeyStroke, aria, arrays, keys, Table, TableRow} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export class TableNavigationEndKeyStroke extends AbstractTableNavigationKeyStroke {

  constructor(table: Table) {
    super(table);
    this.which = [keys.END];
    this.renderingHints.text = 'End';
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      let viewport = this._viewportInfo();
      if (viewport.lastRow) {
        return viewport.lastRow.$row;
      }
    };
  }

  override handle(event: KeyboardEventBase) {
    let table = this.field,
      rows = table.visibleRows,
      lastRow = arrays.last(rows),
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
        lastActionRow = arrays.last(selectedRows);
        lastActionRowIndex = rows.indexOf(lastActionRow);
      }
      newSelectedRows = rows.slice(lastActionRowIndex + 1, rows.length);
      newSelectedRows = arrays.union(selectedRows, newSelectedRows);
    } else {
      newSelectedRows = [lastRow];
    }
    table.selectionHandler.lastActionRow = lastRow;
    table.selectRows(newSelectedRows);
    table.scrollTo(lastRow);
    if (!table.isFocused()) {
      table.focus();
    }

    // Set active descendant to the new row. This should be done last so selection state/focus/etc is
    // all set correctly before the change of active descendant triggers the screen readers announcement.
    aria.linkElementWithActiveDescendant(this.field.$container, lastRow.$row);
  }
}
