/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {scout} from '../../src/index';

describe('TileGrid', function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  afterEach(function() {
    // Stop all running animations to not influence other specs
    $(':animated').finish();
  });

  function createTileGrid(numTiles, model) {
    var tiles = [];
    for (var i = 0; i < numTiles; i++) {
      tiles.push({
        objectType: 'Tile',
        label: 'Tile ' + i
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

  describe('selectTiles', function() {
    it('selects the given tiles and unselects the previously selected ones', function() {
      var tileGrid = createTileGrid(3, {
        selectable: true
      });
      tileGrid.selectTiles(tileGrid.tiles[0]);
      expect(tileGrid.selectedTiles.length).toBe(1);
      expect(tileGrid.selectedTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.tiles[0].selected).toBe(true);
      expect(tileGrid.tiles[1].selected).toBe(false);
      expect(tileGrid.tiles[2].selected).toBe(false);

      tileGrid.selectTiles(tileGrid.tiles[1]);
      expect(tileGrid.selectedTiles.length).toBe(1);
      expect(tileGrid.selectedTiles[0]).toBe(tileGrid.tiles[1]);
      expect(tileGrid.tiles[0].selected).toBe(false);
      expect(tileGrid.tiles[1].selected).toBe(true);
      expect(tileGrid.tiles[2].selected).toBe(false);

      tileGrid.selectTiles([tileGrid.tiles[0], tileGrid.tiles[2]]);
      expect(tileGrid.selectedTiles.length).toBe(2);
      expect(tileGrid.selectedTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.selectedTiles[1]).toBe(tileGrid.tiles[2]);
      expect(tileGrid.tiles[0].selected).toBe(true);
      expect(tileGrid.tiles[1].selected).toBe(false);
      expect(tileGrid.tiles[2].selected).toBe(true);

      tileGrid.selectTiles([tileGrid.tiles[0], tileGrid.tiles[1], tileGrid.tiles[2]]);
      expect(tileGrid.selectedTiles.length).toBe(3);
      expect(tileGrid.selectedTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.selectedTiles[1]).toBe(tileGrid.tiles[1]);
      expect(tileGrid.selectedTiles[2]).toBe(tileGrid.tiles[2]);
      expect(tileGrid.tiles[0].selected).toBe(true);
      expect(tileGrid.tiles[1].selected).toBe(true);
      expect(tileGrid.tiles[2].selected).toBe(true);

      tileGrid.selectTiles([]);
      expect(tileGrid.selectedTiles.length).toBe(0);
      expect(tileGrid.tiles[0].selected).toBe(false);
      expect(tileGrid.tiles[1].selected).toBe(false);
      expect(tileGrid.tiles[2].selected).toBe(false);
    });

    it('does not select if selectable is false', function() {
      var tileGrid = createTileGrid(3, {
        selectable: false
      });
      tileGrid.selectTiles(tileGrid.tiles[0]);
      expect(tileGrid.tiles[0].selected).toBe(false);

      tileGrid.tiles[0].setSelected(true);
      expect(tileGrid.tiles[0].selected).toBe(false);
    });

    it('does not select tiles excluded by filter', function() {
      var tileGrid = createTileGrid(3, {
        selectable: true
      });

      var filter = {
        accept: function(tile) {
          return tile.label.indexOf('1') < 0;
        }
      };
      tileGrid.addFilter(filter);
      tileGrid.filter();

      tileGrid.selectTiles(tileGrid.tiles[1]);
      expect(tileGrid.tiles[1].selected).toBe(false);

      tileGrid.selectTiles(tileGrid.tiles[0]);
      expect(tileGrid.tiles[0].selected).toBe(true);

      tileGrid.removeFilter(filter);
      tileGrid.filter();

      tileGrid.selectTiles(tileGrid.tiles[1]);
      expect(tileGrid.tiles[1].selected).toBe(true);
    });

    it('triggers a property change event', function() {
      var tileGrid = createTileGrid(3, {
        selectable: true
      });
      var eventTriggered = false;
      tileGrid.on('propertyChange', function(event) {
        if (event.propertyName === 'selectedTiles') {
          eventTriggered = true;
        }
      });
      tileGrid.selectTiles(tileGrid.tiles[0]);
      expect(eventTriggered).toBe(true);
    });
  });

  describe('deselectTiles', function() {
    it('deselects the given tiles', function() {
      var tileGrid = createTileGrid(3, {
        selectable: true
      });
      tileGrid.selectAllTiles();
      expect(tileGrid.selectedTiles.length).toBe(3);
      expect(tileGrid.selectedTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.selectedTiles[1]).toBe(tileGrid.tiles[1]);
      expect(tileGrid.selectedTiles[2]).toBe(tileGrid.tiles[2]);
      expect(tileGrid.tiles[0].selected).toBe(true);
      expect(tileGrid.tiles[1].selected).toBe(true);
      expect(tileGrid.tiles[2].selected).toBe(true);

      tileGrid.deselectTiles(tileGrid.tiles[1]);
      expect(tileGrid.selectedTiles.length).toBe(2);
      expect(tileGrid.selectedTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.selectedTiles[1]).toBe(tileGrid.tiles[2]);
      expect(tileGrid.tiles[0].selected).toBe(true);
      expect(tileGrid.tiles[1].selected).toBe(false);
      expect(tileGrid.tiles[2].selected).toBe(true);

      tileGrid.deselectAllTiles();
      expect(tileGrid.selectedTiles.length).toBe(0);
      expect(tileGrid.tiles[0].selected).toBe(false);
      expect(tileGrid.tiles[1].selected).toBe(false);
      expect(tileGrid.tiles[2].selected).toBe(false);
    });

    it('triggers a property change event', function() {
      var tileGrid = createTileGrid(3, {
        selectable: true
      });
      var eventTriggered = false;
      tileGrid.selectAllTiles();
      tileGrid.on('propertyChange', function(event) {
        if (event.propertyName === 'selectedTiles') {
          eventTriggered = true;
        }
      });
      tileGrid.deselectTile(tileGrid.tiles[0]);
      expect(eventTriggered).toBe(true);
    });
  });

  describe('insertTiles', function() {
    it('inserts the given tiles', function() {
      var tileGrid = createTileGrid(0);
      var tile0 = createTile();
      var tile1 = createTile();
      var tile2 = createTile();
      expect(tileGrid.tiles.length).toBe(0);

      tileGrid.insertTiles(tile0);
      expect(tileGrid.tiles.length).toBe(1);
      expect(tileGrid.tiles[0]).toBe(tile0);

      tileGrid.insertTiles([tile1, tile2]);
      expect(tileGrid.tiles.length).toBe(3);
      expect(tileGrid.tiles[0]).toBe(tile0);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile2);
    });

    it('triggers a property change event', function() {
      var tileGrid = createTileGrid(0);
      var tile0 = createTile();
      var eventTriggered = false;
      tileGrid.on('propertyChange', function(event) {
        if (event.propertyName === 'tiles') {
          eventTriggered = true;
        }
      });
      tileGrid.insertTiles(tile0);
      expect(eventTriggered).toBe(true);
    });

    it('links the inserted tiles with the tileGrid', function() {
      var tileGrid = createTileGrid(0);
      var tile0 = createTile();
      var tile1 = createTile();
      var tile2 = createTile();
      expect(tileGrid.tiles.length).toBe(0);
      expect(tile0.parent).toBe(session.desktop);
      expect(tile1.parent).toBe(session.desktop);
      expect(tile2.parent).toBe(session.desktop);

      tileGrid.insertTile(tile0);
      expect(tile0.parent).toBe(tileGrid);

      tileGrid.insertTiles([tile1, tile2]);
      expect(tile1.parent).toBe(tileGrid);
      expect(tile2.parent).toBe(tileGrid);
    });
  });

  describe('deleteTiles', function() {
    it('deletes the given tiles', function() {
      var tileGrid = createTileGrid(0);
      var tile0 = createTile();
      var tile1 = createTile();
      var tile2 = createTile();
      tileGrid.insertTiles([tile0, tile1, tile2]);
      expect(tileGrid.tiles.length).toBe(3);

      tileGrid.deleteTiles(tile1);
      expect(tileGrid.tiles.length).toBe(2);
      expect(tileGrid.tiles[0]).toBe(tile0);
      expect(tileGrid.tiles[1]).toBe(tile2);

      tileGrid.deleteTiles([tile0, tile2]);
      expect(tileGrid.tiles.length).toBe(0);
    });

    it('deselects the deleted tiles', function() {
      var tileGrid = createTileGrid(0, {
        selectable: true
      });
      var tile0 = createTile();
      var tile1 = createTile();
      var tile2 = createTile();
      tileGrid.insertTiles([tile0, tile1, tile2]);
      expect(tileGrid.tiles.length).toBe(3);

      tileGrid.selectAllTiles();
      expect(tileGrid.selectedTiles.length).toBe(3);

      tileGrid.deleteTiles(tile1);
      expect(tileGrid.selectedTiles.length).toBe(2);
      expect(tileGrid.selectedTiles[0]).toBe(tile0);
      expect(tileGrid.selectedTiles[1]).toBe(tile2);

      tileGrid.deleteTiles([tile0, tile2]);
      expect(tileGrid.selectedTiles.length).toBe(0);
    });

    it('triggers a property change event', function() {
      var tileGrid = createTileGrid(3);
      var eventTriggered = false;
      tileGrid.on('propertyChange', function(event) {
        if (event.propertyName === 'tiles') {
          eventTriggered = true;
        }
      });
      tileGrid.deleteTiles(tileGrid.tiles[0]);
      expect(eventTriggered).toBe(true);
    });

    it('destroys the deleted tiles', function() {
      var tileGrid = createTileGrid(0, {
        animateTileRemoval: false
      });
      var tile0 = createTile({
        parent: tileGrid
      });
      var tile1 = createTile({
        parent: tileGrid
      });
      var tile2 = createTile({
        parent: tileGrid
      });
      tileGrid.render();
      tileGrid.insertTiles([tile0, tile1, tile2]);
      expect(tile0.destroyed).toBe(false);
      expect(tile0.rendered).toBe(true);
      expect(tile1.destroyed).toBe(false);
      expect(tile1.rendered).toBe(true);
      expect(tile2.destroyed).toBe(false);
      expect(tile2.rendered).toBe(true);

      tileGrid.deleteTile(tile1);
      expect(tile1.destroyed).toBe(true);
      expect(tile1.rendered).toBe(false);

      tileGrid.deleteTiles([tile0, tile2]);
      expect(tile0.destroyed).toBe(true);
      expect(tile0.rendered).toBe(false);
      expect(tile2.destroyed).toBe(true);
      expect(tile2.rendered).toBe(false);
    });

    /**
     * This spec is important if a tile should be moved from one tileGrid to another.
     */
    it('does not destroy the deleted tiles if the tileGrid is not the owner', function() {
      var tileGrid = createTileGrid(0, {
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
      tileGrid.render();
      tileGrid.insertTiles([tile0, tile1, tile2]);
      expect(tile0.destroyed).toBe(false);
      expect(tile0.rendered).toBe(true);
      expect(tile1.destroyed).toBe(false);
      expect(tile1.rendered).toBe(true);
      expect(tile2.destroyed).toBe(false);
      expect(tile2.rendered).toBe(true);

      tileGrid.deleteTile(tile1);
      expect(tile1.destroyed).toBe(false);
      expect(tile1.rendered).toBe(false);

      tileGrid.deleteTiles([tile0, tile2]);
      expect(tile0.destroyed).toBe(false);
      expect(tile0.rendered).toBe(false);
      expect(tile2.destroyed).toBe(false);
      expect(tile2.rendered).toBe(false);
    });
  });

  describe('deleteAllTiles', function() {
    it('deletes all tiles', function() {
      var tileGrid = createTileGrid(0);
      var tile0 = createTile();
      var tile1 = createTile();
      var tile2 = createTile();
      tileGrid.insertTiles([tile0, tile1, tile2]);
      expect(tileGrid.tiles.length).toBe(3);

      tileGrid.deleteAllTiles();
      expect(tileGrid.tiles.length).toBe(0);
    });

    it('deselects the deleted tiles', function() {
      var tileGrid = createTileGrid(0, {
        selectable: true
      });
      var tile0 = createTile();
      var tile1 = createTile();
      var tile2 = createTile();
      tileGrid.insertTiles([tile0, tile1, tile2]);
      expect(tileGrid.tiles.length).toBe(3);

      tileGrid.selectAllTiles();
      expect(tileGrid.selectedTiles.length).toBe(3);

      tileGrid.deleteAllTiles();
      expect(tileGrid.selectedTiles.length).toBe(0);
    });

    it('adds empty marker', function() {
      var tileGrid = createTileGrid(0);
      var tile0 = createTile();
      var tile1 = createTile();
      var tile2 = createTile();
      tileGrid.render();
      expect(tileGrid.$container).toHaveClass('empty');

      tileGrid.insertTiles([tile0, tile1, tile2]);
      expect(tileGrid.$container).not.toHaveClass('empty');

      tileGrid.deleteAllTiles();
      expect(tileGrid.$container).toHaveClass('empty');
    });
  });

  describe('setTiles', function() {

    it('applies the order of the new tiles to tiles and filteredTiles', function() {
      var tileGrid = createTileGrid(0);
      var tile0 = createTile();
      var tile1 = createTile();
      var tile2 = createTile();
      tileGrid.insertTiles([tile0, tile1, tile2]);

      tileGrid.setTiles([tile2, tile1, tile0]);
      expect(tileGrid.tiles[0]).toBe(tile2);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile0);
      expect(tileGrid.filteredTiles[0]).toBe(tile2);
      expect(tileGrid.filteredTiles[1]).toBe(tile1);
      expect(tileGrid.filteredTiles[2]).toBe(tile0);
    });

    it('applies the order of the new tiles to the rendered elements', function() {
      var tileGrid = createTileGrid(0);
      var tile0 = createTile();
      var tile1 = createTile();
      var tile2 = createTile();
      tileGrid.insertTiles([tile0, tile1, tile2]);
      tileGrid.render();
      var $tiles = tileGrid.$container.children('.tile');
      expect($tiles.eq(0).data('widget')).toBe(tile0);
      expect($tiles.eq(1).data('widget')).toBe(tile1);
      expect($tiles.eq(2).data('widget')).toBe(tile2);

      tileGrid.setTiles([tile2, tile1, tile0]);
      $tiles = tileGrid.$container.children('.tile');
      expect($tiles.eq(0).data('widget')).toBe(tile2);
      expect($tiles.eq(1).data('widget')).toBe(tile1);
      expect($tiles.eq(2).data('widget')).toBe(tile0);
      expect(tileGrid.tiles[0]).toBe(tile2);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile0);
      expect(tileGrid.filteredTiles[0]).toBe(tile2);
      expect(tileGrid.filteredTiles[1]).toBe(tile1);
      expect(tileGrid.filteredTiles[2]).toBe(tile0);
    });

    it('applies the order of the new tiles to the filteredTiles if a filter is active', function() {
      var tileGrid = createTileGrid(3);
      var tile0 = tileGrid.tiles[0];
      var tile1 = tileGrid.tiles[1];
      var tile2 = tileGrid.tiles[2];

      var filter = {
        accept: function(tile) {
          return tile.label.indexOf('1') < 0;
        }
      };
      tileGrid.addFilter(filter);
      tileGrid.filter();

      tileGrid.render();
      var $tiles = tileGrid.$container.children('.tile');
      expect($tiles.eq(0).data('widget')).toBe(tile0);
      expect($tiles.eq(1).data('widget')).toBe(tile1);
      expect($tiles.eq(2).data('widget')).toBe(tile2);
      expect(tileGrid.tiles[0]).toBe(tile0);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile2);
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tile0);
      expect(tileGrid.filteredTiles[1]).toBe(tile2);

      tileGrid.setTiles([tile2, tile1, tile0]);
      $tiles = tileGrid.$container.children('.tile');
      expect($tiles.eq(0).data('widget')).toBe(tile2);
      expect($tiles.eq(1).data('widget')).toBe(tile1);
      expect($tiles.eq(2).data('widget')).toBe(tile0);
      expect(tileGrid.tiles[0]).toBe(tile2);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile0);
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tile2);
      expect(tileGrid.filteredTiles[1]).toBe(tile0);
    });

  });

  describe('sort', function() {

    it('uses the comparator to sort the tiles and filteredTiles', function() {
      var tileGrid = createTileGrid(0);
      var tile0 = createTile({
        label: 'a'
      });
      var tile1 = createTile({
        label: 'b'
      });
      var tile2 = createTile({
        label: 'c'
      });
      tileGrid.insertTiles([tile0, tile1, tile2]);

      tileGrid.setComparator(function(t0, t1) {
        // desc
        return (t0.label < t1.label ? 1 : ((t0.label > t1.label) ? -1 : 0));
      });
      tileGrid.sort();
      expect(tileGrid.tiles[0]).toBe(tile2);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile0);
      expect(tileGrid.filteredTiles[0]).toBe(tile2);
      expect(tileGrid.filteredTiles[1]).toBe(tile1);
      expect(tileGrid.filteredTiles[2]).toBe(tile0);

      tileGrid.setComparator(function(t0, t1) {
        // asc
        return (t0.label < t1.label ? -1 : ((t0.label > t1.label) ? 1 : 0));
      });
      tileGrid.sort();
      expect(tileGrid.tiles[0]).toBe(tile0);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile2);
      expect(tileGrid.filteredTiles[0]).toBe(tile0);
      expect(tileGrid.filteredTiles[1]).toBe(tile1);
      expect(tileGrid.filteredTiles[2]).toBe(tile2);
    });

    it('is executed when new tiles are added', function() {
      var tileGrid = createTileGrid(0);
      var tile0 = createTile({
        label: 'a'
      });
      var tile1 = createTile({
        label: 'b'
      });
      var tile2 = createTile({
        label: 'c'
      });
      tileGrid.insertTiles([tile0, tile1]);

      tileGrid.setComparator(function(t0, t1) {
        // desc
        return (t0.label < t1.label ? 1 : ((t0.label > t1.label) ? -1 : 0));
      });
      tileGrid.sort();
      expect(tileGrid.tiles[0]).toBe(tile1);
      expect(tileGrid.tiles[1]).toBe(tile0);
      expect(tileGrid.filteredTiles[0]).toBe(tile1);
      expect(tileGrid.filteredTiles[1]).toBe(tile0);

      tileGrid.insertTiles([tile2]);
      expect(tileGrid.tiles[0]).toBe(tile2);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile0);
      expect(tileGrid.filteredTiles[0]).toBe(tile2);
      expect(tileGrid.filteredTiles[1]).toBe(tile1);
      expect(tileGrid.filteredTiles[2]).toBe(tile0);
    });

    it('reorders the DOM elements accordingly', function() {
      var tileGrid = createTileGrid(0);
      var tile0 = createTile({
        label: 'a'
      });
      var tile1 = createTile({
        label: 'b'
      });
      var tile2 = createTile({
        label: 'c'
      });
      tileGrid.insertTiles([tile0, tile1, tile2]);

      tileGrid.setComparator(function(t0, t1) {
        // desc
        return (t0.label < t1.label ? 1 : ((t0.label > t1.label) ? -1 : 0));
      });
      tileGrid.render();
      tileGrid.sort();
      expect(tileGrid.tiles[0]).toBe(tile2);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile0);
      expect(tileGrid.filteredTiles[0]).toBe(tile2);
      expect(tileGrid.filteredTiles[1]).toBe(tile1);
      expect(tileGrid.filteredTiles[2]).toBe(tile0);
      var $tiles = tileGrid.$container.children('.tile');
      expect($tiles.eq(0).data('widget')).toBe(tile2);
      expect($tiles.eq(1).data('widget')).toBe(tile1);
      expect($tiles.eq(2).data('widget')).toBe(tile0);
    });

  });

  describe('mouseDown', function() {

    describe('with multiSelect = false', function() {

      it('on a deselected tile selects the tile', function() {
        var tileGrid = createTileGrid(3, {
          selectable: true
        });
        tileGrid.render();
        var tile0 = tileGrid.tiles[0];

        tile0.$container.triggerMouseDown();
        expect(tile0.selected).toBe(true);
        expect(tileGrid.selectedTiles.length).toBe(1);
      });

      it('on a deselected tile selects the tile and unselects others', function() {
        var tileGrid = createTileGrid(3, {
          selectable: true
        });
        tileGrid.render();
        var tile0 = tileGrid.tiles[0];
        var tile1 = tileGrid.tiles[1];
        tileGrid.selectTile(tile1);
        expect(tile1.selected).toBe(true);

        tile0.$container.triggerMouseDown();
        expect(tile0.selected).toBe(true);
        expect(tile1.selected).toBe(false);
        expect(tileGrid.selectedTiles.length).toBe(1);
      });

      it('on a selected tile does nothing', function() {
        var tileGrid = createTileGrid(3, {
          selectable: true
        });
        tileGrid.render();
        var tile0 = tileGrid.tiles[0];
        tileGrid.selectTile(tile0);
        expect(tile0.selected).toBe(true);

        var eventTriggered = false;
        tileGrid.on('propertyChange', function(event) {
          if (event.propertyName === 'selectedTiles') {
            eventTriggered = true;
          }
        });

        tile0.$container.triggerMouseDown();
        expect(tile0.selected).toBe(true);
        expect(tileGrid.selectedTiles.length).toBe(1);
        expect(eventTriggered).toBe(false);
      });

      it('sets focusedTile property to clicked tile when selected', function() {
        var tileGrid = createTileGrid(3, {
          selectable: true
        });
        tileGrid.render();
        var tile0 = tileGrid.tiles[0];

        tile0.$container.triggerMouseDown();
        expect(tile0.selected).toBe(true);
        expect(tileGrid.focusedTile).toBe(tile0);
      });

    });

    describe('with multiSelect = true', function() {

      it('on a deselected tile selects the tile', function() {
        var tileGrid = createTileGrid(3, {
          selectable: true,
          multiSelect: true
        });
        tileGrid.render();
        var tile0 = tileGrid.tiles[0];

        tile0.$container.triggerMouseDown();
        expect(tile0.selected).toBe(true);
        expect(tileGrid.selectedTiles.length).toBe(1);
      });

      it('on a deselected tile selects the tile and unselects others', function() {
        var tileGrid = createTileGrid(3, {
          selectable: true,
          multiSelect: true
        });
        tileGrid.render();
        var tile0 = tileGrid.tiles[0];
        var tile1 = tileGrid.tiles[1];
        tileGrid.selectTile(tile1);
        expect(tile1.selected).toBe(true);

        tile0.$container.triggerMouseDown();
        expect(tile0.selected).toBe(true);
        expect(tile1.selected).toBe(false);
        expect(tileGrid.selectedTiles.length).toBe(1);
      });

      it('on a selected tile does nothing', function() {
        var tileGrid = createTileGrid(3, {
          selectable: true,
          multiSelect: true
        });
        tileGrid.render();
        var tile0 = tileGrid.tiles[0];
        tileGrid.selectTile(tile0);
        expect(tile0.selected).toBe(true);

        var eventTriggered = false;
        tileGrid.on('propertyChange', function(event) {
          if (event.propertyName === 'selectedTiles') {
            eventTriggered = true;
          }
        });

        tile0.$container.triggerMouseDown();
        expect(tile0.selected).toBe(true);
        expect(tileGrid.selectedTiles.length).toBe(1);
        expect(eventTriggered).toBe(false);
      });

      it('on a selected tile keeps the selection but deselects others if other tiles are selected', function() {
        var tileGrid = createTileGrid(3, {
          selectable: true,
          multiSelect: true
        });
        tileGrid.render();
        var tile0 = tileGrid.tiles[0];
        var tile1 = tileGrid.tiles[1];
        tileGrid.selectTiles([tile0, tile1]);
        expect(tile0.selected).toBe(true);
        expect(tile1.selected).toBe(true);

        tile0.$container.triggerMouseDown();
        expect(tile0.selected).toBe(true);
        expect(tile1.selected).toBe(false);
        expect(tileGrid.selectedTiles.length).toBe(1);
      });

      describe('with CTRL pressed', function() {

        it('on a deselected tile adds the tile to the selection', function() {
          var tileGrid = createTileGrid(3, {
            selectable: true,
            multiSelect: true
          });
          tileGrid.render();
          var tile0 = tileGrid.tiles[0];
          var tile1 = tileGrid.tiles[1];
          tileGrid.selectTile(tile1);
          expect(tile0.selected).toBe(false);
          expect(tile1.selected).toBe(true);

          tile0.$container.triggerMouseDown({
            modifier: 'ctrl'
          });
          expect(tile0.selected).toBe(true);
          expect(tile1.selected).toBe(true);
          expect(tileGrid.selectedTiles.length).toBe(2);
        });

        it('on a selected tile removes the tile from the selection', function() {
          var tileGrid = createTileGrid(3, {
            selectable: true,
            multiSelect: true
          });
          tileGrid.render();
          var tile0 = tileGrid.tiles[0];
          var tile1 = tileGrid.tiles[1];
          tileGrid.selectTiles([tile0, tile1]);
          expect(tile0.selected).toBe(true);
          expect(tile1.selected).toBe(true);

          tile0.$container.triggerMouseDown({
            modifier: 'ctrl'
          });
          expect(tile0.selected).toBe(false);
          expect(tile1.selected).toBe(true);
          expect(tileGrid.selectedTiles.length).toBe(1);
        });

        it('sets focusedTile property to null when when clicked tile is unselected', function() {
          var tileGrid = createTileGrid(3, {
            selectable: true
          });
          tileGrid.render();
          var tile0 = tileGrid.tiles[0];
          tileGrid.selectTile(tile0);
          expect(tile0.selected).toBe(true);

          tile0.$container.triggerMouseDown({
            modifier: 'ctrl'
          });
          expect(tile0.selected).toBe(false);
          expect(tileGrid.focusedTile).toBe(null);
        });

      });

    });

  });

  describe('click', function() {
    it('triggers tileClick', function() {
      var tileGrid = createTileGrid(3, {
        selectable: false
      });
      tileGrid.render();
      var tile0 = tileGrid.tiles[0];
      var clickEventCount = 0;
      tileGrid.on('tileClick', function(event) {
        if (event.tile === tile0 && event.mouseButton === 1) {
          clickEventCount++;
        }
      });
      expect(tile0.selected).toBe(false);
      expect(clickEventCount).toBe(0);

      tile0.$container.triggerClick();
      expect(tile0.selected).toBe(false);
      expect(clickEventCount).toBe(1);
    });

    it('triggers tileSelected and tileClick if selectable', function() {
      var tileGrid = createTileGrid(3, {
        selectable: true
      });
      tileGrid.render();
      var tile0 = tileGrid.tiles[0];
      var clickEventCount = 0;
      var selectEventCount = 0;
      var events = [];
      tileGrid.on('propertyChange', function(event) {
        if (event.propertyName === 'selectedTiles') {
          selectEventCount++;
        }
        events.push('select');
      });
      tileGrid.on('tileClick', function(event) {
        if (event.tile === tile0 && event.mouseButton === 1) {
          clickEventCount++;
        }
        events.push('click');
      });
      expect(tile0.selected).toBe(false);
      expect(selectEventCount).toBe(0);
      expect(clickEventCount).toBe(0);

      tile0.$container.triggerClick();
      expect(tile0.selected).toBe(true);
      expect(selectEventCount).toBe(1);
      expect(clickEventCount).toBe(1);
      expect(events.length).toBe(2);
      expect(events[0]).toBe('select');
      expect(events[1]).toBe('click');
    });

    it('triggers tileAction when clicked twice', function() {
      var tileGrid = createTileGrid(3, {
        selectable: true
      });
      tileGrid.render();
      var tile0 = tileGrid.tiles[0];
      var selectEventCount = 0;
      var clickEventCount = 0;
      var actionEventCount = 0;
      var events = [];
      tileGrid.on('propertyChange', function(event) {
        if (event.propertyName === 'selectedTiles') {
          selectEventCount++;
        }
        events.push('select');
      });
      tileGrid.on('tileClick', function(event) {
        if (event.tile === tile0) {
          clickEventCount++;
        }
        events.push('click');
      });
      tileGrid.on('tileAction', function(event) {
        if (event.tile === tile0) {
          actionEventCount++;
        }
        events.push('action');
      });
      expect(tile0.selected).toBe(false);
      expect(selectEventCount).toBe(0);
      expect(clickEventCount).toBe(0);
      expect(actionEventCount).toBe(0);

      tile0.$container.triggerDoubleClick();
      expect(tile0.selected).toBe(true);
      expect(selectEventCount).toBe(1);
      expect(clickEventCount).toBe(1);
      expect(actionEventCount).toBe(1);
      expect(events.length).toBe(3);
      expect(events[0]).toBe('select');
      expect(events[1]).toBe('click');
      expect(events[2]).toBe('action');
    });
  });

  describe('filter', function() {

    it('filters the tiles according to the added filters', function() {
      var tileGrid = createTileGrid(3);
      expect(tileGrid.filteredTiles.length).toBe(3);

      var filter1 = {
        accept: function(tile) {
          return tile.label.indexOf('1') < 0;
        }
      };
      tileGrid.addFilter(filter1);
      tileGrid.filter();
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.filteredTiles[1]).toBe(tileGrid.tiles[2]);
      expect(tileGrid.tiles[0].filterAccepted).toBe(true);
      expect(tileGrid.tiles[1].filterAccepted).toBe(false);
      expect(tileGrid.tiles[2].filterAccepted).toBe(true);

      var filter2 = {
        accept: function(tile) {
          return tile.label.indexOf('2') < 0;
        }
      };
      tileGrid.addFilter(filter2);
      tileGrid.filter();
      expect(tileGrid.filteredTiles.length).toBe(1);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.tiles[0].filterAccepted).toBe(true);
      expect(tileGrid.tiles[1].filterAccepted).toBe(false);
      expect(tileGrid.tiles[2].filterAccepted).toBe(false);

      tileGrid.removeFilter(filter1);
      tileGrid.filter();
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.filteredTiles[1]).toBe(tileGrid.tiles[1]);
      expect(tileGrid.tiles[0].filterAccepted).toBe(true);
      expect(tileGrid.tiles[1].filterAccepted).toBe(true);
      expect(tileGrid.tiles[2].filterAccepted).toBe(false);

      tileGrid.removeFilter(filter2);
      tileGrid.filter();
      expect(tileGrid.filteredTiles.length).toBe(3);
      expect(tileGrid.filters.length).toBe(0);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.filteredTiles[1]).toBe(tileGrid.tiles[1]);
      expect(tileGrid.filteredTiles[2]).toBe(tileGrid.tiles[2]);
      expect(tileGrid.tiles[0].filterAccepted).toBe(true);
      expect(tileGrid.tiles[1].filterAccepted).toBe(true);
      expect(tileGrid.tiles[2].filterAccepted).toBe(true);

      // Add same first filter again
      tileGrid.addFilter(filter1);
      tileGrid.filter();
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.filteredTiles[1]).toBe(tileGrid.tiles[2]);
      expect(tileGrid.tiles[0].filterAccepted).toBe(true);
      expect(tileGrid.tiles[1].filterAccepted).toBe(false);
      expect(tileGrid.tiles[2].filterAccepted).toBe(true);
    });

    it('considers newly inserted tiles', function() {
      var tileGrid = createTileGrid(3);
      var tile3 = createTile({
        label: 'Tile 3'
      });
      var tile4 = createTile({
        label: 'Tile 4'
      });
      expect(tileGrid.tiles.length).toBe(3);

      var filter = {
        accept: function(tile) {
          // Accept tile 1 and 4 only
          return tile.label.indexOf('1') >= 0 || tile.label.indexOf('4') >= 0;
        }
      };
      tileGrid.addFilter(filter);
      tileGrid.filter();
      expect(tileGrid.filteredTiles.length).toBe(1);

      // Insert tile 3 which is not accepted -> still only tile 1 visible
      tileGrid.insertTiles(tile3);
      expect(tileGrid.tiles.length).toBe(4);
      expect(tileGrid.filteredTiles.length).toBe(1);

      // Insert tile 4 which is accepted -> tile 1 and 4 are visible
      tileGrid.insertTiles(tile4);
      expect(tileGrid.tiles.length).toBe(5);
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[1]);
      expect(tileGrid.filteredTiles[1]).toBe(tileGrid.tiles[4]);
    });

    it('deselects not accepted tiles', function() {
      var tileGrid = createTileGrid(3, {
        selectable: true
      });
      tileGrid.selectTiles([tileGrid.tiles[0], tileGrid.tiles[1]]);
      expect(tileGrid.filteredTiles.length).toBe(3);
      expect(tileGrid.selectedTiles.length).toBe(2);
      expect(tileGrid.selectedTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.selectedTiles[1]).toBe(tileGrid.tiles[1]);

      var filter = {
        accept: function(tile) {
          // Accept tile 1 only
          return tile.label.indexOf('1') >= 0;
        }
      };
      tileGrid.addFilter(filter);
      tileGrid.filter();
      expect(tileGrid.filteredTiles.length).toBe(1);
      expect(tileGrid.selectedTiles.length).toBe(1);
      expect(tileGrid.selectedTiles[0]).toBe(tileGrid.tiles[1]);
    });

    it('applies the filters initially, if there is one', function() {
      var tileGrid = createTileGrid(3, {
        filters: [{
          accept: function(tile) {
            // Accept tile 1 only
            return tile.label.indexOf('1') >= 0;
          }
        }]
      });
      expect(tileGrid.tiles.length).toBe(3);
      expect(tileGrid.filteredTiles.length).toBe(1);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[1]);
    });

    it('applies the filters initially even if every tile is accepted', function() {
      var tileGrid = createTileGrid(3, {
        filters: [{
          accept: function(tile) {
            // Accept all
            return true;
          }
        }]
      });
      expect(tileGrid.tiles.length).toBe(3);
      expect(tileGrid.filteredTiles.length).toBe(3);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.filteredTiles[1]).toBe(tileGrid.tiles[1]);
      expect(tileGrid.filteredTiles[2]).toBe(tileGrid.tiles[2]);
    });

    it('updates empty marker', function() {
      var tileGrid = createTileGrid(3);
      tileGrid.render();
      expect(tileGrid.$container).not.toHaveClass('empty');

      var filter = {
        accept: function(tile) {
          // Accept none
          return false;
        }
      };
      tileGrid.addFilter(filter);
      tileGrid.filter();
      expect(tileGrid.$container).toHaveClass('empty');

      tileGrid.removeFilter(filter);
      tileGrid.filter();
      expect(tileGrid.$container).not.toHaveClass('empty');
    });

    it('still works if moved from one grid to anoter', function() {
      var tileGrid = createTileGrid();
      var tile0 = createTile({
        owner: session.desktop,
        label: 'Tile 0'
      });
      var tile1 = createTile({
        owner: session.desktop,
        label: 'Tile 1'
      });
      var tile2 = createTile({
        owner: session.desktop,
        label: 'Tile 2'
      });

      var filter1 = {
        accept: function(tile) {
          return tile.label.indexOf('1') < 0;
        }
      };
      tileGrid.setTiles([tile0, tile1, tile2]);
      tileGrid.addFilter(filter1);
      tileGrid.filter();
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tile0);
      expect(tileGrid.filteredTiles[1]).toBe(tile2);
      expect(tileGrid.tiles[0].filterAccepted).toBe(true);
      expect(tileGrid.tiles[1].filterAccepted).toBe(false);
      expect(tileGrid.tiles[2].filterAccepted).toBe(true);

      tileGrid.deleteAllTiles();
      expect(tileGrid.filteredTiles.length).toBe(0);

      var tileGrid2 = createTileGrid(3);
      tileGrid2.setTiles([tile0, tile1, tile2]);
      tileGrid2.addFilter(filter1);
      tileGrid2.filter();
      expect(tileGrid2.filteredTiles.length).toBe(2);
      expect(tileGrid2.filteredTiles[0]).toBe(tile0);
      expect(tileGrid2.filteredTiles[1]).toBe(tile2);
      expect(tileGrid2.tiles[0].filterAccepted).toBe(true);
      expect(tileGrid2.tiles[1].filterAccepted).toBe(false);
      expect(tileGrid2.tiles[2].filterAccepted).toBe(true);
    });

  });

  describe('addFilters', function() {

    it('adds the given filters', function() {
      var tileGrid = createTileGrid(3);

      var filter0 = {
        accept: function(tile) {
          return tile.label.indexOf('0') < 0;
        }
      };
      tileGrid.addFilters([filter0]);
      expect(tileGrid.filters.length).toBe(1);
      expect(tileGrid.filters).toEqual([filter0]);

      var filter1 = {
        accept: function(tile) {
          return tile.label.indexOf('1') < 0;
        }
      };
      var filter2 = {
        accept: function(tile) {
          return tile.label.indexOf('2') < 0;
        }
      };
      tileGrid.addFilters([filter1, filter2]);
      expect(tileGrid.filters.length).toBe(3);
      expect(tileGrid.filters).toEqual([filter0, filter1, filter2]);

      // Does nothing if same filters are added again
      tileGrid.addFilters([filter1, filter2]);
      expect(tileGrid.filters.length).toBe(3);
      expect(tileGrid.filters).toEqual([filter0, filter1, filter2]);
    });

  });

  describe('removeFilters', function() {

    it('invalidates the logical grid', function() {
      var model = {
        parent: session.desktop,
        objectType: 'Group',
        body: {
          objectType: 'TileGrid',
          tiles: []
        }
      };
      var group = scout.create(model);
      var tileGrid = group.body;
      var tileFilter = scout.create('RemoteTileFilter');
      tileFilter.setTileIds([4, 5, 6]);
      tileGrid.addFilter(tileFilter);
      group.render();
      tileGrid.setTiles([{
        objectType: 'Tile',
        label: 'Tile 1"',
        id: 1
      }, {
        objectType: 'Tile',
        label: 'Tile 2',
        id: 2
      }, {
        objectType: 'Tile',
        label: 'Tile 3',
        id: 3
      }]);

      expect(tileGrid.filters.length).toBe(1);
      expect(tileGrid.filteredTiles.length).toBe(0);

      tileGrid.removeFilter(tileFilter);
      group.bodyAnimating = true; // simulate existing animation
      tileGrid.filter();
      expect(tileGrid.filters.length).toBe(0);
      expect(tileGrid.filteredTiles.length).toBe(3);
    });

    it('removes the given filters', function() {
      var tileGrid = createTileGrid(3);

      var filter0 = {
        accept: function(tile) {
          return tile.label.indexOf('0') < 0;
        }
      };
      var filter1 = {
        accept: function(tile) {
          return tile.label.indexOf('1') < 0;
        }
      };
      var filter2 = {
        accept: function(tile) {
          return tile.label.indexOf('2') < 0;
        }
      };
      tileGrid.setFilters([filter0, filter1, filter2]);
      expect(tileGrid.filters.length).toBe(3);
      expect(tileGrid.filters).toEqual([filter0, filter1, filter2]);

      tileGrid.removeFilters([filter1, filter2]);
      expect(tileGrid.filters.length).toBe(1);
      expect(tileGrid.filters).toEqual([filter0]);

      tileGrid.removeFilters([filter0, filter1]);
      expect(tileGrid.filters.length).toBe(0);
      expect(tileGrid.filters).toEqual([]);
    });

  });

});
