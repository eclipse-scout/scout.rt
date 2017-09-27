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

scout.SmartFieldAdapter.prototype._postCreateWidget = function() {
  scout.SmartFieldAdapter.parent.prototype._postCreateWidget.call(this);
  this.widget.lookupCall = scout.create('RemoteLookupCall', this);
};

scout.SmartFieldAdapter.prototype._syncResult = function(result) {
  this.widget.lookupCall.resolveLookup(result);
};

// When displayText comes from the server we don't we must not call parseAndSetValue here.
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

scout.SmartFieldAdapter.prototype._onWidgetAcceptInput = function(event) {
  var eventData = {
    displayText: event.displayText,
    errorStatus: event.errorStatus
  };

  if (!event.errorStatus) {
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
