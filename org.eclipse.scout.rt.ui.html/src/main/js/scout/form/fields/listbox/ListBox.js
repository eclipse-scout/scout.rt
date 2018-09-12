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
scout.ListBox = function() {
  scout.ListBox.parent.call(this);

  this.gridDataHints.weightY = 1.0;
  this.gridDataHints.h = 2;
  this.lookupCall = null;
  this.table = null;
  this.lookupStatus = null;
  this.value = [];
  this.clearable = scout.ValueField.Clearable.NEVER;

  this._pendingLookup = null;
  this._currentLookupCall = null;
  this._pendingLookup = null;
  this._valueSyncing = false; // true when value is either syncing to table or table to value

  this._addWidgetProperties(['table', 'filterBox']);
  this._addCloneProperties(['lookupCall']);
};
scout.inherits(scout.ListBox, scout.ValueField);

scout.ListBox.ErrorCode = {
  NO_DATA: 1
};

scout.ListBox.prototype._init = function(model) {
  scout.ListBox.parent.prototype._init.call(this, model);
  if (this.filterBox) {
    this.filterBox.enabledComputed = true; // filter is always enabled
    this.filterBox.recomputeEnabled(true);
  }
  this.table.on('rowsChecked', this._onTableRowsChecked.bind(this));
  this.table.setScrollTop(this.scrollTop);
};

scout.ListBox.prototype._initValue = function(value) {
  if (this.lookupCall) {
    this._setLookupCall(this.lookupCall);
  }
  if (!this.table) {
    this.table = this._createDefaultListBoxTable();
  }
  scout.ListBox.parent.prototype._initValue.call(this, value);
};

scout.ListBox.prototype._render = function() {
  this.addContainer(this.$parent, 'list-box');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addStatus();

  this.addFieldContainer(this.$parent.makeDiv());
  var htmlComp = scout.HtmlComponent.install(this.$fieldContainer, this.session);
  htmlComp.setLayout(new scout.ListBoxLayout(this, this.table, this.filterBox));

  this._renderTable();
  if (this.filterBox) {
    this._renderFilterBox();
    this.table.htmlComp.pixelBasedSizing = true;
  }
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

scout.ListBox.prototype._ensureValue = function(value) {
  return scout.arrays.ensure(value);
};

scout.ListBox.prototype._updateEmpty = function() {
  this.empty = scout.arrays.empty(this.value);
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

scout.ListBox.prototype._lookupByAll = function() {
  if (!this.lookupCall) {
    return;
  }
  this._clearPendingLookup();

  var deferred = $.Deferred();
  var doneHandler = function(result) {
    this._lookupByAllDone(result);
    deferred.resolve(result);
  }.bind(this);

  this._executeLookup(this.lookupCall.cloneForAll(), true)
    .done(doneHandler);

  return deferred.promise();
};

scout.ListBox.prototype._clearPendingLookup = function() {
  if (this._pendingLookup) {
    clearTimeout(this._pendingLookup);
    this._pendingLookup = null;
  }
};

scout.ListBox.prototype._executeLookup = function(lookupCall, abortExisting) {
  this.setLoading(true);

  if (abortExisting && this._currentLookupCall) {
    this._currentLookupCall.abort();
  }
  this._currentLookupCall = lookupCall;
  this.trigger('prepareLookupCall', {
    lookupCall: lookupCall
  });

  return lookupCall
    .execute()
    .always(function() {
      this._currentLookupCall = null;
      this.setLoading(false);
      this._clearLookupStatus();
    }.bind(this));
};

scout.ListBox.prototype._lookupByAllDone = function(result) {
  // Oops! Something went wrong while the lookup has been processed.
  if (result.exception) {
    this.setErrorStatus(scout.Status.error({
      message: result.exception
    }));
    return;
  }

  // 'No data' case
  if (result.lookupRows.length === 0) {
    this.setLookupStatus(scout.Status.warning({
      message: this.session.text('SmartFieldNoDataFound'),
      code: scout.ListBox.ErrorCode.NO_DATA
    }));
    return;
  }

  this._populateTable(result);
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

  var lookupRows = [];
  this.table.rows.forEach(function(row) {
    if (row.checked) {
      lookupRows.push(row.lookupRow);
    }
  }, this);

  return lookupRows;
};

scout.ListBox.prototype._clearLookupStatus = function() {
  this.setLookupStatus(null);
};

scout.ListBox.prototype._errorStatus = function() {
  return this.lookupStatus || this.errorStatus;
};

scout.ListBox.prototype.setLookupStatus = function(lookupStatus) {
  this.setProperty('lookupStatus', lookupStatus);
  if (this.rendered) {
    this._renderErrorStatus();
  }
};

scout.ListBox.prototype.clearErrorStatus = function() {
  this.setErrorStatus(null);
  this._clearLookupStatus();
};

scout.ListBox.prototype._clearLookupStatus = function() {
  this.setLookupStatus(null);
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

scout.ListBox.prototype.setLookupCall = function(lookupCall) {
  this.setProperty('lookupCall', lookupCall);
};

scout.ListBox.prototype._setLookupCall = function(lookupCall) {
  this._setProperty('lookupCall', scout.LookupCall.ensure(lookupCall, this.session));
  this._lookupByAll();
};

scout.ListBox.prototype._renderTable = function() {
  this.table.render(this.$fieldContainer);
  this.addField(this.table.$container);
  this.$field.addDeviceClass();
};

scout.ListBox.prototype._renderFilterBox = function() {
  this.filterBox.render(this.$fieldContainer);
};

scout.ListBox.prototype._formatValue = function(value) {
  if (scout.objects.isNullOrUndefined(value)) {
    return '';
  }

  return this._formatLookupRows(this.getCheckedLookupRows());
};

scout.ListBox.prototype._formatLookupRows = function(lookupRows) {
  lookupRows = scout.arrays.ensure(lookupRows);
  if (lookupRows.length === 0) {
    return '';
  }

  var formatted = [];
  lookupRows.forEach(function(row) {
    formatted.push(row.text);
  });
  return scout.strings.join(', ', formatted);
};

scout.ListBox.prototype._readDisplayText = function() {
  return this.displayText;
};

scout.ListBox.prototype._clear = function() {
  this.setValue(null);
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
