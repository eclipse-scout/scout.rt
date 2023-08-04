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

export class TableNavigationPageDownKeyStroke extends AbstractTableNavigationKeyStroke {

  constructor(table: Table) {
    super(table);
    this.which = [keys.PAGE_DOWN];
    this.renderingHints.text = 'PgDn';
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      let viewport = this._viewportInfo();
      if (viewport.lastRow) {
        return viewport.lastRow.$row;
      }
    };
  }

  override handle(event: KeyboardEventBase) {
    let table = this.field,
      viewport = this._viewportInfo(),
      rows = table.visibleRows,
      selectedRows = table.selectedRows,
      lastSelectedRow = arrays.last(selectedRows),
      lastActionRow = table.selectionHandler.lastActionRow,
      lastActionRowIndex = -1,
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

    if (lastActionRow) {
      lastActionRowIndex = rows.indexOf(lastActionRow);
    }
    // last action row index maybe < 0 if row got invisible (e.g. due to filtering), or if the user has not made a selection before
    if (lastActionRowIndex < 0) {
      lastActionRow = lastSelectedRow;
      lastActionRowIndex = rows.indexOf(lastActionRow);
    }

    // If last row in viewport already is selected -> scroll a page down
    // Don't do it if multiple rows are selected and user only presses page down without shift
    if (selectedRows.length > 0 && lastActionRow === viewport.lastRow && !(selectedRows.length > 1 && !event.shiftKey)) {
      table.scrollPageDown();
      viewport = this._viewportInfo();
      if (!viewport.lastRow) {
        // May happen due to same reason as above -> Row will fill the whole viewport after scrolling
        return;
      }
    }

    if (event.shiftKey && selectedRows.length > 0) {
      // Using lastActionRow instead of lastSelectedRow is essential if the user does a multi selection using ctrl and presses shift-pagedown afterwards
      newSelectedRows = rows.slice(lastActionRowIndex + 1, rows.indexOf(viewport.lastRow) + 1);
      newSelectedRows = arrays.union(selectedRows, newSelectedRows);
    } else {
      newSelectedRows = [viewport.lastRow];
    }

    table.selectionHandler.lastActionRow = viewport.lastRow;
    table.selectRows(newSelectedRows, true);
    if (!table.isFocused()) {
      table.focus();
    }

    // Set active descendant to the new row. This should be done last so selection state/focus/etc is
    // all set correctly before the change of active descendant triggers the screen readers announcement.
    aria.linkElementWithActiveDescendant(this.field.$container, viewport.lastRow.$row);
  }
}
