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
scout.SmartColumn.prototype._init = function(model) {
  this._setLookupCall(this.lookupCall);
  this._setCodeType(this.codeType);
};

scout.SmartColumn.prototype._setLookupCall = function(lookupCall) {
  if (typeof lookupCall === 'string') {
    lookupCall = scout.create(lookupCall, {
      session: this.session
    });
  }
  this.lookupCall = lookupCall;
};

scout.SmartColumn.prototype._setCodeType = function(codeType) {
  this.codeType = codeType;
  if (!codeType) {
    return;
  }
  this.lookupCall = scout.create('CodeLookupCall', {
    session: this.session,
    codeType: codeType
  });
};

scout.SmartColumn.prototype._formatValue = function(value) {
  if (!this.lookupCall) {
    return scout.strings.nvl(value) + '';
  }
  return this.lookupCall.textById(value);
};

scout.SmartColumn.prototype.cellValueForGrouping = function(row) {
  return this.cell(row).text;
};

scout.SmartColumn.prototype.cellTextForGrouping = function(row) {
  return this.cell(row).text;
};

scout.SmartColumn.prototype.cellTextForTextFilter = function(row) {
  return this.cell(row).text;
};
