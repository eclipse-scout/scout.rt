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
scout.DesktopNotificationAdapter = function() {
  scout.DesktopNotificationAdapter.parent.call(this);
};
scout.inherits(scout.DesktopNotificationAdapter, scout.ModelAdapter);

scout.DesktopNotificationAdapter.prototype._onWidgetClose = function(event) {
  this._send('closed', {
    ref: event.ref
  });
};

scout.DesktopNotificationAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'close') {
    this._onWidgetClose(event);
  } else {
    scout.DesktopNotificationAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};
