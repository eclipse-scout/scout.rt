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
  this._addAdapterProperties(['tabItems']);
  this.selectedTab;
  this._$tabArea;
  this._$tabContent;
};
scout.inherits(scout.TabBox, scout.CompositeField);

/**
 * @override FormField.js
 */
scout.TabBox.prototype._init = function(model) {
  scout.TabBox.parent.prototype._init.call(this, model);
  this.tabItems[this.selectedTab].setTabActive(true);
  this.menuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.GroupBoxMenuItemsOrder()
  });
};

scout.TabBox.prototype._render = function($parent) {
  this.addContainer($parent, 'tab-box', new scout.TabBoxLayout(this));

  this._$tabArea = this.$container
    .appendDiv('tab-area')
    .on('keydown', this._onKeyDown.bind(this));
  this.addPseudoStatus();
  var htmlComp = new scout.HtmlComponent(this._$tabArea, this.session);
  htmlComp.setLayout(new scout.TabAreaLayout(this));

  this.menuBar.render(this._$tabArea);

  this._$tabContent = this.$container.appendDiv('tab-content');
  htmlComp = new scout.HtmlComponent(this._$tabContent, this.session);
  htmlComp.setLayout(new scout.SingleLayout());
};

/**
 * @override FormField.js
 */
scout.TabBox.prototype._renderProperties = function() {
  scout.TabBox.parent.prototype._renderProperties.call(this);
  this._renderTabs();
  this._renderTabContent();
  this._renderMenus();
};

/**
 * @override FormField.js
 */
scout.TabBox.prototype._remove = function() {
  scout.TabBox.parent.prototype._remove.call(this);
  this._removeTabs();
  this._removeTabContent();
  if (this.menuBar) {
    this.menuBar.remove();
  }
};

/**
 * Must call _selectTab(), the method sets the this.selectedTab property
 * and renders the new tab/content.
 */
scout.TabBox.prototype._syncSelectedTab = function(selectedTab) {
  this._selectTab(this.tabItems[selectedTab], false);
};

scout.TabBox.prototype._renderSelectedTab = function() {
  // NOP - already handled by _syncSelectedTab
};

scout.TabBox.prototype._renderTabs = function() {
  this.tabItems.forEach(function(tabItem) {
    tabItem.renderTab(this._$tabArea);
  }, this);
};

scout.TabBox.prototype._removeTabs = function() {
  this.tabItems.forEach(function(tabItem) {
    tabItem.removeTab();
  }, this);
};

scout.TabBox.prototype._removeTabContent = function() {
  this.tabItems.forEach(function(tabItem) {
    tabItem.remove();
  }, this);
};

scout.TabBox.prototype.rebuildTabs = function() {
  // FIXME AWE: (tab-box) refactor this and work with a clone in the TabBoxLayout - when we remove an existing
  // DOM element which currently has the focus - the focus is lost. An other solution would be, to render the
  // tab at the correct position but probably that's not so easy because the render impl. does always append.
  this.tabItems.forEach(function(tabItem) {
    if (tabItem._tabRendered) {
      tabItem.removeTab();
    }
  }, this);
  this._renderTabs();
};

scout.TabBox.prototype._selectTab = function(tabItem, notifyServer) {
  var tabIndex = this.tabItems.indexOf(tabItem);
  if (tabIndex !== this.selectedTab) {
    $.log.debug('(TabBox#_selectTab) tabItem=' + tabItem + ' tabIndex=' + tabIndex);
    var oldSelectedTab = this.selectedTab;
    this.selectedTab = tabIndex;
    if (scout.nvl(notifyServer, true)) {
      this._send('selected', {
        tabIndex: tabIndex
      });
    }

    var oldSelectedTabItem = this.tabItems[oldSelectedTab],
      selectedTabItem = this.tabItems[this.selectedTab];

    oldSelectedTabItem.setTabActive(false);
    selectedTabItem.setTabActive(true);

    if (this.rendered) {
      // revalidateLayoutTree layout when tab-area has changed, because overflow tabs must be re-arranged
      if (!selectedTabItem._tabRendered) {
        scout.HtmlComponent.get(this._$tabArea).revalidateLayoutTree();
      }
      selectedTabItem.focusTab();
      oldSelectedTabItem.detach();
      this._renderTabContent();
    }
  }
};

// keyboard navigation in tab-box button area
// FIXME AWE: (tab-box) overflow menu must be accessible by keyboard navigation
scout.TabBox.prototype._onKeyDown = function(event) {
  var tabIndex, navigationKey =
    event.which === scout.keys.LEFT ||
    event.which === scout.keys.RIGHT;

  if (!navigationKey) {
    return true;
  }

  event.preventDefault();
  event.stopPropagation();
  tabIndex = this._getNextVisibleTabIndexForKeyStroke(this.selectedTab, event.which);

  if (tabIndex >= 0 && tabIndex < this.tabItems.length) {
    var tabItem = this.tabItems[tabIndex];
    if (tabItem._tabRendered) {
      this._selectTab(tabItem);
    }
  }
};

scout.TabBox.prototype._getNextVisibleTabIndexForKeyStroke = function(actualIndex, keyStroke) {
  var i, tabItem,
    modifier = keyStroke === scout.keys.LEFT ? -1 : 1,
    endFunc = function(i) {
      if (keyStroke === scout.keys.LEFT) {
        return i >= 0;
      }
      return i < this.tabItems.length;
    }.bind(this);
  for (i = actualIndex + modifier; endFunc(i); i = i + modifier) {
    tabItem = this.tabItems[i];
    if (tabItem.visible) {
      return i;
    }
  }
  return actualIndex;
};

scout.TabBox.prototype._renderTabContent = function() {
  // add new tab-content (use from cache or render)
  var selectedTabItem = this.tabItems[this.selectedTab];
  if (selectedTabItem.rendered && !selectedTabItem.attached) {
    selectedTabItem.attach();
  } else {
    // in Swing there's some complicated logic dealing with borders and labels
    // that determines whether the first group-box in a tab-box has a title or not.
    // I decided to simply this and always set the title of the first group-box
    // to invisible.
    selectedTabItem.render(this._$tabContent);
    selectedTabItem._renderLabelVisible(false);
  }
  if (this.rendered) {
    scout.HtmlComponent.get(this._$tabContent).revalidateLayoutTree();
  }
};

scout.TabBox.prototype._renderMenus = function() {
  this.updateMenuBar();
  // TODO BSH Key strokes?
};

scout.TabBox.prototype.updateMenuBar = function() {
  var menuItems = scout.menus.filter(this.menus, ['TabBox.Header']);
  this.menuBar.updateItems(menuItems);
};

/**
 * @override CompositeField.js
 */
scout.TabBox.prototype.getFields = function() {
  return this.tabItems;
};

