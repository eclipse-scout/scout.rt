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
describe("TileGridKeyStrokes", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  function createTileGrid(numTiles, model) {
    var tiles = [];
    for (var i = 0; i < numTiles; i++) {
      tiles.push({
        objectType: 'Tile',
        label: "Tile " + i
      });
    }
    var defaults = {
      parent: session.desktop,
      tiles: tiles
    };
    model = $.extend({}, defaults, model);
    return scout.create('TileGrid', model);
  }

  function createTile(model) {
    var defaults = {
      parent: session.desktop
    };
    model = $.extend({}, defaults, model);
    return scout.create('Tile', model);
  }

  describe('ctrl + a', function() {
    it('selects all tiles', function() {
      var tileGrid = createTileGrid(3, {
        selectable: true
      });
      tileGrid.render();
      expect(tileGrid.selectedTiles.length).toBe(0);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.A, 'ctrl');
      expect(tileGrid.selectedTiles.length).toBe(3);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.A, 'ctrl');

      tileGrid.selectTile(tileGrid.tiles[0]);
      expect(tileGrid.selectedTiles.length).toBe(1);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.A, 'ctrl');
      expect(tileGrid.selectedTiles.length).toBe(3);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.A, 'ctrl');
    });

    it('deselects all tiles if tiles are already selected', function() {
      var tileGrid = createTileGrid(3, {
        selectable: true
      });
      tileGrid.render();
      tileGrid.selectAllTiles();
      expect(tileGrid.selectedTiles.length).toBe(3);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.A, 'ctrl');
      expect(tileGrid.selectedTiles.length).toBe(0);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.A, 'ctrl');

      tileGrid.$container.triggerKeyDownCapture(scout.keys.A, 'ctrl');
      expect(tileGrid.selectedTiles.length).toBe(3);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.A, 'ctrl');

      tileGrid.$container.triggerKeyDownCapture(scout.keys.A, 'ctrl');
      expect(tileGrid.selectedTiles.length).toBe(0);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.A, 'ctrl');
    });

  });

  describe('key right', function() {
    it("selects the next tile", function() {
      var tileGrid = createTileGrid(4, {
        selectable: true
      });
      tileGrid.render();
      tileGrid.selectTile(tileGrid.tiles[1]);
      expect(tileGrid.selectedTiles.length).toBe(1);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.RIGHT);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[2]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.RIGHT);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.RIGHT);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[3]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.RIGHT);
    });

    it("selects the first tile if no tile is selected yet", function() {
      var tileGrid = createTileGrid(4, {
        selectable: true
      });
      tileGrid.render();
      expect(tileGrid.selectedTiles.length).toBe(0);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.RIGHT);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.RIGHT);
    });

    it("does nothing if the last tile is already selected", function() {
      var tileGrid = createTileGrid(4, {
        selectable: true
      });
      tileGrid.render();
      tileGrid.selectTile(scout.arrays.last(tileGrid.tiles));
      expect(tileGrid.selectedTiles).toEqual([scout.arrays.last(tileGrid.tiles)]);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.RIGHT);
      expect(tileGrid.selectedTiles).toEqual([scout.arrays.last(tileGrid.tiles)]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.RIGHT);
    });

    it("selects the only tile if there is only one", function() {
      var tileGrid = createTileGrid(1, {
        selectable: true
      });
      tileGrid.render();
      expect(tileGrid.selectedTiles.length).toBe(0);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.RIGHT);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.RIGHT);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.RIGHT);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.RIGHT);

      tileGrid.deselectAllTiles();

      tileGrid.$container.triggerKeyDownCapture(scout.keys.RIGHT);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.RIGHT);
    });

  });

  describe('key left', function() {
    it("selects the previous tile", function() {
      var tileGrid = createTileGrid(4, {
        selectable: true
      });
      tileGrid.render();
      tileGrid.selectTile(tileGrid.tiles[2]);
      expect(tileGrid.selectedTiles.length).toBe(1);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.LEFT);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[1]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.LEFT);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.LEFT);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.LEFT);
    });

    it("selects the last tile if no tile is selected yet", function() {
      var tileGrid = createTileGrid(4, {
        selectable: true
      });
      tileGrid.render();
      expect(tileGrid.selectedTiles.length).toBe(0);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.LEFT);
      expect(tileGrid.selectedTiles).toEqual([scout.arrays.last(tileGrid.tiles)]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.LEFT);
    });

    it("does nothing if the first tile is already selected", function() {
      var tileGrid = createTileGrid(4, {
        selectable: true
      });
      tileGrid.render();
      tileGrid.selectTile(tileGrid.tiles[0]);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.LEFT);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.LEFT);
    });

    it("selects the only tile if there is only one", function() {
      var tileGrid = createTileGrid(1, {
        selectable: true
      });
      tileGrid.render();
      expect(tileGrid.selectedTiles.length).toBe(0);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.LEFT);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.LEFT);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.LEFT);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.LEFT);

      tileGrid.deselectAllTiles();

      tileGrid.$container.triggerKeyDownCapture(scout.keys.LEFT);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.LEFT);
    });

  });

  describe('key down', function() {
    it("selects the tile below", function() {
      var tileGrid = createTileGrid(8, {
        selectable: true,
        gridColumnCount: 3
      });
      tileGrid.render();
      tileGrid.selectTile(tileGrid.tiles[0]);
      expect(tileGrid.selectedTiles.length).toBe(1);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[3]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.DOWN);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[6]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.DOWN);
    });

    it("selects the first tile if no tile is selected yet", function() {
      var tileGrid = createTileGrid(8, {
        selectable: true,
        gridColumnCount: 3
      });
      tileGrid.render();
      expect(tileGrid.selectedTiles.length).toBe(0);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.DOWN);
    });

    it("does nothing if a tile in the last row is already selected", function() {
      var tileGrid = createTileGrid(8, {
        selectable: true,
        gridColumnCount: 3
      });
      tileGrid.render();
      tileGrid.selectTile(tileGrid.tiles[6]);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[6]]);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[6]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.DOWN);

      tileGrid.selectTile(scout.arrays.last(tileGrid.tiles));
      expect(tileGrid.selectedTiles).toEqual([scout.arrays.last(tileGrid.tiles)]);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(tileGrid.selectedTiles).toEqual([scout.arrays.last(tileGrid.tiles)]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.DOWN);
    });

    it("selects the only tile if there is only one", function() {
      var tileGrid = createTileGrid(1, {
        selectable: true,
        gridColumnCount: 3
      });
      tileGrid.render();
      expect(tileGrid.selectedTiles.length).toBe(0);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.DOWN);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.DOWN);

      tileGrid.deselectAllTiles();

      tileGrid.$container.triggerKeyDownCapture(scout.keys.DOWN);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.DOWN);
    });

  });

  describe('key up', function() {
    it("selects the tile above", function() {
      var tileGrid = createTileGrid(8, {
        selectable: true,
        gridColumnCount: 3
      });
      tileGrid.render();
      tileGrid.selectTile(scout.arrays.last(tileGrid.tiles));
      expect(tileGrid.selectedTiles.length).toBe(1);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.UP);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[4]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.UP);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.UP);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[1]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.UP);
    });

    it("selects the last tile if no tile is selected yet", function() {
      var tileGrid = createTileGrid(8, {
        selectable: true,
        gridColumnCount: 3
      });
      tileGrid.render();
      expect(tileGrid.selectedTiles.length).toBe(0);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.UP);
      expect(tileGrid.selectedTiles).toEqual([scout.arrays.last(tileGrid.tiles)]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.UP);
    });

    it("does nothing if a tile in the first row is already selected", function() {
      var tileGrid = createTileGrid(8, {
        selectable: true,
        gridColumnCount: 3
      });
      tileGrid.render();
      tileGrid.selectTile(tileGrid.tiles[2]);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[2]]);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.UP);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[2]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.UP);

      tileGrid.selectTile(tileGrid.tiles[0]);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.UP);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.UP);
    });

    it("selects the only tile if there is only one", function() {
      var tileGrid = createTileGrid(1, {
        selectable: true,
        gridColumnCount: 3
      });
      tileGrid.render();
      expect(tileGrid.selectedTiles.length).toBe(0);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.UP);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.UP);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.UP);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.UP);

      tileGrid.deselectAllTiles();

      tileGrid.$container.triggerKeyDownCapture(scout.keys.UP);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.UP);
    });

  });

  describe('home', function() {
    it("selects the first tile", function() {
      var tileGrid = createTileGrid(8, {
        selectable: true,
        gridColumnCount: 3
      });
      tileGrid.render();
      tileGrid.selectTile(tileGrid.tiles[1]);
      expect(tileGrid.selectedTiles.length).toBe(1);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.HOME);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.HOME);
    });

    it("does nothing if the first tile is already selected", function() {
      var tileGrid = createTileGrid(4, {
        selectable: true
      });
      tileGrid.render();
      tileGrid.selectTile(tileGrid.tiles[0]);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.HOME);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.HOME);
    });

    it("selects only the first tile if first and other tiles are selected", function() {
      var tileGrid = createTileGrid(8, {
        selectable: true,
        gridColumnCount: 3
      });
      tileGrid.render();
      tileGrid.selectTiles([tileGrid.tiles[0], tileGrid.tiles[1]]);
      expect(tileGrid.selectedTiles.length).toBe(2);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.HOME);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.HOME);
    });

    it("selects the only tile if there is only one", function() {
      var tileGrid = createTileGrid(1, {
        selectable: true,
        gridColumnCount: 3
      });
      tileGrid.render();
      expect(tileGrid.selectedTiles.length).toBe(0);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.HOME);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.HOME);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.HOME);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.HOME);

      tileGrid.deselectAllTiles();

      tileGrid.$container.triggerKeyDownCapture(scout.keys.HOME);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.HOME);
    });

  });

  describe('end', function() {
    it("selects the last tile", function() {
      var tileGrid = createTileGrid(8, {
        selectable: true,
        gridColumnCount: 3
      });
      tileGrid.render();
      expect(tileGrid.selectedTiles.length).toBe(0);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.END);
      expect(tileGrid.selectedTiles).toEqual([scout.arrays.last(tileGrid.tiles)]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.END);
    });

    it("does nothing if the first tile is already selected", function() {
      var tileGrid = createTileGrid(4, {
        selectable: true
      });
      tileGrid.render();
      tileGrid.selectTile(scout.arrays.last(tileGrid.tiles));
      expect(tileGrid.selectedTiles).toEqual([scout.arrays.last(tileGrid.tiles)]);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.END);
      expect(tileGrid.selectedTiles).toEqual([scout.arrays.last(tileGrid.tiles)]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.END);
    });

    it("selects only the last tile if last and other tiles are selected", function() {
      var tileGrid = createTileGrid(4, {
        selectable: true,
        gridColumnCount: 3
      });
      tileGrid.render();
      tileGrid.selectTiles([tileGrid.tiles[2], tileGrid.tiles[3]]);
      expect(tileGrid.selectedTiles.length).toBe(2);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.END);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[3]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.END);
    });

    it("selects the only tile if there is only one", function() {
      var tileGrid = createTileGrid(1, {
        selectable: true,
        gridColumnCount: 3
      });
      tileGrid.render();
      expect(tileGrid.selectedTiles.length).toBe(0);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.END);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.END);

      tileGrid.$container.triggerKeyDownCapture(scout.keys.END);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.END);

      tileGrid.deselectAllTiles();

      tileGrid.$container.triggerKeyDownCapture(scout.keys.END);
      expect(tileGrid.selectedTiles).toEqual([tileGrid.tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(scout.keys.END);
    });

  });

});
