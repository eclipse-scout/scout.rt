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
scout.TileTableHeader = function() {
  scout.TileTableHeader.parent.call(this);

  this.table = null;
  this.groupBox = null;
};
scout.inherits(scout.TileTableHeader, scout.Widget);

scout.TileTableHeader.prototype._init = function(options) {
  scout.TileTableHeader.parent.prototype._init.call(this, options);

  this.groupBox = scout.create('GroupBox', {
    parent: this,
    id: 'TileTableHeaderGroupBox',
    labelVisible: false,
    statusVisible: false,
    gridColumnCount: 7,
    bodyLayoutConfig: {
      hgap: 8
    }
  });

  this.groupBox.insertField(scout.create('PlaceholderField', {
    parent: this.groupBox,
    gridDataHints: {
      w: 5
    }
  }));

  // Group By Field
  this.groupByField = scout.create('SmartField', {
    parent: this.groupBox,
    label: 'Gruppieren nach',
    labelPosition: scout.FormField.LabelPosition.ON_FIELD,
    statusVisible: false
  });
  this.groupByField.setLookupCall(scout.create('StaticLookupCall', {
    session: this.session,
    data: this._getGroupByLookupData()
  }));
  this.groupByField.on('propertyChange', this._onGroupingChange.bind(this));
  this.groupBox.insertField(this.groupByField);

  // Sort By Field
  this.sortByField = scout.create('SmartField', {
    parent: this.groupBox,
    label: 'Sortieren nach',
    labelPosition: scout.FormField.LabelPosition.ON_FIELD,
    statusVisible: false
  });
  this.sortByField.setLookupCall(scout.create('StaticLookupCall', {
    session: this.session,
    data: this._getSortByLookupData()
  }));
  this.sortByField.on('propertyChange', this._onSortingChange.bind(this));
  this.groupBox.insertField(this.sortByField);
};

scout.TileTableHeader.prototype._getGroupByLookupData = function() {
  var data = [];
  this.table.visibleColumns().forEach(function(column, index) {
    if (this.table.isGroupingPossible(column)) {
      data.push([index, column.text]);
    }
  }, this);
  return data;
};

scout.TileTableHeader.prototype._getSortByLookupData = function() {
  return this.table.visibleColumns().map(function(column, index) {
    return [index, column.text];
  });
};

scout.TileTableHeader.prototype._render = function() {
  if (this.table.menuBar.position === scout.MenuBar.Position.TOP) {
    this.$container = this.table.menuBar.$container.afterDiv('tile-table-header');
  } else {
    this.$container = this.table.$container.makeDiv('tile-table-header').prependTo(this.table.$container);
  }
  this.groupBox.render();
};

scout.TileTableHeader.prototype._onGroupingChange = function(event) {
  if (!this.table.tileMode) {
    return;
  }
  if (event.propertyName === 'value') {
    var column;
    if (event.newValue !== null) {
      column = this.table.visibleColumns()[event.newValue];
      var direction = (column.sortIndex >= 0 && !column.sortAscending) ? 'desc' : 'asc';
      this.table.groupColumn(column, false, direction, false);
    } else {
      column = this.table.visibleColumns()[event.oldValue];
      this.table.groupColumn(column, false, null, true);
    }
  }
};

scout.TileTableHeader.prototype._onSortingChange = function(event) {
  if (!this.table.tileMode) {
    return;
  }
  if (event.propertyName === 'value') {
    var column;
    if (event.newValue !== null) {
      column = this.table.visibleColumns()[event.newValue];
      this.table.sort(column, 'asc', false, false);
    } else {
      column = this.table.visibleColumns()[event.oldValue];
      this.table.sort(column, null, false, true);
    }
  }
};
