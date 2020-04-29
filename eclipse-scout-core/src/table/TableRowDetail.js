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
import {arrays, HtmlComponent, strings, Widget} from '../index';

export default class TableRowDetail extends Widget {

  constructor() {
    super();
    this.table = null;
    this.page = null;
    this.row = null;
    this._tableRowsUpdatedHandler = this._onTableRowsUpdated.bind(this);
    this._tableRowsInsertedHandler = this._onTableRowsInserted.bind(this);
  }

  _init(model) {
    super._init(model);
    this.row = this.page.row;
    this.table.on('rowsUpdated', this._tableRowsUpdatedHandler);
    this.table.on('rowsInserted', this._tableRowsInsertedHandler);
  }

  _destroy() {
    this.table.off('rowsUpdated', this._tableRowsUpdatedHandler);
    this.table.off('rowsInserted', this._tableRowsInsertedHandler);
    super._destroy();
  }

  _render() {
    this.$container = this.$parent.appendDiv('table-row-detail');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this._renderRow();
  }

  _renderRow() {
    this.table.visibleColumns().forEach(this._renderCell.bind(this));
    this.invalidateLayoutTree();
  }

  _renderCell(column) {
    let cell = this.table.cell(column, this.row);
    if (strings.empty(cell.text) && !cell.iconId) {
      return;
    }

    let headerText;
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
      let $icon = $field.appendIcon(iconId, 'table-row-detail-icon');
      $icon.toggleClass('with-text', hasCellText);
    }
    if (hasCellText) {
      $field.appendSpan('table-row-detail-value').html(cellText);
    }
  }

  _refreshRow() {
    this.$container.empty();
    this._renderRow();
  }

  _onTableRowsUpdated(event) {
    if (!this.rendered) {
      return;
    }

    let row = arrays.find(event.rows, row => {
      return row.id === this.row.id;
    });

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
  _onTableRowsInserted(event) {
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
