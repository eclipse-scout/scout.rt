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
scout.SmartColumn = function() {
  scout.SmartColumn.parent.call(this);
  this.codeType;
  this.lookupCall;
};
scout.inherits(scout.SmartColumn, scout.Column);

/**
 * @override
 */
scout.SmartColumn.prototype.init = function(model) { // FIXME CGU eigentlich bräuchte es ein _init
  scout.SmartColumn.parent.prototype.init.call(this, model);
  this._syncLookupCall(this.lookupCall);
};

scout.SmartColumn.prototype._syncLookupCall = function(lookupCall) {
  if (typeof lookupCall === 'string') {
    lookupCall = scout.create(lookupCall);
  }
  this.lookupCall = lookupCall;
};


scout.SmartColumn.prototype._createCellModel = function(id) {
  return {
    value: id
  };
};

scout.SmartColumn.prototype.initCell = function(model, row) {
  var cell = scout.SmartColumn.parent.prototype.initCell.call(this, model),
    value = cell.value;

  if (scout.objects.isNullOrUndefined(value)) {
    return cell;
  }

  // FIXME CGU This needs to be done as well if cell value changes
  // FIXME [awe, cgu] 6.1 - it's a bad idea to call updateRow here, because when a row and a cell is initialized and
  // the deferred is resolved immediately the table throws an error because the row is not yet added to the table :-(
  // check if (initialized) below
  this.lookupCall.textById(value).done(function(text) {
    cell.setText(text);
    if (this.table.rows.indexOf(row) > -1) { // add function hasRow()?
      this.table.updateRow(row);
    }
  }.bind(this));

  return cell;
};
