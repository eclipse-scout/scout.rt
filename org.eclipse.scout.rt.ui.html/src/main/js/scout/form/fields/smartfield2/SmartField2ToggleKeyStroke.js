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
scout.SmartField2ToggleKeyStroke = function(field) {
  scout.SmartField2ToggleKeyStroke.parent.call(this);
  this.field = field;
  this.which = [scout.keys.SPACE, scout.keys.ENTER];
  this.stopPropagation = true;

  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.$fieldContainer;
  }.bind(this);
};
scout.inherits(scout.SmartField2ToggleKeyStroke, scout.KeyStroke);

/**
 * @override
 */
scout.SmartField2ToggleKeyStroke.prototype.handle = function(event) {
  if (this.field.popup) {
    this.field.acceptInput();
  } else {
    this.field.openPopup();
  }
};
