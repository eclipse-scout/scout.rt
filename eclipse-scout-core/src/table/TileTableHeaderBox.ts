/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, Column, EventHandler, FormField, GroupBox, InitModelOf, LogicalGridLayoutConfig, LookupRow, PlaceholderField, PropertyChangeEvent, scout, SmartField, StaticLookupCall, Table, TableGroupEvent, TableSortEvent,
  TileTableHeaderBoxModel, TileTableHeaderGroupByLookupCall, TileTableHeaderSortByLookupCall, TileTableHeaderSortKey, ValueField
} from '../index';

export class TileTableHeaderBox extends GroupBox implements TileTableHeaderBoxModel {
  declare model: TileTableHeaderBoxModel;
  declare parent: Table;

  table: Table;
  groupByField: SmartField<Column<any>>;
  sortByField: SmartField<TileTableHeaderSortKey>;
  isGrouping: boolean;
  isSorting: boolean;
  protected _tableGroupHandler: EventHandler<TableGroupEvent>;
  protected _tableSortHandler: EventHandler<TableSortEvent>;
  protected _destroyHandler: () => void;

  constructor() {
    super();

    this.table = null;
    this.labelVisible = false;
    this.statusVisible = false;
    this.gridColumnCount = 7;
    this.bodyLayoutConfig = LogicalGridLayoutConfig.ensure({
      hgap: 8
    });

    this.groupByField = null;
    this.sortByField = null;

    this._tableGroupHandler = this._onTableGroup.bind(this);
    this._tableSortHandler = this._onTableSort.bind(this);
    this._destroyHandler = this._uninstallListeners.bind(this);
  }

  protected _installListeners() {
    this.table.on('group', this._tableGroupHandler);
    this.table.on('sort', this._tableSortHandler);
    this.table.one('destroy', this._destroyHandler);
  }

  protected _uninstallListeners() {
    this.table.off('group', this._tableGroupHandler);
    this.table.off('sort', this._tableSortHandler);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.table = this.parent;
    this._installListeners();

    this.insertField(scout.create(PlaceholderField, {
      id: 'PlaceholderField',
      parent: this,
      gridDataHints: {
        w: 5
      }
    }));

    // Group By Field
    this.groupByField = scout.create(SmartField, {
      id: 'GroupByField',
      parent: this,
      label: this.session.text('GroupBy'),
      labelPosition: FormField.LabelPosition.ON_FIELD,
      clearable: ValueField.Clearable.ALWAYS,
      statusVisible: false,
      displayStyle: SmartField.DisplayStyle.DROPDOWN
    }) as SmartField<Column<any>>;
    this.groupByField.setLookupCall(this._createGroupByLookupCall());
    this.groupByField.setVisible(!arrays.empty((this.groupByField.lookupCall as StaticLookupCall<Column<any>>).data));
    this.groupByField.on('propertyChange', this._onGroupingChange.bind(this));

    this.insertField(this.groupByField);

    // Sort By Field
    this.sortByField = scout.create(SmartField, {
      id: 'SortByField',
      parent: this,
      label: this.session.text('SortBy'),
      labelPosition: FormField.LabelPosition.ON_FIELD,
      clearable: ValueField.Clearable.ALWAYS,
      statusVisible: false,
      displayStyle: SmartField.DisplayStyle.DROPDOWN
    }) as SmartField<TileTableHeaderSortKey>;
    this.sortByField.setLookupCall(this._createSortByLookupCall());
    this.sortByField.setVisible(!arrays.empty((this.sortByField.lookupCall as StaticLookupCall<TileTableHeaderSortKey>).data));
    this.sortByField.on('propertyChange', this._onSortingChange.bind(this));

    this.insertField(this.sortByField);

    // it's okay to sync the fields here, _onGroupingChange/_onSortingChange will return early since the tileMode property is not set yet at this point
    this._syncSortingGroupingFields();
  }

  protected _findSortByLookupRowForKey(key: TileTableHeaderSortKey): TileTableHeaderSortKey {
    let lookupCall = this.sortByField.lookupCall as StaticLookupCall<TileTableHeaderSortKey>;
    return lookupCall.data
      .map((lookupRow: LookupRow<TileTableHeaderSortKey>) => lookupRow[0] as TileTableHeaderSortKey)
      .find(rowKey => rowKey.column === key.column && rowKey.asc === key.asc);
  }

  protected _createGroupByLookupCall(): StaticLookupCall<Column<any>> {
    return scout.create(TileTableHeaderGroupByLookupCall, {
      session: this.session,
      table: this.table
    });
  }

  protected _createSortByLookupCall(): StaticLookupCall<TileTableHeaderSortKey> {
    return scout.create(TileTableHeaderSortByLookupCall, {
      session: this.session,
      table: this.table
    });
  }

  protected _onGroupingChange(event: PropertyChangeEvent<any, SmartField<Column<any>>>) {
    if (!this.table.tileMode) {
      return;
    }
    if (event.propertyName === 'value') {
      this.isGrouping = true;
      if (event.newValue !== null) {
        let column = event.newValue as Column<any>;
        let direction: 'asc' | 'desc' = (column.sortIndex >= 0 && !column.sortAscending) ? 'desc' : 'asc';
        this.table.groupColumn(column, false, direction, false);
      } else {
        let column = event.oldValue as Column<any>;
        this.table.groupColumn(column, false, null, true);
      }
      this.isGrouping = false;
    }
  }

  protected _onSortingChange(event: PropertyChangeEvent<any, SmartField<TileTableHeaderSortKey>>) {
    if (!this.table.tileMode) {
      return;
    }
    if (event.propertyName === 'value') {
      this.isSorting = true;
      if (event.newValue !== null) {
        let newValue = event.newValue as TileTableHeaderSortKey;
        this.table.sort(newValue.column, newValue.asc ? 'asc' : 'desc', false, false);
      } else {
        let oldValue = event.oldValue as TileTableHeaderSortKey;
        this.table.sort(oldValue.column, null, false, true);
      }
      this.isSorting = false;
    }
  }

  protected _syncSortingGroupingFields() {
    let primaryGroupingColumn = arrays.find(this.table.visibleColumns(), column => column.grouped && column.sortIndex === 0);
    if (primaryGroupingColumn) {
      this.groupByField.setValue(primaryGroupingColumn);
    } else {
      this.groupByField.setValue(null);
    }

    let primarySortingColumn = arrays.find(this.table.visibleColumns(), column => column.sortActive && column.sortIndex === 0);
    if (primarySortingColumn) {
      this.sortByField.setValue(this._findSortByLookupRowForKey({
        column: primarySortingColumn,
        asc: primarySortingColumn.sortAscending
      }));
    } else {
      this.sortByField.setValue(null);
    }
  }

  protected _onTableGroup(event: TableGroupEvent) {
    if (!this.isGrouping) {
      this._syncSortingGroupingFields();
    }
  }

  protected _onTableSort(event: TableSortEvent) {
    if (!this.isSorting) {
      this._syncSortingGroupingFields();
    }
  }
}
