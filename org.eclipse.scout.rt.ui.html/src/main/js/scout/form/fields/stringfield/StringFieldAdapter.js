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
scout.StringFieldAdapter = function() {
  scout.StringFieldAdapter.parent.call(this);
};
scout.inherits(scout.StringFieldAdapter, scout.BasicFieldAdapter);

scout.StringFieldAdapter.prototype._onWidgetSelectionChange = function(event) {
  // send delayed to avoid a lot of requests while selecting
  // coalesce: only send the latest selection changed event for a field
  this._send('selectionChanged', {
    selectionStart: event.selectionStart,
    selectionEnd: event.selectionEnd
  }, {
    delay: 500,
    coalesce: function(previous) {
      return this.id === previous.id && this.type === previous.type;
    }
  });
};

scout.StringFieldAdapter.prototype._onWidgetAction = function(event) {
  this._send('callAction');
};

scout.StringFieldAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'selectionChange') {
    this._onWidgetSelectionChange(event);
  } else if (event.type === 'action') {
    this._onWidgetAction(event);
  } else {
    scout.StringFieldAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};
