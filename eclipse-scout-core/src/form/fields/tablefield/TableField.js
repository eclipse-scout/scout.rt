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
import {arrays, EventDelegator, FormField, objects, scout, TableRow} from '../../../index';

export default class TableField extends FormField {

  constructor() {
    super();

    this.gridDataHints.weightY = 1.0;
    this.gridDataHints.h = 3;
    this.eventDelegator = null;
    this._tableChangedHandler = this._onTableChanged.bind(this);
    this._deletedRows = objects.createMap();
    this._insertedRows = objects.createMap();
    this._updatedRows = objects.createMap();
    this._checkedRows = objects.createMap();
    this._addWidgetProperties(['table']);
  }

  static TABLE_CHANGE_EVENTS = 'rowsInserted rowsDeleted allRowsDeleted rowsUpdated rowsChecked';

  _init(model) {
    super._init(model);

    this._setTable(this.table);
  }

  _render() {
    this.addContainer(this.$parent, 'table-field');
    this.addLabel();
    this.addMandatoryIndicator();
    this.addStatus();
    this._renderTable();
  }

  setTable(table) {
    this.setProperty('table', table);
  }

  _setTable(table) {
    if (this.table) {
      this.table.off(TableField.TABLE_CHANGE_EVENTS, this._tableChangedHandler);
      if (this.eventDelegator) {
        this.eventDelegator.destroy();
        this.eventDelegator = null;
      }
    }
    this._setProperty('table', table);
    if (table) {
      table.on(TableField.TABLE_CHANGE_EVENTS, this._tableChangedHandler);
      this.eventDelegator = EventDelegator.create(this, table, {
        delegateProperties: ['enabled', 'disabledStyle', 'loading']
      });
      table.setDisabledStyle(this.disabledStyle);
      table.setLoading(this.loading);
      table.setScrollTop(this.scrollTop);
    }
  }

  _renderTable() {
    if (!this.table) {
      return;
    }
    this.table.render();
    this.addField(this.table.$container);
    this.$field.addDeviceClass();
    this.invalidateLayoutTree();
  }

  _removeTable() {
    if (!this.table) {
      return;
    }
    this.table.remove();
    this._removeField();
    this.invalidateLayoutTree();
  }

  computeRequiresSave() {
    return Object.keys(this._deletedRows).length > 0 ||
      Object.keys(this._insertedRows).length > 0 ||
      Object.keys(this._updatedRows).length > 0 ||
      Object.keys(this._checkedRows).length > 0;
  }

  _onTableChanged(event) {
    if (scout.isOneOf(event.type, 'rowsDeleted', 'allRowsDeleted')) {
      this._updateDeletedRows(event.rows);
    } else if (event.type === 'rowsInserted') {
      this._updateInsertedRows(event.rows);
    } else if (event.type === 'rowsUpdated') {
      this._updateUpdatedRows(event.rows);
    } else if (event.type === 'rowsChecked') {
      this._updateCheckedRows(event.rows);
    }
  }

  _updateDeletedRows(rows) {
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
  }

  _updateInsertedRows(rows) {
    rows.forEach(function(row) {
      this._insertedRows[row.id] = row;
    }, this);
  }

  _updateUpdatedRows(rows) {
    rows.forEach(function(row) {
      if (row.status === TableRow.Status.NON_CHANGED) {
        return;
      }
      this._updatedRows[row.id] = row;
    }, this);
  }

  /**
   * If a row already exists in the _checkedRows array, remove it (row was checked/unchecked again, which
   * means it is no longer changed). Add it to the array otherwise.
   */
  _updateCheckedRows(rows) {
    rows.forEach(function(row) {
      if (row.id in this._checkedRows) {
        delete this._checkedRows[row.id];
      } else {
        this._checkedRows[row.id] = row;
      }
    }, this);
  }

  markAsSaved() {
    super.markAsSaved();
    this._deletedRows = objects.createMap();
    this._insertedRows = objects.createMap();
    this._updatedRows = objects.createMap();
    this._checkedRows = objects.createMap();
    this.table.markRowsAsNonChanged();
  }

  getValidationResult() {
    let desc = super.getValidationResult();
    if (desc && !desc.valid) {
      return desc;
    }

    let validByErrorStatus = !this.errorStatus;
    let validByMandatory = !this.mandatory || !this.empty;

    // check cells
    let rows = arrays.ensure(this.table.rows);
    let columns = arrays.ensure(this.table.columns);
    let reveal = () => {
    };
    let label = this.label || '';

    rows.some(function(row) {
      return columns.some(column => {
        let ret = column.isContentValid(row);
        if (!ret.valid) {
          reveal = () => {
            desc.reveal();
            this.table.focusCell(column, row);
          };
          if (label) {
            label += ': ';
          }
          label += column.text;
          validByErrorStatus = validByErrorStatus && ret.validByErrorStatus;
          validByMandatory = validByMandatory && ret.validByMandatory;
          return !(validByErrorStatus || validByMandatory);
        }
        return false;
      }, this);
    }, this);

    return {
      valid: validByErrorStatus && validByMandatory,
      validByErrorStatus: validByErrorStatus,
      validByMandatory: validByMandatory,
      field: this,
      label: label,
      reveal: reveal
    };
  }

  /**
   * @override
   */
  getDelegateScrollable() {
    return this.table;
  }
}
