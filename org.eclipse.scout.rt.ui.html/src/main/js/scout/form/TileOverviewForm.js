/*******************************************************************************
 * Copyright (c) 2019-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TileOverviewForm = function() {
  scout.TileOverviewForm.parent.call(this);

  this.outline = null;
  this.nodes = null;
  this.tileOverviewTitle = null;
  this.scrollable = true;
  this._addWidgetProperties(['pageTileGrid']);

  this.$content = null;
  this.$title = null;
};
scout.inherits(scout.TileOverviewForm, scout.Form);

scout.TileOverviewForm.prototype._init = function(model) {
  scout.TileOverviewForm.parent.prototype._init.call(this, model);
  if (!this.pageTileGrid) {
    this.pageTileGrid = this._createPageTileGrid();
  }
};

scout.TileOverviewForm.prototype._renderForm = function() {
  scout.TileOverviewForm.parent.prototype._renderForm.call(this);
  this.htmlComp.setLayout(new scout.TileOverviewLayout(this));
  this.$content = this.$container.appendDiv('tile-overview-content');
  this.contentHtmlComp = scout.HtmlComponent.install(this.$content, this.session);
  this.$title = this.$content.appendDiv('tile-overview-title').text(this.tileOverviewTitle);
};

scout.TileOverviewForm.prototype._renderProperties = function() {
  scout.TileOverviewForm.parent.prototype._renderProperties.call(this);
  if (this.pageTileGrid.rendered) {
    this.pageTileGrid = this._createPageTileGrid();
  }
  this._renderPageTileGrid();
  this._renderScrollable();
};

scout.TileOverviewForm.prototype._renderPageTileGrid = function() {
  this.pageTileGrid.render(this.$content);
};

scout.TileOverviewForm.prototype._createPageTileGrid = function() {
  return scout.create('PageTileGrid', {
    parent: this,
    outline: this.outline,
    nodes: this.nodes
  });
};

scout.TileOverviewForm.prototype._renderScrollable = function() {
  if (this.scrollable) {
    this._installScrollbars({
      axis: 'y'
    });
  } else {
    this._uninstallScrollbars();
  }
};

scout.TileOverviewForm.prototype.setPage = function(page) {
  this.outline = page.getOutline();
  this.nodes = page.childNodes;
  this.tileOverviewTitle = page.text;

  this.pageTileGrid.setOutline(this.outline);
  this.pageTileGrid.setPage(page);
  this.pageTileGrid.setNodes(this.nodes);
};

scout.TileOverviewForm.prototype._remove = function() {
  this.$content = null;
  this.$title = null;

  scout.TileOverviewForm.parent.prototype._remove.call(this);
};
