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
scout.TableRowDetail = function() {
  scout.TableRowDetail.parent.call(this);
  this.table = null;
  this.page = null;
  this.row = null;
  this._tableRowsUpdatedHandler = this._onTableRowsUpdated.bind(this);
  this._tableRowsInsertedHandler = this._onTableRowsInserted.bind(this);
};
scout.inherits(scout.TableRowDetail, scout.Widget);

scout.TableRowDetail.prototype._init = function(model) {
  scout.TableRowDetail.parent.prototype._init.call(this, model);
  this.row = this.page.row;

  this.table.on('rowsUpdated', this._tableRowsUpdatedHandler);
  this.table.on('rowsInserted', this._tableRowsInsertedHandler);
};

scout.TableRowDetail.prototype._destroy = function() {
  this.table.off('rowsUpdated', this._tableRowsUpdatedHandler);
  this.table.off('rowsInserted', this._tableRowsInsertedHandler);
  scout.TableRowDetail.parent.prototype._destroy.call(this);
};

scout.TableRowDetail.prototype._render = function() {
  this.$container = this.$parent.appendDiv('table-row-detail');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this._renderRow();
};

scout.TableRowDetail.prototype._renderRow = function() {
  this.table.visibleColumns().forEach(this._renderCell.bind(this));
  this.invalidateLayoutTree();
};

scout.TableRowDetail.prototype._renderCell = function(column) {
  var cell = this.table.cell(column, this.row);
  if (scout.strings.empty(cell.text) && !cell.iconId) {
    return;
  }

  var headerText;
  if (column.headerHtmlEnabled) {
    headerText = scout.strings.plainText(column.text);
  } else {
    headerText = column.text;
  }

  if (scout.strings.empty(headerText)) {
    headerText = column.headerTooltipText;
  }

  var cellText = column.cellTextForRowDetail(this.row);

  var $field = this.$container.appendDiv('table-row-detail-field');
  if (!scout.strings.empty(headerText)) {
    $field.appendSpan('table-row-detail-name').text(headerText + ': ');
  }

  var iconId = cell.iconId;
  var hasCellText = !scout.strings.empty(cellText);
  if (iconId) {
    var $icon = $field.appendIcon(iconId, 'table-row-detail-icon');
    $icon.toggleClass('with-text', hasCellText);
  }
  if (hasCellText) {
    $field.appendSpan('table-row-detail-value').html(cellText);
  }
};

scout.TableRowDetail.prototype._refreshRow = function() {
  this.$container.empty();
  this._renderRow();
};

scout.TableRowDetail.prototype._onTableRowsUpdated = function(event) {
  if (!this.rendered) {
    return;
  }

  var row = scout.arrays.find(event.rows, function(row) {
    return row.id === this.row.id;
  }.bind(this));

  if (!row) {
    return;
  }

  this.row = row;

  this._refreshRow();
};

/**
 * If the table is reloaded without reloading the corresponding nodes,
 * the insert events need to be handled to refresh the table row detail.
 */
scout.TableRowDetail.prototype._onTableRowsInserted = function(event) {
  if (!this.rendered) {
    return;
  }

  if (event.source.rows.indexOf(this.page.row) < 0) {
    return;
  }

  this.row = this.page.row;
  this._refreshRow();
};
