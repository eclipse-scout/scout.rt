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
scout.TileGridAdapter = function() {
  scout.TileGridAdapter.parent.call(this);
  this._addRemoteProperties(['selectedTiles']);
  this._tileFilter = null;
};
scout.inherits(scout.TileGridAdapter, scout.ModelAdapter);

scout.TileGridAdapter.prototype._syncSelectedTiles = function(tiles) {
  // TileGrid.js won't modify the selectedTiles array while processing the response -> ignore every selectedTiles property change
  this.addFilterForPropertyName('selectedTiles');
  this.widget.selectTiles(tiles);
};

scout.TileGridAdapter.prototype._syncTiles = function(tiles) {
  this.addFilterForPropertyName('selectedTiles');
  this.widget.setTiles(tiles);
};

scout.TileGridAdapter.prototype._initProperties = function(model) {
  scout.TileGridAdapter.parent.prototype._initProperties.call(this, model);
  if (!scout.objects.isNullOrUndefined(model.filteredTiles)) {
    // If filteredTiles is set a server side filter is active -> add a tile filter on JS side as well
    this.tileFilter = scout.create('RemoteTileFilter', {
      tileIds: model.filteredTiles
    });
    model.filters = [this.tileFilter];
  }
  // filtered tiles are set by TileGrid.js as soon a applyFilters is called -> don't override with the values sent by the server
  delete model.filteredTiles;
};

scout.TileGridAdapter.prototype._syncFilteredTiles = function(tileIds) {
  // If filteredTiles property changes on the fly, create or remove the filter accordingly
  // -> If filteredTiles is null, no server side filter is active
  // -> If filteredTiles is an empty array, the server side filter rejects every tile
  if (!scout.objects.isNullOrUndefined(tileIds)) {
    if (!this.tileFilter) {
      this.tileFilter = scout.create('RemoteTileFilter');
      this.widget.addFilter(this.tileFilter);
    }
    this.tileFilter.setTileIds(tileIds);
  } else {
    this.widget.removeFilter(this.tileFilter);
    this.tileFilter = null;
  }
  this.widget.filter();
};

scout.TileGridAdapter.prototype._onWidgetTileClick = function(event) {
  var data = {
    tile: event.tile.id,
    mouseButton: event.mouseButton
  };
  this._send('tileClick', data);
};

scout.TileGridAdapter.prototype._onWidgetTileAction = function(event) {
  var data = {
    tile: event.tile.id
  };
  this._send('tileAction', data);
};

scout.TileGridAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'tileClick') {
    this._onWidgetTileClick(event);
  } else if (event.type === 'tileAction') {
    this._onWidgetTileAction(event);
  } else {
    scout.TileGridAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};
