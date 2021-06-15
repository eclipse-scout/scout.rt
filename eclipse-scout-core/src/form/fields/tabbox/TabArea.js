/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, HtmlComponent, KeyStrokeContext, scout, strings, TabAreaLayout, TabAreaLeftKeyStroke, TabAreaRightKeyStroke, Widget} from '../../../index';

export default class TabArea extends Widget {

  constructor() {
    super();
    this.tabBox = null;
    this.tabs = [];
    this.displayStyle = TabArea.DisplayStyle.DEFAULT;
    this.hasSubLabel = false;
    this.selectedTab = null;

    this._tabItemPropertyChangeHandler = this._onTabItemPropertyChange.bind(this);
    this._tabPropertyChangeHandler = this._onTabPropertyChange.bind(this);
    this.ellipsis = null;

    this.$selectionMarker = null;
  }

  static DisplayStyle = {
    DEFAULT: 'default',
    SPREAD_EVEN: 'spreadEven'
  };

  _init(options) {
    super._init(options);
    this.tabBox = options.tabBox;

    this.ellipsis = scout.create('EllipsisMenu', {
      parent: this,
      cssClass: 'overflow-tab-item unfocusable',
      iconId: null,
      inheritAccessibility: false,
      text: '0' // Initialize with the normal value to prevent unnecessary layout invalidation by the TabAreaLayout if ellipsis menus is not visible
    });
  }

  /**
   * @override
   */
  _createKeyStrokeContext() {
    return new KeyStrokeContext();
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.keyStrokeContext.registerKeyStroke([
      new TabAreaLeftKeyStroke(this),
      new TabAreaRightKeyStroke(this)
    ]);
  }

  _render() {
    this.$container = this.$parent.appendDiv('tab-area');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new TabAreaLayout(this));

    this.ellipsis.render(this.$container);

    this.$selectionMarker = this.$container.appendDiv('selection-marker');
  }

  _renderProperties() {
    super._renderProperties();
    this._renderTabs();
    this._renderSelectedTab();
    this._renderHasSubLabel();
    this._renderDisplayStyle();
  }

  /**
   * @override FormField.js
   */
  _remove() {
    super._remove();
    this._removeTabs();
  }

  setSelectedTabItem(tabItem) {
    this.setSelectedTab(this.getTabForItem(tabItem));
  }

  getTabForItem(tabItem) {
    return arrays.find(this.tabs, tab => tab.tabItem === tabItem);
  }

  setSelectedTab(tab) {
    this.setProperty('selectedTab', tab);
  }

  _setSelectedTab(tab) {
    if (this.selectedTab) {
      this.selectedTab.setSelected(false);
    }
    if (tab) {
      tab.setSelected(true);
    }
    this._setProperty('selectedTab', tab);
    this._setTabbableItem(tab);
  }

  _renderSelectedTab() {
    // force a relayout in case the selected tab is overflown. The layout will ensure the selected tab is visible.
    if (this.selectedTab && this.selectedTab.overflown) {
      this.invalidateLayoutTree();
    }
  }

  isTabItemFocused(tabItem) {
    return this.getTabForItem(tabItem).isFocused();
  }

  focusTabItem(tabItem) {
    this.focusTab(this.getTabForItem(tabItem));
  }

  focusTab(tabItem) {
    tabItem.focus();
  }

  setTabItems(tabItems) {
    this.setProperty('tabs', tabItems);
    this._updateHasSubLabel();
    this.invalidateLayoutTree();
  }

  _setTabs(tabItems) {
    let tabsToRemove = this.tabs.slice(),
      tabs = tabItems.map(tabItem => {
        let tab = this.getTabForItem(tabItem);
        if (!tab) {
          tab = scout.create('Tab', {
            parent: this,
            tabItem: tabItem
          });
          tabItem.on('propertyChange', this._tabItemPropertyChangeHandler);
          tab.on('propertyChange', this._tabPropertyChangeHandler);
        } else {
          arrays.remove(tabsToRemove, tab);
        }
        return tab;
      });

    // un-register model listeners
    tabsToRemove.forEach(function(tab) {
      tab.tabItem.off('propertyChange', this._tabItemPropertyChangeHandler);
    }, this);

    this._removeTabs(tabsToRemove);
    this._setProperty('tabs', tabs);
  }

  _renderTabs() {
    this.tabs.slice().reverse().forEach((tab, index, items) => {
      if (!tab.rendered) {
        tab.render();
      }
      tab.$container
        .on('blur', this._onTabItemBlur.bind(this))
        .on('focus', this._onTabItemFocus.bind(this));
      tab.$container.prependTo(this.$container);
      tab.$container
        .on('blur', this._onTabItemBlur.bind(this))
        .on('focus', this._onTabItemFocus.bind(this));
    });
  }

  _removeTabs(tabs) {
    tabs = tabs || this.tabs;
    tabs.forEach(tab => {
      tab.remove();
    });
  }

  setDisplayStyle(displayStyle) {
    this.setProperty('displayStyle', displayStyle);
  }

  _renderDisplayStyle() {
    this.$container.toggleClass('spread-even', this.displayStyle === TabArea.DisplayStyle.SPREAD_EVEN);
    this.invalidateLayoutTree();
  }

  _onTabItemFocus() {
    this.setFocused(true);
  }

  _onTabItemBlur() {
    this.setFocused(false);
  }

  _updateHasSubLabel() {
    let items = this.visibleTabs();
    this._setHasSubLabel(items.some(item => {
      return strings.hasText(item.subLabel);
    }));
  }

  visibleTabs() {
    return this.tabs.filter(tab => tab.isVisible());
  }

  _setHasSubLabel(hasSubLabel) {
    if (this.hasSubLabel === hasSubLabel) {
      return;
    }
    this._setProperty('hasSubLabel', hasSubLabel);
    if (this.rendered) {
      this._renderHasSubLabel();
    }
  }

  _renderHasSubLabel() {
    this.$container.toggleClass('has-sub-label', this.hasSubLabel);
    // Invalidate other tabs as well because the class has an impact on their size, too
    this.visibleTabs().forEach(tab => tab.invalidateLayout());
    this.invalidateLayoutTree();
  }

  selectNextTab(focusTab) {
    this._moveSelectionHorizontal(true, focusTab);
  }

  selectPreviousTab(focusTab) {
    this._moveSelectionHorizontal(false, focusTab);
  }

  _moveSelectionHorizontal(directionRight, focusTab) {
    let tabItems = this.tabs.slice(),
      $focusedElement = this.$container.activeElement(),
      selectNext = false;
    if (!directionRight) {
      tabItems.reverse();
      selectNext = $focusedElement[0] === this.ellipsis.$container[0];
    }

    tabItems.forEach(function(item, index) {
      if (selectNext && item.visible && !item.overflown) {
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
  }

  _setTabbableItem(tabItem) {
    let tabItems = this.tabs;
    if (tabItem) {
      // clear old tabbable
      this.ellipsis.setTabbable(false);
      tabItems.forEach(item => {
        item.setTabbable(false);
      });
      tabItem.setTabbable(true);
    }
  }

  _onTabPropertyChange(event) {
    if (event.propertyName === 'selected') {
      this.setSelectedTab(event.source);
    }
  }

  _onTabItemPropertyChange(event) {
    if (event.propertyName === 'visible') {
      this._updateHasSubLabel();
      this.invalidateLayoutTree();
    }
    if (event.propertyName === 'subLabel') {
      this._updateHasSubLabel();
    }
  }
}
