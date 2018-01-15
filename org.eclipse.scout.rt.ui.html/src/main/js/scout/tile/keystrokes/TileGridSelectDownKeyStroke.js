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
  scout.TileGridSelectDownKeyStroke.parent.call(this, tileGrid);
  this.stopPropagation = true;
  this.repeatable = true;
  this.which = [scout.keys.DOWN];
  this.renderingHints.text = '↓';
};
scout.inherits(scout.TileGridSelectDownKeyStroke, scout.TileGridSelectKeyStroke);

scout.TileGridSelectDownKeyStroke.prototype._computeNewSelection = function(extend) {
  var tiles = this.getSelectionHandler().getVisibleTiles();
  var selectedTiles = this.getSelectionHandler().getSelectedTiles();
  var focusedTile = this.getSelectionHandler().getFocusedTile();
  var focusedTileRow = -1;
  var focusedTileColumn = -1;
  var focusedTileIndex = -1;
  var rowCount = this.getSelectionHandler().getVisibleGridRowCount();

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
  focusedTileRow = this.getSelectionHandler().getVisibleGridY(focusedTile);
  focusedTileColumn = this.getSelectionHandler().getVisibleGridX(focusedTile);
  if (focusedTileRow === rowCount - 1) {
    // Do nothing if focused tile is in the last row
    return;
  }

  var newFocusedTileIndex = this.getSelectionHandler().findVisibleTileIndexAt(focusedTileColumn, focusedTileRow + 1, focusedTileIndex);
  if (newFocusedTileIndex < 0) {
    var tileGrid = this.getSelectionHandler().getTileGridByRow(focusedTileRow + 1);
    if (!tileGrid) {
      return;
    }
    newFocusedTileIndex = tiles.indexOf(scout.arrays.last(tileGrid.filteredTiles));
  }
  return this.getSelectionHandler().computeSelectionBetween(focusedTileIndex, newFocusedTileIndex, extend);
};
