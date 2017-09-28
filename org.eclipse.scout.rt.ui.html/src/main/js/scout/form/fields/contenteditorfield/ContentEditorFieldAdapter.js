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
scout.ContentEditorFieldAdapter = function() {
  scout.ContentEditorFieldAdapter.parent.call(this);
};
scout.inherits(scout.ContentEditorFieldAdapter, scout.FormFieldAdapter);


scout.ContentEditorFieldAdapter.prototype.onModelAction = function(event) {
  if (event.type === 'updateElement') {
    this._onModelUpdateElement(event);
  } else {
    scout.ContentEditorFieldAdapter.parent.prototype.onModelAction.call(this, event);
  }
};

scout.ContentEditorFieldAdapter.prototype._onModelUpdateElement = function(event) {
  this.widget.updateElement(event.elementContent, event.slot, event.elementId);
};

scout.ContentEditorFieldAdapter.prototype._sendContent = function(content) {

};

scout.ContentEditorFieldAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'editElement') {
    this._onEditElement(event);
  }
};

scout.ContentEditorFieldAdapter.prototype._onEditElement = function(event) {
  this._send('editElement', {
    elementContent: event.elementContent,
    slot: event.slot,
    elementId: event.elementId
  });
};
