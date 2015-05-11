/**
 * Enhances the table with selection behaviour.<p>
 *
 * If mouseMoveSelectionEnabled is set to true, the user can select the rows by moving the mouse with pressed left mouse button.
 *
 */
scout.TableSelectionHandler = function(table) {
  this.table = table;
  this.mouseMoveSelectionEnabled = true;
  this._mouseDown;
  this._$selectedRows;
  this._$allRows;

  // Index of the row that got a 'mouseover' event previously (needed to determine if the user is going up or down)
  this._prevSelectedRowIndex;
  // The index of the selected row with the greatest distance to fromIndex (needed to efficiently clear the selection)
  this._maxSelectedRowIndex;
};

// TODO BSH Table Selection | Try to merge this with TableKeystrokeAdapter
//FIXME CGU refactor, this handler should not directly render the selection, instead it should call table.selectRows which updates model and calls renderSelection.
scout.TableSelectionHandler.prototype.onMouseDown = function(event, $row) {
  var fromIndex, toIndex,
    select = true;

  this._mouseDown = true;
  this._$allRows = this.table.$filteredRows();
  this._$selectedRows = this.table.$selectedRows();

  // Click without ctrl always starts new selection, with ctrl toggle
  if (this.table.multiSelect && event.shiftKey) {
    fromIndex = this._$allRows.index(this._$selectedRows.first());
  } else if (this.table.multiSelect && event.ctrlKey) {
    select = !$row.isSelected();
  } else {
    // Click on the already selected row must not clear the selection it to avoid another selection event sent to the server
    // Right click on already selected rows must not clear the selection
    if (!$row.isSelected() || (this._$selectedRows.length > 1 && event.which !== 3)) {
      this._$selectedRows.select(false);
      this._clearSelectionBorder(this._$selectedRows);
    }
  }
  if (fromIndex === undefined || fromIndex < 0) {
    fromIndex = this._$allRows.index($row);
  }

  // just a click... right click do not select if clicked in selection
  if (event.which !== 3 || !$row.is(this._$selectedRows)) {
    toIndex = this._$allRows.index($row);
    handleSelection.call(this);
  }

  this.table.notifyRowsSelected(this._$selectedRows, true);

  if (this.mouseMoveSelectionEnabled) {
    // Handle movement with held mouse button. Each event gets only fired once (to prevent event firing when moving
    // inside the same row), but when the mouse leaves the row, the listener is re-attached (see below).
    this._$allRows.not($row).one('mousemove.selectionHandler', onMouseMove.bind(this));
    this._$allRows.on('mouseleave.selectionHandler', function(event) {
      var $row = $(event.delegateTarget);
      $row.one('mousemove.selectionHandler', onMouseMove.bind(this));
    }.bind(this));

    // This additionally window listener is necessary to track the clicks outside of a table row.
    // If the mouse is released on a table row, onMouseUp gets called by the table's mouseUp listener.
    $(window).one('mouseup.selectionHandler', this.onMouseUp.bind(this));
  }

  // --- Helper functions ---

  function onMouseMove(event) {
    var $row = $(event.delegateTarget);
    toIndex = this._$allRows.index($row);
    handleSelection.call(this);
  }

  function handleSelection() {
    var $rowsToUnselect;
    if (this.table.multiSelect) {
      // Multi-selection -> expand/shrink selection
      var thisIndex = toIndex;
      var goingUp = (thisIndex < this._prevSelectedRowIndex);
      var goingDown = (thisIndex > this._prevSelectedRowIndex);
      var beforeFromSelection = (this._prevSelectedRowIndex < fromIndex);
      var afterFromSelection = (this._prevSelectedRowIndex > fromIndex);

      // In 'ctrlKey' mode, the unselection is done via 'select=false'
      if (!event.ctrlKey) {
        // If we are going _towards_ the startIndex, unselect all rows between the current row and the
        // selected row with the greatest distance (this._maxSelectedRowIndex).
        if (goingUp && afterFromSelection) {
          $rowsToUnselect = this._$allRows.slice(thisIndex + 1, this._maxSelectedRowIndex + 1);
        }
        if (goingDown && beforeFromSelection) {
          $rowsToUnselect = this._$allRows.slice(this._maxSelectedRowIndex, thisIndex);
        }
      }
      // Adjust the indexes
      this._maxSelectedRowIndex = (goingUp ? Math.min(this._maxSelectedRowIndex, thisIndex) : (goingDown ? Math.max(this._maxSelectedRowIndex, thisIndex) : thisIndex));
      this._prevSelectedRowIndex = thisIndex;
    } else {
      // Single selection -> unselect previously selected row
      $rowsToUnselect = this._$allRows.eq(fromIndex);

      // Adjust the indexes
      fromIndex = toIndex;
    }

    // Unselect rows outside the selection
    if ($rowsToUnselect) {
      $rowsToUnselect.select(false);
      this._clearSelectionBorder($rowsToUnselect);
    }

    // Set the new selection
    this._selectRange(fromIndex, toIndex, select);
    this.table.notifyRowsSelected(this._$selectedRows, true);
  }
};

scout.TableSelectionHandler.prototype._selectRange = function(fromIndex, toIndex, select) {
  var startIndex = Math.min(fromIndex, toIndex),
    endIndex = Math.max(fromIndex, toIndex) + 1,
    $actionRow = this._$allRows.slice(startIndex, endIndex);

  // set/remove selection
  if (select) {
    $actionRow.select(true);
  } else {
    $actionRow.select(false);
    this._clearSelectionBorder(this._$selectedRows);
  }

  this._$selectedRows = this.table.$selectedRows();
  this._clearSelectionBorder(this._$selectedRows);
  this._renderSelectionBorder(this._$selectedRows);
};

scout.TableSelectionHandler.prototype.onMouseUp = function(event) {
  if (!this._mouseDown) {
    // May happen when selecting elements with chrome dev tools
    return;
  }
  this._mouseDown = false;
  this.table.notifyRowsSelected(this._$selectedRows);

  // TODO BSH Table Selection | This is way too inefficient for many rows!
  this._$allRows.off('.selectionHandler');
  this._$allRows = null;
  this._$selectedRows = null;
};

scout.TableSelectionHandler.prototype.renderSelection = function() {
  this.clearSelection(true);
  var rowIds = this.table.selectedRowIds;
  var selectedRows = [];
  for (var i = 0; i < rowIds.length; i++) {
    var rowId = rowIds[i];
    var $row = this.table.rowsMap[rowId].$row;
    $row.select(true);
    selectedRows.push($row);
  }

  var $selectedRows = $(selectedRows);
  this._renderSelectionBorder($selectedRows);
  return $selectedRows;
};

scout.TableSelectionHandler.prototype.clearSelection = function(dontFire) {
  var $selectedRows = this.table.$selectedRows();
  $selectedRows.select(false);
  this._clearSelectionBorder($selectedRows);

  if (!dontFire) {
    this.table.notifyRowsSelected();
  }
};

/**
 * Just renders selection border because the css class "selected" was already set by Table._buildRowDiv
 */
scout.TableSelectionHandler.prototype.dataDrawn = function() {
  var $selectedRows = this.table.$selectedRows();

  this._clearSelectionBorder($selectedRows);
  this._renderSelectionBorder($selectedRows);
  return $selectedRows;
};

/**
 * Adds the css classes for the selection border based on the selected rows.
 */
scout.TableSelectionHandler.prototype._renderSelectionBorder = function($selectedRows) {
  var that = this;
  $selectedRows.each(function() {
    var $row = $(this);
    var hasPrev = that.table.$prevFilteredRows($row, true).first().isSelected();
    var hasNext = that.table.$nextFilteredRows($row, true).first().isSelected();

    if (hasPrev && hasNext) {
      $row.addClass('select-middle');
    }
    if (!hasPrev && hasNext) {
      $row.addClass('select-top');
    }
    if (hasPrev && !hasNext) {
      $row.addClass('select-bottom');
    }
    if (!hasPrev && !hasNext) {
      $row.addClass('select-single');
    }
  });
};

scout.TableSelectionHandler.prototype._clearSelectionBorder = function($selectedRows) {
  $selectedRows.removeClass('select-middle select-top select-bottom select-single');
};

scout.TableSelectionHandler.prototype.selectAll = function() {
  if (!this.table.multiSelect) {
    return; // not possible
  }

  this.clearSelection(true);

  var $rows = this.table.$rows();
  $rows.select(true);

  this._renderSelectionBorder($rows);
  this.table.notifyRowsSelected($rows);
};

scout.TableSelectionHandler.prototype.toggleSelection = function() {
  if (this.table.selectedRowIds && this.table.selectedRowIds.length === this.table.rows.length) {
    this.clearSelection();
  } else {
    this.selectAll();
  }
};
