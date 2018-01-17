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
scout.TileGrid = function() {
  scout.TileGrid.parent.call(this);
  this.animateTileRemoval = true;
  this.animateTileInsertion = true;
  this.comparator = null;
  this.empty = false;
  this.filters = [];
  this.filteredTiles = [];
  this.filteredTilesDirty = true;
  this.focusedTile = null;
  // GridColumnCount will be modified by the layout, prefGridColumnCount remains unchanged
  this.gridColumnCount = 4;
  this.prefGridColumnCount = this.gridColumnCount;
  this.logicalGrid = scout.create('scout.HorizontalGrid');
  this.layoutConfig = null;
  this.menus = [];
  this.multiSelect = true;
  this.renderAnimationEnabled = false;
  this.selectable = false;
  this.selectedTiles = [];
  this.selectionHandler = new scout.TileGridSelectionHandler(this);
  this.scrollable = true;
  this.startupAnimationDone = false;
  this.startupAnimationEnabled = false;
  this.tiles = [];
  this.withPlaceholders = false;
  this._filterMenusHandler = this._filterMenus.bind(this);
  this._addWidgetProperties(['tiles', 'selectedTiles', 'menus']);
  this._addPreserveOnPropertyChangeProperties(['selectedTiles']);
};
scout.inherits(scout.TileGrid, scout.Widget);

scout.TileGrid.prototype._init = function(model) {
  scout.TileGrid.parent.prototype._init.call(this, model);
  this._setGridColumnCount(this.gridColumnCount);
  this._setLayoutConfig(this.layoutConfig);
  this._initTiles();
  this._applyFilters(this.tiles);
  this._updateFilteredTiles();
  this._setMenus(this.menus);
};

/**
 * @override
 */
scout.TileGrid.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

/**
 * @override
 */
scout.TileGrid.prototype._initKeyStrokeContext = function() {
  scout.TileGrid.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke([
    new scout.TileGridSelectAllKeyStroke(this),
    new scout.TileGridSelectLeftKeyStroke(this),
    new scout.TileGridSelectRightKeyStroke(this),
    new scout.TileGridSelectDownKeyStroke(this),
    new scout.TileGridSelectUpKeyStroke(this),
    new scout.TileGridSelectFirstKeyStroke(this),
    new scout.TileGridSelectLastKeyStroke(this),
    new scout.ContextMenuKeyStroke(this, this.showContextMenu, this)
  ]);
};

scout.TileGrid.prototype._initTiles = function() {
  this.tiles.forEach(function(tile) {
    this._initTile(tile);
  }, this);
};

scout.TileGrid.prototype._initTile = function(tile) {
  tile.setSelectable(this.selectable);
  tile.setSelected(this.selectedTiles.indexOf(tile) >= 0);

  // Set proper state in case tile was used in another grid
  tile.setParent(this);
  tile.setFilterAccepted(true);
};

scout.TileGrid.prototype._render = function() {
  this.$container = this.$parent.appendDiv('tile-grid');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
  this.$container.on('mousedown', '.tile', this._onTileMouseDown.bind(this));
};

scout.TileGrid.prototype._createLayout = function() {
  return new scout.TileGridLayout(this);
};

scout.TileGrid.prototype._renderProperties = function() {
  scout.TileGrid.parent.prototype._renderProperties.call(this);
  this._renderTiles();
  this._renderLayoutConfig();
  this._renderScrollable();
  this._renderSelectable();
  this._renderEmpty();
};

scout.TileGrid.prototype._renderEnabled = function() {
  scout.TileGrid.parent.prototype._renderEnabled.call(this);

  this._updateTabbable();
};

scout.TileGrid.prototype._updateTabbable = function() {
  this.$container.setTabbable(this.enabled && this.selectable);
};

scout.TileGrid.prototype._renderTiles = function() {
  this.tiles.forEach(function(tile) {
    this._renderTile(tile);
  }, this);
};

scout.TileGrid.prototype.insertTile = function(tile) {
  this.insertTiles([tile]);
};

scout.TileGrid.prototype.insertTiles = function(tilesToInsert, appendPlaceholders) {
  tilesToInsert = scout.arrays.ensure(tilesToInsert);
  if (tilesToInsert.length === 0) {
    return;
  }
  this.setTiles(this.tiles.concat(tilesToInsert), appendPlaceholders);
};

scout.TileGrid.prototype.deleteTile = function(tile) {
  this.deleteTiles([tile]);
};

scout.TileGrid.prototype.deleteTiles = function(tilesToDelete, appendPlaceholders) {
  tilesToDelete = scout.arrays.ensure(tilesToDelete);
  if (tilesToDelete.length === 0) {
    return;
  }
  var tiles = this.tiles.slice();
  scout.arrays.removeAll(tiles, tilesToDelete);
  this.setTiles(tiles, appendPlaceholders);
};

scout.TileGrid.prototype.deleteAllTiles = function() {
  this.setTiles([]);
};

scout.TileGrid.prototype.setTiles = function(tiles, appendPlaceholders) {
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

  // Only insert those which are not already there
  var tilesToInsert = scout.arrays.diff(tiles, this.tiles);
  this._insertTiles(tilesToInsert);

  // Append the existing placeholders, otherwise they would be unnecessarily deleted if a tile is deleted
  if (this.withPlaceholders && scout.nvl(appendPlaceholders, true)) {
    var placeholders = this.placeholders();
    // But only add as much placeholders as needed: If a new tile is added, it should replace the placeholder underneath.
    // If this were not done the placeholders would move animated when a new tile is inserted rather than just staying where they are
    placeholders = placeholders.slice(Math.min(this._filterTiles(tilesToInsert).length, placeholders.length), placeholders.length);
    scout.arrays.pushAll(tiles, placeholders);
  }

  // Only delete those which are not in the new array
  var tilesToDelete = scout.arrays.diff(this.tiles, tiles);
  this._deleteTiles(tilesToDelete);

  this._sort(tiles);
  this._updateTileOrder(tiles);
  this._setProperty('tiles', tiles);

  this.filteredTilesDirty = this.filteredTilesDirty || tilesToDelete.length > 0 || tilesToInsert.length > 0;
  this._updateFilteredTiles();
};

scout.TileGrid.prototype._insertTiles = function(tiles) {
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

scout.TileGrid.prototype._insertTile = function(tile) {
  this._initTile(tile);
  this._applyFilters([tile]);
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

scout.TileGrid.prototype._renderTile = function(tile) {
  tile.render();
  tile.setLayoutData(new scout.LogicalGridData(tile));
};

scout.TileGrid.prototype._deleteTiles = function(tiles) {
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

scout.TileGrid.prototype._deleteTile = function(tile) {
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
  if (tile === this.focusedTile) {
    this.setFocusedTile(null);
  }
};

scout.TileGrid.prototype._animateTileRemoval = function(tile) {
  return this.animateTileRemoval && !(tile instanceof scout.PlaceholderTile);
};

scout.TileGrid.prototype._animateTileInsertion = function(tile) {
  return this.animateTileInsertion && !(tile instanceof scout.PlaceholderTile);
};

scout.TileGrid.prototype._onTileDelete = function(tile) {
  if (!tile.rendered || !tile.animateRemoval || this.tileRemovalPending) {
    return;
  }
  this.tileRemovalPending = true;
  tile.one('remove', function() {
    this.tileRemovalPending = false;
    if (this.rendered && !this.htmlComp.layouting) {
      this.invalidateLayoutTree();
    }
  }.bind(this));
};

scout.TileGrid.prototype.setComparator = function(comparator) {
  if (this.comparator === comparator) {
    return;
  }
  this.comparator = comparator;
};

scout.TileGrid.prototype.sort = function() {
  var tiles = this.tiles.slice();
  this._sort(tiles);
  this._updateTileOrder(tiles);
  this._setProperty('tiles', tiles);

  // Sort list of filtered tiles as well
  this._updateFilteredTiles();
};

scout.TileGrid.prototype._sort = function(tiles) {
  if (this.comparator === null) {
    return;
  }

  var placeholders = [];
  if (this.withPlaceholders) {
    // Don't reorder placeholders -> remove them first, then sort and add them afterwards again
    placeholders = this._deletePlaceholders(tiles);
  }
  tiles.sort(this.comparator);
  scout.arrays.pushAll(tiles, placeholders);
};

scout.TileGrid.prototype._updateTileOrder = function(tiles) {
  var different = false;
  if (!this.rendered) {
    // If it is not rendered, check whether the order is different and if yes, mark the filters as dirty so that this.filteredTiles will be ordered as well
    different = tiles.some(function(tile, i) {
      return this.tiles[i] !== tile;
    }, this);
    if (different) {
      this.filteredTilesDirty = true;
    }
    return;
  }

  // Loop through the the tiles and move every html element to the end of the container
  // Only move if the order is different to the old order
  tiles.forEach(function(tile, i) {
    if (this.tiles[i] !== tile || different) {
      // Start ordering as soon as the order of the arrays starts to differ
      different = true;
      tile.$container.appendTo(this.$container);
    }
  }, this);

  if (different) {
    this.filteredTilesDirty = true;
    this.invalidateLogicalGrid();
  }
};

scout.TileGrid.prototype.invalidateLayoutTree = function() {
  if (this.tileRemovalPending) {
    // Do not invalidate while tile removal is still pending
    return;
  }
  scout.TileGrid.parent.prototype.invalidateLayoutTree.call(this);
};

scout.TileGrid.prototype.setGridColumnCount = function(gridColumnCount) {
  this.setProperty('gridColumnCount', gridColumnCount);
};

scout.TileGrid.prototype._setGridColumnCount = function(gridColumnCount) {
  this._setProperty('gridColumnCount', gridColumnCount);
  this.prefGridColumnCount = gridColumnCount;
  this.invalidateLogicalGrid();
};

scout.TileGrid.prototype.setLayoutConfig = function(layoutConfig) {
  this.setProperty('layoutConfig', layoutConfig);
};

scout.TileGrid.prototype._setLayoutConfig = function(layoutConfig) {
  if (!layoutConfig) {
    layoutConfig = new scout.TileGridLayoutConfig();
  }
  this._setProperty('layoutConfig', scout.TileGridLayoutConfig.ensure(layoutConfig));
};

scout.TileGrid.prototype._renderLayoutConfig = function() {
  this.htmlComp.layout.hgap = this.layoutConfig.hgap;
  this.htmlComp.layout.vgap = this.layoutConfig.vgap;
  this.htmlComp.layout.columnWidth = this.layoutConfig.columnWidth;
  this.htmlComp.layout.rowHeight = this.layoutConfig.rowHeight;
  this.htmlComp.layout.maxWidth = this.layoutConfig.maxWidth;
  var oldMinWidth = this.htmlComp.layout.minWidth;
  this.htmlComp.layout.minWidth = this.layoutConfig.minWidth;
  if (oldMinWidth !== this.layoutConfig.minWidth) {
    this._renderScrollable();
  }
  this.invalidateLayoutTree();
};

scout.TileGrid.prototype._setMenus = function(menus, oldMenus) {
  this.updateKeyStrokes(menus, oldMenus);
  this._setProperty('menus', menus);
};

scout.TileGrid.prototype._filterMenus = function(menus, destination, onlyVisible, enableDisableKeyStroke, notAllowedTypes) {
  return scout.menus.filterAccordingToSelection('TileGrid', this.selectedTiles.length, menus, destination, onlyVisible, enableDisableKeyStroke, notAllowedTypes);
};

scout.TileGrid.prototype.showContextMenu = function(options) {
  this.session.onRequestsDone(this._showContextMenu.bind(this, options));
};

scout.TileGrid.prototype._showContextMenu = function(options) {
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

scout.TileGrid.prototype.setScrollable = function(scrollable) {
  this.setProperty('scrollable', scrollable);
};

scout.TileGrid.prototype._renderScrollable = function() {
  scout.scrollbars.uninstall(this.$container, this.session);

  // horizontal (x-axis) scrollbar is only installed when minWidth is > 0
  if (this.scrollable) {
    scout.scrollbars.install(this.$container, {
      parent: this,
      axis: ((this.layoutConfig.minWidth > 0) ? 'both' : 'y')
    });
  } else if (this.layoutConfig.minWidth > 0) {
    scout.scrollbars.install(this.$container, {
      parent: this,
      axis: 'x'
    });
  }
  this.$container.toggleClass('scrollable', this.scrollable);
  this.invalidateLayoutTree();
};

scout.TileGrid.prototype.setWithPlaceholders = function(withPlaceholders) {
  this.setProperty('withPlaceholders', withPlaceholders);
};

scout.TileGrid.prototype._renderWithPlaceholders = function() {
  this.invalidateLayoutTree();
};

scout.TileGrid.prototype.fillUpWithPlaceholders = function() {
  if (!this.withPlaceholders) {
    this._deleteAllPlaceholders();
    return;
  }
  this._deleteObsoletePlaceholders();
  this._insertMissingPlaceholders();
};

scout.TileGrid.prototype.tilesWithoutPlaceholders = function() {
  if (!this.withPlaceholders) {
    return this.tiles;
  }
  return this.tiles.filter(function(tile) {
    return !(tile instanceof scout.PlaceholderTile);
  });
};

scout.TileGrid.prototype._createPlaceholders = function() {
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

scout.TileGrid.prototype._createPlaceholder = function() {
  return scout.create('PlaceholderTile', {
    parent: this
  });
};

scout.TileGrid.prototype._deleteObsoletePlaceholders = function() {
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

scout.TileGrid.prototype._deleteAllPlaceholders = function() {
  this.deleteTiles(this.placeholders(), false);
};

scout.TileGrid.prototype.placeholders = function() {
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

scout.TileGrid.prototype._insertMissingPlaceholders = function() {
  var placeholders = this._createPlaceholders();
  this.insertTiles(placeholders, false);
};

scout.TileGrid.prototype._deletePlaceholders = function(tiles) {
  var i;
  var deletedPlaceholders = [];
  for (i = tiles.length - 1; i >= 0; i--) {
    if (tiles[i] instanceof scout.PlaceholderTile) {
      deletedPlaceholders.push(tiles[i]);
      scout.arrays.remove(tiles, tiles[i]);
    }
  }
  return deletedPlaceholders.reverse();
};

scout.TileGrid.prototype._replacePlaceholders = function(tiles, tilesToInsert) {
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

scout.TileGrid.prototype.validateLogicalGrid = function() {
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
scout.TileGrid.prototype._setLogicalGrid = function(logicalGrid) {
  scout.TileGrid.parent.prototype._setLogicalGrid.call(this, logicalGrid);
  if (this.logicalGrid) {
    this.logicalGrid.setGridConfig(new scout.TileGridGridConfig());
  }
};

scout.TileGrid.prototype.setFocusedTile = function(tile) {
  if (this.focusedTile === tile) {
    return;
  }
  this.focusedTile = tile;
  if (!this.rendered || !tile || this.isFocused()) {
    return;
  }
  var $scrollable = this.$container.scrollParent();
  if ($scrollable.length === 0) {
    return;
  }
  var oldScrollTop = $scrollable.scrollTop();
  // Make sure the tile grid has the focus when focusing a tile
  if (this.focus()) {
    // Restore old scroll to prevent scrolling by the browser due to the focus() call
    $scrollable[0].scrollTop = oldScrollTop;
  }
};

scout.TileGrid.prototype.setSelectable = function(selectable) {
  this.setProperty('selectable', selectable);
  if (!selectable) {
    this.deselectAllTiles();
  }
  this.tiles.forEach(function(tile) {
    tile.setSelectable(selectable);
  });
};

scout.TileGrid.prototype._renderSelectable = function() {
  this.$container.toggleClass('selectable', this.selectable);
  this._updateTabbable();
  this.invalidateLayoutTree();
};

scout.TileGrid.prototype.setMultiSelect = function(multiSelect) {
  this.setProperty('multiSelect', multiSelect);
};

/**
 * Selects the given tiles and deselects the previously selected ones.
 */
scout.TileGrid.prototype.selectTiles = function(tiles) {
  tiles = scout.arrays.ensure(tiles);
  // Ensure given tiles are real tiles (of type scout.Tile)
  tiles = this._createChildren(tiles);
  tiles = this._filterTiles(tiles); // Selecting invisible tiles is not allowed

  // Ensure no tiles will be selected if selectable is disabled
  if (!this.selectable) {
    tiles = [];
  }

  // Ensure only one tile is selected if multiSelect is disabled
  if (!this.multiSelect && tiles.length > 1) {
    tiles = [tiles[0]];
  }

  // Deselect the tiles which are not part of the new selection
  var tilesToUnselect = this.selectedTiles;
  scout.arrays.removeAll(tilesToUnselect, tiles);
  tilesToUnselect.forEach(function(tile) {
    tile.setSelected(false);
    if (tile === this.focusedTile) {
      this.setFocusedTile(null);
    }
  }, this);

  // Select the tiles
  tiles.forEach(function(tile) {
    tile.setSelected(true);
  }, this);

  this.setProperty('selectedTiles', tiles.slice());
};

scout.TileGrid.prototype.selectTile = function(tile) {
  this.selectTiles([tile]);
};

/**
 * Selects all tiles. As for every selection operation: only filtered tiles are considered.
 */
scout.TileGrid.prototype.selectAllTiles = function() {
  this.selectTiles(this.filteredTiles);
};

scout.TileGrid.prototype.deselectTiles = function(tiles) {
  tiles = scout.arrays.ensure(tiles);
  var selectedTiles = this.selectedTiles.slice();
  if (scout.arrays.removeAll(selectedTiles, tiles)) {
    this.selectTiles(selectedTiles);
  }
};

scout.TileGrid.prototype.deselectTile = function(tile) {
  this.deselectTiles([tile]);
};

scout.TileGrid.prototype.deselectAllTiles = function() {
  this.selectTiles([]);
};

scout.TileGrid.prototype.toggleSelection = function() {
  if (this.selectedTiles.length === this.filteredTiles.length) {
    this.deselectAllTiles();
  } else {
    this.selectAllTiles();
  }
};

scout.TileGrid.prototype.addTilesToSelection = function(tiles) {
  tiles = scout.arrays.ensure(tiles);
  this.selectTiles(this.selectedTiles.concat(tiles));
};

scout.TileGrid.prototype.addTileToSelection = function(tile) {
  this.addTilesToSelection([tile]);
};

scout.TileGrid.prototype._onTileMouseDown = function(event) {
  this._selectTileOnMouseDown(event);

  if (event.which === 3) {
    this.showContextMenu({
      pageX: event.pageX,
      pageY: event.pageY
    });
    return false;
  }
};

scout.TileGrid.prototype.setSelectionHandler = function(selectionHandler) {
  this.selectionHandler = selectionHandler;
};

scout.TileGrid.prototype._selectTileOnMouseDown = function(event) {
  this.selectionHandler.selectTileOnMouseDown(event);
};

scout.TileGrid.prototype.scrollTo = function(tile) {
  tile.reveal();
};

scout.TileGrid.prototype.scrollToTop = function() {
  scout.scrollbars.scrollTop(this.$container, 0);
};

scout.TileGrid.prototype.scrollToBottom = function() {
  scout.scrollbars.scrollToBottom(this.$container);
};

scout.TileGrid.prototype.revealSelection = function() {
  if (!this.rendered) {
    // Execute delayed because tileGrid may be not layouted yet
    this.session.layoutValidator.schedulePostValidateFunction(this.revealSelection.bind(this));
    return;
  }

  if (this.selectedTiles.length > 0) {
    this.scrollTo(this.selectedTiles[0]);
  }
};

scout.TileGrid.prototype.addFilter = function(filter) {
  if (this.filters.indexOf(filter) < 0) {
    this.filters.push(filter);
  }
};

scout.TileGrid.prototype.removeFilter = function(filter) {
  scout.arrays.remove(this.filters, filter);
};

scout.TileGrid.prototype.filter = function() {
  // Full reset is set to true to loop through every tile and make sure tile.filterAccepted is correctly set
  this._applyFilters(this.tiles, true);
  this._updateFilteredTiles();
};

scout.TileGrid.prototype._applyFilters = function(tiles, fullReset) {
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
      if (tile === this.focusedTile) {
        this.setFocusedTile(null);
      }
    }
  }, this);

  if (changed) {
    this.filteredTilesDirty = true;
  }

  // Non visible tiles must be deselected
  this.deselectTiles(newlyHiddenTiles);
};

scout.TileGrid.prototype._updateFilteredTiles = function() {
  var tiles = this.tiles;
  if (this.filters.length > 0) {
    tiles = this._filterTiles();
  }
  if (this.filteredTilesDirty) {
    this.setProperty('filteredTiles', tiles);
    this.filteredTilesDirty = false;
  }
  this._updateEmpty();
};

scout.TileGrid.prototype._updateEmpty = function() {
  this.setEmpty(this.filteredTiles.length === 0);
};

scout.TileGrid.prototype.setEmpty = function(empty) {
  this.setProperty('empty', empty);
};

scout.TileGrid.prototype._renderEmpty = function() {
  this.$container.toggleClass('empty', this.empty);
  this.invalidateLayoutTree();
};

/**
 * @returns {Boolean} true if tile state has changed, false if not
 */
scout.TileGrid.prototype._applyFiltersForTile = function(tile) {
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

scout.TileGrid.prototype._tileAcceptedByFilters = function(tile) {
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
scout.TileGrid.prototype._filterTiles = function(tiles) {
  tiles = scout.nvl(tiles, this.tiles);
  if (this.filters.length === 0) {
    return tiles.slice();
  }
  return tiles.filter(function(tile) {
    return tile.filterAccepted;
  });
};

scout.TileGrid.prototype.findTileIndexAt = function(x, y, startIndex, reverse) {
  startIndex = scout.nvl(startIndex, 0);
  return scout.arrays.findIndexFrom(this.filteredTiles, startIndex, function(tile, i) {
    return tile.gridData.x === x && tile.gridData.y === y;
  }, reverse);
};
