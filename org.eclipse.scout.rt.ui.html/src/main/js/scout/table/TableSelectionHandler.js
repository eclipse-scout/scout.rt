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
  this.lastActionRow;
  this._allRows;
  this.mouseOverHandler;
  this.select = true;
  this.counterDebug = 0;

  this.fromIndex;
  this.toIndex;
  // Index of the row that got a 'mouseover' event previously (needed to determine if the user is going up or down)
  this._prevSelectedRowIndex;
  // The index of the selected row with the greatest distance to fromIndex (needed to efficiently clear the selection)
  this._maxSelectedRowIndex;
};

// TODO BSH Table Selection | Try to merge this with TableKeystrokeAdapter
scout.TableSelectionHandler.prototype.onMouseDown = function(event) {

  var $row = $(event.currentTarget),
    row = $row.data('row'),
    oldSelectedState = $row.isSelected();
  this._mouseDown = true;

  //TODO nbu check option to save filtered rows in model
  this._allRows = this.table.filteredRows();
  if (this.table.multiSelect && event.shiftKey) {
    this.fromIndex = this._allRows.indexOf(this.lastActionRow);
  } else if (event.ctrlKey) {
    this.select = !oldSelectedState;
  } else {
    this.select = true;
    // Click on the already selected row must not clear the selection it to avoid another selection event sent to the server
    // Right click on already selected rows must not clear the selection
    if (!oldSelectedState || (this.table.selectedRows.length > 1 && event.which !== 3)) {
      this.table.clearSelection(true);
    }
  }
  if (this.fromIndex === undefined || this.fromIndex < 0) {
    this.fromIndex = this._allRows.indexOf(row);
  }

  if (event.which !== 3 || !oldSelectedState) {
    this.toIndex = this._allRows.indexOf(row);
    this.handleSelection(event);
  }

  if (this.mouseMoveSelectionEnabled) {
    this.table.$data.off('mouseover', this.mouseOverHandler);
    this.mouseOverHandler = this.onMouseOver.bind(this);
    this.table.$data.on('mouseover', '.table-row', this.mouseOverHandler);
    // This additionally window listener is necessary to track the clicks outside of a table row.
    // If the mouse is released on a table row, onMouseUp gets called by the table's mouseUp listener.
    $(window).one('mouseup.selectionHandler', this.onMouseUp.bind(this));
  }

  this.lastActionRow = row;
};

scout.TableSelectionHandler.prototype.onMouseOver = function(event) {
  var $row = $(event.currentTarget),
    row = $row.data('row');
  this.toIndex = this._allRows.indexOf(row);
  this.handleSelection(event);
  this.lastActionRow = row;

};

scout.TableSelectionHandler.prototype.handleSelection = function(event) {
  var rowsToUnselect;
  if (this.table.multiSelect) {
    // Multi-selection -> expand/shrink selection
    var thisIndex = this.toIndex;
    var goingUp = (thisIndex < this._prevSelectedRowIndex);
    var goingDown = (thisIndex > this._prevSelectedRowIndex);
    var beforeFromSelection = (this._prevSelectedRowIndex < this.fromIndex);
    var afterFromSelection = (this._prevSelectedRowIndex > this.fromIndex);

    // In 'ctrlKey' mode, the unselection is done via 'select=false'
    if (!event.ctrlKey) {
      // If we are going _towards_ the startIndex, unselect all rows between the current row and the
      // selected row with the greatest distance (this._maxSelectedRowIndex).
      if (goingUp && afterFromSelection) {
        rowsToUnselect = this._allRows.slice(thisIndex + 1, this._maxSelectedRowIndex + 1);
      } else if (goingDown && beforeFromSelection) {
        rowsToUnselect = this._allRows.slice(this._maxSelectedRowIndex, thisIndex);
      }
      if (rowsToUnselect) {
        rowsToUnselect.forEach(function(row) {
          this.table.removeRowFromSelection(row, true);
        }, this);
      }
    }
    // Adjust the indexes
    this._maxSelectedRowIndex = (goingUp ? Math.min(this._maxSelectedRowIndex, thisIndex) : (goingDown ? Math.max(this._maxSelectedRowIndex, thisIndex) : thisIndex));
    this._prevSelectedRowIndex = thisIndex;
  } else {
    // Single selection -> unselect previously selected row
    if (this.select) {
      this.table.clearSelection(true);
    }

    // Adjust the indexes
    this.fromIndex = this.toIndex;
  }

  // Set the new selection
  $.log.error("from:" + this.fromIndex + " to:" + this.toIndex);
  this._selectRange(this.fromIndex, this.toIndex, this.select);
};

scout.TableSelectionHandler.prototype._selectRange = function(fromIndex, toIndex, select) {
  var startIndex = Math.min(fromIndex, toIndex),
    endIndex = Math.max(fromIndex, toIndex) + 1,
    actionRows = this._allRows.slice(startIndex, endIndex);
  // set/remove selection
  if (select) {
    actionRows.forEach(function(row) {
      this.table.addRowToSelection(row, true);
    }, this);
  } else {
    actionRows.forEach(function(row) {
      this.table.removeRowFromSelection(row, true);
    }, this);
  }
};

scout.TableSelectionHandler.prototype.onMouseUp = function(event) {
  if (!this._mouseDown) {
    // May happen when selecting elements with chrome dev tools
    return;
  }

  this._mouseDown = false;
  this.table.$data.off('mouseover', this.mouseOverHandler);
  this._allRows = null;
  this.fromIndex = -1;
  this.toIndex = -1;
  this.selected = true;
  // Update selectedRows and allRows, this might have changed in the meantime (e.g. when row
  // was replaced by update event due to cell editing)
  this.table.notifyRowSelectionFinished();
};
