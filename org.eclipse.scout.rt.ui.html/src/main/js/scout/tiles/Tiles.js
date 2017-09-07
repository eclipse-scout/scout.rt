/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Tiles = function() {
  scout.Tiles.parent.call(this);
  this.initialAnimationDone = false;
  this.tiles = [];
  this.gridColumnCount = 4;
  this.logicalGrid = scout.create('scout.TilesGrid');
  this.logicalGridHGap = 15;
  this.logicalGridVGap = 20;
  this.logicalGridRowHeight = 150;
  this.logicalGridColumnWidth = 200;
  this._addWidgetProperties(['tiles']);
};
scout.inherits(scout.Tiles, scout.Widget);

scout.Tiles.prototype._init = function(model) {
  scout.Tiles.parent.prototype._init.call(this, model);
};

scout.Tiles.prototype._render = function() {
  this.$container = this.$parent.appendDiv('tiles');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.TilesLayout(this));
};

scout.Tiles.prototype._renderProperties = function() {
  scout.Tiles.parent.prototype._renderProperties.call(this);
  this._renderTiles();
  this._renderLogicalGridHGap();
  this._renderLogicalGridVGap();
  this._renderLogicalGridRowHeight();
  this._renderLogicalGridColumnWidth();
};

scout.Tiles.prototype.setTiles = function(tiles) {
  this.setProperty('tiles', tiles);
};

scout.Tiles.prototype._renderTiles = function() {
  this.tiles.forEach(function(tile) {
    tile.render();
    tile.setLayoutData(new scout.LogicalGridData(tile));
  }, this);
  this.invalidateLayoutTree();
};

scout.Tiles.prototype._renderLogicalGridHGap = function() {
  this.htmlComp.layout.hgap = this.logicalGridHGap;
  this.invalidateLayoutTree();
};

scout.Tiles.prototype._renderLogicalGridVGap = function() {
  this.htmlComp.layout.vgap = this.logicalGridVGap;
  this.invalidateLayoutTree();
};

scout.Tiles.prototype._renderLogicalGridRowHeight = function() {
  this.htmlComp.layout.rowHeight = this.logicalGridRowHeight;
  this.invalidateLayoutTree();
};

scout.Tiles.prototype._renderLogicalGridColumnWidth = function() {
  this.htmlComp.layout.columnWidth = this.logicalGridColumnWidth;
  this.invalidateLayoutTree();
};
