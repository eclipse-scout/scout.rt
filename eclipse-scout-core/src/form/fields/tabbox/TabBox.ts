/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  aria, arrays, CompositeField, EnumObject, fields, FormField, HtmlComponent, InitModelOf, Menu, ObjectOrChildModel, ObjectUuidProvider, PropertyChangeEvent, scout, SingleLayout, Tab, TabArea, TabAreaStyle, TabBoxEventMap, TabBoxHeader,
  TabBoxLayout, TabBoxModel, TabItem
} from '../../../index';
import $ from 'jquery';

/**
 * Tab-area = where the 1-n tabs are placed (may have multiple runs = lines).
 * Tab-content = where the content of a single tab is displayed.
 */
export class TabBox extends CompositeField implements TabBoxModel {
  declare model: TabBoxModel;
  declare eventMap: TabBoxEventMap;
  declare self: TabBox;

  selectedTab: TabItem;
  header: TabBoxHeader;
  tabItems: TabItem[];
  tabAreaStyle: TabAreaStyle;

  /** @internal */
  _$tabContent: JQuery;

  protected _statusPositionOrig: any;
  protected _tabBoxHeaderPropertyChangeHandler: any;

  constructor() {
    super();

    this.gridDataHints.useUiHeight = true;
    this.gridDataHints.w = FormField.FULL_WIDTH;
    this.header = null;
    this.selectedTab = null;
    this.tabItems = [];
    this.tabAreaStyle = TabArea.DisplayStyle.DEFAULT;

    this._$tabContent = null;
    this._statusPositionOrig = null;
    this._addWidgetProperties(['tabItems', 'selectedTab']);
    this._addPreserveOnPropertyChangeProperties(['selectedTab']);

    this._tabBoxHeaderPropertyChangeHandler = this._onTabBoxHeaderPropertyChange.bind(this);
  }

  static MenuType = {
    /**
     * In most cases, it is not necessary to set this menu type for a tab box menu because it does not affect the
     * visibility of the menu unless the menu is used for widgets other than the tab box. In this case, the menu type can
     * be used to ensure that the menu is only visible on tab boxes.
     */
    Header: 'TabBox.Header'
  } as const;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.header = scout.create(TabBoxHeader, {
      parent: this,
      tabBox: this
    });

    this._initProperties(model);
    this.header.on('propertyChange', this._tabBoxHeaderPropertyChangeHandler);
  }

  protected _initProperties(model: TabBoxModel) {
    this._setTabItems(this.tabItems);
    this._setSelectedTab(this.selectedTab);
    this._setTabAreaStyle(this.tabAreaStyle);
  }

  protected override _destroy() {
    super._destroy();
    this.header.off('propertyChange', this._tabBoxHeaderPropertyChangeHandler);
  }

  protected override _render() {
    this.addContainer(this.$parent, 'tab-box', new TabBoxLayout(this));
    aria.role(this.$container, 'tablist');
    this.header.render(this.$container);
    this.addStatus();

    this._$tabContent = this.$container.appendDiv('tab-content');
    aria.role(this._$tabContent, 'tabpanel');
    let htmlCompContent = HtmlComponent.install(this._$tabContent, this.session);
    htmlCompContent.setLayout(new SingleLayout());
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderSelectedTab();
  }

  protected override _remove() {
    super._remove();
    this._removeSelectedTab();
  }

  override getContextMenuItems(onlyVisible = true): Menu[] {
    // handled by the menubar
    return [];
  }

  protected _removeMenus() {
    // menubar takes care of removal
  }

  deleteTabItem(tabItem: TabItem) {
    let index = this.tabItems.indexOf(tabItem);
    let newTabItems = this.tabItems.slice();
    if (index >= 0) {
      newTabItems.splice(index, 1);
      this.setTabItems(newTabItems);
    }
  }

  /**
   * Inserts a new tab item.
   * @param index The position where the new tab should be inserted. By default, it will be appended at the end of the existing tab items.
   */
  insertTabItem(tabItem: ObjectOrChildModel<TabItem>, index?: number) {
    if (!tabItem) {
      return;
    }
    index = scout.nvl(index, this.tabItems.length);
    let tabItems = this.tabItems.slice() as ObjectOrChildModel<TabItem>[];
    tabItems.splice(index, 0, tabItem);
    this.setTabItems(tabItems);
  }

  setTabItems(tabItems: ObjectOrChildModel<TabItem>[]) {
    this.setProperty('tabItems', tabItems);
  }

  protected _setTabItems(tabItems: TabItem[]) {
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

  protected _renderTabItems(tabItems: TabItem[]) {
    // void only selected tab is rendered
  }

  protected _removeTabItems(tabItems: TabItem[]) {
    // void only selected tab is rendered
  }

  protected _removeTabContent() {
    this.tabItems.forEach(tabItem => {
      tabItem.remove();
    });
  }

  /**
   * @param tabItem if a string is provided, the tab will be resolved automatically
   */
  setSelectedTab(tabItem: TabItem | string) {
    this.setProperty('selectedTab', tabItem);
  }

  protected _setSelectedTab(tabItem: TabItem) {
    $.log.isDebugEnabled() && $.log.debug('(TabBox#_selectTab) tab=' + tabItem);
    if (this.selectedTab && this.selectedTab.rendered) {
      this.selectedTab.remove();
    }
    this._setProperty('selectedTab', tabItem);
    this.header.setSelectedTabItem(this.selectedTab);
  }

  protected _renderSelectedTab() {
    if (this.selectedTab) {
      this.selectedTab.render(this._$tabContent);
      this.selectedTab.get$Scrollable().data('scroll-shadow-customizer', this._updateScrollShadow.bind(this));
    }
    if (this.rendered) {
      this._updateScrollShadow();
      HtmlComponent.get(this._$tabContent).invalidateLayoutTree();
    }
  }

  protected _removeSelectedTab() {
    if (this.selectedTab) {
      this.selectedTab.get$Scrollable().removeData('scroll-shadow-customizer');
      this.selectedTab.remove();
    }
  }

  protected _updateScrollShadow() {
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

  setTabAreaStyle(tabAreaStyle: TabAreaStyle) {
    this.setProperty('tabAreaStyle', tabAreaStyle);
  }

  protected _setTabAreaStyle(tabAreaStyle: TabAreaStyle) {
    this.tabAreaStyle = tabAreaStyle;
    if (this.header && this.header.tabArea) {
      this.header.tabArea.setDisplayStyle(tabAreaStyle);
    }
  }

  protected override _renderStatusPosition() {
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

  protected override _updateFieldStatus() {
    super._updateFieldStatus();
    if (this.selectedTab && this.selectedTab.notification) {
      // Also invalidate tab item if a notification is shown because notification size depends on status visibility
      this.selectedTab.invalidateLayoutTree();
    }
  }

  protected override _renderLabelVisible() {
    super._renderLabelVisible();
    this._updateScrollShadow();
  }

  getFields(): TabItem[] {
    return this.tabItems;
  }

  getTabItem(tabId: string): TabItem {
    return arrays.find(this.tabItems, tabItem => tabItem.id === tabId);
  }

  /**
   * Focuses the selected tab.
   * @returns true if the tab could be focused, false if not.
   */
  override focus(): boolean {
    if (!this.rendered) {
      this.session.layoutValidator.schedulePostValidateFunction(this.focus.bind(this));
      return false;
    }
    if (this.selectedTab) {
      return this.selectedTab.focus();
    }
  }

  /**
   * @returns the focusable element of the selected tab.
   */
  override getFocusableElement(): HTMLElement | JQuery {
    if (this.selectedTab) {
      return this.selectedTab.getFocusableElement();
    }
    return null;
  }

  isTabItemFocused(tabItem: TabItem): boolean {
    return this.header.isTabItemFocused(tabItem);
  }

  focusTabItem(tabItem: TabItem): boolean {
    return this.header.focusTabItem(tabItem);
  }

  getTabForItem(tabItem: TabItem): Tab {
    return this.header.getTabForItem(tabItem);
  }

  protected _onTabBoxHeaderPropertyChange(event: PropertyChangeEvent) {
    if (event.propertyName === 'selectedTabItem') {
      this.setSelectedTab(event.newValue);
    }
  }
}

export type TabBoxMenuType = EnumObject<typeof TabBox.MenuType>;

ObjectUuidProvider.UuidPathSkipWidgets.add(TabBox);
