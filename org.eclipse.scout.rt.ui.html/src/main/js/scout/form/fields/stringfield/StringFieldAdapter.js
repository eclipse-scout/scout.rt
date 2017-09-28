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
scout.StringFieldAdapter = function() {
  scout.StringFieldAdapter.parent.call(this);
};
scout.inherits(scout.StringFieldAdapter, scout.BasicFieldAdapter);

scout.StringFieldAdapter.prototype._initProperties = function(model) {
  if (model.insertText !== undefined) {
    // ignore pseudo property initially (to prevent the function StringField#insertText() to be replaced)
    delete model.insertText;
  }
};

scout.StringFieldAdapter.prototype._syncInsertText = function(insertText) {
  this.widget.insertText(insertText);
};

scout.StringFieldAdapter.prototype._onWidgetSelectionChange = function(event) {
  // send delayed to avoid a lot of requests while selecting
  // coalesce: only send the latest selection changed event for a field
  this._send('selectionChange', {
    selectionStart: event.selectionStart,
    selectionEnd: event.selectionEnd
  }, {
    delay: 500,
    coalesce: function(previous) {
      return this.target === previous.target && this.type === previous.type;
    }
  });
};

scout.StringFieldAdapter.prototype._onWidgetAction = function(event) {
  this._send('action');
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
