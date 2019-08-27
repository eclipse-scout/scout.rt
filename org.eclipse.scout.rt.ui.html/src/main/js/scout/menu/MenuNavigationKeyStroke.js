/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.MenuNavigationKeyStroke = function(popup) {
  scout.MenuNavigationKeyStroke.parent.call(this);
  this.field = popup;
};
scout.inherits(scout.MenuNavigationKeyStroke, scout.KeyStroke);

scout.MenuNavigationKeyStroke.prototype._accept = function(event) {
  var accepted = scout.MenuNavigationKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted || this.field.bodyAnimating) {
    return false;
  }
  return accepted;
};
