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
scout.StringFieldEnterKeyStroke = function(stringField) {
  scout.StringFieldEnterKeyStroke.parent.call(this);
  this.field = stringField;
  this.which = [scout.keys.ENTER];
  this.renderingHints.render = false;
  this.preventDefault = false;
};
scout.inherits(scout.StringFieldEnterKeyStroke, scout.KeyStroke);

scout.StringFieldEnterKeyStroke.prototype._applyPropagationFlags = function(event) {
  scout.StringFieldEnterKeyStroke.parent.prototype._applyPropagationFlags.call(this, event);

  var activeElement = this.field.$container.getActiveElement();
  this.preventInvokeAcceptInputOnActiveValueField = !event.isPropagationStopped() && activeElement.tagName.toLowerCase() === 'textarea';
  if (this.preventInvokeAcceptInputOnActiveValueField) {
    event.stopPropagation();
  }
};

scout.StringFieldEnterKeyStroke.prototype.handle = function(event) {
  // NOP
};
