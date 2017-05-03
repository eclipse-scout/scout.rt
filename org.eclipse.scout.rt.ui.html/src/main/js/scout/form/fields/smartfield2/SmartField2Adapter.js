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

  this._addRemoteProperties(['value']);
};
scout.inherits(scout.SmartField2Adapter, scout.ValueFieldAdapter);

scout.SmartField2Adapter.prototype._postCreateWidget = function() {
  scout.SmartField2Adapter.parent.prototype._postCreateWidget.call(this);
  this.widget.lookupCall = scout.create('ScoutRemoteLookupCall', this);
};

//scout.SmartField2Adapter.prototype.onModelAction = function(event) {
//  if (event.type === 'lookupResult') {
//    this._onLookupResult(event);
//  } else {
//    scout.SmartField2Adapter.parent.prototype.onModelAction.call(this, event);
//  }
//};

//scout.ValueFieldAdapter.prototype._onWidgetEvent = function(event) {
//  if (event.type === 'value') {
//    this._onWidgetDisplayTextChanged(event);
//  } else {
//    scout.ValueFieldAdapter.parent.prototype._onWidgetEvent.call(this, event);
//  }
//};

scout.SmartField2Adapter.prototype._syncResult = function(result) {
  console.log('_syncResult', result);
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

scout.SmartField2Adapter.prototype.lookup = function() {
  var request = {
    text: this.widget._readDisplayText(),
    filterKey: null
  };
  this._send('lookupByText', request);
};
