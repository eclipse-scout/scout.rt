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
};

scout.TableTileGridMediator.prototype.init = function(model) {
  scout.assertParameter('table', model.table);
  $.extend(this, model);

  if (!this.tileAccordion) {
    this.tileAccordion = this._createTileAccordion();
    this._installListeners();
  }

  this.tableState.headerVisible = this.table.headerVisible;
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

scout.TableTileGridMediator.prototype.loadTiles = function() {
  var tiles = this.table.createTiles(this.table.rows);
  if (tiles) {
    this.table._setTiles(tiles);
  }
};

scout.TableTileGridMediator.prototype.resolveMapping = function(tableRowTileMapping) {
  tableRowTileMapping.tile.rowId = tableRowTileMapping.tableRow;
  return tableRowTileMapping.tile;
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
      this.tileAccordion.insertGroup(group);
    }
    tile.parent = group;
    group.setTitleSuffix('(' + (++group.tileCount) + ')');
  }, this);
};

scout.TableTileGridMediator.prototype._createTileAccordion = function() {
  return scout.create('TileAccordion', {
    parent: this.table,
    selectable: true,
    multiselect: true,
    gridColumnCount: 10
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
      objectType: 'TileGrid'
    },
    tileCount: 0
  });
};

scout.TableTileGridMediator.prototype.activate = function() {
  this.table.setHeaderVisible(false);
  if (this.table.tileTableHeaderBox) {
    this.table.tileTableHeaderBox.setVisible(true);
  }

  // hide aggregation table control
  this.table.tableControls.filter(function(control) {
    if (control instanceof scout.AggregateTableControl) {
      control.setVisible(false);
    }
  });

  // doesn't depend upon any tile data, therefore execute on activation
  this._syncFiltersFromTableToTile();
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
  if (this.table.tileTableHeaderBox) {
    this.table.tileTableHeaderBox.setVisible(false);
  }

  // TODO [10.0] rmu: reload tiles on every mode-switch. loadTiles() could be optimized so that only not already existing tiles are reloaded
  // complete reset
  this.reset();
};

scout.TableTileGridMediator.prototype.reset = function() {
  this.tiles = [];
  this.tilesMap = {};
  this.tileFilterMap = {};
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
    this._renderTileAccordion();
  } else {
    // since tileMode is not a widget property remove is not called by setProperty
    this._removeTileAccordion();
    this.table._renderData();
    this.table._renderTableHeader();
  }
};

scout.TableTileGridMediator.prototype.removeTileMode = function() {
  if (this.table.rendered) {
    this._removeTileAccordion();
  }
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
  if (!tiles) {
    return;
  }
  scout.arrays.pushAll(this.tiles, tiles);
  this._refreshTilesMap(tiles);

  // create simplified grouping for tile accordion, grouping on the table can be left as is.
  this._initGroups(tiles);

  this.tileAccordion.setTiles(this.tiles);

  this._syncSelectionFromTableToTile();
  //  this.mediator._syncScrollTopFromTableToTile();
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
    group.setTitleSuffix('(' + (--group.tileCount) + ')');
    if (group.tileCount === 0) {
      this.tileAccordion.deleteGroup(group);
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
  var tableRowTileMappings = this.table.createTiles(event.rows);
  if (tableRowTileMappings) {
    this.insertTiles(tableRowTileMappings.map(this.resolveMapping));
  }
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
  if (!this.table.tileMode) {
    return;
  }
  this.tiles = this.table.visibleRows.map(function(row) {
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
    this.tileAccordion.filterTiles();
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
