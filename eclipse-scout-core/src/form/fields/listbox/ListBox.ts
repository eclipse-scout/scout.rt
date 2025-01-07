/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, Column, InitModelOf, ListBoxLayout, ListBoxModel, ListBoxTableAccessibilityRenderer, LookupBox, lookupField, LookupResult, LookupRow, ObjectOrChildModel, objects, scout, Table, TableModel, TableRow, TableRowModel,
  TableRowsCheckedEvent, Widget
} from '../../../index';
import $ from 'jquery';

export class ListBox<TLookup, TValue = TLookup[]> extends LookupBox<TLookup, TValue> {
  declare model: ListBoxModel<TLookup, TValue>;

  table: Table;
  tableLinesVisible: boolean;

  constructor() {
    super();

    this.table = null;
    this.tableLinesVisible = false;

    this._addWidgetProperties(['table', 'filterBox']);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.table.on('rowsChecked', this._onTableRowsChecked.bind(this));
    this.table.setScrollTop(this.scrollTop);
  }

  protected _initStructure(value: TValue) {
    if (!this.table) {
      this.table = this._createDefaultListBoxTable();
    }
    this.table.accessibilityRenderer = new ListBoxTableAccessibilityRenderer();
    // align checkableColumn in table with checkboxes of tree fields
    if (this.table.checkableColumn) { // may be null if a non-default list-box-table with checkable=false is used
      this.table.checkableColumn.minWidth = 28;
      this.table.checkableColumn.width = this.table.checkableColumn.minWidth; // do not use setWidth here
    }
  }

  protected override _render() {
    super._render();
    this.$container.addClass('list-box');
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderTableLinesVisible();
  }

  protected _createFieldContainerLayout(): ListBoxLayout {
    return new ListBoxLayout(this, this.table, this.filterBox);
  }

  protected _renderStructure() {
    this.table.render(this.$fieldContainer);
    this.addField(this.table.$container);
  }

  setTableLinesVisible(hideTableLines: boolean) {
    this.setProperty('tableLinesVisible', hideTableLines);
  }

  protected _renderTableLinesVisible() {
    this.$container.toggleClass('no-table-lines', !this.tableLinesVisible);
  }

  protected _onTableRowsChecked(event: TableRowsCheckedEvent) {
    this._syncTableToValue();
  }

  protected _syncTableToValue() {
    if (!this.lookupCall || this._valueSyncing) {
      return;
    }
    this._valueSyncing = true;
    this.setValue(this._tableToValue());
    this._valueSyncing = false;
  }

  protected override _valueChanged() {
    super._valueChanged();
    this._syncValueToTable(this.value);
  }

  protected _resetTable() {
    this.table.uncheckRows(this.table.rows, {checkOnlyEnabled: false});
  }

  protected _syncValueToTable(newValue: TValue) {
    if (!this.lookupCall || this._valueSyncing || !this.initialized) {
      return;
    }

    this._valueSyncing = true;
    try {
      if (this._isValueEmpty(newValue)) {
        this._resetTable();
      } else {
        // if lookup was not executed yet: do it now.
        let lookupScheduled = this._ensureLookupCallExecuted();
        if (lookupScheduled) {
          return; // was the first lookup: table has no rows yet. cancel sync. Will be executed again after lookup execution.
        }

        this._valueToTable(newValue);
      }

      this._updateDisplayText();
    } finally {
      this._valueSyncing = false;
    }
  }

  protected override _lookupByAllDone(result: LookupResult<TLookup>) {
    super._lookupByAllDone(result);
    this._populateTable(result);
  }

  protected _populateTable(result: LookupResult<TLookup>) {
    const tableRows = result.lookupRows.map(lookupRow => this._createTableRow(lookupRow));

    this.table.deleteAllRows();
    this.table.insertRows(tableRows);

    this._syncValueToTable(this.value);
  }

  /**
   * Returns a lookup row for each value currently checked.
   */
  getCheckedLookupRows(): LookupRow<TLookup>[] {
    if (this.value === null || arrays.empty(this.value) || this.table.rows.length === 0) {
      return [];
    }

    return this.table.rows
      .filter(row => row.checked)
      .map(row => row.lookupRow);
  }

  protected _createTableRow(lookupRow: LookupRow<TLookup>): TableRowModel {
    let cell = lookupField.createTableCell(lookupRow);
    let row: TableRowModel = {
      cells: [cell],
      lookupRow: lookupRow
    };
    if (lookupRow.enabled === false) {
      row.enabled = false;
    }
    if (lookupRow.cssClass) {
      row.cssClass = lookupRow.cssClass;
    }
    if (lookupRow.active === false) {
      row.active = false;
      row.cssClass = (row.cssClass ? (row.cssClass + ' ') : '') + 'inactive';
    }

    return row;
  }

  protected override _prepareWidgetProperty(propertyName: string, models: ObjectOrChildModel<Widget>): Widget;
  protected override _prepareWidgetProperty(propertyName: string, models: ObjectOrChildModel<Widget>[]): Widget[];
  protected override _prepareWidgetProperty(propertyName: string, models: ObjectOrChildModel<Widget> | ObjectOrChildModel<Widget>[]): Widget | Widget[] {
    if (propertyName === 'table' && objects.isPojo(models)) {
      // Enhance given model with list box specific defaults
      models = $.extend(this._createDefaultListBoxTableModel(), models);
    }
    return super._prepareWidgetProperty(propertyName, models as ObjectOrChildModel<Widget>);
  }

  protected _createDefaultListBoxTable(): Table {
    return scout.create(Table, {
      parent: this,
      ...this._createDefaultListBoxTableModel()
    });
  }

  protected _createDefaultListBoxTableModel(): TableModel {
    return {
      autoResizeColumns: true,
      checkable: true,
      checkableStyle: Table.CheckableStyle.CHECKBOX_TABLE_ROW,
      headerVisible: false,
      footerVisible: false,
      columns: [{
        objectType: Column
      }]
    };
  }

  override getDelegateScrollable(): Widget {
    return this.table;
  }

  /* **********************************************************************
   * Value handling methods
   * ********************************************************************** */

  protected _isValueEmpty(value: TValue): boolean {
    return this._isValueEmptyDefault(value as TLookup[]);
  }

  protected _valueToTable(newValue: TValue) {
    this._valueToTableDefault(newValue as TLookup[]);
  }

  protected _tableToValue(): TValue {
    return this._tableToValueDefault() as TValue;
  }

  /* **********************************************************************
   * Default implementations of value handling methods (TValue = TLookup[])
   * ********************************************************************** */

  protected _isValueEmptyDefault(value: TLookup[]): boolean {
    return arrays.empty(value as TLookup[]);
  }

  protected _valueToTableDefault(newValue: TLookup[]) {
    const opts = {checkOnlyEnabled: false};

    const rowsToCheck: TableRow[] = [];

    this.table.uncheckRows(this.table.rows, opts);
    this.table.rows.forEach(row => {
      if (arrays.containsAny(newValue, row.lookupRow.key)) {
        rowsToCheck.push(row);
      }
    }, this);
    this.table.checkRows(rowsToCheck, opts);
  }

  protected _tableToValueDefault(): TLookup[] {
    return this.table.rows
      .filter(r => r.checked)
      .map(r => r.lookupRow.key);
  }
}
