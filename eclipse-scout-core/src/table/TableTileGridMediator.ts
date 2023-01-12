/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  AggregateTableControl, arrays, Column, Event, EventHandler, Filter, FilterOrFunction, Group, InitModelOf, ObjectOrChildModel, objects, PropertyChangeEvent, scout, ScrollToOptions, Table, TableAllRowsDeletedEvent, TableFilterAddedEvent,
  TableFilterRemovedEvent, TableGroupEvent, TableRow, TableRowOrderChangedEvent, TableRowsDeletedEvent, TableRowsInsertedEvent, TableRowsSelectedEvent, TableRowTileMapping, TableTileGridMediatorEventMap, TableTileGridMediatorModel, Tile,
  TileAccordion, TileActionEvent, TileClickEvent, TileGrid, TileGridLayoutConfig, TileTableHierarchyFilter, Widget
} from '../index';
import $ from 'jquery';

/**
 * Delegates events between the {@link Table} and it's internal {@link TileGrid}.
 */
export class TableTileGridMediator extends Widget implements TableTileGridMediatorModel {
  declare model: TableTileGridMediatorModel;
  declare eventMap: TableTileGridMediatorEventMap;
  declare self: TableTileGridMediator;
  declare parent: Table;

  table: Table;
  tileAccordion: TileAccordion;
  tiles: Tile[];
  tileMappings: TableRowTileMapping[];

  /** tiles by rowId */
  tilesMap: Record<string, Tile>;

  /** groupId by tile */
  groupForTileMap: Record<string, string>;

  /** always stores the last table state before tileMode activation */
  tableState: Record<string, any>;

  /** properties for internal tileAccordion */
  exclusiveExpand: boolean;

  gridColumnCount: number;
  tileGridLayoutConfig: TileGridLayoutConfig;
  withPlaceholders: boolean;

  protected _isUpdatingTiles: boolean;
  protected _tableHierarchyFilter: TileTableHierarchyFilter;
  protected _destroyHandler: () => void;
  protected _tileAccordionPropertyChangeHandler: EventHandler<PropertyChangeEvent<any, TileAccordion>>;
  protected _tileAccordionActionHandler: EventHandler<TileActionEvent<TileAccordion>>;
  protected _tileAccordionClickHandler: EventHandler<TileClickEvent<TileAccordion>>;
  protected _tableFilterAddedHandler: EventHandler<TableFilterAddedEvent>;
  protected _tableFilterRemovedHandler: EventHandler<TableFilterRemovedEvent>;
  protected _tableFilterHandler: EventHandler<Event<Table>>;
  protected _tableGroupHandler: EventHandler<TableGroupEvent>;
  protected _tableRowsSelectedHandler: EventHandler<TableRowsSelectedEvent>;
  protected _tableRowsInsertedHandler: EventHandler<TableRowsInsertedEvent>;
  protected _tableRowsDeletedHandler: EventHandler<TableRowsDeletedEvent>;
  protected _tableAllRowsDeletedHandler: EventHandler<TableAllRowsDeletedEvent>;
  protected _tableRowOrderChangedHandler: EventHandler<TableRowOrderChangedEvent>;
  protected _tablePropertyChangeHandler: EventHandler<PropertyChangeEvent>;

  constructor() {
    super();

    this.table = null;
    this.tileAccordion = null;
    this.tiles = [];
    this.tileMappings = [];
    this.tilesMap = {};
    this.groupForTileMap = {};
    this.tableState = {};
    this._tileAccordionPropertyChangeHandler = this._onTileAccordionPropertyChange.bind(this);
    this._tileAccordionActionHandler = this._onTileAccordionAction.bind(this);
    this._tileAccordionClickHandler = this._onTileAccordionClick.bind(this);
    this._tableFilterAddedHandler = this._onTableFilterAdded.bind(this);
    this._tableFilterRemovedHandler = this._onTableFilterRemoved.bind(this);
    this._tableFilterHandler = this._onTableFilter.bind(this);
    this._tableGroupHandler = this._onTableGroup.bind(this);
    this._tableRowsSelectedHandler = this._onTableRowsSelected.bind(this);
    this._tableRowsInsertedHandler = this._onTableRowsInserted.bind(this);
    this._tableRowsDeletedHandler = this._onTableRowsDeleted.bind(this);
    this._tableAllRowsDeletedHandler = this._onTableAllRowsDeleted.bind(this);
    this._tableRowOrderChangedHandler = this._onTableRowOrderChangedHandler.bind(this);
    this._tablePropertyChangeHandler = this._onTablePropertyChange.bind(this);
    this._destroyHandler = this._uninstallListeners.bind(this);
    this.exclusiveExpand = false;
    this.gridColumnCount = null;
    this.tileGridLayoutConfig = null;
    this.withPlaceholders = null;

    this._addWidgetProperties(['tileAccordion', 'tiles', 'tileMappings']);
  }

  override init(model: InitModelOf<this>) {
    super._init(model);

    this.table = this.parent;

    if (!this.tileAccordion) {
      this.tileAccordion = this._createTileAccordion();
      this._installListeners();
    }
    this.tableState.headerVisible = this.table.headerVisible;

    this._setTiles(this.tiles);
    this._setTileMappings(this.tileMappings);
  }

  protected _installListeners() {
    this.tileAccordion.on('propertyChange', this._tileAccordionPropertyChangeHandler);
    this.tileAccordion.on('tileAction', this._tileAccordionActionHandler);
    this.tileAccordion.on('tileClick', this._tileAccordionClickHandler);
    this.table.on('filterAdded', this._tableFilterAddedHandler);
    this.table.on('filterRemoved', this._tableFilterRemovedHandler);
    this.table.on('filter', this._tableFilterHandler);
    this.table.on('group', this._tableGroupHandler);
    this.table.on('rowsSelected', this._tableRowsSelectedHandler);
    this.table.on('rowsInserted', this._tableRowsInsertedHandler);
    this.table.on('rowsDeleted', this._tableRowsDeletedHandler);
    this.table.on('allRowsDeleted', this._tableAllRowsDeletedHandler);
    this.table.on('rowOrderChanged', this._tableRowOrderChangedHandler);
    this.table.on('propertyChange', this._tablePropertyChangeHandler);

    this.tileAccordion.on('destroy', this._destroyHandler);
    this.table.on('destroy', this._destroyHandler);
  }

  protected _uninstallListeners() {
    this.tileAccordion.off('propertyChange', this._tileAccordionPropertyChangeHandler);
    this.tileAccordion.off('tileAction', this._tileAccordionActionHandler);
    this.tileAccordion.off('tileClick', this._tileAccordionClickHandler);
    this.table.off('filterAdded', this._tableFilterAddedHandler);
    this.table.off('filterRemoved', this._tableFilterRemovedHandler);
    this.table.off('filter', this._tableFilterHandler);
    this.table.off('group', this._tableGroupHandler);
    this.table.off('rowsSelected', this._tableRowsSelectedHandler);
    this.table.off('rowsInserted', this._tableRowsInsertedHandler);
    this.table.off('rowsDeleted', this._tableRowsDeletedHandler);
    this.table.off('allRowsDeleted', this._tableAllRowsDeletedHandler);
    this.table.off('rowOrderChanged', this._tableRowOrderChangedHandler);
    this.table.off('propertyChange', this._tablePropertyChangeHandler);

    this.tileAccordion.off('destroy', this._destroyHandler);
    this.table.off('destroy', this._destroyHandler);
  }

  setGridColumnCount(gridColumnCount: number) {
    this.setProperty('gridColumnCount', gridColumnCount);
    if (this.tileAccordion) {
      this.tileAccordion.setGridColumnCount(gridColumnCount);
    }
  }

  setTileGridLayoutConfig(tileGridLayoutConfig: TileGridLayoutConfig) {
    this.setProperty('tileGridLayoutConfig', tileGridLayoutConfig);
    if (this.tileAccordion) {
      this.tileAccordion.setTileGridLayoutConfig(tileGridLayoutConfig);
    }
  }

  setWithPlaceholders(withPlaceholders: boolean) {
    this.setProperty('withPlaceholders', withPlaceholders);
    if (this.tileAccordion) {
      this.tileAccordion.setWithPlaceholders(withPlaceholders);
    }
  }

  protected _setTileMappings(tableRowTileMappings: TableRowTileMapping[]) {
    this._setProperty('tileMappings', tableRowTileMappings);
    if (!tableRowTileMappings) {
      return;
    }
    let tiles = tableRowTileMappings.map(this.resolveMapping, this);
    this._setTiles(tiles);
  }

  setTiles(tiles: ObjectOrChildModel<Tile>[]) {
    this.setProperty('tiles', tiles);
  }

  protected _setTiles(tiles: Tile[]) {
    this._isUpdatingTiles = true;
    // remove all new tiles from this.tiles to prevent reused tiles from being destroyed in reset()
    arrays.removeAll(this.tiles, tiles);
    this.reset();
    this._setTilesInternal(tiles);
    this._isUpdatingTiles = false;
  }

  protected _setTilesInternal(tiles: Tile[]) {
    // check if all tiles are already available in the table
    let tableRowMissing = tiles.some(tile => this.table.rowsMap[tile.rowId] === undefined);
    if (tableRowMissing) {
      if (this.table.initialized) {
        // wait for next insertRows event on the table to execute this function again
        this.table.one('rowsInserted', this._setTilesInternal.bind(this, tiles));
      } else {
        // if table is not initialized already wait for the init event
        this.table.one('init', this._setTilesInternal.bind(this, tiles));
      }
      return;
    }

    this._refreshTilesMap(tiles);

    // create simplified grouping for tile accordion, grouping on the table can be left as is.
    this._initGroups(tiles);

    this._setProperty('tiles', tiles);

    this.tileAccordion.setTiles(this.tiles);
    this._updateGroupVisibility();

    this._syncSelectionFromTableToTile();
  }

  // only used in ScoutJS, see TableAdapter.modifyTablePrototype()
  loadTiles() {
    // hierarchy is not supported in tile mode. There is no way to visualize a parent-child hierarchy in the tileGrid. Therefore, only top level rows are displayed.
    let rows = this.table.rows.filter(row => !row.parentRow);
    let tiles = this.table.createTiles(rows);
    if (tiles) {
      this.setTiles(tiles);
    }
  }

  resolveMapping(tableRowTileMapping: TableRowTileMapping): Tile {
    let tile = tableRowTileMapping.tile;
    tile.rowId = tableRowTileMapping.tableRow;
    tile.setParent(this);
    tile.setOwner(this);
    return tile;
  }

  // update tilesMap with the given tiles or recreate tilesMap completely in case of null given
  protected _refreshTilesMap(tiles: Tile[]) {
    if (!tiles) {
      tiles = this.tiles;
      this.tilesMap = {};
    }
    tiles.forEach(tile => {
      this.tilesMap[tile.rowId] = tile;
    });
  }

  getTilesForRows(rows: TableRow[]): Tile[] {
    return rows
      .map(row => this.tilesMap[row.id])
      .filter(t => !!t);
  }

  protected _initGroups(tiles: Tile[]) {
    let primaryGroupingColumn = arrays.find(this.table.columns, column => column.grouped && column.sortIndex === 0);

    tiles.forEach(tile => {
      let row = this.table.rowsMap[tile.rowId];
      let groupId = primaryGroupingColumn ? primaryGroupingColumn.cellTextForGrouping(row) : 'default';
      groupId = scout.nvl(groupId, ''); // use empty group to avoid NPE
      this.groupForTileMap[row.id] = groupId;
      // check if group already exists, otherwise create it
      let group = this.tileAccordion.getGroupById(groupId);
      if (!group) {
        group = this._createTileGroup(groupId, primaryGroupingColumn, row);
        this._adaptTileGrid(group.body);
        this.tileAccordion.insertGroup(group);
      }
      tile.parent = group;
    });
  }

  protected _adaptTileGrid(tileGrid: TileGrid) {
    // The table contains the menu items -> pass them to the showContextMenu function of the tileGrid.
    objects.mandatoryFunction(tileGrid, '_showContextMenu');
    let origShowContextMenu = tileGrid._showContextMenu;
    tileGrid._showContextMenu = options => {
      objects.mandatoryFunction(this.table, '_filterMenusForContextMenu');
      options.menuItems = this.table._filterMenusForContextMenu();
      scout.assertProperty(this.table, '_filterMenusHandler');
      options.menuFilter = this.table._filterMenusHandler;
      origShowContextMenu.call(tileGrid, options);
    };
    // use the table's keyStrokeContext bindTarget for each tileGrid as well to ensure that the tileGrid's keyStrokes are active when the table is active
    tileGrid.keyStrokeContext.$bindTarget = this.table.keyStrokeContext.$bindTarget;
  }

  protected _createTileAccordion(): TileAccordion {
    return scout.create(TileAccordion, {
      parent: this.table,
      virtual: true,
      selectable: true,
      multiSelect: this.table.multiSelect,
      exclusiveExpand: this.exclusiveExpand,
      gridColumnCount: this.gridColumnCount,
      tileGridLayoutConfig: this.tileGridLayoutConfig,
      withPlaceholders: this.withPlaceholders
    });
  }

  protected _createTileGroup(groupId: string, primaryGroupingColumn: Column<any>, row: TableRow): Group<TileGrid> {
    let htmlEnabled: boolean, title: string, iconId: string;
    if (primaryGroupingColumn) {
      htmlEnabled = primaryGroupingColumn.htmlEnabled;
      let cell = primaryGroupingColumn.createAggrGroupCell(row);
      title = cell.text;
      iconId = cell.iconId;
    }
    return scout.create((Group<TileGrid>), {
      parent: this.tileAccordion,
      id: groupId,
      headerVisible: groupId !== 'default',
      title: title,
      titleHtmlEnabled: htmlEnabled,
      iconId: iconId,
      body: {
        objectType: TileGrid,
        scrollable: false
      }
    });
  }

  activate() {
    this.tableState.headerVisible = this.table.headerVisible;
    this.table.setHeaderVisible(false);
    if (this.table.tileTableHeader) {
      this.table.tileTableHeader.setVisible(true);
    }

    // hide aggregation table control
    this.table.tableControls.forEach(control => {
      if (control instanceof AggregateTableControl) {
        this.tableState.aggregateTableControlSelected = control.selected;
        control.setSelected(false, {closeWhenUnselected: true, animate: false});
        control.setVisible(false);
      }
    });

    this.tableState.loadingSupportContainer = this.table.loadingSupport.options$Container;
    this.table.loadingSupport.options$Container = function() {
      return this.tileAccordion.$container;
    }.bind(this);

    // check if there exists a hierarchy within the tableRows
    let hasHierarchy = arrays.find(this.table.rows, row => !!row.parentRow) !== null;
    if (hasHierarchy) {
      // add the hierarchyFilter since the tileMode doesn't support hierarchy
      this._tableHierarchyFilter = scout.create(TileTableHierarchyFilter, {
        table: this.table
      });
      this.table.addFilter(this._tableHierarchyFilter);
    }

    // doesn't depend upon any tile data, therefore execute on activation
    this._syncFiltersFromTableToTile();

    this._syncScrollTopFromTableToTile();
  }

  deactivate() {
    // show aggregation table control
    this.table.tableControls.forEach(control => {
      if (control instanceof AggregateTableControl) {
        control.setVisible(true);
      }
    });

    // use _setProperty to avoid instant rendering, render manually later on (this is necessary since TableHeader depends upon table.$data)
    this.table._setProperty('headerVisible', this.tableState.headerVisible);
    if (this.table.tileTableHeader) {
      this.table.tileTableHeader.setVisible(false);
    }

    if (this.tableState.loadingSupportContainer) {
      this.table.loadingSupport.options$Container = this.tableState.loadingSupportContainer;
    }

    if (this._tableHierarchyFilter) {
      this.table.removeFilter(this._tableHierarchyFilter);
      this._tableHierarchyFilter = null;
    }

    this._syncScrollTopFromTileGridToTable();

    // complete reset
    this.reset();
  }

  reset() {
    this.tilesMap = {};
    this.groupForTileMap = {};
    this.tileAccordion.deleteAllTiles();
    this.tileAccordion.deleteAllGroups();

    // destroy tiles manually since owner is the mediator thus the tileGrid can't destroy them
    this.tiles.forEach(tile => tile.destroy());
  }

  renderTileMode() {
    if (this.table.tileMode) {
      // if the table was previously in tileMode this is not necessary...
      if (this.table.$data) {
        this.table._removeData();
      }
      this._renderTileTableHeader();
      this._renderTileAccordion();
    } else {
      this._removeTileTableHeader();
      this._removeTileAccordion();
      this.table._renderData();
      this.table._renderTableHeader();

      // restore selected state of the aggregationTableControl here since it depends on table.$data
      if (this.tableState.aggregateTableControlSelected) {
        arrays.find(this.table.tableControls, control => control instanceof AggregateTableControl).setSelected(true);
      }
    }
    this.table._refreshMenuBarPosition();
  }

  override destroy() {
    // destroy tiles manually since owner is the mediator thus the tileGrid can't destroy them
    this.tiles.forEach(tile => tile.destroy());
    this.tileAccordion.destroy();
    this.tileAccordion = null;
  }

  insertTiles(tiles: Tile[] | Tile) {
    tiles = arrays.ensure(tiles);
    if (tiles.length === 0) {
      return;
    }
    this.setTiles(this.tiles.concat(tiles));
  }

  deleteTiles(tiles?: Tile[]) {
    if (!tiles) {
      tiles = this.tiles.slice();
    }
    arrays.removeAll(this.tiles, tiles);
    tiles.forEach(tile => {
      delete this.tilesMap[tile.rowId];
      delete this.groupForTileMap[tile.rowId];
      let group = this.tileAccordion.getGroupByTile(tile);
      if (group) {
        // if there's only one left remove the group (tile is removed later)
        if (group.body.tiles.length === 1) {
          this.tileAccordion.deleteGroup(group);
        }
      }
      tile.destroy();
    });
    this.tileAccordion.deleteTiles(tiles);
  }

  protected _onTileAccordionPropertyChange(event: PropertyChangeEvent<any, TileAccordion>) {
    if (!this.table.tileMode) {
      return;
    }
    if (event.propertyName === 'selectedTiles') {
      this._syncSelectionFromTileGridToTable(event.source.getSelectedTiles());
      if (this.tileAccordion.rendered) {
        // Depending on the tiles content, selecting tiles with shift can lead to a mix of selecting the tiles content
        // and the tiles itself, which doesn't look nice. Remove the text selection when selection tiles to avoid this.
        this.tileAccordion.$container.document(true).getSelection().removeAllRanges();
      }
    } else if (event.propertyName === 'filteredTiles') {
      this._updateGroupVisibility();
    }
  }

  protected _onTileAccordionAction(event: TileActionEvent<TileAccordion>) {
    if (!this.table.tileMode) {
      return;
    }
    this.table.doRowAction(this.table.rowsMap[event.tile.rowId]);
  }

  protected _onTileAccordionClick(event: TileClickEvent<TileAccordion>) {
    if (!this.table.tileMode) {
      return;
    }
    this.table._triggerRowClick(event.originalEvent, this.table.rowsMap[event.tile.rowId], event.mouseButton);
  }

  protected _onTableRowsSelected(event: TableRowsSelectedEvent) {
    if (!this.table.tileMode) {
      return;
    }
    this._syncSelectionFromTableToTile();
  }

  protected _onTableRowsInserted(event: TableRowsInsertedEvent) {
    if (!this.table.tileMode) {
      return;
    }
    this.insertTiles(this.table.createTiles(event.rows));
  }

  protected _onTableRowsDeleted(event: TableRowsDeletedEvent) {
    if (!this.table.tileMode) {
      return;
    }
    this.deleteTiles(this.getTilesForRows(event.rows));
  }

  protected _onTableAllRowsDeleted(event: TableAllRowsDeletedEvent) {
    if (!this.table.tileMode) {
      return;
    }
    this.deleteTiles();
  }

  protected _onTableRowOrderChangedHandler(event: TableRowOrderChangedEvent) {
    // ignore event when not in tileMode or when this.tilesMap is not (yet) initialized correctly
    if (!this.table.tileMode || $.isEmptyObject(this.tilesMap)) {
      return;
    }
    this.tiles = this.table.rows.map(row => this.tilesMap[row.id]);
    this.tileAccordion.setTiles(this.tiles);
  }

  /** @internal */
  _onTableGroup(event?: TableGroupEvent) {
    if (!this.table.tileMode) {
      return;
    }
    this.tileAccordion.deleteAllGroups();
    this._initGroups(this.tiles);
    this.tileAccordion.setTiles(this.tiles);
  }

  protected _onTableFilterAdded(event: TableFilterAddedEvent) {
    if (!this.table.tileMode) {
      return;
    }
    this._addFilter(event.filter);
  }

  protected _onTableFilterRemoved(event: TableFilterRemovedEvent & { filter: TableFilterWithTileFilter }) {
    if (!this.table.tileMode) {
      return;
    }
    this.tileAccordion.removeFilter(event.filter.tileFilter);
  }

  protected _addFilter(tableFilter: TableFilterWithTileFilter) {
    let tileFilter = {
      table: this.table,
      accept: (tile: Tile) => {
        let rowForTile = this.table.rowsMap[tile.rowId];
        if (rowForTile) {
          return tableFilter.accept(rowForTile);
        }
        return false;
      }
    };
    if (tableFilter.tileFilter) {
      this.tileAccordion.removeFilter(tableFilter.tileFilter, false);
    }
    tableFilter.tileFilter = tileFilter;
    this.tileAccordion.addFilter(tileFilter);
  }

  protected _onTableFilter(event: Event<Table>) {
    if (!this.table.tileMode) {
      return;
    }
    this.tileAccordion.filter();
  }

  protected _syncSelectionFromTableToTile() {
    if (this.tileAccordion) {
      this.tileAccordion.selectTiles(this.getTilesForRows(this.table.selectedRows));
    }
  }

  protected _syncSelectionFromTileGridToTable(selectedTiles: Tile[]) {
    if (!this._isUpdatingTiles) {
      let selectedRows = selectedTiles
        .map(tile => this.table.rowsMap[tile.rowId])
        .filter(t => Boolean(t));
      this.table.selectRows(selectedRows);
    }
  }

  protected _updateGroupVisibility() {
    this.tileAccordion.groups.forEach(group => {
      // Make groups invisible if a tile filter is active and no tiles match (= no tiles are visible)
      let body = group.body;
      let groupEmpty = body.filters.length > 0 && body.filteredTiles.length === 0;
      group.setVisible(!groupEmpty);
      group.setTitleSuffix(body.filteredTiles.length + '');
    });
  }

  protected _syncScrollTopFromTableToTile() {
    let rowIndex = this.table._rowIndexAtScrollTop(this.table.scrollTop);
    if (rowIndex <= 0) {
      return;
    }
    let tile = this.tilesMap[this.table.rows[rowIndex].id];
    if (!tile) {
      return;
    }

    // reset scrollTop on tileAccordion, otherwise it would overwrite the synced scrollTop
    this.tileAccordion.scrollTop = null;

    let options: ScrollToOptions = {
      align: 'top'
    };

    if (!tile.rendered) {
      // Execute delayed because table may be not layouted yet
      this.table.session.layoutValidator.schedulePostValidateFunction(tile.reveal.bind(tile, options));
      return;
    }
    tile.reveal(options);
  }

  protected _syncScrollTopFromTileGridToTable() {
    let tile = this.tileAccordion.tileAtScrollTop(this.tileAccordion.scrollTop);
    if (tile) {
      let options: ScrollToOptions = {
        align: 'top'
      };
      if (!this.table._isDataRendered()) {
        this.table.session.layoutValidator.schedulePostValidateFunction(this.table.scrollTo.bind(this.table, this.table.rowsMap[tile.rowId], options));
      } else {
        this.table.scrollTo(this.table.rowsMap[tile.rowId], options);
      }
    }
  }

  protected _syncFiltersFromTableToTile() {
    if (this.tileAccordion) {
      this.tileAccordion.setFilters([]);
      this.table.filters.forEach(tableFilter => this._addFilter(tableFilter));
    }
  }

  protected _onTablePropertyChange(event: PropertyChangeEvent) {
    if (event.propertyName === 'multiSelect') {
      this.tileAccordion.setMultiSelect(event.newValue as boolean);
    }
  }

  protected _renderTileTableHeader() {
    if (this.table.tileTableHeader) {
      this.table.tileTableHeader.render();
    }
  }

  protected _removeTileTableHeader() {
    if (this.table.tileTableHeader) {
      this.table.tileTableHeader.remove();
    }
  }

  protected _renderTileAccordion() {
    if (!this.tileAccordion.rendered) {
      this.tileAccordion.render();
    }
  }

  protected _removeTileAccordion() {
    if (this.tileAccordion.rendered) {
      this.tileAccordion.remove();
    }
  }
}

export type TableFilterWithTileFilter = Filter<TableRow> & { tileFilter?: FilterOrFunction<Tile> | FilterOrFunction<Tile>[] };
