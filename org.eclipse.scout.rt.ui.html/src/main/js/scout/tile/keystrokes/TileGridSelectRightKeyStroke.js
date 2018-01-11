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
  scout.TileGridSelectRightKeyStroke.parent.call(this, tileGrid);
  this.stopPropagation = true;
  this.repeatable = true;
  this.which = [scout.keys.RIGHT];
  this.renderingHints.text = '→';
};
scout.inherits(scout.TileGridSelectRightKeyStroke, scout.TileGridSelectKeyStroke);

scout.TileGridSelectRightKeyStroke.prototype._computeNewSelection = function(extend) {
  var tileGrid = this.field;
  var tiles = tileGrid.filteredTiles;
  var selectedTiles = tileGrid.selectedTiles;
  var focusedTile = tileGrid.focusedTile;
  var focusedTileIndex = -1;

  if (selectedTiles.length === 0) {
    // Select first tile if no tiles are selected
    focusedTile = scout.arrays.first(tiles);
    return {
      selectedTiles: [focusedTile],
      focusedTile: focusedTile
    };
  }

  // Focused tile may be null if tile has been deleted or if the user has not made a selection before
  if (!focusedTile) {
    focusedTile = scout.arrays.last(selectedTiles);
  }

  focusedTileIndex = tiles.indexOf(focusedTile);
  return this._computeSelectionBetween(focusedTileIndex, focusedTileIndex + 1, extend);
};
