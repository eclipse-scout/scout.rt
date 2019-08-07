/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TileTableHeaderBox = function() {
  scout.TileTableHeaderBox.parent.call(this);

  this.table = null;
  this.labelVisible = false;
  this.statusVisible = false;
  this.gridColumnCount = 7;
  this.bodyLayoutConfig = {
    hgap: 8
  };

  this.groupByField = null;
  this.sortByField = null;

  this._tableGroupHandler = this._onTableGroup.bind(this);
  this._tableSortHandler = this._onTableSort.bind(this);
  this._destroyHandler = this._uninstallListeners.bind(this);
};
scout.inherits(scout.TileTableHeaderBox, scout.GroupBox);

scout.TileTableHeaderBox.prototype._installListeners = function() {
  this.table.on('group', this._tableGroupHandler);
  this.table.on('sort', this._tableSortHandler);
  this.table.one('destroy', this._destroyHandler);
};

scout.TileTableHeaderBox.prototype._uninstallListeners = function() {
  this.table.off('group', this._tableGroupHandler);
  this.table.off('sort', this._tableSortHandler);
};

scout.TileTableHeaderBox.prototype._init = function(model) {
  scout.TileTableHeaderBox.parent.prototype._init.call(this, model);

  this.table = this.parent;
  this._installListeners();

  this.insertField(scout.create('PlaceholderField', {
    parent: this,
    gridDataHints: {
      w: 5
    }
  }));

  // Group By Field
  this.groupByField = scout.create('SmartField', {
    parent: this,
    label: this.session.text('GroupBy'),
    labelPosition: scout.FormField.LabelPosition.ON_FIELD,
    clearable: scout.ValueField.Clearable.ALWAYS,
    statusVisible: false,
    displayStyle: scout.SmartField.DisplayStyle.DROPDOWN
  });
  this.groupByField.setLookupCall(this._createGroupByLookupCall());
  this.groupByField.setVisible(!scout.arrays.empty(this.groupByField.lookupCall.data));
  this.groupByField.on('propertyChange', this._onGroupingChange.bind(this));

  this.insertField(this.groupByField);

  // Sort By Field
  this.sortByField = scout.create('SmartField', {
    parent: this,
    label: this.session.text('SortBy'),
    labelPosition: scout.FormField.LabelPosition.ON_FIELD,
    clearable: scout.ValueField.Clearable.ALWAYS,
    statusVisible: false,
    displayStyle: scout.SmartField.DisplayStyle.DROPDOWN
  });
  this.sortByField.setLookupCall(this._createSortByLookupCall());
  this.sortByField.setVisible(!scout.arrays.empty(this.sortByField.lookupCall.data));
  this.sortByField.on('propertyChange', this._onSortingChange.bind(this));

  this.insertField(this.sortByField);

  // it's okay to sync the fields here, _onGroupingChange/_onSortingChange will return early since the tileMode property is not set yet at this point
  this._syncSortingGroupingFields();
};

scout.TileTableHeaderBox.prototype._findSortByLookupRowForKey = function(key) {
  return this.sortByField.lookupCall.data.map(function(lookupRow) {
    return lookupRow[0];
  }).find(function(rowKey) {
    return rowKey.column === key.column && rowKey.asc === key.asc;
  }, this);
};

scout.TileTableHeaderBox.prototype._createGroupByLookupCall = function() {
  return scout.create('TileTableHeaderGroupByLookupCall', {
    session: this.session,
    table: this.table
  });
};

scout.TileTableHeaderBox.prototype._createSortByLookupCall = function() {
  return scout.create('TileTableHeaderSortByLookupCall', {
    session: this.session,
    table: this.table
  });
};

scout.TileTableHeaderBox.prototype._onGroupingChange = function(event) {
  if (!this.table.tileMode) {
    return;
  }
  if (event.propertyName === 'value') {
    this.isGrouping = true;
    var column;
    if (event.newValue !== null) {
      column = event.newValue;
      var direction = (column.sortIndex >= 0 && !column.sortAscending) ? 'desc' : 'asc';
      this.table.groupColumn(column, false, direction, false);
    } else {
      column = event.oldValue;
      this.table.groupColumn(column, false, null, true);
    }
    this.isGrouping = false;
  }
};

scout.TileTableHeaderBox.prototype._onSortingChange = function(event) {
  if (!this.table.tileMode) {
    return;
  }
  if (event.propertyName === 'value') {
    this.isSorting = true;
    var column, sortInfo;
    if (event.newValue !== null) {
      sortInfo = event.newValue.column;
      column = event.newValue.column;
      this.table.sort(event.newValue.column, event.newValue.asc ? 'asc' : 'desc', false, false);
    } else {
      this.table.sort(event.oldValue, null, false, true);
    }
    this.isSorting = false;
  }
};

scout.TileTableHeaderBox.prototype._syncSortingGroupingFields = function() {
  var primaryGroupingColumn = scout.arrays.find(this.table.visibleColumns(), function(column) {
    return column.grouped && column.sortIndex === 0;
  });
  if (primaryGroupingColumn) {
    this.groupByField.setValue(primaryGroupingColumn);
  } else {
    this.groupByField.setValue(null);
  }

  var primarySortingColumn = scout.arrays.find(this.table.visibleColumns(), function(column) {
    return column.sortActive && column.sortIndex === 0;
  });

  if (primarySortingColumn) {
    this.sortByField.setValue(this._findSortByLookupRowForKey({
      column: primarySortingColumn,
      asc: primarySortingColumn.sortAscending
    }));
  } else {
    this.sortByField.setValue(null);
  }
};

scout.TileTableHeaderBox.prototype._onTableGroup = function(event) {
  if (!this.isGrouping) {
    this._syncSortingGroupingFields();
  }
};

scout.TileTableHeaderBox.prototype._onTableSort = function(event) {
  if (!this.isSorting) {
    this._syncSortingGroupingFields();
  }
};
