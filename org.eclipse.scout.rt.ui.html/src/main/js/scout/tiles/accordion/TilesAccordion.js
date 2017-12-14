/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TilesAccordion = function() {
  scout.TilesAccordion.parent.call(this);
  this.gridColumnCount = null;
  this.multiSelect = null;
  this.selectable = null;
  this.tileGridLayoutConfig = null;
  this.tilesSelectionHandler = new scout.TilesAccordionSelectionHandler(this);
  this.withPlaceholders = null;
  this._selectionUpdateLocked = false;
};
scout.inherits(scout.TilesAccordion, scout.Accordion);

scout.TilesAccordion.prototype._render = function() {
  scout.TilesAccordion.parent.prototype._render.call(this);
  this.$container.addClass('tiles-accordion');
};

scout.TilesAccordion.prototype._initGroup = function(group) {
  scout.TilesAccordion.parent.prototype._initGroup.call(this, group);
  group.body.setSelectionHandler(this.tilesSelectionHandler);

  // Copy properties from accordion to new group. If the properties are not set yet, copy them from the group to the accordion
  // This gives the possibility to either define the properties on the accordion or on the tileGrid initially
  if (this.gridColumnCount !== null) {
    group.body.setGridColumnCount(this.gridColumnCount);
  }
  this.setProperty('gridColumnCount', group.body.gridColumnCount);

  if (this.multiSelect !== null) {
    group.body.setMultiSelect(this.multiSelect);
  }
  this.setProperty('multiSelect', group.body.multiSelect);

  if (this.selectable !== null) {
    group.body.setSelectable(this.selectable);
  }
  this.setProperty('selectable', group.body.selectable);

  if (this.tileGridLayoutConfig !== null) {
    group.body.setLayoutConfig(this.tileGridLayoutConfig);
  }
  this.setProperty('tileGridLayoutConfig', group.body.layoutConfig);

  if (this.withPlaceholders !== null) {
    group.body.setWithPlaceholders(this.withPlaceholders);
  }
  this.setProperty('withPlaceholders', group.body.withPlaceholders);

  group.body.on('propertyChange', this._onTileGridPropertyChange.bind(this));
};

scout.TilesAccordion.prototype.setGridColumnCount = function(gridColumnCount) {
  this.groups.forEach(function(group) {
    group.body.setGridColumnCount(gridColumnCount);
  });
  this._setProperty('gridColumnCount', gridColumnCount);
};

scout.TilesAccordion.prototype.setTileGridLayoutConfig = function(layoutConfig) {
  this.groups.forEach(function(group) {
    group.body.setLayoutConfig(layoutConfig);
    layoutConfig = group.body.layoutConfig; // May be converted from plain object to TilesLayoutConfig
  });
  this._setProperty('tileGridLayoutConfig', layoutConfig);
};

scout.TilesAccordion.prototype.setWithPlaceholders = function(withPlaceholders) {
  this.groups.forEach(function(group) {
    group.body.setWithPlaceholders(withPlaceholders);
  });
  this._setProperty('withPlaceholders', withPlaceholders);
};

scout.TilesAccordion.prototype.setSelectable = function(selectable) {
  this.groups.forEach(function(group) {
    group.body.setSelectable(selectable);
  });
  this._setProperty('selectable', selectable);
};

scout.TilesAccordion.prototype.setMultiSelect = function(multiSelect) {
  this.groups.forEach(function(group) {
    group.body.setMultiSelect(multiSelect);
  });
  this._setProperty('multiSelect', multiSelect);
};

scout.TilesAccordion.prototype.getGroupById = function(id) {
  return scout.arrays.find(this.groups, function(group) {
    return group.id === id;
  });
};

scout.TilesAccordion.prototype.getGroupByTile = function(tile) {
  return tile.findParent(function(parent) {
    return parent instanceof scout.Group;
  });
};

/**
 * Distribute the tiles to the corresponding groups and returns an object with group id as key and array of tiles as value.
 * Always returns all current groups even if the given tiles may not be distributed to all groups.
 */
scout.TilesAccordion.prototype._groupTiles = function(tiles) {
  // Create a map of groups, key is the id, value is an array of tiles
  var tilesPerGroup = {};
  this.groups.forEach(function(group) {
    tilesPerGroup[group.id] = [];
  });

  // Distribute the tiles to the corresponding groups
  tiles.forEach(function(tile) {
    var group = this.getGroupByTile(tile);
    if (!group) {
      throw new Error('No group found for tile ' + tile.id);
    }
    if (!tilesPerGroup[group.id]) {
      tilesPerGroup[group.id] = [];
    }
    tilesPerGroup[group.id].push(tile);
  }, this);

  return tilesPerGroup;
};

scout.TilesAccordion.prototype.deleteTile = function(tile) {
  this.deleteTiles([tile]);
};

scout.TilesAccordion.prototype.deleteTiles = function(tilesToDelete, appendPlaceholders) {
  tilesToDelete = scout.arrays.ensure(tilesToDelete);
  if (tilesToDelete.length === 0) {
    return;
  }
  var tiles = this.getTiles();
  scout.arrays.removeAll(tiles, tilesToDelete);
  this.setTiles(tiles, appendPlaceholders);
};

scout.TilesAccordion.prototype.deleteAllTiles = function() {
  this.setTiles([]);
};

/**
 * Distributes the given tiles to their corresponding groups.
 * <p>
 * If the list contains new tiles not assigned to a group yet, an exception will be thrown.
 */
scout.TilesAccordion.prototype.setTiles = function(tiles) {
  tiles = scout.arrays.ensure(tiles);
  if (scout.objects.equals(this.getTiles(), tiles)) {
    return;
  }

  // Ensure given tiles are real tiles (of type scout.Tile)
  tiles = this._createChildren(tiles);

  // Distribute the tiles to the corresponding groups (result may contain groups without tiles)
  var tilesPerGroup = this._groupTiles(tiles);

  // Update the tile grids
  for (var id in tilesPerGroup) { // NOSONAR
    var group = this.getGroupById(id);
    group.body.setTiles(tilesPerGroup[id]);
  }
};

scout.TilesAccordion.prototype.getTiles = function() {
  var tiles = [];
  this.groups.forEach(function(group) {
    scout.arrays.pushAll(tiles, group.body.tiles);
  });
  return tiles;
};

scout.TilesAccordion.prototype.getTileCount = function() {
  var count = 0;
  this.groups.forEach(function(group) {
    count += group.body.tiles.length;
  });
  return count;
};

scout.TilesAccordion.prototype.getFilteredTiles = function() {
  var tiles = [];
  this.groups.forEach(function(group) {
    scout.arrays.pushAll(tiles, group.body.filteredTiles);
  });
  return tiles;
};

scout.TilesAccordion.prototype.getFilteredTileCount = function() {
  var count = 0;
  this.groups.forEach(function(group) {
    count += group.body.filteredTiles.length;
  });
  return count;
};

/**
 * Selects the given tiles and deselects the previously selected ones.
 */
scout.TilesAccordion.prototype.selectTiles = function(tiles) {
  tiles = scout.arrays.ensure(tiles);
  // Ensure given tiles are real tiles (of type scout.Tile)
  tiles = this._createChildren(tiles);

  // Split tiles into separate lists for each group (result may contain groups without tiles)
  var tilesPerGroup = this._groupTiles(tiles);

  // Select the tiles in the the corresponding tile grids
  for (var id in tilesPerGroup) { // NOSONAR
    var group = this.getGroupById(id);
    group.body.selectTiles(tilesPerGroup[id]);
  }
};

scout.TilesAccordion.prototype.selectTile = function(tile) {
  this.selectTiles([tile]);
};

/**
 * Selects all visible tiles
 */
scout.TilesAccordion.prototype.selectAllTiles = function(tile) {
  this.selectTiles(this.getTiles());
};

scout.TilesAccordion.prototype.deselectTiles = function(tiles) {
  tiles = scout.arrays.ensure(tiles);
  var selectedTiles = this.getSelectedTiles().slice();
  if (scout.arrays.removeAll(selectedTiles, tiles)) {
    this.selectTiles(selectedTiles);
  }
};

scout.TilesAccordion.prototype.deselectTile = function(tile) {
  this.deselectTiles([tile]);
};

scout.TilesAccordion.prototype.deselectAllTiles = function(tiles) {
  this.selectTiles([]);
};

scout.TilesAccordion.prototype.addTilesToSelection = function(tiles) {
  tiles = scout.arrays.ensure(tiles);
  this.selectTiles(this.getSelectedTiles().concat(tiles));
};

scout.TilesAccordion.prototype.addTileToSelection = function(tile) {
  this.addTilesToSelection([tile]);
};

scout.TilesAccordion.prototype.getSelectedTiles = function() {
  var selectedTiles = [];
  this.groups.forEach(function(group) {
    scout.arrays.pushAll(selectedTiles, group.body.selectedTiles);
  });
  return selectedTiles;
};

scout.TilesAccordion.prototype.getSelectedTile = function() {
  return this.getSelectedTiles()[0];
};

scout.TilesAccordion.prototype.getSelectedTileCount = function() {
  var count = 0;
  this.groups.forEach(function(group) {
    count += group.body.selectedTiles.length;
  });
  return count;
};

scout.TilesAccordion.prototype._onTileGridSelectedTilesChange = function(event) {
  if (this._selectionUpdateLocked) {
    // Don't execute when deselecting other tiles to minimize the amount of property change events
    return;
  }
  var tileGrid = event.source;
  if (!this.multiSelect && tileGrid.selectedTiles.length > 0) {
    this._selectionUpdateLocked = true;
    // Ensure only one grid has a selected tile if multiSelect is false
    this.groups.forEach(function(group) {
      if (group.body !== tileGrid) {
        group.body.deselectAllTiles();
      }
    });
    this._selectionUpdateLocked = false;
  }
  this.triggerPropertyChange('selectedTiles', null, this.getSelectedTiles());
};

scout.TilesAccordion.prototype._onTileGridPropertyChange = function(event) {
  if (event.propertyName === 'selectedTiles') {
    this._onTileGridSelectedTilesChange(event);
  }
};
