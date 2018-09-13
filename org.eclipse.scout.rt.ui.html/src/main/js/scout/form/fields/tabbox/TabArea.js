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
scout.TabArea = function() {
  scout.TabArea.parent.call(this);
  this.tabBox = null;
  this.tabs = [];

  this._tabItemPropertyChangeHandler = this._onTabItemPropertyChange.bind(this);
  this._tabPropertyChangeHandler = this._onTabPropertyChange.bind(this);
  this._tabSelectionHandler = this._onTabSelect.bind(this);
  this.ellipsis = null;

  this.$selectionMarker = null;
};
scout.inherits(scout.TabArea, scout.Widget);

scout.TabArea.prototype._init = function(options) {
  scout.TabArea.parent.prototype._init.call(this, options);
  this.tabBox = options.tabBox;

  this.ellipsis = scout.create('EllipsisMenu', {
    parent: this,
    cssClass: 'overflow-tab-item unfocusable',
    iconId: null
  });
};

/**
 * @override
 */
scout.TabArea.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

/**
 * @override
 */
scout.TabArea.prototype._initKeyStrokeContext = function() {
  scout.TabArea.parent.prototype._initKeyStrokeContext.call(this);
  this.keyStrokeContext.registerKeyStroke([
    new scout.TabAreaLeftKeyStroke(this),
    new scout.TabAreaRightKeyStroke(this)
  ]);
};

scout.TabArea.prototype._render = function() {
  this.$container = this.$parent.appendDiv('tab-area');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.TabAreaLayout(this));

  this.ellipsis.render(this.$container);

  this.$selectionMarker = this.$container.appendDiv('selection-marker');
};

scout.TabArea.prototype._renderProperties = function() {
  scout.TabArea.parent.prototype._renderProperties.call(this);
  this._renderTabs();
  this._renderSelectedTab();
  this._renderHasSubLabel();
};

/**
 * @override FormField.js
 */
scout.TabArea.prototype._remove = function() {
  scout.TabArea.parent.prototype._remove.call(this);
  this._removeTabs();
};

scout.TabArea.prototype.setSelectedTabItem = function(tabItem) {
  this.setSelectedTab(scout.arrays.find(this.tabs, function(tab) {
    return tab.tabItem === tabItem;
  }, this));
};

scout.TabArea.prototype.setSelectedTab = function(tab) {
  this.setProperty('selectedTab', tab);
};

scout.TabArea.prototype._setSelectedTab = function(tab) {
  if (this.selectedTab) {
    this.selectedTab.setSelected(false);
  }
  if (tab) {
    tab.setSelected(true);
  }
  this._setProperty('selectedTab', tab);
  this._setTabbableItem(tab);
};

scout.TabArea.prototype._renderSelectedTab = function() {
  // force a relayout in case the selected tab is overflown. The layout will ensure the selected tab is visible.
  if (this.selectedTab && this.selectedTab.tabOverflown) {
    this.invalidateLayoutTree();
  }
};

scout.TabArea.prototype.focusTabItem = function(tabItem) {
  this.focusTab(scout.arrays.find(this.tabs, function(tab) {
    return tab.tabItem === tabItem;
  }, this));
};

scout.TabArea.prototype.focusTab = function(tabItem) {
  tabItem.focus();
};

scout.TabArea.prototype.setTabItems = function(tabItems) {
  this.setProperty('tabs', tabItems);
  this._updateHasSubLabel();
  this.invalidateLayoutTree();
};

scout.TabArea.prototype._setTabs = function(tabItems) {
  var tabsToRemove = this.tabs.slice(),
    tabs = tabItems.map(function(tabItem) {
      var tab = scout.arrays.find(this.tabs, function(tab) {
        return tab.tabItem === tabItem;
      });
      if (!tab) {
        tab = scout.create('Tab', {
          parent: this,
          tabItem: tabItem
        });
        tabItem.on('propertyChange', this._tabItemPropertyChangeHandler);
        tab.on('propertyChange', this._tabPropertyChangeHandler);
      } else {
        scout.arrays.remove(tabsToRemove, tab);
      }
      return tab;
    }, this);

  this._removeTabs(tabsToRemove);

  this._setProperty('tabs', tabs);
};

scout.TabArea.prototype._renderTabs = function() {
  this.tabs.slice().reverse().forEach(function(tab, index, items) {
    if (!tab.rendered) {
      tab.render();
    }
    tab.$container.on('blur', this._onTabItemBlur.bind(this))
      .on('focus', this._onTabItemFocus.bind(this));
    tab.$container.prependTo(this.$container);
    tab.$container.on('blur', this._onTabItemBlur.bind(this))
      .on('focus', this._onTabItemFocus.bind(this));
  }, this);
};

scout.TabArea.prototype._removeTabs = function(tabs) {
  tabs = tabs || this.tabs;
  tabs.forEach(function(tab) {
    tab.tabItem.off('propertyChange', this._tabItemPropertyChangeHandler);
    tab.off('select', this._tabSelectionHandler);
    tab.remove();
  }, this);
};

scout.TabArea.prototype._onTabItemFocus = function() {
  this.setFocused(true);
};

scout.TabArea.prototype._onTabItemBlur = function() {
  this.setFocused(false);
};

scout.TabArea.prototype._updateHasSubLabel = function() {
  var items = this.tabs || [];
  this._setHasSubLabel(items.some(function(item) {
    return scout.strings.hasText(item.subLabel);
  }));
};

scout.TabArea.prototype._setHasSubLabel = function(hasSubLabel) {
  if (this.hasSubLabel === hasSubLabel) {
    return;
  }
  this._setProperty('hasSubLabel', hasSubLabel);
  if (this.rendered) {
    this._renderHasSubLabel();
  }
};

scout.TabArea.prototype._renderHasSubLabel = function() {
  this.$container.toggleClass('has-sub-label', this.hasSubLabel);
  this.invalidateLayoutTree();
};

scout.TabArea.prototype.selectNextTab = function() {
  var currentIndex = this.tabs.indexOf(this.selectedTab),
    nextTab;
  if (this.tabss.length > currentIndex + 1) {
    nextTab = this.tabs[currentIndex + 1];
    this.setSelectedTab(nextTab);
    nextTab.focus();
  }
};

scout.TabArea.prototype.selectPreviousTab = function() {
  var currentIndex = this.tabs.indexOf(this.selectedTab),
    previousTab;
  if (currentIndex - 1 > -1) {
    previousTab = this.tabs[currentIndex - 1];
    this.setSelectedTab(previousTab);
    previousTab.focus();
  }
};

scout.TabArea.prototype.selectNextTab = function(focusTab) {
  this._moveSelectionHorizontal(true, focusTab);
};
scout.TabArea.prototype.selectPreviousTab = function(focusTab) {
  this._moveSelectionHorizontal(false, focusTab);
};

scout.TabArea.prototype._moveSelectionHorizontal = function(directionRight, focusTab) {
  var tabItems = this.tabs.slice(),
    $focusedElement = this.$container.activeElement(),
    selectNext = false;
  if (!directionRight) {
    tabItems.reverse();
    selectNext = $focusedElement[0] === this.ellipsis.$container[0];
  }

  tabItems.forEach(function(item, index) {
    if (selectNext && item.visible && !item.tabOverflown) {
      this.setSelectedTab(item);
      this._setTabbableItem(item);
      if (focusTab) {
        item.focus();
      }
      selectNext = false;
      return;
    }
    if ($focusedElement[0] === item.$container[0]) {
      selectNext = true;
    }
  }, this);

  if (directionRight && selectNext && this.ellipsis.isTabTarget()) {
    this._setTabbableItem(this.ellipsis);
    if (focusTab) {
      this.ellipsis.focus();
    }
  }
};

scout.TabArea.prototype._setTabbableItem = function(tabItem) {
  var tabItems = this.tabs;
  if (tabItem) {
    // clear old tabbable
    this.ellipsis.setTabbable(false);
    tabItems.forEach(function(item) {
      item.setTabbable(false);
    });
    tabItem.setTabbable(true);
  }
};

scout.TabArea.prototype._onTabSelect = function(event) {
  // translate tab into tabItem
  this.trigger('tabItemSelect', {
    tabItem: event.tab.tabItem
  });
};

scout.TabArea.prototype._onTabPropertyChange = function(event) {
  if (event.propertyName === 'selected') {
    this.setSelectedTab(event.source);
  }
};

scout.TabArea.prototype._onTabItemPropertyChange = function(event) {
  if (event.propertyName === 'visible') {
    this.invalidateLayoutTree();
  }
  if (event.propertyName === 'subLabel') {
    this._updateHasSubLabel();
  }
};
