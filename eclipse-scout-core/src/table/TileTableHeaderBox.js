/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, FormField, GroupBox, scout, SmartField, ValueField} from '../index';

export default class TileTableHeaderBox extends GroupBox {

  constructor() {
    super();

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
  }

  _installListeners() {
    this.table.on('group', this._tableGroupHandler);
    this.table.on('sort', this._tableSortHandler);
    this.table.one('destroy', this._destroyHandler);
  }

  _uninstallListeners() {
    this.table.off('group', this._tableGroupHandler);
    this.table.off('sort', this._tableSortHandler);
  }

  _init(model) {
    super._init(model);

    this.table = this.parent;
    this._installListeners();

    this.insertField(scout.create('PlaceholderField', {
      id: 'PlaceholderField',
      parent: this,
      gridDataHints: {
        w: 5
      }
    }));

    // Group By Field
    this.groupByField = scout.create('SmartField', {
      id: 'GroupByField',
      parent: this,
      label: this.session.text('GroupBy'),
      labelPosition: FormField.LabelPosition.ON_FIELD,
      clearable: ValueField.Clearable.ALWAYS,
      statusVisible: false,
      displayStyle: SmartField.DisplayStyle.DROPDOWN
    });
    this.groupByField.setLookupCall(this._createGroupByLookupCall());
    this.groupByField.setVisible(!arrays.empty(this.groupByField.lookupCall.data));
    this.groupByField.on('propertyChange', this._onGroupingChange.bind(this));

    this.insertField(this.groupByField);

    // Sort By Field
    this.sortByField = scout.create('SmartField', {
      id: 'SortByField',
      parent: this,
      label: this.session.text('SortBy'),
      labelPosition: FormField.LabelPosition.ON_FIELD,
      clearable: ValueField.Clearable.ALWAYS,
      statusVisible: false,
      displayStyle: SmartField.DisplayStyle.DROPDOWN
    });
    this.sortByField.setLookupCall(this._createSortByLookupCall());
    this.sortByField.setVisible(!arrays.empty(this.sortByField.lookupCall.data));
    this.sortByField.on('propertyChange', this._onSortingChange.bind(this));

    this.insertField(this.sortByField);

    // it's okay to sync the fields here, _onGroupingChange/_onSortingChange will return early since the tileMode property is not set yet at this point
    this._syncSortingGroupingFields();
  }

  _findSortByLookupRowForKey(key) {
    return this.sortByField.lookupCall.data.map(lookupRow => {
      return lookupRow[0];
    }).find(rowKey => {
      return rowKey.column === key.column && rowKey.asc === key.asc;
    }, this);
  }

  _createGroupByLookupCall() {
    return scout.create('TileTableHeaderGroupByLookupCall', {
      session: this.session,
      table: this.table
    });
  }

  _createSortByLookupCall() {
    return scout.create('TileTableHeaderSortByLookupCall', {
      session: this.session,
      table: this.table
    });
  }

  _onGroupingChange(event) {
    if (!this.table.tileMode) {
      return;
    }
    if (event.propertyName === 'value') {
      this.isGrouping = true;
      let column;
      if (event.newValue !== null) {
        column = event.newValue;
        let direction = (column.sortIndex >= 0 && !column.sortAscending) ? 'desc' : 'asc';
        this.table.groupColumn(column, false, direction, false);
      } else {
        column = event.oldValue;
        this.table.groupColumn(column, false, null, true);
      }
      this.isGrouping = false;
    }
  }

  _onSortingChange(event) {
    if (!this.table.tileMode) {
      return;
    }
    if (event.propertyName === 'value') {
      this.isSorting = true;
      let column, sortInfo;
      if (event.newValue !== null) {
        sortInfo = event.newValue.column;
        column = event.newValue.column;
        this.table.sort(event.newValue.column, event.newValue.asc ? 'asc' : 'desc', false, false);
      } else {
        this.table.sort(event.oldValue, null, false, true);
      }
      this.isSorting = false;
    }
  }

  _syncSortingGroupingFields() {
    let primaryGroupingColumn = arrays.find(this.table.visibleColumns(), column => {
      return column.grouped && column.sortIndex === 0;
    });
    if (primaryGroupingColumn) {
      this.groupByField.setValue(primaryGroupingColumn);
    } else {
      this.groupByField.setValue(null);
    }

    let primarySortingColumn = arrays.find(this.table.visibleColumns(), column => {
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
  }

  _onTableGroup(event) {
    if (!this.isGrouping) {
      this._syncSortingGroupingFields();
    }
  }

  _onTableSort(event) {
    if (!this.isSorting) {
      this._syncSortingGroupingFields();
    }
  }
}
