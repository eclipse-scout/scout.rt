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
scout.TabItemLayout = function(tabItem) {
  scout.TabItemLayout.parent.call(this, tabItem);
};
scout.inherits(scout.TabItemLayout, scout.GroupBoxLayout);

scout.TabItemLayout.prototype._layoutStatus = function() {
  // NOP: $status width is set in TabItem.addStatus()
};

scout.TabItemLayout.prototype._menuBarSize = function(htmlMenuBar, containerSize, statusWidth) {
  var menuBarSize = scout.MenuBarLayout.size(htmlMenuBar, containerSize);
  var tabBox = this._groupBox.parent;
  menuBarSize.width = tabBox._$tabArea.outerWidth();
  return menuBarSize;
};
