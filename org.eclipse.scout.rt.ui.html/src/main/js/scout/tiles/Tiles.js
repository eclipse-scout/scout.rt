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
scout.Tiles = function() {
  scout.Tiles.parent.call(this);
  this.animateTileRemoval = true;
  this.animateTileInsertion = true;
  this.filters = [];
  this.filteredTiles = [];
  this.filteredTilesDirty = true;
  // GridColumnCount will be modified by the layout, prefGridColumnCount remains unchanged
  this.gridColumnCount = 4;
  this.prefGridColumnCount = this.gridColumnCount;
  this.logicalGrid = scout.create('scout.HorizontalGrid');
  this.logicalGridHGap = 15;
  this.logicalGridVGap = 20;
  this.logicalGridColumnWidth = 200;
  this.logicalGridRowHeight = 150;
  this.maxContentWidth = -1;
  this.menus = [];
  this.multiSelect = true;
  this.withPlaceholders = false;
  this.selectable = false;
  this.selectedTiles = [];
  this.scrollable = true;
  this.renderAnimationEnabled = false;
  this.startupAnimationDone = false;
  this.startupAnimationEnabled = false;
  this.tiles = [];
  this._filterMenusHandler = this._filterMenus.bind(this);
  this._addWidgetProperties(['tiles', 'selectedTiles', 'menus']);
  this._addPreserveOnPropertyChangeProperties(['selectedTiles']);
};
scout.inherits(scout.Tiles, scout.Widget);

scout.Tiles.prototype._init = function(model) {
  scout.Tiles.parent.prototype._init.call(this, model);
  this._setGridColumnCount(this.gridColumnCount);
  this._initTiles();
  this._applyFilters(this.tiles);
  this._updateFilteredTiles();
  this._setMenus(this.menus);
};

/**
 * @override
 */
scout.Tiles.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

scout.Tiles.prototype._initTiles = function() {
  this.tiles.forEach(function(tile) {
    this._initTile(tile);
  }, this);
};

scout.Tiles.prototype._initTile = function(tile) {
  tile.setSelectable(this.selectable);
  tile.setSelected(this.selectedTiles.indexOf(tile) >= 0);
  tile.setParent(this);
};

scout.Tiles.prototype._render = function() {
  this.$container = this.$parent.appendDiv('tiles');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
  this.$container.on('mousedown', '.tile', this._onTileMouseDown.bind(this));
};

scout.Tiles.prototype._createLayout = function() {
  return new scout.TilesLayout(this);
};

scout.Tiles.prototype._renderProperties = function() {
  scout.Tiles.parent.prototype._renderProperties.call(this);
  this._renderTiles();
  this._renderLogicalGridHGap();
  this._renderLogicalGridVGap();
  this._renderLogicalGridRowHeight();
  this._renderLogicalGridColumnWidth();
  this._renderMaxContentWidth();
  this._renderScrollable();
  this._renderSelectable();
};

scout.Tiles.prototype._renderTiles = function() {
  this.tiles.forEach(function(tile) {
    this._renderTile(tile);
  }, this);
};

scout.Tiles.prototype.insertTile = function(tile) {
  this.insertTiles([tile]);
};

scout.Tiles.prototype.insertTiles = function(tilesToInsert, appendPlaceholders) {
  tilesToInsert = scout.arrays.ensure(tilesToInsert);
  this.setTiles(this.tiles.concat(tilesToInsert), appendPlaceholders);
};

scout.Tiles.prototype.deleteTile = function(tile) {
  this.deleteTiles([tile]);
};

scout.Tiles.prototype.deleteTiles = function(tilesToDelete, appendPlaceholders) {
  tilesToDelete = scout.arrays.ensure(tilesToDelete);
  var tiles = this.tiles.slice();
  scout.arrays.removeAll(tiles, tilesToDelete);
  this.setTiles(tiles, appendPlaceholders);
};

scout.Tiles.prototype.deleteAllTiles = function() {
  this.setTiles([]);
};

scout.Tiles.prototype.setTiles = function(tiles, appendPlaceholders) {
  tiles = scout.arrays.ensure(tiles);
  if (scout.objects.equals(this.tiles, tiles)) {
    return;
  }

  // Ensure given tiles are real tiles (of type scout.Tile)
  tiles = this._createChildren(tiles);

  if (this.withPlaceholders && scout.nvl(appendPlaceholders, true)) {
    // Remove placeholders from new tiles, they will be added later
    this._deletePlaceholders(tiles);
  }

  var tilesToInsert = scout.arrays.diff(tiles, this.tiles);
  this._applyFilters(tilesToInsert);

  // Append the existing placeholders, otherwise they would be unnecessarily deleted if a tile is deleted
  if (this.withPlaceholders && scout.nvl(appendPlaceholders, true)) {
    var placeholders = this.placeholders();
    // But only add as much placeholders as needed: If a new tile is added, it should replace the placeholder underneath.
    // If this were not done the placeholders would move animated when a new tile is inserted rather than just staying where they are
    placeholders = placeholders.slice(Math.min(this._filterTiles(tilesToInsert).length, placeholders.length), placeholders.length);
    scout.arrays.pushAll(tiles, placeholders);
  }

  // Only delete those which are not in the new array
  // Only insert those which are not already there
  var tilesToDelete = scout.arrays.diff(this.tiles, tiles);

  this._deleteTiles(tilesToDelete);
  this._insertTiles(tilesToInsert);
  this._setProperty('tiles', tiles);

  this.filteredTilesDirty = this.filteredTilesDirty || tilesToDelete.length > 0 || tilesToInsert.length > 0;
  this._updateFilteredTiles();
};

scout.Tiles.prototype._insertTiles = function(tiles) {
  if (tiles.length === 0) {
    return;
  }

  tiles.forEach(function(tile) {
    this._insertTile(tile);
  }, this);

  if (this.rendered && !this.htmlComp.layouting) {
    // no need to invalidate when tile placeholders are added or removed while layouting
    this.invalidateLayoutTree();
  }
};

scout.Tiles.prototype._insertTile = function(tile) {
  this._initTile(tile);
  if (this.rendered) {
    this._renderTile(tile);
    tile.$container.addClass('invisible');
    // Wait until the layout animation is done before animating the insert operation.
    // Also make them invisible to not cover existing tiles while they are moving or changing size.
    // Also do it for tiles which don't have an insert animation (e.g. placeholders), due to the same reason.
    this.one('layoutAnimationDone', function() {
      if (tile.rendered) {
        tile.$container.removeClass('invisible');
        if (this._animateTileInsertion(tile)) {
          tile.$container.addClassForAnimation('animate-insert');
        }
      }
    }.bind(this));
  }
};

scout.Tiles.prototype._renderTile = function(tile) {
  tile.render();
  tile.setLayoutData(new scout.LogicalGridData(tile));
};

scout.Tiles.prototype._deleteTiles = function(tiles) {
  if (tiles.length === 0) {
    return;
  }

  tiles.forEach(function(tile) {
    this._deleteTile(tile);
  }, this);
  this.deselectTiles(tiles);

  if (this.rendered && !this.htmlComp.layouting) {
    // no need to invalidate when tile placeholders are added or removed while layouting
    this.invalidateLayoutTree();
  }
};

scout.Tiles.prototype._deleteTile = function(tile) {
  if (this._animateTileRemoval(tile)) {
    // Animate tile removal, but not while layouting when tile placeholders are added or removed
    tile.animateRemoval = true;
  }
  // Destroy only if it is the owner, if tile belongs to another widget, just remove it
  if (tile.owner === this) {
    tile.destroy();
  } else if (this.rendered) {
    tile.remove();
  }
  this._onTileDelete(tile);
  tile.animateRemoval = false;
};

scout.Tiles.prototype._animateTileRemoval = function(tile) {
  return this.animateTileRemoval && !(tile instanceof scout.PlaceholderTile);
};

scout.Tiles.prototype._animateTileInsertion = function(tile) {
  return this.animateTileInsertion && !(tile instanceof scout.PlaceholderTile);
};

scout.Tiles.prototype._onTileDelete = function(tile) {
  if (!tile.rendered || !tile.animateRemoval || this.tileRemovalPending) {
    return;
  }
  this.tileRemovalPending = true;
  tile.$container.oneAnimationEnd(function() {
    this.tileRemovalPending = false;
    if (this.rendered && !this.htmlComp.layouting) {
      this.invalidateLayoutTree();
    }
  }.bind(this));
};

scout.Tiles.prototype.invalidateLayoutTree = function() {
  if (this.tileRemovalPending) {
    // Do not invalidate while tile removal is still pending
    return;
  }
  scout.Tiles.parent.prototype.invalidateLayoutTree.call(this);
};

scout.Tiles.prototype.setGridColumnCount = function(gridColumnCount) {
  this.setProperty('gridColumnCount', gridColumnCount);
};

scout.Tiles.prototype._setGridColumnCount = function(gridColumnCount) {
  this._setProperty('gridColumnCount', gridColumnCount);
  this.prefGridColumnCount = gridColumnCount;
  this.invalidateLogicalGrid();
};

scout.Tiles.prototype.setLogicalGridHGap = function(logicalGridHGap) {
  this.setProperty('logicalGridHGap', logicalGridHGap);
};

scout.Tiles.prototype._renderLogicalGridHGap = function() {
  this.htmlComp.layout.hgap = this.logicalGridHGap;
  this.invalidateLayoutTree();
};

scout.Tiles.prototype.setLogicalGridVGap = function(logicalGridVGap) {
  this.setProperty('logicalGridVGap', logicalGridVGap);
};

scout.Tiles.prototype._renderLogicalGridVGap = function() {
  this.htmlComp.layout.vgap = this.logicalGridVGap;
  this.invalidateLayoutTree();
};

scout.Tiles.prototype.setLogicalGridColumnWidth = function(logicalGridColumnWidth) {
  this.setProperty('logicalGridColumnWidth', logicalGridColumnWidth);
};

scout.Tiles.prototype._renderLogicalGridColumnWidth = function() {
  this.htmlComp.layout.columnWidth = this.logicalGridColumnWidth;
  this.invalidateLayoutTree();
};

scout.Tiles.prototype.setLogicalGridRowHeight = function(logicalGridRowHeight) {
  this.setProperty('logicalGridRowHeight', logicalGridRowHeight);
};

scout.Tiles.prototype._renderLogicalGridRowHeight = function() {
  this.htmlComp.layout.rowHeight = this.logicalGridRowHeight;
  this.invalidateLayoutTree();
};

scout.Tiles.prototype.setMaxContentWidth = function(maxContentWidth) {
  this.setProperty('maxContentWidth', maxContentWidth);
};

scout.Tiles.prototype._renderMaxContentWidth = function() {
  this.htmlComp.layout.maxContentWidth = this.maxContentWidth;
  this.invalidateLayoutTree();
};

scout.Tiles.prototype._setMenus = function(menus, oldMenus) {
  this.updateKeyStrokes(menus, oldMenus);
  this._setProperty('menus', menus);
};

scout.Tiles.prototype._filterMenus = function(menus, destination, onlyVisible, enableDisableKeyStroke, notAllowedTypes) {
  return scout.menus.filterAccordingToSelection('Tiles', this.selectedTiles.length, menus, destination, onlyVisible, enableDisableKeyStroke, notAllowedTypes);
};

scout.Tiles.prototype.showContextMenu = function(options) {
  this.session.onRequestsDone(this._showContextMenu.bind(this, options));
};

scout.Tiles.prototype._showContextMenu = function(options) {
  options = options || {};
  if (!this.rendered || !this.attached) { // check needed because function is called asynchronously
    return;
  }
  if (this.selectedTiles.length === 0) {
    return;
  }
  var menuItems = this._filterMenus(this.menus, scout.MenuDestinations.CONTEXT_MENU, true, false);
  if (menuItems.length === 0) {
    return;
  }
  var pageX = scout.nvl(options.pageX, null);
  var pageY = scout.nvl(options.pageY, null);
  if (pageX === null || pageY === null) {
    var $selectedTile = scout.arrays.last(this.selectedTiles).$container;
    var offset = $selectedTile.offset();
    pageX = offset.left + 10;
    pageY = offset.top + 10;
  }
  // Prevent firing of 'onClose'-handler during contextMenu.open()
  // (Can lead to null-access when adding a new handler to this.contextMenu)
  if (this.contextMenu) {
    this.contextMenu.close();
  }
  this.contextMenu = scout.create('ContextMenuPopup', {
    parent: this,
    menuItems: menuItems,
    location: {
      x: pageX,
      y: pageY
    },
    $anchor: this.$container,
    menuFilter: this._filterMenusHandler
  });
  this.contextMenu.open();
};

scout.Tiles.prototype.setScrollable = function(scrollable) {
  this.setProperty('scrollable', scrollable);
};

scout.Tiles.prototype._renderScrollable = function() {
  if (this.scrollable) {
    scout.scrollbars.install(this.$container, {
      parent: this,
      axis: 'y'
    });
  } else {
    scout.scrollbars.uninstall(this.$container, this.session);
  }
  this.$container.toggleClass('scrollable', this.scrollable);
  this.invalidateLayoutTree();
};

scout.Tiles.prototype.setWithPlaceholders = function(withPlaceholders) {
  this.setProperty('withPlaceholders', withPlaceholders);
};

scout.Tiles.prototype._renderWithPlaceholders = function() {
  this.invalidateLayoutTree();
};

scout.Tiles.prototype.fillUpWithPlaceholders = function() {
  if (!this.withPlaceholders) {
    this._deleteAllPlaceholders();
    return;
  }
  this._deleteObsoletePlaceholders();
  this._insertMissingPlaceholders();
};

scout.Tiles.prototype.tilesWithoutPlaceholders = function() {
  if (!this.withPlaceholders) {
    return this.tiles;
  }
  return this.tiles.filter(function(tile) {
    return !(tile instanceof scout.PlaceholderTile);
  });
};

scout.Tiles.prototype._createPlaceholders = function() {
  var numPlaceholders, lastX,
    columnCount = this.gridColumnCount,
    tiles = this.filteredTiles,
    placeholders = [];

  if (tiles.length > 0) {
    lastX = tiles[tiles.length - 1].gridData.x;
  } else {
    // If there are no tiles, create one row with placeholders
    lastX = -1;
  }

  if (lastX === columnCount - 1) {
    // If last tile is the last element in the row, don't create placeholders
    return [];
  }

  // Otherwise create placeholders for every missing tile in the last row
  numPlaceholders = columnCount - 1 - lastX;
  for (var i = 0; i < numPlaceholders; i++) {
    placeholders.push(this._createPlaceholder());
  }
  return placeholders;
};

scout.Tiles.prototype._createPlaceholder = function() {
  return scout.create('PlaceholderTile', {
    parent: this
  });
};

scout.Tiles.prototype._deleteObsoletePlaceholders = function() {
  var obsoletePlaceholders = [],
    obsolete = false;

  var placeholders = this.placeholders();
  placeholders.forEach(function(placeholder) {
    // Remove all placeholder in the row if there is one at x=0 (don't do it if there are only placeholders)
    if (placeholder.gridData.x === 0 && this.filteredTiles[0] !== placeholder) {
      obsolete = true;
    }
    if (obsolete) {
      obsoletePlaceholders.push(placeholder);
    }
  }, this);

  this.deleteTiles(obsoletePlaceholders, false);
};

scout.Tiles.prototype._deleteAllPlaceholders = function() {
  var tiles = this.tiles.filter(function(tile) {
    return !(tile instanceof scout.PlaceholderTile);
  });
  this.setTiles(tiles, false);
};

scout.Tiles.prototype.placeholders = function() {
  var i, placeholders = [];
  for (i = this.tiles.length - 1; i >= 0; i--) {
    if (!(this.tiles[i] instanceof scout.PlaceholderTile)) {
      // Placeholders are always at the end -> we may stop as soon as no more placeholders are found
      break;
    }
    scout.arrays.insert(placeholders, this.tiles[i], 0);
  }
  return placeholders;
};

scout.Tiles.prototype._insertMissingPlaceholders = function() {
  var placeholders = this._createPlaceholders();
  this.insertTiles(placeholders, false);
};

scout.Tiles.prototype._deletePlaceholders = function(tiles) {
  var i;
  for (i = tiles.length - 1; i >= 0; i--) {
    if (tiles[i] instanceof scout.PlaceholderTile) {
      scout.arrays.remove(tiles, tiles[i]);
    }
  }
};

scout.Tiles.prototype._replacePlaceholders = function(tiles, tilesToInsert) {
  // Find index of the first tile which is not a placeholder (placeholders are always added at the end, so it is faster if search is done backwards)
  var index = scout.arrays.findIndexFromReverse(tiles, tiles.length - 1, function(tile) {
    return !(tile instanceof scout.PlaceholderTile);
  });

  var numPlaceholders = tiles.length - 1 - index;
  for (var i = 1; i <= numPlaceholders; i++) {
    var tile = tiles[index + i];
    if (tilesToInsert[i - 1] && !(tilesToInsert[i - 1] instanceof scout.PlaceholderTile)) {
      scout.arrays.remove(tiles, tile);
    }
  }
};

scout.Tiles.prototype.validateLogicalGrid = function() {
  if (!this.logicalGrid.dirty) {
    return;
  }
  this.logicalGrid.validate(this);
  this.fillUpWithPlaceholders();
  this.logicalGrid.setDirty(true);
  this.logicalGrid.validate(this);
};

/**
 * @override
 */
scout.Tiles.prototype._setLogicalGrid = function(logicalGrid) {
  scout.Tiles.parent.prototype._setLogicalGrid.call(this, logicalGrid);
  if (this.logicalGrid) {
    this.logicalGrid.setGridConfig(new scout.TilesGridConfig());
  }
};

scout.Tiles.prototype.setSelectable = function(selectable) {
  this.setProperty('selectable', selectable);
  if (!selectable) {
    this.deselectAllTiles();
  }
  this.tiles.forEach(function(tile) {
    tile.setSelectable(selectable);
  });
};

scout.Tiles.prototype._renderSelectable = function() {
  this.$container.toggleClass('selectable', this.selectable);
  this.invalidateLayoutTree();
};

scout.Tiles.prototype.setMultiSelect = function(multiSelect) {
  this.setProperty('multiSelect', multiSelect);
};

/**
 * Selects the given tiles and deselects the previously selected ones.
 */
scout.Tiles.prototype.selectTiles = function(tiles) {
  tiles = scout.arrays.ensure(tiles);
  tiles = this._prepareWidgetProperty('selectedTiles', tiles);
  tiles = this._filterTiles(tiles); // Selecting invisible tiles is not allowed

  // Deselect the tiles which are not part of the new selection
  var tilesToUnselect = this.selectedTiles;
  scout.arrays.removeAll(tilesToUnselect, tiles);
  tilesToUnselect.forEach(function(tile) {
    tile.setSelected(false);
  }, this);

  if (!this.selectable) {
    this.setProperty('selectedTiles', []);
    return;
  }

  if (!this.multiSelect && tiles.length > 1) {
    tiles = [tiles[0]];
  }

  // Select the tiles
  tiles.forEach(function(tile) {
    tile.setSelected(true);
  }, this);

  this.setProperty('selectedTiles', tiles.slice());
};

scout.Tiles.prototype.selectTile = function(tile) {
  this.selectTiles([tile]);
};

/**
 * Selects all visible tiles
 */
scout.Tiles.prototype.selectAllTiles = function(tile) {
  this.selectTiles(this.tiles);
};

scout.Tiles.prototype.deselectTiles = function(tiles) {
  tiles = scout.arrays.ensure(tiles);
  var selectedTiles = this.selectedTiles.slice();
  if (scout.arrays.removeAll(selectedTiles, tiles)) {
    this.selectTiles(selectedTiles);
  }
};

scout.Tiles.prototype.deselectTile = function(tile) {
  this.deselectTiles([tile]);
};

scout.Tiles.prototype.deselectAllTiles = function(tiles) {
  this.selectTiles([]);
};

scout.Tiles.prototype.addTilesToSelection = function(tiles) {
  tiles = scout.arrays.ensure(tiles);
  this.selectTiles(this.selectedTiles.concat(tiles));
};

scout.Tiles.prototype.addTileToSelection = function(tile) {
  this.addTilesToSelection([tile]);
};

scout.Tiles.prototype._onTileMouseDown = function(event) {
  this._selectTileOnMouseDown(event);

  if (event.which === 3) {
    this.showContextMenu({
      pageX: event.pageX,
      pageY: event.pageY
    });
    return false;
  }
};

scout.Tiles.prototype._selectTileOnMouseDown = function(event) {
  if (!this.selectable) {
    return;
  }
  if (this.selected && event.which === 3) {
    // Do not toggle if context menus should be shown and tile already is selected
    return;
  }

  var $tile = $(event.currentTarget);
  var tile = $tile.data('widget');
  if (tile instanceof scout.PlaceholderTile) {
    return;
  }

  // Click on a tile toggles the selection ...
  var selected = !tile.selected;
  if (tile.selected && this.selectedTiles.length > 1 && !event.ctrlKey) {
    // ... but if multiple tiles are selected, click on an already selected tile deselects every other tile but keeps the selection of the clicked one
    selected = true;
  }

  // CTRL click on a tile adds or removes that tile to or from the selection
  if (this.multiSelect && event.ctrlKey) {
    if (selected) {
      this.addTilesToSelection(tile);
    } else {
      this.deselectTile(tile);
    }
    return;
  }

  // If multi selection is disabled or no CTRL key is pressed, only the clicked tile may be selected
  if (selected) {
    this.selectTile(tile);
  } else {
    this.deselectAllTiles();
  }
};

scout.Tiles.prototype.addFilter = function(filter) {
  if (this.filters.indexOf(filter) < 0) {
    this.filters.push(filter);
  }
};

scout.Tiles.prototype.removeFilter = function(filter) {
  scout.arrays.remove(this.filters, filter);
};

scout.Tiles.prototype.filter = function() {
  // Full reset is set to true to loop through every tile and make sure tile.filterAccepted is correctly set
  this._applyFilters(this.tiles, true);
  this._updateFilteredTiles();
};

scout.Tiles.prototype._applyFilters = function(tiles, fullReset) {
  if (this.filters.length === 0 && !scout.nvl(fullReset, false)) {
    return;
  }
  var newlyHiddenTiles = [];
  var changed = false;
  tiles.forEach(function(tile) {
    if (this._applyFiltersForTile(tile)) {
      changed = true;
    }
    if (!tile.filterAccepted && changed) {
      newlyHiddenTiles.push(tile);
    }
  }, this);

  if (changed) {
    this.filteredTilesDirty = true;
  }

  // Non visible tiles must be deselected
  this.deselectTiles(newlyHiddenTiles);
};

scout.Tiles.prototype._updateFilteredTiles = function() {
  var tiles = this.tiles;
  if (this.filters.length > 0) {
    tiles = this._filterTiles();
  }
  if (this.filteredTilesDirty) {
    this.setProperty('filteredTiles', tiles);
    this.filteredTilesDirty = false;
  }
};

/**
 * @returns {Boolean} true if tile state has changed, false if not
 */
scout.Tiles.prototype._applyFiltersForTile = function(tile) {
  if (this._tileAcceptedByFilters(tile)) {
    if (!tile.filterAccepted) {
      tile.setFilterAccepted(true);
      return true;
    }
  } else {
    if (tile.filterAccepted) {
      tile.setFilterAccepted(false);
      return true;
    }
  }
  return false;
};

scout.Tiles.prototype._tileAcceptedByFilters = function(tile) {
  return !this.filters.some(function(filter) {
    // return true if an element was found which is not accepted by the filter to break the some() loop
    if (tile instanceof scout.PlaceholderTile) {
      return false;
    }
    if (!filter.accept(tile)) {
      return true;
    }
  });
};

/**
 * @returns the tiles which are accepted by the filter and therefore visible.
 */
scout.Tiles.prototype._filterTiles = function(tiles) {
  tiles = scout.nvl(tiles, this.tiles);
  if (this.filters.length === 0) {
    return tiles.slice();
  }
  return tiles.filter(function(tile) {
    return tile.filterAccepted;
  });
};
