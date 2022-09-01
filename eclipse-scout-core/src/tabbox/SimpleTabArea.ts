/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, EnumObject, Event, EventHandler, HtmlComponent, SimpleTab, SimpleTabAreaEventMap, SimpleTabAreaLayout, SimpleTabAreaModel, Widget, widgets} from '../index';

export type SimpleTabAreaDisplayStyle = EnumObject<typeof SimpleTabArea.DisplayStyle>;

export default class SimpleTabArea extends Widget implements SimpleTabAreaModel {
  declare model: SimpleTabAreaModel;
  declare eventMap: SimpleTabAreaEventMap;

  static DisplayStyle = {
    DEFAULT: 'default',
    SPREAD_EVEN: 'spreadEven'
  } as const;

  displayStyle: SimpleTabAreaDisplayStyle;
  tabs: SimpleTab[];

  protected _selectedViewTab: SimpleTab;
  protected _tabClickHandler: EventHandler<Event<SimpleTab>>;

  constructor() {
    super();
    this.displayStyle = SimpleTabArea.DisplayStyle.DEFAULT;
    this.tabs = [];
    this._selectedViewTab = null;
  }

  protected override _init(model: SimpleTabAreaModel) {
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
    this.tabs.slice().reverse().forEach(tab => this._renderTab(tab));
    widgets.updateFirstLastMarker(this.tabs);
  }

  protected _renderTab(tab: SimpleTab) {
    tab.renderAfter(this.$container);
  }

  protected override _renderVisible() {
    this.$container.setVisible(this.visible && this.tabs.length > 0);
    this.invalidateLayoutTree();
  }

  protected _onTabClick(event: Event<SimpleTab>) {
    this.selectTab(event.source);
  }

  getTabs(): SimpleTab[] {
    return this.tabs;
  }

  getVisibleTabs(): SimpleTab[] {
    return this.tabs.filter(tab => {
      // Layout operates on dom elements directly -> check dom visibility
      if (tab.$container) {
        return tab.$container.isVisible();
      }
      return tab.visible;
    });
  }

  selectTab(viewTab: SimpleTab) {
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

  deselectTab(viewTab: SimpleTab) {
    if (!viewTab) {
      return;
    }
    if (this._selectedViewTab !== viewTab) {
      return;
    }
    this._selectedViewTab.deselect();
  }

  getSelectedTab(): SimpleTab {
    return this._selectedViewTab;
  }

  addTab(tab: SimpleTab, sibling?: SimpleTab) {
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

  destroyTab(tab: SimpleTab) {
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
