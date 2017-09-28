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
scout.TableAdapter = function() {
  scout.TableAdapter.parent.call(this);
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
      return this.target === previous.target && this.type === previous.type;
    }
  });
};

scout.TableAdapter.prototype._sendRowClick = function(rowId, mouseButton, columnId) {
  var data = {
    rowId: rowId,
    columnId: columnId,
    mouseButton: mouseButton
  };
  this._send('rowClick', data);
};

scout.TableAdapter.prototype._onWidgetRowsSelected = function(event) {
  var rowIds = this.widget._rowsToIds(this.widget.selectedRows);
  this._sendRowsSelected(rowIds, event.debounce);
};

scout.TableAdapter.prototype._onWidgetRowClick = function(event) {
  var columnId;
  if (event.column !== undefined) {
    columnId = event.column.id;
  }

  this._sendRowClick(event.row.id, event.mouseButton, columnId);
};

scout.TableAdapter.prototype._onWidgetFilterAdded = function(event) {
  var filter = event.filter;
  if (filter instanceof scout.TableUserFilter) {
    this._send('filterAdded', filter.createFilterAddedEventData());
  }
};

scout.TableAdapter.prototype._onWidgetFilterRemoved = function(event) {
  var filter = event.filter;
  if (filter instanceof scout.TableUserFilter) {
    this._send('filterRemoved', filter.createFilterRemovedEventData());
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
      return this.target === previous.target && this.type === previous.type && this.columnId === previous.columnId;
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

scout.TableAdapter.prototype._onWidgetFilter = function(event) {
  var rowIds = this.widget._rowsToIds(this.widget.filteredRows());
  this._sendFilter(rowIds);
};

scout.TableAdapter.prototype._sendFilter = function(rowIds) {
  var eventData = {};
  if (rowIds.length === this.widget.rows.length) {
    eventData.remove = true;
  } else {
    eventData.rowIds = rowIds;
  }

  // send with timeout, mainly for incremental load of a large table
  // coalesce: only send last event (don't coalesce remove and 'add' events, the UI server needs both)
  this._send('filter', eventData, {
    delay: 250,
    coalesce: function(previous) {
      return this.target === previous.target && this.type === previous.type && this.remove === previous.remove;
    },
    showBusyIndicator: false
  });
};

scout.TableAdapter.prototype._onWidgetSort = function(event) {
  this._send('sort', {
    columnId: event.column.id,
    sortAscending: event.sortAscending,
    sortingRemoved: event.sortingRemoved,
    multiSort: event.multiSort,
    sortingRequested: event.sortingRequested
  });
};

scout.TableAdapter.prototype._onWidgetGroup = function(event) {
  this._send('group', {
    columnId: event.column.id,
    groupAscending: event.groupAscending,
    groupingRemoved: event.groupingRemoved,
    multiGroup: event.multiGroup,
    groupingRequested: event.groupingRequested
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
  this._send('clipboardExport');
};

scout.TableAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'rowsSelected') {
    this._onWidgetRowsSelected(event);
  } else if (event.type === 'rowsChecked') {
    this._onWidgetRowsChecked(event);
  } else if (event.type === 'filter') {
    this._onWidgetFilter(event);
  } else  if (event.type === 'sort') {
    this._onWidgetSort(event);
  } else  if (event.type === 'group') {
    this._onWidgetGroup(event);
  } else if (event.type === 'rowClick') {
    this._onWidgetRowClick(event);
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
  } else if (event.type === 'clipboardExport') {
    this._onWidgetExportToClipbaord(event);
  } else if (event.type === 'reload') {
    this._onWidgetReload(event);
  } else if (event.type === 'filterAdded') {
    this._onWidgetFilterAdded(event);
  } else if (event.type === 'filterRemoved') {
    this._onWidgetFilterRemoved(event);
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
  this.widget.insertRows(rows);
  this._rebuildingTable = false;
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
  // TODO [7.0] cgu what is this for? seems wrong here
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
  this._rebuildingTable = true;
  this.widget.updateColumnStructure(columns);
};

scout.TableAdapter.prototype._onColumnOrderChanged = function(columnIds) {
  var columns = this.widget.columnsByIds(columnIds);
  this.widget.updateColumnOrder(columns);
};

scout.TableAdapter.prototype._onColumnHeadersUpdated = function(columns) {
  columns.forEach(function(column) {
    scout.defaultValues.applyTo(column);
  });
  this.widget.updateColumnHeaders(columns);
};

scout.TableAdapter.prototype._onStartCellEdit = function(columnId, rowId, fieldId) {
  var column = this.widget.columnById(columnId),
    row = this.widget._rowById(rowId),
    field = this.session.getOrCreateWidget(fieldId, this.widget);

  this.widget.startCellEdit(column, row, field);
};

scout.TableAdapter.prototype._onEndCellEdit = function(fieldId) {
  var field = this.session.getModelAdapter(fieldId);
  this.widget.endCellEdit(field.widget);
};

scout.TableAdapter.prototype._onRequestFocus = function() {
  this.widget.focus();
};

scout.TableAdapter.prototype._onScrollToSelection = function() {
  this.widget.revealSelection();
};

scout.TableAdapter.prototype._onColumnBackgroundEffectChanged = function(event) {
  event.eventParts.forEach(function(eventPart) {
    var column = this.widget.columnById(eventPart.columnId),
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
    column = this.widget.columnById(event.columnId);

  this.widget.focusCell(column, row);
};

scout.TableAdapter.prototype._onAggregationFunctionChanged = function(event) {
  var columns = [],
    functions = [];

  event.eventParts.forEach(function(eventPart) {
    var func = eventPart.aggregationFunction,
      column = this.widget.columnById(eventPart.columnId);

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
  this.addFilterForWidgetEventType('filterAdded');
  this.addFilterForWidgetEventType('filterRemoved');

  this.widget.setFilters(filters);
  // do not refilter while the table is being rebuilt (because column.index in filter and row.cells may be inconsistent)
  if (!this._rebuildingTable) {
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
  scout.objects.replacePrototypeFunction(scout.Table, 'prepareCellEditInternal', function(column, row, openFieldPopupOnCellEdit) {
    if (this.modelAdapter) {
      this.openFieldPopupOnCellEdit = scout.nvl(openFieldPopupOnCellEdit, false);
      this.trigger('prepareCellEdit', {
        column: column,
        row: row
      });
    } else {
      this.prepareCellEditInternalOrig(column, row, openFieldPopupOnCellEdit);
    }
  }, true);

  // completeCellEdit
  scout.objects.replacePrototypeFunction(scout.Table, 'completeCellEdit', function(field) {
    if (this.modelAdapter) {
      this.trigger('completeCellEdit', {
        field: field
      });
    } else {
      this.completeCellEditOrig(field);
    }
  }, true);

  // cancelCellEdit
  scout.objects.replacePrototypeFunction(scout.Table, 'cancelCellEdit', function(field) {
    if (this.modelAdapter) {
      this.trigger('cancelCellEdit', {
        field: field
      });
    } else {
      this.cancelCellEditOrig(field);
    }
  }, true);

  scout.objects.replacePrototypeFunction(scout.Table, '_sortAfterInsert', function(wasEmpty) {
    if (this.modelAdapter) {
      // There will only be a row order changed event if table was not empty.
      // If it was empty, there will be NO row order changed event (tableEventBuffer) -> inserted rows are already in correct order -> no sort necessary but group is
      if (wasEmpty) {
        this._group();
      }
    } else {
      this._sortAfterInsertOrig(wasEmpty);
    }
  }, true);

  // _sortAfterUpdate
  scout.objects.replacePrototypeFunction(scout.Table, '_sortAfterUpdate', function() {
    if (this.modelAdapter) {
      this._group();
    } else {
      this._sortAfterUpdateOrig();
    }
  }, true);

  // uiSortPossible
  scout.objects.replacePrototypeFunction(scout.Table, '_isSortingPossible', function(sortColumns) {
    if (this.modelAdapter) {
       // In a JS only app the flag 'uiSortPossible' is never set and thus defaults to true. Additionally we check if each column can install
       // its comparator used to sort. If installation failed for some reason, sorting is not possible. In a remote app the server sets the
       // 'uiSortPossible' flag, which decides if the column must be sorted by the server or can be sorted by the client.
      var uiSortPossible = scout.nvl(this.uiSortPossible, true);
      return uiSortPossible && this._isSortingPossibleOrig(sortColumns);
    }
    return this._isSortingPossibleOrig(sortColumns);
  }, true);
};

scout.TableAdapter.modifyColumnPrototype = function() {
  if (!scout.app.remote) {
    return;
  }

  // init
  scout.objects.replacePrototypeFunction(scout.Column, 'init', function(model) {
    if (model.table && model.table.modelAdapter) {
      // Fill in the missing default values only in remote case, don't do it JS case to not accidentally set undefined properties (e.g. uiSortEnabled)
      model = $.extend({}, model);
      scout.defaultValues.applyTo(model);
    }
    this.initOrig(model);
  }, true);

  // _ensureCell
  scout.objects.replacePrototypeFunction(scout.Column, '_ensureCell', function(vararg) {
    if (this.table.modelAdapter) {
      // Note: we do almost the same thing as in _ensureCellOrig, the difference is that
      // we treat a plain object always as cell-model and we always must apply defaultValues
      // to this cell model. In the JS only case a plain-object has no special meaning and
      // can be used as cell-value in the same way as a scalar value. Also we must not apply
      // defaultValues in JS only case, because it would destroy the 'undefined' state of the
      // cell properties, which is required because the Column checks, whether it should apply
      // defaults from the Column instance to a cell, or use the values from the cell.
      var model;
      if (scout.objects.isPlainObject(vararg)) {
        model = vararg;
        model.value = this._parseValue(model.value);
        // Parse the value if a text but no value is provided. The server does only set the text if value and text are equal.
        // It is also necessary for custom columns which don't have a UI representation and never send the value.
        // Do not parse the value if there is an error status.
        // If editing fails, the display text will be the user input, the value unchanged, and the server will set the error status.
        if (model.text && model.value === undefined  && !model.errorStatus) {
          model.value = this._parseValue(model.text);
        }
        // use null instead of undefined
        if (model.value === undefined) {
          model.value = null;
        }
      } else {
        model = {
          value: this._parseValue(vararg)
        };
      }
      scout.defaultValues.applyTo(model, 'Cell');
      return scout.create('Cell', model);
    } else {
      return this._ensureCellOrig(vararg);
    }
  }, true);

  // uiSortPossible
  scout.objects.replacePrototypeFunction(scout.Column, 'isSortingPossible', function() {
    if (this.table.modelAdapter) {
       // Returns whether or not this column can be used to sort on the client side. In a JS only app the flag 'uiSortPossible'
       // is never set and defaults to true. As a side effect of this function a comparator is installed.
       // The comparator returns false if it could not be installed which means sorting should be delegated to server (e.g. collator is not available).
       // In a remote app the server sets the 'uiSortPossible' flag, which decides if the column must be sorted by the
       // server or can be sorted by the client.
      var uiSortPossible = scout.nvl(this.uiSortPossible, true);
      return uiSortPossible && this.installComparator();
    }
    return this.isSortingPossibleOrig();
  }, true);
};

scout.TableAdapter.modifyBooleanColumnPrototype = function() {
  if (!scout.app.remote) {
    return;
  }

  // _toggleCellValue
  scout.objects.replacePrototypeFunction(scout.BooleanColumn, '_toggleCellValue', function(row, cell) {
    if (this.table.modelAdapter) {
      // NOP - do nothing, since server will handle the click, see Java AbstractTable#interceptRowClickSingleObserver
    } else {
      this._toggleCellValueOrig();
    }
  }, true);
};

scout.addAppListener('bootstrap', scout.TableAdapter.modifyTablePrototype);
scout.addAppListener('bootstrap', scout.TableAdapter.modifyColumnPrototype);
scout.addAppListener('bootstrap', scout.TableAdapter.modifyBooleanColumnPrototype);
