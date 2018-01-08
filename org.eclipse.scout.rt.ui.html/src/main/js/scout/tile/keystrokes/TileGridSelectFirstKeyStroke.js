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
scout.TileGridSelectFirstKeyStroke = function(tileGrid) {
  scout.TileGridSelectFirstKeyStroke.parent.call(this);
  this.field = tileGrid;
  this.repeatable = true;
  this.which = [scout.keys.HOME];
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var tiles = this._computeNewSelection();
    if (tiles && tiles.length > 0) {
      return tiles[0].$container;
    }
  }.bind(this);
};
scout.inherits(scout.TileGridSelectFirstKeyStroke, scout.TileGridSelectKeyStroke);

scout.TileGridSelectFirstKeyStroke.prototype.handle = function(event) {
  var tileGrid = this.field;
  var newSelectedTiles = this._computeNewSelection();

  if (newSelectedTiles && newSelectedTiles.length > 0) {
    tileGrid.selectTiles(newSelectedTiles);
    tileGrid.scrollTo(newSelectedTiles[0]);
  }
};

scout.TileGridSelectFirstKeyStroke.prototype._computeNewSelection = function() {
  var tileGrid = this.field;
  var tiles = tileGrid.filteredTiles;
  var selectedTiles = tileGrid.selectedTiles;

  if (selectedTiles.length === 1 && selectedTiles[0] === tiles[0]) {
    // Do nothing if first tile is already selected
    return;
  }
  // Select first tile
  return [tiles[0]];
};
