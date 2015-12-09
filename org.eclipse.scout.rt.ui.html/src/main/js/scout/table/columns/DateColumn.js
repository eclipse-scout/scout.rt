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
scout.DateColumn = function() {
  scout.DateColumn.parent.call(this);
};
scout.inherits(scout.DateColumn, scout.Column);

scout.DateColumn.prototype.init = function(model) {
  scout.DateColumn.parent.prototype.init.call(this, model);

  this.groupFormatFormatter = new scout.DateFormat(this.session.locale, this.groupFormat);
};

/**
 * Converts the value which is a json date to a javascript date.
 */
scout.DateColumn.prototype.initCell = function(cell) {
  cell = scout.DateColumn.parent.prototype.initCell.call(this, cell);
  if (cell.value !== undefined) {
    cell.value = scout.dates.parseJsonDate(cell.value);
  }
  return cell;
};

scout.DateColumn.prototype.cellTextForGrouping = function(row) {
  if (this.groupFormat === undefined || this.groupFormat === this.format || !this.groupFormatFormatter) {
    // fallback/shortcut, if no groupFormat defined or groupFormat equals format use cellText
    return scout.DateColumn.parent.prototype.cellTextForGrouping.call(this, row);
  }

  var val = this.table.cellValue(this, row);
  return this.groupFormatFormatter.format(val);
};
