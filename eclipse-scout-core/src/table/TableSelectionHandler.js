/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import $ from 'jquery';
import {scout} from '../index';

/**
 * Enhances the table with selection behaviour.<p>
 *
 * If mouseMoveSelectionEnabled is set to true, the user can select the rows by moving the mouse with pressed left mouse button.
 *
 */
export default class TableSelectionHandler {

  constructor(table) {
    this.table = table;
    this.mouseMoveSelectionEnabled = true;
    this._mouseDown = false;
    this.lastActionRow = null;
    this.mouseOverHandler = null;
    this.select = true;
    this.counterDebug = 0;

    this.fromIndex = -1;
    this.toIndex = -1;
    // Index of the row that got a 'mouseover' event previously (needed to determine if the user is going up or down)
    this._prevSelectedRowIndex = -1;
    // The index of the selected row with the greatest distance to fromIndex (needed to efficiently clear the selection)
    this._maxSelectedRowIndex = -1;
  }

  clearLastSelectedRowMarker() {
    this.lastActionRow = null;
  }

  // TODO [7.0] bsh: Table Selection | Try to merge this with TableKeystrokeContext
  onMouseDown(event) {
    let $row = $(event.currentTarget);
    let row = $row.data('row');
    let oldSelectedState = $row.isSelected();
    let rows = this.table.visibleRows;

    this._mouseDown = true;
    this.select = true;
    if (this.table.multiSelect && event.shiftKey) {
      // when a selected row in the middle of a selection-block has
      // been clicked while shift is pressed -> do nothing
      if (this.table.selectedRows.indexOf(row) > -1) {
        return;
      }
      if (this.table.selectedRows.length === 0) {
        // Shift-click was pressed without selection -> behave like normal click
        this.lastActionRow = row;
      }
      if (!this.lastActionRow) {
        // The last action row may have been cleared, e.g. when rows have been replaced. In that case, simply assume
        // the first or the last of the currently selected rows as being the last action row to make shift-click
        // behave as expected (depending on which row is nearer from the clicked row).
        let thisRowIndex = rows.indexOf(row);
        let firstSelectedRow = this.table.selectedRows[0];
        let lastSelectedRow = this.table.selectedRows[this.table.selectedRows.length - 1];
        if (thisRowIndex <= (rows.indexOf(firstSelectedRow) + rows.indexOf(lastSelectedRow)) / 2) {
          this.lastActionRow = firstSelectedRow;
        } else {
          this.lastActionRow = lastSelectedRow;
        }
        this._maxSelectedRowIndex = rows.indexOf(lastSelectedRow);
        this._prevSelectedRowIndex = rows.indexOf(this.lastActionRow);
      }
      this.fromIndex = rows.indexOf(this.lastActionRow);
    } else if (event.ctrlKey) {
      this.select = !oldSelectedState;
    } else {
      // Click on the already selected row must not clear the selection it to avoid another selection event sent to the server
      // Right click on already selected rows must not clear the selection
      if (!oldSelectedState || (this.table.selectedRows.length > 1 && event.which !== 3)) {
        this.table._removeSelection();
        this.table.selectedRows = [];
      }
    }
    if (this.fromIndex < 0) {
      this.fromIndex = rows.indexOf(row);
    }

    if (event.which !== 3 || !oldSelectedState) {
      this.toIndex = rows.indexOf(row);
      this.handleSelection(event);
      this.table.notifyRowSelectionFinished();
    }

    if (this.mouseMoveSelectionEnabled && event.which !== 3) {
      this.table.$data.off('mouseover', this.mouseOverHandler);
      this.mouseOverHandler = this.onMouseOver.bind(this);
      this.table.$data.on('mouseover', '.table-row', this.mouseOverHandler);
      // This additionally window listener is necessary to track the clicks outside of a table row.
      // If the mouse is released on a table row, onMouseUp gets called by the table's mouseUp listener.
    }

    $row.window().one('mouseup.selectionHandler', this.onMouseUp.bind(this));
    this.lastActionRow = row;
  }

  onMouseOver(event) {
    let $row = $(event.currentTarget);
    let row = $row.data('row');
    let rows = this.table.visibleRows;

    this.toIndex = rows.indexOf(row);
    this.handleSelection(event);
    this.lastActionRow = row;
  }

  handleSelection(event) {
    let rowsToUnselect;
    let rows = this.table.visibleRows;
    if (this.table.multiSelect) {
      // Multi-selection -> expand/shrink selection
      let thisIndex = this.toIndex;
      let goingUp = (thisIndex < this._prevSelectedRowIndex);
      let goingDown = (thisIndex > this._prevSelectedRowIndex);
      let beforeFromSelection = (this._prevSelectedRowIndex < this.fromIndex);
      let afterFromSelection = (this._prevSelectedRowIndex > this.fromIndex);

      // In 'ctrlKey' mode, the unselection is done via 'select=false'
      // Also prevent unselect in shiftKey mode, because otherwise we'd could
      // possibly have unwanted gaps within the selection block (see #172929).
      if (!event.ctrlKey) {

        // If we are going _towards_ the startIndex, unselect all rows between the current row and the
        // selected row with the greatest distance (this._maxSelectedRowIndex).
        if (goingUp && afterFromSelection) {
          rowsToUnselect = rows.slice(thisIndex + 1, this._maxSelectedRowIndex + 1);
        } else if (goingDown && beforeFromSelection) {
          rowsToUnselect = rows.slice(this._maxSelectedRowIndex, thisIndex);
        }

        // when shift is pressed: only unselect when first or last row (but not in the middle of the selection, see #172929)
        if (rowsToUnselect && event.shiftKey) {
          let selectionIndizes = this.getMinMaxSelectionIndizes();
          rowsToUnselect = rowsToUnselect.reduce((aggr, row) => {
            let rowIndex = rows.indexOf(row);
            if (scout.isOneOf(rowIndex, selectionIndizes[0], selectionIndizes[1])) {
              aggr.push(row);
            }
            return aggr;
          }, []);
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
        this.table._removeSelection();
        this.table.selectedRows = [];
      }

      // Adjust the indexes
      this.fromIndex = this.toIndex;
    }

    // Set the new selection
    this._selectRange(this.fromIndex, this.toIndex, this.select);
  }

  _selectRange(fromIndex, toIndex, select) {
    let rows = this.table.visibleRows;
    let startIndex = Math.min(fromIndex, toIndex);
    let endIndex = Math.max(fromIndex, toIndex) + 1;
    let actionRows = rows.slice(startIndex, endIndex);

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
  }

  getMinMaxSelectionIndizes() {
    let
      selectedRows = this.table.selectedRows,
      allRows = this.table.visibleRows;

    if (!selectedRows || selectedRows.length === 0) {
      return [-1, -1];
    }

    let min = -1,
      max = -1;
    selectedRows.forEach(row => {
      let index = allRows.indexOf(row);
      if (min === -1 || index < min) {
        min = index;
      }
      if (max === -1 || index > max) {
        max = index;
      }
    });
    return [min, max];
  }

  onMouseUp(event) {
    if (!this._mouseDown) {
      // May happen when selecting elements with chrome dev tools
      return;
    }
    if (!this.table.rendered) {
      // May happen when the table is removed between the mouse down and the mouse up event
      // (e.g. when the user clicks 3 times very fast --> table is removed after double click).
      return;
    }

    this._mouseDown = false;
    this.table.$data.off('mouseover', this.mouseOverHandler);
    this.fromIndex = -1;
    this.toIndex = -1;
    this.select = true;
    this.table.notifyRowSelectionFinished();
  }
}
