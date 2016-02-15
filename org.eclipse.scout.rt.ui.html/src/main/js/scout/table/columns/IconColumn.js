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
scout.IconColumn = function() {
  scout.IconColumn.parent.call(this);
  this.minWidth = scout.Column.NARROW_MIN_WIDTH;
  this.filterType = 'ColumnUserFilter';
};
scout.inherits(scout.IconColumn, scout.Column);

/**
 * @override
 */
scout.IconColumn.prototype.buildCell = function(cell, row) {
  cell.iconId = cell.value || cell.iconId;
  return scout.IconColumn.parent.prototype.buildCell.call(this, cell, row);
};

/**
 * @override
 */
scout.IconColumn.prototype.cellTextForGrouping = function(row) {
  var cell = this.table.cell(this, row);
  return cell.value;
};

/**
 * @override
 */
scout.IconColumn.prototype.createAggrGroupCell = function(row) {
  var cell = scout.IconColumn.parent.prototype.createAggrGroupCell.call(this, row);
  // Make sure only icon and no text is displayed
  cell.text = null;
  return cell;
};
