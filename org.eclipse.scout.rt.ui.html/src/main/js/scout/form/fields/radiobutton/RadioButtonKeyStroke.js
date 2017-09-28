/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.RadioButtonKeyStroke = function(button, keyStroke) {
  scout.RadioButtonKeyStroke.parent.call(this, button, keyStroke);
};
scout.inherits(scout.RadioButtonKeyStroke, scout.ButtonKeyStroke);

/**
 * @override ButtonKeyStroke.js
 *
 * To not prevent a parent key stroke context from execution of the event, the key stroke event is only accepted if the radio button is not selected.
 */
scout.RadioButtonKeyStroke.prototype._accept = function(event) {
  var accepted = scout.RadioButtonKeyStroke.parent.prototype._accept.call(this, event);
  return accepted && !this.field.selected;
};
