/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
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
  this.initialized = false;
  this.parentRow;
  this.childRows = [];
  this.expanded = false;
  this.status = scout.TableRow.Status.NON_CHANGED;
};

scout.TableRow.Status = {
  NON_CHANGED: 'nonChanged',
  INSERTED: 'inserted',
  UPDATED: 'updated'
};

scout.TableRow.prototype.init = function(model) {
  this._init(model);
  this.initialized = true;
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
      cell = column.initCell(cell, this);
      this.cells[column.index] = cell;
    }
  }, this);
};

scout.TableRow.prototype.animateExpansion = function() {
  var $row = this.$row,
    $rowControl;
  if (!$row) {
    return;
  }
  $rowControl = $row.find('.table-row-control');
  if (this.expanded) {
    $rowControl.addClassForAnimation('expand-rotate');
  } else {
    $rowControl.addClassForAnimation('collapse-rotate');
  }
};

scout.TableRow.prototype.hasFilterAcceptedChildren = function() {
  return this.childRows.some(function(childRow) {
    return childRow.filterAccepted || childRow.hasFilterAcceptedChildren();
  });
};

scout.TableRow.prototype.getTable = function() {
  return this.parent;
};
