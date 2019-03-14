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
scout.TabBoxHeader = function() {
  scout.TabBoxHeader.parent.call(this);

  this.tabBox = null;
  this.tabArea = null;
  this.menuBar = null;
  this.$container = null;
  this.htmlComp = null;
  this.$borderBottom = null;
  this._tabBoxPropertyChangeHandler = this._onTabBoxPropertyChange.bind(this);
  this._tabAreaPropertyChangeHandler = this._onTabAreaPropertyChange.bind(this);
};
scout.inherits(scout.TabBoxHeader, scout.Widget);

scout.TabBoxHeader.prototype._init = function(options) {
  scout.TabBoxHeader.parent.prototype._init.call(this, options);
  this.tabBox = options.tabBox;

  this.tabArea = scout.create('TabArea', {
    parent: this,
    tabBox: this.tabBox
  });
  this.tabArea.on('propertyChange', this._tabAreaPropertyChangeHandler);

  this.menuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.GroupBoxMenuItemsOrder()
  });
  // tabbox listener
  this.tabBox.on('propertyChange', this._tabBoxPropertyChangeHandler);

  this.menuBar.setMenuItems(this.tabBox.menus);
  this.htmlComp = new scout.HtmlComponent(null, this.session);
};

scout.TabBoxHeader.prototype._render = function() {
  this.$container = this.$parent.appendDiv('tab-box-header');
  this.$borderBottom = this.$container.appendDiv('tab-box-header-bottom-border');
//  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.bind(this.$container);
  this.htmlComp.setLayout(new scout.TabBoxHeaderLayout(this));
  this.tabArea.render(this.$container);
  this.menuBar.render(this.$container);
  this.$container.append(this.menuBar.$container);
};

scout.TabBoxHeader.prototype._destroy = function() {
  this.tabBox.off('propertyChange', this._tabBoxPropertyChangeHandler);
  this.tabArea.off('propertyChange', this._tabAreaPropertyChangeHandler);
  scout.TabBoxHeader.parent.prototype._destroy.call(this);
};

scout.TabBoxHeader.prototype.setTabItems = function(tabItems) {
  this.tabArea.setTabItems(tabItems);
};

scout.TabBoxHeader.prototype._setSelectedTab = function(tab) {
  if (tab) {
    this.setSelectedTabItem(tab.tabItem);
  } else {
    this.setSelectedTabItem(null);
  }
};

scout.TabBoxHeader.prototype.setSelectedTabItem = function(tabItem) {
  this.setProperty('selectedTabItem', tabItem);
};
scout.TabBoxHeader.prototype._setSelectedTabItem = function(tabItem) {
  this._setProperty('selectedTabItem', tabItem);
  this.tabArea.setSelectedTabItem(tabItem);
};

scout.TabBoxHeader.prototype.focusTabItem = function(tabItem) {
  this.tabArea.focusTabItem(tabItem);
};

scout.TabBoxHeader.prototype._onTabBoxPropertyChange = function(event) {
  if (event.propertyName === 'menus') {
    this.menuBar.setMenuItems(this.tabBox.menus);
  }
};

scout.TabBoxHeader.prototype._onTabAreaPropertyChange = function(event) {
  if (event.propertyName === 'selectedTab') {
    this._setSelectedTab(event.newValue);
  }
};
