/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Column, InitModelOf, ListBoxLayout, ListBoxModel, ListBoxTableAccessibilityRenderer, LookupBox, lookupField, LookupResult, LookupRow, scout, Table, TableRowModel, TableRowsCheckedEvent, Widget} from '../../../index';

export class ListBox<TValue> extends LookupBox<TValue> implements ListBoxModel<TValue> {
  declare model: ListBoxModel<TValue>;

  table: Table;

  constructor() {
    super();

    this.table = null;

    this._addWidgetProperties(['table', 'filterBox']);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.table.on('rowsChecked', this._onTableRowsChecked.bind(this));
    this.table.setScrollTop(this.scrollTop);
  }

  protected _initStructure(value: TValue[]) {
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

  protected _createFieldContainerLayout(): ListBoxLayout {
    return new ListBoxLayout(this, this.table, this.filterBox);
  }

  protected _renderStructure() {
    this.table.render(this.$fieldContainer);
    this.addField(this.table.$container);
  }

  protected _onTableRowsChecked(event: TableRowsCheckedEvent) {
    this._syncTableToValue();
  }

  protected _syncTableToValue() {
    if (!this.lookupCall || this._valueSyncing) {
      return;
    }
    this._valueSyncing = true;
    let valueArray = [];
    this.table.rows.forEach(row => {
      if (row.checked) {
        valueArray.push(row.lookupRow.key);
      }
    }, this);

    this.setValue(valueArray);
    this._valueSyncing = false;
  }

  protected override _valueChanged() {
    super._valueChanged();
    this._syncValueToTable(this.value);
  }

  protected _syncValueToTable(newValue: TValue[]) {
    if (!this.lookupCall || this._valueSyncing || !this.initialized) {
      return;
    }

    this._valueSyncing = true;
    let opts = {
      checkOnlyEnabled: false
    };
    try {
      if (arrays.empty(newValue)) {
        this.table.uncheckRows(this.table.rows, opts);
      } else {
        // if lookup was not executed yet: do it now.
        let lookupScheduled = this._ensureLookupCallExecuted();
        if (lookupScheduled) {
          return; // was the first lookup: table has no rows yet. cancel sync. Will be executed again after lookup execution.
        }

        let rowsToCheck = [];

        this.table.uncheckRows(this.table.rows, opts);
        this.table.rows.forEach(row => {
          if (arrays.containsAny(newValue, row.lookupRow.key)) {
            rowsToCheck.push(row);
          }
        }, this);
        this.table.checkRows(rowsToCheck, opts);
      }

      this._updateDisplayText();
    } finally {
      this._valueSyncing = false;
    }
  }

  protected override _lookupByAllDone(result: LookupResult<TValue>) {
    super._lookupByAllDone(result);
    this._populateTable(result);
  }

  protected _populateTable(result: LookupResult<TValue>) {
    let
      tableRows = [],
      lookupRows = result.lookupRows;

    lookupRows.forEach(function(lookupRow) {
      tableRows.push(this._createTableRow(lookupRow));
    }, this);

    this.table.deleteAllRows();
    this.table.insertRows(tableRows);

    this._syncValueToTable(this.value);
  }

  /**
   * Returns a lookup row for each value currently checked.
   */
  getCheckedLookupRows(): LookupRow<TValue>[] {
    if (this.value === null || arrays.empty(this.value) || this.table.rows.length === 0) {
      return [];
    }

    return this.table.rows
      .filter(row => row.checked)
      .map(row => row.lookupRow);
  }

  protected _createTableRow(lookupRow: LookupRow<TValue>): TableRowModel {
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

  protected _createDefaultListBoxTable(): Table {
    return scout.create(Table, {
      parent: this,
      autoResizeColumns: true,
      checkable: true,
      checkableStyle: Table.CheckableStyle.CHECKBOX_TABLE_ROW,
      headerVisible: false,
      footerVisible: false,
      columns: [{
        objectType: Column
      }]
    });
  }

  override getDelegateScrollable(): Widget {
    return this.table;
  }
}
