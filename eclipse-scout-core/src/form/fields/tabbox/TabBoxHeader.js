/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {GroupBoxMenuItemsOrder, HtmlComponent, scout, TabBoxHeaderLayout, Widget} from '../../../index';

export default class TabBoxHeader extends Widget {

  constructor() {
    super();

    this.tabBox = null;
    this.tabArea = null;
    this.menuBar = null;
    this.$container = null;
    this.htmlComp = null;
    this.$borderBottom = null;
    this._tabBoxPropertyChangeHandler = this._onTabBoxPropertyChange.bind(this);
    this._tabAreaPropertyChangeHandler = this._onTabAreaPropertyChange.bind(this);
  }

  _init(options) {
    super._init(options);

    this.tabArea = scout.create('TabArea', {
      parent: this,
      tabBox: this.tabBox
    });
    this.tabArea.on('propertyChange', this._tabAreaPropertyChangeHandler);

    this.menuBar = scout.create('MenuBar', {
      parent: this,
      menuOrder: new GroupBoxMenuItemsOrder()
    });

    this.tabBox.on('propertyChange', this._tabBoxPropertyChangeHandler);
    this.menuBar.setMenuItems(this.tabBox.menus);
    this.setVisible(this.tabBox.labelVisible);
  }

  _render() {
    this.$container = this.$parent.appendDiv('tab-box-header');
    this.$borderBottom = this.$container.appendDiv('bottom-border');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new TabBoxHeaderLayout(this));
    this.tabArea.render(this.$container);
    this.menuBar.render(this.$container);
    this.$container.append(this.menuBar.$container);
  }

  _destroy() {
    this.tabBox.off('propertyChange', this._tabBoxPropertyChangeHandler);
    this.tabArea.off('propertyChange', this._tabAreaPropertyChangeHandler);
    super._destroy();
  }

  setTabItems(tabItems) {
    this.tabArea.setTabItems(tabItems);
  }

  _setSelectedTab(tab) {
    if (tab) {
      this.setSelectedTabItem(tab.tabItem);
    } else {
      this.setSelectedTabItem(null);
    }
  }

  setSelectedTabItem(tabItem) {
    this.setProperty('selectedTabItem', tabItem);
  }

  _setSelectedTabItem(tabItem) {
    this._setProperty('selectedTabItem', tabItem);
    this.tabArea.setSelectedTabItem(tabItem);
  }

  isTabItemFocused(tabItem) {
    return this.tabArea.isTabItemFocused(tabItem);
  }

  focusTabItem(tabItem) {
    this.tabArea.focusTabItem(tabItem);
  }

  getTabForItem(tabItem) {
    return this.tabArea.getTabForItem(tabItem);
  }

  _onTabBoxPropertyChange(event) {
    if (event.propertyName === 'menus') {
      this.menuBar.setMenuItems(this.tabBox.menus);
    } else if (event.propertyName === 'labelVisible') {
      this.setVisible(this.tabBox.labelVisible);
    }
  }

  _onTabAreaPropertyChange(event) {
    if (event.propertyName === 'selectedTab') {
      this._setSelectedTab(event.newValue);
    }
  }
}
