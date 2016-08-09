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
scout.TableAdapter = function() {
  scout.TableAdapter.parent.call(this);
  this._addAdapterProperties(['tableControls', 'menus', 'keyStrokes']);
};
scout.inherits(scout.TableAdapter, scout.ModelAdapter);

scout.TableAdapter.prototype._syncTableStatusVisible = function(visible) {
  this.widget.setTableStatusVisible(visible);
  return false;
};

scout.TableAdapter.prototype._sendRowsSelected = function(rowIds, debounceSend) {
  var eventData = {
    rowIds: rowIds
  };

  // send delayed to avoid a lot of requests while selecting
  // coalesce: only send the latest selection changed event for a field
  this._send('rowsSelected', eventData, debounceSend ? 250 : 0, function(previous) {
    return this.id === previous.id && this.type === previous.type;
  });
};

scout.TableAdapter.prototype._sendRowClicked = function(rowId, mouseButton, columnId) {
  var data = {
    rowId: rowId,
    columnId: columnId,
    mouseButton: mouseButton
  };
  this._send('rowClicked', data);
};

scout.TableAdapter.prototype._onWidgetRowsSelected = function(event) {
  var rowIds = this.widget._rowsToIds(this.widget.selectedRows);
  this._sendRowsSelected(rowIds, event.debounce);
};

scout.TableAdapter.prototype._onWidgetRowClicked = function(event) {
  var columnId;
  if (event.column !== undefined) {
    columnId = event.column.id;
  }

  this._sendRowClicked(event.row.id, event.mouseButton, columnId);
};

scout.TableAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'rowsSelected') {
    this._onWidgetRowsSelected(event);
  } else if (event.type === 'rowClicked') {
    this._onWidgetRowClicked(event);
  } else {
    scout.TableAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};

/**
 * @override
 */
scout.TableAdapter.prototype._onChildAdapterCreation = function(propertyName, model) {
  if (propertyName === 'tableControls') {
    model.table = this; // FIXME [6.1] CGU fix this, this is not executed anymore
  }
};

scout.TableAdapter.prototype._onRowsInserted = function(rows) {
  this.widget.insertRows(rows, true);
};

scout.TableAdapter.prototype._onRowsDeleted = function(rowIds) {
  var rows = this.widget._rowsByIds(rowIds);
  this.widget.deleteRows(rows);
};

scout.TableAdapter.prototype._onAllRowsDeleted = function() {
  this.widget.deleteAllRows();
};

scout.TableAdapter.prototype._onRowsUpdated = function(rows) {
  this.widget.updateRows(rows);
};

scout.TableAdapter.prototype._onRowsSelected = function(rowIds) {
  var rows = this.widget._rowsByIds(rowIds);
  this.widget.selectRows(rows);
  // FIXME [6.1] CGU what is this for? seems wrong here
  this.widget.selectionHandler.clearLastSelectedRowMarker();
};

scout.TableAdapter.prototype._onRowsChecked = function(rows) {
  var checkedRows = [],
    uncheckedRows = [];

  rows.forEach(function(rowData) {
    var row = this.widget._rowById(rowData.id);
    if (rowData.checked) {
      checkedRows.push(row);
    } else {
      uncheckedRows.push(row);
    }
  }, this);

  this.widget.checkRows(checkedRows, {
    checked: true,
    notifyServer: false,
    checkOnlyEnabled: false
  });
  this.widget.uncheckRows(uncheckedRows, {
    notifyServer: false,
    checkOnlyEnabled: false
  });
};

scout.TableAdapter.prototype._onRowOrderChanged = function(rowIds) {
  var rows = this.widget._rowsByIds(rowIds);
  this.widget.updateRowOrder(rows);
};

scout.TableAdapter.prototype._onColumnStructureChanged = function(columns) {
  this.widget.updateColumnStructure(columns);
};

scout.TableAdapter.prototype._onColumnOrderChanged = function(columnIds) {
  var columns = this.widget._columnsByIds(columnIds);
  this.widget.updateColumnOrder(columns);
};

scout.TableAdapter.prototype._onColumnHeadersUpdated = function(columns) {
  this.widget.updateColumnHeaders(columns);
};

scout.TableAdapter.prototype._onStartCellEdit = function(columnId, rowId, fieldId) {
  var column = this.widget._columnById(columnId),
    row = this.widget._rowById(rowId),
    field = this.session.getOrCreateModelAdapter(fieldId, this);

  field.createWidget(this.widget);
  this.widget.startCellEdit(column, row, field.widget);
};

scout.TableAdapter.prototype._onEndCellEdit = function(fieldId) {
  var field = this.session.getModelAdapter(fieldId);
  this.widget.endCellEdit(field.widget);
  // FIXME [6.1] CGU field must be destroyed, listener or do it here?
};

scout.TableAdapter.prototype._onRequestFocus = function() {
  this.widget.requestFocus();
};

scout.TableAdapter.prototype._onScrollToSelection = function() {
  this.widget.revealSelection();
};

scout.TableAdapter.prototype._onColumnBackgroundEffectChanged = function(event) {
  var columnId, column;
  event.eventParts.forEach(function(eventPart) {
    columnId = eventPart.columnId;
    column = this.widget._columnById(columnId);
    column.setBackgroundEffect(eventPart.backgroundEffect, false);
  }, this);
};

scout.TableAdapter.prototype._onRequestFocusInCell = function(event) {
  var row = this.widget._rowById(event.rowId),
    column = this.widget._columnById(event.columnId);

  this.widget.requestFocusInCell(column, row);
};

scout.TableAdapter.prototype._onAggregationFunctionChanged = function(event) {
  var columns = [],
    functions = [];

  event.eventParts.forEach(function(eventPart) {
    var func = eventPart.aggregationFunction,
      column = this.widget._columnById(eventPart.columnId);
    columns.push(column);
    functions.push(func);
  }, this);

  this.widget.changeAggregations(columns, functions);
};

scout.TableAdapter.prototype._onFiltersChanged = function(filters) {
  this.widget.setFilters(filters);
  // do not refilter while the table is being rebuilt (because column.index in filter and row.cells may be inconsistent)
  if (!this.widget._rebuildingTable) {//FIXME CGU [6.1] gehört das nicht direkt in filter rein?
    this.filter();
  }
};

scout.TableAdapter.prototype._onColumnActionsChanged = function(event) {
  // FIXME [6.1] cgu still needed? Never called
  this.widget.header.onColumnActionsChanged(event);
};

scout.TableAdapter.prototype.onModelAction = function(event) {
  // _renderRows() might not have drawn all rows yet, therefore postpone the
  // execution of this method to prevent conflicts on the row objects.
  if (this.widget._renderRowsInProgress) {
    var that = this;
    setTimeout(function() {
      that.onModelAction(event);
    }, 0);
    return;
  }

  if (event.type === 'rowsInserted') {
    this._onRowsInserted(event.rows);
  } else if (event.type === 'rowsDeleted') {
    this._onRowsDeleted(event.rowIds);
  } else if (event.type === 'allRowsDeleted') {
    this._onAllRowsDeleted();
  } else if (event.type === 'rowsSelected') {
    this._onRowsSelected(event.rowIds);
  } else if (event.type === 'rowOrderChanged') {
    this._onRowOrderChanged(event.rowIds);
  } else if (event.type === 'rowsUpdated') {
    this._onRowsUpdated(event.rows);
  } else if (event.type === 'filtersChanged') {
    this._onFiltersChanged(event.filters);
  } else if (event.type === 'rowsChecked') {
    this._onRowsChecked(event.rows);
  } else if (event.type === 'columnStructureChanged') {
    this._onColumnStructureChanged(event.columns);
  } else if (event.type === 'columnOrderChanged') {
    this._onColumnOrderChanged(event.columnIds);
  } else if (event.type === 'columnHeadersUpdated') {
    this._onColumnHeadersUpdated(event.columns);
  } else if (event.type === 'startCellEdit') {
    this._onStartCellEdit(event.columnId, event.rowId, event.fieldId);
  } else if (event.type === 'endCellEdit') {
    this._onEndCellEdit(event.fieldId);
  } else if (event.type === 'requestFocus') {
    this._onRequestFocus();
  } else if (event.type === 'scrollToSelection') {
    this._onScrollToSelection();
  } else if (event.type === 'aggregationFunctionChanged') {
    this._onAggregationFunctionChanged(event);
  } else if (event.type === 'columnBackgroundEffectChanged') {
    this._onColumnBackgroundEffectChanged(event);
  } else if (event.type === 'requestFocusInCell') {
    this._onRequestFocusInCell(event);
  } else if (event.type === 'columnActionsChanged') {
    this._onColumnActionsChanged(event);
  } else {
    scout.TableAdapter.parent.prototype.onModelAction.call(this, event);
  }
};
