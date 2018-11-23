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
scout.ListBox = function() {
  scout.ListBox.parent.call(this);

  this.table = null;
  this.lookupStatus = null;
  this.clearable = scout.ValueField.Clearable.NEVER;

  this._addWidgetProperties(['table', 'filterBox']);
};
scout.inherits(scout.ListBox, scout.LookupBox);

scout.ListBox.prototype._init = function(model) {
  scout.ListBox.parent.prototype._init.call(this, model);
  this.table.on('rowsChecked', this._onTableRowsChecked.bind(this));
  this.table.setScrollTop(this.scrollTop);
};

scout.ListBox.prototype._initStructure = function(value) {
  if (!this.table) {
    this.table = this._createDefaultListBoxTable();
  }
};

scout.ListBox.prototype._render = function() {
  scout.ListBox.parent.prototype._render.call(this);
  this.$container.addClass('list-box');
};

scout.ListBox.prototype._createLayout = function() {
  return new scout.ListBoxLayout(this, this.table, this.filterBox);
};

scout.ListBox.prototype._renderStructure = function() {
  this.table.render(this.$fieldContainer);
  this.addField(this.table.$container);
};

scout.ListBox.prototype._onTableRowsChecked = function(event) {
  this._syncTableToValue();
};

scout.ListBox.prototype._syncTableToValue = function() {
  if (!this.lookupCall || this._valueSyncing) {
    return;
  }
  this._valueSyncing = true;
  var valueArray = [];
  this.table.rows.forEach(function(row) {
    if (row.checked) {
      valueArray.push(row.lookupRow.key);
    }
  }, this);

  this.setValue(valueArray);
  this._valueSyncing = false;
};

scout.ListBox.prototype._valueChanged = function() {
  scout.ListBox.parent.prototype._valueChanged.call(this);
  this._syncValueToTable(this.value);
};

scout.ListBox.prototype._syncValueToTable = function(newValue) {
  if (!this.lookupCall || this._valueSyncing) {
    return;
  }

  this._valueSyncing = true;
  if (scout.arrays.empty(newValue)) {
    this.table.uncheckRows(this.table.rows);
  } else {
    // if table is empty and lookup was not executed yet. do it now.
    if (scout.arrays.empty(this.table.rows)) {
      this._valueSyncing = false;
      this._lookupByAll();
      return;
    }

    var rowsToCheck = [];

    this.table.uncheckRows(this.table.rows);
    this.table.rows.forEach(function(row) {
      if (scout.arrays.containsAny(newValue, row.lookupRow.key)) {
        rowsToCheck.push(row);
      }
    }, this);
    this.table.checkRows(rowsToCheck);
  }

  this._updateDisplayText();
  this._valueSyncing = false;
};

scout.ListBox.prototype._lookupByAllDone = function(result) {
  if (scout.ListBox.parent.prototype._lookupByAllDone.call(this, result)) {
    this._populateTable(result);
  }
};

scout.ListBox.prototype._populateTable = function(result) {
  var
    tableRows = [],
    lookupRows = result.lookupRows;

  lookupRows.forEach(function(lookupRow) {
    tableRows.push(this._createTableRow(lookupRow));
  }, this);

  this.table.deleteAllRows();
  this.table.insertRows(tableRows);

  this._syncValueToTable(this.value);
};

/**
 * Returns a lookup row for each value currently checked.
 */
scout.ListBox.prototype.getCheckedLookupRows = function() {
  if (this.value === null || scout.arrays.empty(this.value) || this.table.rows.length === 0) {
    return [];
  }

  return this.table.rows.filter(function(row) {
    return row.checked;
  }).map(function(row) {
    return row.lookupRow;
  });
};

scout.ListBox.prototype._createTableRow = function(lookupRow) {
  var
    cell = scout.create('Cell', {
      text: lookupRow.text
    }),
    cells = [cell],
    row = {
      cells: cells,
      lookupRow: lookupRow
    };

  if (lookupRow.iconId) {
    cell.iconId = lookupRow.iconId;
  }
  if (lookupRow.tooltipText) {
    cell.tooltipText = lookupRow.tooltipText;
  }
  if (lookupRow.backgroundColor) {
    cell.backgroundColor = lookupRow.backgroundColor;
  }
  if (lookupRow.foregroundColor) {
    cell.foregroundColor = lookupRow.foregroundColor;
  }
  if (lookupRow.font) {
    cell.font = lookupRow.font;
  }
  if (lookupRow.enabled === false) {
    row.enabled = false;
  }
  if (lookupRow.active === false) {
    row.active = false;
  }
  if (lookupRow.cssClass) {
    row.cssClass = lookupRow.cssClass;
  }

  return row;
};

scout.ListBox.prototype._createDefaultListBoxTable = function() {
  return scout.create('Table', {
    parent: this,
    autoResizeColumns: true,
    checkable: true,
    headerVisible: false,
    footerVisible: false,
    columns: [{
      objectType: "Column"
    }]
  });
};

/**
 * @override
 */
scout.ListBox.prototype.getDelegateScrollable = function() {
  return this.table;
};
