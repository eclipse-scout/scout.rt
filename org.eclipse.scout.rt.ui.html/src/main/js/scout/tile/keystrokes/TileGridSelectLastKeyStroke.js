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
scout.TileGridSelectLastKeyStroke = function(tileGrid) {
  scout.TileGridSelectLastKeyStroke.parent.call(this);
  this.field = tileGrid;
  this.repeatable = true;
  this.which = [scout.keys.END];
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var tiles = this._computeNewSelection();
    if (tiles && tiles.length > 0) {
      return scout.arrays.last(tiles).$container;
    }
  }.bind(this);
};
scout.inherits(scout.TileGridSelectLastKeyStroke, scout.TileGridSelectKeyStroke);

scout.TileGridSelectLastKeyStroke.prototype.handle = function(event) {
  var tileGrid = this.field;
  var newSelectedTiles = this._computeNewSelection();

  if (newSelectedTiles && newSelectedTiles.length > 0) {
    tileGrid.selectTiles(newSelectedTiles);
    tileGrid.scrollTo(scout.arrays.last(newSelectedTiles));
  }
};

scout.TileGridSelectLastKeyStroke.prototype._computeNewSelection = function() {
  var tileGrid = this.field;
  var tiles = tileGrid.filteredTiles;
  var selectedTiles = tileGrid.selectedTiles;

  if (selectedTiles.length === 1 && scout.arrays.last(selectedTiles) === scout.arrays.last(tiles)) {
    // Do nothing if last tile is already selected
    return;
  }
  // Select last tile
  return [scout.arrays.last(tiles)];
};
