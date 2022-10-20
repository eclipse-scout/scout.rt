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
import {HtmlComponent, SimpleTabAreaLayout, Widget, widgets} from '../index';

export default class SimpleTabArea extends Widget {

  static DisplayStyle = {
    DEFAULT: 'default',
    SPREAD_EVEN: 'spreadEven'
  };

  constructor() {
    super();
    this.displayStyle = SimpleTabArea.DisplayStyle.DEFAULT;
    this.tabs = [];
    this._selectedViewTab = null;
  }

  _init(model) {
    super._init(model);

    this._tabClickHandler = this._onTabClick.bind(this);
  }

  _render() {
    this.$container = this.$parent.appendDiv('simple-tab-area');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(this._createLayout());
  }

  _createLayout() {
    return new SimpleTabAreaLayout(this);
  }

  _renderProperties() {
    super._renderProperties();
    this._renderDisplayStyle();
    this._renderTabs();
  }

  setDisplayStyle(displayStyle) {
    this.setProperty('displayStyle', displayStyle);
  }

  _renderDisplayStyle() {
    this.$container.toggleClass('spread-even', this.displayStyle === SimpleTabArea.DisplayStyle.SPREAD_EVEN);
    this.invalidateLayoutTree();
  }

  _renderTabs() {
    // reverse since tab.renderAfter() called without sibling=true argument (see _renderTab)
    // will _prepend_ themselves into the container.
    this.tabs.slice().reverse()
      .forEach(tab => {
        this._renderTab(tab);
      });
    widgets.updateFirstLastMarker(this.tabs);
  }

  _renderTab(tab) {
    tab.renderAfter(this.$container);
  }

  _renderVisible() {
    this.$container.setVisible(this.visible && this.tabs.length > 0);
    this.invalidateLayoutTree();
  }

  _onTabClick(event) {
    this.selectTab(event.source);
  }

  getTabs() {
    return this.tabs;
  }

  getVisibleTabs() {
    return this.tabs.filter(tab => {
      // Layout operates on dom elements directly -> check dom visibility
      if (tab.$container) {
        return tab.$container.isVisible();
      }
      return tab.visible;
    });
  }

  selectTab(viewTab) {
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

  deselectTab(viewTab) {
    if (!viewTab) {
      return;
    }
    if (this._selectedViewTab !== viewTab) {
      return;
    }
    this._selectedViewTab.deselect();
  }

  getSelectedTab() {
    return this._selectedViewTab;
  }

  addTab(tab, sibling) {
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

  destroyTab(tab) {
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
