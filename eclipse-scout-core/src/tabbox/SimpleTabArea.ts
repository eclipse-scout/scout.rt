/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  AbstractLayout, EnumObject, Event, EventHandler, HtmlComponent, InitModelOf, scout, SimpleTab, SimpleTabAreaEventMap, SimpleTabAreaLayout, SimpleTabAreaModel, SimpleTabOverflowMenu, SimpleTabView, TabbableCoordinator, TabbableItem,
  Widget, widgets
} from '../index';

export type SimpleTabAreaPosition = EnumObject<typeof SimpleTabArea.Position>;
export type SimpleTabAreaDisplayStyle = EnumObject<typeof SimpleTabArea.DisplayStyle>;

export class SimpleTabArea<TView extends SimpleTabView = SimpleTabView> extends Widget implements SimpleTabAreaModel<TView> {
  declare model: SimpleTabAreaModel<TView>;
  declare eventMap: SimpleTabAreaEventMap<TView>;
  declare self: SimpleTabArea<any>;

  static Position = {
    TOP: 'top',
    BOTTOM: 'bottom',
    RIGHT: 'right',
    LEFT: 'left'
  } as const;

  static DisplayStyle = {
    DEFAULT: 'default',
    SPREAD_EVEN: 'spreadEven'
  } as const;

  position: SimpleTabAreaPosition;
  displayStyle: SimpleTabAreaDisplayStyle;
  tabs: SimpleTab<TView>[];
  tabbableCoordinator: TabbableCoordinator;
  selectOnFocus = true;

  protected _selectedViewTab: SimpleTab<TView>;
  protected _tabClickHandler: EventHandler<Event<SimpleTab<TView>>>;

  constructor() {
    super();
    this.position = SimpleTabArea.Position.TOP;
    this.displayStyle = SimpleTabArea.DisplayStyle.DEFAULT;
    this.tabs = [];
    this._selectedViewTab = null;
    this._tabClickHandler = this._onTabClick.bind(this);
    this._addWidgetProperties(['tabs']);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.tabbableCoordinator = scout.create(TabbableCoordinator, {parent: this});
    if (this.selectOnFocus) {
      this.tabbableCoordinator.on('propertyChange:currentItem', event => {
        if (event.newValue instanceof SimpleTab) {
          this.selectTab(event.newValue);
        }
      });
    }
    this._setPosition(this.position);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('simple-tab-area');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(this._createLayout());
  }

  protected _createLayout(): AbstractLayout {
    return new SimpleTabAreaLayout(this);
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderPosition();
    this._renderDisplayStyle();
    this._renderTabs();
  }

  setPosition(position: SimpleTabAreaPosition) {
    this.setProperty('position', position);
  }

  protected _setPosition(position: SimpleTabAreaPosition) {
    this._setProperty('position', position);
    this.tabbableCoordinator.setOrientation(scout.isOneOf(this.position, SimpleTabArea.Position.TOP, SimpleTabArea.Position.BOTTOM) ? 'horizontal' : 'vertical');
  }

  protected _renderPosition() {
    const positions: SimpleTabAreaPosition[] = [SimpleTabArea.Position.TOP, SimpleTabArea.Position.BOTTOM, SimpleTabArea.Position.LEFT, SimpleTabArea.Position.RIGHT];
    positions.forEach(position => this.$container.toggleClass(this._cssClassForPosition(position), this.position === position));
    this.invalidateLayoutTree();
  }

  protected _cssClassForPosition(position: SimpleTabAreaPosition) {
    return 'position-' + position;
  }

  setDisplayStyle(displayStyle: SimpleTabAreaDisplayStyle) {
    this.setProperty('displayStyle', displayStyle);
  }

  protected _renderDisplayStyle() {
    this.$container.toggleClass('spread-even', this.displayStyle === SimpleTabArea.DisplayStyle.SPREAD_EVEN);
    this.invalidateLayoutTree();
  }

  protected _renderTabs() {
    // reverse since tab.renderAfter() called without sibling=true argument (see _renderTab)
    // will _prepend_ themselves into the container.
    this.tabs.slice().reverse().forEach(tab => this._renderTab(tab));
    widgets.updateFirstLastMarker(this.tabs);
  }

  protected _renderTab(tab: SimpleTab<TView>) {
    tab.renderAfter(this.$container);
  }

  protected override _renderVisible() {
    this.$container.setVisible(this.visible && this.tabs.length > 0);
    this.invalidateLayoutTree();
  }

  protected _onTabClick(event: Event<SimpleTab<TView>>) {
    this.selectTab(event.source);
  }

  getTabs(): SimpleTab<TView>[] {
    return this.tabs;
  }

  /**
   * @param true to also return the visible tabs that are currently overflown. Default is false.
   */
  getVisibleTabs(includeOverflown = false): SimpleTab<TView>[] {
    return this.tabs.filter(tab => {
      let visible = tab.visible;
      if (!includeOverflown) {
        visible &&= !tab.overflown;
      }
      return visible;
    });
  }

  selectTab(viewTab: SimpleTab<TView>) {
    if (this._selectedViewTab === viewTab) {
      return;
    }
    this.deselectTab(this._selectedViewTab);
    this._selectedViewTab = viewTab;
    if (viewTab) {
      if (this.selectOnFocus) {
        this.tabbableCoordinator.setCurrentItem(viewTab);
      }
      // Select the new view tab.
      viewTab.select();
    }
    this.trigger('tabSelect', {viewTab});
    if (viewTab?.overflown) {
      this.invalidateLayoutTree();
    }
  }

  deselectTab(viewTab: SimpleTab<TView>) {
    if (!viewTab) {
      return;
    }
    if (this._selectedViewTab !== viewTab) {
      return;
    }
    this._selectedViewTab.deselect();
  }

  getSelectedTab(): SimpleTab<TView> {
    return this._selectedViewTab;
  }

  addTab(tab: SimpleTab<TView>, sibling?: SimpleTab<TView>) {
    let insertPosition = -1;
    if (sibling) {
      insertPosition = this.tabs.indexOf(sibling);
    }
    this.tabs.splice(insertPosition + 1, 0, tab);
    tab.on('click', this._tabClickHandler);
    this._updateTabbableItems();
    if (this.rendered) {
      this._renderVisible();
      tab.renderAfter(this.$container, sibling);
      widgets.updateFirstLastMarker(this.getTabs());
      this.invalidateLayoutTree();
    }
  }

  destroyTab(tab: SimpleTab<TView>) {
    let index = this.tabs.indexOf(tab);
    if (index < 0) {
      return;
    }
    this.tabs.splice(index, 1);
    tab.destroy();
    tab.off('click', this._tabClickHandler);
    this._updateTabbableItems();
    if (this.rendered) {
      this._renderVisible();
      widgets.updateFirstLastMarker(this.getTabs());
      this.invalidateLayoutTree();
    }
  }

  /**
   * @internal
   */
  _updateTabbableItems() {
    let items: TabbableItem[] = [...this.tabs];
    let overflowTab = this.findChild(SimpleTabOverflowMenu);
    if (overflowTab) {
      items.push(overflowTab);
    }
    this.tabbableCoordinator.setItems(items);
  }
}
