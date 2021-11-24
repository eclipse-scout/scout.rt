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
import {ModelAdapter, objects, scout} from '../index';

export default class TileGridAdapter extends ModelAdapter {

  constructor() {
    super();
    this._addRemoteProperties(['selectedTiles']);
    this._tileFilter = null;
  }

  _syncSelectedTiles(tiles) {
    // TileGrid.js won't modify the selectedTiles array while processing the response -> ignore every selectedTiles property change
    this.addFilterForPropertyName('selectedTiles');
    this.widget.selectTiles(tiles);
  }

  _syncTiles(tiles) {
    this.addFilterForPropertyName('selectedTiles');
    this.widget.setTiles(tiles);
  }

  _initProperties(model) {
    super._initProperties(model);
    if (!objects.isNullOrUndefined(model.filteredTiles)) {
      // If filteredTiles is set a server side filter is active -> add a tile filter on JS side as well
      this.tileFilter = scout.create('RemoteTileFilter', {
        tileIds: model.filteredTiles
      });
      model.filters = [this.tileFilter];
    }
    // filtered tiles are set by TileGrid.js as soon a applyFilters is called -> don't override with the values sent by the server
    delete model.filteredTiles;
  }

  _syncFilteredTiles(tileIds) {
    // If filteredTiles property changes on the fly, create or remove the filter accordingly
    // -> If filteredTiles is null, no server side filter is active
    // -> If filteredTiles is an empty array, the server side filter rejects every tile
    if (!objects.isNullOrUndefined(tileIds)) {
      if (!this.tileFilter) {
        this.tileFilter = scout.create('RemoteTileFilter');
        this.widget.addFilter(this.tileFilter, false);
      }
      this.tileFilter.setTileIds(tileIds);
      this.widget.filter();
    } else {
      this.widget.removeFilter(this.tileFilter);
      this.tileFilter = null;
    }
  }

  _onWidgetTileClick(event) {
    let data = {
      tile: event.tile.id,
      mouseButton: event.mouseButton
    };
    this._send('tileClick', data);
  }

  _onWidgetTileAction(event) {
    let data = {
      tile: event.tile.id
    };
    this._send('tileAction', data);
  }

  _onWidgetEvent(event) {
    if (event.type === 'tileClick') {
      this._onWidgetTileClick(event);
    } else if (event.type === 'tileAction') {
      this._onWidgetTileAction(event);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
