/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.BeanColumn = function() {
  scout.BeanColumn.parent.call(this);
};
scout.inherits(scout.BeanColumn, scout.Column);

scout.BeanColumn.prototype.buildCellForRow = function(row) {
  var $cell, value;
  $cell = $(scout.BeanColumn.parent.prototype.buildCellForRow.call(this, row));
  // Clear any content (e.g. nbsp due to empty text)
  $cell.empty();
  $cell.removeClass('empty');

  value = this.table.cellValue(this, row);
  this._renderValue($cell, value);
  if (scout.device.tableAdditionalDivRequired) {
    $cell.html('<div class="width-fix" style="max-width: ' + (this.width - this.table.cellHorizontalPadding - 2 /* unknown IE9 extra space */ ) + 'px; ' + '">' + $cell.html() + '</div>');
  }
  return $cell[0].outerHTML;
};

/**
 * Override to render the value.<p>
 * If you have a large table you should consider overriding buildCellForRow instead and create the html as string instead of using jquery.
 */
scout.BeanColumn.prototype._renderValue = function($cell, value) {
  // to be implemented by the subclass
};

/**
 * Default approach reads the html using buildCellForRow and uses _preprocessTextForGrouping to generate the value. Just using text() does not work because new lines get omitted.
 * If this approach does not work for a specific bean column, just override this method.
 */
scout.BeanColumn.prototype.cellValueForGrouping = function(row) {
  var html = this.buildCellForRow(row);
  return this._preprocessTextForValueGrouping(html, true);
};

scout.BeanColumn.prototype.cellTextForGrouping = function(row) {
  var html = this.buildCellForRow(row);
  return this._preprocessTextForGrouping(html, true);
};

scout.BeanColumn.prototype.cellTextForTextFilter = function(row) {
  var html = this.buildCellForRow(row);
  return this._preprocessTextForTextFilter(html, true);
};
