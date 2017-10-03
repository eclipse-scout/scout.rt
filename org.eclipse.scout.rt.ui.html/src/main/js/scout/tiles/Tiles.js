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
  // GridColumnCount will be modified by the layout, prefGridColumnCount remains unchanged
  this.gridColumnCount = 4;
  this.prefGridColumnCount = this.gridColumnCount;
  this.logicalGrid = scout.create('scout.HorizontalGrid');
  this.logicalGridHGap = 15;
  this.logicalGridVGap = 20;
  this.logicalGridColumnWidth = 200;
  this.logicalGridRowHeight = 150;
  this.withPlaceholders = false;
  this.scrollable = true;
  this._addWidgetProperties(['tiles']);
};
scout.inherits(scout.Tiles, scout.Widget);

scout.Tiles.prototype._init = function(model) {
  scout.Tiles.parent.prototype._init.call(this, model);
  this._setGridColumnCount(this.gridColumnCount);
};

scout.Tiles.prototype._render = function() {
  this.$container = this.$parent.appendDiv('tiles');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
};

scout.Tiles.prototype._createLayout = function() {
  return new scout.TilesLayout(this);
};

scout.Tiles.prototype._renderProperties = function() {
  scout.Tiles.parent.prototype._renderProperties.call(this);
  this._renderTiles();
  this._renderLogicalGridHGap();
  this._renderLogicalGridVGap();
  this._renderLogicalGridRowHeight();
  this._renderLogicalGridColumnWidth();
  this._renderScrollable();
};

scout.Tiles.prototype.setTiles = function(tiles) {
  if (scout.objects.equals(this.tiles, tiles)) {
    return;
  }

  tiles = this._prepareProperty('tiles', tiles);
  if (this.rendered) {
    var tilesToRemove = this.tiles.slice();

    // only remove those which are not in the new array
    scout.arrays.removeAll(tilesToRemove, tiles);
    this._removeTiles(tilesToRemove);
  }
  this._setProperty('tiles', tiles);
  if (this.rendered) {
    this._renderTiles();
  }
};

scout.Tiles.prototype._renderTiles = function() {
  this.tiles.forEach(function(tile) {
    if (!tile.rendered) {
      tile.render();
      tile.setLayoutData(new scout.LogicalGridData(tile));
    }
  });
  if (!this.htmlComp.layouting) {
    // no need to invalidate when tile placeholders are added or removed while layouting
    this.invalidateLayoutTree();
  }
};

scout.Tiles.prototype._removeTiles = function(tiles) {
  tiles.forEach(function(tile) {
    tile.remove();
  });
};

scout.Tiles.prototype.addTiles = function(tiles) {
  this.setTiles(this.tiles.concat(tiles));
};

scout.Tiles.prototype.deleteTiles = function(tilesToDelete) {
  var tiles = this.tiles.slice();
  scout.arrays.removeAll(tiles, tilesToDelete);
  this.setTiles(tiles);
};

scout.Tiles.prototype.setGridColumnCount = function(gridColumnCount) {
  this.setProperty('gridColumnCount', gridColumnCount);
};

scout.Tiles.prototype._setGridColumnCount = function(gridColumnCount) {
  this._setProperty('gridColumnCount', gridColumnCount);
  this.prefGridColumnCount = gridColumnCount;
  this.invalidateLogicalGrid();
};

scout.Tiles.prototype.setLogicalGridHGap = function(logicalGridHGap) {
  this.setProperty('logicalGridHGap', logicalGridHGap);
};

scout.Tiles.prototype._renderLogicalGridHGap = function() {
  this.htmlComp.layout.hgap = this.logicalGridHGap;
  this.invalidateLayoutTree();
};

scout.Tiles.prototype.setLogicalGridVGap = function(logicalGridVGap) {
  this.setProperty('logicalGridVGap', logicalGridVGap);
};

scout.Tiles.prototype._renderLogicalGridVGap = function() {
  this.htmlComp.layout.vgap = this.logicalGridVGap;
  this.invalidateLayoutTree();
};

scout.Tiles.prototype.setLogicalGridColumnWidth = function(logicalGridColumnWidth) {
  this.setProperty('logicalGridColumnWidth', logicalGridColumnWidth);
};

scout.Tiles.prototype._renderLogicalGridColumnWidth = function() {
  this.htmlComp.layout.columnWidth = this.logicalGridColumnWidth;
  this.invalidateLayoutTree();
};

scout.Tiles.prototype.setLogicalGridRowHeight = function(logicalGridRowHeight) {
  this.setProperty('logicalGridRowHeight', logicalGridRowHeight);
};

scout.Tiles.prototype._renderLogicalGridRowHeight = function() {
  this.htmlComp.layout.rowHeight = this.logicalGridRowHeight;
  this.invalidateLayoutTree();
};

scout.Tiles.prototype._renderScrollable = function() {
  if (this.scrollable) {
    scout.scrollbars.install(this.$container, {
      parent: this,
      axis: 'y'
    });
  } else {
    scout.scrollbars.uninstall(this.$container, this.session);
  }
};

scout.Tiles.prototype.setWithPlaceholders = function(withPlaceholders) {
  this.setProperty('withPlaceholders', withPlaceholders);
};

scout.Tiles.prototype._renderWithPlaceholders = function() {
  this.invalidateLayoutTree();
};

scout.Tiles.prototype.fillUpWithPlaceholders= function() {
  if (!this.withPlaceholders) {
    this._deleteAllPlaceholders();
    return;
  }
  this._deleteObsoletePlaceholders();
  this._addPlaceholders();
};

scout.Tiles.prototype._createPlaceholders = function() {
  var numPlaceholders, lastX,
    columnCount = this.gridColumnCount,
    tiles = this.tiles,
    placeholders = [];

  if (tiles.length > 0) {
    lastX = tiles[tiles.length - 1].gridData.x;
  } else {
    // If there are no tiles, create one row with placeholders
    lastX = -1;
  }

  if (lastX === columnCount - 1) {
    // If last tile is the last element in the row, don't create placeholders
    return [];
  }

  // Otherwise create placeholders for every missing tile in the last row
  numPlaceholders = columnCount - 1 - lastX;
  for (var i = 0; i < numPlaceholders; i++) {
    placeholders.push(this._createPlaceholder());
  }
  return placeholders;
};

scout.Tiles.prototype._createPlaceholder = function() {
  var placeholder = scout.create('PlaceholderTile', {
    parent: this
  });
  // If the first tile in the box is a tile with a form field, add the class with-form-fields to the placeholder because form field tiles have a mandatory indicator
  // If mixed tiles are used so that the first one is not a form field tile but others are, the class has to be added manually
  if (this.tiles[0] && this.tiles[0] instanceof scout.FormFieldTile) {
    placeholder.addCssClass('with-form-fields');
  }
  return placeholder;
};

scout.Tiles.prototype._deleteObsoletePlaceholders = function() {
  var tiles = [],
    obsolete = false;

  this.tiles.forEach(function(tile) {
    if (!(tile instanceof scout.PlaceholderTile)) {
      tiles.push(tile);
      return;
    }
    // Remove all placeholder in the row if there is one at x=0
    if (tile.gridData.x === 0) {
      obsolete = true;
    }
    if (!obsolete) {
      tiles.push(tile);
    }
  }, this);

  this.setTiles(tiles);
};

scout.Tiles.prototype._deleteAllPlaceholders = function() {
  var tiles = this.tiles.filter(function(tile) {
    return !(tile instanceof scout.PlaceholderTile);
  });
  this.setTiles(tiles);
};

scout.Tiles.prototype._addPlaceholders = function() {
  var placeholders = this._createPlaceholders();
  this.addTiles(placeholders);
};

scout.Tiles.prototype.validateLogicalGrid = function() {
  if (!this.logicalGrid.dirty) {
    return;
  }
  this.logicalGrid.validate(this);
  this.fillUpWithPlaceholders();
  this.logicalGrid.setDirty(true);
  this.logicalGrid.validate(this);
};

/**
 * @override
 */
scout.Tiles.prototype._setLogicalGrid = function(logicalGrid) {
  scout.Tiles.parent.prototype._setLogicalGrid.call(this, logicalGrid);
  if (this.logicalGrid) {
    this.logicalGrid.setGridConfig(new scout.TilesGridConfig());
  }
};
