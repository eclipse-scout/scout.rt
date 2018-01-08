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
scout.TileGridSelectDownKeyStroke = function(tileGrid) {
  scout.TileGridSelectDownKeyStroke.parent.call(this);
  this.field = tileGrid;
  this.repeatable = true;
  this.which = [scout.keys.DOWN];
  this.renderingHints.text = '↓';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var tiles = this._computeNewSelection();
    if (tiles && tiles.length > 0) {
      return scout.arrays.last(tiles).$container;
    }
  }.bind(this);
};
scout.inherits(scout.TileGridSelectDownKeyStroke, scout.TileGridSelectKeyStroke);

scout.TileGridSelectDownKeyStroke.prototype.handle = function(event) {
  var tileGrid = this.field;
  var newSelectedTiles = this._computeNewSelection();

  if (newSelectedTiles && newSelectedTiles.length > 0) {
    tileGrid.selectTiles(newSelectedTiles);
    tileGrid.scrollTo(scout.arrays.last(newSelectedTiles));
  }
};

scout.TileGridSelectDownKeyStroke.prototype._computeNewSelection = function() {
  var tileGrid = this.field;
  var tiles = tileGrid.filteredTiles;
  var rowCount = Math.ceil(tiles.length / tileGrid.gridColumnCount);
  var selectedTiles = tileGrid.selectedTiles;
  var selectedTileIndex = tiles.indexOf(scout.arrays.last(selectedTiles));
  var selectedTileRow = Math.floor(selectedTileIndex / tileGrid.gridColumnCount);
  var newSelectedTileIndex = [];

  if (selectedTiles.length > 0 && selectedTileRow === rowCount - 1) {
    // Do nothing if one tile of the last row is already selected
    return;
  }
  if (selectedTiles.length === 0) {
    // Select first tile if no tiles are selected
    newSelectedTileIndex = 0;
  } else {
    // Select next tile
    newSelectedTileIndex = Math.min(tiles.length - 1, selectedTileIndex + tileGrid.gridColumnCount);
  }
  return [tiles[newSelectedTileIndex]];
};
