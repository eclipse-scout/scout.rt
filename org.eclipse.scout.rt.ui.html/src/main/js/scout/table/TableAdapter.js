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
  this._addRemoteProperties(['contextColumn']);
};
scout.inherits(scout.TableAdapter, scout.ModelAdapter);

scout.TableAdapter.prototype._sendRowsSelected = function(rowIds, debounceSend) {
  var eventData = {
    rowIds: rowIds
  };

  // send delayed to avoid a lot of requests while selecting
  // coalesce: only send the latest selection changed event for a field
  this._send('rowsSelected', eventData, {
    delay: debounceSend ? 250 : 0,
    coalesce: function(previous) {
      return this.id === previous.id && this.type === previous.type;
    }
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

scout.TableAdapter.prototype._onWidgetAddFilter = function(event) {
  var filter = event.filter;
  if (filter instanceof scout.TableUserFilter) {
    this._send('addFilter', filter.createAddFilterEventData());
  }
};

scout.TableAdapter.prototype._onWidgetRemoveFilter = function(event) {
  var filter = event.filter;
  if (filter instanceof scout.TableUserFilter) {
    this._send('removeFilter', filter.createRemoveFilterEventData());
  }
};

scout.TableAdapter.prototype._onWidgetColumnResized = function(event) {
  this._sendColumnResized(event.column);
};

scout.TableAdapter.prototype._sendColumnResized = function(column) {
  if (column.fixedWidth || this.widget.autoResizeColumns) {
    return;
  }

  var eventData = {
    columnId: column.id,
    width: column.width
  };

  // send delayed to avoid a lot of requests while resizing
  // coalesce: only send the latest resize event for a column
  this._send('columnResized', eventData, {
    delay: 750,
    coalesce: function(previous) {
      return this.id === previous.id && this.type === previous.type && this.columnId === previous.columnId;
    },
    showBusyIndicator: false
  });
};

scout.TableAdapter.prototype._onWidgetAggregationFunctionChanged = function(event) {
  this._sendAggregationFunctionChanged(event.column);
};

scout.TableAdapter.prototype._sendAggregationFunctionChanged = function(column) {
  var data = {
    columnId: column.id,
    aggregationFunction: column.aggregationFunction
  };
  this._send('aggregationFunctionChanged', data);
};

scout.TableAdapter.prototype._onWidgetColumnBackgroundEffectChanged = function(event) {
  this._sendColumnBackgroundEffectChanged(event.column);
};

scout.TableAdapter.prototype._sendColumnBackgroundEffectChanged = function(column) {
  var data = {
    columnId: column.id,
    backgroundEffect: column.backgroundEffect
  };
  this._send('columnBackgroundEffectChanged', data);
};

scout.TableAdapter.prototype._onWidgetColumnOrganizeAction = function(event) {
  this._send('columnOrganizeAction', {
    action: event.action,
    columnId: event.column.id
  });
};

scout.TableAdapter.prototype._onWidgetColumnMoved = function(event) {
  var index = event.newPos;
  this.widget.columns.forEach(function(iteratingColumn, i) {
    // Adjust index if column is only known on the gui
    if (iteratingColumn.guiOnly) {
      index--;
    }
  });
  this._sendColumnMoved(event.column, index);
};

scout.TableAdapter.prototype._sendColumnMoved = function(column, index) {
  var data = {
    columnId: column.id,
    index: index
  };
  this._send('columnMoved', data);
};

scout.TableAdapter.prototype._onWidgetPrepareCellEdit = function(event) {
  this._sendPrepareCellEdit(event.row, event.column);
};

scout.TableAdapter.prototype._sendPrepareCellEdit = function(row, column) {
  var data = {
    rowId: row.id,
    columnId: column.id
  };
  this._send('prepareCellEdit', data);
};

scout.TableAdapter.prototype._onWidgetCompleteCellEdit = function(event) {
  this._sendCompleteCellEdit(event.field);
};

scout.TableAdapter.prototype._sendCompleteCellEdit = function(field) {
  var data = {
    fieldId: field.id
  };
  this._send('completeCellEdit', data);
};

scout.TableAdapter.prototype._onWidgetCancelCellEdit = function(event) {
  this._sendCancelCellEdit(event.field);
};

scout.TableAdapter.prototype._sendCancelCellEdit = function(field) {
  var data = {
    fieldId: field.id
  };
  this._send('cancelCellEdit', data);
};

scout.TableAdapter.prototype._onWidgetRowsChecked = function(event) {
  this._sendRowsChecked(event.rows);
};

scout.TableAdapter.prototype._sendRowsChecked = function(rows) {
  var data = {
    rows: []
  };

  for (var i = 0; i < rows.length; i++) {
    data.rows.push({
      rowId: rows[i].id,
      checked: rows[i].checked
    });
  }

  this._send('rowsChecked', data);
};

scout.TableAdapter.prototype._onWidgetRowsFiltered = function(event) {
  var rowIds = this.widget._rowsToIds(this.widget.filteredRows());
  this._sendRowsFiltered(rowIds);
};

scout.TableAdapter.prototype._sendRowsFiltered = function(rowIds) {
  var eventData = {};
  if (rowIds.length === this.widget.rows.length) {
    eventData.remove = true;
  } else {
    eventData.rowIds = rowIds;
  }

  // send with timeout, mainly for incremental load of a large table
  // coalesce: only send last event (don't coalesce remove and 'add' events, the UI server needs both)
  this._send('rowsFiltered', eventData, {
    delay: 250,
    coalesce: function(previous) {
      return this.id === previous.id && this.type === previous.type && this.remove === previous.remove;
    },
    showBusyIndicator: false
  });
};

scout.TableAdapter.prototype._onWidgetRowAction = function(event) {
  this._sendRowAction(event.row, event.column);
};

scout.TableAdapter.prototype._sendRowAction = function(row, column) {
  this._send('rowAction', {
    rowId: row.id,
    columnId: column.id
  });
};

scout.TableAdapter.prototype._onWidgetAppLinkAction = function(event) {
  this._sendAppLinkAction(event.column, event.ref);
};

scout.TableAdapter.prototype._sendAppLinkAction = function(column, ref) {
  this._send('appLinkAction', {
    columnId: column.id,
    ref: ref
  });
};

scout.TableAdapter.prototype._sendContextColumn = function(contextColumn) {
  if (contextColumn.guiOnly) {
    contextColumn = null;
    this.widget.contextColumn = null;
  }
  var columnId = null;
  if (contextColumn) {
    columnId = contextColumn.id;
  }
  this._send('property', {
    contextColumn: columnId
  });
};

scout.TableAdapter.prototype._onWidgetReload = function(event) {
  this._send('reload');
};

scout.TableAdapter.prototype._onWidgetExportToClipbaord = function(event) {
  this._send('exportToClipboard');
};

scout.TableAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'rowsSelected') {
    this._onWidgetRowsSelected(event);
  } else if (event.type === 'rowsChecked') {
    this._onWidgetRowsChecked(event);
  } else if (event.type === 'rowsFiltered') {
    this._onWidgetRowsFiltered(event);
  } else if (event.type === 'rowClicked') {
    this._onWidgetRowClicked(event);
  } else if (event.type === 'rowAction') {
    this._onWidgetRowAction(event);
  } else if (event.type === 'prepareCellEdit') {
    this._onWidgetPrepareCellEdit(event);
  } else if (event.type === 'completeCellEdit') {
    this._onWidgetCompleteCellEdit(event);
  } else if (event.type === 'cancelCellEdit') {
    this._onWidgetCancelCellEdit(event);
  } else if (event.type === 'appLinkAction') {
    this._onWidgetAppLinkAction(event);
  } else if (event.type === 'exportToClipboard') {
    this._onWidgetExportToClipbaord(event);
  } else if (event.type === 'reload') {
    this._onWidgetReload(event);
  } else if (event.type === 'addFilter') {
    this._onWidgetAddFilter(event);
  } else if (event.type === 'removeFilter') {
    this._onWidgetRemoveFilter(event);
  } else if (event.type === 'columnResized') {
    this._onWidgetColumnResized(event);
  } else if (event.type === 'columnMoved') {
    this._onWidgetColumnMoved(event);
  } else if (event.type === 'columnBackgroundEffectChanged') {
    this._onWidgetColumnBackgroundEffectChanged(event);
  } else if (event.type === 'columnOrganizeAction') {
    this._onWidgetColumnOrganizeAction(event);
  } else if (event.type === 'aggregationFunctionChanged') {
    this._onWidgetAggregationFunctionChanged(event);
  } else {
    scout.TableAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};

scout.TableAdapter.prototype._onRowsInserted = function(rows) {
  this.widget.insertRows(rows, true);
};

scout.TableAdapter.prototype._onRowsDeleted = function(rowIds) {
  var rows = this.widget._rowsByIds(rowIds);
  this.addFilterForWidgetEventType('rowsSelected');
  this.widget.deleteRows(rows);
};

scout.TableAdapter.prototype._onAllRowsDeleted = function() {
  this.addFilterForWidgetEventType('rowsSelected');
  this.widget.deleteAllRows();
};

scout.TableAdapter.prototype._onRowsUpdated = function(rows) {
  this.widget.updateRows(rows);
};

scout.TableAdapter.prototype._onRowsSelected = function(rowIds) {
  var rows = this.widget._rowsByIds(rowIds);
  this.addFilterForWidgetEventType('rowsSelected');
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

  this.addFilterForWidgetEventType('rowsChecked');
  this.widget.checkRows(checkedRows, {
    checked: true,
    checkOnlyEnabled: false
  });
  this.widget.uncheckRows(uncheckedRows, {
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
    field = this.session.getOrCreateWidget(fieldId, this.widget);

  this.widget.startCellEdit(column, row, field);
};

scout.TableAdapter.prototype._onEndCellEdit = function(fieldId) {
  var field = this.session.getModelAdapter(fieldId);
  this.widget.endCellEdit(field.widget);
};

scout.TableAdapter.prototype._onRequestFocus = function() {
  this.widget.requestFocus();
};

scout.TableAdapter.prototype._onScrollToSelection = function() {
  this.widget.revealSelection();
};

scout.TableAdapter.prototype._onColumnBackgroundEffectChanged = function(event) {
  event.eventParts.forEach(function(eventPart) {
    var column = this.widget._columnById(eventPart.columnId),
      backgroundEffect = eventPart.backgroundEffect;

    this.addFilterForWidgetEvent(function(widgetEvent) {
      return (widgetEvent.type === 'columnBackgroundEffectChanged' &&
        widgetEvent.column.id === column.id &&
        widgetEvent.column.backgroundEffect === backgroundEffect);
    });

    column.setBackgroundEffect(backgroundEffect);
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

    this.addFilterForWidgetEvent(function(widgetEvent) {
      return (widgetEvent.type === 'aggregationFunctionChanged' &&
        widgetEvent.column.id === column.id &&
        widgetEvent.column.aggregationFunction === func);
    });

    columns.push(column);
    functions.push(func);
  }, this);

  this.widget.changeAggregations(columns, functions);
};

scout.TableAdapter.prototype._onFiltersChanged = function(filters) {
  this.addFilterForWidgetEventType('addFilter');
  this.addFilterForWidgetEventType('removeFilter');

  this.widget.setFilters(filters);
  // do not refilter while the table is being rebuilt (because column.index in filter and row.cells may be inconsistent)
  if (!this.widget._rebuildingTable) { //FIXME CGU [6.1] gehört das nicht direkt in filter rein?
    this.widget.filter();
  }
};

scout.TableAdapter.prototype.onModelAction = function(event) {
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
  } else {
    scout.TableAdapter.parent.prototype.onModelAction.call(this, event);
  }
};

/**
 * @override ModelAdapter.js
 */
scout.TableAdapter.prototype.exportAdapterData = function(adapterData) {
  adapterData = scout.TableAdapter.parent.prototype.exportAdapterData.call(this, adapterData);
  delete adapterData.selectedRows;
  adapterData.rows = [];
  adapterData.columns.forEach(function(column) {
    delete column.classId;
    delete column.modelClass;
  });
  return adapterData;
};

/**
 * Static method to modify the prototype of scout.Table.
 */
scout.TableAdapter.modifyTablePrototype = function() {
  if (!scout.app.remote) {
    return;
  }

  // prepareCellEdit
  scout.objects.replacePrototypeFunction(scout.Table, 'prepareCellEdit', function(column, row, openFieldPopupOnCellEdit) {
    this.openFieldPopupOnCellEdit = scout.nvl(openFieldPopupOnCellEdit, false);
    this.trigger('prepareCellEdit', {
      column: column,
      row: row
    });
  });

  // completeCellEdit
  scout.objects.replacePrototypeFunction(scout.Table, 'completeCellEdit', function(field) {
    this.trigger('completeCellEdit', {
      field: field
    });
  });

  // cancelCellEdit
  scout.objects.replacePrototypeFunction(scout.Table, 'cancelCellEdit', function(field) {
    this.trigger('cancelCellEdit', {
      field: field
    });
  });
};

scout.TableAdapter.modifyBooleanColumnPrototype = function() {
  if (!scout.app.remote) {
    return;
  }

  // _toggleCellValue
  scout.objects.replacePrototypeFunction(scout.BooleanColumn, '_toggleCellValue', function(row, cell) {
    // NOP - do nothing, since server will handle the click, see Java AbstractTable#interceptRowClickSingleObserver
  });
};

scout.addAppListener('bootstrap', scout.TableAdapter.modifyTablePrototype);
scout.addAppListener('bootstrap', scout.TableAdapter.modifyBooleanColumnPrototype);
