/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {scout, Tile, TileGrid, TileGridModel} from '../../src/index';
import {Optional} from '../../src/types';
import {AdapterData} from '../../src/session/Session';
import TileModel from '../../src/tile/TileModel';
import {ObjectType} from '../../src/ObjectFactory';

describe('TileGridAdapter', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createTileGrid(model?: Optional<TileGridModel, 'parent'>): TileGrid {
    let defaults = {
      parent: session.desktop,
      session: session,
      objectType: 'TileGrid'
    };
    let m = $.extend({}, defaults, model) as TileGridModel & {objectType: ObjectType<TileGrid>};
    let tileGridAdapter = session.createModelAdapter(m as AdapterData);
    return tileGridAdapter.createWidget(m, m.parent) as TileGrid;
  }

  function createTile(model?: Optional<TileModel, 'parent'>): Tile {
    let defaults = {
      parent: session.desktop
    };
    model = $.extend({}, defaults, model);
    let tile = scout.create(Tile, model as TileModel);
    linkWidgetAndAdapter(tile, 'TileAdapter');
    return tile;
  }

  describe('initProperties', () => {

    it('creates a tile filter if tiles are filtered by the server', () => {
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      let tileGrid = createTileGrid({
        tiles: [tile0.id, tile1.id, tile2.id],
        filteredTiles: [tile1.id, tile2.id]
      });
      expect(tileGrid.tiles.length).toBe(3);
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tile1);
      expect(tileGrid.filteredTiles[1]).toBe(tile2);
      expect(tileGrid.filters.length).toBe(1);

      // Assert that filter will be removed as soon as server side filter is removed
      let event = createPropertyChangeEvent(tileGrid, {
        filteredTiles: null
      });
      tileGrid.modelAdapter.onModelPropertyChange(event);
      expect(tileGrid.tiles.length).toBe(3);
      expect(tileGrid.filteredTiles.length).toBe(3);
      expect(tileGrid.filteredTiles[0]).toBe(tile0);
      expect(tileGrid.filteredTiles[1]).toBe(tile1);
      expect(tileGrid.filteredTiles[2]).toBe(tile2);
      expect(tileGrid.filters.length).toBe(0);
    });

  });

  describe('_syncFilteredTiles', () => {

    it('creates a tile filter if tiles are filtered by the server', () => {
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      let tileGrid = createTileGrid({
        tiles: [tile0.id, tile1.id, tile2.id]
      });
      expect(tileGrid.tiles.length).toBe(3);
      expect(tileGrid.filteredTiles.length).toBe(3);
      expect(tileGrid.filters.length).toBe(0);

      // Tiles are filtered on server side
      let event = createPropertyChangeEvent(tileGrid, {
        filteredTiles: [tile1.id, tile2.id]
      });
      tileGrid.modelAdapter.onModelPropertyChange(event);
      expect(tileGrid.tiles.length).toBe(3);
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tile1);
      expect(tileGrid.filteredTiles[1]).toBe(tile2);
      expect(tileGrid.filters.length).toBe(1);

      // Server side filter was removed again
      event = createPropertyChangeEvent(tileGrid, {
        filteredTiles: null
      });
      tileGrid.modelAdapter.onModelPropertyChange(event);
      expect(tileGrid.tiles.length).toBe(3);
      expect(tileGrid.filteredTiles.length).toBe(3);
      expect(tileGrid.filters.length).toBe(0);
    });

  });

});
