/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
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

  this._addRemoteProperties(['value', 'activeFilter', 'errorStatus', 'displayText']);
};
scout.inherits(scout.SmartField2Adapter, scout.ValueFieldAdapter);

// FIXME [awe] 7.1 - SF2: set defaults for smartfield2 values in defaults.json

scout.SmartField2Adapter.prototype._postCreateWidget = function() {
  scout.SmartField2Adapter.parent.prototype._postCreateWidget.call(this);
  this.widget.lookupCall = scout.create('RemoteLookupCall', this);
};

scout.SmartField2Adapter.prototype._syncResult = function(result) {
  this.widget.lookupCall.resolveLookup(result);
};

scout.SmartField2Adapter.prototype._onLookupResult = function(event) {
  this.widget.lookupCall.resolveLookup(event.data);
};

// FIXME [awe] 7.1 - SF2: discuss with B.SH, we must not call parseAndSetValue here
// Das war notwendig, im Zusammenhang mit einem Searchform und dem Reset Button
// Der Button setzt auf dem Server den Value auf null. Weil wir aber im Classic Protokoll
// den Value gar nie übertragen, setzen wir den value wenn der displayText ändert. Das
// funktioniert gut für String/Number fields u.ä. - aber leider nicht gut für SmartField
// Idee für den Scout Classic Fall ist, dass man anstatt dem Value eine andere property
// wie z.B. 'smartfieldKey' verwendet. Im JS only fall würde diese property aber nicht
// verwendet.
scout.SmartField2Adapter.prototype._syncDisplayText = function(displayText) {
  this.widget.setDisplayText(displayText);
  // this.widget.parseAndSetValue(displayText);
};

scout.SmartField2Adapter.prototype.lookupAll = function() {
  this._send('lookupAll');
};

scout.SmartField2Adapter.prototype.lookupByText = function(searchText) {
  this._send('lookupByText', {
    searchText: searchText
  });
};

scout.SmartField2Adapter.prototype.lookupByRec = function(rec) {
  this._send('lookupByRec', {
    rec: rec
  });
};

