/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TileGridSelectUpKeyStroke = function(tileGrid) {
  scout.TileGridSelectUpKeyStroke.parent.call(this);
  this.field = tileGrid;
  this.repeatable = true;
  this.which = [scout.keys.UP];
  this.renderingHints.text = '↑';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var tiles = this._computeNewSelection();
    if (tiles && tiles.length > 0) {
      return tiles[0].$container;
    }
  }.bind(this);
};
scout.inherits(scout.TileGridSelectUpKeyStroke, scout.TileGridSelectKeyStroke);

scout.TileGridSelectUpKeyStroke.prototype.handle = function(event) {
  var tileGrid = this.field;
  var newSelectedTiles = this._computeNewSelection();

  if (newSelectedTiles && newSelectedTiles.length > 0) {
    tileGrid.selectTiles(newSelectedTiles);
    tileGrid.scrollTo(newSelectedTiles[0]);
  }
};

scout.TileGridSelectUpKeyStroke.prototype._computeNewSelection = function() {
  var tileGrid = this.field;
  var tiles = tileGrid.filteredTiles;
  var selectedTiles = tileGrid.selectedTiles;
  var selectedTileIndex = tiles.indexOf(selectedTiles[0]);
  var selectedTileRow = Math.floor(selectedTileIndex / tileGrid.gridColumnCount);
  var newSelectedTileIndex = [];

  if (selectedTiles.length > 0 && selectedTileRow === 0) {
    // Do nothing if one tile of the first row is already selected
    return;
  }
  if (selectedTiles.length === 0) {
    // Select last tile if not tiles are selected
    newSelectedTileIndex = tiles.length - 1;
  } else {
    // Select tile above
    newSelectedTileIndex = selectedTileIndex - tileGrid.gridColumnCount;
  }
  return [tiles[newSelectedTileIndex]];
};
