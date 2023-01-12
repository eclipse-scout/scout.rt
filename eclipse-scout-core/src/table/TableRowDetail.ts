/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Column, EventHandler, HtmlComponent, InitModelOf, Page, SomeRequired, strings, Table, TableRow, TableRowDetailModel, TableRowsInsertedEvent, TableRowsUpdatedEvent, Widget} from '../index';

export class TableRowDetail extends Widget implements TableRowDetailModel {
  declare model: TableRowDetailModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'table' | 'page'>;

  table: Table;
  page: Page;
  row: TableRow;

  protected _tableRowsUpdatedHandler: EventHandler<TableRowsUpdatedEvent>;
  protected _tableRowsInsertedHandler: EventHandler<TableRowsInsertedEvent>;

  constructor() {
    super();
    this.table = null;
    this.page = null;
    this.row = null;
    this._tableRowsUpdatedHandler = this._onTableRowsUpdated.bind(this);
    this._tableRowsInsertedHandler = this._onTableRowsInserted.bind(this);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.row = this.page.row;
    this.table.on('rowsUpdated', this._tableRowsUpdatedHandler);
    this.table.on('rowsInserted', this._tableRowsInsertedHandler);
  }

  protected override _destroy() {
    this.table.off('rowsUpdated', this._tableRowsUpdatedHandler);
    this.table.off('rowsInserted', this._tableRowsInsertedHandler);
    super._destroy();
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('table-row-detail');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this._renderRow();
  }

  protected _renderRow() {
    this.table.visibleColumns().forEach(this._renderCell.bind(this));
    this.invalidateLayoutTree();
  }

  protected _renderCell(column: Column<any>) {
    let cell = this.table.cell(column, this.row);
    if (strings.empty(cell.text) && !cell.iconId) {
      return;
    }

    let headerText: string;
    if (column.headerHtmlEnabled) {
      headerText = strings.plainText(column.text);
    } else {
      headerText = column.text;
    }

    if (strings.empty(headerText)) {
      if (column.headerTooltipHtmlEnabled) {
        headerText = strings.plainText(column.headerTooltipText);
      } else {
        headerText = column.headerTooltipText;
      }
    }

    let cellText = column.cellTextForRowDetail(this.row);
    let $field = this.$container.appendDiv('table-row-detail-field');
    if (!strings.empty(headerText)) {
      $field.appendSpan('table-row-detail-name').text(headerText + ': ');
    }

    let iconId = cell.iconId;
    let hasCellText = !strings.empty(cellText);
    if (iconId) {
      let $icon = $field.appendIcon(iconId, 'table-row-detail-icon') as JQuery;
      $icon.toggleClass('with-text', hasCellText);
    }
    if (hasCellText) {
      $field.appendSpan('table-row-detail-value').html(cellText);
    }
  }

  protected _refreshRow() {
    this.$container.empty();
    this._renderRow();
  }

  protected _onTableRowsUpdated(event: TableRowsUpdatedEvent) {
    if (!this.rendered) {
      return;
    }

    let rows = event.rows;
    let row = arrays.find(rows, row => row.id === this.row.id);
    if (!row) {
      return;
    }

    this.row = row;
    this._refreshRow();
  }

  /**
   * If the table is reloaded without reloading the corresponding nodes,
   * the insert events need to be handled to refresh the table row detail.
   */
  protected _onTableRowsInserted(event: TableRowsInsertedEvent) {
    if (!this.rendered) {
      return;
    }

    if (event.source.rows.indexOf(this.page.row) < 0) {
      return;
    }

    this.row = this.page.row;
    this._refreshRow();
  }
}
