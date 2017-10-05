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
describe("Tiles", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  function createTiles(numTiles, model) {
    var tiles = [];
    for (var i = 0; i < numTiles; i++) {
      tiles.push(scout.create('Tile', {
        parent: session.desktop
      }));
    }
    var defaults = {
      parent: session.desktop,
      tiles: tiles
    };
    model = $.extend({}, defaults, model);
    return scout.create('Tiles', model);
  }

  describe('selectTiles', function() {
    it('selects the given tiles and unselects the previously selected ones', function() {
      var tiles = createTiles(3, {
        selectable: true
      });
      tiles.selectTiles(tiles.tiles[0]);
      expect(tiles.selectedTiles().length).toBe(1);
      expect(tiles.tiles[0].selected).toBe(true);
      expect(tiles.tiles[1].selected).toBe(false);
      expect(tiles.tiles[2].selected).toBe(false);

      tiles.selectTiles(tiles.tiles[1]);
      expect(tiles.selectedTiles().length).toBe(1);
      expect(tiles.tiles[0].selected).toBe(false);
      expect(tiles.tiles[1].selected).toBe(true);
      expect(tiles.tiles[2].selected).toBe(false);

      tiles.selectTiles([tiles.tiles[0], tiles.tiles[2]]);
      expect(tiles.selectedTiles().length).toBe(2);
      expect(tiles.tiles[0].selected).toBe(true);
      expect(tiles.tiles[1].selected).toBe(false);
      expect(tiles.tiles[2].selected).toBe(true);

      tiles.selectTiles([tiles.tiles[0], tiles.tiles[1], tiles.tiles[2]]);
      expect(tiles.selectedTiles().length).toBe(3);
      expect(tiles.tiles[0].selected).toBe(true);
      expect(tiles.tiles[1].selected).toBe(true);
      expect(tiles.tiles[2].selected).toBe(true);

      tiles.selectTiles([]);
      expect(tiles.selectedTiles().length).toBe(0);
      expect(tiles.tiles[0].selected).toBe(false);
      expect(tiles.tiles[1].selected).toBe(false);
      expect(tiles.tiles[2].selected).toBe(false);
    });

    it('does not select if selectable is false', function() {
      var tiles = createTiles(3, {
        selectable: false
      });
      tiles.selectTiles(tiles.tiles[0]);
      expect(tiles.tiles[0].selected).toBe(false);

      tiles.tiles[0].setSelected(true);
      expect(tiles.tiles[0].selected).toBe(false);
    });
  });

  describe('deselectTiles', function() {
    it('deselects the given tiles', function() {
      var tiles = createTiles(3, {
        selectable: true
      });
      tiles.selectAllTiles();
      expect(tiles.selectedTiles().length).toBe(3);
      expect(tiles.tiles[0].selected).toBe(true);
      expect(tiles.tiles[1].selected).toBe(true);
      expect(tiles.tiles[2].selected).toBe(true);

      tiles.deselectTiles(tiles.tiles[1]);
      expect(tiles.selectedTiles().length).toBe(2);
      expect(tiles.tiles[0].selected).toBe(true);
      expect(tiles.tiles[1].selected).toBe(false);
      expect(tiles.tiles[2].selected).toBe(true);

      tiles.deselectAllTiles();
      expect(tiles.selectedTiles().length).toBe(0);
      expect(tiles.tiles[0].selected).toBe(false);
      expect(tiles.tiles[1].selected).toBe(false);
      expect(tiles.tiles[2].selected).toBe(false);
    });
  });
});
