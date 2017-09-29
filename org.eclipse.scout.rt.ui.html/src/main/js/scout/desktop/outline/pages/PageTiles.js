/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.PageTiles = function() {
  scout.PageTiles.parent.call(this);
  this.outline = null;
  this.withPlaceholders = true;
  this.scrollable = false;
  this._outlineNodeChangedHandler = this._onOutlineNodeChanged.bind(this);
  this._outlineStructureChangedHandler = this._onOutlineStructureChanged.bind(this);
};
scout.inherits(scout.PageTiles, scout.Tiles);

scout.PageTiles.prototype._init = function(model) {
  scout.PageTiles.parent.prototype._init.call(this, model);
  this._setOutline(this.outline);
};

scout.PageTiles.prototype._destroy = function() {
  this._setOutline(null);
  scout.PageTiles.parent.prototype._destroy.call(this);
};

/**
 * @override
 */
scout.PageTiles.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

/**
 * @override
 */
scout.PageTiles.prototype._initKeyStrokeContext = function() {
  scout.PageTiles.parent.prototype._initKeyStrokeContext.call(this);
  this.keyStrokeContext.$bindTarget = this.session.$entryPoint;
  this.keyStrokeContext.registerKeyStroke([
    new scout.PageTilesSelectKeyStroke(this)
  ]);
};

scout.PageTiles.prototype._setOutline = function(outline) {
  var tiles = [];
  if (this.outline) {
    this.outline.off('nodeChanged', this._outlineNodeChangedHandler);
    this.outline.off('nodesDeleted', this._outlineStructureChangedHandler);
    this.outline.off('nodesInserted', this._outlineStructureChangedHandler);
    this.outline.off('allChildNodesDeleted', this._outlineStructureChangedHandler);
    this.outline.off('childNodeOrderChanged', this._outlineStructureChangedHandler);
  }
  this._setProperty('outline', outline);
  if (this.outline) {
    this.outline.on('nodeChanged', this._outlineNodeChangedHandler);
    this.outline.on('nodesDeleted', this._outlineStructureChangedHandler);
    this.outline.on('nodesInserted', this._outlineStructureChangedHandler);
    this.outline.on('allChildNodesDeleted', this._outlineStructureChangedHandler);
    this.outline.on('childNodeOrderChanged', this._outlineStructureChangedHandler);
    tiles = this._createPageTiles(this.outline.nodes);
  }
  this.setTiles(tiles);
};

scout.PageTiles.prototype._createPageTiles = function(pages) {
  var tiles = [];
  pages.forEach(function(page) {
    var tile = this._createPageTile(page);
    tiles.push(tile);
  }, this);
  return tiles;
};

scout.PageTiles.prototype._createPageTile = function(page) {
  var button = scout.create('PageTileButton', {
    parent: this,
    outline: this.outline,
    page: page
  });
  var tile = scout.create('FormFieldTile', {
    parent: this,
    colorScheme: 'default-inverted',
    refWidget: button
  });
  page.tile = tile;
  return tile;
};

scout.PageTiles.prototype._rebuild = function() {
  this.setTiles(this._createPageTiles(this.outline.nodes));
};

scout.PageTiles.prototype._onOutlineNodeChanged = function(event) {
  var tile = event.node.tile;
  if (!tile) {
    return;
  }
  tile.field.notifyPageChanged();
};

scout.PageTiles.prototype._onOutlineStructureChanged = function(event) {
  if (event.parentNode) {
    // only rebuild if top level nodes change
    return;
  }

  this._rebuild();
};
