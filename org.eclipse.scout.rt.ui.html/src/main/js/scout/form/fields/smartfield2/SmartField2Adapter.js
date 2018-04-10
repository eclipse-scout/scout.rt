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

  this._addRemoteProperties(['activeFilter']);
};
scout.inherits(scout.SmartField2Adapter, scout.ValueFieldAdapter);

/**
 * Property lookup-row must be handled before value, since the smart-field has either a lookup-row
 * or a value but never both (when we only have a value, the smart-field must perform a lookup by key
 * in order to resolve the display name for that value).
 */
scout.SmartField2Adapter.PROPERTIES_ORDER = ['lookupRow', 'value', 'errorStatus', 'displayText'];

scout.SmartField2Adapter.prototype._postCreateWidget = function() {
  scout.SmartField2Adapter.parent.prototype._postCreateWidget.call(this);
  this.widget.lookupCall = scout.create('RemoteLookupCall', this);
};

scout.SmartField2Adapter.prototype._syncResult = function(result) {
  this.widget.lookupCall.resolveLookup(result);
};

// When displayText comes from the server we don't must not call parseAndSetValue here.
scout.SmartField2Adapter.prototype._syncDisplayText = function(displayText) {
  this.widget.setDisplayText(displayText);
};

scout.SmartField2Adapter.prototype.lookupAll = function() {
  this._send('lookupAll', {
    showBusyIndicator: false
  });
};

scout.SmartField2Adapter.prototype.lookupByText = function(searchText) {
  this._send('lookupByText', {
    showBusyIndicator: false,
    searchText: searchText
  });
};

scout.SmartField2Adapter.prototype.lookupByKey = function(key) {
  this._send('lookupByKey', {
    showBusyIndicator: false,
    key: key
  });
};

scout.SmartField2Adapter.prototype.lookupByRec = function(rec) {
  this._send('lookupByRec', {
    showBusyIndicator: false,
    rec: rec
  });
};

scout.SmartField2Adapter.prototype._orderPropertyNamesOnSync = function(newProperties) {
  return Object.keys(newProperties).sort(this._createPropertySortFunc(scout.SmartField2Adapter.PROPERTIES_ORDER));
};

scout.SmartField2Adapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'acceptByText') {
    this._onWidgetAcceptByText(event);
  } else {
    scout.SmartField2Adapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};

scout.SmartField2Adapter.prototype._onWidgetAcceptByText = function(event) {
  this._sendProperty('errorStatus', event.errorStatus);
};

scout.SmartField2Adapter.prototype._onWidgetAcceptInput = function(event) {
  var eventData = {
    displayText: event.displayText,
    errorStatus: event.errorStatus
  };

  if (event.errorStatus) {
    // 'clear' case
    if (scout.strings.empty(event.displayText)) {
      eventData.value = null;
    }
  } else {
    eventData.value = event.value;
    if (event.acceptByLookupRow) {
      eventData.lookupRow = event.lookupRow;
    }
  }

  this._send('acceptInput', eventData, {
    showBusyIndicator: !event.whileTyping,
    coalesce: function(previous) {
      return this.target === previous.target && this.type === previous.type && this.whileTyping === previous.whileTyping;
    }
  });
};
