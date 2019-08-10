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
scout.CodeLookupCall = function() {
  scout.CodeLookupCall.parent.call(this);
  this.codeType = null;
};
scout.inherits(scout.CodeLookupCall, scout.StaticLookupCall);

scout.CodeLookupCall.prototype._lookupRowByKey = function(key) {
  var codeType = scout.codes.codeType(this.codeType, true);
  if (!codeType) {
    return null;
  }
  return this._createLookupRow(codeType.optGet(key));
};

scout.CodeLookupCall.prototype._lookupRowsByAll = function() {
  return this._collectLookupRows();
};

scout.CodeLookupCall.prototype._lookupRowsByText = function(text) {
  return this._collectLookupRows(function(lookupRow) {
    var lookupRowText = lookupRow.text || '';
    return scout.strings.startsWith(lookupRowText.toLowerCase(), text.toLowerCase());
  });
};

scout.CodeLookupCall.prototype._lookupRowsByRec = function(rec) {
  return this._collectLookupRows(function(lookupRow) {
    return lookupRow.parentKey === rec;
  });
};

scout.CodeLookupCall.prototype._collectLookupRows = function(predicate) {
  var codeType = scout.codes.codeType(this.codeType, true);
  if (!codeType) {
    return [];
  }
  var lookupRows = [];
  codeType.visit(function(code) {
    var lookupRow = this._createLookupRow(code);
    if (!predicate || predicate(lookupRow)) {
      lookupRows.push(lookupRow);
    }
  }.bind(this));
  return lookupRows;
};

scout.CodeLookupCall.prototype._createLookupRow = function(code) {
  if (!code) {
    return null;
  }
  var lookupRow = scout.create('LookupRow', {
    key: code.id,
    text: code.text(this.session.locale),
    parentKey: code.parent && code.parent.id
  });
  return lookupRow;
};
