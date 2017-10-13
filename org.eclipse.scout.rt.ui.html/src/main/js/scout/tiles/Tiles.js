/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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
  this.initialAnimationDone = false;
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
  this.tiles = [];
  this._filterMenusHandler = this._filterMenus.bind(this);
  this._tilePropertyChangeHandler = this._onTilePropertyChange.bind(this);
  this._addWidgetProperties(['tiles', 'selectedTiles', 'menus']);
  this._addPreserveOnPropertyChangeProperties(['selectedTiles']);
};
scout.inherits(scout.Tiles, scout.Widget);

scout.Tiles.prototype._init = function(model) {
  scout.Tiles.parent.prototype._init.call(this, model);
  this._setGridColumnCount(this.gridColumnCount);
  this._initTiles();
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
  this._attachTile(tile);
  tile.setSelectable(this.selectable);
  tile.setSelected(this.selectedTiles.indexOf(tile) >= 0);
};

scout.Tiles.prototype._destroy = function() {
  this._destroyTiles();
  scout.Tiles.parent.prototype._destroy.call(this);
};

scout.Tiles.prototype._destroyTiles = function() {
  this.tiles.forEach(function(tile) {
    this._detachTile(tile);
  }, this);
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

scout.Tiles.prototype.insertTiles = function(tiles) {
  tiles = scout.arrays.ensure(tiles);
  this.setTiles(this.tiles.concat(tiles));
};

scout.Tiles.prototype.deleteTile = function(tile) {
  this.deleteTiles([tile]);
};

scout.Tiles.prototype.deleteTiles = function(tilesToDelete) {
  tilesToDelete = scout.arrays.ensure(tilesToDelete);
  var tiles = this.tiles.slice();
  scout.arrays.removeAll(tiles, tilesToDelete);
  this.setTiles(tiles);
};

scout.Tiles.prototype.deleteAllTiles = function() {
  this.setTiles([]);
};

scout.Tiles.prototype.setTiles = function(tiles) {
  if (scout.objects.equals(this.tiles, tiles)) {
    return;
  }

  // Ensure given tiles are real tiles (of type scout.Tile)
  tiles = this._createChildren(tiles);

  // Only delete those which are not in the new array
  // Only insert those which are not already there
  var tilesToDelete = this.tiles.slice();
  scout.arrays.removeAll(tilesToDelete, tiles);
  var tilesToInsert = tiles.slice();
  scout.arrays.removeAll(tilesToInsert, this.tiles);

  this._deleteTiles(tilesToDelete);
  if (tilesToInsert.length > 0 || tilesToDelete.length > 0) {
    this._setProperty('tiles', tiles);
  }
  this._insertTiles(tilesToInsert);
};

scout.Tiles.prototype._insertTiles = function(tiles) {
  if (tiles.length === 0) {
    return;
  }

  tiles.forEach(function(tile) {
    this._initTile(tile);
    if (this.rendered) {
      this._renderTile(tile);
    }
  }, this);

  if (this.rendered && !this.htmlComp.layouting) {
    // no need to invalidate when tile placeholders are added or removed while layouting
    this.invalidateLayoutTree();
  }
};

scout.Tiles.prototype._attachTile = function(tile) {
  tile.on('propertyChange', this._tilePropertyChangeHandler);
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
    this._detachTile(tile);
    if (this.rendered) {
      if (this._animateTileRemoval(tile)) {
        // Animate tile removal, but not while layouting when tile placeholders are added or removed
        tile.animateRemoval = true;
      }
      tile.destroy();
      this._onTileRemove(tile);
      tile.animateRemoval = false;
    }
  }, this);
  this.deselectTiles(tiles);

  if (this.rendered && !this.htmlComp.layouting) {
    // no need to invalidate when tile placeholders are added or removed while layouting
    this.invalidateLayoutTree();
  }
};

scout.Tiles.prototype._animateTileRemoval = function(tile) {
  return !(tile instanceof scout.PlaceholderTile);
};

scout.Tiles.prototype._onTileRemove = function(tile) {
  if (!tile.animateRemoval || this.tileRemovalPending) {
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

scout.Tiles.prototype._detachTile = function(tile) {
  tile.off('propertyChange', this._tilePropertyChangeHandler);
};

scout.Tiles.prototype._onTilePropertyChange = function(event) {
  if (event.propertyName === 'selected') {
    this._onTileSelected(event);
  }
};

scout.Tiles.prototype._onTileSelected = function(event) {
  if (!this.selectable) {
    event.preventDefault();
    return;
  }

  var tile = event.source;
  var selected = event.newValue;
  if (this.multiSelect && selected) {
    this.addTilesToSelection(event.source);
    return;
  }

  if (selected) {
    this.selectTile(tile);
  } else {
    this.deselectTile(tile);
  }
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
  this._insertPlaceholders();
};

scout.Tiles.prototype._createPlaceholders = function() {
  var numPlaceholders, lastX,
    columnCount = this.gridColumnCount,
    tiles = this.tiles,
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
  var tiles = [],
    obsolete = false;

  this.tiles.forEach(function(tile) {
    if (!(tile instanceof scout.PlaceholderTile)) {
      tiles.push(tile);
      return;
    }
    // Remove all placeholder in the row if there is one at x=0
    if (tile.gridData.x === 0) {
      obsolete = true;
    }
    if (!obsolete) {
      tiles.push(tile);
    }
  }, this);

  this.setTiles(tiles);
};

scout.Tiles.prototype._deleteAllPlaceholders = function() {
  var tiles = this.tiles.filter(function(tile) {
    return !(tile instanceof scout.PlaceholderTile);
  });
  this.setTiles(tiles);
};

scout.Tiles.prototype._insertPlaceholders = function() {
  var placeholders = this._createPlaceholders();
  this.insertTiles(placeholders);
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

scout.Tiles.prototype.selectTiles = function(tiles) {
  tiles = scout.arrays.ensure(tiles);
  tiles = this._prepareWidgetProperty('selectedTiles', tiles);

  // Deselect the tiles which are not part of the new selection
  var tilesToUnselect = this.selectedTiles;
  scout.arrays.removeAll(tilesToUnselect, tiles);
  tilesToUnselect.forEach(function(tile) {
    this._detachTile(tile);
    tile.setSelected(false);
    this._attachTile(tile);
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
    this._detachTile(tile);
    tile.setSelected(true);
    this._attachTile(tile);
  }, this);

  this.setProperty('selectedTiles', tiles.slice());
};

scout.Tiles.prototype.selectTile = function(tile) {
  this.selectTiles([tile]);
};

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
  if (event.which === 3) {
    this.showContextMenu({
      pageX: event.pageX,
      pageY: event.pageY
    });
    return false;
  }
};
