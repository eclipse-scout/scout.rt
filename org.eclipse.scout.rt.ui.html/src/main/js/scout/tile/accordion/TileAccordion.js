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
scout.TileAccordion = function() {
  scout.TileAccordion.parent.call(this);
  this.exclusiveExpand = false;
  this.gridColumnCount = null;
  this.multiSelect = null;
  this.selectable = null;
  this.takeTileFiltersFromGroup = true;
  this.tileComparator = null;
  this.tileFilters = [];
  this.tileGridLayoutConfig = null;
  this.tileGridSelectionHandler = new scout.TileAccordionSelectionHandler(this);
  this.withPlaceholders = null;
  this._selectionUpdateLocked = false;
  this._tileGridPropertyChangeHandler = this._onTileGridPropertyChange.bind(this);
};
scout.inherits(scout.TileAccordion, scout.Accordion);

/**
 * @override
 */
scout.TileAccordion.prototype._render = function() {
  scout.TileAccordion.parent.prototype._render.call(this);
  this.$container.addClass('tile-accordion');
};

/**
 * @override
 */
scout.TileAccordion.prototype._initGroup = function(group) {
  scout.TileAccordion.parent.prototype._initGroup.call(this, group);
  group.body.setSelectionHandler(this.tileGridSelectionHandler);

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

  if (this.tileComparator !== null) {
    group.body.setComparator(this.tileComparator);
    group.body.sort();
  }
  this.setProperty('tileComparator', group.body.comparator);

  if (this.tileFilters.length > 0) {
    group.body.addFilters(this.tileFilters);
    group.body.filter();
  }
  if (this.takeTileFiltersFromGroup) {
    this.setProperty('tileFilters', group.body.filters);
  }

  if (this.withPlaceholders !== null) {
    group.body.setWithPlaceholders(this.withPlaceholders);
  }
  this.setProperty('withPlaceholders', group.body.withPlaceholders);

  if (group.body.selectedTiles.length > 0) {
    this._handleSelectionChanged(group.body);
  }

  group.body.on('propertyChange', this._tileGridPropertyChangeHandler);
  this._handleCollapsed(group);

  // Delegate events so that consumers don't need to attach a listener to each tile grid by themselves
  group.body.__tileAccordionEventDelegator = scout.EventDelegator.create(group.body, this, {
    delegateEvents: ['tileClick', 'tileAction']
  });
};

/**
 * @override
 */
scout.TileAccordion.prototype._deleteGroup = function(group) {
  if (group.body) {
    group.body.off('propertyChange', this._tileGridPropertyChangeHandler);
    group.body.__tileAccordionEventDelegator.destroy();
    group.body.__tileAccordionEventDelegator = null;
  }
  scout.TileAccordion.parent.prototype._deleteGroup.call(this, group);
};

/**
 * @override
 */
scout.TileAccordion.prototype.setGroups = function(groups) {
  var oldTileCount = this.getTileCount();
  var oldFilteredTileCount = this.getFilteredTileCount();
  var oldSelectedTileCount = this.getSelectedTileCount();
  scout.TileAccordion.parent.prototype.setGroups.call(this, groups);

  var tileCount = this.getTileCount();
  var filteredTileCount = this.getFilteredTileCount();
  var selectedTileCount = this.getSelectedTileCount();

  // Trigger artificial property changes if necessary
  // See _onTileGridPropertyChange why parameters are null
  if (tileCount !== oldTileCount) {
    this.triggerPropertyChange('tiles', null, null);
  }
  if (filteredTileCount !== oldFilteredTileCount) {
    this.triggerPropertyChange('filteredTiles', null, null);
  }
  if (selectedTileCount !== oldSelectedTileCount) {
    this.triggerPropertyChange('selectedTiles', null, null);
  }
};

scout.TileAccordion.prototype.setGridColumnCount = function(gridColumnCount) {
  this.groups.forEach(function(group) {
    group.body.setGridColumnCount(gridColumnCount);
  });
  this.setProperty('gridColumnCount', gridColumnCount);
};

scout.TileAccordion.prototype.setTileGridLayoutConfig = function(layoutConfig) {
  this.groups.forEach(function(group) {
    group.body.setLayoutConfig(layoutConfig);
    layoutConfig = group.body.layoutConfig; // May be converted from plain object to TileGridLayoutConfig
  });
  this.setProperty('tileGridLayoutConfig', layoutConfig);
};

scout.TileAccordion.prototype.setWithPlaceholders = function(withPlaceholders) {
  this.groups.forEach(function(group) {
    group.body.setWithPlaceholders(withPlaceholders);
  });
  this.setProperty('withPlaceholders', withPlaceholders);
};

scout.TileAccordion.prototype.setSelectable = function(selectable) {
  this.groups.forEach(function(group) {
    group.body.setSelectable(selectable);
  });
  this.setProperty('selectable', selectable);
};

scout.TileAccordion.prototype.setMultiSelect = function(multiSelect) {
  this.groups.forEach(function(group) {
    group.body.setMultiSelect(multiSelect);
  });
  this.setProperty('multiSelect', multiSelect);
};

scout.TileAccordion.prototype.getGroupById = function(id) {
  return scout.arrays.find(this.groups, function(group) {
    return group.id === id;
  });
};

scout.TileAccordion.prototype.getGroupByTile = function(tile) {
  return tile.findParent(function(parent) {
    return parent instanceof scout.Group;
  });
};

/**
 * Distribute the tiles to the corresponding groups and returns an object with group id as key and array of tiles as value.
 * Always returns all current groups even if the given tiles may not be distributed to all groups.
 */
scout.TileAccordion.prototype._groupTiles = function(tiles) {
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

scout.TileAccordion.prototype.deleteTile = function(tile) {
  this.deleteTiles([tile]);
};

scout.TileAccordion.prototype.deleteTiles = function(tilesToDelete, appendPlaceholders) {
  tilesToDelete = scout.arrays.ensure(tilesToDelete);
  if (tilesToDelete.length === 0) {
    return;
  }
  var tiles = this.getTiles();
  scout.arrays.removeAll(tiles, tilesToDelete);
  this.setTiles(tiles, appendPlaceholders);
};

scout.TileAccordion.prototype.deleteAllTiles = function() {
  this.setTiles([]);
};

/**
 * Distributes the given tiles to their corresponding groups.
 * <p>
 * If the list contains new tiles not assigned to a group yet, an exception will be thrown.
 */
scout.TileAccordion.prototype.setTiles = function(tiles) {
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

scout.TileAccordion.prototype.getTiles = function() {
  var tiles = [];
  this.groups.forEach(function(group) {
    scout.arrays.pushAll(tiles, group.body.tiles);
  });
  return tiles;
};

scout.TileAccordion.prototype.getTileCount = function() {
  var count = 0;
  this.groups.forEach(function(group) {
    count += group.body.tiles.length;
  });
  return count;
};

scout.TileAccordion.prototype.addTileFilter = function(filter) {
  var filters = this.tileFilters.slice();
  if (filters.indexOf(filter) >= 0) {
    return;
  }
  filters.push(filter);
  this.setTileFilters(filters);
};

scout.TileAccordion.prototype.removeTileFilter = function(filter) {
  var filters = this.tileFilters.slice();
  if (!scout.arrays.remove(filters, filter)) {
    return;
  }
  this.setTileFilters(filters);
};

scout.TileAccordion.prototype.setTileFilters = function(filters) {
  filters = scout.arrays.ensure(filters);
  this.groups.forEach(function(group) {
    group.body.setFilters(filters);
  });
  this.setProperty('tileFilters', filters.slice());
};

scout.TileAccordion.prototype.filterTiles = function() {
  this.groups.forEach(function(group) {
    group.body.filter();
  });
};

scout.TileAccordion.prototype.getFilteredTiles = function() {
  var tiles = [];
  this.groups.forEach(function(group) {
    scout.arrays.pushAll(tiles, group.body.filteredTiles);
  });
  return tiles;
};

scout.TileAccordion.prototype.getFilteredTileCount = function() {
  var count = 0;
  this.groups.forEach(function(group) {
    count += group.body.filteredTiles.length;
  });
  return count;
};

/**
 * Compared to #getFilteredTiles(), this function considers the collapsed state of the group as well, meaning only filtered tiles of expanded groups are returned.
 */
scout.TileAccordion.prototype.getVisibleTiles = function() {
  var tiles = [];
  this.expandedGroups().forEach(function(group) {
    scout.arrays.pushAll(tiles, group.body.filteredTiles);
  });
  return tiles;
};

/**
 * Compared to #getFilteredTiles(), this function considers the collapsed state of the group as well, meaning only filtered tiles of expanded groups are counted.
 */
scout.TileAccordion.prototype.getVisibleTileCount = function() {
  var count = 0;
  this.expandedGroups().forEach(function(group) {
    count += group.body.filteredTiles.length;
  });
  return count;
};

scout.TileAccordion.prototype.findVisibleTileIndexAt = function(x, y, startIndex, reverse) {
  startIndex = scout.nvl(startIndex, 0);
  return scout.arrays.findIndexFrom(this.getVisibleTiles(), startIndex, function(tile, i) {
    return this.getVisibleGridX(tile) === x && this.getVisibleGridY(tile) === y;
  }.bind(this), reverse);
};

/**
 * Selects the given tiles and deselects the previously selected ones.
 */
scout.TileAccordion.prototype.selectTiles = function(tiles) {
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

scout.TileAccordion.prototype.selectTile = function(tile) {
  this.selectTiles([tile]);
};

/**
 * Selects all tiles. As for every selection operation: only considers filtered tiles and tiles of expanded groups
 */
scout.TileAccordion.prototype.selectAllTiles = function() {
  this.selectTiles(this.getVisibleTiles());
};

scout.TileAccordion.prototype.deselectTiles = function(tiles) {
  tiles = scout.arrays.ensure(tiles);
  var selectedTiles = this.getSelectedTiles().slice();
  if (scout.arrays.removeAll(selectedTiles, tiles)) {
    this.selectTiles(selectedTiles);
  }
};

scout.TileAccordion.prototype.deselectTile = function(tile) {
  this.deselectTiles([tile]);
};

scout.TileAccordion.prototype.deselectAllTiles = function() {
  this.selectTiles([]);
};

scout.TileAccordion.prototype.addTilesToSelection = function(tiles) {
  tiles = scout.arrays.ensure(tiles);
  this.selectTiles(this.getSelectedTiles().concat(tiles));
};

scout.TileAccordion.prototype.addTileToSelection = function(tile) {
  this.addTilesToSelection([tile]);
};

scout.TileAccordion.prototype.getSelectedTiles = function() {
  var selectedTiles = [];
  this.groups.forEach(function(group) {
    scout.arrays.pushAll(selectedTiles, group.body.selectedTiles);
  });
  return selectedTiles;
};

scout.TileAccordion.prototype.getSelectedTile = function() {
  return this.getSelectedTiles()[0];
};

scout.TileAccordion.prototype.getSelectedTileCount = function() {
  var count = 0;
  this.groups.forEach(function(group) {
    count += group.body.selectedTiles.length;
  });
  return count;
};

scout.TileAccordion.prototype.toggleSelection = function() {
  if (this.getSelectedTileCount() === this.getVisibleTileCount()) {
    this.deselectAllTiles();
  } else {
    this.selectAllTiles();
  }
};

scout.TileAccordion.prototype.setTileComparator = function(comparator) {
  this.groups.forEach(function(group) {
    group.body.setComparator(comparator);
  });
  this.setProperty('tileComparator', comparator);
};

scout.TileAccordion.prototype.sortTiles = function() {
  this.groups.forEach(function(group) {
    group.body.sort();
  });
};

scout.TileAccordion.prototype.setFocusedTile = function(tile) {
  var groupForTile = null;
  if (tile !== null) {
    groupForTile = this.getGroupByTile(tile);
  }
  this.groups.forEach(function(group) {
    if (group === groupForTile) {
      group.body.setFocusedTile(tile);
    } else {
      group.body.setFocusedTile(null);
    }
  });
};

scout.TileAccordion.prototype.getFocusedTile = function() {
  var focusedTile = null;
  this.groups.some(function(group) {
    if (group.body.focusedTile) {
      focusedTile = group.body.focusedTile;
      return true;
    }
  });
  return focusedTile;
};

scout.TileAccordion.prototype.getVisibleGridRowCount = function() {
  return this.expandedGroups().reduce(function(acc, group) {
    return acc + group.body.logicalGrid.gridRows;
  }, 0);
};

scout.TileAccordion.prototype.getVisibleGridX = function(tile) {
  return tile.gridData.x;
};

scout.TileAccordion.prototype.getVisibleGridY = function(tile) {
  var group = this.getGroupByTile(tile);
  var yCorr = this.getVisibleRowByGroup(group);
  return tile.gridData.y + yCorr;
};

scout.TileAccordion.prototype.getGroupByVisibleRow = function(rowToFind) {
  if (rowToFind < 0 || rowToFind >= this.getVisibleGridRowCount()) {
    return null;
  }
  var currentIndex = 0;
  return scout.arrays.find(this.expandedGroups(), function(group) {
    var rowCount = group.body.logicalGrid.gridRows;
    if (currentIndex <= rowToFind && rowToFind < currentIndex + rowCount) {
      return true;
    }
    currentIndex += rowCount;
  });
};

/**
 * @returns the index of the row where the group is located.<p>
 *          Example: There are 3 rows and 2 groups. The first group contains 2 rows, the second 1 row.
 *          The index of the first group is 0, the index of the second group is 2.
 */
scout.TileAccordion.prototype.getVisibleRowByGroup = function(groupToFind) {
  var currentIndex = 0;
  var found = this.expandedGroups().some(function(group) {
    var rowCount = group.body.logicalGrid.gridRows;
    if (group === groupToFind) {
      return true;
    }
    currentIndex += rowCount;
  });
  if (!found) {
    return -1;
  }
  return currentIndex;
};

scout.TileAccordion.prototype.expandedGroups = function() {
  return this.groups.filter(function(group) {
    return !group.collapsed;
  });
};

scout.TileAccordion.prototype._handleSelectionChanged = function(tileGrid) {
  if (this._selectionUpdateLocked) {
    // Don't execute when deselecting other tiles to minimize the amount of property change events
    return;
  }
  var group = tileGrid.parent;
  if (tileGrid.selectedTiles.length > 0 && group.collapsed) {
    // Do not allow selection in a collapsed group (breaks keyboard navigation and is confusing for the user if invisible tiles are selected)
    tileGrid.deselectAllTiles();
    return;
  }
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
};

scout.TileAccordion.prototype._onTileGridPropertyChange = function(event) {
  // Trigger artificial property changes with newValue set to null.
  // Reason: these property changes are fired for each grid. Creating the compound arrays using getFilteredTiles() etc.
  // costs some time (even if only some ms) but may not be necessary at all. The consumer can still call these functions by himself.
  // Also: oldValue cannot be estimated either way which makes it consistent
  if (event.propertyName === 'selectedTiles') {
    this._handleSelectionChanged(event.source);
    this.triggerPropertyChange('selectedTiles', null, null);
  } else if (event.propertyName === 'filteredTiles') {
    this.triggerPropertyChange('filteredTiles', null, null);
  } else if (event.propertyName === 'tiles') {
    this.triggerPropertyChange('tiles', null, null);
  }
};

/**
 * @override
 */
scout.TileAccordion.prototype._onGroupCollapsedChange = function(event) {
  scout.TileAccordion.parent.prototype._onGroupCollapsedChange.call(this, event);

  this._handleCollapsed(event.source);
};

scout.TileAccordion.prototype._handleCollapsed = function(group) {
  if (group.collapsed) {
    // Deselect tiles of a collapsed group (this will also set focusedTile to null) -> actions on invisible elements is confusing, and key strokes only operate on visible elements, too
    group.body.deselectAllTiles();
  }
};
