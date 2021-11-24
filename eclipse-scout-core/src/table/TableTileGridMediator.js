/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AggregateTableControl, arrays, objects, scout, Widget} from '../index';
import $ from 'jquery';

/**
 * Delegates events between the Table and it's internal TileGrid.
 *
 */
export default class TableTileGridMediator extends Widget {

  constructor() {
    super();

    this.table = null;
    this.tileAccordion = null;

    this._tileGridListener = null;
    this._tableListener = null;
    this._destroyHandler = null;

    this.tiles = [];
    this.tileMappings = []; // used only in scout classic
    this.tilesMap = {}; // tiles by rowId
    this.groups = {};
    this.groupForTileMap = {}; // groupId by tile
    this.tableState = {}; // always stores the last table state before tileMode activation

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

    this._destroyHandler = this._uninstallListeners.bind(this);

    // properties for internal tileAccordion
    this.exclusiveExpand = false;
    this.gridColumnCount = null;
    this.tileGridLayoutConfig = null;
    this.withPlaceholders = null;

    this._addWidgetProperties(['tileAccordion', 'tiles', 'tileMappings']);
  }

  init(model) {
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

  _installListeners() {
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

    this.tileAccordion.on('destroy', this._destroyHandler);
    this.table.on('destroy', this._destroyHandler);
  }

  _uninstallListeners() {
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

    this.tileAccordion.off('destroy', this._destroyHandler);
    this.table.off('destroy', this._destroyHandler);
  }

  setGridColumnCount(gridColumnCount) {
    this.setProperty('gridColumnCount', gridColumnCount);
    if (this.tileAccordion) {
      this.tileAccordion.setGridColumnCount(gridColumnCount);
    }
  }

  setTileGridLayoutConfig(tileGridLayoutConfig) {
    this.setProperty('tileGridLayoutConfig', tileGridLayoutConfig);
    if (this.tileAccordion) {
      this.tileAccordion.setTileGridLayoutConfig(tileGridLayoutConfig);
    }
  }

  setWithPlaceholders(withPlaceholders) {
    this.setProperty('withPlaceholders', withPlaceholders);
    if (this.tileAccordion) {
      this.tileAccordion.setWithPlaceholders(withPlaceholders);
    }
  }

  _setTileMappings(tableRowTileMappings) {
    this._setProperty('tileMappings', tableRowTileMappings);
    if (!tableRowTileMappings) {
      return;
    }
    let tiles = tableRowTileMappings.map(this.resolveMapping, this);
    this._setTiles(tiles);
  }

  setTiles(tiles) {
    this.setProperty('tiles', tiles);
  }

  _setTiles(tiles) {
    this._isUpdatingTiles = true;
    // remove all new tiles from this.tiles to prevent reused tiles from being destroyed in reset()
    arrays.removeAll(this.tiles, tiles);
    this.reset();
    this._setTilesInternal(tiles);
    this._isUpdatingTiles = false;
  }

  _setTilesInternal(tiles) {
    // check if all tiles are already available in the table
    let tableRowMissing = tiles.some(function(tile) {
      return this.table.rowsMap[tile.rowId] === undefined;
    }, this);

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
    // hierarchy is not supported in tile mode. There is no way to visualize a parent-child hierarchy in the tileGrid. Therefore only top level rows are displayed.
    let rows = this.table.rows.filter(row => {
      return !row.parentRow;
    });
    let tiles = this.table.createTiles(rows);
    if (tiles) {
      this.setTiles(tiles);
    }
  }

  resolveMapping(tableRowTileMapping) {
    let tile = tableRowTileMapping.tile;
    tile.rowId = tableRowTileMapping.tableRow;
    tile.setParent(this);
    tile.setOwner(this);
    return tile;
  }

  // update tilesMap with the given tiles or recreate tilesMap completely in case of null given
  _refreshTilesMap(tiles) {
    if (!tiles) {
      tiles = this.tiles;
      this.tilesMap = {};
    }
    tiles.forEach(function(tile) {
      this.tilesMap[tile.rowId] = tile;
    }, this);
  }

  getTilesForRows(rows) {
    return rows.map(function(row) {
      return this.tilesMap[row.id];
    }, this).filter(t => {
      return !!t;
    });
  }

  _initGroups(tiles) {
    let primaryGroupingColumn = arrays.find(this.table.columns, column => {
      return column.grouped && column.sortIndex === 0;
    });

    tiles.forEach(function(tile) {
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
    }, this);
  }

  _adaptTileGrid(tileGrid) {
    // The table contains the menu items -> pass them to the showContextMenu function of the tileGrid.
    objects.mandatoryFunction(tileGrid, '_showContextMenu');
    let origShowContextMenu = tileGrid._showContextMenu;
    tileGrid._showContextMenu = function(options) {
      objects.mandatoryFunction(this.table, '_filterMenusForContextMenu');
      options.menuItems = this.table._filterMenusForContextMenu();
      scout.assertProperty(this.table, '_filterMenusHandler');
      options.menuFilter = this.table._filterMenusHandler;
      origShowContextMenu.call(tileGrid, options);
    }.bind(this);
    // use the table's keyStrokeContext bindTarget for each tileGrid as well to ensure that the tileGrid's keyStrokes are active when the table is active
    tileGrid.keyStrokeContext.$bindTarget = this.table.keyStrokeContext.$bindTarget;
  }

  _createTileAccordion() {
    return scout.create('TileAccordion', {
      parent: this.table,
      virtual: true,
      selectable: true,
      multiselect: this.table.multiSelect,
      exclusiveExpand: this.exclusiveExpand,
      gridColumnCount: this.gridColumnCount,
      tileGridLayoutConfig: this.tileGridLayoutConfig,
      withPlaceholders: this.withPlaceholders
    });
  }

  _createTileGroup(groupId, primaryGroupingColumn, row) {
    let htmlEnabled, title, iconId;
    if (primaryGroupingColumn) {
      htmlEnabled = primaryGroupingColumn.htmlEnabled;
      let cell = primaryGroupingColumn.createAggrGroupCell(row);
      title = cell.text;
      iconId = cell.iconId;
    }
    return scout.create('Group', {
      parent: this.tileAccordion,
      id: groupId,
      headerVisible: groupId !== 'default',
      title: title,
      titleHtmlEnabled: htmlEnabled,
      iconId: iconId,
      body: {
        objectType: 'TileGrid',
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
    this.table.tableControls.forEach(function(control) {
      if (control instanceof AggregateTableControl) {
        this.tableState.aggregateTableControlSelected = control.selected;
        control.setSelected(false, {
          closeWhenUnselected: true,
          animate: false
        });
        control.setVisible(false);
      }
    }, this);

    this.tableState.loadingSupportContainer = this.table.loadingSupport.options$Container;
    this.table.loadingSupport.options$Container = function() {
      return this.tileAccordion.$container;
    }.bind(this);

    // check if there exists a hierarchy within the tableRows
    let hasHierarchy = arrays.find(this.table.rows, row => {
      return row.parentRow;
    }) !== null;

    if (hasHierarchy) {
      // add the hierarchyFilter since the tileMode doesn't support hierarchy
      this._tableHierarchyFilter = scout.create('TileTableHierarchyFilter', {
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
    }, this);

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
    this.groups = {};
    this.groupForTileMap = {};
    this.tileAccordion.deleteAllTiles();
    this.tileAccordion.deleteAllGroups();

    // destroy tiles manually since owner is the mediator thus the tileGrid can't destroy them
    this.tiles.forEach(tile => {
      tile.destroy();
    });
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
        arrays.find(this.table.tableControls,
          control => control instanceof AggregateTableControl)
          .setSelected(true);
      }

    }
    this.table._refreshMenuBarPosition();
  }

  destroy() {
    // destroy tiles manually since owner is the mediator thus the tileGrid can't destroy them
    this.tiles.forEach(tile => {
      tile.destroy();
    });

    this.tileAccordion.destroy();
    this.tileAccordion = null;
  }

  insertTiles(tiles) {
    tiles = arrays.ensure(tiles);
    if (tiles.length === 0) {
      return;
    }
    this.setTiles(this.tiles.concat(tiles));
  }

  deleteTiles(tiles) {
    if (!tiles) {
      tiles = this.tiles.slice();
    }
    arrays.removeAll(this.tiles, tiles);
    tiles.forEach(function(tile) {
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
    }, this);
    this.tileAccordion.deleteTiles(tiles);
  }

  _onTileAccordionPropertyChange(event) {
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
    }
    if (event.propertyName === 'filteredTiles') {
      this._updateGroupVisibility();
    }
  }

  _onTileAccordionAction(event) {
    if (!this.table.tileMode) {
      return;
    }
    this.table.doRowAction(this.table.rowsMap[event.tile.rowId]);
  }

  _onTileAccordionClick(event) {
    if (!this.table.tileMode) {
      return;
    }
    this.table._triggerRowClick(event, this.table.rowsMap[event.tile.rowId], event.mouseButton);
  }

  _onTableRowsSelected(event) {
    if (!this.table.tileMode) {
      return;
    }
    this._syncSelectionFromTableToTile();
  }

  _onTableRowsInserted(event) {
    if (!this.table.tileMode) {
      return;
    }
    this.insertTiles(this.table.createTiles(event.rows));
  }

  _onTableRowsDeleted(event) {
    if (!this.table.tileMode) {
      return;
    }
    this.deleteTiles(this.getTilesForRows(event.rows));
  }

  _onTableAllRowsDeleted(event) {
    if (!this.table.tileMode) {
      return;
    }
    this.deleteTiles();
  }

  _onTableRowOrderChangedHandler(event) {
    // ignore event when not in tileMode or when this.tilesMap is not (yet) initialized correctly
    if (!this.table.tileMode || $.isEmptyObject(this.tilesMap)) {
      return;
    }
    this.tiles = this.table.rows.map(function(row) {
      return this.tilesMap[row.id];
    }, this);
    this.tileAccordion.setTiles(this.tiles);
  }

  _onTableGroup(event) {
    if (!this.table.tileMode) {
      return;
    }
    this.tileAccordion.deleteAllGroups();
    this._initGroups(this.tiles);
    this.tileAccordion.setTiles(this.tiles);
  }

  _onTableFilterAdded(event) {
    if (!this.table.tileMode) {
      return;
    }
    this._addFilter(event.filter);
  }

  _onTableFilterRemoved(event) {
    if (!this.table.tileMode) {
      return;
    }

    this.tileAccordion.removeFilter(event.filter.tileFilter);
  }

  _addFilter(tableFilter) {
    let tileFilter = {
      table: this.table,
      accept: function(tile) {
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

  _onTableFilter(event) {
    if (!this.table.tileMode) {
      return;
    }
    this.tileAccordion.filter();
  }

  _syncSelectionFromTableToTile() {
    if (this.tileAccordion) {
      this.tileAccordion.selectTiles(this.getTilesForRows(this.table.selectedRows));
    }
  }

  _syncSelectionFromTileGridToTable(selectedTiles) {
    if (!this._isUpdatingTiles) {
      let selectedRows = selectedTiles.map(function(tile) {
        return this.table.rowsMap[tile.rowId];
      }, this).filter(t => {
        return Boolean(t);
      });
      this.table.selectRows(selectedRows);
    }
  }

  _updateGroupVisibility() {
    this.tileAccordion.groups.forEach(group => {
      // Make groups invisible if a tile filter is active and no tiles match (= no tiles are visible)
      let groupEmpty = group.body.filters.length > 0 && group.body.filteredTiles.length === 0;
      group.setVisible(!groupEmpty);
      group.setTitleSuffix(group.body.filteredTiles.length);
    });
  }

  _syncScrollTopFromTableToTile() {
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

    let options = {
      align: 'top'
    };

    if (!tile.rendered) {
      // Execute delayed because table may be not layouted yet
      this.table.session.layoutValidator.schedulePostValidateFunction(tile.reveal.bind(tile, options));
      return;
    }
    tile.reveal(options);
  }

  _syncScrollTopFromTileGridToTable() {
    let tile = this.tileAccordion._tileAtScrollTop(this.tileAccordion.scrollTop);
    if (tile) {
      let options = {
        align: 'top'
      };
      if (!this.table._isDataRendered()) {
        this.table.session.layoutValidator.schedulePostValidateFunction(this.table.scrollTo.bind(this.table, this.table.rowsMap[tile.rowId], options));
      } else {
        this.table.scrollTo(this.table.rowsMap[tile.rowId], options);
      }
    }
  }

  _syncFiltersFromTableToTile() {
    if (this.tileAccordion) {
      this.tileAccordion.setFilters([]);
      this.table.filters.forEach(tableFilter => this._addFilter(tableFilter));
    }
  }

  _renderTileTableHeader() {
    if (this.table.tileTableHeader) {
      this.table.tileTableHeader.render();
    }
  }

  _removeTileTableHeader() {
    if (this.table.tileTableHeader) {
      this.table.tileTableHeader.remove();
    }
  }

  _renderTileAccordion() {
    if (!this.tileAccordion.rendered) {
      this.tileAccordion.render();
    }
  }

  _removeTileAccordion() {
    if (this.tileAccordion.rendered) {
      this.tileAccordion.remove();
    }
  }
}
