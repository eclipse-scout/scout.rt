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
scout.TabArea = function() {
  scout.TabArea.parent.call(this);
  this.tabBox = null;
  this.tabItems = [];

  this._tabPropertyChangeHandler = this._onTabPropertyChange.bind(this);
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
  this._renderTabItems();
  this._renderHasSubLabel();
};

scout.TabArea.prototype.setSelectedTab = function(tabItem) {
  this.tabBox.setSelectedTab(tabItem);
  this.setProperty('selectedTab', tabItem);
  this._setTabbableItem(tabItem);
};

scout.TabArea.prototype._renderSelectedTab = function() {
  // force a relayout in case the selected tab is overflown. The layout will ensure the selected tab is visible.
  if (this.selectedTab && this.selectedTab.tabOverflown) {
    this.invalidateLayoutTree();
  }
};

/**
 * @override FormField.js
 */
scout.TabArea.prototype._remove = function() {
  scout.TabArea.parent.prototype._remove.call(this);
  this._removeTabs();
};

scout.TabArea.prototype._removeTabs = function() {
  this.tabItems.forEach(function(tabItem) {
    tabItem.removeTab();
  }, this);
};

scout.TabArea.prototype._removeTabHandlers = function() {
  if (this.tabItems) {
    this.tabItems.forEach(function(item) {
      item.off('propertyChange', this._tabPropertyChangeHandler);
    }.bind(this));
  }
};

scout.TabArea.prototype.setTabItems = function(tabItems) {
  this._removeTabHandlers();
  if (this.rendered) {
    this._removeTabItems();
  }
  tabItems.forEach(function(item) {
    item.on('propertyChange', this._tabPropertyChangeHandler);
  }, this);

  this.setProperty('tabItems', tabItems);
  this._updateHasSubLabel();
  this.invalidateLayoutTree();
};

scout.TabArea.prototype._renderTabItems = function() {
  this.tabItems.slice().reverse().forEach(function(tabItem, index, items) {
    tabItem.renderTab(this.$container);
    tabItem.$tabContainer.prependTo(this.$container);
    tabItem.$tabContainer.on('blur', this._onTabItemBlur.bind(this))
      .on('focus', this._onTabItemFocus.bind(this));
  }, this);
};

scout.TabArea.prototype._onTabItemFocus = function() {
  this.setFocused(true);
};

scout.TabArea.prototype._onTabItemBlur = function() {
  this.setFocused(false);
};

scout.TabArea.prototype._removeTabItems = function() {
  this.tabItems.forEach(function(tabItem) {
    tabItem.removeTab();
  }, this);
};

scout.TabArea.prototype._updateHasSubLabel = function() {
  var items = this.tabItems || [];
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
  var currentIndex = this.tabItems.indexOf(this.selectedTab),
    nextTab;
  if (this.tabItems.length > currentIndex + 1) {
    nextTab = this.tabItems[currentIndex + 1];
    this.setSelectedTab(nextTab);
    nextTab.focus();
  }
};

scout.TabArea.prototype.selectPreviousTab = function() {
  var currentIndex = this.tabItems.indexOf(this.selectedTab),
    previousTab;
  if (currentIndex - 1 > -1) {
    previousTab = this.tabItems[currentIndex - 1];
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
  var tabItems = this.tabItems.slice(),
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
        item.focusTab();
      }
      selectNext = false;
      return;
    }
    if ($focusedElement[0] === item.$tabContainer[0]) {
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
  var tabItems = this.tabItems;
  if (tabItem) {
    // clear old tabbable
    this.ellipsis.setTabbable(false);
    tabItems.forEach(function(item) {
      item.setTabbable(false);
    });
    tabItem.setTabbable(true);
  }
};

scout.TabArea.prototype._onTabPropertyChange = function(event) {
  if (event.propertyName === 'visible') {
    this.invalidateLayoutTree();
  }
  if (event.propertyName === 'subLabel') {
    this._updateHasSubLabel();
  }
};
