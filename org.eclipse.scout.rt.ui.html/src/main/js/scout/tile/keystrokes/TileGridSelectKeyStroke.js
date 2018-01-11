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
scout.TileGridSelectKeyStroke = function(tileGrid) {
  scout.TileGridSelectKeyStroke.parent.call(this);
  this.field = tileGrid;
  this.shift = !tileGrid.multiSelect ? false : undefined;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var result = this._computeNewSelection();
    if (result && result.focusedTile) {
      return result.focusedTile.$container;
    }
  }.bind(this);
};
scout.inherits(scout.TileGridSelectKeyStroke, scout.KeyStroke);

scout.TileGridSelectKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TileGridSelectKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }
  if (!this.field.selectable) {
    return false;
  }
  if (this.field.filteredTiles.length === 0) {
    return false;
  }
  return true;
};

scout.TileGridSelectKeyStroke.prototype.handle = function(event) {
  var tileGrid = this.field;
  var result = this._computeNewSelection(event.shiftKey);
  if (!result) {
    return;
  }
  if (result.selectedTiles && result.selectedTiles.length > 0) {
    tileGrid.selectTiles(result.selectedTiles);
    tileGrid.scrollTo(scout.arrays.last(result.selectedTiles));
  }
  tileGrid.setFocusedTile(result.focusedTile);
};

scout.TileGridSelectKeyStroke.prototype._computeNewSelection = function(extend) {
  // To be implemented by subclasses
};

scout.TileGridSelectKeyStroke.prototype._computeSelectionBetween = function(focusedTileIndex, newFocusedTileIndex, extend) {
  var tileGrid = this.field;
  var tiles = tileGrid.filteredTiles;
  var selectedTiles = tileGrid.selectedTiles;
  var newFocusedTile = tiles[newFocusedTileIndex];

  if (focusedTileIndex < 0 || focusedTileIndex > tiles.length - 1 ||
      newFocusedTileIndex < 0 || newFocusedTileIndex > tiles.length - 1 ||
      focusedTileIndex === newFocusedTileIndex) {
    // Do nothing if indices are out of bounds or equal
    return;
  }

  if (!extend) {
    // Select only the tile at the newFocusedTileindex
    return {
      selectedTiles: [newFocusedTile],
      focusedTile: newFocusedTile
    };
  }

  // Adjust existing selection
  var newSelectedTiles = [];
  if (!newFocusedTile.selected) {
    // Add all tiles between focused tile and newly focused tile to selection
    if (newFocusedTileIndex > focusedTileIndex) {
      newSelectedTiles = scout.arrays.union(selectedTiles, tiles.slice(focusedTileIndex, newFocusedTileIndex + 1));
      newFocusedTile = this._findLastSelectedTileAfter(tileGrid, newFocusedTileIndex);
    } else {
      newSelectedTiles = scout.arrays.union(tiles.slice(newFocusedTileIndex, focusedTileIndex + 1), selectedTiles);
      newFocusedTile = this._findLastSelectedTileBefore(tileGrid, newFocusedTileIndex);
    }
  } else {
    // TOOO CGU what is Bug #172929 about? Do we need to consider this as well?
    if (newFocusedTileIndex > focusedTileIndex) {
      // Remove all tiles between focused tile and newly focused tile from selection if newly focused tile already is selected
      newSelectedTiles = selectedTiles.slice();
      scout.arrays.removeAll(newSelectedTiles, tiles.slice(focusedTileIndex, newFocusedTileIndex));
    } else {
      newSelectedTiles = selectedTiles.slice();
      scout.arrays.removeAll(newSelectedTiles, tiles.slice(newFocusedTileIndex + 1, focusedTileIndex + 1));
    }
  }

  return {
    selectedTiles: newSelectedTiles,
    focusedTile: newFocusedTile
  };
};

/**
 * Searches for the last selected tile in the current selection block, starting from tileIndex. Expects tile at tileIndex to be selected.
 */
scout.TileGridSelectKeyStroke.prototype._findLastSelectedTileBefore = function(tileGrid, tileIndex) {
  var tile, tiles = tileGrid.filteredTiles;
  if (tileIndex === 0) {
    return tiles[tileIndex];
  }
  tile = scout.arrays.findFromReverse(tiles, tileIndex, function(tile, i) {
    var previousTile = tiles[i - 1];
    if (!previousTile) {
      return false;
    }
    return !previousTile.selected;
  });
  // when no tile has been found, use first tile in tileGrid
  if (!tile) {
    tile = tiles[0];
  }
  return tile;
};

/**
 * Searches for the last selected tile in the current selection block, starting from tileIndex. Expects tile at tileIndex to be selected.
 */
scout.TileGridSelectKeyStroke.prototype._findLastSelectedTileAfter = function(tileGrid, tileIndex) {
  var tile, tiles = tileGrid.filteredTiles;
  if (tileIndex === tiles.length - 1) {
    return tiles[tileIndex];
  }
  tile = scout.arrays.findFrom(tiles, tileIndex, function(tile, i) {
    var nextTile = tiles[i + 1];
    if (!nextTile) {
      return false;
    }
    return !nextTile.selected;
  });
  // when no tile has been found, use last tile in tileGrid
  if (!tile) {
    tile = tiles[tiles.length - 1];
  }
  return tile;
};
