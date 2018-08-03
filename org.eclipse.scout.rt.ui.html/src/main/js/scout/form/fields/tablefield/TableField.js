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
scout.TableField = function() {
  scout.TableField.parent.call(this);

  this.gridDataHints.weightY = 1.0;
  this.gridDataHints.h = 3;
  this.eventDelegator = null;
  this._tableChangedHandler = this._onTableChanged.bind(this);
  this._deletedRows = scout.objects.createMap();
  this._insertedRows = scout.objects.createMap();
  this._updatedRows = scout.objects.createMap();
  this._checkedRows = scout.objects.createMap();
  this._addWidgetProperties(['table']);
};
scout.inherits(scout.TableField, scout.FormField);

scout.TableField.TABLE_CHANGE_EVENTS = 'rowsInserted rowsDeleted allRowsDeleted rowsUpdated rowsChecked';

scout.TableField.prototype._init = function(model) {
  scout.TableField.parent.prototype._init.call(this, model);

  this._setTable(this.table);
};

scout.TableField.prototype._render = function() {
  this.addContainer(this.$parent, 'table-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addStatus();
  this._renderTable();
};

scout.TableField.prototype.setTable = function(table) {
  this.setProperty('table', table);
};

scout.TableField.prototype._setTable = function(table) {
  if (this.table) {
    this.table.off(scout.TableField.TABLE_CHANGE_EVENTS, this._tableChangedHandler);
    if (this.eventDelegator) {
      this.eventDelegator.destroy();
      this.eventDelegator = null;
    }
  }
  this._setProperty('table', table);
  if (table) {
    table.on(scout.TableField.TABLE_CHANGE_EVENTS, this._tableChangedHandler);
    this.eventDelegator = scout.EventDelegator.create(this, table, {
      delegateProperties: ['enabled', 'disabledStyle', 'loading']
    });
    table.setEnabled(this.enabled);
    table.setDisabledStyle(this.disabledStyle);
    table.setLoading(this.loading);
    table.setScrollTop(this.scrollTop);
  }
};

scout.TableField.prototype.setTable = function(table) {
  this.setProperty('table', table);
};

scout.TableField.prototype._renderTable = function() {
  if (!this.table) {
    return;
  }
  this.table.render();
  this.addField(this.table.$container);
  this.$field.addDeviceClass();
  this.invalidateLayoutTree();
};

scout.TableField.prototype._removeTable = function() {
  if (!this.table) {
    return;
  }
  this.table.remove();
  this._removeField();
  this.invalidateLayoutTree();
};

scout.TableField.prototype.computeRequiresSave = function() {
  return Object.keys(this._deletedRows).length > 0 ||
      Object.keys(this._insertedRows).length > 0 ||
      Object.keys(this._updatedRows).length > 0 ||
      Object.keys(this._checkedRows).length > 0;
};

scout.TableField.prototype._onTableChanged = function(event) {
  if (scout.isOneOf(event.type, 'rowsDeleted', 'allRowsDeleted')) {
    this._updateDeletedRows(event.rows);
  } else if (event.type === 'rowsInserted') {
    this._updateInsertedRows(event.rows);
  } else if (event.type === 'rowsUpdated') {
    this._updateUpdatedRows(event.rows);
  } else if (event.type === 'rowsChecked') {
    this._updateCheckedRows(event.rows);
  }
};

scout.TableField.prototype._updateDeletedRows = function (rows) {
  rows.forEach(function(row) {
    if (row.id in this._insertedRows) {
      // If a row is contained in _insertedRows an inserted row has been deleted again.
      // In that case we can remove that row from the maps and don't have to add it to deletedRows as well.
      delete this._insertedRows[row.id];
      delete this._updatedRows[row.id];
      delete this._checkedRows[row.id];
      return;
    }
    this._deletedRows[row.id] = row;
  }, this);
};

scout.TableField.prototype._updateInsertedRows = function (rows) {
  rows.forEach(function(row) {
    this._insertedRows[row.id] = row;
  }, this);
};

scout.TableField.prototype._updateUpdatedRows = function (rows) {
  rows.forEach(function(row) {
    this._updatedRows[row.id] = row;
  }, this);
};

/**
 * If a row already exists in the _checkedRows array, remove it (row was checked/unchecked again, which
 * means it is no longer changed). Add it to the array otherwise.
 */
scout.TableField.prototype._updateCheckedRows = function (rows) {
  rows.forEach(function(row) {
    if (row.id in this._checkedRows) {
      delete this._checkedRows[row.id];
    } else {
      this._checkedRows[row.id] = row;
    }
  }, this);
};

scout.TableField.prototype.markAsSaved = function() {
  scout.TableField.parent.prototype.markAsSaved.call(this);
  this._deletedRows = scout.objects.createMap();
  this._insertedRows = scout.objects.createMap();
  this._updatedRows = scout.objects.createMap();
  this._checkedRows = scout.objects.createMap();
};

scout.TableField.prototype.validate = function() {
  var desc = scout.TableField.parent.prototype.validate.call(this);
  if (desc && !desc.valid) {
    return desc;
  }

  var validByErrorStatus = !this.errorStatus;
  var validByMandatory = !this.mandatory || !this.empty;

  // check cells
  var rows = scout.arrays.ensure(this.table.rows);
  var columns = scout.arrays.ensure(this.table.columns);

  rows.some(function(row) {
    return columns.some(function(column) {
      var ret = column.isContentValid(row);
      if (!ret.valid) {
        validByErrorStatus = validByErrorStatus && ret.validByErrorStatus;
        validByMandatory = validByMandatory && ret.validByMandatory;
        return !(validByErrorStatus || validByMandatory);
      }
    }, this);
  }, this);

  return {
    valid: validByErrorStatus && validByMandatory,
    validByErrorStatus: validByErrorStatus,
    validByMandatory: validByMandatory
  };
};

/**
 * @override
 */
scout.TableField.prototype.getDelegateScrollable = function() {
  return this.table;
};
