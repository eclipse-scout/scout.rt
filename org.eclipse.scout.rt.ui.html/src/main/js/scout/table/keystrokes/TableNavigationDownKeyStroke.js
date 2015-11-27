/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TableNavigationDownKeyStroke = function(table) {
  scout.TableNavigationDownKeyStroke.parent.call(this, table);
  this.which = [scout.keys.DOWN];
  this.renderingHints.text = '↓';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.$firstRowAfterSelection();
  }.bind(this);
};
scout.inherits(scout.TableNavigationDownKeyStroke, scout.AbstractTableNavigationKeyStroke);

scout.TableNavigationDownKeyStroke.prototype.handle = function(event) {
  var table = this.field,
    rows = table.filteredRows(),
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
      if (rows.length === selectedRows.length){
        lastActionRow = scout.arrays.first(rows);
      } else {
        lastActionRow = scout.arrays.last(selectedRows);
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
        scout.arrays.pushAll(newSelectedRows, selectedRows);
        scout.arrays.remove(newSelectedRows, lastActionRow);
      } else {
        newSelectedRows = scout.arrays.union(selectedRows, newSelectedRows);
        newActionRow = this._findLastSelectedRowAfter(table, newActionRowIndex);
      }
    }
  } else {
    newSelectedRows = [scout.arrays.first(rows)];
    newActionRow = newSelectedRows[0];
  }

  table.selectionHandler.lastActionRow = newActionRow;
  table.selectRows(newSelectedRows, true, true);
  table.scrollTo(newActionRow);
};
