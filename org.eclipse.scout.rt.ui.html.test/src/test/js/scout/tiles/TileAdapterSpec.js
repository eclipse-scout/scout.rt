/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("TileGridAdapter", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createTileGrid(model) {
    var defaults = {
      parent: session.desktop,
      session: session,
      objectType: 'TileGrid'
    };
    model = $.extend({}, defaults, model);
    var tileGridAdapter = session.createModelAdapter(model);
    return tileGridAdapter.createWidget(model, model.parent);
  }

  function createTile(model) {
    var defaults = {
      parent: session.desktop
    };
    model = $.extend({}, defaults, model);
    var tile = scout.create('Tile', model);
    linkWidgetAndAdapter(tile, 'TileAdapter');
    return tile;
  }

  describe("initProperties", function() {

    it("creates a tile filter if tiles are filtered by the server", function() {
      var tile0 = createTile();
      var tile1 = createTile();
      var tile2 = createTile();
      var tileGrid = createTileGrid({
        tiles: [tile0.id, tile1.id, tile2.id],
        filteredTiles: [tile1.id, tile2.id]
      });
      expect(tileGrid.tiles.length).toBe(3);
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tile1);
      expect(tileGrid.filteredTiles[1]).toBe(tile2);
      expect(tileGrid.filters.length).toBe(1);

      // Assert that filter will be removed as soon as server side filter is removed
      var event = createPropertyChangeEvent(tileGrid, {
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

  describe("_syncFilteredTiles", function() {

    it("creates a tile filter if tiles are filtered by the server", function() {
      var tile0 = createTile();
      var tile1 = createTile();
      var tile2 = createTile();
      var tileGrid = createTileGrid({
        tiles: [tile0.id, tile1.id, tile2.id]
      });
      expect(tileGrid.tiles.length).toBe(3);
      expect(tileGrid.filteredTiles.length).toBe(3);
      expect(tileGrid.filters.length).toBe(0);

      // Tiles are filtered on server side
      var event = createPropertyChangeEvent(tileGrid, {
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
