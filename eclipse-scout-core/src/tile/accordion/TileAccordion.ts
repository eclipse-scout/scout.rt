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
  Accordion, arrays, Comparator, Event, EventDelegator, EventHandler, Filter, FilterOrFunction, FilterResult, FilterSupport, Group, InitModelOf, KeyStrokeContext, ObjectOrChildModel, ObjectOrModel, objects, PropertyChangeEvent, Rectangle,
  scout, TextFilter, Tile, TileAccordionEventMap, TileAccordionLayout, TileAccordionModel, TileAccordionSelectionHandler, TileGrid, TileGridLayout, TileGridLayoutConfig, TileTextFilter
} from '../../index';

export class TileAccordion<TTile extends Tile = Tile> extends Accordion implements TileAccordionModel {
  declare model: TileAccordionModel;
  declare eventMap: TileAccordionEventMap;
  declare self: TileAccordion<TTile>;
  declare groups: Group<TileGrid<TTile>>[];

  gridColumnCount: number;
  multiSelect: boolean;
  selectable: boolean;
  takeTileFiltersFromGroup: boolean;
  tileComparator: Comparator<TTile>;
  filters: Filter<TTile>[];
  tileGridLayoutConfig: TileGridLayoutConfig;
  tileGridSelectionHandler: TileAccordionSelectionHandler;
  withPlaceholders: boolean;
  virtual: boolean;
  textFilterEnabled: boolean;
  filterSupport: FilterSupport<TTile>;
  createTextFilter: () => TextFilter<TTile>;
  updateTextFilterText: string;
  $filterFieldContainer: JQuery;
  protected _selectionUpdateLocked: boolean;
  protected _tileGridPropertyChangeHandler: EventHandler<PropertyChangeEvent>;
  protected _groupBodyHeightChangeHandler: EventHandler<Event<Group<TileGrid<TTile>>>>;

  constructor() {
    super();
    this.exclusiveExpand = false;
    this.gridColumnCount = null;
    this.multiSelect = null;
    this.selectable = null;
    this.takeTileFiltersFromGroup = true;
    this.tileComparator = null;
    this.filters = [];
    this.tileGridLayoutConfig = null;
    this.tileGridSelectionHandler = new TileAccordionSelectionHandler(this);
    this.withPlaceholders = null;
    this.virtual = null;

    this.$filterFieldContainer = null;
    this.textFilterEnabled = false;
    this.filterSupport = this._createFilterSupport();
    this.createTextFilter = null;
    this.updateTextFilterText = null;

    this._selectionUpdateLocked = false;
    this._tileGridPropertyChangeHandler = this._onTileGridPropertyChange.bind(this);
    this._groupBodyHeightChangeHandler = this._onGroupBodyHeightChange.bind(this);
  }

  protected override _render() {
    super._render();
    this.$container.addClass('tile-accordion');
    this.$filterFieldContainer = this.$container.prependDiv('filter-field-container');
  }

  protected override _createLayout(): TileAccordionLayout {
    return new TileAccordionLayout(this);
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderTextFilterEnabled();
  }

  protected override _remove() {
    this.filterSupport.remove();
    super._remove();
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.setFilters(this.filters);
    this._setTileGridLayoutConfig(this.tileGridLayoutConfig);
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _initGroup(group: Group<TileGrid<TTile>> & { body: TileGrid<TTile> & { __tileAccordionEventDelegator?: EventDelegator } }) {
    super._initGroup(group);
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

    if (this.filters.length > 0) {
      group.body.addFilter(this.filters);
    }
    if (this.takeTileFiltersFromGroup) {
      this.setFilters(group.body.filters);
    }

    if (this.withPlaceholders !== null) {
      group.body.setWithPlaceholders(this.withPlaceholders);
    }
    this.setProperty('withPlaceholders', group.body.withPlaceholders);

    if (this.virtual !== null) {
      group.body.setVirtual(this.virtual);
    }
    this.setProperty('virtual', group.body.virtual);

    if (group.body.selectedTiles.length > 0) {
      this._handleSelectionChanged(group.body);
    }

    group.body.on('propertyChange', this._tileGridPropertyChangeHandler);
    this._handleCollapsed(group);

    // Delegate events so that consumers don't need to attach a listener to each tile grid by themselves
    group.body.__tileAccordionEventDelegator = EventDelegator.create(group.body, this, {
      delegateEvents: ['tileClick', 'tileAction']
    });
  }

  protected override _deleteGroup(group: Group<TileGrid<TTile>> & { body: TileGrid<TTile> & { __tileAccordionEventDelegator?: EventDelegator } }) {
    if (group.body) {
      group.body.off('propertyChange', this._tileGridPropertyChangeHandler);
      group.body.__tileAccordionEventDelegator.destroy();
      group.body.__tileAccordionEventDelegator = null;
    }
    super._deleteGroup(group);
  }

  override setGroups(groups: Group<TileGrid<TTile>>[]) {
    let oldTileCount = this.getTileCount();
    let oldFilteredTileCount = this.getFilteredTileCount();
    let oldSelectedTileCount = this.getSelectedTileCount();
    super.setGroups(groups);

    let tileCount = this.getTileCount();
    let filteredTileCount = this.getFilteredTileCount();
    let selectedTileCount = this.getSelectedTileCount();

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
  }

  /** @see TileAccordionModel.gridColumnCount */
  setGridColumnCount(gridColumnCount: number) {
    this.groups.forEach(group => {
      group.body.setGridColumnCount(gridColumnCount);
    });
    this.setProperty('gridColumnCount', gridColumnCount);
  }

  /** @see TileAccordionModel.tileGridLayoutConfig */
  setTileGridLayoutConfig(layoutConfig: ObjectOrModel<TileGridLayoutConfig>) {
    this.groups.forEach(group => {
      group.body.setLayoutConfig(layoutConfig);
      layoutConfig = group.body.layoutConfig; // May be converted from plain object to TileGridLayoutConfig
    });
    this.setProperty('tileGridLayoutConfig', layoutConfig);
  }

  protected _setTileGridLayoutConfig(layoutConfig: ObjectOrModel<TileGridLayoutConfig>) {
    this._setProperty('tileGridLayoutConfig', TileGridLayoutConfig.ensure(layoutConfig));
  }

  /** @see TileAccordionModel.withPlaceholders */
  setWithPlaceholders(withPlaceholders: boolean) {
    this.groups.forEach(group => {
      group.body.setWithPlaceholders(withPlaceholders);
    });
    this.setProperty('withPlaceholders', withPlaceholders);
  }

  /** @see TileAccordionModel.virtual */
  setVirtual(virtual: boolean) {
    this.groups.forEach(group => {
      group.body.setVirtual(virtual);
    });
    this.setProperty('virtual', virtual);
  }

  /** @see TileAccordionModel.selectable */
  setSelectable(selectable: boolean) {
    this.groups.forEach(group => {
      group.body.setSelectable(selectable);
    });
    this.setProperty('selectable', selectable);
  }

  /** @see TileAccordionModel.multiSelect */
  setMultiSelect(multiSelect: boolean) {
    this.groups.forEach(group => {
      group.body.setMultiSelect(multiSelect);
    });
    this.setProperty('multiSelect', multiSelect);
  }

  getGroupById(id: string): Group<TileGrid<TTile>> {
    return arrays.find(this.groups, group => group.id === id);
  }

  getGroupByTile(tile: TTile): Group<TileGrid<TTile>> {
    return tile.findParent(Group<TileGrid<TTile>>);
  }

  /**
   * Distribute the tiles to the corresponding groups and returns an object with group id as key and array of tiles as value.
   * Always returns all current groups even if the given tiles may not be distributed to all groups.
   */
  protected _groupTiles(tiles: TTile[]): Record<string, TTile[]> {
    // Create a map of groups, key is the id, value is an array of tiles
    let tilesPerGroup = {};
    this.groups.forEach(group => {
      tilesPerGroup[group.id] = [];
    });

    // Distribute the tiles to the corresponding groups
    tiles.forEach(function(tile) {
      let group = this.getGroupByTile(tile);
      if (!group) {
        throw new Error('No group found for tile ' + tile.id);
      }
      if (!tilesPerGroup[group.id]) {
        tilesPerGroup[group.id] = [];
      }
      tilesPerGroup[group.id].push(tile);
    }, this);

    return tilesPerGroup;
  }

  deleteTile(tile: TTile) {
    this.deleteTiles([tile]);
  }

  deleteTiles(tilesToDelete: TTile[] | TTile) {
    tilesToDelete = arrays.ensure(tilesToDelete);
    if (tilesToDelete.length === 0) {
      return;
    }
    let tiles = this.getTiles();
    arrays.removeAll(tiles, tilesToDelete);
    this.setTiles(tiles);
  }

  deleteAllTiles() {
    this.setTiles([]);
  }

  /**
   * Distributes the given tiles to their corresponding groups.
   *
   * If the list contains new tiles not assigned to a group yet, an exception will be thrown.
   */
  setTiles(tilesOrModels: ObjectOrChildModel<TTile> | ObjectOrChildModel<TTile>[]) {
    let tilesOrModelsArr = arrays.ensure(tilesOrModels);
    if (objects.equals(this.getTiles(), tilesOrModelsArr)) {
      return;
    }

    // Ensure given tiles are real tiles (of type TTile)
    let tiles = this._createChildren(tilesOrModelsArr);

    // Distribute the tiles to the corresponding groups (result may contain groups without tiles)
    let tilesPerGroup = this._groupTiles(tiles);

    // Update the tile grids
    for (let id in tilesPerGroup) { // NOSONAR
      let group = this.getGroupById(id);
      group.body.setTiles(tilesPerGroup[id]);
    }
  }

  getTiles(): TTile[] {
    let tiles = [];
    this.groups.forEach(group => {
      arrays.pushAll(tiles, group.body.tiles);
    });
    return tiles;
  }

  getTileCount(): number {
    let count = 0;
    this.groups.forEach(group => {
      count += group.body.tiles.length;
    });
    return count;
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

  protected _setFilters(filter: FilterOrFunction<TTile> | FilterOrFunction<TTile>[]) {
    let filters = arrays.ensure(filter);
    this.groups.forEach(group => {
      group.body.setFilters(filters.slice(), false);
    });
    this._setProperty('filters', filters.slice());
  }

  filter() {
    this.filterSupport.filter();
  }

  protected _filter(): FilterResult<TTile> {
    this.groups.forEach(group => group.body.filter());
    return null; // FilterSupport of the TileGrids take care of the results
  }

  protected _createFilterSupport(): FilterSupport<TTile> {
    return new FilterSupport({
      widget: this,
      $container: () => this.$filterFieldContainer,
      filterElements: this._filter.bind(this),
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

  /** @see TileAccordionModel.textFilterEnabled */
  setTextFilterEnabled(textFilterEnabled: boolean) {
    this.setProperty('textFilterEnabled', textFilterEnabled);
  }

  isTextFilterFieldVisible(): boolean {
    return this.textFilterEnabled;
  }

  protected _renderTextFilterEnabled() {
    this.filterSupport.renderFilterField();
  }

  getFilteredTiles(): TTile[] {
    let tiles = [];
    this.groups.forEach(group => {
      arrays.pushAll(tiles, group.body.filteredTiles);
    });
    return tiles;
  }

  getFilteredTileCount(): number {
    let count = 0;
    this.groups.forEach(group => {
      count += group.body.filteredTiles.length;
    });
    return count;
  }

  /**
   * Compared to {@link getFilteredTiles()}, this function considers the collapsed state of the group as well, meaning only filtered tiles of expanded groups are returned.
   */
  getVisibleTiles(): TTile[] {
    let tiles = [];
    this.expandedGroups().forEach(group => {
      arrays.pushAll(tiles, group.body.filteredTiles);
    });
    return tiles;
  }

  /**
   * Compared to {@link getFilteredTiles()}, this function considers the collapsed state of the group as well, meaning only filtered tiles of expanded groups are counted.
   */
  getVisibleTileCount(): number {
    let count = 0;
    this.expandedGroups().forEach(group => {
      count += group.body.filteredTiles.length;
    });
    return count;
  }

  findVisibleTileIndexAt(x: number, y: number, startIndex?: number, reverse?: boolean): number {
    startIndex = scout.nvl(startIndex, 0);
    return arrays.findIndexFrom(this.getVisibleTiles(), startIndex, (tile, i) => {
      return new Rectangle(this.getVisibleGridX(tile), this.getVisibleGridY(tile), tile.gridData.w, tile.gridData.h).contains(x, y);
    }, reverse);
  }

  /**
   * Selects the given tiles and deselects the previously selected ones.
   *
   * Tiles, that are currently invisible due to an active filter, are excluded and won't be selected.
   */
  selectTiles(tiles: TTile[]) {
    tiles = arrays.ensure(tiles);

    // Split tiles into separate lists for each group (result may contain groups without tiles)
    let tilesPerGroup = this._groupTiles(tiles);

    // Select the tiles in the corresponding tile grids
    for (let id in tilesPerGroup) { // NOSONAR
      let group = this.getGroupById(id);
      group.body.selectTiles(tilesPerGroup[id]);
    }
  }

  /** @see selectTiles */
  selectTile(tile: TTile) {
    this.selectTiles([tile]);
  }

  /**
   * Selects all tiles. As for every selection operation: only considers filtered tiles and tiles of expanded groups
   */
  selectAllTiles() {
    this.selectTiles(this.getVisibleTiles());
  }

  deselectTiles(tiles: TTile[]) {
    tiles = arrays.ensure(tiles);
    let selectedTiles = this.getSelectedTiles().slice();
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

  addTilesToSelection(tiles: TTile[]) {
    tiles = arrays.ensure(tiles);
    this.selectTiles(this.getSelectedTiles().concat(tiles));
  }

  addTileToSelection(tile: TTile) {
    this.addTilesToSelection([tile]);
  }

  getSelectedTiles(): TTile[] {
    let selectedTiles = [];
    this.groups.forEach(group => {
      arrays.pushAll(selectedTiles, group.body.selectedTiles);
    });
    return selectedTiles;
  }

  getSelectedTile(): TTile {
    return this.getSelectedTiles()[0];
  }

  getSelectedTileCount(): number {
    let count = 0;
    this.groups.forEach(group => {
      count += group.body.selectedTiles.length;
    });
    return count;
  }

  /**
   * Deselects every tile if all tiles are selected. Otherwise, selects all tiles.
   */
  toggleSelection() {
    if (this.getSelectedTileCount() === this.getVisibleTileCount()) {
      this.deselectAllTiles();
    } else {
      this.selectAllTiles();
    }
  }

  /** @see TileAccordionModel.tileComparator */
  setTileComparator(comparator: Comparator<TTile>) {
    this.groups.forEach(group => {
      group.body.setComparator(comparator);
    });
    this.setProperty('tileComparator', comparator);
  }

  sortTiles() {
    this.groups.forEach(group => {
      group.body.sort();
    });
  }

  setFocusedTile(tile: TTile) {
    let groupForTile = null;
    if (tile !== null) {
      groupForTile = this.getGroupByTile(tile);
    }
    this.groups.forEach(group => {
      if (group === groupForTile) {
        group.body.setFocusedTile(tile);
      } else {
        group.body.setFocusedTile(null);
      }
    });
  }

  getFocusedTile(): TTile {
    let focusedTile = null;
    this.groups.some(group => {
      if (group.body.focusedTile) {
        focusedTile = group.body.focusedTile;
        return true;
      }
      return false;
    });
    return focusedTile;
  }

  getVisibleGridRowCount(): number {
    return this.expandedGroups().reduce((acc, group) => {
      return acc + group.body.logicalGrid.gridRows;
    }, 0);
  }

  getVisibleGridX(tile: TTile): number {
    return tile.gridData.x;
  }

  getVisibleGridY(tile: TTile): number {
    let group = this.getGroupByTile(tile);
    let yCorr = this.getVisibleRowByGroup(group);
    return tile.gridData.y + yCorr;
  }

  getGroupByVisibleRow(rowToFind: number): Group<TileGrid<TTile>> {
    if (rowToFind < 0 || rowToFind >= this.getVisibleGridRowCount()) {
      return null;
    }
    let currentIndex = 0;
    return arrays.find(this.expandedGroups(), group => {
      let rowCount = group.body.logicalGrid.gridRows;
      if (currentIndex <= rowToFind && rowToFind < currentIndex + rowCount) {
        return true;
      }
      currentIndex += rowCount;
    });
  }

  /**
   * @returns the index of the row where the group is located.<p>
   *          Example: There are 3 rows and 2 groups. The first group contains 2 rows, the second 1 row.
   *          The index of the first group is 0, the index of the second group is 2.
   */
  getVisibleRowByGroup(groupToFind: Group<TileGrid<TTile>>): number {
    let currentIndex = 0;
    let found = this.expandedGroups().some(group => {
      let rowCount = group.body.logicalGrid.gridRows;
      if (group === groupToFind) {
        return true;
      }
      currentIndex += rowCount;
      return false;
    });
    if (!found) {
      return -1;
    }
    return currentIndex;
  }

  expandedGroups(): Group<TileGrid<TTile>>[] {
    return this.groups.filter(group => !group.collapsed);
  }

  protected _handleSelectionChanged(tileGrid: TileGrid<TTile>) {
    if (this._selectionUpdateLocked) {
      // Don't execute when deselecting other tiles to minimize the amount of property change events
      return;
    }
    let group = tileGrid.parent as Group;
    if (tileGrid.selectedTiles.length > 0 && group.collapsed) {
      // Do not allow selection in a collapsed group (breaks keyboard navigation and is confusing for the user if invisible tiles are selected)
      tileGrid.deselectAllTiles();
      return;
    }
    if (!this.multiSelect && tileGrid.selectedTiles.length > 0) {
      this._selectionUpdateLocked = true;
      // Ensure only one grid has a selected tile if multiSelect is false
      this.groups.forEach(group => {
        if (group.body !== tileGrid) {
          group.body.deselectAllTiles();
        }
      });
      this._selectionUpdateLocked = false;
    }
  }

  protected _onTileGridPropertyChange(event: PropertyChangeEvent<any, TileGrid<TTile>>) {
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
  }

  protected override _onGroupCollapsedChange(event: PropertyChangeEvent<boolean, Group<TileGrid<TTile>>>) {
    super._onGroupCollapsedChange(event);

    this._handleCollapsed(event.source);
  }

  protected _handleCollapsed(group: Group<TileGrid<TTile>>) {
    if (group.collapsed) {
      // Deselect tiles of a collapsed group (this will also set focusedTile to null) -> actions on invisible elements is confusing, and keystrokes only operate on visible elements, too
      group.body.deselectAllTiles();
    }
    if (group.rendered) {
      group.on('bodyHeightChange', this._groupBodyHeightChangeHandler);
      group.one('bodyHeightChangeDone', this._onGroupBodyHeightChangeDone.bind(this));
    }
  }

  protected _onGroupBodyHeightChange(event: Event<Group<TileGrid<TTile>>>) {
    this.groups.forEach(group => {
      if (event.source === group || group.bodyAnimating) {
        // No need to layout body for the group which is already expanding / collapsing since it does it anyway
        // Btw: another group may be doing it as well at the same time (e.g. because of exclusiveExpand)
        return;
      }
      if (group.body.virtual && group.body.htmlComp) {
        (group.body.htmlComp.layout as TileGridLayout).updateViewPort();
      }
    });
  }

  protected _onGroupBodyHeightChangeDone(event: Event<Group<TileGrid<TTile>>>) {
    event.source.off('bodyHeightChange', this._groupBodyHeightChangeHandler);
  }

  /**
   * @returns the first fully visible tile at the scrollTop.
   */
  tileAtScrollTop(scrollTop: number): TTile {
    return arrays.find(this.getTiles().filter(tile => tile.rendered), tile => {
      return tile.$container.position().top >= scrollTop;
    });
  }
}
