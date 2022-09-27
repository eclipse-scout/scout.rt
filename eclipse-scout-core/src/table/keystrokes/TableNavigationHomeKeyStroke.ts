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

export default class TableNavigationHomeKeyStroke extends AbstractTableNavigationKeyStroke {

  constructor(table) {
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

  handle(event) {
    let table = this.field,
      rows = table.visibleRows,
      firstRow = arrays.first(rows),
      selectedRows = table.selectedRows,
      newSelectedRows = [],
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
      newSelectedRows = firstRow;
    }
    table.selectionHandler.lastActionRow = firstRow;
    table.selectRows(newSelectedRows);
    table.scrollTo(firstRow);
    if (!table.isFocused()) {
      table.focus();
    }
  }
}
