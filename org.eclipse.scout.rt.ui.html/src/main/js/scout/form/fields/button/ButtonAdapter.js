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
scout.ButtonAdapter = function() {
  scout.ButtonAdapter.parent.call(this);
  this._addRemoteProperties(['selected']);
};
scout.inherits(scout.ButtonAdapter, scout.FormFieldAdapter);

scout.ButtonAdapter.prototype._postCreateWidget = function() {
  if (!this.widget.keyStrokeScope) {
    return;
  }

  var formAdapter = this.widget.getForm().modelAdapter;
  if (formAdapter.attached) {
    this.resolveKeyStrokeScope();
    return;
  }
  // KeyStrokeScope is another widget (form or formfield) which may not be initialized and attached to the adapter yet.
  // The widget must be on the same form as the button, so once that form is attached the keyStrokeScope has to be available
  formAdapter.events.one('attach', this._resolveKeyStrokeScope.bind(this));
};

scout.ButtonAdapter.prototype._resolveKeyStrokeScope = function() {
  this.widget.keyStrokeScope = this.session.getWidget(this.widget.keyStrokeScope);
  if (!this.widget.keyStrokeScope) {
    throw new Error('Could not resolve keyStrokeScope: ' + this.widget.keyStrokeScope);
  }
};

scout.ButtonAdapter.prototype._onWidgetClick = function(event) {
  this._send('clicked');
};

scout.ButtonAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'click') {
    this._onWidgetClick(event);
  } else {
    scout.ButtonAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};
