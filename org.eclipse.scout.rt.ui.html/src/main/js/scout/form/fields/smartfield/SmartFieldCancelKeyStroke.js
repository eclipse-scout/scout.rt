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
/**
 * Closes the popup without accepting the proposal
 */
scout.SmartFieldCancelKeyStroke = function(field) {
  scout.SmartFieldCancelKeyStroke.parent.call(this);
  this.field = field;
  this.which = [scout.keys.ESC];
  this.stopPropagation = true;
  this.preventInvokeAcceptInputOnActiveValueField = true;

  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.$fieldContainer;
  }.bind(this);
};
scout.inherits(scout.SmartFieldCancelKeyStroke, scout.KeyStroke);

scout.SmartFieldCancelKeyStroke.prototype._accept = function(event) {
  var accepted = scout.SmartFieldCancelKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }
  if (!this.field.popup) {
    return false;
  }
  return true;
};

/**
 * @override
 */
scout.SmartFieldCancelKeyStroke.prototype.handle = function(event) {
  this.field.closePopup();
  this.field.resetDisplayText();
};
