/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TagFieldAdapter = function() {
  scout.TagFieldAdapter.parent.call(this);
};
scout.inherits(scout.TagFieldAdapter, scout.LookupFieldAdapter);

scout.TagFieldAdapter.prototype._initProperties = function(model) {
  if (model.insertText !== undefined) {
    // ignore pseudo property initially (to prevent the function StringField#insertText() to be replaced)
    delete model.insertText;
  }
};

scout.TagFieldAdapter.prototype._postCreateWidget = function() {
  scout.TagFieldAdapter.parent.prototype._postCreateWidget.call(this);
  this.widget.lookupCall = scout.create('RemoteLookupCall', this);
};

scout.TagFieldAdapter.prototype._syncResult = function(result) {
  if (this.widget._currentLookupCall) {
    this.widget._currentLookupCall.resolveLookup(result);
  }
};

scout.TagFieldAdapter.prototype._onWidgetAcceptInput = function(event) {
  this._send('acceptInput', {
    displayText: event.displayText,
    value: event.value
  });
};
