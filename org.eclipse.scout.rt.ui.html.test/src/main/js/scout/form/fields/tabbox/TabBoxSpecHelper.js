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
scout.TabBoxSpecHelper = function(session) {
  this.session = session;
};

scout.TabBoxSpecHelper.prototype.createTabBoxWith2Tabs = function(model) {
  model = $.extend({
    tabItems: [{
      objectType: "TabItem",
      label: "first"
    }, {
      objectType: "TabItem",
      label: "second"
    }]
  }, model);
  return this.createTabBox(model);
};

scout.TabBoxSpecHelper.prototype.createTabBoxWith = function(tabItems) {
  tabItems = scout.nvl(tabItems, []);
  return this.createTabBox({
    tabItems: tabItems,
    selectedTab: tabItems[0]
  });
};

scout.TabBoxSpecHelper.prototype.createTabBox = function(model) {
  model = $.extend({
    parent: this.session.desktop
  }, model);

  return scout.create('TabBox', model);
};

scout.TabBoxSpecHelper.prototype.createTabItem = function(model) {
  model = $.extend({
    parent: this.session.desktop
  }, model);
  return scout.create('TabItem', model);
};
