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
scout.ValueFieldAdapter = function() {
  scout.ValueFieldAdapter.parent.call(this);
};
scout.inherits(scout.ValueFieldAdapter, scout.FormFieldAdapter);

scout.ValueFieldAdapter.prototype._onWidgetAcceptInput = function(event) {
  this._send('acceptInput', {
    displayText: event.displayText,
    whileTyping: event.whileTyping
  }, {
    showBusyIndicator: !event.whileTyping,
    coalesce: function(previous) {
      return this.target === previous.target && this.type === previous.type && this.whileTyping === previous.whileTyping;
    }
  });
};

scout.ValueFieldAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'acceptInput') {
    this._onWidgetAcceptInput(event);
  } else {
    scout.ValueFieldAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};

/**
 * @override ModelAdapter.js
 */
scout.ValueFieldAdapter.prototype.exportAdapterData = function(adapterData) {
  adapterData = scout.ValueFieldAdapter.parent.prototype.exportAdapterData.call(this, adapterData);
  delete adapterData.displayText;
  return adapterData;
};

scout.ValueFieldAdapter.prototype._syncDisplayText = function(displayText) {
  this.widget.setDisplayText(displayText);
  this.widget.parseAndSetValue(displayText);
};
