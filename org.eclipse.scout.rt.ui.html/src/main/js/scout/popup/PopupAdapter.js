/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.PopupAdapter = function() {
  scout.PopupAdapter.parent.call(this);
};
scout.inherits(scout.PopupAdapter, scout.ModelAdapter);

scout.PopupAdapter.prototype._onWidgetClose = function(event) {
  // Do not close the popup immediately, server will send the close event
  event.preventDefault();
  this._send('close');
};

scout.PopupAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'close') {
    this._onWidgetClose(event);
  } else {
    scout.PopupAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};
