/*
 * Copyright (c) 2019-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import {Form, HtmlComponent, RowLayout} from '../index';

export default class TileOverviewForm extends Form {
  constructor() {
    super();

    this.outline = null;
    this.nodes = null;
    this.tileOverviewTitle = null;
    this.scrollable = true;
    this._addWidgetProperties(['pageTileGrid']);

    this.$content = null;
    this.$title = null;
  }

  _init(model) {
    super._init(model);
    if (!this.pageTileGrid) {
      this.pageTileGrid = this._createPageTileGrid();
    }
  }

  _renderForm() {
    super._renderForm();
    this.htmlComp.setLayout(new RowLayout());
    this.$content = this.$container.appendDiv('tile-overview-content');
    this.contentHtmlComp = HtmlComponent.install(this.$content, this.session);
    this.contentHtmlComp.setLayout(new RowLayout({stretch: this.outline.compact}));
    this.$title = this.$content.appendDiv('tile-overview-title').text(this.tileOverviewTitle);
    HtmlComponent.install(this.$title, this.session);
  }

  _renderProperties() {
    super._renderProperties();
    if (this.pageTileGrid.rendered) {
      this.pageTileGrid = this._createPageTileGrid();
    }
    this._renderPageTileGrid();
    this._renderScrollable();
  }

  _renderPageTileGrid() {
    this.pageTileGrid.render(this.$content);
  }

  _createPageTileGrid() {
    return scout.create('PageTileGrid', {
      parent: this,
      outline: this.outline,
      nodes: this.nodes
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

  setPage(page) {
    this.outline = page.getOutline();
    this.nodes = page.childNodes;
    this.tileOverviewTitle = page.text;
    this.setScrollable(!this.outline.compact);

    this.pageTileGrid.setCompact(this.outline.compact);
    this.pageTileGrid.setOutline(this.outline);
    this.pageTileGrid.setPage(page);
    this.pageTileGrid.setNodes(this.nodes);
  }

  _remove() {
    this.$content = null;
    this.$title = null;

    super._remove();
  }
}
