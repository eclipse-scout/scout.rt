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
import {defaultValues} from '../index';
import * as $ from 'jquery';

export default class TableRow {

constructor() {
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
  this.status = TableRow.Status.NON_CHANGED;
}

static Status = {
  NON_CHANGED: 'nonChanged',
  INSERTED: 'inserted',
  UPDATED: 'updated'
};

init(model) {
  this._init(model);
  this.initialized = true;
}

_init(model) {
  if (!model.parent) {
    throw new Error('missing property \'parent\'');
  }
  $.extend(this, model);
  defaultValues.applyTo(this);
  this._initCells();
}

_initCells() {
  this.getTable().columns.forEach(function(column) {
    if (!column.guiOnly) {
      var cell = this.cells[column.index];
      cell = column.initCell(cell, this);
      this.cells[column.index] = cell;
    }
  }, this);
}

animateExpansion() {
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
}

hasFilterAcceptedChildren() {
  return this.childRows.some(function(childRow) {
    return childRow.filterAccepted || childRow.hasFilterAcceptedChildren();
  });
}

getTable() {
  return this.parent;
}
}
