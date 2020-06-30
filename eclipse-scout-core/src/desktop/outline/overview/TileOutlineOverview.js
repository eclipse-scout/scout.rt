/*
 * Copyright (c) 2014-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HtmlComponent, OutlineOverview, RowLayout, scout} from '../../../index';

export default class TileOutlineOverview extends OutlineOverview {

  constructor() {
    super();
    this.pageTileGrid = null;
    this.scrollable = true;
    this._addWidgetProperties(['pageTileGrid']);
  }

  _init(model) {
    super._init(model);
    if (!this.pageTileGrid) {
      this.pageTileGrid = this._createPageTileGrid();
    }
    this.scrollable = !this.outline.compact;
  }

  _render() {
    this.$container = this.$parent.appendDiv('tile-overview');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new RowLayout({stretch: this.outline.compact}));
    this.$content = this.$container.appendDiv('tile-overview-content');
    this.contentHtmlComp = HtmlComponent.install(this.$content, this.session);
    this.contentHtmlComp.setLayout(new RowLayout({stretch: this.outline.compact}));
    this.$title = this.$content.appendDiv('tile-overview-title').text(this.outline.title);
    HtmlComponent.install(this.$title, this.session);
  }

  _renderProperties() {
    super._renderProperties();
    this._renderPageTileGrid();
    this._renderScrollable();
  }

  _renderPageTileGrid() {
    this.pageTileGrid.render(this.$content);
  }

  _createPageTileGrid() {
    let page;
    let nodes;
    if (this.outline.compact) {
      page = this.outline.compactRootNode();
      if (page) {
        nodes = page.childNodes;
      }
    }
    return scout.create('PageTileGrid', {
      parent: this,
      outline: this.outline,
      compact: this.outline.compact,
      page: page,
      nodes: nodes
    });
  }

  setScrollable(scrollable) {
    this.setProperty('scrollable', scrollable);
  }

  _renderScrollable() {
    if (this.scrollable) {
      this._installScrollbars({
        axis: 'y'
      });
    } else {
      this._uninstallScrollbars();
    }
  }
}
