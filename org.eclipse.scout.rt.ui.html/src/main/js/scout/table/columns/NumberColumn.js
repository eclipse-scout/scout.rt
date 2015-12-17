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
scout.NumberColumn = function() {
  scout.NumberColumn.parent.call(this);
  this.filterType = 'NumberColumnUserFilter';
};
scout.inherits(scout.NumberColumn, scout.Column);

/**
 * @override Column.js
 */
scout.NumberColumn.prototype.init = function(model) {
  scout.NumberColumn.parent.prototype.init.call(this, model);
  if (!(this.decimalFormat instanceof scout.DecimalFormat)) {
    this.decimalFormat = new scout.DecimalFormat(this.session.locale, this.decimalFormat);
  }
};

/**
 * @override Column.js
 */
scout.NumberColumn.prototype._initCell = function(cell) {
  // server sends cell.value only if it differs from text -> make sure cell.value is set and has the right type
  // Cell.value may be undefined for other column types -> use table.cellValue to access the value.
  // The only reason is to save some memory (may get obsolete in the future)
  if (cell.value === undefined && cell.text) { // Number('') would generate 0 -> don't set in that case
    cell.value = Number(cell.text);
  }
};

scout.NumberColumn.prototype.createAggrValueCell = function(value) {
  return {
    text: this.decimalFormat.format(value),
    iconId: this.aggrSymbol,
    horizontalAlignment: this.horizontalAlignment,
    cssClass: 'table-aggregate-cell'
  };
};
