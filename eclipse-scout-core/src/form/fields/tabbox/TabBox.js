/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, CompositeField, fields, FormField, HtmlComponent, scout, SingleLayout, TabArea, TabBoxLayout} from '../../../index';
import $ from 'jquery';

/**
 * Tab-area = where the 1-n tabs are placed (may have multiple runs = lines).
 * Tab-content = where the content of a single tab is displayed.
 */
export default class TabBox extends CompositeField {

  constructor() {
    super();

    this.gridDataHints.useUiHeight = true;
    this.gridDataHints.w = FormField.FULL_WIDTH;
    this.selectedTab = null;
    this.tabItems = [];
    this.tabAreaStyle = TabArea.DisplayStyle.DEFAULT;

    this._$tabContent = null;
    this._statusPositionOrig = null;
    this._addWidgetProperties(['tabItems', 'selectedTab']);
    this._addPreserveOnPropertyChangeProperties(['selectedTab']);

    this._tabBoxHeaderPropertyChangeHander = this._onTabBoxHeaderPropertyChange.bind(this);
  }

  /**
   * @override FormField.js
   */
  _init(model) {
    super._init(model);
    this.header = scout.create('TabBoxHeader', {
      parent: this,
      tabBox: this
    });

    this._initProperties(model);
    this.header.on('propertyChange', this._tabBoxHeaderPropertyChangeHander);
  }

  _initProperties(model) {
    this._setTabItems(this.tabItems);
    this._setSelectedTab(this.selectedTab);
    this._setTabAreaStyle(this.tabAreaStyle);
  }

  _destroy() {
    super._destroy();
    this.header.off('propertyChange', this._tabBoxHeaderPropertyChangeHander);
  }

  _render() {
    this.addContainer(this.$parent, 'tab-box', new TabBoxLayout(this));

    this.header.render(this.$container);
    this.addStatus();

    this._$tabContent = this.$container.appendDiv('tab-content');
    let htmlCompContent = HtmlComponent.install(this._$tabContent, this.session);
    htmlCompContent.setLayout(new SingleLayout());
  }

  /**
   * @override FormField.js
   */
  _renderProperties() {
    super._renderProperties();
    this._renderSelectedTab();
  }

  /**
   * @override FormField.js
   */
  _remove() {
    super._remove();
    this._removeSelectedTab();
  }

  getContextMenuItems(onlyVisible = true) {
    // handled by the menubar
    return [];
  }

  _removeMenus() {
    // menubar takes care about removal
  }

  deleteTabItem(tabItem) {
    let index = this.tabItems.indexOf(tabItem);
    let newTabItems = this.tabItems.slice();
    if (index >= 0) {
      newTabItems.splice(index, 1);
      this.setTabItems(newTabItems);
    }
  }

  insertTabItem(tabItem, index) {
    if (!tabItem) {
      return;
    }
    index = scout.nvl(index, this.tabItems.length);
    let newTabItems = this.tabItems.slice();
    newTabItems.splice(index, 0, tabItem);
    this.setTabItems(newTabItems);
  }

  setTabItems(tabItems) {
    this.setProperty('tabItems', tabItems);
  }

  _setTabItems(tabItems) {
    tabItems = tabItems || [];
    let tabsToRemove = this.tabItems || [];
    tabsToRemove.filter(tabItem => tabItems.indexOf(tabItem) < 0
    ).forEach(tabItem => {
      tabItem.remove();
    });

    this._setProperty('tabItems', tabItems);
    this.header.setTabItems(this.tabItems);
    // if no tab is selected select first
    if (this.tabItems.indexOf(this.selectedTab) < 0) {
      this.setSelectedTab(this.tabItems[0]);
    }
  }

  _renderTabItems(tabItems) {
    // void only selected tab is rendered
  }

  _removeTabItems(tabItems) {
    // void only selected tab is rendered
  }

  _removeTabContent() {
    this.tabItems.forEach(tabItem => {
      tabItem.remove();
    }, this);
  }

  selectTabById(tabId) {
    let tab = this.getTabItem(tabId);
    if (!tab) {
      throw new Error('Tab with ID \'' + tabId + '\' does not exist');
    }
    this.setSelectedTab(tab);
  }

  setSelectedTab(tabItem) {
    this.setProperty('selectedTab', tabItem);
  }

  _setSelectedTab(tabItem) {
    $.log.isDebugEnabled() && $.log.debug('(TabBox#_selectTab) tab=' + tabItem);
    if (this.selectedTab && this.selectedTab.rendered) {
      this.selectedTab.remove();
    }
    this._setProperty('selectedTab', tabItem);
    this.header.setSelectedTabItem(this.selectedTab);
  }

  _renderSelectedTab() {
    if (this.selectedTab) {
      this.selectedTab.render(this._$tabContent);
      this.selectedTab.get$Scrollable().data('scroll-shadow-customizer', this._updateScrollShadow.bind(this));
    }
    if (this.rendered) {
      this._updateScrollShadow();
      HtmlComponent.get(this._$tabContent).invalidateLayoutTree();
    }
  }

  _removeSelectedTab() {
    if (this.selectedTab) {
      this.selectedTab.get$Scrollable().removeData('scroll-shadow-customizer');
      this.selectedTab.remove();
    }
  }

  _updateScrollShadow() {
    if (!this.rendered) {
      return;
    }
    let hasScrollShadowTop = this.selectedTab && this.selectedTab.hasScrollShadow('top');
    let oldHasScrollShadowTop = this.$container.hasClass('has-scroll-shadow-top');
    this.$container.toggleClass('has-scroll-shadow-top', hasScrollShadowTop);
    if (oldHasScrollShadowTop !== hasScrollShadowTop) {
      this.invalidateLayoutTree(false);
    }

    // Enlarge header line if there is a shadow, but only if there is a header (controlled by labelVisible)
    fields.adjustStatusPositionForScrollShadow(this, () => hasScrollShadowTop && this.labelVisible);
  }

  setTabAreaStyle(tabAreaStyle) {
    this.setProperty('tabAreaStyle', tabAreaStyle);
  }

  _setTabAreaStyle(tabAreaStyle) {
    this.tabAreaStyle = tabAreaStyle;
    if (this.header && this.header.tabArea) {
      this.header.tabArea.setDisplayStyle(tabAreaStyle);
    }
  }

  /**
   * @override FormField.js
   */
  _renderStatusPosition() {
    super._renderStatusPosition();
    if (!this.fieldStatus) {
      return;
    }
    if (this.statusPosition === FormField.StatusPosition.TOP) {
      // move into header
      this.$status.appendTo(this.header.$container);
    } else {
      this.$status.appendTo(this.$container);
    }
    this.invalidateLayoutTree();
  }

  _updateFieldStatus() {
    super._updateFieldStatus();
    if (this.selectedTab && this.selectedTab.notification) {
      // Also invalidate tab item if a notification is shown because notification size depends on status visibility
      this.selectedTab.invalidateLayoutTree();
    }
  }

  _renderLabelVisible() {
    super._renderLabelVisible();
    this._updateScrollShadow();
  }

  /**
   * @override CompositeField.js
   */
  getFields() {
    return this.tabItems;
  }

  getTabItem(tabId) {
    return arrays.find(this.tabItems, tabItem => {
      return tabItem.id === tabId;
    });
  }

  /**
   * @override FormField.js
   */
  focus() {
    if (!this.rendered) {
      this.session.layoutValidator.schedulePostValidateFunction(this.focus.bind(this));
      return false;
    }
    if (this.selectedTab) {
      return this.selectedTab.focus();
    }
  }

  /**
   * @override
   */
  getFocusableElement() {
    if (this.selectedTab) {
      return this.selectedTab.getFocusableElement();
    }
    return null;
  }

  isTabItemFocused(tabItem) {
    return this.header.isTabItemFocused(tabItem);
  }

  focusTabItem(tabItem) {
    if (this.selectedTab !== tabItem) {
      this.selectTab(tabItem);
    }
    this.header.focusTabItem(tabItem);
  }

  getTabForItem(tabItem) {
    return this.header.getTabForItem(tabItem);
  }

  _onTabBoxHeaderPropertyChange(event) {
    if (event.propertyName === 'selectedTabItem') {
      this.setSelectedTab(event.newValue);
    }
  }
}
