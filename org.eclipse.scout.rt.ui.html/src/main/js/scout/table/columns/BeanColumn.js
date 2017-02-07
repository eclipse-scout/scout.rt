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
  this.additionalDivMode = scout.BeanColumn.AdditionalDivMode.AUTO;
};
scout.inherits(scout.BeanColumn, scout.Column);

scout.BeanColumn.AdditionalDivMode = {
  NEVER: 0,
  AUTO: 1
};

scout.BeanColumn.prototype.buildCellForRow = function(row) {
  var $cell, value;
  $cell = $(scout.BeanColumn.parent.prototype.buildCellForRow.call(this, row));
  // Clear any content (e.g. nbsp due to empty text)
  $cell.empty();
  $cell.removeClass('empty');

  value = this.table.cellValue(this, row);
  this._renderValue($cell, value);
  if (this.additionalDivMode === scout.BeanColumn.AdditionalDivMode.AUTO && scout.device.tableAdditionalDivRequired) {
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

scout.BeanColumn.prototype._plainTextForRow = function(row) {
  var cell = this.table.cell(this, row);
  if (!cell.plainText) {
    // Convert to plain text and cache it because rendering is expensive
    var html = this.buildCellForRow(row);
    cell.plainText = scout.strings.plainText(html);
  }
  return cell.plainText;
};

/**
 * Default approach reads the html using buildCellForRow and uses _preprocessTextForGrouping to generate the value. Just using text() does not work because new lines get omitted.
 * If this approach does not work for a specific bean column, just override this method.
 */
scout.BeanColumn.prototype.cellValueOrTextForCalculation = function(row) {
  var plainText = this._plainTextForRow(row);
  return this._preprocessTextForCalculation(plainText);
};

scout.BeanColumn.prototype.cellTextForGrouping = function(row) {
  var plainText = this._plainTextForRow(row);
  return this._preprocessTextForGrouping(plainText);
};

scout.BeanColumn.prototype.cellTextForTextFilter = function(row) {
  var plainText = this._plainTextForRow(row);
  return this._preprocessTextForTextFilter(plainText);
};

scout.BeanColumn.prototype.compare = function(row1, row2) {
  var plainText1 = this._plainTextForRow(row1);
  var plainText2 = this._plainTextForRow(row2);
  return this.comparator.compare(plainText1, plainText2);
};
