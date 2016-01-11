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
scout.ButtonKeyStroke = function(button, keyStroke) {
  scout.ButtonKeyStroke.parent.call(this);
  this.field = button;
  this.parseAndSetKeyStroke(keyStroke);
  this.stopPropagation = true;

  this.renderingHints.offset = 4;
  this.renderingHints.hAlign = scout.hAlign.RIGHT;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.$container;
  }.bind(this);
};
scout.inherits(scout.ButtonKeyStroke, scout.KeyStroke);

/**
 * @override KeyStroke.js
 */
scout.ButtonKeyStroke.prototype._accept = function(event) {
  var accepted = scout.ButtonKeyStroke.parent.prototype._accept.call(this, event);
  return accepted && this.field.$field.isAttached();
};

/**
 * @override KeyStroke.js
 */
scout.ButtonKeyStroke.prototype.handle = function(event) {
  this.field.doAction(event);
};
