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
scout.TableMediator = function(table) {
  this.table = table;
  this.tileGrid = table.tileGrid;

  this._tileGridListener = null;
  this._tableListener = null;
  this._destroyHandler = null;

  this._installListeners();

  this.tileFilterMap = {};
};
// TODO [10.0] rmu inherit from EventDelegator?

scout.TableMediator.prototype.destroy = function() {
  this._uninstallListeners();
};

scout.TableMediator.prototype._installListeners = function() {
  if (this._tileGridListener) {
    throw new Error('source listeners already installed.');
  }
  this._tileGridListener = {
    func: this._onTileGridEvent.bind(this)
  };
  this._tableListener = {
    func: this._onTableEvent.bind(this)
  };
  this.tileGrid.events.addListener(this._tileGridListener);
  this.table.events.addListener(this._tableListener);
  this._destroyHandler = this._uninstallListeners.bind(this);
  this.tileGrid.on('destroy', this._destroyHandler);
  this.table.on('destroy', this._destroyHandler);
};

scout.TableMediator.prototype._uninstallListeners = function() {
  if (this._tileGridListener) {
    this.tileGrid.events.removeListener(this._tileGridListener);
    this._tileGridListener = null;
    this.table.events.removeListener(this._tableListener);
    this._tableListener = null;
  }
  if (this._destroyHandler) {
    this.tileGrid.off('destroy', this._destroyHandler);
    this.table.off('destroy', this._destroyHandler);
    this._destroyHandler = null;
  }
};

scout.TableMediator.prototype._onTileGridEvent = function(event) {
  if (event.type === 'propertyChange' && event.propertyName === 'selectedTiles') {
    var selectedRows = event.newValue.map(function(tile) {
      return this.table.rowsMap[tile.rowId];
    }, this);
    this.table.selectRows(selectedRows);
  }
};

scout.TableMediator.prototype._onTableEvent = function(event) {
  if (event.type === 'filterAdded') {
    // XXX temporary prototype-like filter until filter logic is defined....
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
  } else if (event.type === 'filterRemoved') {
    this.tileGrid.removeFilter(this.tileFilterMap[event.filter.createKey()]);
    this.tileGrid.filter();
  } else
  if (event.type === 'filterReset') {
    // nop
  } else if (event.type === 'filter') {
    this.tileGrid.filter();
  } else if (event.type === 'rowsSelected') {
    this.syncSelectionFromTableToTile(this.table.selectedRows);
  }
};

scout.TableMediator.prototype.syncSelectionFromTableToTile = function(selectedRows) {
  this.tileGrid.selectTiles(this.table.getTilesForRows(selectedRows));
};
