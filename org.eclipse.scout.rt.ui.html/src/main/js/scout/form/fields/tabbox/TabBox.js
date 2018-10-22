/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Tab-area = where the 1-n tabs are placed (may have multiple runs = lines).
 * Tab-content = where the content of a single tab is displayed.
 */
scout.TabBox = function() {
  scout.TabBox.parent.call(this);

  this.gridDataHints.useUiHeight = true;
  this.gridDataHints.w = scout.FormField.FULL_WIDTH;
  this.menusVisible = false; // TabBox shows its menus in the tab box header -> don't draw an ellipsis status icon
  this.selectedTab = null;
  this.tabItems = [];
  this._addWidgetProperties(['tabItems', 'selectedTab']);
  this._addPreserveOnPropertyChangeProperties(['selectedTab']); // TODO [7.0] awe: do this in Calendar too, for selectedComponent
  this._$tabContent = null;

  this._tabBoxHeaderPropertyChangeHander = this._onTabBoxHeaderPropertyChange.bind(this);
};
scout.inherits(scout.TabBox, scout.CompositeField);

/**
 * @override FormField.js
 */
scout.TabBox.prototype._init = function(model) {
  scout.TabBox.parent.prototype._init.call(this, model);
  this.header = scout.create('TabBoxHeader', {
    parent: this,
    tabBox: this
  });

  this._initProperties(model);
  this.header.on('propertyChange', this._tabBoxHeaderPropertyChangeHander);
};

scout.TabBox.prototype._initProperties = function(model) {
  this._setTabItems(this.tabItems);
  this._setSelectedTab(this.selectedTab);
};

scout.TabBox.prototype._destroy = function() {
  scout.TabBox.parent.prototype._destroy.call(this);
  this.header.off('propertyChange', this._tabBoxHeaderPropertyChangeHander);
};

scout.TabBox.prototype._render = function() {
  this.addContainer(this.$parent, 'tab-box', new scout.TabBoxLayout(this));

  this.header.render(this.$container);
  this.addStatus();

  this._$tabContent = this.$container.appendDiv('tab-content');
  var htmlCompContent = scout.HtmlComponent.install(this._$tabContent, this.session);
  htmlCompContent.setLayout(new scout.SingleLayout());
};

/**
 * @override FormField.js
 */
scout.TabBox.prototype._renderProperties = function() {
  scout.TabBox.parent.prototype._renderProperties.call(this);
  this._renderSelectedTab();
};

/**
 * @override FormField.js
 */
scout.TabBox.prototype._remove = function() {
  scout.TabBox.parent.prototype._remove.call(this);
  this._removeSelectedTab();
};

scout.TabBox.prototype._getCurrentMenus = function() {
  // handled by the menubar
  return [];
};

scout.TabBox.prototype._removeMenus = function() {
  // menubar takes care about removal
};

scout.TabBox.prototype.deleteTabItem = function(tabItem) {
  var index = this.tabItems.indexOf(tabItem);
  var newTabItems = this.tabItems.slice();
  if (index >= 0) {
    newTabItems.splice(index, 1);
    this.setTabItems(newTabItems);
  }
};

scout.TabBox.prototype.insertTabItem = function(tabItem, index) {
  if (!tabItem) {
    return;
  }
  index = scout.nvl(index, this.tabItems.length);
  var newTabItems = this.tabItems.slice();
  newTabItems.splice(index, 0, tabItem);
  this.setTabItems(newTabItems);
};

scout.TabBox.prototype.setTabItems = function(tabItems) {
  this.setProperty('tabItems', tabItems);
};

scout.TabBox.prototype._setTabItems = function(tabItems) {
  tabItems = tabItems || [];
  var tabsToRemove = this.tabItems || [];
  tabsToRemove.filter(function(tabItem) {
    return tabItems.indexOf(tabItem) < 0;
  }, this).forEach(function(tabItem) {
    tabItem.remove();
  });

  this._setProperty('tabItems', tabItems);
  this.header.setTabItems(this.tabItems);
  // if no tab is selected select first
  if (this.tabItems.indexOf(this.selectedTab) < 0) {
    this.setSelectedTab(this.tabItems[0]);
  }
};

scout.TabBox.prototype._renderTabItems = function(tabItems) {
  // void only selected tab is rendered
};
scout.TabBox.prototype._removeTabItems = function(tabItems) {
  // void only selected tab is rendered
};

scout.TabBox.prototype._removeTabContent = function() {
  this.tabItems.forEach(function(tabItem) {
    tabItem.remove();
  }, this);
};

scout.TabBox.prototype.selectTabById = function(tabId) {
  var tab = this.getTabItem(tabId);
  if (!tab) {
    throw new Error('Tab with ID \'' + tabId + '\' does not exist');
  }
  this.setSelectedTab(tab);
};

scout.TabBox.prototype.setSelectedTab = function(tabItem) {
  this.setProperty('selectedTab', tabItem);
};

scout.TabBox.prototype._setSelectedTab = function(tabItem) {
  $.log.isDebugEnabled() && $.log.debug('(TabBox#_selectTab) tab=' + tabItem);
  if (this.selectedTab && this.selectedTab.rendered) {
    this.selectedTab.remove();
  }
  this._setProperty('selectedTab', tabItem);
  this.header.setSelectedTabItem(this.selectedTab);
};

scout.TabBox.prototype._renderSelectedTab = function() {
  if (this.selectedTab) {
    this.selectedTab.render(this._$tabContent);
  }
  if (this.rendered) {
    scout.HtmlComponent.get(this._$tabContent).revalidateLayoutTree();
  }
};

scout.TabBox.prototype._removeSelectedTab = function() {
  this.selectedTab.remove();
};

/**
 * @override FormField.js
 */
scout.TabBox.prototype._renderStatusPosition = function() {
  scout.TabBox.parent.prototype._renderStatusPosition.call(this);
  if (!this.fieldStatus) {
    return;
  }
  if (this.statusPosition === scout.FormField.StatusPosition.TOP) {
    // move into title
    this.$status.appendTo(this.header.$container);
  } else {
    this.$status.appendTo(this.$container);
  }
  this.invalidateLayoutTree();
};

/**
 * @override CompositeField.js
 */
scout.TabBox.prototype.getFields = function() {
  return this.tabItems;
};

scout.TabBox.prototype.getTabItem = function(tabId) {
  return scout.arrays.find(this.tabItems, function(tabItem) {
    return tabItem.id === tabId;
  });
};

/**
 * @override FormField.js
 */
scout.TabBox.prototype.focus = function() {
  if (!this.rendered) {
    this.session.layoutValidator.schedulePostValidateFunction(this.focus.bind(this));
    return false;
  }
  if (this.selectedTab) {
    return this.selectedTab.focus();
  }
};

/**
 * @override
 */
scout.TabBox.prototype.getFocusableElement = function() {
  if (this.selectedTab) {
    return this.selectedTab.getFocusableElement();
  }
  return null;
};

scout.TabBox.prototype.focusTab = function(tab) {
  if (this.selectedTab !== tab) {
    this.selectTab(tab);
  }
  this.header.focusTabItem(tab);
};

scout.TabBox.prototype._onTabBoxHeaderPropertyChange = function(event) {
  if (event.propertyName === 'selectedTabItem') {
    this.setSelectedTab(event.newValue);
  }
};
