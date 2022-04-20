/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {App, arrays, BooleanColumn, Column, ColumnUserFilter, defaultValues, ModelAdapter, objects, scout, Table, TableUserFilter} from '../index';
import $ from 'jquery';

export default class TableAdapter extends ModelAdapter {

  constructor() {
    super();
    this._addRemoteProperties(['contextColumn']);
  }

  _initProperties(model) {
    super._initProperties(model);
    model.compactHandler = null; // Disable Scout JS compact handling, will be done on the server
  }

  _postCreateWidget() {
    // if a newly created table has already a userfilter defined, we need to fire the filter event after creation
    // because the original event had been fired before the eventhandler was registered.
    if (this.widget.hasUserFilter()) {
      this._onWidgetFilter();
    }
  }

  _sendRowsSelected(rowIds, debounceSend) {
    let eventData = {
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
  }

  _sendRowClick(rowId, mouseButton, columnId) {
    let data = {
      rowId: rowId,
      columnId: columnId,
      mouseButton: mouseButton
    };
    this._send('rowClick', data);
  }

  _onWidgetRowsSelected(event) {
    let rowIds = this.widget._rowsToIds(this.widget.selectedRows);
    this._sendRowsSelected(rowIds, event.debounce);
  }

  _onWidgetRowClick(event) {
    let columnId;
    if (event.column !== undefined) {
      columnId = event.column.id;
    }

    this._sendRowClick(event.row.id, event.mouseButton, columnId);
  }

  _onWidgetFilterAdded(event) {
    let filter = event.filter;
    if (!(filter instanceof TableUserFilter) || (filter instanceof ColumnUserFilter && filter.column.guiOnly)) {
      return;
    }
    this._send('filterAdded', filter.createFilterAddedEventData());
  }

  _onWidgetFilterRemoved(event) {
    let filter = event.filter;
    if (!(filter instanceof TableUserFilter) || (filter instanceof ColumnUserFilter && filter.column.guiOnly)) {
      return;
    }
    this._send('filterRemoved', filter.createFilterRemovedEventData());
  }

  _onWidgetColumnResized(event) {
    this._sendColumnResized(event.column);
  }

  _sendColumnResized(column) {
    if (column.fixedWidth || column.guiOnly || this.widget.autoResizeColumns) {
      return;
    }

    let eventData = {
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
  }

  _onWidgetAggregationFunctionChanged(event) {
    this._sendAggregationFunctionChanged(event.column);
  }

  _sendAggregationFunctionChanged(column) {
    if (column.guiOnly) {
      return;
    }
    let data = {
      columnId: column.id,
      aggregationFunction: column.aggregationFunction
    };
    this._send('aggregationFunctionChanged', data);
  }

  _onWidgetColumnBackgroundEffectChanged(event) {
    this._sendColumnBackgroundEffectChanged(event.column);
  }

  _sendColumnBackgroundEffectChanged(column) {
    if (column.guiOnly) {
      return;
    }
    let data = {
      columnId: column.id,
      backgroundEffect: column.backgroundEffect
    };
    this._send('columnBackgroundEffectChanged', data);
  }

  _onWidgetColumnOrganizeAction(event) {
    this._send('columnOrganizeAction', {
      action: event.action,
      columnId: event.column.id
    });
  }

  _onWidgetColumnMoved(event) {
    let index = event.newPos;
    this.widget.columns.forEach((iteratingColumn, i) => {
      // Adjust index if column is only known on the gui
      if (iteratingColumn.guiOnly) {
        index--;
      }
    });
    this._sendColumnMoved(event.column, index);
  }

  _sendColumnMoved(column, index) {
    if (column.guiOnly) {
      return;
    }
    let data = {
      columnId: column.id,
      index: index
    };
    this._send('columnMoved', data);
  }

  _onWidgetPrepareCellEdit(event) {
    event.preventDefault();
    this._sendPrepareCellEdit(event.row, event.column);
  }

  _sendPrepareCellEdit(row, column) {
    if (column.guiOnly) {
      return;
    }
    let data = {
      rowId: row.id,
      columnId: column.id
    };
    this._send('prepareCellEdit', data);
  }

  _onWidgetCompleteCellEdit(event) {
    event.preventDefault();
    this._sendCompleteCellEdit();
  }

  _sendCompleteCellEdit() {
    this._send('completeCellEdit');
  }

  _onWidgetCancelCellEdit(event) {
    event.preventDefault();
    this._sendCancelCellEdit();
  }

  _sendCancelCellEdit() {
    this._send('cancelCellEdit');
  }

  _onWidgetRowsChecked(event) {
    this._sendRowsChecked(event.rows);
  }

  _sendRowsChecked(rows) {
    let data = {
      rows: []
    };

    for (let i = 0; i < rows.length; i++) {
      data.rows.push({
        rowId: rows[i].id,
        checked: rows[i].checked
      });
    }

    this._send('rowsChecked', data);
  }

  _onWidgetRowsExpanded(event) {
    this._sendRowsExpanded(event.rows);
  }

  _sendRowsExpanded(rows) {
    let data = {
      rows: rows.map(row => {
        return {
          rowId: row.id,
          expanded: row.expanded
        };
      })
    };
    this._send('rowsExpanded', data);
  }

  _onWidgetFilter(event) {
    let rowIds = this.widget._rowsToIds(this.widget.filteredRows());
    this._sendFilter(rowIds);
  }

  _sendFilter(rowIds) {
    let eventData = {};
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
  }

  _onWidgetSort(event) {
    if (event.column.guiOnly) {
      return;
    }
    this._send('sort', {
      columnId: event.column.id,
      sortAscending: event.sortAscending,
      sortingRemoved: event.sortingRemoved,
      multiSort: event.multiSort,
      sortingRequested: event.sortingRequested
    });
  }

  _onWidgetGroup(event) {
    if (event.column.guiOnly) {
      return;
    }
    this._send('group', {
      columnId: event.column.id,
      groupAscending: event.groupAscending,
      groupingRemoved: event.groupingRemoved,
      multiGroup: event.multiGroup,
      groupingRequested: event.groupingRequested
    });
  }

  _onWidgetRowAction(event) {
    this._sendRowAction(event.row, event.column);
  }

  _sendRowAction(row, column) {
    if (column.guiOnly) {
      // Send row action with a real column
      // If there is only one guiOnly column (e.g. CompactColumn), sent column will be null
      column = arrays.find(this.columns, col => {
        return !col.guiOnly;
      });
    }
    let columnId = column ? column.id : null;
    this._send('rowAction', {
      rowId: row.id,
      columnId: columnId
    });
  }

  _onWidgetAppLinkAction(event) {
    this._sendAppLinkAction(event.column, event.ref);
  }

  _sendAppLinkAction(column, ref) {
    this._send('appLinkAction', {
      columnId: column.id,
      ref: ref
    });
  }

  _sendContextColumn(contextColumn) {
    if (contextColumn.guiOnly) {
      contextColumn = null;
      this.widget.contextColumn = null;
    }
    let columnId = null;
    if (contextColumn) {
      columnId = contextColumn.id;
    }
    this._send('property', {
      contextColumn: columnId
    });
  }

  _onWidgetReload(event) {
    let data = {
      reloadReason: event.reloadReason
    };
    this._send('reload', data);
  }

  _onWidgetExportToClipboard(event) {
    this._send('clipboardExport');
    event.preventDefault();
  }

  _onWidgetEvent(event) {
    if (event.type === 'rowsSelected') {
      this._onWidgetRowsSelected(event);
    } else if (event.type === 'rowsChecked') {
      this._onWidgetRowsChecked(event);
    } else if (event.type === 'rowsExpanded') {
      this._onWidgetRowsExpanded(event);
    } else if (event.type === 'filter') {
      this._onWidgetFilter(event);
    } else if (event.type === 'sort') {
      this._onWidgetSort(event);
    } else if (event.type === 'group') {
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
      this._onWidgetExportToClipboard(event);
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
    } else if (event.type === 'drop' && this.widget.dragAndDropHandler) {
      this.widget.dragAndDropHandler.uploadFiles(event);
    } else {
      super._onWidgetEvent(event);
    }
  }

  _onRowsInserted(rows) {
    this.widget.insertRows(rows);
    this._rebuildingTable = false;
  }

  _onRowsDeleted(rowIds) {
    let rows = this.widget._rowsByIds(rowIds);
    this.addFilterForWidgetEventType('rowsSelected');
    this.widget.deleteRows(rows);
  }

  _onAllRowsDeleted() {
    this.addFilterForWidgetEventType('rowsSelected');
    this.widget.deleteAllRows();
  }

  _onRowsUpdated(rows) {
    this.widget.updateRows(rows);
  }

  _onRowsSelected(rowIds) {
    let rows = this.widget._rowsByIds(rowIds);
    this.addFilterForWidgetEventType('rowsSelected');
    this.widget.selectRows(rows);
    // TODO [7.0] cgu what is this for? seems wrong here
    this.widget.selectionHandler.clearLastSelectedRowMarker();
  }

  _onRowsChecked(rows) {
    let checkedRows = [],
      uncheckedRows = [];

    rows.forEach(function(rowData) {
      let row = this.widget._rowById(rowData.id);
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
  }

  _onRowsExpanded(rows) {
    let expandedRows = [],
      collapsedRows = [];
    rows.forEach(function(rowData) {
      let row = this.widget._rowById(rowData.id);
      if (rowData.expanded) {
        expandedRows.push(row);
      } else {
        collapsedRows.push(row);
      }
    }, this);
    this.addFilterForWidgetEventType('rowsExpanded');

    this.widget.expandRows(expandedRows);
    this.widget.collapseRows(collapsedRows);
  }

  _onRowOrderChanged(rowIds) {
    let rows = this.widget._rowsByIds(rowIds);
    this.widget.updateRowOrder(rows);
  }

  _onColumnStructureChanged(columns) {
    this._rebuildingTable = true;
    this.widget.updateColumnStructure(columns);
  }

  _onColumnOrderChanged(columnIds) {
    let columns = this.widget.columnsByIds(columnIds);
    this.widget.updateColumnOrder(columns);
  }

  _onColumnHeadersUpdated(columns) {
    columns.forEach(column => {
      defaultValues.applyTo(column);
    });
    this.widget.updateColumnHeaders(columns);

    if (this.widget.tileMode && this.widget.tableTileGridMediator) {
      // grouping might have changed, trigger reinit of the groups on the tileGrid in tileMode
      this.widget.tableTileGridMediator._onTableGroup();
      // removing of a group column doesn't cause a rowOrderChange, nonetheless aggregation columns might need to be removed.
      this.widget.updateRowOrder(this.widget.rows);
    }
  }

  _onStartCellEdit(columnId, rowId, fieldId) {
    let column = this.widget.columnById(columnId),
      row = this.widget._rowById(rowId),
      field = this.session.getOrCreateWidget(fieldId, this.widget);

    this.widget.startCellEdit(column, row, field);
  }

  _onEndCellEdit(fieldId) {
    let field = this.session.getModelAdapter(fieldId);
    if (!field) {
      throw new Error('Field adapter could not be resolved. Id: ' + fieldId);
    }
    this.widget.endCellEdit(field.widget);
  }

  _onRequestFocus() {
    this.widget.focus();
  }

  _onScrollToSelection() {
    this.widget.revealSelection();
  }

  _onColumnBackgroundEffectChanged(event) {
    event.eventParts.forEach(function(eventPart) {
      let column = this.widget.columnById(eventPart.columnId),
        backgroundEffect = eventPart.backgroundEffect;

      this.addFilterForWidgetEvent(widgetEvent => {
        return (widgetEvent.type === 'columnBackgroundEffectChanged' &&
          widgetEvent.column.id === column.id &&
          widgetEvent.column.backgroundEffect === backgroundEffect);
      });

      column.setBackgroundEffect(backgroundEffect);
    }, this);
  }

  _onRequestFocusInCell(event) {
    let row = this.widget._rowById(event.rowId),
      column = this.widget.columnById(event.columnId);

    this.widget.focusCell(column, row);
  }

  _onAggregationFunctionChanged(event) {
    let columns = [],
      functions = [];

    event.eventParts.forEach(function(eventPart) {
      let func = eventPart.aggregationFunction,
        column = this.widget.columnById(eventPart.columnId);

      this.addFilterForWidgetEvent(widgetEvent => {
        return (widgetEvent.type === 'aggregationFunctionChanged' &&
          widgetEvent.column.id === column.id &&
          widgetEvent.column.aggregationFunction === func);
      });

      columns.push(column);
      functions.push(func);
    }, this);

    this.widget.changeAggregations(columns, functions);
  }

  _onFiltersChanged(filters) {
    this.addFilterForWidgetEventType('filterAdded');
    this.addFilterForWidgetEventType('filterRemoved');

    this.widget.setFilters(filters);
    // do not refilter while the table is being rebuilt (because column.index in filter and row.cells may be inconsistent)
    if (!this._rebuildingTable) {
      this.widget.filter();
    }
  }

  onModelAction(event) {
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
    } else if (event.type === 'rowsExpanded') {
      this._onRowsExpanded(event.rows);
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
      super.onModelAction(event);
    }
  }

  /**
   * @override ModelAdapter.js
   */
  exportAdapterData(adapterData) {
    adapterData = super.exportAdapterData(adapterData);
    delete adapterData.selectedRows;
    adapterData.rows = [];
    adapterData.columns.forEach(column => {
      delete column.classId;
      delete column.modelClass;
    });
    return adapterData;
  }

  /**
   * Static method to modify the prototype of Table.
   */
  static modifyTablePrototype() {
    if (!App.get().remote) {
      return;
    }

    // _sortAfterInsert
    objects.replacePrototypeFunction(Table, '_sortAfterInsert', function(wasEmpty) {
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
    objects.replacePrototypeFunction(Table, '_sortAfterUpdate', function() {
      if (this.modelAdapter) {
        this._group();
      } else {
        this._sortAfterUpdateOrig();
      }
    }, true);

    // uiSortPossible
    objects.replacePrototypeFunction(Table, '_isSortingPossible', function(sortColumns) {
      if (this.modelAdapter) {
        // In a JS only app the flag 'uiSortPossible' is never set and thus defaults to true. Additionally we check if each column can install
        // its comparator used to sort. If installation failed for some reason, sorting is not possible. In a remote app the server sets the
        // 'uiSortPossible' flag, which decides if the column must be sorted by the server or can be sorted by the client.
        let uiSortPossible = scout.nvl(this.uiSortPossible, true);
        return uiSortPossible && this._isSortingPossibleOrig(sortColumns);
      }
      return this._isSortingPossibleOrig(sortColumns);
    }, true);

    // sort
    objects.replacePrototypeFunction(Table, 'sort', function(column, direction, multiSort, remove) {
      if (this.modelAdapter && column.guiOnly) {
        return;
      }
      this.sortOrig(column, direction, multiSort, remove);
    }, true);

    // no js default tileTableHeader in classic mode
    objects.replacePrototypeFunction(Table, '_createTileTableHeader', function() {
      if (this.modelAdapter) {
        // nop in classic mode
        return;
      }
      return this._createTileTableHeaderOrig();
    }, true);

    // not used in classic mode since tiles are created by the server
    objects.replacePrototypeFunction(Table, 'createTiles', function(rows) {
      if (this.modelAdapter) {
        // nop in classic mode
        return;
      }
      return this.createTilesOrig(rows);
    }, true);
  }

  static modifyColumnPrototype() {
    if (!App.get().remote) {
      return;
    }

    // init
    objects.replacePrototypeFunction(Column, 'init', function(model) {
      if (model.table && model.table.modelAdapter) {
        // Fill in the missing default values only in remote case, don't do it JS case to not accidentally set undefined properties (e.g. uiSortEnabled)
        model = $.extend({}, model);
        defaultValues.applyTo(model);
      }
      this.initOrig(model);
    }, true);

    // _ensureCell
    objects.replacePrototypeFunction(Column, '_ensureCell', function(vararg) {
      if (this.table.modelAdapter) {
        // Note: we do almost the same thing as in _ensureCellOrig, the difference is that
        // we treat a plain object always as cell-model and we always must apply defaultValues
        // to this cell model. In the JS only case a plain-object has no special meaning and
        // can be used as cell-value in the same way as a scalar value. Also we must not apply
        // defaultValues in JS only case, because it would destroy the 'undefined' state of the
        // cell properties, which is required because the Column checks, whether it should apply
        // defaults from the Column instance to a cell, or use the values from the cell.
        let model;
        if (objects.isPlainObject(vararg)) {
          model = vararg;
          model.value = this._parseValue(model.value);
          // Parse the value if a text but no value is provided. The server does only set the text if value and text are equal.
          // It is also necessary for custom columns which don't have a UI representation and never send the value.
          // Do not parse the value if there is an error status.
          // If editing fails, the display text will be the user input, the value unchanged, and the server will set the error status.
          if (model.text && model.value === undefined && !model.errorStatus) {
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
        defaultValues.applyTo(model, 'Cell');
        return scout.create('Cell', model);
      }
      return this._ensureCellOrig(vararg);
    }, true);

    // uiSortPossible
    objects.replacePrototypeFunction(Column, 'isSortingPossible', function() {
      if (this.table.modelAdapter) {
        // Returns whether or not this column can be used to sort on the client side. In a JS only app the flag 'uiSortPossible'
        // is never set and defaults to true. As a side effect of this function a comparator is installed.
        // The comparator returns false if it could not be installed which means sorting should be delegated to server (e.g. collator is not available).
        // In a remote app the server sets the 'uiSortPossible' flag, which decides if the column must be sorted by the
        // server or can be sorted by the client.
        let uiSortPossible = scout.nvl(this.uiSortPossible, true);
        return uiSortPossible && this.installComparator();
      }
      return this.isSortingPossibleOrig();
    }, true);
  }

  static modifyBooleanColumnPrototype() {
    if (!App.get().remote) {
      return;
    }

    // _toggleCellValue
    objects.replacePrototypeFunction(BooleanColumn, '_toggleCellValue', function(row, cell) {
      if (this.table.modelAdapter) {
        // NOP - do nothing, since server will handle the click, see Java AbstractTable#interceptRowClickSingleObserver
      } else {
        this._toggleCellValueOrig(row, cell);
      }
    }, true);
  }
}

App.addListener('bootstrap', TableAdapter.modifyTablePrototype);
App.addListener('bootstrap', TableAdapter.modifyColumnPrototype);
App.addListener('bootstrap', TableAdapter.modifyBooleanColumnPrototype);
