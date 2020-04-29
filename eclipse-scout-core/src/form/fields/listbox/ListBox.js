/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, ListBoxLayout, LookupBox, scout, Table, ValueField} from '../../../index';

export default class ListBox extends LookupBox {

  constructor() {
    super();

    this.table = null;
    this.lookupStatus = null;
    this.clearable = ValueField.Clearable.NEVER;

    this._addWidgetProperties(['table', 'filterBox']);
  }

  _init(model) {
    super._init(model);
    this.table.on('rowsChecked', this._onTableRowsChecked.bind(this));
    this.table.setScrollTop(this.scrollTop);
  }

  _initStructure(value) {
    if (!this.table) {
      this.table = this._createDefaultListBoxTable();
    }
  }

  _render() {
    super._render();
    this.$container.addClass('list-box');
  }

  _createFieldContainerLayout() {
    return new ListBoxLayout(this, this.table, this.filterBox);
  }

  _renderStructure() {
    this.table.render(this.$fieldContainer);
    this.addField(this.table.$container);
  }

  _onTableRowsChecked(event) {
    this._syncTableToValue();
  }

  _syncTableToValue() {
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

  _valueChanged() {
    super._valueChanged();
    this._syncValueToTable(this.value);
  }

  _syncValueToTable(newValue) {
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

  _lookupByAllDone(result) {
    if (super._lookupByAllDone(result)) {
      this._populateTable(result);
    }
  }

  _populateTable(result) {
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
  getCheckedLookupRows() {
    if (this.value === null || arrays.empty(this.value) || this.table.rows.length === 0) {
      return [];
    }

    return this.table.rows.filter(row => {
      return row.checked;
    }).map(row => {
      return row.lookupRow;
    });
  }

  _createTableRow(lookupRow) {
    let
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
  }

  _createDefaultListBoxTable() {
    return scout.create('Table', {
      parent: this,
      autoResizeColumns: true,
      checkable: true,
      checkableStyle: Table.CheckableStyle.CHECKBOX_TABLE_ROW,
      headerVisible: false,
      footerVisible: false,
      columns: [{
        objectType: 'Column'
      }]
    });
  }

  /**
   * @override
   */
  getDelegateScrollable() {
    return this.table;
  }
}
