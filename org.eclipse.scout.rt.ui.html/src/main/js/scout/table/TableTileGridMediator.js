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
  this.tileGrid = table.tileGrid;

  this._tileGridListener = null;
  this._tableListener = null;
  this._destroyHandler = null;

  this.tiles = [];
  this.tilesMap = {}; // tiles by rowId
  this.tileFilterMap = {};

  this._tileGridPropertyChangeHandler = this._onTileGridPropertyChange.bind(this);
  this._tableFilterAddedHandler = this._onTableFilterAdded.bind(this);
  this._tableFilterRemovedHandler = this._onTableFilterRemoved.bind(this);
  this._tableFilterHandler = this._onTableFilter.bind(this);
  this._tableRowsSelectedHandler = this._onTableRowsSelected.bind(this);
  this._tableRowsInsertedHandler = this._onTableRowsInserted.bind(this);
  this._tableRowsDeletedHandler = this._onTableRowsDeleted.bind(this);
  this._tableReloadHandler = this._onTableReload.bind(this);

  this._destroyHandler = this._uninstallListeners.bind(this);

  this._installListeners();
};

scout.TableTileGridMediator.prototype.destroy = function() {
  this._uninstallListeners();
};

scout.TableTileGridMediator.prototype._installListeners = function() {
  this.tileGrid.on('propertyChange', this._tileGridPropertyChangeHandler);
  this.table.on('filterAdded', this._tableFilterAddedHandler);
  this.table.on('filterRemoved', this._tableFilterRemovedHandler);
  this.table.on('filter', this._tableFilterHandler);
  this.table.on('rowsSelected', this._tableRowsSelectedHandler);
  this.table.on('rowsInserted', this._tableRowsInsertedHandler);
  this.table.on('rowsDeleted', this._tableRowsDeletedHandler);
  this.table.on('reload', this._tableReloadHandler);

  this.tileGrid.on('destroy', this._destroyHandler);
  this.table.on('destroy', this._destroyHandler);
};

scout.TableTileGridMediator.prototype._uninstallListeners = function() {
  this.tileGrid.off('propertyChange', this._tileGridPropertyChangeHandler);
  this.table.off('filterAdded', this._tableFilterAddedHandler);
  this.table.off('filterRemoved', this._tableFilterRemovedHandler);
  this.table.off('filter', this._tableFilterHandler);
  this.table.off('rowsSelected', this._onTableRowsSelected);
  this.table.off('rowsInserted', this._tableRowsInsertedHandler);
  this.table.off('rowsDeleted', this._tableRowsDeletedHandler);
  this.table.off('reload', this._tableReloadHandler);

  this.tileGrid.off('destroy', this._destroyHandler);
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
  }, this);
};

scout.TableTileGridMediator.prototype.insertTiles = function(tiles) {
  scout.arrays.pushAll(this.tiles, tiles);
  this._refreshTilesMap(tiles);
  this.tileGrid.insertTiles(tiles);
};

scout.TableTileGridMediator.prototype.deleteTiles = function(tiles) {
  if (!tiles) {
    tiles = this.tiles.slice();
  }
  scout.arrays.removeAll(this.tiles, tiles);
  this._refreshTilesMap();
  this.tileGrid.deleteTiles(tiles);
};

scout.TableTileGridMediator.prototype._onTileGridPropertyChange = function(event) {
  if (event.propertyName === 'selectedTiles') {
    this.syncSelectionFromTileGridToTable(event.newValue);
  }
};

scout.TableTileGridMediator.prototype._onTableReload = function(event) {
  this.deleteTiles();
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

scout.TableTileGridMediator.prototype._onTableFilterAdded = function(event) {
  // TODO [10.0] rmu temporary prototype-like filter until filter logic is defined....
  var tileFilter = {
    table: this.table,
    accept: function(tile) {
      var rowForTile = this.table.rowsMap[tile.rowId];
      if (rowForTile) {
        return event.filter.accept(rowForTile);
      } else {
        return false;
      }
    }
  };
  var key = event.filter.createKey();
  if (this.tileFilterMap[key]) {
    this.tileGrid.removeFilter(this.tileFilterMap[key]);
  }
  this.tileFilterMap[key] = tileFilter;
  this.tileGrid.addFilter(tileFilter);
};

scout.TableTileGridMediator.prototype._onTableFilterRemoved = function(event) {
  this.tileGrid.removeFilter(this.tileFilterMap[event.filter.createKey()]);
  this.tileGrid.filter();
};

scout.TableTileGridMediator.prototype._onTableFilter = function(event) {
  this.tileGrid.filter();
};

scout.TableTileGridMediator.prototype._onTableRowsSelected = function(event) {
  this.syncSelectionFromTableToTile();
};

scout.TableTileGridMediator.prototype.syncSelectionFromTileGridToTable = function(selectedTiles) {
  var selectedRows = selectedTiles.map(function(tile) {
    return this.table.rowsMap[tile.rowId];
  }, this);
  this.table.selectRows(selectedRows);
};

scout.TableTileGridMediator.prototype.syncScrollTopFromTableToTile = function() {
  var rowIndex = this.table._rowIndexAtScrollTop(this.table.scrollTop);
  var tile = this.tilesMap[this.table.rows[rowIndex].id];
  this.tileGrid.scrollTo(tile, {
    align: 'top'
  });
};

scout.TableTileGridMediator.prototype.syncSelectionFromTableToTile = function() {
  this.tileGrid.selectTiles(this.getTilesForRows(this.table.selectedRows));
};
