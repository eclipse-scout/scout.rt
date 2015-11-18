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
scout.ClickActiveElementKeyStroke = function(field, which) {
  scout.ClickActiveElementKeyStroke.parent.call(this);
  this.field = field;
  this.which = which;
  this.stopPropagation = true;
  this.renderingHints.render = true;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return event._$activeElement;
  };
};
scout.inherits(scout.ClickActiveElementKeyStroke, scout.KeyStroke);

scout.ClickActiveElementKeyStroke.prototype._accept = function(event) {
  var accepted = scout.ClickActiveElementKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }

  event._$activeElement = this.field.$container.getActiveElement();
  return true;
};

/**
 * @override KeyStroke.js
 */
scout.ClickActiveElementKeyStroke.prototype.handle = function(event) {
  event._$activeElement.trigger({
    type: 'click',
    which: 1
  });
};
