/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TagFieldCancelKeyStroke = function(field) {
  scout.TagFieldCancelKeyStroke.parent.call(this);
  this.field = field;
  this.which = [scout.keys.ESC];
  this.stopPropagation = true;
  this.preventInvokeAcceptInputOnActiveValueField = true;

  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.$fieldContainer;
  }.bind(this);
};
scout.inherits(scout.TagFieldCancelKeyStroke, scout.KeyStroke);

scout.TagFieldCancelKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TagFieldCancelKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }
  if (!this.field.chooser) {
    return false;
  }
  return true;
};

scout.TagFieldCancelKeyStroke.prototype.handle = function(event) {
  this.field.closeChooserPopup();
};
