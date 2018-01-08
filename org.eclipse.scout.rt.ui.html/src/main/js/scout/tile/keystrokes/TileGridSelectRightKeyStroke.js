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
scout.TileGridSelectRightKeyStroke = function(tileGrid) {
  scout.TileGridSelectRightKeyStroke.parent.call(this);
  this.field = tileGrid;
  this.repeatable = true;
  this.which = [scout.keys.RIGHT];
  this.renderingHints.text = '→';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var tiles = this._computeNewSelection();
    if (tiles && tiles.length > 0) {
      return scout.arrays.last(tiles).$container;
    }
  }.bind(this);
};
scout.inherits(scout.TileGridSelectRightKeyStroke, scout.TileGridSelectKeyStroke);

scout.TileGridSelectRightKeyStroke.prototype.handle = function(event) {
  var tileGrid = this.field;
  var newSelectedTiles = this._computeNewSelection();

  if (newSelectedTiles && newSelectedTiles.length > 0) {
    tileGrid.selectTiles(newSelectedTiles);
    tileGrid.scrollTo(scout.arrays.last(newSelectedTiles));
  }
};

scout.TileGridSelectRightKeyStroke.prototype._computeNewSelection = function() {
  var tileGrid = this.field;
  var tiles = tileGrid.filteredTiles;
  var selectedTiles = tileGrid.selectedTiles;
  var selectedTileIndex = tiles.indexOf(scout.arrays.last(selectedTiles));
  var newSelectedTileIndex = [];

  if (scout.arrays.last(selectedTiles) === scout.arrays.last(tiles)) {
    // Do nothing if last tile is already selected
    return;
  }
  if (selectedTiles.length === 0) {
    // Select first tile if no tiles are selected
    newSelectedTileIndex = 0;
  } else {
    // Select next tile
    newSelectedTileIndex = selectedTileIndex + 1;
  }
  return [tiles[newSelectedTileIndex]];
};
