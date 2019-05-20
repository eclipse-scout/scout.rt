/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
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
  this._doubleClickSupport = new scout.DoubleClickSupport();
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
  this.scrolling = false;
  this.scrollTopDirty = false;
  this.startupAnimationDone = false;
  this.startupAnimationEnabled = false;
  this.tiles = [];
  this.tileRemovalPendingCount = 0;
  this.viewRangeSize = 0;
  this.viewRangeRendered = new scout.Range(0, 0);
  this.virtual = false;
  this.virtualScrolling = null;
  this.withPlaceholders = false;
  this._filterMenusHandler = this._filterMenus.bind(this);
  this._renderViewPortAfterAttach = false;
  this._scrollParentScrollHandler = this._onScrollParentScroll.bind(this);
  this._addWidgetProperties(['tiles', 'selectedTiles', 'menus']);
  this._addPreserveOnPropertyChangeProperties(['selectedTiles']);

  this.$fillBefore = null;
  this.$fillAfter = null;
};
scout.inherits(scout.TileGrid, scout.Widget);

scout.TileGrid.prototype._init = function(model) {
  scout.TileGrid.parent.prototype._init.call(this, model);
  this._setGridColumnCount(this.gridColumnCount);
  this._setLayoutConfig(this.layoutConfig);
  this._initVirtualScrolling();
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

scout.TileGrid.prototype._initVirtualScrolling = function() {
  this.virtualScrolling = this._createVirtualScrolling();
};

scout.TileGrid.prototype._createVirtualScrolling = function() {
  return new scout.VirtualScrolling({
    widget: this,
    enabled: this.virtual,
    viewRangeSize: this.viewRangeSize,
    rowHeight: this._heightForRow.bind(this),
    rowCount: this.rowCount.bind(this),
    _renderViewRange: this._renderViewRange.bind(this)
  });
};

/**
 * @override
 */
scout.TileGrid.prototype._createLoadingSupport = function() {
  return new scout.LoadingSupport({
    widget: this
  });
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
  this.$container
    .on('mousedown', '.tile', this._onTileMouseDown.bind(this))
    .on('click', '.tile', this._onTileClick.bind(this))
    .on('dblclick', '.tile', this._onTileDoubleClick.bind(this));
};

scout.TileGrid.prototype._createLayout = function() {
  return new scout.TileGridLayout(this, this.layoutConfig);
};

scout.TileGrid.prototype._renderProperties = function() {
  scout.TileGrid.parent.prototype._renderProperties.call(this);
  this._renderLayoutConfig();
  this._renderScrollable();
  this._renderVirtual();
  this._renderSelectable();
  this._renderEmpty();
};

scout.TileGrid.prototype._remove = function() {
  this.$fillBefore = null;
  this.$fillAfter = null;
  this.viewRangeRendered = new scout.Range(0, 0);
  this._updateVirtualScrollable();
  scout.TileGrid.parent.prototype._remove.call(this);
};

/**
 * @override
 */
scout.TileGrid.prototype._renderOnAttach = function() {
  scout.TileGrid.parent.prototype._renderOnAttach.call(this);
  if (this._renderViewPortAfterAttach) {
    this._renderViewPort();
    this._renderViewPortAfterAttach = false;
  }
};

scout.TileGrid.prototype._renderEnabled = function() {
  scout.TileGrid.parent.prototype._renderEnabled.call(this);

  this._updateTabbable();
};

scout.TileGrid.prototype._updateTabbable = function() {
  this.$container.setTabbable(this.enabled && this.selectable);
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
  this.filteredTilesDirty = this.filteredTilesDirty || tilesToDelete.length > 0 || tilesToInsert.length > 0 || !scout.arrays.equals(this.tiles, tiles); // last check necessary if sorting changed
  var currentTiles = this.tiles;
  this._setProperty('tiles', tiles);
  this._updateFilteredTiles();

  if (this.rendered) {
    this._renderTileDelta();
    this._renderTileOrder(currentTiles);
    this._renderInsertTiles(tilesToInsert);
  }
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
  if (!this.virtual && this.rendered) {
    this._renderTile(tile);
  }
};

scout.TileGrid.prototype._renderTile = function(tile) {
  if (tile.removalPending) {
    // If tile is being removed by the filter and the filter cleared so that the tile should be rendered again while the animation is still running,
    // we need to wait for the remove animation, otherwise an already rendered exception occurs
    tile.one('remove', function() {
      if (tile.rendered) {
        // Might be already rendered again by renderTileDelta because filter was changed again
        return;
      }
      this._renderTile(tile);
      this._renderTileVisibleForFilter(tile);
      if (this.tileRemovalPendingCount === 0) {
        this.invalidateLayoutTree();
      }
    }.bind(this));
    return;
  }
  tile.render();
  tile.setLayoutData(new scout.LogicalGridData(tile));
  tile.$container.addClass('newly-rendered');
};

scout.TileGrid.prototype._renderInsertTiles = function(tiles) {
  tiles.forEach(function(tile) {
    if (!tile.rendered) {
      return;
    }
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
  }, this);
};

scout.TileGrid.prototype._removeAllTiles = function() {
  this.tiles.forEach(function(tile) {
    tile.remove();
  });
  this.viewRangeRendered = new scout.Range(0, 0);
};

scout.TileGrid.prototype._renderAllTiles = function() {
  this.tiles.forEach(function(tile) {
    this._renderTile(tile);
  }, this);
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
  this._onAnimatedTileRemove(tile);
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

scout.TileGrid.prototype._onAnimatedTileRemove = function(tile) {
  if (!tile.rendered || !tile.animateRemoval) {
    return;
  }
  this.tileRemovalPendingCount++;
  tile.one('remove', function() {
    this.tileRemovalPendingCount--;
    if (this.rendered && this.tileRemovalPendingCount === 0 && !this.htmlComp.layouting) {
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
  if (scout.arrays.equals(this.tiles, tiles)) {
    // Check is needed anyway to determine whether filteredTilesDirty needs to be set, so we can use it here as well to early return if nothing changed
    return;
  }
  var currentTiles = this.tiles;
  this._setProperty('tiles', tiles);

  // Sort list of filtered tiles as well
  this.filteredTilesDirty = true;
  this._updateFilteredTiles();

  if (this.rendered) {
    this._renderTileDelta();
    this._renderTileOrder(currentTiles);
    this.validateLayoutTree(); // prevent flickering in virtual mode
  }
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

scout.TileGrid.prototype.invalidateLayoutTree = function(invalidateParents) {
  if (this.tileRemovalPendingCount > 0) {
    // Do not invalidate while tile removal is still pending
    return;
  }
  scout.TileGrid.parent.prototype.invalidateLayoutTree.call(this, invalidateParents);
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
  var oldMinWidth = this.htmlComp.layout.minWidth;
  this.layoutConfig.applyToLayout(this.htmlComp.layout);
  if (this.virtualScrolling) {
    this.virtualScrolling.setMinRowHeight(this._minRowHeight());
    this.setViewRangeSize(this.virtualScrolling.viewRangeSize, false);
  }
  if (oldMinWidth !== this.htmlComp.layout.minWidth) {
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
  this._uninstallScrollbars();

  // horizontal (x-axis) scrollbar is only installed when minWidth is > 0
  if (this.scrollable) {
    this._installScrollbars({
      axis: ((this.layoutConfig.minWidth > 0) ? 'both' : 'y')
    });
  } else if (this.layoutConfig.minWidth > 0) {
    this._installScrollbars({
      axis: 'x'
    });
  }
  this.$container.toggleClass('scrollable', this.scrollable);
  this._updateVirtualScrollable();
  this.invalidateLayoutTree();
};

/**
 * @override
 */
scout.TileGrid.prototype._onScroll = function() {
  var scrollTop = this.$container[0].scrollTop;
  var scrollLeft = this.$container[0].scrollLeft;
  if (this.scrollTop !== scrollTop && this.virtual) {
    this.scrolling = true;
    this.revalidateLayout();
    this.scrolling = false;
  }
  this.scrollTop = scrollTop;
  this.scrollLeft = scrollLeft;
};

scout.TileGrid.prototype._onScrollParentScroll = function(event) {
  this.scrolling = true;
  this.revalidateLayoutTree(false);
  this.scrolling = false;
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
  var $scrollables = this.$container.scrollParents();
  if ($scrollables.length === 0) {
    return;
  }
  var oldScrollTopArr = $scrollables.map(function(i, $elem) {
    return $elem.scrollTop();
  }).toArray();
  // Make sure the tile grid has the focus when focusing a tile
  if (this.focus()) {
    // Restore old scroll to prevent scrolling by the browser due to the focus() call
    oldScrollTopArr.forEach(function(val, idx) {
      $scrollables[idx].scrollTop(val);
    }, this);
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

  if (scout.arrays.equals(this.selectedTiles, tiles)) {
    // Do nothing if new selection is same as old one
    return;
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
  this._doubleClickSupport.mousedown(event);
  this._selectTileOnMouseDown(event);

  if (event.which === 3) {
    this.showContextMenu({
      pageX: event.pageX,
      pageY: event.pageY
    });
    return false;
  }
};

scout.TileGrid.prototype._onTileClick = function(event) {
  var $tile = $(event.currentTarget);
  var tile = $tile.data('widget');
  if (tile instanceof scout.PlaceholderTile) {
    return;
  }

  if (this._doubleClickSupport.doubleClicked()) {
    // Don't execute on double click events
    return;
  }

  var mouseButton = event.which;
  this._triggerTileClick(tile, mouseButton);
};

scout.TileGrid.prototype._triggerTileClick = function(tile, mouseButton) {
  var event = {
    tile: tile,
    mouseButton: mouseButton
  };
  this.trigger('tileClick', event);
};

scout.TileGrid.prototype._onTileDoubleClick = function(event) {
  var $tile = $(event.currentTarget);
  var tile = $tile.data('widget');
  if (tile instanceof scout.PlaceholderTile) {
    return;
  }
  this.doTileAction(tile);
};

scout.TileGrid.prototype.doTileAction = function(tile) {
  if (!tile) {
    return;
  }
  this._triggerTileAction(tile);
};

scout.TileGrid.prototype._triggerTileAction = function(tile) {
  this.trigger('tileAction', {
    tile: tile
  });
};

scout.TileGrid.prototype.setSelectionHandler = function(selectionHandler) {
  this.selectionHandler = selectionHandler;
};

scout.TileGrid.prototype._selectTileOnMouseDown = function(event) {
  this.selectionHandler.selectTileOnMouseDown(event);
};

scout.TileGrid.prototype.scrollTo = function(tile, options) {
  this.ensureTileRendered(tile);
  // If tile was not rendered it is not yet positioned correctly -> make sure layout is valid before trying to scroll
  // Layout must not render the viewport because scroll position is not correct yet -> just make sure tiles are at the correct position
  this.scrolling = true;
  this.scrollTopDirty = true;
  this.validateLayoutTree();
  this.scrolling = false;
  scout.scrollbars.scrollTo(this.$container, tile.$container, options);
  this.scrollTopDirty = false;
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
  this.addFilters([filter]);
};

scout.TileGrid.prototype.addFilters = function(filtersToAdd) {
  filtersToAdd = scout.arrays.ensure(filtersToAdd);
  var filters = this.filters.slice();
  filtersToAdd.forEach(function(filter) {
    if (filters.indexOf(filter) >= 0) {
      return;
    }
    filters.push(filter);
  });
  if (filters.length === this.filters.length) {
    return;
  }
  this.setFilters(filters);
};

scout.TileGrid.prototype.removeFilter = function(filter) {
  this.removeFilters([filter]);
};

scout.TileGrid.prototype.removeFilters = function(filtersToRemove) {
  filtersToRemove = scout.arrays.ensure(filtersToRemove);
  var filters = this.filters.slice();
  if (!scout.arrays.removeAll(filters, filtersToRemove)) {
    return;
  }
  this.setFilters(filters);
};

scout.TileGrid.prototype.setFilters = function(filters) {
  this.setProperty('filters', filters.slice());
};

scout.TileGrid.prototype.filter = function() {
  var currentTiles = this.tiles;
  // Full reset is set to true to loop through every tile and make sure tile.filterAccepted is correctly set
  var filterResult = this._applyFilters(this.tiles, true);
  this._updateFilteredTiles();
  if (this.rendered) {
    // Not all tiles may be rendered yet (e.g. if filter is active before grid is rendered and removed after grid is rendered)
    // But updating the view range is necessary anyway (fillers, scrollbars, viewRangeRendered etc.)
    this._renderTileDelta(filterResult);
    this._renderTileOrder(currentTiles);
  }
};

scout.TileGrid.prototype._applyFilters = function(tiles, fullReset) {
  if (this.filters.length === 0 && !scout.nvl(fullReset, false)) {
    return;
  }
  var newlyShownTiles = [];
  var newlyHiddenTiles = [];
  var changed = false;
  tiles.forEach(function(tile) {
    if (this._applyFiltersForTile(tile)) {
      changed = true;
      if (tile.filterAccepted) {
        newlyShownTiles.push(tile);
      } else {
        newlyHiddenTiles.push(tile);
        if (tile === this.focusedTile) {
          this.setFocusedTile(null);
        }
      }
    }
  }, this);

  if (changed) {
    this.filteredTilesDirty = true;
  }

  // Non visible tiles must be deselected
  this.deselectTiles(newlyHiddenTiles);
  return {
    newlyHiddenTiles: newlyHiddenTiles,
    newlyShownTiles: newlyShownTiles
  };
};

scout.TileGrid.prototype._updateFilteredTiles = function() {
  var tiles = this.tiles;
  if (this.filters.length > 0) {
    tiles = this._filterTiles();
  }
  if (this.filteredTilesDirty) {
    this.setProperty('filteredTiles', tiles);
    this.invalidateLogicalGrid(false);
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

/**
 * If the max range is used, the live list of filtered tiles is returned, because every tile has to be in the range.
 */
scout.TileGrid.prototype.findTilesInRange = function(viewRange, filter) {
  if (viewRange.equals(this.virtualScrolling.maxViewRange())) {
    // Directly return all tiles if max view range
    return this.filteredTiles;
  }

  var tiles = [];
  for (var row = viewRange.from; row < viewRange.to; row++) {
    this.eachTileInRow(row, function(tile) { // jshint ignore:line
      if (!filter || filter(tile)) {
        tiles.push(tile);
      }
    });
  }
  return tiles;
};

scout.TileGrid.prototype.findTilesInRow = function(row) {
  var tiles = [];
  this.eachTileInRow(row, function(tile) {
    tiles.push(tile);
  });
  return tiles;
};

/**
 * Executes the given function for each tile in a row.
 */
scout.TileGrid.prototype.eachTileInRow = function(row, func) {
  var startIndex = row * this.gridColumnCount;
  var tiles = [];
  for (var i = startIndex; i < startIndex + this.gridColumnCount; i++) {
    if (this.filteredTiles[i]) {
      func(this.filteredTiles[i], i);
    }
  }
  return tiles;
};

scout.TileGrid.prototype.setVirtual = function(virtual) {
  this.setProperty('virtual', virtual);
};

scout.TileGrid.prototype._setVirtual = function(virtual) {
  this._setProperty('virtual', virtual);
  this.virtualScrolling.setEnabled(this.virtual);
};

scout.TileGrid.prototype._renderVirtual = function() {
  this._updateVirtualScrollable();
  if (!this.rendering) {
    // No need to do it while rendering, will be done by the layout. But needs to be done if virtual changes on the fly
    this.setViewRangeSize(this.calculateViewRangeSize(), false);
  }

  if (this.rendered) {
    // When virtual toggles, remove all tiles and render them anew (to have the correct tiles rendered in the new mode)
    this._removeAllTiles();
    if (this.virtual) {
      // RenderViewPort may do nothing if all tiles are already in the view port, but fillers may not be created yet
      this._renderFiller();
    }
  }
  if (!this.virtual) {
    // Render all tiles (on toggle and initially) (_renderViewRange is not used in non virtual mode because filtered tiles need to be rendered as well)
    this._renderAllTiles();
  }

  this._renderViewPort();
  this.invalidateLayoutTree();
};

scout.TileGrid.prototype._updateVirtualScrollable = function() {
  var $scrollable = this.virtualScrolling.$scrollable;
  if ($scrollable) {
    $scrollable.off('scroll', this._scrollParentScrollHandler);
  }
  if (!this.virtual || this.removing) {
    this.virtualScrolling.set$Scrollable(null);
    return;
  }
  if (this.scrollable) {
    this.virtualScrolling.set$Scrollable(this.$container);
  } else {
    this.virtualScrolling.set$Scrollable(this.$container.scrollParent());
    this.virtualScrolling.$scrollable.on('scroll', this._scrollParentScrollHandler);
  }
};

scout.TileGrid.prototype.calculateViewRangeSize = function() {
  return this.virtualScrolling.calculateViewRangeSize();
};

scout.TileGrid.prototype.setViewRangeSize = function(viewRangeSize, updateViewPort) {
  if (this.viewRangeSize === viewRangeSize) {
    return;
  }
  this._setProperty('viewRangeSize', viewRangeSize);
  this.virtualScrolling.setViewRangeSize(viewRangeSize, updateViewPort);
};

scout.TileGrid.prototype._heightForRow = function(row) {
  var height = 0;

  height = this.htmlComp.layout.rowHeight;
  if (row !== this.rowCount() - 1) {
    // Add row gap unless it is the last row
    height += this.htmlComp.layout.vgap;
  }

  if (!scout.numbers.isNumber(height)) {
    throw new Error('Calculated height is not a number: ' + height);
  }
  return height;
};

/**
 * Used for virtual scrolling to calculate the view range size.
 * @returns the configured rowHeight + vgap / 2. Reason: the gaps are only between rows, the first and last row therefore only have 1 gap.
 */
scout.TileGrid.prototype._minRowHeight = function() {
  return this.htmlComp.layout.rowHeight + this.htmlComp.layout.vgap / 2;
};

scout.TileGrid.prototype.rowCount = function(gridColumnCount) {
  gridColumnCount = scout.nvl(gridColumnCount, this.gridColumnCount);
  return Math.ceil(this.filteredTiles.length / gridColumnCount);
};

/**
 * Calculates and renders the rows which should be visible in the current viewport based on scroll top.
 */
scout.TileGrid.prototype._renderViewPort = function() {
  if (!this.isAttachedAndRendered()) {
    // if grid is not attached the correct viewPort can not be evaluated. Mark for render after attach.
    this._renderViewPortAfterAttach = true;
    return;
  }
  if (!this.virtual) {
    return;
  }
  this.virtualScrolling._renderViewPort();
};

/**
 * Renders the rows visible in the viewport and removes the other rows
 */
scout.TileGrid.prototype._renderViewRange = function(viewRange) {
  if (viewRange.equals(this.viewRangeRendered)) {
    if (viewRange.size() === 0) {
      // Iif view range is empty initially viewRangeRendered will be empty as well -> make sure fillers are rendered correctly (used for pref size)
      this._renderFiller();
    }
    // Range already rendered -> do nothing
    return;
  }
  var rangesToRemove = this.viewRangeRendered.subtract(viewRange).filter(function(range) {
    return range.size() > 0;
  });
  rangesToRemove.forEach(function(range) {
    this._removeTilesInRange(range);
  }.bind(this));

  var rangesToRender = viewRange.subtract(this.viewRangeRendered).filter(function(range) {
    return range.size() > 0;
  });
  rangesToRender.forEach(function(range) {
    this._renderTilesInRange(range);
  }.bind(this));

  this._renderFiller();
};

scout.TileGrid.prototype._renderTilesInRange = function(range) {
  var numRowsRendered = 0;
  var tilesRendered = 0;
  var tiles = this.filteredTiles;
  if (tiles.length === 0) {
    return;
  }

  var maxRange = this.virtualScrolling.maxViewRange();
  range = maxRange.intersect(range);
  var newRange = this.viewRangeRendered.union(range);
  if (newRange.length === 2) {
    throw new Error('Can only prepend or append rows to the existing range. Existing: ' + this.viewRangeRendered + '. New: ' + newRange);
  }
  this.viewRangeRendered = newRange[0];

  for (var row = range.from; row < range.to; row++) {
    this.eachTileInRow(row, renderTile.bind(this));
    numRowsRendered++;
  }

  if ($.log.isTraceEnabled()) {
    $.log.trace(numRowsRendered + ' new rows rendered from ' + range);
    $.log.trace(this._rowsRenderedInfo());
  }

  function renderTile(tile) {
    if (tile.rendered) {
      return;
    }
    this._renderTile(tile);
    tilesRendered++;
  }
};

/**
 * @returns the newly rendered tiles
 */
scout.TileGrid.prototype._renderTileDelta = function(filterResult) {
  if (!this.virtual) {
    return;
  }
  var prevTiles = this.renderedTiles();
  var newViewRange = this.virtualScrolling.calculateCurrentViewRange();
  var newTiles = this.findTilesInRange(newViewRange);

  var tilesToRemove = scout.arrays.diff(prevTiles, newTiles);
  var tilesToRender = scout.arrays.diff(newTiles, prevTiles);
  if (filterResult) {
    filterResult.newlyHiddenTiles.forEach(function(tile) {
      if (tile.rendered) {
        this._removeTileByFilter(tile);
      }
    }, this);
  }

  // tilesToRemove contains newlyHiddenTiles as well but remove() does nothing if it is already removing
  tilesToRemove.forEach(function(tile) {
    tile.remove();
  });
  tilesToRender.forEach(function(tile) {
    this._renderTile(tile);
  }, this);

  if (filterResult) {
    // Suppress because Tile.js would invalidate which leads to poor performance if grid is used in a Group.js and group is being expanded while tiles are shown
    // invalidating will be done afterwards anyway so no need to do it for each tile
    this.htmlComp.suppressInvalidate = true;
    filterResult.newlyShownTiles.forEach(function(tile) {
      if (tile.rendered) {
        this._renderTileVisibleForFilter(tile);
      }
    }, this);
    this.htmlComp.suppressInvalidate = false;
  }

  this.viewRangeRendered = newViewRange;
  this._renderFiller();
  if (!this.htmlComp.layouting) {
    // If a tile is inserted while a group of the tile accordion is being expanded,
    // invalidating may create a loop because the group resizes the body which triggers the TileGridLayout and eventually calls this function again -> Don't invalidate while layouting
    this.invalidateLayoutTree();
  }
  return tilesToRender;
};

scout.TileGrid.prototype._removeTileByFilter = function(tile) {
  // In virtual mode, filtered tiles are not rendered. In normal mode, the filter animation is triggerd by _renderVisible of the tile.
  // Since the tile is removed immediately, the invisible animation would not start, so we use the remove animation instead.
  // But because the delete animation is a different one to the filter animation, the removeClass needs to be swapped
  // Remove class first to make sure animation won't be finished before the animationend listener is attached in Widget._removeAnimated (which may happen because a setTimeout is used there)
  tile.$container.removeClass('animate-invisible');
  tile.animateRemoval = true;
  tile.animateRemovalClass = 'animate-invisible';
  tile.remove();
  this._onAnimatedTileRemove(tile);
  tile.animateRemoval = false;
  // Remove animation is started by a set timeout -> use set timeout as well to come after
  setTimeout(function() {
    // Reset to default
    tile.animateRemovalClass = 'animate-remove';
  });
};

scout.TileGrid.prototype._renderTileVisibleForFilter = function(tile) {
  if (!tile.filterAccepted || tile.$container.hasClass('animate-visible')) {
    return;
  }
  if (tile.removalPending) {
    return;
  }
  // Start filter animation (at the time setFilterAccepted was set the tile was not rendered)
  tile.$container.setVisible(false);
  tile._renderVisible();
};

scout.TileGrid.prototype._renderTileOrder = function(prevTiles) {
  // Loop through the tiles and move every html element to the end of the container
  // Only move if the order is different to the old order
  // This is actually only necessary to make debugging easier, since the tiles are positioned absolutely it would work without it
  var different = false;
  this.tiles.forEach(function(tile, i) {
    if (prevTiles[i] !== tile || different) {
      // Start ordering as soon as the order of the arrays starts to differ
      if (this.virtual && !tile.rendered) {
        // In non virtual mode, every tile is rendered, even the filtered one. So if a tile is not rendered ignore it in virtual, but fail in non virtual
        return;
      }
      different = true;
      tile.$container.appendTo(this.$container);
    }
  }, this);

  if (different && !this.virtual) {
    // In virtual mode this is done by _renderTileDelta()
    this.invalidateLayoutTree();
  }
};

scout.TileGrid.prototype._rowsRenderedInfo = function() {
  var numRenderedTiles = this.$container.children('.tile').length,
    renderedRowsRange = '(' + this.viewRangeRendered + ')',
    text = numRenderedTiles + ' tiles rendered in range ' + renderedRowsRange;
  return text;
};

scout.TileGrid.prototype._removeTilesInRange = function(range) {
  var numRowsRemoved = 0;
  var newRange = this.viewRangeRendered.subtract(range);
  if (newRange.length === 2) {
    throw new Error('Can only remove rows at the beginning or end of the existing range. ' + this.viewRangeRendered + '. New: ' + newRange);
  }
  this.viewRangeRendered = newRange[0];

  for (var i = range.from; i < range.to; i++) {
    this._removeTilesInRow(i);
    numRowsRemoved++;
  }

  if ($.log.isTraceEnabled()) {
    $.log.trace(numRowsRemoved + ' rows removed from ' + range + '.');
    $.log.trace(this._rowsRenderedInfo());
  }
};

scout.TileGrid.prototype._removeTilesInRow = function(row) {
  var tiles = this.findTilesInRow(row);
  tiles.forEach(function(tile) {
    tile.remove();
  });
};

scout.TileGrid.prototype.rowHasRenderedTiles = function(row) {
  var tilesInRow = this.findTilesInRow(row);
  return tilesInRow.some(function(tile) {
    return tile.rendered && !tile.removing;
  });
};

scout.TileGrid.prototype.ensureTileRendered = function(tile) {
  if (!tile.rendered) {
    var rowIndex = tile.gridData.y;
    this.virtualScrolling._renderViewRangeForRowIndex(rowIndex);
    this.invalidateLayoutTree();
  }
};

scout.TileGrid.prototype._renderFiller = function() {
  if (!this.$fillBefore) {
    this.$fillBefore = this.$container.prependDiv('filler');
  }

  var fillBeforeHeight = this._calculateFillerHeight(new scout.Range(0, this.viewRangeRendered.from));
  this.$fillBefore.cssHeight(fillBeforeHeight);
  this.$fillBefore.css('width', '100%');
  $.log.isTraceEnabled() && $.log.trace('FillBefore height: ' + fillBeforeHeight);

  if (!this.$fillAfter) {
    this.$fillAfter = this.$container.appendDiv('filler');
  }
  // Make sure filler is always at the end
  this.$fillAfter.appendTo(this.$container);

  var renderedTilesHeight = this._calculateFillerHeight(new scout.Range(this.viewRangeRendered.from, this.viewRangeRendered.to));
  this.$fillAfter.cssTop(fillBeforeHeight + renderedTilesHeight);

  var fillAfterHeight = this._calculateFillerHeight(new scout.Range(this.viewRangeRendered.to, this.rowCount()));
  this.$fillAfter.cssHeight(fillAfterHeight);
  this.$fillAfter.css('width', '100%');

  $.log.isTraceEnabled() && $.log.trace('FillAfter height: ' + fillAfterHeight);
};

scout.TileGrid.prototype._calculateFillerHeight = function(range) {
  var totalHeight = 0;
  for (var i = range.from; i < range.to; i++) {
    totalHeight += this._heightForRow(i);
  }
  return totalHeight;
};

/**
 * If virtual is false, the live list of filtered tiles is returned, because every tile has to be rendered. If virtual is true, the rendered tiles are collected and returned.
 */
scout.TileGrid.prototype.renderedTiles = function() {
  if (!this.rendered) {
    return [];
  }
  if (!this.virtual) {
    return this.filteredTiles;
  }
  var tiles = [];
  this.$container.children('.tile').each(function(i, elem) {
    var tile = scout.widget(elem);
    if (!tile.removalPending) {
      // Don't return the tiles which are being removed
      // Otherwise delta could be wrong if called while removing. Example: filter is added and removed right after while the tiles are still being removed -> RenderTileDelta has to render the tiles being removed
      tiles.push(tile);
    }
  });
  return tiles;
};
