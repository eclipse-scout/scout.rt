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
import {Range, scout} from '../../src/index';

describe('VirtualTileGrid', () => {
  let session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  function createTileGrid(numTiles, model) {
    let tiles = [];
    for (let i = 0; i < numTiles; i++) {
      tiles.push({
        objectType: 'Tile',
        label: 'Tile ' + i
      });
    }
    let defaults = {
      parent: session.desktop,
      tiles: tiles,
      virtual: true,
      gridColumnCount: 2
    };
    model = $.extend({}, defaults, model);
    return scout.create('TileGrid', model);
  }

  function createTile(model) {
    let defaults = {
      parent: session.desktop
    };
    model = $.extend({}, defaults, model);
    return scout.create('Tile', model);
  }

  describe('virtual', () => {
    it('only renders the tiles in the view range, if true', () => {
      let tileGrid = createTileGrid(7, {
        viewRangeSize: 2
      });
      tileGrid.render();
      let $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(4);
      expect($tiles.eq(0).data('widget')).toBe(tileGrid.tiles[0]);
      expect($tiles.eq(1).data('widget')).toBe(tileGrid.tiles[1]);
      expect($tiles.eq(2).data('widget')).toBe(tileGrid.tiles[2]);
      expect($tiles.eq(3).data('widget')).toBe(tileGrid.tiles[3]);

      tileGrid._renderViewRange(new Range(1, 3));
      $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(4);
      expect($tiles.eq(0).data('widget')).toBe(tileGrid.tiles[2]);
      expect($tiles.eq(1).data('widget')).toBe(tileGrid.tiles[3]);
      expect($tiles.eq(2).data('widget')).toBe(tileGrid.tiles[4]);
      expect($tiles.eq(3).data('widget')).toBe(tileGrid.tiles[5]);

      tileGrid._renderViewRange(new Range(2, 4));
      $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(3);
      expect($tiles.eq(0).data('widget')).toBe(tileGrid.tiles[4]);
      expect($tiles.eq(1).data('widget')).toBe(tileGrid.tiles[5]);
      expect($tiles.eq(2).data('widget')).toBe(tileGrid.tiles[6]);
    });

    it('can be toggled dynamically', () => {
      let tileGrid = createTileGrid(7, {
        viewRangeSize: 2,
        virtual: false
      });
      tileGrid.render();
      let $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(7); // All tiles rendered

      tileGrid.virtualScrolling.calculateViewRangeSize = () => {
        // Is called when toggling virtual, cannot determined correctly in the specs -> always return 2
        return 2;
      };
      tileGrid.setVirtual(true);
      $tiles = tileGrid.$container.children('.tile');
      expect(tileGrid.viewRangeRendered.equals(new Range(0, 2))).toBe(true);
      expect($tiles.length).toBe(4); // Only first to rows rendered
      expect($tiles.eq(0).data('widget')).toBe(tileGrid.tiles[0]);
      expect($tiles.eq(1).data('widget')).toBe(tileGrid.tiles[1]);
      expect($tiles.eq(2).data('widget')).toBe(tileGrid.tiles[2]);
      expect($tiles.eq(3).data('widget')).toBe(tileGrid.tiles[3]);

      tileGrid.setVirtual(false);
      $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(7); // All rows rendered again
    });

    it('does nothing if all tiles are in the view port', () => {
      let tileGrid = createTileGrid(4, {
        viewRangeSize: 2,
        virtual: false
      });
      tileGrid.render();
      let $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(4);

      tileGrid.virtualScrolling.calculateViewRangeSize = () => {
        // Is called when toggling virtual, cannot determined correctly in the specs -> always return 2
        return 2;
      };
      tileGrid.setVirtual(true);
      $tiles = tileGrid.$container.children('.tile');
      expect(tileGrid.viewRangeRendered.equals(new Range(0, 2))).toBe(true);
      expect($tiles.length).toBe(4); // Still 4, all tiles are visible
      tileGrid.validateLayout(); // Enforce layout to check whether it runs without exceptions

      tileGrid.setVirtual(false);
      $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(4);
      tileGrid.validateLayout();
    });

    it('can be enabled even if tiles have been inserted', () => {
      let tileGrid = createTileGrid(2, {
        viewRangeSize: 2,
        virtual: false
      });
      tileGrid.render();
      let $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(2); // All tiles rendered

      // Insert new tiles, will be rendered because virtual is false
      tileGrid.insertTiles([createTile(), createTile(), createTile(), createTile()]);
      $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(6);

      tileGrid.virtualScrolling.calculateViewRangeSize = () => {
        // Is called when toggling virtual, cannot determined correctly in the specs -> always return 2
        return 2;
      };
      tileGrid.setVirtual(true);
      $tiles = tileGrid.$container.children('.tile');
      expect(tileGrid.viewRangeRendered.equals(new Range(0, 2))).toBe(true);
      expect($tiles.length).toBe(4); // Only first to rows rendered
      expect($tiles.eq(0).data('widget')).toBe(tileGrid.tiles[0]);
      expect($tiles.eq(1).data('widget')).toBe(tileGrid.tiles[1]);
      expect($tiles.eq(2).data('widget')).toBe(tileGrid.tiles[2]);
      expect($tiles.eq(3).data('widget')).toBe(tileGrid.tiles[3]);
    });

    it('removes tiles correctly when enabled even if a filter is active', () => {
      let tileGrid = createTileGrid(4, {
        viewRangeSize: 1,
        virtual: false
      });
      let filter = {
        accept: tile => {
          // Accept 0 and 1
          return tile.label.indexOf('0') >= 0 || tile.label.indexOf('1') >= 0;
        }
      };
      tileGrid.addFilter(filter);
      tileGrid.render();
      let $tiles = tileGrid.$container.children('.tile');
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect($tiles.length).toBe(4); // All tiles rendered

      tileGrid.virtualScrolling.calculateViewRangeSize = () => {
        // Is called when toggling virtual, cannot determined correctly in the specs -> always return 2
        return 2;
      };
      tileGrid.setVirtual(true);
      $tiles = tileGrid.$container.children('.tile');
      expect(tileGrid.viewRangeRendered.equals(new Range(0, 1))).toBe(true);
      expect($tiles.length).toBe(2); // Only first row has to be rendered
      expect($tiles.eq(0).data('widget')).toBe(tileGrid.filteredTiles[0]);
      expect($tiles.eq(1).data('widget')).toBe(tileGrid.filteredTiles[1]);
    });
  });

  describe('selectTiles', () => {
    it('selects the given tiles but renders the selection only for the tiles in the view range', () => {
      let tileGrid = createTileGrid(3, {
        selectable: true,
        viewRangeSize: 1
      });
      tileGrid.render();
      tileGrid.selectTiles([tileGrid.tiles[0], tileGrid.tiles[1], tileGrid.tiles[2]]);
      expect(tileGrid.selectedTiles.length).toBe(3);
      expect(tileGrid.selectedTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.selectedTiles[1]).toBe(tileGrid.tiles[1]);
      expect(tileGrid.selectedTiles[2]).toBe(tileGrid.tiles[2]);
      expect(tileGrid.tiles[0].selected).toBe(true);
      expect(tileGrid.tiles[1].selected).toBe(true);
      expect(tileGrid.tiles[2].selected).toBe(true);
      expect(tileGrid.$container.children('.tile.selected').length).toBe(2);
      expect(tileGrid.$container.children('.tile.selected').eq(0)[0]).toBe(tileGrid.selectedTiles[0].$container[0]);
      expect(tileGrid.$container.children('.tile.selected').eq(1)[0]).toBe(tileGrid.selectedTiles[1].$container[0]);

      // Scroll down
      tileGrid._renderViewRange(new Range(1, 2));
      expect(tileGrid.$container.children('.tile.selected').length).toBe(1);
      expect(tileGrid.$container.children('.tile.selected').eq(0)[0]).toBe(tileGrid.selectedTiles[2].$container[0]);

      // Scroll up again
      tileGrid._renderViewRange(new Range(0, 1));
      expect(tileGrid.$container.children('.tile.selected').length).toBe(2);
      expect(tileGrid.$container.children('.tile.selected').eq(0)[0]).toBe(tileGrid.selectedTiles[0].$container[0]);
      expect(tileGrid.$container.children('.tile.selected').eq(1)[0]).toBe(tileGrid.selectedTiles[1].$container[0]);
    });
  });

  describe('deselectTiles', () => {
    it('deselects the given tiles, no matter if they are in the view port or not', () => {
      let tileGrid = createTileGrid(3, {
        selectable: true,
        viewRangeSize: 1
      });
      tileGrid.render();
      tileGrid.selectAllTiles();
      expect(tileGrid.selectedTiles.length).toBe(3);
      expect(tileGrid.$container.children('.tile.selected').length).toBe(2);
      expect(tileGrid.$container.children('.tile.selected').eq(0)[0]).toBe(tileGrid.selectedTiles[0].$container[0]);
      expect(tileGrid.$container.children('.tile.selected').eq(1)[0]).toBe(tileGrid.selectedTiles[1].$container[0]);

      tileGrid.deselectTiles([tileGrid.tiles[1], tileGrid.tiles[2]]);
      expect(tileGrid.selectedTiles.length).toBe(1);
      expect(tileGrid.selectedTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.tiles[0].selected).toBe(true);
      expect(tileGrid.$container.children('.tile.selected').length).toBe(1);
      expect(tileGrid.$container.children('.tile.selected').eq(0)[0]).toBe(tileGrid.selectedTiles[0].$container[0]);
    });
  });

  describe('insertTiles', () => {
    it('inserts the given tiles and renders them if they are in the viewport', () => {
      let tileGrid = createTileGrid(0, {
        viewRangeSize: 1
      });
      tileGrid.render();
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      expect(tileGrid.tiles.length).toBe(0);
      expect(tileGrid.$container.children('.tile').length).toBe(0);

      tileGrid.insertTiles(tile0);
      expect(tileGrid.tiles.length).toBe(1);
      expect(tileGrid.tiles[0]).toBe(tile0);
      expect(tileGrid.$container.children('.tile').length).toBe(1);
      expect(tileGrid.$container.children('.tile').eq(0)[0]).toBe(tile0.$container[0]);

      tileGrid.insertTiles([tile1, tile2]);
      expect(tileGrid.tiles.length).toBe(3);
      expect(tileGrid.tiles[0]).toBe(tile0);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile2);
      expect(tileGrid.$container.children('.tile').length).toBe(2);
      expect(tileGrid.$container.children('.tile').eq(0)[0]).toBe(tile0.$container[0]);
      expect(tileGrid.$container.children('.tile').eq(1)[0]).toBe(tile1.$container[0]);
    });
  });

  describe('deleteTiles', () => {
    it('deletes the given tiles no matter if they are in the view port or not', () => {
      jasmine.clock().install();
      let tileGrid = createTileGrid(0, {
        viewRangeSize: 1
      });
      tileGrid.render();
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      tileGrid.insertTiles([tile0, tile1, tile2]);
      expect(tileGrid.tiles.length).toBe(3);
      expect(tileGrid.$container.children('.tile').length).toBe(2);
      expect(tileGrid.$container.children('.tile').eq(0)[0]).toBe(tile0.$container[0]);
      expect(tileGrid.$container.children('.tile').eq(1)[0]).toBe(tile1.$container[0]);

      tileGrid.deleteTiles(tile2);
      expect(tileGrid.tiles.length).toBe(2);
      expect(tileGrid.tiles[0]).toBe(tile0);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.$container.children('.tile').length).toBe(2);
      expect(tileGrid.$container.children('.tile').eq(0)[0]).toBe(tile0.$container[0]);
      expect(tileGrid.$container.children('.tile').eq(1)[0]).toBe(tile1.$container[0]);

      tileGrid.deleteTiles([tile0]);
      expect(tileGrid.tiles.length).toBe(1);
      expect(tileGrid.$container.children('.tile').length).toBe(2); // Still 2, remove animation not yet finished
      expect(tileGrid.$container.children('.tile').eq(0)[0]).toBe(tile0.$container[0]);
      expect(tileGrid.$container.children('.tile').eq(1)[0]).toBe(tile1.$container[0]);

      jasmine.clock().tick(); // animate-remove is added later
      expect(tileGrid.$container.children('.tile').eq(0)).toHaveClass('animate-remove');
      tile0._removeInternal();
      expect(tileGrid.$container.children('.tile').length).toBe(1);
      expect(tileGrid.$container.children('.tile').eq(0)[0]).toBe(tile1.$container[0]);

      tileGrid.deleteTiles([tile1]);
      tile1._removeInternal();
      expect(tileGrid.tiles.length).toBe(0);
      expect(tileGrid.$container.children('.tile').length).toBe(0);
      jasmine.clock().uninstall();
    });

    it('destroys the deleted tiles', () => {
      let tileGrid = createTileGrid(0, {
        animateTileRemoval: false,
        viewRangeSize: 1
      });
      let tile0 = createTile({
        parent: tileGrid
      });
      let tile1 = createTile({
        parent: tileGrid
      });
      let tile2 = createTile({
        parent: tileGrid
      });
      tileGrid.render();
      tileGrid.insertTiles([tile0, tile1, tile2]);
      expect(tile0.destroyed).toBe(false);
      expect(tile0.rendered).toBe(true);
      expect(tile1.destroyed).toBe(false);
      expect(tile1.rendered).toBe(true);
      expect(tile2.destroyed).toBe(false);
      expect(tile2.rendered).toBe(false);

      tileGrid.deleteTile(tile1);
      expect(tile1.destroyed).toBe(true);
      expect(tile1.rendered).toBe(false);

      tileGrid.deleteTiles([tile0, tile2]);
      expect(tile0.destroyed).toBe(true);
      expect(tile0.rendered).toBe(false);
      expect(tile2.destroyed).toBe(true);
      expect(tile2.rendered).toBe(false);
    });
  });

  describe('deleteAllTiles', () => {
    it('adds empty marker also if virtual is true', () => {
      let tileGrid = createTileGrid(0, {
        viewRangeSize: 1
      });
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      tileGrid.render();
      expect(tileGrid.$container).toHaveClass('empty');

      tileGrid.insertTiles([tile0, tile1, tile2]);
      expect(tileGrid.$container).not.toHaveClass('empty');

      tileGrid.deleteAllTiles();
      expect(tileGrid.$container).toHaveClass('empty');
    });
  });

  describe('setTiles', () => {

    it('applies the order of the new tiles to the rendered elements in the view range', () => {
      let tileGrid = createTileGrid(0, {
        viewRangeSize: 1
      });
      let tile0 = createTile();
      let tile1 = createTile();
      let tile2 = createTile();
      tileGrid.insertTiles([tile0, tile1, tile2]);
      tileGrid.render();
      let $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(2);
      expect($tiles.eq(0).data('widget')).toBe(tile0);
      expect($tiles.eq(1).data('widget')).toBe(tile1);

      tileGrid.setTiles([tile2, tile1, tile0]);
      $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(2);
      expect($tiles.eq(0).data('widget')).toBe(tile2);
      expect($tiles.eq(1).data('widget')).toBe(tile1);
      expect(tileGrid.tiles[0]).toBe(tile2);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile0);
      expect(tileGrid.filteredTiles[0]).toBe(tile2);
      expect(tileGrid.filteredTiles[1]).toBe(tile1);
      expect(tileGrid.filteredTiles[2]).toBe(tile0);
    });

    it('applies the order of the new tiles to the filteredTiles if a filter is active', () => {
      let tileGrid = createTileGrid(3, {
        viewRangeSize: 1
      });
      let tile0 = tileGrid.tiles[0];
      let tile1 = tileGrid.tiles[1];
      let tile2 = tileGrid.tiles[2];

      let filter = {
        accept: tile => tile.label.indexOf('1') < 0
      };
      tileGrid.addFilter(filter);

      tileGrid.render();
      let $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(2);
      expect($tiles.eq(0).data('widget')).toBe(tile0);
      expect($tiles.eq(1).data('widget')).toBe(tile2);
      expect(tileGrid.tiles[0]).toBe(tile0);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile2);
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tile0);
      expect(tileGrid.filteredTiles[1]).toBe(tile2);

      tileGrid.setTiles([tile2, tile1, tile0]);
      $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(2);
      expect($tiles.eq(0).data('widget')).toBe(tile2);
      expect($tiles.eq(1).data('widget')).toBe(tile0);
      expect(tileGrid.tiles[0]).toBe(tile2);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile0);
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tile2);
      expect(tileGrid.filteredTiles[1]).toBe(tile0);
    });

  });

  describe('sort', () => {

    it('reorders the DOM elements in the view range according to the new order', () => {
      let tileGrid = createTileGrid(0, {
        viewRangeSize: 1
      });
      let tile0 = createTile({
        label: 'a'
      });
      let tile1 = createTile({
        label: 'b'
      });
      let tile2 = createTile({
        label: 'c'
      });
      tileGrid.insertTiles([tile0, tile1]);

      tileGrid.setComparator((t0, t1) => {
        // desc
        return (t0.label < t1.label ? 1 : ((t0.label > t1.label) ? -1 : 0));
      });
      tileGrid.render();
      tileGrid.sort();
      expect(tileGrid.tiles[0]).toBe(tile1);
      expect(tileGrid.tiles[1]).toBe(tile0);
      expect(tileGrid.filteredTiles[0]).toBe(tile1);
      expect(tileGrid.filteredTiles[1]).toBe(tile0);
      let $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(2);
      expect($tiles.eq(0).data('widget')).toBe(tile1);
      expect($tiles.eq(1).data('widget')).toBe(tile0);

      tileGrid.insertTiles([tile2]);
      expect(tileGrid.tiles[0]).toBe(tile2);
      expect(tileGrid.tiles[1]).toBe(tile1);
      expect(tileGrid.tiles[2]).toBe(tile0);
      expect(tileGrid.filteredTiles[0]).toBe(tile2);
      expect(tileGrid.filteredTiles[1]).toBe(tile1);
      expect(tileGrid.filteredTiles[2]).toBe(tile0);
      $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(2);
      expect($tiles.eq(0).data('widget')).toBe(tile2);
      expect($tiles.eq(1).data('widget')).toBe(tile1);
    });

  });

  describe('mouseDown', () => {

    describe('with multiSelect = true', () => {

      describe('with CTRL pressed', () => {

        it('on a deselected tile adds the tile to the selection, even if the selection is not in the view range', () => {
          let tileGrid = createTileGrid(3, {
            selectable: true,
            multiSelect: true,
            viewRange: 1
          });
          tileGrid.render();
          let tile0 = tileGrid.tiles[0];
          let tile2 = tileGrid.tiles[2];
          tileGrid.selectTile(tile0);
          expect(tile0.selected).toBe(true);
          expect(tile2.selected).toBe(false);

          tileGrid._renderViewRange(new Range(1, 2));
          expect(tile0.rendered).toBe(false);
          tile2.$container.triggerMouseDown({
            modifier: 'ctrl'
          });
          expect(tile0.selected).toBe(true);
          expect(tile2.selected).toBe(true);
          expect(tileGrid.selectedTiles.length).toBe(2);
        });
      });
    });
  });

  describe('filter', () => {

    it('removes not accepted elements', () => {
      jasmine.clock().install();
      let tileGrid = createTileGrid(3, {
        viewRangeSize: 1
      });
      let tile0 = tileGrid.tiles[0];
      let tile1 = tileGrid.tiles[1];
      let tile2 = tileGrid.tiles[2];
      tileGrid.render();
      expect(tileGrid.filteredTiles.length).toBe(3);
      let $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(2);
      expect($tiles.eq(0).data('widget')).toBe(tile0);
      expect($tiles.eq(1).data('widget')).toBe(tile1);

      let filter1 = {
        accept: tile => tile.label.indexOf('1') < 0
      };
      tileGrid.addFilter(filter1);
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tile0);
      expect(tileGrid.filteredTiles[1]).toBe(tile2);

      jasmine.clock().tick(); // animate-invisible is added later
      expect($tiles.eq(1)).toHaveClass('animate-invisible');
      tile1._removeInternal();
      $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(2);
      expect($tiles.eq(0).data('widget')).toBe(tile0);
      expect($tiles.eq(1).data('widget')).toBe(tile2);

      let filter2 = {
        accept: tile => tile.label.indexOf('2') < 0
      };
      tileGrid.addFilter(filter2);
      expect(tileGrid.filteredTiles.length).toBe(1);
      expect(tileGrid.filteredTiles[0]).toBe(tile0);

      jasmine.clock().tick(); // animate-invisible is added later
      expect($tiles.eq(1)).toHaveClass('animate-invisible');
      tile2._removeInternal();
      $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(1);
      expect($tiles.eq(0).data('widget')).toBe(tile0);

      tileGrid.removeFilter(filter1);
      tileGrid.validateLayoutTree();
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.filteredTiles[1]).toBe(tileGrid.tiles[1]);
      $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(2);
      expect($tiles.eq(1)).toHaveClass('animate-visible');
      expect($tiles.eq(0).data('widget')).toBe(tile0);
      expect($tiles.eq(1).data('widget')).toBe(tile1);

      tileGrid.removeFilter(filter2);
      expect(tileGrid.filteredTiles.length).toBe(3);
      expect(tileGrid.filters.length).toBe(0);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[0]);
      expect(tileGrid.filteredTiles[1]).toBe(tileGrid.tiles[1]);
      expect(tileGrid.filteredTiles[2]).toBe(tileGrid.tiles[2]);
      $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(2);
      expect($tiles.eq(0).data('widget')).toBe(tile0);
      expect($tiles.eq(1).data('widget')).toBe(tile1);

      tileGrid._renderViewRange(new Range(1, 2));
      $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(1);
      expect($tiles.eq(0).data('widget')).toBe(tile2);
      jasmine.clock().uninstall();
    });

    it('considers newly inserted tiles', () => {
      let tileGrid = createTileGrid(3, {
        viewRangeSize: 1
      });
      let tile3 = createTile({
        label: 'Tile 3'
      });
      let tile4 = createTile({
        label: 'Tile 4'
      });
      expect(tileGrid.tiles.length).toBe(3);

      let filter = {
        accept: tile => {
          // Accept tile 1 and 4 only
          return tile.label.indexOf('1') >= 0 || tile.label.indexOf('4') >= 0;
        }
      };
      tileGrid.addFilter(filter);
      tileGrid.render();
      expect(tileGrid.filteredTiles.length).toBe(1);

      // Insert tile 3 which is not accepted -> still only tile 1 visible
      tileGrid.insertTiles(tile3);
      expect(tileGrid.tiles.length).toBe(4);
      expect(tileGrid.filteredTiles.length).toBe(1);
      let $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(1);
      expect($tiles.eq(0).data('widget')).toBe(tileGrid.tiles[1]);

      // Insert tile 4 which is accepted -> tile 1 and 4 are visible
      tileGrid.insertTiles(tile4);
      expect(tileGrid.tiles.length).toBe(5);
      expect(tileGrid.filteredTiles.length).toBe(2);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[1]);
      expect(tileGrid.filteredTiles[1]).toBe(tileGrid.tiles[4]);
      $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(2);
      expect($tiles.eq(0).data('widget')).toBe(tileGrid.tiles[1]);
      expect($tiles.eq(1).data('widget')).toBe(tileGrid.tiles[4]);
    });

    it('applies the filters initially, if there is one', () => {
      let tileGrid = createTileGrid(3, {
        viewRangeSize: 1,
        filters: [{
          accept: tile => {
            // Accept tile 1 only
            return tile.label.indexOf('1') >= 0;
          }
        }]
      });
      tileGrid.render();
      expect(tileGrid.tiles.length).toBe(3);
      expect(tileGrid.filteredTiles.length).toBe(1);
      expect(tileGrid.filteredTiles[0]).toBe(tileGrid.tiles[1]);
      let $tiles = tileGrid.$container.children('.tile');
      expect($tiles.length).toBe(1);
      expect($tiles.eq(0).data('widget')).toBe(tileGrid.tiles[1]);
    });

    it('applies the filters initially even if every tile is accepted', () => {
      let tileGrid = createTileGrid(3, {
        filters: [{
          accept: tile => {
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

    it('updates empty marker', () => {
      let tileGrid = createTileGrid(3, {
        viewRangeSize: 1
      });
      tileGrid.render();
      expect(tileGrid.$container).not.toHaveClass('empty');

      let filter = {
        accept: tile => {
          // Accept none
          return false;
        }
      };
      tileGrid.addFilter(filter);
      expect(tileGrid.$container).toHaveClass('empty');

      tileGrid.removeFilter(filter);
      expect(tileGrid.$container).not.toHaveClass('empty');
    });

    it('updates view port if filter changed while container was invisible and scroll parent not at y=0', () => {
      jasmine.clock().install();
      let tileGrid = createTileGrid(3, {
        viewRangeSize: 1,
        scrollable: false
      });
      let group = scout.create('Group', {
        parent: session.desktop,
        body: tileGrid
      });
      let accordion = scout.create('TileAccordion', {
        parent: session.desktop,
        groups: [group]
      });
      accordion.render();
      accordion.$container.cssTop(20); // Move away from 0 to reproduce the real case, see commit comment
      tileGrid.validateLayoutTree();
      expect(tileGrid.filteredTiles.length).toBe(3);
      expect(tileGrid.renderedTiles().length).toBe(2);
      expect(tileGrid.viewRangeRendered.size()).toBe(1);

      // Hide group
      group.setVisible(false);
      let filter = {
        // Accept none
        accept: () => false
      };
      tileGrid.addFilter(filter);
      jasmine.clock().tick(); // Ensure tiles are really removed
      tileGrid.validateLayoutTree();
      expect(tileGrid.filteredTiles.length).toBe(0);
      expect(tileGrid.renderedTiles().length).toBe(0);
      expect(tileGrid.viewRangeRendered.size()).toBe(0);

      // Make the filter accept all tiles -> ensure tiles will be rendered
      tileGrid.filters[0].accept = () => true;
      tileGrid.filter();
      jasmine.clock().tick();
      tileGrid.validateLayoutTree();
      expect(tileGrid.filteredTiles.length).toBe(3);
      expect(tileGrid.renderedTiles().length).toBe(2);
      expect(tileGrid.viewRangeRendered.size()).toBe(1);

      // Show group again -> Tiles are already rendered yet
      group.setVisible(true);
      tileGrid.validateLayoutTree();
      expect(tileGrid.filteredTiles.length).toBe(3);
      expect(tileGrid.renderedTiles().length).toBe(2);
      expect(tileGrid.viewRangeRendered.size()).toBe(1);
      jasmine.clock().uninstall();
    });
  });
});
