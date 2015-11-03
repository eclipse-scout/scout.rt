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
scout.StringFieldCtrlEnterKeyStroke = function(stringField) {
  scout.StringFieldCtrlEnterKeyStroke.parent.call(this);
  this.field = stringField;
  this.which = [scout.keys.ENTER];
  this.ctrl = true;
};
scout.inherits(scout.StringFieldCtrlEnterKeyStroke, scout.KeyStroke);

scout.StringFieldCtrlEnterKeyStroke.prototype._accept = function(event) {
  var accepted = scout.StringFieldCtrlEnterKeyStroke.parent.prototype._accept.call(this, event);
  return accepted && this.field.hasAction;
};

scout.StringFieldCtrlEnterKeyStroke.prototype.handle = function(event) {
  this.field._onIconClick();
};
