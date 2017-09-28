/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.AbstractTableNavigationKeyStroke = function(table) {
  scout.AbstractTableNavigationKeyStroke.parent.call(this);
  this.repeatable = true;
  this.field = table;
  this.shift = table.multiSelect ? undefined : false; // multiselect tables have both, shift and not-shift functionality
  this.stopPropagation = true;
  this.keyStrokeMode = scout.KeyStrokeMode.DOWN;
};
scout.inherits(scout.AbstractTableNavigationKeyStroke, scout.KeyStroke);

scout.AbstractTableNavigationKeyStroke.prototype._accept = function(event) {
  var accepted = scout.AbstractTableNavigationKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }

  if (!this.field.filteredRows().length) {
    return false;
  }

  var activeElement = this.field.$container.activeElement(true),
    elementType = activeElement.tagName.toLowerCase();
  if (activeElement.className !== 'table-text-filter' &&
    (elementType === 'textarea' || elementType === 'input') &&
    (!event.originalEvent || (event.originalEvent && !event.originalEvent.smartFieldEvent))) {
    return false;
  }

  return true;
};

/**
 * Returns viewport sensitive information containing the first and last visible row in the viewport.
 */
scout.AbstractTableNavigationKeyStroke.prototype._viewportInfo = function() {
  var x, y, viewportBounds, dataInsets, dataMarginTop, firstRow, lastRow,
    table = this.field,
    viewport = {},
    rows = table.filteredRows();

  if (rows.length === 0) {
    return viewport;
  }

  viewportBounds = scout.graphics.offsetBounds(table.$data);
  dataInsets = scout.graphics.insets(table.$data);
  dataMarginTop = table.$data.cssMarginTop();
  viewportBounds = viewportBounds.subtract(dataInsets);

  // if data has a negative margin, adjust viewport otherwise a selected first row will never be in the viewport
  if (dataMarginTop < 0) {
    viewportBounds.y -= Math.abs(dataMarginTop);
    viewportBounds.height += Math.abs(dataMarginTop);
  }

  // get first element at the top of the viewport
  x = viewportBounds.x + 1;
  y = viewportBounds.y + 1;

  firstRow = this._findFirstRowInViewport(table, viewportBounds);
  lastRow = this._findLastRowInViewport(table, rows.indexOf(firstRow), viewportBounds);

  viewport.firstRow = firstRow;
  viewport.lastRow = lastRow;
  return viewport;
};

scout.AbstractTableNavigationKeyStroke.prototype.firstRowAfterSelection = function() {
  var $selectedRows = this.field.$selectedRows();
  if (!$selectedRows.length) {
    return;
  }

  var rows = this.field.filteredRows(),
    row = $selectedRows.last().data('row'),
    rowIndex = this.field.filteredRows().indexOf(row);

  return rows[rowIndex + 1];
};

scout.AbstractTableNavigationKeyStroke.prototype.firstRowBeforeSelection = function() {
  var $selectedRows = this.field.$selectedRows();
  if (!$selectedRows.length) {
    return;
  }
  var rows = this.field.filteredRows(),
    row = $selectedRows.first().data('row'),
    rowIndex = this.field.filteredRows().indexOf(row);

  return rows[rowIndex - 1];
};

/**
 * Searches for the last selected row in the current selection block, starting from rowIndex. Expects row at rowIndex to be selected.
 */
scout.AbstractTableNavigationKeyStroke.prototype._findLastSelectedRowBefore = function(table, rowIndex) {
  var row, rows = table.filteredRows();
  if (rowIndex === 0) {
    return rows[rowIndex];
  }
  row = scout.arrays.findFromReverse(rows, rowIndex, function(row, i) {
    var previousRow = rows[i - 1];
    if (!previousRow) {
      return false;
    }
    return !table.isRowSelected(previousRow);
  });
  // when no row has been found, use first row in table
  if (!row) {
    row = rows[0];
  }
  return row;
};

/**
 * Searches for the last selected row in the current selection block, starting from rowIndex. Expects row at rowIndex to be selected.
 */
scout.AbstractTableNavigationKeyStroke.prototype._findLastSelectedRowAfter = function(table, rowIndex) {
  var row, rows = table.filteredRows();
  if (rowIndex === rows.length - 1) {
    return rows[rowIndex];
  }
  row = scout.arrays.findFromReverse(rows, rowIndex, function(row, i) {
    var nextRow = rows[i + 1];
    if (!nextRow) {
      return false;
    }
    return !table.isRowSelected(nextRow);
  });
  // when no row has been found, use last row in table
  if (!row) {
    row = rows[rows.length - 1];
  }
  return row;
};

scout.AbstractTableNavigationKeyStroke.prototype._findFirstRowInViewport = function(table, viewportBounds) {
  var rows = table.filteredRows();
  return scout.arrays.find(rows, function(row, i) {
    var rowOffset, rowMarginTop,
      $row = row.$row;

    if (!row.$row) {
      // If row is not rendered, it cannot be part of the view port -> check next row
      return false;
    }
    rowOffset = $row.offset();
    rowMarginTop = row.$row.cssMarginTop();
    // Selected row has a negative row margin
    // -> add this margin to the offset to make sure this function does always return the same row independent of selection state
    if (rowMarginTop < 0) {
      rowOffset.top += Math.abs(rowMarginTop);
    }

    // If the row is fully visible in the viewport -> break and return the row
    return viewportBounds.contains(rowOffset.left, rowOffset.top);
  });
};

scout.AbstractTableNavigationKeyStroke.prototype._findLastRowInViewport = function(table, startRowIndex, viewportBounds) {
  var rows = table.filteredRows();
  if (startRowIndex === rows.length - 1) {
    return rows[startRowIndex];
  }
  return scout.arrays.findFromForward(rows, startRowIndex, function(row, i) {
    var nextRowOffsetBounds, $nextRow,
      nextRow = rows[i + 1];

    if (!nextRow) {
      // If next row is not available (row is the last row) -> break and return current row
      return true;
    }
    $nextRow = nextRow.$row;
    if (!$nextRow) {
      // If next row is not rendered anymore, current row has to be the last in the viewport
      return true;
    }
    nextRowOffsetBounds = scout.graphics.offsetBounds($nextRow);
    // If the next row is not fully visible in the viewport -> break and return current row
    return !viewportBounds.contains(nextRowOffsetBounds.x, nextRowOffsetBounds.y + nextRowOffsetBounds.height - 1);
  });
};
