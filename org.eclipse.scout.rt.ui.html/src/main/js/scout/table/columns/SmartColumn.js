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
  this.uiSortPossible = true;
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

  if (value === null || value === undefined) {
    return cell;
  }

  // FIXME CGU This needs to be done as well if cell value changes
  this.lookupCall.textById(value).done(function(text) {
    cell.setText(text);
    this.table.updateRow(row);
  }.bind(this));

  return cell;
};
