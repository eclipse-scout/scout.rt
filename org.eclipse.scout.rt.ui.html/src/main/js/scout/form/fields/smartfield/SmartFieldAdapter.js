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
scout.SmartFieldAdapter = function() {
  scout.SmartFieldAdapter.parent.call(this);

  this._addRemoteProperties(['activeFilter']);
};
scout.inherits(scout.SmartFieldAdapter, scout.ValueFieldAdapter);

/**
 * Property lookup-row must be handled before value, since the smart-field has either a lookup-row
 * or a value but never both (when we only have a value, the smart-field must perform a lookup by key
 * in order to resolve the display name for that value).
 */
scout.SmartFieldAdapter.PROPERTIES_ORDER = ['lookupRow', 'value', 'errorStatus', 'displayText'];

scout.SmartFieldAdapter.prototype._postCreateWidget = function() {
  scout.SmartFieldAdapter.parent.prototype._postCreateWidget.call(this);
  this.widget.lookupCall = scout.create('RemoteLookupCall', this);
};

scout.SmartFieldAdapter.prototype._syncResult = function(result) {
  var executedLookupCall = this.widget._currentLookupCall;
  if (!executedLookupCall && this.widget.touchMode && this.widget.popup && this.widget.popup._field) {
    // in case lookupCall is executed from within the popup (touch):
    executedLookupCall = this.widget.popup._field._currentLookupCall;
  }
  if (executedLookupCall) {
    executedLookupCall.resolveLookup(result);
  }
};

// When displayText comes from the server we must not call parseAndSetValue here.
scout.SmartFieldAdapter.prototype._syncDisplayText = function(displayText) {
  this.widget.setDisplayText(displayText);
};

/**
 * @param {scout.QueryBy} queryBy
 * @param {object} [queryData] optional data (text, key, rec)
 */
scout.SmartFieldAdapter.prototype.sendLookup = function(queryBy, queryData) {
  var propertyName = queryBy.toLowerCase(),
    requestType = 'lookupBy' + scout.strings.toUpperCaseFirstLetter(propertyName),
    requestData = {
      showBusyIndicator: false
    };
  if (!scout.objects.isNullOrUndefined(queryData)) {
    requestData[propertyName] = queryData;
  }
  this._send(requestType, requestData);
};

scout.SmartFieldAdapter.prototype._orderPropertyNamesOnSync = function(newProperties) {
  return Object.keys(newProperties).sort(this._createPropertySortFunc(scout.SmartFieldAdapter.PROPERTIES_ORDER));
};

scout.SmartFieldAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'acceptByText') {
    this._onWidgetAcceptByText(event);
  } else {
    scout.SmartFieldAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};

scout.SmartFieldAdapter.prototype._onWidgetAcceptByText = function(event) {
  this._sendProperty('errorStatus', event.errorStatus);
};

scout.SmartFieldAdapter.prototype._onWidgetAcceptInput = function(event) {
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
