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
  this.table;
  this.row;
};
scout.inherits(scout.TableRowDetail, scout.Widget);

scout.TableRowDetail.prototype._init = function(model) {
  scout.TableRowDetail.parent.prototype._init.call(this, model);
  this.table = model.table;
  this.row = model.row;
};

scout.TableRowDetail.prototype._render = function() {
  this.$container = this.$parent.appendDiv('table-row-detail');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this._renderRow();
};

scout.TableRowDetail.prototype._renderRow = function() {
  this.table.visibleColumns().forEach(this._renderCell.bind(this));
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

  var cellText;
  if (cell.htmlEnabled) {
    cellText = scout.strings.plainText(cell.text);
  } else {
    cellText = cell.text;
  }

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
    $field.appendSpan('table-row-detail-value').text(cellText);
  }
};
