/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, ContextMenuKeyStroke, DoubleClickSupport, FilterSupport, graphics, HtmlComponent, KeyStrokeContext, LoadingSupport, LogicalGridData, MenuDestinations, menus as menus_1, numbers, objects, PlaceholderTile, Range, scout, TileGridGridConfig, TileGridLayout, TileGridLayoutConfig, TileGridSelectAllKeyStroke, TileGridSelectDownKeyStroke, TileGridSelectFirstKeyStroke, TileGridSelectionHandler, TileGridSelectLastKeyStroke, TileGridSelectLeftKeyStroke, TileGridSelectRightKeyStroke, TileGridSelectUpKeyStroke, TileTextFilter, VirtualScrolling, Widget} from '../index';
import $ from 'jquery';

/**
 * Only select top-level tile elements. Do not select elements with a 'tile' class deeper in the tree.
 * This prevents errors when a developer accidentally adds an element that has the 'tile' class. #262146
 * @type {string}
 */
const TILE_SELECTOR = '> .tile';

export default class TileGrid extends Widget {
  constructor() {
    super();
    this.animateTileRemoval = true;
    this.animateTileInsertion = true;
    this.comparator = null;
    this._doubleClickSupport = new DoubleClickSupport();
    this.empty = false;
    this.filters = [];
    this.filteredTiles = [];
    this.filteredElementsDirty = true;
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
    this.selectionHandler = new TileGridSelectionHandler(this);
    this.scrollable = true;
    this.startupAnimationDone = false;
    this.startupAnimationEnabled = false;
    this.tiles = [];
    this.tileRemovalPendingCount = 0;
    this.viewRangeSize = 0;
    this.viewRangeRendered = new Range(0, 0);
    this.virtual = false;
    this.virtualScrolling = null;
    this.withPlaceholders = false;
    this.placeholderProducer = null;

    this.$filterFieldContainer = null;
    this.textFilterEnabled = false;
    this.filterSupport = this._createFilterSupport();
    this.createTextFilter = null;
    this.updateTextFilterText = null;

    this._filterMenusHandler = this._filterMenus.bind(this);
    this._renderViewPortAfterAttach = false;
    this._scrollParentScrollHandler = this._onScrollParentScroll.bind(this);
    this._addWidgetProperties(['tiles', 'selectedTiles', 'menus']);
    this._addPreserveOnPropertyChangeProperties(['selectedTiles']);

    this.$fillBefore = null;
    this.$fillAfter = null;
  }

  _init(model) {
    super._init(model);
    this._setGridColumnCount(this.gridColumnCount);
    this._setLayoutConfig(this.layoutConfig);
    this._initVirtualScrolling();
    this._initTiles();
    this.setFilters(this.filters, false);
    this.filter();
    this.updateFilteredElements();
    this._setMenus(this.menus);
  }

  /**
   * @override
   */
  _createKeyStrokeContext() {
    return new KeyStrokeContext();
  }

  _initVirtualScrolling() {
    this.virtualScrolling = this._createVirtualScrolling();
  }

  _createVirtualScrolling() {
    return new VirtualScrolling({
      widget: this,
      enabled: this.virtual,
      viewRangeSize: this.viewRangeSize,
      rowHeight: this._heightForRow.bind(this),
      rowCount: this.rowCount.bind(this),
      _renderViewRange: this._renderViewRange.bind(this)
    });
  }

  /**
   * @override
   */
  _createLoadingSupport() {
    return new LoadingSupport({
      widget: this
    });
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStroke([
      new TileGridSelectAllKeyStroke(this),
      new TileGridSelectLeftKeyStroke(this),
      new TileGridSelectRightKeyStroke(this),
      new TileGridSelectDownKeyStroke(this),
      new TileGridSelectUpKeyStroke(this),
      new TileGridSelectFirstKeyStroke(this),
      new TileGridSelectLastKeyStroke(this),
      new ContextMenuKeyStroke(this, this.showContextMenu, this)
    ]);
  }

  _initTiles() {
    this.tiles.forEach(function(tile) {
      this._initTile(tile);
    }, this);
  }

  _initTile(tile) {
    tile.setSelectable(this.selectable);
    tile.setSelected(this.selectedTiles.indexOf(tile) >= 0);

    // Set proper state in case tile was used in another grid
    tile.setParent(this);
    tile.setFilterAccepted(true);
  }

  _render() {
    this.$container = this.$parent.appendDiv('tile-grid');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(this._createLayout());
    this.$container
      .on('mousedown', TILE_SELECTOR, this._onTileMouseDown.bind(this))
      .on('click', TILE_SELECTOR, this._onTileClick.bind(this))
      .on('dblclick', TILE_SELECTOR, this._onTileDoubleClick.bind(this));
    this.$filterFieldContainer = this.$container.prependDiv('filter-field-container');
  }

  _createLayout() {
    return new TileGridLayout(this, this.layoutConfig);
  }

  _renderProperties() {
    super._renderProperties();
    this._renderLayoutConfig();
    this._renderScrollable();
    this._renderVirtual();
    this._renderSelectable();
    this._renderEmpty();
    this._renderTextFilterEnabled();
  }

  _remove() {
    this.$fillBefore = null;
    this.$fillAfter = null;
    this.filterSupport.remove();
    this.viewRangeRendered = new Range(0, 0);
    this._updateVirtualScrollable();
    super._remove();
  }

  /**
   * @override
   */
  _renderOnAttach() {
    super._renderOnAttach();
    if (this._renderViewPortAfterAttach) {
      this._renderViewPort();
      this._renderViewPortAfterAttach = false;
    }
  }

  _renderEnabled() {
    super._renderEnabled();

    this._updateTabbable();
  }

  _updateTabbable() {
    if (!this.textFilterEnabled && !this.selectable) {
      this.$container.setTabbable(false);
    } else {
      this.$container.setTabbableOrFocusable(this.enabledComputed);
    }
  }

  insertTile(tile) {
    this.insertTiles([tile]);
  }

  insertTiles(tilesToInsert, appendPlaceholders) {
    tilesToInsert = arrays.ensure(tilesToInsert);
    if (tilesToInsert.length === 0) {
      return;
    }
    this.setTiles(this.tiles.concat(tilesToInsert), appendPlaceholders);
  }

  deleteTile(tile) {
    this.deleteTiles([tile]);
  }

  deleteTiles(tilesToDelete, appendPlaceholders) {
    tilesToDelete = arrays.ensure(tilesToDelete);
    if (tilesToDelete.length === 0) {
      return;
    }
    let tiles = this.tiles.slice();
    arrays.removeAll(tiles, tilesToDelete);
    this.setTiles(tiles, appendPlaceholders);
  }

  deleteAllTiles() {
    this.setTiles([]);
  }

  setTiles(tiles, appendPlaceholders) {
    tiles = arrays.ensure(tiles);
    if (objects.equals(this.tiles, tiles)) {
      return;
    }

    // Ensure given tiles are real tiles (of type Tile)
    tiles = this._createChildren(tiles);

    if (this.withPlaceholders && scout.nvl(appendPlaceholders, true)) {
      // Remove placeholders from new tiles, they will be added later
      this._deletePlaceholders(tiles);
    }

    // Only insert those which are not already there
    let tilesToInsert = arrays.diff(tiles, this.tiles);
    this._insertTiles(tilesToInsert);

    // Append the existing placeholders, otherwise they would be unnecessarily deleted if a tile is deleted
    if (this.withPlaceholders && scout.nvl(appendPlaceholders, true)) {
      let placeholders = this.placeholders();
      // But only add as much placeholders as needed: If a new tile is added, it should replace the placeholder underneath.
      // If this were not done the placeholders would move animated when a new tile is inserted rather than just staying where they are
      placeholders = placeholders.slice(Math.min(this._filterTiles(tilesToInsert).length, placeholders.length), placeholders.length);
      arrays.pushAll(tiles, placeholders);
    }

    // Only delete those which are not in the new array
    let tilesToDelete = arrays.diff(this.tiles, tiles);
    this._deleteTiles(tilesToDelete);

    this._sort(tiles);
    this.filteredElementsDirty = this.filteredElementsDirty || tilesToDelete.length > 0 || tilesToInsert.length > 0 || !arrays.equals(this.tiles, tiles); // last check necessary if sorting changed
    let currentTiles = this.tiles;
    this._setProperty('tiles', tiles);
    this.updateFilteredElements();

    if (this.rendered) {
      this._renderTileDelta();
      this._renderTileOrder(currentTiles);
      this._renderInsertTiles(tilesToInsert);
    }
  }

  _insertTiles(tiles) {
    if (tiles.length === 0) {
      return;
    }

    tiles.forEach(function(tile) {
      this._insertTile(tile);
    }, this);
  }

  _insertTile(tile) {
    this._initTile(tile);
    this._applyFilters([tile]);
    if (!this.virtual && this.rendered) {
      this._renderTile(tile);
    }
  }

  _renderTile(tile) {
    if (tile.removalPending) {
      // If tile is being removed by the filter and the filter cleared so that the tile should be rendered again while the animation is still running,
      // we need to wait for the remove animation, otherwise an already rendered exception occurs
      tile.one('remove', () => {
        if (tile.rendered) {
          // Might be already rendered again by renderTileDelta because filter was changed again
          return;
        }
        this._renderTile(tile);
        this._renderTileVisibleForFilter(tile);
        if (this.tileRemovalPendingCount === 0) {
          this.invalidateLayoutTree();
        }
      });
      return;
    }
    tile.render();
    tile.setLayoutData(new LogicalGridData(tile));
    tile.$container.addClass('newly-rendered');
  }

  _renderInsertTiles(tiles) {
    tiles.forEach(function(tile) {
      if (!tile.rendered) {
        return;
      }
      tile.$container.addClass('invisible');
      // Wait until the layout animation is done before animating the insert operation.
      // Also make them invisible to not cover existing tiles while they are moving or changing size.
      // Also do it for tiles which don't have an insert animation (e.g. placeholders), due to the same reason.
      this.one('layoutAnimationDone', () => {
        if (tile.rendered) {
          tile.$container.removeClass('invisible');
          if (this._animateTileInsertion(tile)) {
            tile.$container.addClassForAnimation('animate-insert');
          }
        }
      });
    }, this);

    if (!this.htmlComp.layouting) {
      // no need to invalidate when tile placeholders are added or removed while layouting
      this.invalidateLayoutTree();
    }
  }

  _removeAllTiles() {
    this.tiles.forEach(tile => {
      tile.remove();
    });
    this.viewRangeRendered = new Range(0, 0);
  }

  _renderAllTiles() {
    this.tiles.forEach(function(tile) {
      this._renderTile(tile);
    }, this);
  }

  _deleteTiles(tiles) {
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
  }

  _deleteTile(tile) {
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
  }

  _animateTileRemoval(tile) {
    return this.animateTileRemoval && tile && tile.isVisible() && !(tile instanceof PlaceholderTile);
  }

  _animateTileInsertion(tile) {
    return this.animateTileInsertion && tile && tile.isVisible() && !(tile instanceof PlaceholderTile);
  }

  _onAnimatedTileRemove(tile) {
    if (!tile.removalPending) {
      return;
    }
    this.tileRemovalPendingCount++;
    tile.one('remove', () => {
      this.tileRemovalPendingCount--;
      if (this.rendered && this.tileRemovalPendingCount === 0 && !this.htmlComp.layouting) {
        this.invalidateLayoutTree();
      }
    });
  }

  setComparator(comparator) {
    if (this.comparator === comparator) {
      return;
    }
    this.comparator = comparator;
  }

  sort() {
    let tiles = this.tiles.slice();
    this._sort(tiles);
    if (arrays.equals(this.tiles, tiles)) {
      // Check is needed anyway to determine whether filteredElementsDirty needs to be set, so we can use it here as well to early return if nothing changed
      return;
    }
    let currentTiles = this.tiles;
    this._setProperty('tiles', tiles);

    // Sort list of filtered tiles as well
    this.filteredElementsDirty = true;
    this.updateFilteredElements();

    if (this.rendered) {
      this._renderTileDelta();
      this._renderTileOrder(currentTiles);
    }
  }

  _sort(tiles) {
    if (this.comparator === null) {
      return;
    }

    let placeholders = [];
    if (this.withPlaceholders) {
      // Don't reorder placeholders -> remove them first, then sort and add them afterwards again
      placeholders = this._deletePlaceholders(tiles);
    }
    tiles.sort(this.comparator);
    arrays.pushAll(tiles, placeholders);
  }

  invalidateLayoutTree(invalidateParents) {
    if (this.tileRemovalPendingCount > 0) {
      // Do not invalidate while tile removal is still pending
      return;
    }
    super.invalidateLayoutTree(invalidateParents);
  }

  setGridColumnCount(gridColumnCount) {
    this.setProperty('gridColumnCount', gridColumnCount);
  }

  _setGridColumnCount(gridColumnCount) {
    this._setProperty('gridColumnCount', gridColumnCount);
    this.prefGridColumnCount = gridColumnCount;
    this.invalidateLogicalGrid();
  }

  setLayoutConfig(layoutConfig) {
    this.setProperty('layoutConfig', layoutConfig);
  }

  _setLayoutConfig(layoutConfig) {
    if (!layoutConfig) {
      layoutConfig = new TileGridLayoutConfig();
    }
    this._setProperty('layoutConfig', TileGridLayoutConfig.ensure(layoutConfig));
  }

  _renderLayoutConfig() {
    let oldMinWidth = this.htmlComp.layout.minWidth;
    this.layoutConfig.applyToLayout(this.htmlComp.layout);
    if (this.virtualScrolling) {
      this.virtualScrolling.setMinRowHeight(this._minRowHeight());
      this.setViewRangeSize(this.virtualScrolling.viewRangeSize, false);
    }
    if (oldMinWidth !== this.htmlComp.layout.minWidth) {
      this._renderScrollable();
    }
    this.invalidateLayoutTree();
  }

  _setMenus(menus, oldMenus) {
    this.updateKeyStrokes(menus, oldMenus);
    this._setProperty('menus', menus);
  }

  _filterMenus(menus, destination, onlyVisible, enableDisableKeyStroke, notAllowedTypes) {
    return menus_1.filterAccordingToSelection('TileGrid', this.selectedTiles.length, menus, destination, onlyVisible, enableDisableKeyStroke, notAllowedTypes);
  }

  showContextMenu(options) {
    this.session.onRequestsDone(this._showContextMenu.bind(this, options));
  }

  /**
   * @param options may contain pageX, pageY, menuItems and menuFilter.
   * If these properties are not provided they are determined automatically.
   */
  _showContextMenu(options) {
    options = options || {};
    if (!this.rendered || !this.attached) { // check needed because function is called asynchronously
      return;
    }
    if (this.selectedTiles.length === 0) {
      return;
    }
    let menuItems = options.menuItems || this._filterMenus(this.menus, MenuDestinations.CONTEXT_MENU, true, false);
    if (menuItems.length === 0) {
      return;
    }
    let pageX = scout.nvl(options.pageX, null);
    let pageY = scout.nvl(options.pageY, null);
    if (pageX === null || pageY === null) {
      let offset;
      let $scrollable = this.$container.scrollParent();
      if ($scrollable.length === 0) {
        $scrollable = this.$container;
      }
      let scrollableBounds = graphics.offsetBounds($scrollable);
      let focusedTile = this.focusedTile || arrays.last(this.selectedTiles);
      if (this.isTileInView(focusedTile)) {
        // Place the context menu on the focused tile if possible
        offset = focusedTile.$container.offset();
      } else {
        // If focused tile is not in view place the popup in the top left corner of the tile grid
        offset = this.$container.offset();
      }
      pageX = offset.left + 10;
      pageY = offset.top + 10;
      // Ensure popup is always in view. Add +-1 to make sure it won't be made invisible by Popup._isInView even if bounds are fractional
      pageX = Math.min(Math.max(pageX, scrollableBounds.x + 1), scrollableBounds.right() - 1);
      pageY = Math.min(Math.max(pageY, scrollableBounds.y + 1), scrollableBounds.bottom() - 1);
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
      menuFilter: options.menuFilter || this._filterMenusHandler
    });
    this.contextMenu.open();
  }

  setScrollable(scrollable) {
    this.setProperty('scrollable', scrollable);
  }

  _renderScrollable() {
    this._uninstallScrollbars();

    // horizontal (x-axis) scrollbar is only installed when minWidth is > 0
    if (this.scrollable) {
      this._installScrollbars({
        axis: this.layoutConfig.minWidth > 0 ? 'both' : 'y'
      });
    } else if (this.layoutConfig.minWidth > 0) {
      this._installScrollbars({
        axis: 'x'
      });
    }
    this.$container.toggleClass('scrollable', this.scrollable);
    this._updateVirtualScrollable();
    this.invalidateLayoutTree();
  }

  /**
   * @override
   */
  _onScroll() {
    let scrollTop = this.$container[0].scrollTop;
    let scrollLeft = this.$container[0].scrollLeft;
    if (this.scrollTop !== scrollTop && this.virtual) {
      this.htmlComp.layout.updateViewPort();
    }
    this.scrollTop = scrollTop;
    this.scrollLeft = scrollLeft;
  }

  _onScrollParentScroll(event) {
    this.htmlComp.layout.updateViewPort();
  }

  setWithPlaceholders(withPlaceholders) {
    this.setProperty('withPlaceholders', withPlaceholders);
  }

  _renderWithPlaceholders() {
    this.invalidateLayoutTree();
  }

  setPlaceholderProducer(placeholderProducer) {
    this.setProperty('placeholderProducer', placeholderProducer);
  }

  fillUpWithPlaceholders() {
    if (!this.withPlaceholders) {
      this._deleteAllPlaceholders();
      return;
    }
    this._deleteObsoletePlaceholders();
    this._insertMissingPlaceholders();
  }

  tilesWithoutPlaceholders() {
    if (!this.withPlaceholders) {
      return this.tiles;
    }
    return this.tiles.filter(tile => !(tile instanceof PlaceholderTile));
  }

  _createPlaceholders() {
    let numPlaceholders, lastX,
      columnCount = this.gridColumnCount,
      tiles = this.filteredTiles,
      placeholders = [];

    if (tiles.length > 0) {
      let tile = tiles[tiles.length - 1];
      lastX = tile.gridData.x + tile.gridData.w - 1;
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
    for (let i = 0; i < numPlaceholders; i++) {
      placeholders.push(this._createPlaceholder());
    }
    return placeholders;
  }

  _createPlaceholder() {
    let placeholder = (this.placeholderProducer && this.placeholderProducer()) || {};
    if (placeholder instanceof PlaceholderTile) {
      return placeholder;
    }
    if (objects.isPlainObject(placeholder)) {
      return scout.create($.extend(true, {}, {
        objectType: 'PlaceholderTile',
        parent: this
      }, placeholder));
    }
    throw new Error('Placeholder producer returned unexpected result.');
  }

  _deleteObsoletePlaceholders() {
    let obsoletePlaceholders = [],
      obsolete = false;

    let placeholders = this.placeholders();
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
  }

  _deleteAllPlaceholders() {
    this.deleteTiles(this.placeholders(), false);
  }

  placeholders() {
    let i, placeholders = [];
    for (i = this.tiles.length - 1; i >= 0; i--) {
      if (!(this.tiles[i] instanceof PlaceholderTile)) {
        // Placeholders are always at the end -> we may stop as soon as no more placeholders are found
        break;
      }
      arrays.insert(placeholders, this.tiles[i], 0);
    }
    return placeholders;
  }

  _insertMissingPlaceholders() {
    let placeholders = this._createPlaceholders();
    this.insertTiles(placeholders, false);
  }

  _deletePlaceholders(tiles) {
    let i;
    let deletedPlaceholders = [];
    for (i = tiles.length - 1; i >= 0; i--) {
      if (tiles[i] instanceof PlaceholderTile) {
        deletedPlaceholders.push(tiles[i]);
        arrays.remove(tiles, tiles[i]);
      }
    }
    return deletedPlaceholders.reverse();
  }

  _replacePlaceholders(tiles, tilesToInsert) {
    // Find index of the first tile which is not a placeholder (placeholders are always added at the end, so it is faster if search is done backwards)
    let index = arrays.findIndexFromReverse(tiles, tiles.length - 1, tile => {
      return !(tile instanceof PlaceholderTile);
    });

    let numPlaceholders = tiles.length - 1 - index;
    for (let i = 1; i <= numPlaceholders; i++) {
      let tile = tiles[index + i];
      if (tilesToInsert[i - 1] && !(tilesToInsert[i - 1] instanceof PlaceholderTile)) {
        arrays.remove(tiles, tile);
      }
    }
  }

  validateLogicalGrid() {
    if (!this.logicalGrid.dirty) {
      return;
    }
    this.logicalGrid.validate(this);
    this.fillUpWithPlaceholders();
    this.logicalGrid.setDirty(true);
    this.logicalGrid.validate(this);
  }

  /**
   * @override
   */
  _setLogicalGrid(logicalGrid) {
    super._setLogicalGrid(logicalGrid);
    if (this.logicalGrid) {
      this.logicalGrid.setGridConfig(new TileGridGridConfig());
    }
  }

  setFocusedTile(tile) {
    if (this.focusedTile === tile) {
      return;
    }
    this.focusedTile = tile;
    if (!this.rendered || !tile || this.isFocused()) {
      return;
    }
    let $scrollables = this.$container.scrollParents();
    if ($scrollables.length === 0) {
      return;
    }
    // Make sure the tile grid has the focus when focusing a tile
    this.focus({
      preventScroll: true
    });
  }

  setSelectable(selectable) {
    this.setProperty('selectable', selectable);
    if (!selectable) {
      this.deselectAllTiles();
    }
    this.tiles.forEach(tile => {
      tile.setSelectable(selectable);
    });
  }

  _renderSelectable() {
    this.$container.toggleClass('selectable', this.selectable);
    this._updateTabbable();
    this.invalidateLayoutTree();
  }

  setMultiSelect(multiSelect) {
    this.setProperty('multiSelect', multiSelect);
  }

  /**
   * Selects the given tiles and deselects the previously selected ones.
   */
  selectTiles(tiles) {
    tiles = arrays.ensure(tiles);
    // Ensure given tiles are real tiles (of type Tile)
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

    if (arrays.equals(this.selectedTiles, tiles)) {
      // Do nothing if new selection is same as old one
      return;
    }

    // Deselect the tiles which are not part of the new selection
    let tilesToUnselect = this.selectedTiles;
    arrays.removeAll(tilesToUnselect, tiles);
    tilesToUnselect.forEach(function(tile) {
      tile.setSelected(false);
      if (tile === this.focusedTile) {
        this.setFocusedTile(null);
      }
    }, this);

    // Select the tiles
    tiles.forEach(tile => {
      tile.setSelected(true);
    }, this);

    this.setProperty('selectedTiles', tiles.slice());
  }

  selectTile(tile) {
    this.selectTiles([tile]);
  }

  /**
   * Selects all tiles. As for every selection operation: only filtered tiles are considered.
   */
  selectAllTiles() {
    this.selectTiles(this.filteredTiles);
  }

  deselectTiles(tiles) {
    tiles = arrays.ensure(tiles);
    let selectedTiles = this.selectedTiles.slice();
    if (arrays.removeAll(selectedTiles, tiles)) {
      this.selectTiles(selectedTiles);
    }
  }

  deselectTile(tile) {
    this.deselectTiles([tile]);
  }

  deselectAllTiles() {
    this.selectTiles([]);
  }

  toggleSelection() {
    if (this.selectedTiles.length === this.filteredTiles.length) {
      this.deselectAllTiles();
    } else {
      this.selectAllTiles();
    }
  }

  addTilesToSelection(tiles) {
    tiles = arrays.ensure(tiles);
    this.selectTiles(this.selectedTiles.concat(tiles));
  }

  addTileToSelection(tile) {
    this.addTilesToSelection([tile]);
  }

  /**
   * @returns {boolean} true if the tile is completely or partially visible in the first scrollable parent.
   */
  isTileInView(tile) {
    let $scrollable = this.$container.scrollParent();
    if ($scrollable.length === 0) {
      $scrollable = this.$container;
    }
    if (!tile || !tile.$container || !$scrollable) {
      return false;
    }
    return graphics.offsetBounds(tile.$container).intersects(graphics.offsetBounds($scrollable));
  }

  _onTileMouseDown(event) {
    this._doubleClickSupport.mousedown(event);
    this._selectTileOnMouseDown(event);

    if (event.which === 3) {
      this.showContextMenu({
        pageX: event.pageX,
        pageY: event.pageY
      });
      return false;
    }
  }

  _onTileClick(event) {
    let $tile = $(event.currentTarget);
    let tile = $tile.data('widget');
    if (tile instanceof PlaceholderTile) {
      return;
    }

    if (this._doubleClickSupport.doubleClicked()) {
      // Don't execute on double click events
      return;
    }

    let mouseButton = event.which;
    this._triggerTileClick(tile, mouseButton);
  }

  _triggerTileClick(tile, mouseButton) {
    let event = {
      tile: tile,
      mouseButton: mouseButton
    };
    this.trigger('tileClick', event);
  }

  _onTileDoubleClick(event) {
    let $tile = $(event.currentTarget);
    let tile = $tile.data('widget');
    if (tile instanceof PlaceholderTile) {
      return;
    }
    this.doTileAction(tile);
  }

  doTileAction(tile) {
    if (!tile) {
      return;
    }
    this._triggerTileAction(tile);
  }

  _triggerTileAction(tile) {
    this.trigger('tileAction', {
      tile: tile
    });
  }

  setSelectionHandler(selectionHandler) {
    this.selectionHandler = selectionHandler;
  }

  _selectTileOnMouseDown(event) {
    this.selectionHandler.selectTileOnMouseDown(event);
  }

  scrollTo(tile, options) {
    this.ensureTileRendered(tile);
    // If tile was not rendered it is not yet positioned correctly -> make sure layout is valid before trying to scroll
    // Layout must not render the viewport because scroll position is not correct yet -> just make sure tiles are at the correct position
    this.htmlComp.layout.updateViewPort(true);
    tile.reveal(options);
  }

  revealSelection() {
    if (!this.rendered) {
      // Execute delayed because tileGrid may be not layouted yet
      this.session.layoutValidator.schedulePostValidateFunction(this.revealSelection.bind(this));
      return;
    }

    if (this.selectedTiles.length > 0) {
      this.scrollTo(this.selectedTiles[0]);
    }
  }

  /**
   * @param {Filter|function|(Filter|function)[]} filter The filters to add.
   * @param {boolean} applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   */
  addFilter(filter, applyFilter = true) {
    this.filterSupport.addFilter(filter, applyFilter);
  }

  /**
   * @param {Filter|function|(Filter|function)[]} filter The filters to remove.
   * @param {boolean} applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   */
  removeFilter(filter, applyFilter = true) {
    this.filterSupport.removeFilter(filter, applyFilter);
  }

  /**
   * @param {Filter|function|(Filter|function)[]} filter The new filters.
   * @param {boolean} applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   */
  setFilters(filters, applyFilter = true) {
    this.filterSupport.setFilters(filters, applyFilter);
  }

  filter() {
    this.filterSupport.filter();
  }

  _applyFilters(tiles, fullReset) {
    return this.filterSupport.applyFilters(tiles.filter(tile => !(tile instanceof PlaceholderTile)), fullReset);
  }

  /**
   * @returns {FilterSupport}
   */
  _createFilterSupport() {
    return new FilterSupport({
      widget: this,
      $container: () => this.$filterFieldContainer,
      getElementsForFiltering: this.tilesWithoutPlaceholders.bind(this),
      createTextFilter: this._createTextFilter.bind(this),
      updateTextFilterText: this._updateTextFilterText.bind(this)
    });
  }

  _createTextFilter() {
    if (objects.isFunction(this.createTextFilter)) {
      return this.createTextFilter();
    }
    return new TileTextFilter();
  }

  _updateTextFilterText(filter, text) {
    if (objects.isFunction(this.updateTextFilterText)) {
      return this.updateTextFilterText(filter, text);
    }
    if (filter instanceof TileTextFilter) {
      return filter.setText(text);
    }
    return false;
  }

  setTextFilterEnabled(textFilterEnabled) {
    this.setProperty('textFilterEnabled', textFilterEnabled);
  }

  isTextFilterFieldVisible() {
    return this.textFilterEnabled;
  }

  _renderTextFilterEnabled() {
    this._updateTabbable();
    this.filterSupport.renderFilterField();
  }

  updateFilteredElements(result, opts) {
    if (!this.filteredElementsDirty) {
      this._updateEmpty();
      return;
    }

    this.setProperty('filteredTiles', this._filterTiles());
    this.invalidateLogicalGrid(false);
    this.filteredElementsDirty = false;

    if (result) {
      if (result.newlyHidden.some(tile => tile === this.focusedTile)) {
        this.setFocusedTile(null);
      }
      // Non visible tiles must be deselected
      this.deselectTiles(result.newlyHidden);

      if (this.rendered && !this.removing) {
        // Not all tiles may be rendered yet (e.g. if filter is active before grid is rendered and removed after grid is rendered)
        // But updating the view range is necessary anyway (fillers, scrollbars, viewRangeRendered etc.)
        this._renderTileDelta(result);
        this._renderTileOrder(this.tiles);
      }
    }

    this._updateEmpty();
  }

  _updateEmpty() {
    this.setEmpty(this.filteredTiles.length === 0);
  }

  setEmpty(empty) {
    this.setProperty('empty', empty);
  }

  _renderEmpty() {
    this.$container.toggleClass('empty', this.empty);
    this.invalidateLayoutTree();
  }

  /**
   * @returns {Tile[]} the tiles which are accepted by the filter and therefore visible.
   */
  _filterTiles(tiles) {
    tiles = scout.nvl(tiles, this.tiles);
    if (this.filters.length === 0) {
      return tiles.slice();
    }
    return tiles.filter(tile => {
      return tile.filterAccepted;
    });
  }

  findTileIndexAt(x, y, startIndex, reverse) {
    startIndex = scout.nvl(startIndex, 0);
    return arrays.findIndexFrom(this.filteredTiles, startIndex, (tile, i) => {
      return tile.gridData.x === x && tile.gridData.y === y;
    }, reverse);
  }

  /**
   * If the max range is used, the live list of filtered tiles is returned, because every tile has to be in the range.
   */
  findTilesInRange(viewRange, filter) {
    if (viewRange.equals(this.virtualScrolling.maxViewRange())) {
      // Directly return all tiles if max view range
      return this.filteredTiles;
    }

    let tiles = [];
    for (let row = viewRange.from; row < viewRange.to; row++) {
      this.eachTileInRow(row, tile => { // jshint ignore:line
        if (!filter || filter(tile)) {
          tiles.push(tile);
        }
      });
    }
    return tiles;
  }

  findTilesInRow(row) {
    let tiles = [];
    this.eachTileInRow(row, tile => {
      tiles.push(tile);
    });
    return tiles;
  }

  /**
   * Executes the given function for each tile in a row.
   */
  eachTileInRow(row, func) {
    let startIndex = row * this.gridColumnCount;
    let tiles = [];
    for (let i = startIndex; i < startIndex + this.gridColumnCount; i++) {
      if (this.filteredTiles[i]) {
        func(this.filteredTiles[i], i);
      }
    }
    return tiles;
  }

  setVirtual(virtual) {
    this.setProperty('virtual', virtual);
  }

  _setVirtual(virtual) {
    this._setProperty('virtual', virtual);
    this.virtualScrolling.setEnabled(this.virtual);
  }

  _renderVirtual() {
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
  }

  _updateVirtualScrollable() {
    let $scrollable = this.virtualScrolling.$scrollable;
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
  }

  calculateViewRangeSize() {
    return this.virtualScrolling.calculateViewRangeSize();
  }

  setViewRangeSize(viewRangeSize, updateViewPort) {
    if (this.viewRangeSize === viewRangeSize) {
      return;
    }
    this._setProperty('viewRangeSize', viewRangeSize);
    this.virtualScrolling.setViewRangeSize(viewRangeSize, updateViewPort);
  }

  _heightForRow(row) {
    let height = 0;

    height = this.htmlComp.layout.rowHeight;
    if (row !== this.rowCount() - 1) {
      // Add row gap unless it is the last row
      height += this.htmlComp.layout.vgap;
    }

    if (!numbers.isNumber(height)) {
      throw new Error('Calculated height is not a number: ' + height);
    }
    return height;
  }

  /**
   * Used for virtual scrolling to calculate the view range size.
   * @returns {number} the configured rowHeight + vgap / 2. Reason: the gaps are only between rows, the first and last row therefore only have 1 gap.
   */
  _minRowHeight() {
    return this.htmlComp.layout.rowHeight + this.htmlComp.layout.vgap / 2;
  }

  rowCount(gridColumnCount) {
    gridColumnCount = scout.nvl(gridColumnCount, this.gridColumnCount);
    return Math.ceil(this.filteredTiles.length / gridColumnCount);
  }

  /**
   * Calculates and renders the rows which should be visible in the current viewport based on scroll top.
   */
  _renderViewPort() {
    if (!this.isAttachedAndRendered()) {
      // if grid is not attached the correct viewPort can not be evaluated. Mark for render after attach.
      this._renderViewPortAfterAttach = true;
      return;
    }
    if (!this.virtual) {
      return;
    }
    this.virtualScrolling._renderViewPort();
  }

  /**
   * Renders the rows visible in the viewport and removes the other rows
   */
  _renderViewRange(viewRange) {
    if (viewRange.equals(this.viewRangeRendered)) {
      if (viewRange.size() === 0) {
        // Iif view range is empty initially viewRangeRendered will be empty as well -> make sure fillers are rendered correctly (used for pref size)
        this._renderFiller();
      }
      // Range already rendered -> do nothing
      return;
    }
    let rangesToRemove = this.viewRangeRendered.subtract(viewRange).filter(range => {
      return range.size() > 0;
    });
    rangesToRemove.forEach(range => {
      this._removeTilesInRange(range);
    });

    let rangesToRender = viewRange.subtract(this.viewRangeRendered).filter(range => {
      return range.size() > 0;
    });
    rangesToRender.forEach(range => {
      this._renderTilesInRange(range);
    });

    this._renderFiller();
  }

  _renderTilesInRange(range) {
    let numRowsRendered = 0;
    let tilesRendered = 0;
    let tiles = this.filteredTiles;
    if (tiles.length === 0) {
      return;
    }

    let maxRange = this.virtualScrolling.maxViewRange();
    range = maxRange.intersect(range);
    let newRange = this.viewRangeRendered.union(range);
    if (newRange.length === 2) {
      throw new Error('Can only prepend or append rows to the existing range. Existing: ' + this.viewRangeRendered + '. New: ' + newRange);
    }
    this.viewRangeRendered = newRange[0];

    for (let row = range.from; row < range.to; row++) {
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
  }

  /**
   * @returns {Tile[]} the newly rendered tiles
   */
  _renderTileDelta(filterResult) {
    if (!this.virtual) {
      return [];
    }
    let prevTiles = this.renderedTiles();
    let newViewRange = this.virtualScrolling.calculateCurrentViewRange();
    let newTiles = this.findTilesInRange(newViewRange);

    let tilesToRemove = arrays.diff(prevTiles, newTiles);
    let tilesToRender = arrays.diff(newTiles, prevTiles);
    if (filterResult) {
      filterResult.newlyHidden.forEach(function(tile) {
        if (tile.rendered) {
          this._removeTileByFilter(tile);
        }
      }, this);
    }

    // tilesToRemove contains newlyHidden as well but remove() does nothing if it is already removing
    tilesToRemove.forEach(tile => {
      tile.remove();
    });
    tilesToRender.forEach(function(tile) {
      this._renderTile(tile);
    }, this);

    if (filterResult) {
      // Suppress because Tile.js would invalidate which leads to poor performance if grid is used in a Group.js and group is being expanded while tiles are shown
      // invalidating will be done afterwards anyway so no need to do it for each tile
      this.htmlComp.suppressInvalidate = true;
      filterResult.newlyShown.forEach(function(tile) {
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
  }

  _removeTileByFilter(tile) {
    // In virtual mode, filtered tiles are not rendered. In normal mode, the filter animation is triggered by _renderVisible of the tile.
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
    setTimeout(() => {
      // Reset to default
      tile.animateRemovalClass = 'animate-remove';
    });
  }

  _renderTileVisibleForFilter(tile) {
    if (!tile.filterAccepted || tile.$container.hasClass('animate-visible')) {
      return;
    }
    if (tile.removalPending) {
      return;
    }
    // Start filter animation (at the time setFilterAccepted was set the tile was not rendered)
    tile.$container.setVisible(false);
    tile._renderVisible();
  }

  _renderTileOrder(prevTiles) {
    // Loop through the tiles and move every html element to the end of the container
    // Only move if the order is different to the old order
    // This is actually only necessary to make debugging easier, since the tiles are positioned absolutely it would work without it
    let different = false;
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
  }

  _rowsRenderedInfo() {
    let numRenderedTiles = this.$container.children('.tile').length;
    let renderedRowsRange = '(' + this.viewRangeRendered + ')';
    return numRenderedTiles + ' tiles rendered in range ' + renderedRowsRange;
  }

  _removeTilesInRange(range) {
    let numRowsRemoved = 0;
    let newRange = this.viewRangeRendered.subtract(range);
    if (newRange.length === 2) {
      throw new Error('Can only remove rows at the beginning or end of the existing range. ' + this.viewRangeRendered + '. New: ' + newRange);
    }
    this.viewRangeRendered = newRange[0];

    for (let i = range.from; i < range.to; i++) {
      this._removeTilesInRow(i);
      numRowsRemoved++;
    }

    if ($.log.isTraceEnabled()) {
      $.log.trace(numRowsRemoved + ' rows removed from ' + range + '.');
      $.log.trace(this._rowsRenderedInfo());
    }
  }

  _removeTilesInRow(row) {
    let tiles = this.findTilesInRow(row);
    tiles.forEach(tile => {
      tile.remove();
    });
  }

  rowHasRenderedTiles(row) {
    let tilesInRow = this.findTilesInRow(row);
    return tilesInRow.some(tile => {
      return tile.rendered && !tile.removing;
    });
  }

  ensureTileRendered(tile) {
    if (!tile.rendered) {
      let rowIndex = tile.gridData.y;
      this.virtualScrolling._renderViewRangeForRowIndex(rowIndex);
      this.invalidateLayoutTree();
    }
  }

  _renderFiller() {
    if (!this.$fillBefore) {
      this.$fillBefore = this.$container.prependDiv('filler');
    }

    let fillBeforeHeight = this._calculateFillerHeight(new Range(0, this.viewRangeRendered.from));
    this.$fillBefore.cssHeight(fillBeforeHeight);
    this.$fillBefore.css('width', '100%');
    $.log.isTraceEnabled() && $.log.trace('FillBefore height: ' + fillBeforeHeight);

    if (!this.$fillAfter) {
      this.$fillAfter = this.$container.appendDiv('filler');
    }
    // Make sure filler is always at the end
    this.$fillAfter.appendTo(this.$container);

    let renderedTilesHeight = this._calculateFillerHeight(new Range(this.viewRangeRendered.from, this.viewRangeRendered.to));
    this.$fillAfter.cssTop(fillBeforeHeight + renderedTilesHeight);

    let fillAfterHeight = this._calculateFillerHeight(new Range(this.viewRangeRendered.to, this.rowCount()));
    this.$fillAfter.cssHeight(fillAfterHeight);
    this.$fillAfter.css('width', '100%');

    $.log.isTraceEnabled() && $.log.trace('FillAfter height: ' + fillAfterHeight);
  }

  _calculateFillerHeight(range) {
    let totalHeight = 0;
    for (let i = range.from; i < range.to; i++) {
      totalHeight += this._heightForRow(i);
    }
    return totalHeight;
  }

  /**
   * If virtual is false, the live list of filtered tiles is returned, because every tile has to be rendered. If virtual is true, the rendered tiles are collected and returned.
   */
  renderedTiles() {
    if (!this.rendered) {
      return [];
    }
    if (!this.virtual) {
      return this.filteredTiles;
    }
    let tiles = [];
    this.$container.children('.tile').each((i, elem) => {
      let tile = scout.widget(elem);
      if (!tile.removalPending) {
        // Don't return the tiles which are being removed
        // Otherwise delta could be wrong if called while removing. Example: filter is added and removed right after while the tiles are still being removed -> RenderTileDelta has to render the tiles being removed
        tiles.push(tile);
      }
    });
    return tiles;
  }
}
