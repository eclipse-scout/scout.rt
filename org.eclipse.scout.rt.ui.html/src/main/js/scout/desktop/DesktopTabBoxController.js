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

scout.DesktopTabBoxController = function() {
  scout.DesktopTabBoxController.parent.call(this);
};
scout.inherits(scout.DesktopTabBoxController, scout.SimpleTabBoxController);

scout.DesktopTabBoxController.prototype._createTab = function(view) {
  return scout.create('DesktopTab', {
    parent: this.tabArea,
    view: view
  });
};
