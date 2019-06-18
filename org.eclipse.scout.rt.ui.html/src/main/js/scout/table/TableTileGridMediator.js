/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
/**
 * Delegates events between the Table and it's internal TileGrid.
 *
 */
scout.TableTileGridMediator = function(table) {
  this.table = table;

  this.tileAccordion = null;

  this._tileGridListener = null;
  this._tableListener = null;
  this._destroyHandler = null;

  this.tiles = [];
  this.tilesMap = {}; // tiles by rowId
  this.tileFilterMap = {};
  this.primaryGroupingColumn = null;
  this.groups = {};
  this.groupForTileMap = {}; // groupId by tile
  this.tableState = {}; // always stores the last table state before tileMode activation

  this._tileAccordionPropertyChangeHandler = this._onTileAccordionPropertyChange.bind(this);
  this._tableFilterAddedHandler = this._onTableFilterAdded.bind(this);
  this._tableFilterRemovedHandler = this._onTableFilterRemoved.bind(this);
  this._tableFilterHandler = this._onTableFilter.bind(this);
  this._tableRowsInsertedHandler = this._onTableRowsInserted.bind(this);
  this._tableRowsDeletedHandler = this._onTableRowsDeleted.bind(this);
  this._tableAllRowsDeletedHandler = this._onTableAllRowsDeleted.bind(this);

  this._destroyHandler = this._uninstallListeners.bind(this);
};

scout.TableTileGridMediator.prototype._installListeners = function() {
  this.tileAccordion.on('propertyChange', this._tileAccordionPropertyChangeHandler);
  this.table.on('filterAdded', this._tableFilterAddedHandler);
  this.table.on('filterRemoved', this._tableFilterRemovedHandler);
  this.table.on('filter', this._tableFilterHandler);
  this.table.on('rowsInserted', this._tableRowsInsertedHandler);
  this.table.on('rowsDeleted', this._tableRowsDeletedHandler);
  this.table.on('allRowsDeleted', this._tableAllRowsDeletedHandler);

  this.tileAccordion.on('destroy', this._destroyHandler);
  this.table.on('destroy', this._destroyHandler);
};

scout.TableTileGridMediator.prototype._uninstallListeners = function() {
  this.tileAccordion.off('propertyChange', this._tileAccordionPropertyChangeHandler);
  this.table.off('filterAdded', this._tableFilterAddedHandler);
  this.table.off('filterRemoved', this._tableFilterRemovedHandler);
  this.table.off('filter', this._tableFilterHandler);
  this.table.off('rowsInserted', this._tableRowsInsertedHandler);
  this.table.off('rowsDeleted', this._tableRowsDeletedHandler);
  this.table.off('allRowsDeleted', this._tableAllRowsDeletedHandler);

  this.tileAccordion.off('destroy', this._destroyHandler);
  this.table.off('destroy', this._destroyHandler);
};

scout.TableTileGridMediator.prototype.loadTiles = function() {
  if (this.tiles.length === 0) {
    this.insertTiles(this.table.createTiles(this.table.rows));
  }
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

scout.TableTileGridMediator.prototype._initGroups = function() {
  this.groups = {};

  this.primaryGroupingColumn = this.table.columns.find(function(column) {
    return column.grouped && column.sortIndex === 0;
  });

  this.table.rows.forEach(function(row) {
    var groupId = this._groupIdForRow(row);
    this.groupForTileMap[row.id] = groupId;
    // used as a set
    this.groups[groupId] = null;
  }, this);

  Object.keys(this.groups).forEach(function(groupId) {
    this.tileAccordion.insertGroup(this._createTileGroup(groupId));
  }, this);
};

scout.TableTileGridMediator.prototype._groupIdForRow = function(row) {
  if (this.primaryGroupingColumn) {
    return this.primaryGroupingColumn.cellTextForGrouping(row);
  } else {
    return 'default';
  }
};

scout.TableTileGridMediator.prototype._createTileAccordion = function() {
  return scout.create('TileAccordion', {
    parent: this.table,
    selectable: true,
    multiselect: true,
    gridColumnCount: 10
  });
};

scout.TableTileGridMediator.prototype._createTileGroup = function(groupId) {
  return new scout.create('Group', {
    parent: this.tileAccordion,
    id: groupId,
    // TODO [10.0] rmu temporary workaround that in case of default group no title is shown...
    headerVisible: groupId === 'default' ? false : true,
    title: groupId,
    body: {
      objectType: 'TileGrid'
    }
  });
};

scout.TableTileGridMediator.prototype._groupTiles = function(tiles) {
  tiles.forEach(function(tile) {
    var groupId = 'default';
    if (this.groupForTileMap[tile.rowId]) {
      groupId = this.groupForTileMap[tile.rowId];
    } else {
      // try to evaluate grouping info for this tile
      groupId = this._groupIdForRow(this.table.rowsMap[tile.rowId]);
      this.groupForTileMap[tile.rowId] = groupId;
    }
    tile.parent = this.tileAccordion.getGroupById(groupId);
  }, this);
};

scout.TableTileGridMediator.prototype.init = function() {
  if (!this.tileAccordion) {
    this.tileAccordion = this._createTileAccordion();
    this._installListeners();
  }

  this.tableState.headerVisible = this.table.headerVisible;
};

scout.TableTileGridMediator.prototype.activate = function() {
  // render/model? maybe _setHeaderVisible to not trigger rendering...?
  this.table.setHeaderVisible(false);

  // hide aggregation table control
  this.table.tableControls.filter(function(control) {
    if (control instanceof scout.AggregateTableControl) {
      control.setVisible(false);
    }
  });

  // only makes sense after tiles are loaded... in scout classic this should be executed by the TableAdapter
  this._syncFiltersFromTableToTile();
  //  this._syncScrollTopFromTableToTile();

  // create simplified grouping for tile accordion, grouping on the table can be left as is.
  this._initGroups();
  this._groupTiles(this.tiles);

  this._syncSelectionFromTableToTile();

  this.tileAccordion.setTiles(this.tiles);
};

scout.TableTileGridMediator.prototype.deactivate = function() {
  // model / render...?
  // hide aggregation table control
  this.table.tableControls.filter(function(control) {
    if (control instanceof scout.AggregateTableControl) {
      control.setVisible(true);
    }
  });
  this.table.setHeaderVisible(this.tableState.headerVisible);

  this.tileAccordion.deleteAllTiles();
  this.tileAccordion.deleteAllGroups();
};

scout.TableTileGridMediator.prototype.render = function() {

  // if the table was previously in tileMode this is not necessary...
  if (this.table.$data) {
    this.table._removeData();
  }
  this._renderTileAccordion();
};

scout.TableTileGridMediator.prototype.remove = function() {
  // render
  if (this.table.rendered) {
    this._removeTileAccordion();
  }

  this.tileAccordion.remove();
};

scout.TableTileGridMediator.prototype.destroy = function() {
  // model...?
  // destroy tiles manually since owner is this.table thus the tileGrid can't destroy them
  this.tiles.forEach(function(tile) {
    tile.destroy();
  });

  this.tileAccordion.destroy();
  this.tileAccordion = null;
};

scout.TableTileGridMediator.prototype.insertTiles = function(tiles) {
  if (!tiles) {
    return;
  }
  scout.arrays.pushAll(this.tiles, tiles);
  this._refreshTilesMap(tiles);

  // tileAccordion is only ready when in tileMode.
  if (this.table.tileMode) {
    this._groupTiles(tiles);
    this.tileAccordion.setTiles(this.tiles);
  }
};

scout.TableTileGridMediator.prototype.deleteTiles = function(tiles) {
  if (!tiles) {
    tiles = this.tiles.slice();
  }
  scout.arrays.removeAll(this.tiles, tiles);
  this._refreshTilesMap();
  tiles.forEach(function(tile) {
    tile.destroy();
  });
  this.tileAccordion.deleteTiles(tiles);
};

scout.TableTileGridMediator.prototype._onTileAccordionPropertyChange = function(event) {
  if (!this.table.tileMode) {
    return;
  }
  if (event.propertyName === 'selectedTiles') {
    this.syncSelectionFromTileGridToTable(event.source.getSelectedTiles());
  }
};

scout.TableTileGridMediator.prototype._onTableRowsInserted = function(event) {
  var tiles = this.table.createTiles(event.rows);
  if (tiles) {
    this.insertTiles(tiles);
  }
};

scout.TableTileGridMediator.prototype._onTableRowsDeleted = function(event) {
  this.deleteTiles(this.getTilesForRows(event.rows));
};

scout.TableTileGridMediator.prototype._onTableAllRowsDeleted = function(event) {
  this.deleteTiles();
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
  // TODO [10.0] rmu instead of filtering again use this.setProperty('filteredTiles', tiles);
  //  var filteredTiles = this.table._filteredRows.map(function(row) {
  //    return this.tilesMap[row.id];
  //  }, this);
  //  this.tileAccordion.groups.forEach(function(group) {
  //    var tilesForGroup = filteredTiles.filter(function(tile) {
  //      return this.groupForTileMap[tile.rowId] === group.id;
  //    }, this);
  //
  //    // TODO [10.0] move and refactor this code into TileGrid.js (see scout.TileGrid.prototype.filter)
  //    group.body.setProperty('filteredTiles', tilesForGroup);
  //    group.body.invalidateLogicalGrid(false);
  //
  //    if (group.rendered) {
  //      // Not all tiles may be rendered yet (e.g. if filter is active before grid is rendered and removed after grid is rendered)
  //      // But updating the view range is necessary anyway (fillers, scrollbars, viewRangeRendered etc.)
  //      group.body._renderTileDelta();
  //      group.body._renderTileOrder(group.body.tiles);
  //    }
  //  }, this);
};

scout.TableTileGridMediator.prototype.syncSelectionFromTileGridToTable = function(selectedTiles) {
  var selectedRows = selectedTiles.map(function(tile) {
    return this.table.rowsMap[tile.rowId];
  }, this).filter(function(t) {
    return !!t;
  });
  this.table.selectRows(selectedRows);
};

scout.TableTileGridMediator.prototype._syncScrollTopFromTableToTile = function() {
  var rowIndex = this.table._rowIndexAtScrollTop(this.table.scrollTop);
  if (rowIndex < 0) {
    return;
  }
  var tile = this.tilesMap[this.table.rows[rowIndex].id];
  if (!tile) {
    return;
  }
  this.tileAccordion.scrollTo(tile, {
    align: 'top'
  });
};

scout.TableTileGridMediator.prototype._syncFiltersFromTableToTile = function() {
  if (this.tileAccordion) {
    this.tileAccordion.setTileFilters([]);
    Object.values(this.table._filterMap).forEach(function(tableFilter) {
      this._addFilter(tableFilter);
    }, this);
  }
};

scout.TableTileGridMediator.prototype._syncSelectionFromTableToTile = function() {
  if (this.tileAccordion) {
    this.tileAccordion.selectTiles(this.getTilesForRows(this.table.selectedRows));
  }
};

scout.TableTileGridMediator.prototype._renderTileAccordion = function() {
  if (this.tileAccordion.rendered) {
    return;
  }
  this.tileAccordion.render();
};

scout.TableTileGridMediator.prototype._removeTileAccordion = function() {
  if (!this.tileAccordion.rendered) {
    return;
  }
  this.tileAccordion.remove();
};
