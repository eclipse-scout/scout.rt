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

  var elementType = document.activeElement.tagName.toLowerCase();
  if (document.activeElement.className !== 'control-filter' &&
    (elementType === 'textarea' || elementType === 'input') &&
    (!event.originalEvent || (event.originalEvent && !event.originalEvent.smartFieldEvent))) {
    return false;
  }

  return this._acceptForNavigation(event);
};

/**
 * Returns whether to accept the given event for navigation.
 */
scout.AbstractTableNavigationKeyStroke.prototype._acceptForNavigation = function(event) {
  return true;
};

/**
 * Returns whether the very last row is selected.
 */
scout.AbstractTableNavigationKeyStroke.prototype._isLastRowSelected = function() {
  var $selectedRows = this.field.$selectedRows();
  if (!$selectedRows.length) {
    return false;
  }

  return $selectedRows.last().is(this.field.$filteredRows().last());
};

/**
 * Returns whether the very first row is selected.
 */
scout.AbstractTableNavigationKeyStroke.prototype._isFirstRowSelected = function() {
  var $selectedRows = this.field.$selectedRows();
  if (!$selectedRows.length) {
    return false;
  }

  return $selectedRows.first().is(this.field.$filteredRows().first());
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

scout.AbstractTableNavigationKeyStroke.prototype._calculateLastActionRowUp = function(lastActionRow, deselect) {
  var $prev = lastActionRow.$row.prevAll('.table-row:not(.invisible):first');
  if ($prev.prev().isSelected() && !deselect) {
    return this._calculateLastActionRowUp($prev.data('row'), deselect);
  }
  return $prev.length > 0 ? $prev.data('row') : lastActionRow;
};

scout.AbstractTableNavigationKeyStroke.prototype._calculateLastActionRowDown = function(lastActionRow, deselect) {
  var $next = lastActionRow.$row.nextAll('.table-row:not(.invisible):first');
  if ($next.next().isSelected() && !deselect) {
    return this._calculateLastActionRowDown($next.data('row'), deselect);
  }
  return $next.length > 0 ? $next.data('row') : lastActionRow;
};
