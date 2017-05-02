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
scout.SmartField2Adapter = function() {
  scout.SmartField2Adapter.parent.call(this);
};
scout.inherits(scout.SmartField2Adapter, scout.ValueFieldAdapter);

scout.SmartField2Adapter.prototype._postCreateWidget = function() {
  scout.SmartField2Adapter.parent.prototype._postCreateWidget.call(this);
  this.widget.lookupCall = scout.create('ScoutRemoteLookupCall', this);
};

scout.SmartField2Adapter.prototype.onModelAction = function(event) {
  if (event.type === 'lookupResult') {
    this._onLookupResult(event);
  } else {
    scout.SmartField2Adapter.parent.prototype.onModelAction.call(this, event);
  }
};

scout.SmartField2Adapter.prototype._syncResult = function(result) {
  console.log('_syncResult', result);
  this.widget.lookupCall.resolveLookup(result);
};

scout.SmartField2Adapter.prototype._onLookupResult = function(event) {
  this.widget.lookupCall.resolveLookup(event.data);
};

scout.SmartField2Adapter.prototype.lookup = function() {
  var request = {
    query: this.widget._readDisplayText(),
    filterKey: null
  };
  this._send('lookup', request);
};
