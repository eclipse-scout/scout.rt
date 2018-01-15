/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TileGridSelectionHandler = function(tileGrid) {
  this.tileGrid = tileGrid;
};

scout.TileGridSelectionHandler.prototype.selectTileOnMouseDown = function(event) {
  if (!this.isSelectable()) {
    return;
  }

  var $tile = $(event.currentTarget);
  var tile = $tile.data('widget');

  if (tile instanceof scout.PlaceholderTile) {
    return;
  }
  if (tile.selected && event.which === 3) {
    // Do not toggle if context menus should be shown and tile already is selected
    return;
  }

  // Clicking a tile selects it, CTRL-click toggles the selection
  var selected = true;
  if (event.ctrlKey) {
    selected = !tile.selected;
  }

  // If multiSelect is enabled, CTRL-Click on a tile adds or removes that tile to or from the selection
  if (event.ctrlKey && this.isMultiSelect()) {
    if (selected) {
      this.addTilesToSelection(tile);
      this.setFocusedTile(tile);
    } else {
      this.deselectTile(tile);
      this.setFocusedTile(null);
    }
    return;
  }

  // Shift-Click adds or removes the tiles between the last focused tile and the clicked tile to or from the selection
  if (event.shiftKey && this.isMultiSelect()) {
    var tiles = this.getVisibleTiles();
    var focusedTile = this.getFocusedTile();
    if (!focusedTile) {
      focusedTile = tiles[0];
    }
    var result = this.computeSelectionBetween(tiles.indexOf(focusedTile), tiles.indexOf(tile), true);
    if (result) {
      this.selectTiles(result.selectedTiles);
      this.setFocusedTile(result.focusedTile);
    }
    return;
  }

  // If multi selection is disabled or no CTRL key is pressed, only the clicked tile may be selected
  if (selected) {
    this.selectTile(tile);
    this.setFocusedTile(tile);
  } else {
    this.deselectAllTiles();
    this.setFocusedTile(null);
  }
};

scout.TileGridSelectionHandler.prototype.getFilteredTiles = function() {
  return this.tileGrid.filteredTiles;
};

scout.TileGridSelectionHandler.prototype.getFilteredTileCount = function() {
  return this.tileGrid.filteredTiles.length;
};

scout.TileGridSelectionHandler.prototype.getVisibleTiles = function() {
  return this.tileGrid.filteredTiles;
};

scout.TileGridSelectionHandler.prototype.getVisibleTileCount = function() {
  return this.tileGrid.filteredTiles.length;
};

scout.TileGridSelectionHandler.prototype.getGridColumnCount = function() {
  return this.tileGrid.gridColumnCount;
};

scout.TileGridSelectionHandler.prototype.getVisibleGridRowCount = function() {
  return this.tileGrid.logicalGrid.gridRows;
};

scout.TileGridSelectionHandler.prototype.getVisibleGridX = function(tile) {
  return tile.gridData.x;
};

scout.TileGridSelectionHandler.prototype.getVisibleGridY = function(tile) {
  return tile.gridData.y;
};

scout.TileGridSelectionHandler.prototype.getSelectedTiles = function(event) {
  return this.tileGrid.selectedTiles;
};

scout.TileGridSelectionHandler.prototype.isSelectable = function() {
  return this.tileGrid.selectable;
};

scout.TileGridSelectionHandler.prototype.isMultiSelect = function() {
  return this.tileGrid.multiSelect;
};

scout.TileGridSelectionHandler.prototype.addTilesToSelection = function(tiles) {
  this.tileGrid.addTilesToSelection(tiles);
};

scout.TileGridSelectionHandler.prototype.selectTile = function(tile) {
  this.tileGrid.selectTile(tile);
};

scout.TileGridSelectionHandler.prototype.selectTiles = function(tiles) {
  this.tileGrid.selectTiles(tiles);
};

scout.TileGridSelectionHandler.prototype.deselectTile = function(tile) {
  this.tileGrid.deselectTile(tile);
};

scout.TileGridSelectionHandler.prototype.deselectTiles = function(tiles) {
  this.tileGrid.deselectTiles(tiles);
};

scout.TileGridSelectionHandler.prototype.deselectAllTiles = function() {
  this.tileGrid.deselectAllTiles();
};

scout.TileGridSelectionHandler.prototype.toggleSelection = function() {
  this.tileGrid.toggleSelection();
};

scout.TileGridSelectionHandler.prototype.getFocusedTile = function() {
  return this.tileGrid.focusedTile;
};

scout.TileGridSelectionHandler.prototype.setFocusedTile = function(tile) {
  this.tileGrid.setFocusedTile(tile);
};

scout.TileGridSelectionHandler.prototype.scrollTo = function(tile) {
  this.tileGrid.scrollTo(tile);
};

scout.TileGridSelectionHandler.prototype.findVisibleTileIndexAt = function(x, y, startIndex, reverse) {
  return this.tileGrid.findTileIndexAt(x, y, startIndex, reverse);
};

scout.TileGridSelectionHandler.prototype.getTileGridByRow = function(rowIndex) {
  if (rowIndex < 0 || rowIndex >= this.getVisibleGridRowCount()) {
    return null;
  }
  return this.tileGrid;
};

scout.TileGridSelectionHandler.prototype.computeSelectionBetween = function(focusedTileIndex, newFocusedTileIndex, extend) {
  var tiles = this.getVisibleTiles();
  var selectedTiles = this.getSelectedTiles();
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
      newFocusedTile = this._findLastSelectedTileAfter(tiles, newFocusedTileIndex);
    } else {
      newSelectedTiles = scout.arrays.union(tiles.slice(newFocusedTileIndex, focusedTileIndex + 1), selectedTiles);
      newFocusedTile = this._findLastSelectedTileBefore(tiles, newFocusedTileIndex);
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
scout.TileGridSelectionHandler.prototype._findLastSelectedTileBefore = function(tiles, tileIndex) {
  if (tileIndex === 0) {
    return tiles[tileIndex];
  }
  var tile = scout.arrays.findFromReverse(tiles, tileIndex, function(tile, i) {
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
scout.TileGridSelectionHandler.prototype._findLastSelectedTileAfter = function(tiles, tileIndex) {
  if (tileIndex === tiles.length - 1) {
    return tiles[tileIndex];
  }
  var tile = scout.arrays.findFrom(tiles, tileIndex, function(tile, i) {
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
