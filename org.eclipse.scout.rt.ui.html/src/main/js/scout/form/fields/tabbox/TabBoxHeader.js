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
  this.tabArea.setTabItems(this.tabBox.tabItems);
  this.tabArea.setSelectedTab(this.tabBox.selectedTab);
};

scout.TabBoxHeader.prototype._render = function() {
  this.$container = this.$parent.appendDiv('tab-box-header');
  this.$borderBottom = this.$container.appendDiv('tab-box-header-bottom-border');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
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

scout.TabBoxHeader.prototype._onTabBoxPropertyChange = function(event) {
  if (event.propertyName === 'menus') {
    this.menuBar.setMenuItems(this.tabBox.menus);
  } else if (event.propertyName === 'tabItems') {
    this.tabArea.setTabItems(this.tabBox.tabItems);
  } else if (event.propertyName === 'selectedTab') {
    this.tabArea.setSelectedTab(this.tabBox.selectedTab);
  }
};

scout.TabBoxHeader.prototype._onTabAreaPropertyChange = function(event) {
  if (event.propertyName === 'selectedTab') {
    this.tabBox.setSelectedTab(this.tabArea.selectedTab);
  }
};
