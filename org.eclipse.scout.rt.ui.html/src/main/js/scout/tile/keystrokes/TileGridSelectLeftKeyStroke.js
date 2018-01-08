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
scout.TileGridSelectLeftKeyStroke = function(tileGrid) {
  scout.TileGridSelectLeftKeyStroke.parent.call(this);
  this.field = tileGrid;
  this.repeatable = true;
  this.which = [scout.keys.LEFT];
  this.renderingHints.text = '←';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var tiles = this._computeNewSelection();
    if (tiles && tiles.length > 0) {
      return tiles[0].$container;
    }
  }.bind(this);
};
scout.inherits(scout.TileGridSelectLeftKeyStroke, scout.TileGridSelectKeyStroke);

scout.TileGridSelectLeftKeyStroke.prototype.handle = function(event) {
  var tileGrid = this.field;
  var newSelectedTiles = this._computeNewSelection();

  if (newSelectedTiles && newSelectedTiles.length > 0) {
    tileGrid.selectTiles(newSelectedTiles);
    tileGrid.scrollTo(newSelectedTiles[0]);
  }
};

scout.TileGridSelectLeftKeyStroke.prototype._computeNewSelection = function() {
  var tileGrid = this.field;
  var tiles = tileGrid.filteredTiles;
  var selectedTiles = tileGrid.selectedTiles;
  var selectedTileIndex = tiles.indexOf(selectedTiles[0]);
  var newSelectedTileIndex = [];

  if (selectedTileIndex === 0) {
    // Do nothing if first tile is already selected
    return;
  }
  if (selectedTiles.length === 0) {
    // Select last tile if not tiles are selected
    newSelectedTileIndex = tiles.length - 1;
  } else {
    // Select previous tile
    newSelectedTileIndex = selectedTileIndex - 1;
  }
  return [tiles[newSelectedTileIndex]];
};
