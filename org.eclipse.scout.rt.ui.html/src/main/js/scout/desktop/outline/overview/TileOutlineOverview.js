/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TileOutlineOverview = function() {
  scout.TileOutlineOverview.parent.call(this);
  this.pageTiles = null;
  this.scrollable = true;
  this._addWidgetProperties(['pageTiles']);
};
scout.inherits(scout.TileOutlineOverview, scout.OutlineOverview);

scout.TileOutlineOverview.prototype._init = function(model) {
  scout.TileOutlineOverview.parent.prototype._init.call(this, model);
  if (!this.pageTiles) {
    this.pageTiles = this._createPageTiles();
  }
};

scout.TileOutlineOverview.prototype._render = function() {
  this.$container = this.$parent.appendDiv('tile-outline-overview');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.TileOutlineOverviewLayout(this));
  this.$content = this.$container.appendDiv('tile-outline-overview-content');
  this.contentHtmlComp = scout.HtmlComponent.install(this.$content, this.session);
  this.$title = this.$content.appendDiv('tile-outline-overview-title').text(this.outline.title);
};

scout.TileOutlineOverview.prototype._renderProperties = function() {
  scout.TileOutlineOverview.parent.prototype._renderProperties.call(this);
  this._renderPageTiles();
  this._renderScrollable();
};

scout.TileOutlineOverview.prototype._renderPageTiles = function() {
  this.pageTiles.render(this.$content);
};

scout.TileOutlineOverview.prototype._createPageTiles = function() {
  return scout.create('PageTiles', {
    parent: this,
    outline: this.outline
  });
};

scout.TileOutlineOverview.prototype._renderScrollable = function() {
  if (this.scrollable) {
    scout.scrollbars.install(this.$container, {
      parent: this,
      axis: 'y'
    });
  } else {
    scout.scrollbars.uninstall(this.$container, this.session);
  }
};
