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
scout.TagFieldDeleteKeyStroke = function(fieldOrPopup) {
  scout.TagFieldDeleteKeyStroke.parent.call(this);
  this.field = new scout._TagFieldAdapter(fieldOrPopup);
  this.which = [scout.keys.DELETE];
  this.renderingHints.render = false;
  this.preventDefault = false;
};
scout.inherits(scout.TagFieldDeleteKeyStroke, scout.KeyStroke);

scout.TagFieldDeleteKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TagFieldDeleteKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }
  return this._$focusedTag().length > 0;
};

scout.TagFieldDeleteKeyStroke.prototype.handle = function(event) {
  var $tag = this._$focusedTag();
  var tag = scout.TagBar.getTagData($tag);
  this.field.removeTag(tag);
};

scout.TagFieldDeleteKeyStroke.prototype._$focusedTag = function() {
  return scout.TagBar.findFocusedTagElement(this.field.$container());
};
