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
scout.FormAdapter = function() {
  scout.FormAdapter.parent.call(this);
};
scout.inherits(scout.FormAdapter, scout.ModelAdapter);

/**
 * @override
 */
scout.FormAdapter.prototype._initModel = function(model, parent) {
  model = scout.FormAdapter.parent.prototype._initModel.call(this, model, parent);
  // Set logical grid to null -> Calculation happens on server side
  model.logicalGrid = null;
  return model;
};

scout.FormAdapter.prototype._onWidgetAbort = function(event) {
  // Do not close the form immediately, server will send the close event
  event.preventDefault();

  this._send('formClosing');
};

scout.FormAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'abort') {
    this._onWidgetAbort(event);
  } else {
    scout.FormAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};

scout.FormAdapter.prototype.onModelAction = function(event) {
  if (event.type === 'requestFocus') {
    this._onRequestFocus(event.formField);
  } else {
    scout.FormAdapter.parent.prototype.onModelAction.call(this, event);
  }
};

scout.FormAdapter.prototype._onRequestFocus = function(formFieldId) {
  var formField = this.session.getOrCreateWidget(formFieldId, this.widget);
  formField.focus();
};
