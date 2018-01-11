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
  scout.TileGridSelectUpKeyStroke.parent.call(this, tileGrid);
  this.stopPropagation = true;
  this.repeatable = true;
  this.which = [scout.keys.UP];
  this.renderingHints.text = '↑';
};
scout.inherits(scout.TileGridSelectUpKeyStroke, scout.TileGridSelectKeyStroke);

scout.TileGridSelectUpKeyStroke.prototype._computeNewSelection = function(extend) {
  var tileGrid = this.field;
  var tiles = tileGrid.filteredTiles;
  var selectedTiles = tileGrid.selectedTiles;
  var focusedTile = tileGrid.focusedTile;
  var focusedTileIndex = -1;

  if (selectedTiles.length === 0) {
    // Select last tile if no tiles are selected
    focusedTile = scout.arrays.last(tiles);
    return {
      selectedTiles: [focusedTile],
      focusedTile: focusedTile
    };
  }

  // Focused tile may be null if tile has been deleted or if the user has not made a selection before
  if (!focusedTile) {
    focusedTile = scout.arrays.first(selectedTiles);
  }

  focusedTileIndex = tiles.indexOf(focusedTile);
  return this._computeSelectionBetween(focusedTileIndex, focusedTileIndex - tileGrid.gridColumnCount, extend);
};
