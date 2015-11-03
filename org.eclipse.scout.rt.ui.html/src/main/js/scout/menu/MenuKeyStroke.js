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
scout.MenuKeyStroke = function(action) {
  scout.MenuKeyStroke.parent.call(this, action);
};
scout.inherits(scout.MenuKeyStroke, scout.ActionKeyStroke);

scout.MenuKeyStroke.prototype._isEnabled = function() {
  if (this.field.excludedByFilter) {
    return false;
  } else {
    return scout.MenuKeyStroke.parent.prototype._isEnabled.call(this);
  }
};
