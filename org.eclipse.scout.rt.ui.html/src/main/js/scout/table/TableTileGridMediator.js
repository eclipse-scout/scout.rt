/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

/**
 * Delegates events between the Table and it's internal TileGrid.
 *
 */
scout.TableTileGridMediator = function() {
  scout.TableTileGridMediator.parent.call(this);

  this.table = null;
  this.tileAccordion = null;

  this._tileGridListener = null;
  this._tableListener = null;
  this._destroyHandler = null;

  this.tiles = [];
  this.tileMappings = []; // used only in scout classic
  this.tilesMap = {}; // tiles by rowId
  this.tileFilterMap = {};
  this.groups = {};
  this.groupForTileMap = {}; // groupId by tile
  this.tableState = {}; // always stores the last table state before tileMode activation

  this._tileAccordionPropertyChangeHandler = this._onTileAccordionPropertyChange.bind(this);
  this._tileAccordionActionHandler = this._onTileAccordionAction.bind(this);
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
};
scout.inherits(scout.TableTileGridMediator, scout.Widget);

scout.TableTileGridMediator.prototype.init = function(model) {
  scout.TableTileGridMediator.parent.prototype._init.call(this, model);

  this.table = this.parent;

  if (!this.tileAccordion) {
    this.tileAccordion = this._createTileAccordion();
    this._installListeners();
  }
  this.tableState.headerVisible = this.table.headerVisible;

  this._setTiles(this.tiles);
  this._setTileMappings(this.tileMappings);
};

scout.TableTileGridMediator.prototype._installListeners = function() {
  this.tileAccordion.on('propertyChange', this._tileAccordionPropertyChangeHandler);
  this.tileAccordion.on('tileAction', this._tileAccordionActionHandler);
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
};

scout.TableTileGridMediator.prototype._uninstallListeners = function() {
  this.tileAccordion.off('propertyChange', this._tileAccordionPropertyChangeHandler);
  this.tileAccordion.off('tileAction', this._tileAccordionActionHandler);
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
};

scout.TableTileGridMediator.prototype.setGridColumnCount = function(gridColumnCount) {
  this.setProperty('gridColumnCount', gridColumnCount);
  if (this.tileAccordion) {
    this.tileAccordion.setGridColumnCount(gridColumnCount);
  }
};

scout.TableTileGridMediator.prototype.setTileGridLayoutConfig = function(tileGridLayoutConfig) {
  this.setProperty('tileGridLayoutConfig', tileGridLayoutConfig);
  if (this.tileAccordion) {
    this.tileAccordion.setTileGridLayoutConfig(tileGridLayoutConfig);
  }
};

scout.TableTileGridMediator.prototype.setWithPlaceholders = function(withPlaceholders) {
  this.setProperty('withPlaceholders', withPlaceholders);
  if (this.tileAccordion) {
    this.tileAccordion.setWithPlaceholders(withPlaceholders);
  }
};

scout.TableTileGridMediator.prototype._setTileMappings = function(tableRowTileMappings) {
  if (!tableRowTileMappings) {
    return;
  }
  var tiles = tableRowTileMappings.map(this.resolveMapping, this);
  this._setTiles(tiles);
};

scout.TableTileGridMediator.prototype.setTiles = function(tiles) {
  this.setProperty('tiles', tiles);
};

scout.TableTileGridMediator.prototype._setTiles = function(tiles) {
  this._isUpdatingTiles = true;
  this.reset();
  this._setTilesInternal(tiles);
  this._isUpdatingTiles = false;
};

scout.TableTileGridMediator.prototype._setTilesInternal = function(tiles) {
  // check if all tiles are already available in the table
  var tableRowMissing = tiles.some(function(tile) {
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
};

// only used in ScoutJS, see TableAdapter.modifyTablePrototype()
scout.TableTileGridMediator.prototype.loadTiles = function() {
  var tiles = this.table.createTiles(this.table.rows);
  if (tiles) {
    this.setTiles(tiles);
  }
};

scout.TableTileGridMediator.prototype.resolveMapping = function(tableRowTileMapping) {
  var tile = tableRowTileMapping.tile;
  tile.rowId = tableRowTileMapping.tableRow;
  tile.setParent(this);
  tile.setOwner(this);
  return tile;
};

//update tilesMap with the given tiles or recreate tilesMap completely in case of null given
scout.TableTileGridMediator.prototype._refreshTilesMap = function(tiles) {
  if (!tiles) {
    tiles = this.tiles;
    this.tilesMap = {};
  }
  tiles.forEach(function(tile) {
    this.tilesMap[tile.rowId] = tile;
  }, this);
};

scout.TableTileGridMediator.prototype.getTilesForRows = function(rows) {
  return rows.map(function(row) {
    return this.tilesMap[row.id];
  }, this).filter(function(t) {
    return !!t;
  });
};

scout.TableTileGridMediator.prototype._initGroups = function(tiles) {
  var primaryGroupingColumn = scout.arrays.find(this.table.columns, function(column) {
    return column.grouped && column.sortIndex === 0;
  });

  tiles.forEach(function(tile) {
    var row = this.table.rowsMap[tile.rowId];
    var groupId = primaryGroupingColumn ? primaryGroupingColumn.cellTextForGrouping(row) : 'default';
    var htmlEnabled = primaryGroupingColumn ? primaryGroupingColumn.htmlEnabled : false;
    this.groupForTileMap[row.id] = groupId;
    // check if group already exists, otherwise create it
    var group = this.tileAccordion.getGroupById(groupId);
    if (!group) {
      group = this._createTileGroup(groupId, htmlEnabled);
      this._adaptTileGrid(group.body);
      this.tileAccordion.insertGroup(group);
    }
    tile.parent = group;
  }, this);
};

scout.TableTileGridMediator.prototype._adaptTileGrid = function(tileGrid) {
  // we want to use the table's context menu, redirect request to show the context menu. The selection is already synchronized.
  tileGrid.showContextMenu = function(options) {
    this.session.onRequestsDone(this.table._showContextMenu.bind(this.table, options));
  }.bind(this);
};

scout.TableTileGridMediator.prototype._createTileAccordion = function() {
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
};

scout.TableTileGridMediator.prototype._createTileGroup = function(groupId, htmlEnabled) {
  return new scout.create('Group', {
    parent: this.tileAccordion,
    id: groupId,
    headerVisible: groupId === 'default' ? false : true,
    title: groupId,
    titleHtmlEnabled: htmlEnabled,
    body: {
      objectType: 'TileGrid',
      scrollable: false
    }
  });
};

scout.TableTileGridMediator.prototype.activate = function() {
  this.tableState.headerVisible = this.table.headerVisible;
  this.table.setHeaderVisible(false);
  if (this.table.tileTableHeader) {
    this.table.tileTableHeader.setVisible(true);
  }

  // hide aggregation table control
  this.table.tableControls.filter(function(control) {
    if (control instanceof scout.AggregateTableControl) {
      control.setVisible(false);
    }
  });

  this.tableState.loadingSupportContainer = this.table.loadingSupport.options$Container;
  this.table.loadingSupport.options$Container = function() {
    return this.tileAccordion.$container;
  }.bind(this);

  // doesn't depend upon any tile data, therefore execute on activation
  this._syncFiltersFromTableToTile();
  this._syncScrollTopFromTableToTile();
};

scout.TableTileGridMediator.prototype.deactivate = function() {
  // show aggregation table control
  this.table.tableControls.filter(function(control) {
    if (control instanceof scout.AggregateTableControl) {
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

  this._syncScrollTopFromTileGridToTable();

  // complete reset
  this.reset();
};

scout.TableTileGridMediator.prototype.reset = function() {
  this.tilesMap = {};
  this.groups = {};
  this.groupForTileMap = {};
  this.tileAccordion.deleteAllTiles();
  this.tileAccordion.deleteAllGroups();
};

scout.TableTileGridMediator.prototype.renderTileMode = function() {
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
  }
  this.table._refreshMenuBarPosition();
};

scout.TableTileGridMediator.prototype.destroy = function() {
  // destroy tiles manually since owner is this.table thus the tileGrid can't destroy them
  this.tiles.forEach(function(tile) {
    tile.destroy();
  });

  this.tileAccordion.destroy();
  this.tileAccordion = null;
};

scout.TableTileGridMediator.prototype.insertTiles = function(tiles) {
  tiles = scout.arrays.ensure(tiles);
  if (tiles.length === 0) {
    return;
  }
  this.setTiles(this.tiles.concat(tiles));
};

scout.TableTileGridMediator.prototype.deleteTiles = function(tiles) {
  if (!tiles) {
    tiles = this.tiles.slice();
  }
  scout.arrays.removeAll(this.tiles, tiles);
  tiles.forEach(function(tile) {
    delete this.tilesMap[tile.rowId];
    delete this.groupForTileMap[tile.rowId];
    var group = this.tileAccordion.getGroupByTile(tile);
    if (group) {
      // if there's only one left remove the group (tile is removed later)
      if (group.body.tiles.length === 1) {
        this.tileAccordion.deleteGroup(group);
      }
    }
    tile.destroy();
  }, this);
  this.tileAccordion.deleteTiles(tiles);
};

scout.TableTileGridMediator.prototype._onTileAccordionPropertyChange = function(event) {
  if (!this.table.tileMode) {
    return;
  }
  if (event.propertyName === 'selectedTiles') {
    this.syncSelectionFromTileGridToTable(event.source.getSelectedTiles());
  }
  if (event.propertyName === 'filteredTiles') {
    this._updateGroupVisibility();
  }
};

scout.TableTileGridMediator.prototype._onTileAccordionAction = function(event) {
  if (!this.table.tileMode) {
    return;
  }
  this.table.doRowAction(this.table.rowsMap[event.tile.rowId]);
};

scout.TableTileGridMediator.prototype._onTableRowsSelected = function(event) {
  if (!this.table.tileMode) {
    return;
  }
  this._syncSelectionFromTableToTile();
};

scout.TableTileGridMediator.prototype._onTableRowsInserted = function(event) {
  if (!this.table.tileMode) {
    return;
  }
  this.insertTiles(this.table.createTiles(event.rows));
};

scout.TableTileGridMediator.prototype._onTableRowsDeleted = function(event) {
  if (!this.table.tileMode) {
    return;
  }
  this.deleteTiles(this.getTilesForRows(event.rows));
};

scout.TableTileGridMediator.prototype._onTableAllRowsDeleted = function(event) {
  if (!this.table.tileMode) {
    return;
  }
  this.deleteTiles();
};

scout.TableTileGridMediator.prototype._onTableRowOrderChangedHandler = function(event) {
  // ignore event when not in tileMode or when this.tilesMap is not (yet) initialized correctly
  if (!this.table.tileMode || $.isEmptyObject(this.tilesMap)) {
    return;
  }
  this.tiles = this.table.rows.map(function(row) {
    return this.tilesMap[row.id];
  }, this);
  this.tileAccordion.setTiles(this.tiles);
};

scout.TableTileGridMediator.prototype._onTableGroup = function(event) {
  if (!this.table.tileMode) {
    return;
  }
  this.tileAccordion.deleteAllGroups();
  this._initGroups(this.tiles);
  this.tileAccordion.setTiles(this.tiles);
};

scout.TableTileGridMediator.prototype._onTableFilterAdded = function(event) {
  if (!this.table.tileMode) {
    return;
  }
  this._addFilter(event.filter);
};

scout.TableTileGridMediator.prototype._onTableFilterRemoved = function(event) {
  if (!this.table.tileMode) {
    return;
  }

  this.tileAccordion.removeTileFilter(this.tileFilterMap[event.filter.createKey()]);
  this.tileAccordion.filterTiles();
};

scout.TableTileGridMediator.prototype._addFilter = function(tableFilter) {
  var tileFilter = {
    table: this.table,
    accept: function(tile) {
      var rowForTile = this.table.rowsMap[tile.rowId];
      if (rowForTile) {
        return tableFilter.accept(rowForTile);
      } else {
        return false;
      }
    }
  };
  var key = tableFilter.createKey();
  if (this.tileFilterMap[key]) {
    this.tileAccordion.removeTileFilter(this.tileFilterMap[key]);
  }
  this.tileFilterMap[key] = tileFilter;
  this.tileAccordion.addTileFilter(tileFilter);
};

scout.TableTileGridMediator.prototype._onTableFilter = function(event) {
  if (!this.table.tileMode) {
    return;
  }
  this.tileAccordion.filterTiles();
};

scout.TableTileGridMediator.prototype._syncSelectionFromTableToTile = function() {
  if (this.tileAccordion) {
    this.tileAccordion.selectTiles(this.getTilesForRows(this.table.selectedRows));
  }
};

scout.TableTileGridMediator.prototype.syncSelectionFromTileGridToTable = function(selectedTiles) {
  if (!this._isUpdatingTiles) {
    var selectedRows = selectedTiles.map(function(tile) {
      return this.table.rowsMap[tile.rowId];
    }, this).filter(function(t) {
      return !!t;
    });
    this.table.selectRows(selectedRows);
  }
};

scout.TableTileGridMediator.prototype._updateGroupVisibility = function() {
  this.tileAccordion.groups.forEach(function(group) {
    // Make groups invisible if a tile filter is active and no tiles match (= no tiles are visible)
    var groupEmpty = group.body.filters.length > 0 && group.body.filteredTiles.length === 0;
    group.setVisible(!groupEmpty);
    group.setTitleSuffix('(' + group.body.filteredTiles.length + ')');
  });
};

scout.TableTileGridMediator.prototype._syncScrollTopFromTableToTile = function() {
  var rowIndex = this.table._rowIndexAtScrollTop(this.table.scrollTop);
  if (rowIndex <= 0) {
    return;
  }
  var tile = this.tilesMap[this.table.rows[rowIndex].id];
  if (!tile) {
    return;
  }

  // reset scrollTop on tileAccordion, otherwise it would overwrite the synced scrollTop
  this.tileAccordion.scrollTop = null;

  var options = {
    align: 'top'
  };

  if (!tile.rendered) {
    // Execute delayed because table may be not layouted yet
    this.table.session.layoutValidator.schedulePostValidateFunction(tile.reveal.bind(tile, options));
    return;
  }
  tile.reveal(options);
};

scout.TableTileGridMediator.prototype._syncScrollTopFromTileGridToTable = function() {
  var tile = this.tileAccordion._tileAtScrollTop(this.tileAccordion.scrollTop);
  if (tile) {
    var options = {
      align: 'top'
    };
    if (!this.table._isDataRendered()) {
      this.table.session.layoutValidator.schedulePostValidateFunction(this.table.scrollTo.bind(this.table, this.table.rowsMap[tile.rowId], options));
    } else {
      this.table.scrollTo(this.table.rowsMap[tile.rowId], options);
    }
  }
};

scout.TableTileGridMediator.prototype._syncFiltersFromTableToTile = function() {
  if (this.tileAccordion) {
    this.tileAccordion.setTileFilters([]);
    Object.values(this.table._filterMap).forEach(function(tableFilter) {
      this._addFilter(tableFilter);
    }, this);
    this.tileAccordion.filterTiles();
  }
};

scout.TableTileGridMediator.prototype._renderTileTableHeader = function() {
  if (this.table.tileTableHeader) {
    this.table.tileTableHeader.render();
  }
};

scout.TableTileGridMediator.prototype._removeTileTableHeader = function() {
  if (this.table.tileTableHeader) {
    this.table.tileTableHeader.remove();
  }
};

scout.TableTileGridMediator.prototype._renderTileAccordion = function() {
  if (!this.tileAccordion.rendered) {
    this.tileAccordion.render();
  }
};

scout.TableTileGridMediator.prototype._removeTileAccordion = function() {
  if (this.tileAccordion.rendered) {
    this.tileAccordion.remove();
  }
};
