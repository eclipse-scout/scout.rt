/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.BrowserFieldAdapter = function() {
  scout.BrowserFieldAdapter.parent.call(this);
};
scout.inherits(scout.BrowserFieldAdapter, scout.ValueFieldAdapter);

scout.BrowserFieldAdapter.prototype._onWidgetMessage = function(event) {
  this._send('postMessage', {
    data: event.data,
    origin: event.origin
  });
};

scout.BrowserFieldAdapter.prototype._onWidgetExternalWindowStateChange = function(event) {
  this._send('externalWindowStateChange', {
    windowState: event.windowState
  });
};

scout.BrowserFieldAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'message') {
    this._onWidgetMessage(event);
  } else if (event.type === 'externalWindowStateChange') {
    this._onWidgetExternalWindowStateChange(event);
  } else {
    scout.BrowserFieldAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};
