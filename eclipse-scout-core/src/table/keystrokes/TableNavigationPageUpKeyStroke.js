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
import {AbstractTableNavigationKeyStroke, arrays, keys} from '../../index';

export default class TableNavigationPageUpKeyStroke extends AbstractTableNavigationKeyStroke {

  constructor(table) {
    super(table);
    this.which = [keys.PAGE_UP];
    this.renderingHints.text = 'PgUp';
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      let viewport = this._viewportInfo();
      if (viewport.firstRow) {
        return viewport.firstRow.$row;
      }
    };
  }

  handle(event) {
    let table = this.field,
      viewport = this._viewportInfo(),
      rows = table.visibleRows,
      selectedRows = table.selectedRows,
      firstSelectedRow = arrays.first(selectedRows),
      lastActionRow = table.selectionHandler.lastActionRow,
      lastActionRowIndex = -1,
      newSelectedRows;

    // First row may be undefined if there is only one row visible in the viewport and this row is bigger than the viewport. In that case just scroll up.
    // If it already is at the top nothing will happen
    if (!viewport.firstRow) {
      table.scrollPageUp();
      viewport = this._viewportInfo();
      if (!viewport.firstRow) {
        return;
      }
    }

    if (lastActionRow) {
      lastActionRowIndex = rows.indexOf(lastActionRow);
    }
    // last action row index maybe < 0 if row got invisible (e.g. due to filtering), or if the user has not made a selection before
    if (lastActionRowIndex < 0) {
      lastActionRow = firstSelectedRow;
      lastActionRowIndex = rows.indexOf(lastActionRow);
    }

    // If first row in viewport already is selected -> scroll a page up
    // Don't do it if multiple rows are selected and user only presses page up without shift
    if (selectedRows.length > 0 && lastActionRow === viewport.firstRow && !(selectedRows.length > 1 && !event.shiftKey)) {
      table.scrollPageUp();
      viewport = this._viewportInfo();
      if (!viewport.firstRow) {
        // May happen due to same reason as above -> Row will fill the whole viewport after scrolling
        return;
      }
    }

    if (event.shiftKey && selectedRows.length > 0) {
      newSelectedRows = rows.slice(rows.indexOf(viewport.firstRow), lastActionRowIndex);
      newSelectedRows = arrays.union(selectedRows, newSelectedRows);
    } else {
      newSelectedRows = [viewport.firstRow];
    }

    table.selectionHandler.lastActionRow = viewport.firstRow;
    table.selectRows(newSelectedRows, true);
    if (!table.isFocused()) {
      table.focus();
    }
  }
}
