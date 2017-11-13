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
    return scout.create('Tiles', model);
  }

  function createTile(model) {
    var defaults = {
      parent: session.desktop
    };
    model = $.extend({}, defaults, model);
    return scout.create('Tile', model);
  }

  describe('selectTiles', function() {
    it('selects the given tiles and unselects the previously selected ones', function() {
      var tiles = createTiles(3, {
        selectable: true
      });
      tiles.selectTiles(tiles.tiles[0]);
      expect(tiles.selectedTiles.length).toBe(1);
      expect(tiles.selectedTiles[0]).toBe(tiles.tiles[0]);
      expect(tiles.tiles[0].selected).toBe(true);
      expect(tiles.tiles[1].selected).toBe(false);
      expect(tiles.tiles[2].selected).toBe(false);

      tiles.selectTiles(tiles.tiles[1]);
      expect(tiles.selectedTiles.length).toBe(1);
      expect(tiles.selectedTiles[0]).toBe(tiles.tiles[1]);
      expect(tiles.tiles[0].selected).toBe(false);
      expect(tiles.tiles[1].selected).toBe(true);
      expect(tiles.tiles[2].selected).toBe(false);

      tiles.selectTiles([tiles.tiles[0], tiles.tiles[2]]);
      expect(tiles.selectedTiles.length).toBe(2);
      expect(tiles.selectedTiles[0]).toBe(tiles.tiles[0]);
      expect(tiles.selectedTiles[1]).toBe(tiles.tiles[2]);
      expect(tiles.tiles[0].selected).toBe(true);
      expect(tiles.tiles[1].selected).toBe(false);
      expect(tiles.tiles[2].selected).toBe(true);

      tiles.selectTiles([tiles.tiles[0], tiles.tiles[1], tiles.tiles[2]]);
      expect(tiles.selectedTiles.length).toBe(3);
      expect(tiles.selectedTiles[0]).toBe(tiles.tiles[0]);
      expect(tiles.selectedTiles[1]).toBe(tiles.tiles[1]);
      expect(tiles.selectedTiles[2]).toBe(tiles.tiles[2]);
      expect(tiles.tiles[0].selected).toBe(true);
      expect(tiles.tiles[1].selected).toBe(true);
      expect(tiles.tiles[2].selected).toBe(true);

      tiles.selectTiles([]);
      expect(tiles.selectedTiles.length).toBe(0);
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

    it('does not select tiles excluded by filter', function() {
      var tiles = createTiles(3, {
        selectable: true
      });

      var filter = {
        accept: function(tile) {
          return tile.label.indexOf('1') < 0;
        }
      };
      tiles.addFilter(filter);
      tiles.filter();

      tiles.selectTiles(tiles.tiles[1]);
      expect(tiles.tiles[1].selected).toBe(false);

      tiles.selectTiles(tiles.tiles[0]);
      expect(tiles.tiles[0].selected).toBe(true);

      tiles.removeFilter(filter);
      tiles.filter();

      tiles.selectTiles(tiles.tiles[1]);
      expect(tiles.tiles[1].selected).toBe(true);
    });

    it('triggers a property change event', function() {
      var tiles = createTiles(3, {
        selectable: true
      });
      var eventTriggered = false;
      tiles.on('propertyChange', function(event) {
        if (event.propertyName === 'selectedTiles') {
          eventTriggered = true;
        }
      });
      tiles.selectTiles(tiles.tiles[0]);
      expect(eventTriggered).toBe(true);
    });
  });

  describe('deselectTiles', function() {
    it('deselects the given tiles', function() {
      var tiles = createTiles(3, {
        selectable: true
      });
      tiles.selectAllTiles();
      expect(tiles.selectedTiles.length).toBe(3);
      expect(tiles.selectedTiles[0]).toBe(tiles.tiles[0]);
      expect(tiles.selectedTiles[1]).toBe(tiles.tiles[1]);
      expect(tiles.selectedTiles[2]).toBe(tiles.tiles[2]);
      expect(tiles.tiles[0].selected).toBe(true);
      expect(tiles.tiles[1].selected).toBe(true);
      expect(tiles.tiles[2].selected).toBe(true);

      tiles.deselectTiles(tiles.tiles[1]);
      expect(tiles.selectedTiles.length).toBe(2);
      expect(tiles.selectedTiles[0]).toBe(tiles.tiles[0]);
      expect(tiles.selectedTiles[1]).toBe(tiles.tiles[2]);
      expect(tiles.tiles[0].selected).toBe(true);
      expect(tiles.tiles[1].selected).toBe(false);
      expect(tiles.tiles[2].selected).toBe(true);

      tiles.deselectAllTiles();
      expect(tiles.selectedTiles.length).toBe(0);
      expect(tiles.tiles[0].selected).toBe(false);
      expect(tiles.tiles[1].selected).toBe(false);
      expect(tiles.tiles[2].selected).toBe(false);
    });

    it('triggers a property change event', function() {
      var tiles = createTiles(3, {
        selectable: true
      });
      var eventTriggered = false;
      tiles.selectAllTiles();
      tiles.on('propertyChange', function(event) {
        if (event.propertyName === 'selectedTiles') {
          eventTriggered = true;
        }
      });
      tiles.deselectTile(tiles.tiles[0]);
      expect(eventTriggered).toBe(true);
    });
  });

  describe('insertTiles', function() {
    it('inserts the given tiles', function() {
      var tiles = createTiles(0);
      var tile0 = createTile();
      var tile1 = createTile();
      var tile2 = createTile();
      expect(tiles.tiles.length).toBe(0);

      tiles.insertTiles(tile0);
      expect(tiles.tiles.length).toBe(1);
      expect(tiles.tiles[0]).toBe(tile0);

      tiles.insertTiles([tile1, tile2]);
      expect(tiles.tiles.length).toBe(3);
      expect(tiles.tiles[0]).toBe(tile0);
      expect(tiles.tiles[1]).toBe(tile1);
      expect(tiles.tiles[2]).toBe(tile2);
    });

    it('triggers a property change event', function() {
      var tiles = createTiles(0);
      var tile0 = createTile();
      var eventTriggered = false;
      tiles.on('propertyChange', function(event) {
        if (event.propertyName === 'tiles') {
          eventTriggered = true;
        }
      });
      tiles.insertTiles(tile0);
      expect(eventTriggered).toBe(true);
    });

    it('links the inserted tiles with the tiles container', function() {
      var tiles = createTiles(0);
      var tile0 = createTile();
      var tile1 = createTile();
      var tile2 = createTile();
      expect(tiles.tiles.length).toBe(0);
      expect(tile0.parent).toBe(session.desktop);
      expect(tile1.parent).toBe(session.desktop);
      expect(tile2.parent).toBe(session.desktop);

      tiles.insertTile(tile0);
      expect(tile0.parent).toBe(tiles);

      tiles.insertTiles([tile1, tile2]);
      expect(tile1.parent).toBe(tiles);
      expect(tile2.parent).toBe(tiles);
    });
  });

  describe('deleteTiles', function() {
    it('deletes the given tiles', function() {
      var tiles = createTiles(0);
      var tile0 = createTile();
      var tile1 = createTile();
      var tile2 = createTile();
      tiles.insertTiles([tile0, tile1, tile2]);
      expect(tiles.tiles.length).toBe(3);

      tiles.deleteTiles(tile1);
      expect(tiles.tiles.length).toBe(2);
      expect(tiles.tiles[0]).toBe(tile0);
      expect(tiles.tiles[1]).toBe(tile2);

      tiles.deleteTiles([tile0, tile2]);
      expect(tiles.tiles.length).toBe(0);
    });

    it('deselects the deleted tiles', function() {
      var tiles = createTiles(0, {
        selectable: true
      });
      var tile0 = createTile();
      var tile1 = createTile();
      var tile2 = createTile();
      tiles.insertTiles([tile0, tile1, tile2]);
      expect(tiles.tiles.length).toBe(3);

      tiles.selectAllTiles();
      expect(tiles.selectedTiles.length).toBe(3);

      tiles.deleteTiles(tile1);
      expect(tiles.selectedTiles.length).toBe(2);
      expect(tiles.selectedTiles[0]).toBe(tile0);
      expect(tiles.selectedTiles[1]).toBe(tile2);

      tiles.deleteTiles([tile0, tile2]);
      expect(tiles.selectedTiles.length).toBe(0);
    });

    it('triggers a property change event', function() {
      var tiles = createTiles(3);
      var eventTriggered = false;
      tiles.on('propertyChange', function(event) {
        if (event.propertyName === 'tiles') {
          eventTriggered = true;
        }
      });
      tiles.deleteTiles(tiles.tiles[0]);
      expect(eventTriggered).toBe(true);
    });

    it('destroys the deleted tiles', function() {
      var tiles = createTiles(0, {
        animateTileRemoval: false
      });
      var tile0 = createTile({
        parent: tiles
      });
      var tile1 = createTile({
        parent: tiles
      });
      var tile2 = createTile({
        parent: tiles
      });
      tiles.render();
      tiles.insertTiles([tile0, tile1, tile2]);
      expect(tile0.destroyed).toBe(false);
      expect(tile0.rendered).toBe(true);
      expect(tile1.destroyed).toBe(false);
      expect(tile1.rendered).toBe(true);
      expect(tile2.destroyed).toBe(false);
      expect(tile2.rendered).toBe(true);

      tiles.deleteTile(tile1);
      expect(tile1.destroyed).toBe(true);
      expect(tile1.rendered).toBe(false);

      tiles.deleteTiles([tile0, tile2]);
      expect(tile0.destroyed).toBe(true);
      expect(tile0.rendered).toBe(false);
      expect(tile2.destroyed).toBe(true);
      expect(tile2.rendered).toBe(false);
    });

    /**
     * This spec is important if a tile should be moved from one tiles container to another.
     */
    it('does not destroy the deleted tiles if the tiles container is not the owner', function() {
      var tiles = createTiles(0, {
        animateTileRemoval: false
      });
      var tile0 = createTile({
        owner: session.desktop
      });
      var tile1 = createTile({
        owner: session.desktop
      });
      var tile2 = createTile({
        owner: session.desktop
      });
      tiles.render();
      tiles.insertTiles([tile0, tile1, tile2]);
      expect(tile0.destroyed).toBe(false);
      expect(tile0.rendered).toBe(true);
      expect(tile1.destroyed).toBe(false);
      expect(tile1.rendered).toBe(true);
      expect(tile2.destroyed).toBe(false);
      expect(tile2.rendered).toBe(true);

      tiles.deleteTile(tile1);
      expect(tile1.destroyed).toBe(false);
      expect(tile1.rendered).toBe(false);

      tiles.deleteTiles([tile0, tile2]);
      expect(tile0.destroyed).toBe(false);
      expect(tile0.rendered).toBe(false);
      expect(tile2.destroyed).toBe(false);
      expect(tile2.rendered).toBe(false);
    });
  });

  describe('deleteAllTiles', function() {
    it('deletes all tiles', function() {
      var tiles = createTiles(0);
      var tile0 = createTile();
      var tile1 = createTile();
      var tile2 = createTile();
      tiles.insertTiles([tile0, tile1, tile2]);
      expect(tiles.tiles.length).toBe(3);

      tiles.deleteAllTiles();
      expect(tiles.tiles.length).toBe(0);
    });

    it('deselects the deleted tiles', function() {
      var tiles = createTiles(0, {
        selectable: true
      });
      var tile0 = createTile();
      var tile1 = createTile();
      var tile2 = createTile();
      tiles.insertTiles([tile0, tile1, tile2]);
      expect(tiles.tiles.length).toBe(3);

      tiles.selectAllTiles();
      expect(tiles.selectedTiles.length).toBe(3);

      tiles.deleteAllTiles();
      expect(tiles.selectedTiles.length).toBe(0);
    });
  });

  describe('mouseDown', function() {

    describe('with multiSelect = false', function() {

      it('on a deselected tile selects the tile', function() {
        var tiles = createTiles(3, {
          selectable: true
        });
        tiles.render();
        var tile0 = tiles.tiles[0];

        tile0.$container.triggerMouseDown();
        expect(tile0.selected).toBe(true);
        expect(tiles.selectedTiles.length).toBe(1);
      });

      it('on a deselected tile selects the tile and unselects others', function() {
        var tiles = createTiles(3, {
          selectable: true
        });
        tiles.render();
        var tile0 = tiles.tiles[0];
        var tile1 = tiles.tiles[1];
        tiles.selectTile(tile1);
        expect(tile1.selected).toBe(true);

        tile0.$container.triggerMouseDown();
        expect(tile0.selected).toBe(true);
        expect(tile1.selected).toBe(false);
        expect(tiles.selectedTiles.length).toBe(1);
      });

      it('on a selected tile deselects the tile', function() {
        var tiles = createTiles(3, {
          selectable: true
        });
        tiles.render();
        var tile0 = tiles.tiles[0];
        tiles.selectTile(tile0);
        expect(tile0.selected).toBe(true);

        tile0.$container.triggerMouseDown();
        expect(tile0.selected).toBe(false);
        expect(tiles.selectedTiles.length).toBe(0);
      });

    });

    describe('with multiSelect = true', function() {

      it('on a deselected tile selects the tile', function() {
        var tiles = createTiles(3, {
          selectable: true,
          multiSelect: true
        });
        tiles.render();
        var tile0 = tiles.tiles[0];

        tile0.$container.triggerMouseDown();
        expect(tile0.selected).toBe(true);
        expect(tiles.selectedTiles.length).toBe(1);
      });

      it('on a deselected tile selects the tile and unselects others', function() {
        var tiles = createTiles(3, {
          selectable: true,
          multiSelect: true
        });
        tiles.render();
        var tile0 = tiles.tiles[0];
        var tile1 = tiles.tiles[1];
        tiles.selectTile(tile1);
        expect(tile1.selected).toBe(true);

        tile0.$container.triggerMouseDown();
        expect(tile0.selected).toBe(true);
        expect(tile1.selected).toBe(false);
        expect(tiles.selectedTiles.length).toBe(1);
      });

      it('on a selected tile deselects the tile', function() {
        var tiles = createTiles(3, {
          selectable: true,
          multiSelect: true
        });
        tiles.render();
        var tile0 = tiles.tiles[0];
        tiles.selectTile(tile0);
        expect(tile0.selected).toBe(true);

        tile0.$container.triggerMouseDown();
        expect(tile0.selected).toBe(false);
        expect(tiles.selectedTiles.length).toBe(0);
      });

      it('on a selected tile keeps the selection but deselects others if other tiles are selected', function() {
        var tiles = createTiles(3, {
          selectable: true,
          multiSelect: true
        });
        tiles.render();
        var tile0 = tiles.tiles[0];
        var tile1 = tiles.tiles[1];
        tiles.selectTiles([tile0, tile1]);
        expect(tile0.selected).toBe(true);
        expect(tile1.selected).toBe(true);

        tile0.$container.triggerMouseDown();
        expect(tile0.selected).toBe(true);
        expect(tile1.selected).toBe(false);
        expect(tiles.selectedTiles.length).toBe(1);
      });

      describe('with CTRL pressed', function() {

        it('on a deselected tile adds the tile to the selection', function() {
          var tiles = createTiles(3, {
            selectable: true,
            multiSelect: true
          });
          tiles.render();
          var tile0 = tiles.tiles[0];
          var tile1 = tiles.tiles[1];
          tiles.selectTile(tile1);
          expect(tile0.selected).toBe(false);
          expect(tile1.selected).toBe(true);

          tile0.$container.triggerMouseDown({
            modifier: 'ctrl'
          });
          expect(tile0.selected).toBe(true);
          expect(tile1.selected).toBe(true);
          expect(tiles.selectedTiles.length).toBe(2);
        });

        it('on a selected tile removes the tile from the selection', function() {
          var tiles = createTiles(3, {
            selectable: true,
            multiSelect: true
          });
          tiles.render();
          var tile0 = tiles.tiles[0];
          var tile1 = tiles.tiles[1];
          tiles.selectTiles([tile0, tile1]);
          expect(tile0.selected).toBe(true);
          expect(tile1.selected).toBe(true);

          tile0.$container.triggerMouseDown({
            modifier: 'ctrl'
          });
          expect(tile0.selected).toBe(false);
          expect(tile1.selected).toBe(true);
          expect(tiles.selectedTiles.length).toBe(1);
        });

      });

    });

  });

  describe('filter', function() {

    it('filters the tiles according to the added filters', function() {
      var tiles = createTiles(3);
      expect(tiles.filteredTiles.length).toBe(3);

      var filter1 = {
        accept: function(tile) {
          return tile.label.indexOf('1') < 0;
        }
      };
      tiles.addFilter(filter1);
      tiles.filter();
      expect(tiles.filteredTiles.length).toBe(2);
      expect(tiles.filteredTiles[0]).toBe(tiles.tiles[0]);
      expect(tiles.filteredTiles[1]).toBe(tiles.tiles[2]);
      expect(tiles.tiles[0].filterAccepted).toBe(true);
      expect(tiles.tiles[1].filterAccepted).toBe(false);
      expect(tiles.tiles[2].filterAccepted).toBe(true);

      var filter2 = {
        accept: function(tile) {
          return tile.label.indexOf('2') < 0;
        }
      };
      tiles.addFilter(filter2);
      tiles.filter();
      expect(tiles.filteredTiles.length).toBe(1);
      expect(tiles.filteredTiles[0]).toBe(tiles.tiles[0]);
      expect(tiles.tiles[0].filterAccepted).toBe(true);
      expect(tiles.tiles[1].filterAccepted).toBe(false);
      expect(tiles.tiles[2].filterAccepted).toBe(false);

      tiles.removeFilter(filter1);
      tiles.filter();
      expect(tiles.filteredTiles.length).toBe(2);
      expect(tiles.filteredTiles[0]).toBe(tiles.tiles[0]);
      expect(tiles.filteredTiles[1]).toBe(tiles.tiles[1]);
      expect(tiles.tiles[0].filterAccepted).toBe(true);
      expect(tiles.tiles[1].filterAccepted).toBe(true);
      expect(tiles.tiles[2].filterAccepted).toBe(false);

      tiles.removeFilter(filter2);
      tiles.filter();
      expect(tiles.filteredTiles.length).toBe(3);
      expect(tiles.filters.length).toBe(0);
      expect(tiles.filteredTiles[0]).toBe(tiles.tiles[0]);
      expect(tiles.filteredTiles[1]).toBe(tiles.tiles[1]);
      expect(tiles.filteredTiles[2]).toBe(tiles.tiles[2]);
      expect(tiles.tiles[0].filterAccepted).toBe(true);
      expect(tiles.tiles[1].filterAccepted).toBe(true);
      expect(tiles.tiles[2].filterAccepted).toBe(true);

      // Add same first filter again
      tiles.addFilter(filter1);
      tiles.filter();
      expect(tiles.filteredTiles.length).toBe(2);
      expect(tiles.filteredTiles[0]).toBe(tiles.tiles[0]);
      expect(tiles.filteredTiles[1]).toBe(tiles.tiles[2]);
      expect(tiles.tiles[0].filterAccepted).toBe(true);
      expect(tiles.tiles[1].filterAccepted).toBe(false);
      expect(tiles.tiles[2].filterAccepted).toBe(true);
    });

    it('considers newly inserted tiles', function() {
      var tiles = createTiles(3);
      var tile3 = createTile({
        label: "Tile 3"
      });
      var tile4 = createTile({
        label: "Tile 4"
      });
      expect(tiles.tiles.length).toBe(3);

      var filter = {
        accept: function(tile) {
          // Accept tile 1 and 4 only
          return tile.label.indexOf('1') >= 0 || tile.label.indexOf('4') >= 0;
        }
      };
      tiles.addFilter(filter);
      tiles.filter();
      expect(tiles.filteredTiles.length).toBe(1);

      // Insert tile 3 which is not accepted -> still only tile 1 visible
      tiles.insertTiles(tile3);
      expect(tiles.tiles.length).toBe(4);
      expect(tiles.filteredTiles.length).toBe(1);

      // Insert tile 4 which is accepted -> tile 1 and 4 are visible
      tiles.insertTiles(tile4);
      expect(tiles.tiles.length).toBe(5);
      expect(tiles.filteredTiles.length).toBe(2);
      expect(tiles.filteredTiles[0]).toBe(tiles.tiles[1]);
      expect(tiles.filteredTiles[1]).toBe(tiles.tiles[4]);
    });

    it('deselects not accepted tiles', function() {
      var tiles = createTiles(3, {
        selectable: true
      });
      tiles.selectTiles([tiles.tiles[0], tiles.tiles[1]]);
      expect(tiles.filteredTiles.length).toBe(3);
      expect(tiles.selectedTiles.length).toBe(2);
      expect(tiles.selectedTiles[0]).toBe(tiles.tiles[0]);
      expect(tiles.selectedTiles[1]).toBe(tiles.tiles[1]);

      var filter = {
        accept: function(tile) {
          // Accept tile 1 only
          return tile.label.indexOf('1') >= 0;
        }
      };
      tiles.addFilter(filter);
      tiles.filter();
      expect(tiles.filteredTiles.length).toBe(1);
      expect(tiles.selectedTiles.length).toBe(1);
      expect(tiles.selectedTiles[0]).toBe(tiles.tiles[1]);
    });

    it('applies the filters initially, if there is one', function() {
      var tiles = createTiles(3, {
        filters: [{
          accept: function(tile) {
            // Accept tile 1 only
            return tile.label.indexOf('1') >= 0;
          }
        }]
      });
      expect(tiles.tiles.length).toBe(3);
      expect(tiles.filteredTiles.length).toBe(1);
      expect(tiles.filteredTiles[0]).toBe(tiles.tiles[1]);
    });

  });

});
