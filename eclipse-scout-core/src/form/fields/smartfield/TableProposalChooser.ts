/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  aria, arrays, Cell, Column, ColumnDescriptor, ListBoxTableAccessibilityRenderer, lookupField, LookupRow, objects, ProposalChooser, scout, SmartFieldLookupResult, Table, TableLayoutResetter, TableRow, TableRowClickEvent, TableRowModel,
  TableRowsSelectedEvent
} from '../../../index';

export class TableProposalChooser<TValue> extends ProposalChooser<TValue, Table, TableRow> {

  protected override _createContent(): Table {
    let headerVisible = false,
      columns = [],
      descriptors = this.smartField.columnDescriptors;

    if (descriptors) {
      descriptors.forEach(function(descriptor, index) {
        headerVisible = headerVisible || !!descriptor.text;
        columns.push(this._createColumnForDescriptor(descriptor));
      }, this);
    } else {
      columns.push(this._createColumn());
    }

    let table = this._createTable(columns, headerVisible);
    // this also renders smartfields with actual tables as list boxes, this seems to be fine
    table.accessibilityRenderer = new ListBoxTableAccessibilityRenderer();
    table.on('rowClick', this._onRowClick.bind(this));
    table.on('rowsSelected', this._onRowsSelected.bind(this));

    return table;
  }

  protected override _createLayoutResetter(): TableLayoutResetter {
    return scout.create(TableLayoutResetter, this.content);
  }

  protected _createColumn(): Column<TValue> {
    return scout.create(Column, {
      session: this.session,
      width: Column.NARROW_MIN_WIDTH,
      horizontalAlignment: this.smartField.gridData.horizontalAlignment
    }) as Column<TValue>;
  }

  protected _createColumnForDescriptor(descriptor: ColumnDescriptor): Column {
    let width = Column.NARROW_MIN_WIDTH;
    if (descriptor.width && descriptor.width > 0) { // 0 = default
      width = descriptor.width;
    }
    return scout.create(scout.nvl(descriptor.objectType, Column), {
      session: this.session,
      text: descriptor.text,
      cssClass: scout.nvl(descriptor.cssClass, null),
      width: width, // needs to be passed here to make sure initialWidth is also set, if set using setWidth() autoResizeColumn won't work because initialWidth would still be NARROW_MIN_WIDTH
      autoOptimizeWidth: scout.nvl(descriptor.autoOptimizeWidth, false),
      fixedWidth: scout.nvl(descriptor.fixedWidth, false),
      fixedPosition: scout.nvl(descriptor.fixedPosition, false),
      horizontalAlignment: scout.nvl(descriptor.horizontalAlignment, this.smartField.gridData.horizontalAlignment),
      visible: scout.nvl(descriptor.visible, true),
      htmlEnabled: scout.nvl(descriptor.htmlEnabled, false),
      headerIconId: scout.nvl(descriptor.headerIconId, null)
    });
  }

  protected _createTable(columns: Column<TValue>[], headerVisible: boolean): Table {
    const table = scout.create(Table, {
      parent: this,
      headerVisible: headerVisible,
      autoResizeColumns: true,
      multiSelect: false,
      multilineText: true,
      scrollToSelection: true,
      columns: columns,
      headerMenusEnabled: false,
      textFilterEnabled: false
    });
    table.selectionHandler.mouseMoveSelectionEnabled = false;
    return table;
  }

  protected _onRowClick(event: TableRowClickEvent) {
    let row = event.row;
    if (!row || !row.enabled) {
      return;
    }
    this.setBusy(true);
    this.triggerLookupRowSelected(row);
  }

  protected override _postRender() {
    super._postRender();
    let row = this.content.selectedRow();
    this._renderSelectedRow(row);
    aria.hasPopup(this.smartField.$field, 'grid');
  }

  protected _onRowsSelected(event: TableRowsSelectedEvent) {
    let row = this.content.selectedRow();
    this._renderSelectedRow(row);
  }

  protected _renderSelectedRow(row: TableRow) {
    if (row && row.$row) {
      aria.linkElementWithActiveDescendant(this.smartField.$field, row.$row);
    } else {
      aria.removeActiveDescendant(this.smartField.$field);
    }
  }

  override selectedRow(): TableRow {
    return this.content.selectedRow();
  }

  override setLookupResult(result: SmartFieldLookupResult<TValue>) {
    let
      tableRows = [],
      lookupRows = result.lookupRows,
      multipleColumns = !!this.smartField.columnDescriptors;

    this.content.deleteAllRows();
    lookupRows.forEach(lookupRow => {
      tableRows.push(this._createTableRow(lookupRow, multipleColumns));
    });
    this.content.insertRows(tableRows);

    this._selectProposal(result, tableRows);
  }

  override trySelectCurrentValue() {
    let currentValue = this.smartField.getValueForSelection();
    if (objects.isNullOrUndefined(currentValue)) {
      return;
    }
    let tableRow = arrays.find(this.content.rows, row => {
      return row.lookupRow.key === currentValue;
    });
    if (tableRow) {
      this.content.selectRow(tableRow);
    }
  }

  override selectFirstLookupRow() {
    if (this.content.rows.length) {
      this.content.selectRow(this.content.rows[0]);
    }
  }

  override clearSelection() {
    this.content.deselectAll();
  }

  override clearLookupRows() {
    this.content.deleteAllRows();
  }

  /**
   * Creates a table-row for the given lookup-row.
   *
   * @returns table-row model
   */
  protected _createTableRow(lookupRow: LookupRow<TValue>, multipleColumns: boolean): TableRowModel {
    let row = lookupField.createTableRow(lookupRow, multipleColumns);
    if (multipleColumns) {
      arrays.pushAll(row.cells, this._transformTableRowData(lookupRow, lookupRow.additionalTableRowData));
    }
    return row;
  }

  protected override _renderContent() {
    this.content.setVirtual(this.smartField.virtual());
    super._renderContent();
    this.content.$data.addClass('top-border-on-first-row');
  }

  override getSelectedLookupRow(): LookupRow<TValue> {
    let selectedRow = this.content.selectedRow();
    if (!selectedRow) {
      return null;
    }
    return selectedRow.lookupRow;
  }

  /**
   * Takes the TableRowData bean and the infos provided by the column descriptors to create an
   * array of additional values in the correct order, as defined by the descriptors.
   */
  protected _transformTableRowData(lookupRow: LookupRow<TValue>, tableRowData: object): Cell<TValue>[] {
    let descriptors = this.smartField.columnDescriptors;
    let cells = [];
    descriptors.forEach(desc => {
      cells.push(lookupField.createTableCell(lookupRow, desc, tableRowData));
    });
    return cells;
  }
}
