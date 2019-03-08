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
  this.stopPropagation = true;
};
scout.inherits(scout.TagFieldEnterKeyStroke, scout.KeyStroke);

scout.TagFieldEnterKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TagFieldEnterKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }
  // set the stopPropagation flag dynamically. While the user is typing we only want the field
  // to apply the current displayText as tag, when the user presses ENTER. But when the displayText
  // is empty, the ENTER key should propagate up to the form.
  this.stopPropagation = scout.strings.hasText(this.field._readDisplayText());
  return this.field.isInputFocused();
};

scout.TagFieldEnterKeyStroke.prototype.handle = function(event) {
  this.field.acceptInput();
};
