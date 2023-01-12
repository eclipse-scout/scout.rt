/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, EnumObject, Event, EventHandler, HtmlComponent, InitModelOf, SimpleTab, SimpleTabAreaEventMap, SimpleTabAreaLayout, SimpleTabAreaModel, SimpleTabView, Widget, widgets} from '../index';

export type SimpleTabAreaDisplayStyle = EnumObject<typeof SimpleTabArea.DisplayStyle>;

export class SimpleTabArea<TView extends SimpleTabView = SimpleTabView> extends Widget implements SimpleTabAreaModel<TView> {
  declare model: SimpleTabAreaModel<TView>;
  declare eventMap: SimpleTabAreaEventMap<TView>;
  declare self: SimpleTabArea<any>;

  static DisplayStyle = {
    DEFAULT: 'default',
    SPREAD_EVEN: 'spreadEven'
  } as const;

  displayStyle: SimpleTabAreaDisplayStyle;
  tabs: SimpleTab<TView>[];

  protected _selectedViewTab: SimpleTab<TView>;
  protected _tabClickHandler: EventHandler<Event<SimpleTab<TView>>>;

  constructor() {
    super();
    this.displayStyle = SimpleTabArea.DisplayStyle.DEFAULT;
    this.tabs = [];
    this._selectedViewTab = null;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this._tabClickHandler = this._onTabClick.bind(this);
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
    this._renderDisplayStyle();
    this._renderTabs();
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
    // noinspection JSVoidFunctionReturnValueUsed
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

  getVisibleTabs(): SimpleTab<TView>[] {
    return this.tabs.filter(tab => {
      // Layout operates on dom elements directly -> check dom visibility
      if (tab.$container) {
        return tab.$container.isVisible();
      }
      return tab.visible;
    });
  }

  selectTab(viewTab: SimpleTab<TView>) {
    if (this._selectedViewTab === viewTab) {
      return;
    }
    this.deselectTab(this._selectedViewTab);
    this._selectedViewTab = viewTab;
    if (viewTab) {
      // Select the new view tab.
      viewTab.select();
    }
    this.trigger('tabSelect', {
      viewTab: viewTab
    });
    if (viewTab && viewTab.rendered && !viewTab.$container.isVisible()) {
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
    if (this.rendered) {
      this._renderVisible();
      tab.renderAfter(this.$container, sibling);
      widgets.updateFirstLastMarker(this.getTabs());
      this.invalidateLayoutTree();
    }
  }

  destroyTab(tab: SimpleTab<TView>) {
    let index = this.tabs.indexOf(tab);
    if (index > -1) {
      this.tabs.splice(index, 1);
      tab.destroy();
      tab.off('click', this._tabClickHandler);
      if (this.rendered) {
        this._renderVisible();
        widgets.updateFirstLastMarker(this.getTabs());
        this.invalidateLayoutTree();
      }
    }
  }
}
