/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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
  this.field = table;
  this.shift = table.multiSelect ? undefined : false; // multiselect tables have both, shift and not-shift functionality
  this.stopPropagation = true;
  this.keyStrokeMode = scout.keyStrokeMode.DOWN;
};
scout.inherits(scout.AbstractTableNavigationKeyStroke, scout.KeyStroke);

scout.AbstractTableNavigationKeyStroke.prototype._accept = function(event) {
  var accepted = scout.AbstractTableNavigationKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }

  if (!this.field.$filteredRows().length) {
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
 * Returns viewport sensitive information about rows visible to the user.
 *
 * FIXME: [nbu] is not viewport sensitive yet.
 */
scout.AbstractTableNavigationKeyStroke.prototype._viewportInfo = function(table) {
  var viewport = {};

  if (!table.$filteredRows().length) {
    return viewport;
  }

  var $rows = table.$filteredRows(),
    $selectedRows = table.$selectedRows();

  viewport.$firstRow = $rows.first();
  viewport.$lastRow = $rows.last();

  if (!$selectedRows.length) {
    return viewport;
  }

  viewport.selection = true;

  if (!$selectedRows.first().is(viewport.firstRow)) {
    viewport.$rowBeforeSelection = table.$prevFilteredRow($selectedRows.first(), false);
  }
  if (!$selectedRows.last().is(viewport.$lastRow)) {
    viewport.$rowAfterSelection = table.$nextFilteredRow($selectedRows.last(), false);
  }

  return viewport;
};

scout.AbstractTableNavigationKeyStroke.prototype._applyRowSelection = function(table, $oldSelection, $newSelection, shiftKey, deselect, directionDown) {
  if (!$newSelection || !$newSelection.length) {
    return;
  }

  if (shiftKey) {
    $newSelection = (deselect ? $oldSelection.not($newSelection) : $oldSelection.add($newSelection));
  }

  var rows = [];
  $newSelection.each(function() {
    rows.push($(this).data('row'));
  });

  table.selectRows(rows, true, true);

  // scroll selection into view (if not visible)
  table.scrollTo(directionDown ? scout.arrays.last(rows) : scout.arrays.first(rows));
};

/**
 * Searches for the last selected row, starting from rowIndex. Expects row at rowIndex to be selected.
 */
scout.AbstractTableNavigationKeyStroke.prototype._findLastSelectedRowBefore = function(table, rowIndex) {
  var rows = table.filteredRows();
  if (rowIndex === 0) {
    return rows[rowIndex];
  }
  return scout.arrays.findFromPrev(rows, rowIndex, function(row, i) {
    var previousRow = rows[i - 1];
    if (!previousRow) {
      return false;
    }
    return !table.isRowSelected(previousRow);
  });
};


/**
 * Searches for the last selected row, starting from rowIndex. Expects row at rowIndex to be selected.
 */
scout.AbstractTableNavigationKeyStroke.prototype._findLastSelectedRowAfter = function(table, rowIndex) {
  var rows = table.filteredRows();
  if (rowIndex === rows.length - 1) {
    return rows[rowIndex];
  }
  return scout.arrays.findFromPrev(rows, rowIndex, function(row, i) {
    var nextRow = rows[i + 1];
    if (!nextRow) {
      return false;
    }
    return !table.isRowSelected(nextRow);
  });
};
