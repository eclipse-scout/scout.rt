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
scout.NumberFieldAdapter = function() {
  scout.NumberFieldAdapter.parent.call(this);
};
scout.inherits(scout.NumberFieldAdapter, scout.BasicFieldAdapter);

scout.NumberFieldAdapter.prototype._onWidgetParseError = function(event) {
  // The parsing might fail on JS side, but it might succeed on server side -> Don't show an error status, instead let the server decide
  event.preventDefault();
};

scout.NumberFieldAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'parseError') {
    this._onWidgetParseError(event);
  } else {
    scout.NumberFieldAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};

scout.NumberFieldAdapter.modifyPrototype = function() {
  if (!scout.app.remote) {
    return;
  }

  scout.objects.replacePrototypeFunction(scout.NumberField, 'clearErrorStatus', function() {
    if (this.modelAdapter) {
      // Don't do anything -> let server handle it
      return;
    } else {
      return this.clearErrorStatusOrig();
    }
  }, true);
};

scout.addAppListener('bootstrap', scout.NumberFieldAdapter.modifyPrototype);
