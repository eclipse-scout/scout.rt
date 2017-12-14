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
scout.TagFieldEnterKeyStroke = function(tagField) {
  scout.TagFieldEnterKeyStroke.parent.call(this);
  this.field = tagField;
  this.which = [scout.keys.ENTER];
  this.renderingHints.render = false;
  this.preventDefault = false;
};
scout.inherits(scout.TagFieldEnterKeyStroke, scout.KeyStroke);

//scout.TagFieldEnterKeyStroke.prototype._applyPropagationFlags = function(event) {
//  scout.TagFieldEnterKeyStroke.parent.prototype._applyPropagationFlags.call(this, event);
//
//  var activeElement = this.field.$container.activeElement(true);
//  this.preventInvokeAcceptInputOnActiveValueField = !event.isPropagationStopped() && activeElement.tagName.toLowerCase() === 'textarea';
//  if (this.preventInvokeAcceptInputOnActiveValueField) {
//    event.stopPropagation();
//  }
//};

scout.TagFieldEnterKeyStroke.prototype.handle = function(event) {
  this.field.acceptInput();
};
