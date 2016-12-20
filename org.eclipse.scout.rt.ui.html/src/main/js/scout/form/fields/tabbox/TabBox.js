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
/**
 * Tab-area = where the 1-n tabs are placed (may have multiple runs = lines).
 * Tab-content = where the content of a single tab is displayed.
 */
scout.TabBox = function() {
  scout.TabBox.parent.call(this);

  this.gridDataHints.useUiHeight = true;
  this.gridDataHints.w = scout.FormField.FULL_WIDTH;
  this.selectedTab;
  this._addAdapterProperties(['tabItems', 'selectedTab']);
  this._addPreserveOnPropertyChangeProperties(['selectedTab']); // FIXME [awe] 6.1 - do this in Calendar too, for selectedComponent
  this._$tabArea;
  this._$tabContent;

  this._tabItemPropertyChangeHandler = function(event) {
    var numProperties = event.changedProperties.length;
    if (numProperties === 1 && event.changedProperties[0] === 'enabled') {
      // Optimization: don't invalidate layout when only the enabled state has changed (this should not affect the layout).
      return;
    }
    if (numProperties > 0) {
      scout.HtmlComponent.get(this._$tabArea).invalidateLayoutTree();
    }
  }.bind(this);
};
scout.inherits(scout.TabBox, scout.CompositeField);

/**
 * @override FormField.js
 */
scout.TabBox.prototype._init = function(model) {
  scout.TabBox.parent.prototype._init.call(this, model);
  if (this.selectedTab) {
    this.selectedTab.setTabActive(true);
  }
  this.menuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.GroupBoxMenuItemsOrder()
  });
  this._updateMenuBar(this.menus);
};

scout.TabBox.prototype._render = function($parent) {
  this.addContainer($parent, 'tab-box', new scout.TabBoxLayout(this));

  this._$tabArea = this.$container
    .appendDiv('tab-area')
    .on('keydown', this._onKeyDown.bind(this));
  this.addStatus();
  var htmlAreaComp = scout.HtmlComponent.install(this._$tabArea, this.session);
  htmlAreaComp.setLayout(new scout.TabAreaLayout(this));

  this.menuBar.render(this._$tabArea);

  this._$tabContent = this.$container.appendDiv('tab-content');
  var htmlCompContent = scout.HtmlComponent.install(this._$tabContent, this.session);
  htmlCompContent.setLayout(new scout.SingleLayout());
};

/**
 * @override FormField.js
 */
scout.TabBox.prototype._renderProperties = function() {
  scout.TabBox.parent.prototype._renderProperties.call(this);
  this._renderTabs();
  this._renderTabContent();
};

/**
 * @override FormField.js
 */
scout.TabBox.prototype._remove = function() {
  scout.TabBox.parent.prototype._remove.call(this);
  this._removeTabs();
  this._removeTabContent();
};

scout.TabBox.prototype._renderTabs = function() {
  this.tabItems.forEach(function(tabItem) {
    tabItem.renderTab(this._$tabArea);
    tabItem.on('propertyChange', this._tabItemPropertyChangeHandler);
  }, this);
};

scout.TabBox.prototype._removeTabs = function() {
  this.tabItems.forEach(function(tabItem) {
    tabItem.removeTab();
    tabItem.off('propertyChange', this._tabItemPropertyChangeHandler);
  }, this);
};

scout.TabBox.prototype._removeTabContent = function() {
  this.tabItems.forEach(function(tabItem) {
    tabItem.remove();
  }, this);
};

scout.TabBox.prototype.rebuildTabs = function() {
  // FIXME awe: (tab-box) refactor this and work with a clone in the TabBoxLayout - when we remove an existing
  // DOM element which currently has the focus - the focus is lost. An other solution would be, to render the
  // tab at the correct position but probably that's not so easy because the render impl. does always append.
  // Temporary focus fix
  var $focusedElement = $(document.activeElement),
    focusedElement = null;
  if ($focusedElement.is('.tab-item')) {
    focusedElement = $focusedElement.data('tabItem');
  }
  this.tabItems.forEach(function(tabItem) {
    if (tabItem._tabRendered) {
      tabItem.removeTab();
    }
  }, this);
  this._renderTabs();
  if (focusedElement) {
    this.session.focusManager.requestFocus(focusedElement.$tabContainer);
  }
};

scout.TabBox.prototype.selectTabById = function(tabId) {
  var tab = this.getTabItem(tabId);
  if (!tab) {
    throw new Error('Tab with ID \'' + tabId + '\' does not exist');
  }
  this.setSelectedTab(tab);
};

scout.TabBox.prototype.setSelectedTab = function(selectedTab) {
  this.setProperty('selectedTab', selectedTab);
};

scout.TabBox.prototype._setSelectedTab = function(tab, notifyServer) {
  if (this.selectedTab === tab) {
    return;
  }
  $.log.debug('(TabBox#_selectTab) tab=' + tab);
  var oldSelectedTab = this.selectedTab;
  var selectedTab = tab;
  if (oldSelectedTab) {
    oldSelectedTab.setTabActive(false);
  }
  if (selectedTab) {
    selectedTab.setTabActive(true);
  }
  this._setProperty('selectedTab', selectedTab);

  if (this.rendered) {
    // revalidateLayoutTree layout when tab-area has changed, because overflow tabs must be re-arranged
    if (!this.selectedTab || !this.selectedTab._tabRendered) {
      scout.HtmlComponent.get(this._$tabArea).revalidateLayoutTree();
    }
    if (this.selectedTab) {
      this.selectedTab.focusTab();
    }
    if (oldSelectedTab) {
      oldSelectedTab.detach();
    }
    this._renderTabContent();
  }
};

// keyboard navigation in tab-box button area
// TODO awe: (tab-box) overflow menu should be accessible by keyboard navigation
scout.TabBox.prototype._onKeyDown = function(event) {
  var navigationKey =
    event.which === scout.keys.LEFT ||
    event.which === scout.keys.RIGHT;
  if (!navigationKey) {
    return true;
  }

  event.preventDefault();
  event.stopPropagation();

  var nextTab = this._getNextVisibleTabForKeyStroke(event.which);
  if (nextTab && nextTab._tabRendered) {
    this.setSelectedTab(nextTab);
  }
};

scout.TabBox.prototype._getNextVisibleTabForKeyStroke = function(keyStroke) {
  var nextTab,
    dir = (keyStroke === scout.keys.LEFT) ? -1 : 1,
    i = this.tabItems.indexOf(this.selectedTab) + dir;

  while (i >= 0 && i < this.tabItems.length) {
    nextTab = this.tabItems[i];
    if (nextTab.visible) {
      return nextTab;
    }
    i += dir;
  }
  return null;
};

scout.TabBox.prototype._renderTabContent = function() {
  // add new tab-content (use from cache or render)
  var selectedTab = this.selectedTab;
  if (selectedTab) {
    if (selectedTab.rendered && !selectedTab.attached) {
      selectedTab.attach();
    } else {
      // in Swing there's some complicated logic dealing with borders and labels
      // that determines whether the first group-box in a tab-box has a title or not.
      // I decided to simplify this and always set the title of the first group-box
      // to invisible.
      selectedTab.render(this._$tabContent);
    }
  }
  if (this.rendered) {
    scout.HtmlComponent.get(this._$tabContent).revalidateLayoutTree();
  }
};

scout.TabBox.prototype._setMenus = function(menus) {
  scout.TabBox.parent.prototype._setMenus.call(this, menus);
  if (this.menuBar) {
    // updateMenuBar is required because menuBar is not created yet when synMenus is called initially
    this._updateMenuBar(menus);
  }
};

scout.TabBox.prototype._removeMenus = function() {
  // menubar takes care about removal
};

scout.TabBox.prototype._updateMenuBar = function(menus) {
  var menuItems = scout.menus.filter(this.menus, ['TabBox.Header']);
  this.menuBar.setMenuItems(menuItems);
};

scout.TabBox.prototype._renderStatusPosition = function() {
  if (this.statusPosition === scout.FormField.STATUS_POSITION_TOP) {
    // move into title
    this.$status.appendTo(this._$tabArea);
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
