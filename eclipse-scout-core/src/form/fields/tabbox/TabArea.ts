/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, EllipsisMenu, EnumObject, EventHandler, HtmlComponent, InitModelOf, KeyStrokeContext, PropertyChangeEvent, scout, SomeRequired, strings, Tab, TabAreaEventMap, TabAreaLayout, TabAreaLeftKeyStroke, TabAreaModel,
  TabAreaRightKeyStroke, TabBox, TabItem, Widget
} from '../../../index';

export type TabAreaStyle = EnumObject<typeof TabArea.DisplayStyle>;

export class TabArea extends Widget implements TabAreaModel {
  declare model: TabAreaModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'tabBox'>;
  declare eventMap: TabAreaEventMap;
  declare self: TabArea;

  tabBox: TabBox;
  tabs: Tab[];
  displayStyle: TabAreaStyle;
  hasSubLabel: boolean;
  selectedTab: Tab;
  ellipsis: EllipsisMenu;
  $selectionMarker: JQuery;
  protected _tabItemPropertyChangeHandler: EventHandler<PropertyChangeEvent>;
  protected _tabPropertyChangeHandler: EventHandler<PropertyChangeEvent>;

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
  } as const;

  protected override _init(options: InitModelOf<this>) {
    super._init(options);
    this.tabBox = options.tabBox;

    this.ellipsis = scout.create(EllipsisMenu, {
      parent: this,
      cssClass: 'overflow-tab-item unfocusable',
      iconId: null,
      inheritAccessibility: false,
      text: '0' // Initialize with the normal value to prevent unnecessary layout invalidation by the TabAreaLayout if ellipsis menus is not visible
    });
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this.keyStrokeContext.registerKeyStrokes([
      new TabAreaLeftKeyStroke(this),
      new TabAreaRightKeyStroke(this)
    ]);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('tab-area');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new TabAreaLayout(this));

    this.ellipsis.render(this.$container);

    this.$selectionMarker = this.$container.appendDiv('selection-marker');
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderTabs();
    this._renderSelectedTab();
    this._renderHasSubLabel();
    this._renderDisplayStyle();
  }

  protected override _remove() {
    super._remove();
    this._removeTabs();
  }

  setSelectedTabItem(tabItem: TabItem) {
    this.setSelectedTab(this.getTabForItem(tabItem));
  }

  getTabForItem(tabItem: TabItem): Tab {
    return arrays.find(this.tabs, tab => tab.tabItem === tabItem);
  }

  setSelectedTab(tab: Tab) {
    this.setProperty('selectedTab', tab);
  }

  protected _setSelectedTab(tab: Tab) {
    if (this.selectedTab) {
      this.selectedTab.setSelected(false);
    }
    if (tab) {
      tab.setSelected(true);
    }
    this._setProperty('selectedTab', tab);
    this._setTabbableItem(tab);
  }

  protected _renderSelectedTab() {
    // force a relayout in case the selected tab is overflown. The layout will ensure the selected tab is visible.
    if (this.selectedTab && this.selectedTab.overflown) {
      this.invalidateLayoutTree();
    }
  }

  isTabItemFocused(tabItem: TabItem): boolean {
    return this.getTabForItem(tabItem).isFocused();
  }

  focusTabItem(tabItem: TabItem): boolean {
    return this.focusTab(this.getTabForItem(tabItem));
  }

  focusTab(tab: Tab): boolean {
    return tab.focus();
  }

  setTabItems(tabItems: TabItem[]) {
    this.setProperty('tabs', tabItems);
    this._updateHasSubLabel();
    this.invalidateLayoutTree();
  }

  protected _setTabs(tabItems: TabItem[]) {
    let tabsToRemove = this.tabs.slice(),
      tabs = tabItems.map(tabItem => {
        let tab = this.getTabForItem(tabItem);
        if (!tab) {
          tab = scout.create(Tab, {
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
    tabsToRemove.forEach(tab => {
      tab.tabItem.off('propertyChange', this._tabItemPropertyChangeHandler);
    });

    this._removeTabs(tabsToRemove);
    this._setProperty('tabs', tabs);
  }

  protected _renderTabs() {
    // noinspection JSVoidFunctionReturnValueUsed Obviously an IntelliJ bug, it assumes reverse is from Animation rather than from Array
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

  protected _removeTabs(tabs?: Tab[]) {
    tabs = tabs || this.tabs;
    tabs.forEach(tab => {
      tab.remove();
    });
  }

  setDisplayStyle(displayStyle: TabAreaStyle) {
    this.setProperty('displayStyle', displayStyle);
  }

  protected _renderDisplayStyle() {
    this.$container.toggleClass('spread-even', this.displayStyle === TabArea.DisplayStyle.SPREAD_EVEN);
    this.invalidateLayoutTree();
  }

  protected _onTabItemFocus() {
    this.setFocused(true);
  }

  protected _onTabItemBlur() {
    this.setFocused(false);
  }

  protected _updateHasSubLabel() {
    let items = this.visibleTabs();
    this._setHasSubLabel(items.some(item => {
      return strings.hasText(item.subLabel);
    }));
  }

  visibleTabs(): Tab[] {
    return this.tabs.filter(tab => tab.isVisible());
  }

  protected _setHasSubLabel(hasSubLabel: boolean) {
    if (this.hasSubLabel === hasSubLabel) {
      return;
    }
    this._setProperty('hasSubLabel', hasSubLabel);
    if (this.rendered) {
      this._renderHasSubLabel();
    }
  }

  protected _renderHasSubLabel() {
    this.$container.toggleClass('has-sub-label', this.hasSubLabel);
    // Invalidate other tabs as well because the class has an impact on their size, too
    this.visibleTabs().forEach(tab => tab.invalidateLayout());
    this.invalidateLayoutTree();
  }

  selectNextTab(focusTab: boolean) {
    this._moveSelectionHorizontal(true, focusTab);
  }

  selectPreviousTab(focusTab: boolean) {
    this._moveSelectionHorizontal(false, focusTab);
  }

  protected _moveSelectionHorizontal(directionRight: boolean, focusTab: boolean) {
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

  protected _setTabbableItem(tab: Tab | EllipsisMenu) {
    let tabs = this.tabs;
    if (tab) {
      // clear old tabbable
      this.ellipsis.setTabbable(false);
      tabs.forEach(item => {
        item.setTabbable(false);
      });
      tab.setTabbable(true);
    }
  }

  protected _onTabPropertyChange(event: PropertyChangeEvent<any, Tab>) {
    if (event.propertyName === 'selected') {
      this.setSelectedTab(event.source);
    }
  }

  protected _onTabItemPropertyChange(event: PropertyChangeEvent) {
    if (event.propertyName === 'visible') {
      this._updateHasSubLabel();
      this.invalidateLayoutTree();
    }
    if (event.propertyName === 'subLabel') {
      this._updateHasSubLabel();
    }
  }
}
