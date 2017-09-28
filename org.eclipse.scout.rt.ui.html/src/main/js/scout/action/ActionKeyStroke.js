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
scout.ActionKeyStroke = function(action) {
  scout.ActionKeyStroke.parent.call(this);
  this.field = action;
  this.parseAndSetKeyStroke(action.keyStroke);
  this.stopPropagation = true;
  this.stopImmediatePropagation = false;
};
scout.inherits(scout.ActionKeyStroke, scout.KeyStroke);

scout.ActionKeyStroke.prototype._isEnabled = function() {
  if (!this.which.length) {
    return false; // actions without a keystroke are not enabled.
  } else {
    return scout.ActionKeyStroke.parent.prototype._isEnabled.call(this);
  }
};

scout.ActionKeyStroke.prototype.handle = function(event) {
  this.field.doAction();
};
