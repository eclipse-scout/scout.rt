/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  AbstractGrid, aria, arrays, Comparator, ContextMenuKeyStroke, ContextMenuPopup, DoubleClickSupport, EnumObject, Filter, FilterOrFunction, FilterResult, FilterSupport, FullModelOf, graphics, HorizontalGrid, HtmlComponent, InitModelOf,
  KeyStrokeContext, LoadingSupport, LogicalGrid, LogicalGridData, LogicalGridLayoutConfig, Menu, MenuDestinations, MenuFilter, menus as menuUtil, numbers, ObjectOrChildModel, ObjectOrModel, objects, PlaceholderTile, Predicate, Range,
  Resizable, scout, ScrollToOptions, TextFilter, Tile, TileGridEventMap, TileGridGridConfig, TileGridLayout, TileGridLayoutConfig, TileGridModel, TileGridMoveSupport, TileGridSelectAllKeyStroke, TileGridSelectDownKeyStroke,
  TileGridSelectFirstKeyStroke, TileGridSelectionHandler, TileGridSelectLastKeyStroke, TileGridSelectLeftKeyStroke, TileGridSelectRightKeyStroke, TileGridSelectUpKeyStroke, TileResizeHandler, TileTextFilter, UpdateFilteredElementsOptions,
  VirtualScrolling, Widget
} from '../index';
import $ from 'jquery';

/**
 * Only select top-level tile elements. Do not select elements with a 'tile' class deeper in the tree.
 * This prevents errors when a developer accidentally adds an element that has the 'tile' class. #262146
 */
const TILE_SELECTOR = '> .tile';

export class TileGrid<TTile extends Tile = Tile> extends Widget implements TileGridModel {
  declare model: TileGridModel;
  declare eventMap: TileGridEventMap;
  declare self: TileGrid<TTile>;
  declare logicalGrid: AbstractGrid;

  animateTileRemoval: boolean;
  animateTileInsertion: boolean;
  comparator: Comparator<TTile>;
  contextMenu: ContextMenuPopup;
  draggable: boolean;
  empty: boolean;
  filters: Filter<TTile>[];
  filteredElementsDirty: boolean;
  focusedTile: TTile;
  // GridColumnCount will be modified by the layout, prefGridColumnCount remains unchanged
  gridColumnCount: number;
  prefGridColumnCount: number;
  layoutConfig: TileGridLayoutConfig;
  menus: Menu[];
  multiSelect: boolean;
  renderAnimationEnabled: boolean;
  selectable: boolean;
  selectedTiles: TTile[];
  selectionHandler: TileGridSelectionHandler;
  scrollable: boolean;
  startupAnimationDone: boolean;
  startupAnimationEnabled: boolean;
  tileRemovalPendingCount: number;
  viewRangeSize: number;
  viewRangeRendered: Range;
  virtual: boolean;
  virtualScrolling: VirtualScrolling;
  withPlaceholders: boolean;
  placeholderProducer: () => ObjectOrModel<PlaceholderTile>;
  textFilterEnabled: boolean;
  filterSupport: FilterSupport<TTile>;
  createTextFilter: () => TextFilter<TTile>;
  updateTextFilterText: string;
  defaultMenuTypes: string[];
  wrappable: boolean;
  resizableProducer: (tile: Tile) => Resizable;

  $filterFieldContainer: JQuery;
  $fillBefore: JQuery;
  $fillAfter: JQuery;
  protected _moveData: any;
  protected _tiles: (TTile | PlaceholderTile)[];
  protected _filteredTiles: (TTile | PlaceholderTile)[];
  protected _doubleClickSupport: DoubleClickSupport;
  protected _filterMenusHandler: (menuItems: Menu[], destination: MenuDestinations) => Menu[];
  protected _renderViewPortAfterAttach: boolean;
  protected _scrollParentScrollHandler: (event: JQuery.ScrollEvent) => void;
  protected _dragTileMouseDownHandler: (event: JQuery.MouseDownEvent) => void;

  // protected _resizeTileMouseDownHandler: (event: JQuery.MouseDownEvent) => void;

  constructor() {
    super();
    this.animateTileRemoval = true;
    this.animateTileInsertion = true;
    this.comparator = null;
    this.contextMenu = null;
    this.draggable = false;
    this._doubleClickSupport = new DoubleClickSupport();
    this.empty = false;
    this.filters = [];
    this._filteredTiles = [];
    this.filteredElementsDirty = true;
    this.focusedTile = null;
    this.gridColumnCount = 4;
    this.prefGridColumnCount = this.gridColumnCount;
    this.logicalGrid = scout.create(HorizontalGrid);
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
    this._tiles = [];
    this.tileRemovalPendingCount = 0;
    this.viewRangeSize = 0;
    this.viewRangeRendered = new Range(0, 0);
    this.virtual = false;
    this.virtualScrolling = null;
    this.withPlaceholders = false;
    this.placeholderProducer = null;
    this.wrappable = true;
    this.resizableProducer = this._createResizableProducer();

    this.$filterFieldContainer = null;
    this.textFilterEnabled = false;
    this.filterSupport = this._createFilterSupport();
    this.createTextFilter = null;
    this.updateTextFilterText = null;

    this.defaultMenuTypes = [TileGrid.MenuType.EmptySpace];

    this._filterMenusHandler = this._filterMenus.bind(this);
    this._renderViewPortAfterAttach = false;
    this._scrollParentScrollHandler = this._onScrollParentScroll.bind(this);
    this._dragTileMouseDownHandler = this._onDragTileMouseDown.bind(this);
    // this._resizeTileMouseDownHandler = this._onResizeTileMouseDown.bind(this);
    this._addWidgetProperties(['tiles', 'selectedTiles', 'menus']);
    this._addPreserveOnPropertyChangeProperties(['selectedTiles']);
    this._addComputedProperties(['tiles', 'filteredTiles']);

    this.$fillBefore = null;
    this.$fillAfter = null;
  }

  static MenuType = {
    EmptySpace: 'TileGrid.EmptySpace',
    SingleSelection: 'TileGrid.SingleSelection',
    MultiSelection: 'TileGrid.MultiSelection'
  } as const;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setGridColumnCount(this.gridColumnCount);
    this._setLayoutConfig(this.layoutConfig);
    this._initVirtualScrolling();
    this._initTiles();
    this.setFilters(this.filters, false);
    this.filter();
    this._setMenus(this.menus);
    this._sortWhileInit();
    this.updateFilteredElements();
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected _createResizableProducer(): (tile: Tile) => Resizable {
    return tile => scout.create(TileResizeHandler, {
      tileGrid: this,
      $container: tile.$container,
      useOverlay: true
    });
  }

  setResizableProducer(producer: (tile: Tile) => Resizable) {
    this.setProperty('resizableProducer', producer);
  }

  protected _initVirtualScrolling() {
    this.virtualScrolling = this._createVirtualScrolling();
  }

  protected _createVirtualScrolling(): VirtualScrolling {
    return new VirtualScrolling({
      widget: this,
      enabled: this.virtual,
      viewRangeSize: this.viewRangeSize,
      rowHeight: this._heightForRow.bind(this),
      rowCount: this.rowCount.bind(this),
      _renderViewRange: this._renderViewRange.bind(this)
    });
  }

  protected override _createLoadingSupport(): LoadingSupport {
    return new LoadingSupport({
      widget: this
    });
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStrokes([
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

  protected _initTiles() {
    this._tiles.forEach(tile => {
      this._initTile(tile);
    });
  }

  protected _initTile(tile: (TTile | PlaceholderTile)) {
    tile.setSelectable(this.selectable);
    tile.setSelected(this.selectedTiles.indexOf(tile as TTile) >= 0);
    tile.setResizableProducer(() => this.resizableProducer(tile));

    // Set proper state in case tile was used in another grid
    tile.setParent(this);
    tile.setFilterAccepted(true);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('tile-grid');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(this._createLayout());
    this.$container
      .on('mousedown', TILE_SELECTOR, this._onTileMouseDown.bind(this))
      .on('click', TILE_SELECTOR, this._onTileClick.bind(this))
      .on('dblclick', TILE_SELECTOR, this._onTileDoubleClick.bind(this));
    this.$filterFieldContainer = this.$container.prependDiv('filter-field-container');
  }

  protected _createLayout(): TileGridLayout {
    return new TileGridLayout(this, this.layoutConfig);
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderLayoutConfig();
    this._renderScrollable();
    this._renderVirtual();
    this._renderSelectable();
    this._renderEmpty();
    this._renderTextFilterEnabled();
    this._renderDraggable();
  }

  protected override _remove() {
    this.$fillBefore = null;
    this.$fillAfter = null;
    this.filterSupport.remove();
    this.viewRangeRendered = new Range(0, 0);
    this._updateVirtualScrollable();
    super._remove();
  }

  protected override _renderOnAttach() {
    super._renderOnAttach();
    if (this._renderViewPortAfterAttach) {
      this._renderViewPort();
      this._renderViewPortAfterAttach = false;
    }
  }

  protected override _renderEnabled() {
    super._renderEnabled();

    this._updateTabbable();
  }

  protected _updateTabbable() {
    if (!this.textFilterEnabled && !this.selectable) {
      this.$container.setTabbable(false);
    } else {
      this.$container.setTabbableOrFocusable(this.enabledComputed);
    }
  }

  insertTile(tile: ObjectOrChildModel<TTile>) {
    this.insertTiles([tile]);
  }

  insertTiles(tilesToInsert: ObjectOrChildModel<TTile> | ObjectOrChildModel<TTile>[]) {
    this._insertTilesInternal(tilesToInsert);
  }

  protected _insertTilesInternal(tilesToInsert: ObjectOrChildModel<TTile | PlaceholderTile> | ObjectOrChildModel<TTile | PlaceholderTile>[], appendPlaceholders?: boolean) {
    tilesToInsert = arrays.ensure(tilesToInsert);
    if (tilesToInsert.length === 0) {
      return;
    }
    let tiles = this._tiles as ObjectOrChildModel<TTile | PlaceholderTile>[];
    this._setTilesInternal(tiles.concat(tilesToInsert), appendPlaceholders);
  }

  deleteTile(tile: TTile) {
    this.deleteTiles([tile]);
  }

  deleteTiles(tilesToDelete: TTile | (TTile)[]) {
    this._deleteTilesInternal(tilesToDelete);
  }

  protected _deleteTilesInternal(tilesToDelete: TTile | PlaceholderTile | (TTile | PlaceholderTile)[], appendPlaceholders?: boolean) {
    tilesToDelete = arrays.ensure(tilesToDelete);
    if (tilesToDelete.length === 0) {
      return;
    }
    let tiles = this._tiles.slice();
    arrays.removeAll(tiles, tilesToDelete);
    this._setTilesInternal(tiles, appendPlaceholders);
  }

  deleteAllTiles() {
    this.setTiles([]);
  }

  setTiles(tilesOrModels: ObjectOrChildModel<TTile> | ObjectOrChildModel<TTile>[]) {
    this._setTilesInternal(tilesOrModels);
  }

  protected _setTilesInternal(tilesOrModels: ObjectOrChildModel<TTile | PlaceholderTile> | ObjectOrChildModel<TTile | PlaceholderTile>[], appendPlaceholders?: boolean) {
    let tilesOrModelsArr = arrays.ensure(tilesOrModels);
    if (objects.equals(this._tiles, tilesOrModels)) {
      return;
    }

    // Ensure given tiles are real tiles (of type Tile)
    let tiles = this._createChildren(tilesOrModelsArr) as (TTile | PlaceholderTile)[];

    if (this.withPlaceholders && scout.nvl(appendPlaceholders, true)) {
      // Remove placeholders from new tiles, they will be added later
      this._deletePlaceholders(tiles);
    }

    // Only insert those which are not already there
    let tilesToInsert = arrays.diff(tiles, this._tiles);
    this._insertTiles(tilesToInsert);

    // Append the existing placeholders, otherwise they would be unnecessarily deleted if a tile is deleted
    if (this.withPlaceholders && scout.nvl(appendPlaceholders, true)) {
      let placeholders = this.placeholders();
      // But only add as many placeholders as needed: If a new tile is added, it should replace the placeholder underneath.
      // If this were not done the placeholders would move animated when a new tile is inserted rather than just staying where they are
      placeholders = placeholders.slice(Math.min(this._filterTiles(tilesToInsert).length, placeholders.length), placeholders.length);
      arrays.pushAll(tiles, placeholders);
    }

    // Only delete those which are not in the new array
    let tilesToDelete = arrays.diff(this._tiles, tiles);
    this._deleteTiles(tilesToDelete);

    this._sort(tiles);
    this.filteredElementsDirty = this.filteredElementsDirty || tilesToDelete.length > 0 || tilesToInsert.length > 0 || !arrays.equals(this._tiles, tiles); // last check necessary if sorting changed
    let currentTiles = this._tiles;
    this._setProperty('tiles', tiles);
    this.updateFilteredElements();

    if (this.rendered) {
      this._renderTileDelta();
      this._renderTileOrder(currentTiles);
      this._renderInsertTiles(tilesToInsert);
    }
  }

  protected _insertTiles(tiles: (TTile | PlaceholderTile)[]) {
    if (tiles.length === 0) {
      return;
    }

    tiles.forEach(tile => {
      this._insertTile(tile);
    });
  }

  protected _insertTile(tile: (TTile | PlaceholderTile)) {
    this._initTile(tile);
    this._applyFilters([tile]);
    if (!this.virtual && this.rendered) {
      this._renderTile(tile);
    }
  }

  protected _renderTile(tile: Tile) {
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

  protected _renderInsertTiles(tiles: (TTile | PlaceholderTile)[]) {
    if (!this.animateTileInsertion) {
      return;
    }
    tiles.forEach(tile => {
      if (!tile.rendered) {
        return;
      }
      tile.$container.addClass('before-animate-insert');
      // Wait until the layout animation is done before animating the insert operation.
      // Also make them invisible to not cover existing tiles while they are moving or changing size.
      // Also do it for tiles which don't have an insert animation (e.g. placeholders), due to the same reason.
      this.one('layoutAnimationDone', () => {
        if (tile.rendered) {
          tile.$container.removeClass('before-animate-insert');
          if (this._animateTileInsertion(tile)) {
            tile.$container.addClassForAnimation('animate-insert');
          }
        }
      });
    });

    if (!this.htmlComp.layouting) {
      // no need to invalidate when tile placeholders are added or removed while layouting
      this.invalidateLayoutTree();
    }
  }

  protected _removeAllTiles() {
    this._tiles.forEach(tile => {
      tile.remove();
    });
    this.viewRangeRendered = new Range(0, 0);
  }

  protected _renderAllTiles() {
    this._tiles.forEach(tile => {
      this._renderTile(tile);
    });
  }

  protected _deleteTiles(tiles: (TTile | PlaceholderTile)[]) {
    if (tiles.length === 0) {
      return;
    }

    tiles.forEach(tile => {
      this._deleteTile(tile);
    });
    this.deselectTiles(tiles as TTile[]);

    if (this.rendered && !this.htmlComp.layouting) {
      // no need to invalidate when tile placeholders are added or removed while layouting
      this.invalidateLayoutTree();
    }
  }

  protected _deleteTile(tile: TTile | PlaceholderTile) {
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

  protected _animateTileRemoval(tile: TTile | PlaceholderTile): boolean {
    // Explicitly check animateRemoval !== false to only enable animated removal if value is null or true to make it possible to disable it
    return this.animateTileRemoval && tile && tile.visible && !(tile instanceof PlaceholderTile) && tile.animateRemoval !== false;
  }

  protected _animateTileInsertion(tile: TTile | PlaceholderTile): boolean {
    return this.animateTileInsertion && tile && tile.visible && !(tile instanceof PlaceholderTile);
  }

  protected _onAnimatedTileRemove(tile: Tile) {
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

  /**
   * Sets a comparator that is used to sort the tiles. After setting a comparator, you need to call {@link sort}.
   *
   * The tiles will be sorted automatically whenever new tiles are inserted.
   */
  setComparator(comparator: Comparator<TTile>) {
    if (this.comparator === comparator) {
      return;
    }
    this.comparator = comparator;
  }

  protected _sortWhileInit() {
    this.sort();
  }

  sort() {
    let tiles = this._tiles.slice();
    this._sort(tiles);
    if (arrays.equals(this._tiles, tiles)) {
      // Check is needed anyway to determine whether filteredElementsDirty needs to be set, so we can use it here as well to early return if nothing changed
      return;
    }
    let currentTiles = this._tiles;
    this._setProperty('tiles', tiles);

    // Sort list of filtered tiles as well
    this.filteredElementsDirty = true;
    this.updateFilteredElements();

    if (this.rendered) {
      this._renderTileDelta();
      this._renderTileOrder(currentTiles);
    }
  }

  protected _sort(tiles: (TTile | PlaceholderTile)[]) {
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

  override invalidateLayoutTree(invalidateParents?: boolean) {
    if (this.tileRemovalPendingCount > 0) {
      // Do not invalidate while tile removal is still pending
      return;
    }
    super.invalidateLayoutTree(invalidateParents);
  }

  /** @see TileGridModel.gridColumnCount */
  setGridColumnCount(gridColumnCount: number) {
    this.setProperty('gridColumnCount', gridColumnCount);
  }

  protected _setGridColumnCount(gridColumnCount: number) {
    this._setProperty('gridColumnCount', gridColumnCount);
    this.prefGridColumnCount = gridColumnCount;
    this.invalidateLogicalGrid();
  }

  /** @see TileGridModel.layoutConfig */
  setLayoutConfig(layoutConfig: ObjectOrModel<TileGridLayoutConfig>) {
    this.setProperty('layoutConfig', layoutConfig);
  }

  protected _setLayoutConfig(layoutConfig: ObjectOrModel<TileGridLayoutConfig>) {
    if (!layoutConfig) {
      layoutConfig = new TileGridLayoutConfig();
    }
    this._setProperty('layoutConfig', TileGridLayoutConfig.ensure(layoutConfig));
    LogicalGridLayoutConfig.initHtmlEnvChangeHandler(this, () => this.layoutConfig, layoutConfig => this.setLayoutConfig(layoutConfig));
  }

  protected _renderLayoutConfig() {
    let layout = this.htmlComp.layout as TileGridLayout;
    let oldMinWidth = layout.minWidth;
    this.layoutConfig.applyToLayout(layout);
    if (this.virtualScrolling) {
      this.virtualScrolling.setMinRowHeight(this._minRowHeight());
      this.setViewRangeSize(this.virtualScrolling.viewRangeSize, false);
    }
    if (oldMinWidth !== layout.minWidth) {
      this._renderScrollable();
    }
    this.invalidateLayoutTree();
  }

  protected _setMenus(menus: Menu[]) {
    this.updateKeyStrokes(menus, this.menus);
    this._setProperty('menus', menus);
  }

  protected _filterMenus(menus: Menu[], destination: MenuDestinations, onlyVisible?: boolean, enableDisableKeyStrokes?: boolean, notAllowedTypes?: string | string[]): Menu[] {
    return menuUtil.filterAccordingToSelection('TileGrid', this.selectedTiles.length, menus, destination, {onlyVisible, enableDisableKeyStrokes, notAllowedTypes, defaultMenuTypes: this.defaultMenuTypes});
  }

  showContextMenu(options: { pageX?: number; pageY?: number }) {
    this.session.onRequestsDone(this._showContextMenu.bind(this, options));
  }

  /**
   * @param options may contain pageX, pageY, menuItems and menuFilter.
   * If these properties are not provided they are determined automatically.
   * @internal
   */
  _showContextMenu(options?: { pageX?: number; pageY?: number; menuItems?: Menu[]; menuFilter?: MenuFilter }) {
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
    this.contextMenu = scout.create(ContextMenuPopup, {
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

  /** @see TileGridModel.scrollable */
  setScrollable(scrollable: boolean) {
    this.setProperty('scrollable', scrollable);
  }

  protected _renderScrollable() {
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

  protected override _onScroll(event: JQuery.ScrollEvent) {
    let scrollTop = this.$container[0].scrollTop;
    let scrollLeft = this.$container[0].scrollLeft;
    if (this.scrollTop !== scrollTop && this.virtual) {
      (this.htmlComp.layout as TileGridLayout).updateViewPort();
    }
    this.scrollTop = scrollTop;
    this.scrollLeft = scrollLeft;
  }

  protected _onScrollParentScroll(event: JQuery.ScrollEvent) {
    (this.htmlComp.layout as TileGridLayout).updateViewPort();
  }

  setWithPlaceholders(withPlaceholders: boolean) {
    this.setProperty('withPlaceholders', withPlaceholders);
  }

  protected _renderWithPlaceholders() {
    this.invalidateLayoutTree();
  }

  setPlaceholderProducer(placeholderProducer: () => ObjectOrModel<PlaceholderTile>) {
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

  /**
   * @deprecated Use {@link tiles} instead
   */
  tilesWithoutPlaceholders(): TTile[] {
    return this.tiles;
  }

  /**
   * @returns all tiles of the tile grid without {@link PlaceholderTile}s.
   * @see TileGridModel.withPlaceholders
   */
  get tiles(): TTile[] {
    return this._tilesWithoutPlaceholders(this._tiles);
  }

  /**
   * @returns all tiles of the tile grid that accept the {@link filters} without {@link PlaceholderTile}s.
   * @see TileGridModel.filters
   * @see TileGridModel.withPlaceholders
   */
  get filteredTiles(): TTile[] {
    return this._tilesWithoutPlaceholders(this._filteredTiles);
  }

  protected _tilesWithoutPlaceholders(tiles: (TTile | PlaceholderTile)[]): TTile[] {
    if (!this.withPlaceholders) {
      // No need to filter the list if placeholders are disabled
      return tiles as TTile[];
    }
    return tiles.filter(tile => !(tile instanceof PlaceholderTile)) as TTile[];
  }

  getFilteredTilesWithPlaceholders() {
    return this._filteredTiles;
  }

  protected _createPlaceholders(): PlaceholderTile[] {
    let numPlaceholders, lastX,
      columnCount = this.gridColumnCount,
      tiles = this._filteredTiles,
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

  protected _createPlaceholder(): PlaceholderTile {
    let placeholder = (this.placeholderProducer && this.placeholderProducer()) || {};
    if (placeholder instanceof PlaceholderTile) {
      return placeholder;
    }
    if (objects.isPlainObject(placeholder)) {
      return scout.create($.extend(true, {}, {
        objectType: PlaceholderTile,
        parent: this
      }, placeholder) as FullModelOf<PlaceholderTile>);
    }
    throw new Error('Placeholder producer returned unexpected result.');
  }

  protected _deleteObsoletePlaceholders() {
    let obsoletePlaceholders = [],
      obsolete = false;

    let placeholders = this.placeholders();
    placeholders.forEach(placeholder => {
      // Remove all placeholder in the row if there is one at x=0 (don't do it if there are only placeholders)
      if (placeholder.gridData.x === 0 && this._filteredTiles[0] !== placeholder) {
        obsolete = true;
      }
      if (obsolete) {
        obsoletePlaceholders.push(placeholder);
      }
    });

    this._deleteTilesInternal(obsoletePlaceholders, false);
  }

  protected _deleteAllPlaceholders() {
    this._deleteTilesInternal(this.placeholders(), false);
  }

  placeholders(): PlaceholderTile[] {
    let i, placeholders = [];
    for (i = this._tiles.length - 1; i >= 0; i--) {
      if (!(this._tiles[i] instanceof PlaceholderTile)) {
        // Placeholders are always at the end -> we may stop as soon as no more placeholders are found
        break;
      }
      arrays.insert(placeholders, this._tiles[i], 0);
    }
    return placeholders;
  }

  protected _insertMissingPlaceholders() {
    let placeholders = this._createPlaceholders();
    this._insertTilesInternal(placeholders, false);
  }

  /**
   * @returns the deleted placeholders
   */
  protected _deletePlaceholders(tiles: (TTile | PlaceholderTile)[]): PlaceholderTile[] {
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

  override validateLogicalGrid() {
    if (!this.logicalGrid.dirty) {
      return;
    }
    this.logicalGrid.validate(this);
    this.fillUpWithPlaceholders();
    this.logicalGrid.setDirty(true);
    this.logicalGrid.validate(this);
  }

  protected override _setLogicalGrid(logicalGrid: LogicalGrid | string) {
    super._setLogicalGrid(logicalGrid);
    if (this.logicalGrid) {
      this.logicalGrid.setGridConfig(new TileGridGridConfig());
    }
  }

  setFocusedTile(tile: TTile) {
    if (this.focusedTile === tile) {
      return;
    }
    this.focusedTile = tile;
    if (tile) {
      tile.markAsActiveDescendantFor(this.$container);
    } else {
      aria.removeActiveDescendant(this.$container);
    }
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

  /** @see TileGridModel.selectable */
  setSelectable(selectable: boolean) {
    this.setProperty('selectable', selectable);
    if (!selectable) {
      this.deselectAllTiles();
    }
    this._tiles.forEach(tile => {
      tile.setSelectable(selectable);
    });
  }

  protected _renderSelectable() {
    this.$container.toggleClass('selectable', this.selectable);
    this._updateTabbable();
    this.invalidateLayoutTree();
  }

  /** @see TileGridModel.multiSelect */
  setMultiSelect(multiSelect: boolean) {
    this.setProperty('multiSelect', multiSelect);
  }

  /**
   * Selects the given tiles and deselects the previously selected ones.
   *
   * Tiles, that are currently invisible due to an active filter, are excluded and won't be selected.
   */
  selectTiles(tileOrIds: TTile | string | (TTile | string)[]) {
    // Resolve the tiles (they are not actually created, just resolved by id)
    let tiles = this._createChildren(arrays.ensure(tileOrIds));
    tiles = this._filterTiles(this._tilesWithoutPlaceholders(tiles)); // Selecting invisible tiles or placeholders is not allowed

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
    tilesToUnselect.forEach(tile => {
      tile.setSelected(false);
      if (tile === this.focusedTile) {
        this.setFocusedTile(null);
      }
    });

    // Select the tiles
    tiles.forEach(tile => {
      tile.setSelected(true);
    });

    this.setProperty('selectedTiles', tiles.slice());
  }

  /** @see selectTiles */
  selectTile(tile: TTile) {
    this.selectTiles([tile]);
  }

  /**
   * Selects all tiles. As for every selection operation: only filtered tiles are considered.
   */
  selectAllTiles() {
    this.selectTiles(this.filteredTiles);
  }

  deselectTiles(tiles: TTile | TTile[]) {
    tiles = arrays.ensure(tiles);
    let selectedTiles = this.selectedTiles.slice();
    if (arrays.removeAll(selectedTiles, tiles)) {
      this.selectTiles(selectedTiles);
    }
  }

  deselectTile(tile: TTile) {
    this.deselectTiles([tile]);
  }

  deselectAllTiles() {
    this.selectTiles([]);
  }

  /**
   * Deselects every tile if all tiles are selected. Otherwise selects all tiles.
   */
  toggleSelection() {
    if (this.selectedTiles.length === this._filteredTiles.length) {
      this.deselectAllTiles();
    } else {
      this.selectAllTiles();
    }
  }

  addTilesToSelection(tiles: TTile[]) {
    tiles = arrays.ensure(tiles);
    this.selectTiles(this.selectedTiles.concat(tiles));
  }

  addTileToSelection(tile: TTile) {
    this.addTilesToSelection([tile]);
  }

  isTileSelected(tile: TTile): boolean {
    return this.selectedTiles.includes(tile);
  }

  /**
   * @returns true if the tile is completely or partially visible in the first scrollable parent.
   */
  isTileInView(tile: TTile): boolean {
    let $scrollable = this.$container.scrollParent();
    if ($scrollable.length === 0) {
      $scrollable = this.$container;
    }
    if (!tile || !tile.$container || !$scrollable) {
      return false;
    }
    return graphics.offsetBounds(tile.$container).intersects(graphics.offsetBounds($scrollable));
  }

  /** @see TileGridModel.wrappable */
  setWrappable(wrappable: boolean) {
    this.setProperty('wrappable', wrappable);
  }

  setDraggable(draggable: boolean) {
    this.setProperty('draggable', draggable);
  }

  protected _renderDraggable() {
    if (this.draggable) {
      this.$container.on('mousedown touchstart', TILE_SELECTOR, this._dragTileMouseDownHandler);
    } else {
      this.$container.off('mousedown touchstart', TILE_SELECTOR, this._dragTileMouseDownHandler);
    }
  }

  protected _onDragTileMouseDown(event: JQuery.MouseDownEvent) {
    let $target = $(event.target);
    if ($target.hasClass('resizable-handle')) {
      return;
    }
    let tile = scout.widget($(event.currentTarget)) as Tile;
    // Install move support for each drag operation so that a tile can be dragged even if another one is still finishing dragging
    new TileGridMoveSupport(this).start(event, this.tiles, tile);
  }

  protected _onTileMouseDown(event: JQuery.MouseDownEvent): boolean {
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

  protected _onTileClick(event: JQuery.ClickEvent) {
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
    this._triggerTileClick(tile, mouseButton, event);
  }

  protected _triggerTileClick(tile: TTile, mouseButton: number, originalEvent: JQuery.ClickEvent) {
    this.trigger('tileClick', {
      tile: tile,
      mouseButton: mouseButton,
      originalEvent: originalEvent
    });
  }

  protected _onTileDoubleClick(event: JQuery.DoubleClickEvent) {
    let $tile = $(event.currentTarget);
    let tile = $tile.data('widget');
    if (tile instanceof PlaceholderTile) {
      return;
    }
    this.doTileAction(tile);
  }

  doTileAction(tile: TTile) {
    if (!tile) {
      return;
    }
    this._triggerTileAction(tile);
  }

  protected _triggerTileAction(tile: TTile) {
    this.trigger('tileAction', {
      tile: tile
    });
  }

  setSelectionHandler(selectionHandler: TileGridSelectionHandler) {
    this.selectionHandler = selectionHandler;
  }

  protected _selectTileOnMouseDown(event: JQuery.MouseDownEvent) {
    this.selectionHandler.selectTileOnMouseDown(event);
  }

  scrollTo(tile: TTile, options?: ScrollToOptions) {
    this.ensureTileRendered(tile);
    // If tile was not rendered it is not yet positioned correctly -> make sure layout is valid before trying to scroll
    // Layout must not render the viewport because scroll position is not correct yet -> just make sure tiles are at the correct position
    (this.htmlComp.layout as TileGridLayout).updateViewPort(true);
    tile.reveal(options);
  }

  /**
   * Brings the first selected tile into view by scrolling the first scrollable parent.
   */
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
   * @param filter The filters to add.
   * @param applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   */
  addFilter(filter: FilterOrFunction<TTile> | FilterOrFunction<TTile>[], applyFilter = true) {
    this.filterSupport.addFilter(filter, applyFilter);
  }

  /**
   * @param filter The filters to remove.
   * @param applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   */
  removeFilter(filter: FilterOrFunction<TTile> | FilterOrFunction<TTile>[], applyFilter = true) {
    this.filterSupport.removeFilter(filter, applyFilter);
  }

  /**
   * @param filter The new filters.
   * @param applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   */
  setFilters(filters: FilterOrFunction<TTile> | FilterOrFunction<TTile>[], applyFilter = true) {
    this.filterSupport.setFilters(filters, applyFilter);
  }

  filter() {
    this.filterSupport.filter();
  }

  protected _applyFilters(tiles: (TTile | PlaceholderTile)[], fullReset?: boolean): FilterResult<TTile> {
    return this.filterSupport.applyFilters(this._tilesWithoutPlaceholders(tiles), fullReset);
  }

  protected _createFilterSupport(): FilterSupport<TTile> {
    return new FilterSupport({
      widget: this,
      $container: () => this.$filterFieldContainer,
      getElementsForFiltering: () => this.tiles,
      createTextFilter: this._createTextFilter.bind(this),
      updateTextFilterText: this._updateTextFilterText.bind(this)
    });
  }

  protected _createTextFilter(): TextFilter<TTile> {
    if (objects.isFunction(this.createTextFilter)) {
      return this.createTextFilter();
    }
    return new TileTextFilter();
  }

  protected _updateTextFilterText(filter: Filter<TTile>, text: string): boolean {
    if (objects.isFunction(this.updateTextFilterText)) {
      return this.updateTextFilterText(filter, text);
    }
    if (filter instanceof TileTextFilter) {
      return filter.setText(text);
    }
    return false;
  }

  /** @see TileGridModel.textFilterEnabled */
  setTextFilterEnabled(textFilterEnabled: boolean) {
    this.setProperty('textFilterEnabled', textFilterEnabled);
  }

  isTextFilterFieldVisible(): boolean {
    return this.textFilterEnabled;
  }

  protected _renderTextFilterEnabled() {
    this._updateTabbable();
    this.filterSupport.renderFilterField();
  }

  updateFilteredElements(result?: FilterResult<TTile>, opts?: UpdateFilteredElementsOptions) {
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
        this._renderTileOrder(this._tiles);
      }
    }

    this._updateEmpty();
  }

  protected _updateEmpty() {
    this.setEmpty(this._filteredTiles.length === 0);
  }

  setEmpty(empty: boolean) {
    this.setProperty('empty', empty);
  }

  protected _renderEmpty() {
    this.$container.toggleClass('empty', this.empty);
    this.invalidateLayoutTree();
  }

  /**
   * @returns the tiles which are accepted by the filter and therefore visible.
   */
  protected _filterTiles<TTile extends Tile>(tiles?: TTile[]): TTile[] {
    tiles = scout.nvl(tiles, this._tiles);
    if (this.filters.length === 0) {
      return tiles.slice();
    }
    return tiles.filter(tile => tile.filterAccepted);
  }

  findTileIndexAt(x: number, y: number, startIndex?: number, reverse?: boolean): number {
    startIndex = scout.nvl(startIndex, 0);
    return arrays.findIndexFrom(this._filteredTiles, startIndex, (tile, i) => {
      return tile.gridData.x === x && tile.gridData.y === y;
    }, reverse);
  }

  findTilesInRange(viewRange: Range, filter?: Predicate<TTile>): TTile[] {
    return this._findTilesInRange(viewRange, filter, false);
  }

  /**
   * If the max range is used, the live list of filtered tiles is returned, because every tile has to be in the range.
   * @param considerPlaceholders whether placeholder tiles should be processed as well. Default is true.
   */
  protected _findTilesInRange<T extends boolean>(viewRange: Range, filter?: Predicate<TTile>, considerPlaceholders?: T): T extends true ? (TTile | PlaceholderTile)[] : TTile[] {
    considerPlaceholders = scout.nvl(considerPlaceholders, true);
    if (viewRange.equals(this.virtualScrolling.maxViewRange())) {
      // Directly return all tiles if max view range
      return considerPlaceholders ? this._filteredTiles : this.filteredTiles as any;
    }

    let tiles = [];
    for (let row = viewRange.from; row < viewRange.to; row++) {
      this._eachTileInRow(row, tile => {
        if (!filter || tile instanceof PlaceholderTile || filter(tile)) {
          tiles.push(tile);
        }
      }, considerPlaceholders);
    }
    return tiles;
  }

  findTilesInRow(row: number): TTile[] {
    return this._findTilesInRow(row, false);
  }

  /**
   * @param considerPlaceholders whether placeholder tiles should be processed as well. Default is true.
   */
  protected _findTilesInRow<T extends boolean>(row: number, considerPlaceholders?: T): T extends true ? (TTile | PlaceholderTile)[] : TTile[] {
    let tiles = [];
    this._eachTileInRow(row, tile => {
      tiles.push(tile);
    }, considerPlaceholders);
    return tiles;
  }

  eachTileInRow(row: number, func: (tile: TTile, index: number) => void): TTile[] {
    return this._eachTileInRow(row, func, false);
  }

  /**
   * Executes the given function for each tile in a row.
   * @param considerPlaceholders whether placeholder tiles should be processed as well. Default is true.
   */
  protected _eachTileInRow<T extends boolean>(row: number, func: (tile: TTile | PlaceholderTile, index: number) => void, considerPlaceholders?: T): T extends true ? (TTile | PlaceholderTile)[] : TTile[] {
    let startIndex = row * this.gridColumnCount;
    let tiles = [];
    for (let i = startIndex; i < startIndex + this.gridColumnCount; i++) {
      let tile = this._filteredTiles[i];
      if (!scout.nvl(considerPlaceholders, true) && tile instanceof PlaceholderTile) {
        continue;
      }
      if (this._filteredTiles[i]) {
        func(this._filteredTiles[i], i);
      }
    }
    return tiles;
  }

  /** @see TileGridModel.virtual */
  setVirtual(virtual: boolean) {
    this.setProperty('virtual', virtual);
  }

  protected _setVirtual(virtual: boolean) {
    this._setProperty('virtual', virtual);
    this.virtualScrolling.setEnabled(this.virtual);
  }

  protected _renderVirtual() {
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

  protected _updateVirtualScrollable() {
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

  calculateViewRangeSize(): number {
    return this.virtualScrolling.calculateViewRangeSize();
  }

  /** @see TileGrid.viewRangeSize */
  setViewRangeSize(viewRangeSize: number, updateViewPort?: boolean) {
    if (this.viewRangeSize === viewRangeSize) {
      return;
    }
    this._setProperty('viewRangeSize', viewRangeSize);
    this.virtualScrolling.setViewRangeSize(viewRangeSize, updateViewPort);
  }

  protected _heightForRow(row: number): number {
    let height = 0;

    let layout = this.htmlComp.layout as TileGridLayout;
    height = layout.rowHeight;
    if (row !== this.rowCount() - 1) {
      // Add row gap unless it is the last row
      height += layout.vgap;
    }

    if (!numbers.isNumber(height)) {
      throw new Error('Calculated height is not a number: ' + height);
    }
    return height;
  }

  /**
   * Used for virtual scrolling to calculate the view range size.
   * @returns the configured rowHeight + vgap / 2. Reason: the gaps are only between rows, the first and last row therefore only have 1 gap.
   */
  protected _minRowHeight(): number {
    let layout = this.htmlComp.layout as TileGridLayout;
    return layout.rowHeight + layout.vgap / 2;
  }

  rowCount(gridColumnCount?: number): number {
    gridColumnCount = scout.nvl(gridColumnCount, this.gridColumnCount);
    return Math.ceil(this._filteredTiles.length / gridColumnCount);
  }

  /**
   * Calculates and renders the rows which should be visible in the current viewport based on scroll top.
   * @internal
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
    this.virtualScrolling.renderViewPort();
  }

  /**
   * Renders the rows visible in the viewport and removes the other rows
   */
  protected _renderViewRange(viewRange: Range) {
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

  protected _renderTilesInRange(range: Range) {
    let numRowsRendered = 0;
    let tilesRendered = 0;
    let tiles = this._filteredTiles;
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
      this._eachTileInRow(row, renderTile.bind(this));
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
   * @returns the newly rendered tiles
   * @internal
   */
  _renderTileDelta(filterResult?: FilterResult<TTile | PlaceholderTile>): (TTile | PlaceholderTile)[] {
    if (!this.virtual) {
      return [];
    }
    let prevTiles = this.renderedTiles(true);
    let newViewRange = this.virtualScrolling.calculateCurrentViewRange();
    let newTiles = this._findTilesInRange(newViewRange);

    let tilesToRemove = arrays.diff(prevTiles, newTiles);
    let tilesToRender = arrays.diff(newTiles, prevTiles);
    if (filterResult) {
      filterResult.newlyHidden.forEach(tile => {
        if (tile.rendered) {
          this._removeTileByFilter(tile);
        }
      });
    }

    // tilesToRemove contains newlyHidden as well but remove() does nothing if it is already removing
    tilesToRemove.forEach(tile => {
      tile.remove();
    });
    tilesToRender.forEach(tile => {
      this._renderTile(tile);
    });

    if (filterResult) {
      // Suppress because Tile.js would invalidate which leads to poor performance if grid is used in a Group.js and group is being expanded while tiles are shown
      // invalidating will be done afterwards anyway so no need to do it for each tile
      this.htmlComp.suppressInvalidate = true;
      filterResult.newlyShown.forEach(tile => {
        if (tile.rendered) {
          this._renderTileVisibleForFilter(tile);
        }
      });
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

  protected _removeTileByFilter(tile: TTile | PlaceholderTile) {
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

  protected _renderTileVisibleForFilter(tile: Tile) {
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

  protected _renderTileOrder(prevTiles: (TTile | PlaceholderTile)[]) {
    // Loop through the tiles and move every html element to the end of the container
    // Only move if the order is different to the old order
    // This is actually only necessary to make debugging easier, since the tiles are positioned absolutely it would work without it
    let different = false;
    this._tiles.forEach((tile, i) => {
      if (prevTiles[i] !== tile || different) {
        // Start ordering as soon as the order of the arrays starts to differ
        if (this.virtual && !tile.rendered) {
          // In non virtual mode, every tile is rendered, even the filtered one. So if a tile is not rendered ignore it in virtual, but fail in non virtual
          return;
        }
        different = true;
        tile.$container.appendTo(this.$container);
      }
    });

    if (different && !this.virtual) {
      // In virtual mode this is done by _renderTileDelta()
      this.invalidateLayoutTree();
    }
  }

  protected _rowsRenderedInfo(): string {
    let numRenderedTiles = this.$container.children('.tile').length;
    let renderedRowsRange = '(' + this.viewRangeRendered + ')';
    return numRenderedTiles + ' tiles rendered in range ' + renderedRowsRange;
  }

  protected _removeTilesInRange(range: Range) {
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

  protected _removeTilesInRow(row: number) {
    let tiles = this._findTilesInRow(row);
    tiles.forEach(tile => {
      tile.remove();
    });
  }

  ensureTileRendered(tile: TTile) {
    if (!tile.rendered) {
      let rowIndex = tile.gridData.y;
      this.virtualScrolling.renderViewRangeForRowIndex(rowIndex);
      this.invalidateLayoutTree();
    }
  }

  protected _renderFiller() {
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

  protected _calculateFillerHeight(range: Range): number {
    let totalHeight = 0;
    for (let i = range.from; i < range.to; i++) {
      totalHeight += this._heightForRow(i);
    }
    return totalHeight;
  }

  /**
   * If virtual is false, the live list of filtered tiles is returned, because every tile has to be rendered.
   * If virtual is true, the rendered tiles are collected and returned.
   */
  renderedTiles<T extends boolean>(considerPlaceholders?: T): T extends true ? (TTile | PlaceholderTile)[] : TTile[] {
    if (!this.rendered) {
      return [];
    }
    considerPlaceholders = scout.nvl(considerPlaceholders, false);
    if (!this.virtual) {
      return considerPlaceholders ? this._filteredTiles : this.filteredTiles as any;
    }
    let tiles = [];
    this.$container.children('.tile').each((i, elem) => {
      let tile = scout.widget(elem);
      if (!considerPlaceholders && tile instanceof PlaceholderTile) {
        return;
      }
      if (!tile.removalPending) {
        // Don't return the tiles which are being removed
        // Otherwise delta could be wrong if called while removing. Example: filter is added and removed right after while the tiles are still being removed -> RenderTileDelta has to render the tiles being removed
        tiles.push(tile);
      }
    });
    return tiles;
  }
}

export type TileGridMenuType = EnumObject<typeof TileGrid.MenuType>;
