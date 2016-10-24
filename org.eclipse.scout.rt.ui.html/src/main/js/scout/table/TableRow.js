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
scout.TableRow = function() {
  this.$row;
  this.aggregateRowAfter;
  this.cells = [];
  this.checked = false;
  this.enabled = true;
  this.filterAccepted = true;
  this.height;
  this.hasError = false;
  this.id;
};

scout.TableRow.prototype.init = function(model) {
  this._init(model);
};

scout.TableRow.prototype._init = function(model) {
  if (!model.parent) {
    throw new Error('missing property \'parent\'');
  }
  $.extend(this, model);
  scout.defaultValues.applyTo(this);
  this._initCells();
};

scout.TableRow.prototype._initCells = function() {
  this.getTable().columns.forEach(function(column) {
    if (!column.guiOnly) {
      var cell = this.cells[column.index];
      cell = column.initCell(cell);
      this.cells[column.index] = cell;
    }
  }, this);
};

scout.TableRow.prototype.getTable = function() {
  return this.parent;
};

scout.TableRow.prototype.clone = function() {
  var cloneRow = $.extend({}, this);
  cloneRow.cells = [];
  this.cells.forEach(function(cell) {
    cloneRow.cells.push($.extend({}, cell));
  });
  return cloneRow;
};
