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
scout.TagFieldOpenPopupKeyStroke = function(tagField) {
  scout.TagFieldOpenPopupKeyStroke.parent.call(this);
  this.field = tagField;
  this.which = [scout.keys.ENTER, scout.keys.SPACE];
  this.renderingHints.render = false;
  this.preventDefault = false;
};
scout.inherits(scout.TagFieldOpenPopupKeyStroke, scout.KeyStroke);

scout.TagFieldOpenPopupKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TagFieldDeleteKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }
  return this.field.isOverflowIconFocused();
};

scout.TagFieldOpenPopupKeyStroke.prototype.handle = function(event) {
  this.field.openOverflowPopup();
};
