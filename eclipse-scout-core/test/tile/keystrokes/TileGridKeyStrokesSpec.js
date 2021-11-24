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
import {arrays, keys, scout} from '../../../src/index';

describe('TileGridKeyStrokes', () => {
  let session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    // Set a fixed width to prevent tiles from wrapping on small screens (e.g. PhantomJS)
    $('<style>' +
      '.tile-grid {position: relative; border: 1px dotted black; padding: 5px; width: 1000px;}' +
      '.tile {position: absolute; border: 1px solid black;}' +
      '.tile.selected {border-color: blue;}' +
      '.scrollbar {position: absolute;}' +
      '</style>').appendTo($('#sandbox'));
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
      tiles: tiles
    };
    model = $.extend({}, defaults, model);
    return scout.create('TileGrid', model);
  }

  describe('ctrl + a', () => {
    it('selects all tiles', () => {
      let tileGrid = createTileGrid(3, {
        selectable: true
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();

      tileGrid.$container.triggerKeyDownCapture(keys.A, 'ctrl');
      expect(tileGrid.selectedTiles.length).toBe(3);
      tileGrid.$container.triggerKeyUpCapture(keys.A, 'ctrl');

      tileGrid.selectTile(tiles[0]);
      expect(tileGrid.selectedTiles.length).toBe(1);

      tileGrid.$container.triggerKeyDownCapture(keys.A, 'ctrl');
      expect(tileGrid.selectedTiles.length).toBe(3);
      tileGrid.$container.triggerKeyUpCapture(keys.A, 'ctrl');
    });

    it('deselects all tiles if tiles are already selected', () => {
      let tileGrid = createTileGrid(3, {
        selectable: true
      });
      tileGrid.render();
      tileGrid.validateLayout();
      tileGrid.selectAllTiles();

      tileGrid.$container.triggerKeyDownCapture(keys.A, 'ctrl');
      expect(tileGrid.selectedTiles.length).toBe(0);
      tileGrid.$container.triggerKeyUpCapture(keys.A, 'ctrl');

      tileGrid.$container.triggerKeyDownCapture(keys.A, 'ctrl');
      expect(tileGrid.selectedTiles.length).toBe(3);
      tileGrid.$container.triggerKeyUpCapture(keys.A, 'ctrl');

      tileGrid.$container.triggerKeyDownCapture(keys.A, 'ctrl');
      expect(tileGrid.selectedTiles.length).toBe(0);
      tileGrid.$container.triggerKeyUpCapture(keys.A, 'ctrl');
    });

  });

  describe('key right', () => {
    it('selects the next tile', () => {
      let tileGrid = createTileGrid(4, {
        selectable: true
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();
      tileGrid.selectTile(tiles[1]);

      tileGrid.$container.triggerKeyDownCapture(keys.RIGHT);
      expect(tileGrid.selectedTiles).toEqual([tiles[2]]);
      tileGrid.$container.triggerKeyUpCapture(keys.RIGHT);

      tileGrid.$container.triggerKeyDownCapture(keys.RIGHT);
      expect(tileGrid.selectedTiles).toEqual([tiles[3]]);
      tileGrid.$container.triggerKeyUpCapture(keys.RIGHT);
    });

    it('selects the first tile if no tile is selected yet', () => {
      let tileGrid = createTileGrid(4, {
        selectable: true
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();

      tileGrid.$container.triggerKeyDownCapture(keys.RIGHT);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.RIGHT);
    });

    it('does nothing if the last tile is already selected', () => {
      let tileGrid = createTileGrid(4, {
        selectable: true
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();
      tileGrid.selectTile(arrays.last(tiles));

      tileGrid.$container.triggerKeyDownCapture(keys.RIGHT);
      expect(tileGrid.selectedTiles).toEqual([arrays.last(tiles)]);
      tileGrid.$container.triggerKeyUpCapture(keys.RIGHT);
    });

    it('selects the only tile if there is only one', () => {
      let tileGrid = createTileGrid(1, {
        selectable: true
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();

      tileGrid.$container.triggerKeyDownCapture(keys.RIGHT);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.RIGHT);

      tileGrid.$container.triggerKeyDownCapture(keys.RIGHT);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.RIGHT);

      tileGrid.deselectAllTiles();

      tileGrid.$container.triggerKeyDownCapture(keys.RIGHT);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.RIGHT);
    });

    describe('with shift', () => {
      it('adds the next tile to the selection', () => {
        let tileGrid = createTileGrid(4, {
          selectable: true
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTile(tiles[1]);

        tileGrid.$container.triggerKeyDownCapture(keys.RIGHT, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[1], tiles[2]]);
        tileGrid.$container.triggerKeyUpCapture(keys.RIGHT, 'shift');

        tileGrid.$container.triggerKeyDownCapture(keys.RIGHT, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[1], tiles[2], tiles[3]]);
        tileGrid.$container.triggerKeyUpCapture(keys.RIGHT, 'shift');
      });

      it('removes the next tile from the selection if the focused tile is the first tile of the selection', () => {
        let tileGrid = createTileGrid(4, {
          selectable: true
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTiles([tiles[0], tiles[1], tiles[2]]);
        tileGrid.focusedTile = tiles[0];

        tileGrid.$container.triggerKeyInputCapture(keys.RIGHT, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[1], tiles[2]]);

        tileGrid.$container.triggerKeyInputCapture(keys.RIGHT, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[2]]);

        tileGrid.$container.triggerKeyInputCapture(keys.RIGHT, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[2], tiles[3]]);
      });

      it('does nothing if the last tile is already selected', () => {
        let tileGrid = createTileGrid(4, {
          selectable: true
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTile(tiles[2]);

        tileGrid.$container.triggerKeyDownCapture(keys.RIGHT, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[2], tiles[3]]);
        tileGrid.$container.triggerKeyUpCapture(keys.RIGHT, 'shift');

        tileGrid.$container.triggerKeyDownCapture(keys.RIGHT, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[2], tiles[3]]);
        tileGrid.$container.triggerKeyUpCapture(keys.RIGHT, 'shift');
      });

      it('adds the correct tile to the selection if the focused tile gets invisible', () => {
        let tileGrid = createTileGrid(4, {
          selectable: true
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTiles([tiles[0], tiles[1]]);
        tileGrid.focusedTile = tiles[0];

        let filter = {
          accept: tile => {
            return tile !== tiles[0]; // Make tile 0 invisible
          }
        };
        tileGrid.addFilter(filter);
        expect(tileGrid.selectedTiles).toEqual([tiles[1]]);
        expect(tileGrid.focusedTile).toBe(null);

        tileGrid.$container.triggerKeyInputCapture(keys.RIGHT, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[1], tiles[2]]);
      });

      it('connects two selections blocks and sets the focused tile to the beginning of the new block', () => {
        let tileGrid = createTileGrid(6, {
          selectable: true
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTiles([tiles[1], tiles[4]]);
        tileGrid.focusedTile = tiles[1];

        tileGrid.$container.triggerKeyInputCapture(keys.RIGHT, 'shift');
        expect(arrays.equalsIgnoreOrder(tileGrid.selectedTiles, [tiles[1], tiles[2], tiles[4]])).toBe(true);

        tileGrid.$container.triggerKeyInputCapture(keys.RIGHT, 'shift');
        expect(arrays.equalsIgnoreOrder(tileGrid.selectedTiles, [tiles[1], tiles[2], tiles[3], tiles[4]])).toBe(true);

        tileGrid.$container.triggerKeyInputCapture(keys.RIGHT, 'shift');
        expect(arrays.equalsIgnoreOrder(tileGrid.selectedTiles, [tiles[1], tiles[2], tiles[3], tiles[4], tiles[5]])).toBe(true);

        tileGrid.$container.triggerKeyInputCapture(keys.RIGHT, 'shift');
        expect(arrays.equalsIgnoreOrder(tileGrid.selectedTiles, [tiles[1], tiles[2], tiles[3], tiles[4], tiles[5]])).toBe(true);
      });
    });
  });

  describe('key left', () => {
    it('selects the previous tile', () => {
      let tileGrid = createTileGrid(4, {
        selectable: true
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();
      tileGrid.selectTile(tiles[2]);

      tileGrid.$container.triggerKeyDownCapture(keys.LEFT);
      expect(tileGrid.selectedTiles).toEqual([tiles[1]]);
      tileGrid.$container.triggerKeyUpCapture(keys.LEFT);

      tileGrid.$container.triggerKeyDownCapture(keys.LEFT);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.LEFT);
    });

    it('selects the last tile if no tile is selected yet', () => {
      let tileGrid = createTileGrid(4, {
        selectable: true
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();

      tileGrid.$container.triggerKeyDownCapture(keys.LEFT);
      expect(tileGrid.selectedTiles).toEqual([arrays.last(tiles)]);
      tileGrid.$container.triggerKeyUpCapture(keys.LEFT);
    });

    it('does nothing if the first tile is already selected', () => {
      let tileGrid = createTileGrid(4, {
        selectable: true
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();
      tileGrid.selectTile(tiles[0]);

      tileGrid.$container.triggerKeyDownCapture(keys.LEFT);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.LEFT);
    });

    it('selects the only tile if there is only one', () => {
      let tileGrid = createTileGrid(1, {
        selectable: true
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();

      tileGrid.$container.triggerKeyDownCapture(keys.LEFT);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.LEFT);

      tileGrid.$container.triggerKeyDownCapture(keys.LEFT);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.LEFT);

      tileGrid.deselectAllTiles();

      tileGrid.$container.triggerKeyDownCapture(keys.LEFT);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.LEFT);
    });

    describe('with shift', () => {
      it('adds the previous tile to the selection', () => {
        let tileGrid = createTileGrid(4, {
          selectable: true
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTile(tiles[3]);

        tileGrid.$container.triggerKeyDownCapture(keys.LEFT, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[2], tiles[3]]);
        tileGrid.$container.triggerKeyUpCapture(keys.LEFT, 'shift');

        tileGrid.$container.triggerKeyDownCapture(keys.LEFT, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[1], tiles[2], tiles[3]]);
        tileGrid.$container.triggerKeyUpCapture(keys.LEFT, 'shift');
      });

      it('does nothing if the first tile is already selected', () => {
        let tileGrid = createTileGrid(4, {
          selectable: true
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTile(tiles[1]);

        tileGrid.$container.triggerKeyDownCapture(keys.LEFT, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[0], tiles[1]]);
        tileGrid.$container.triggerKeyUpCapture(keys.LEFT, 'shift');

        tileGrid.$container.triggerKeyDownCapture(keys.LEFT, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[0], tiles[1]]);
        tileGrid.$container.triggerKeyUpCapture(keys.LEFT, 'shift');
      });

      it('removes the previous tile from the selection if the next tile is already selected', () => {
        let tileGrid = createTileGrid(4, {
          selectable: true
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTiles([tiles[1], tiles[2], tiles[3]]);
        tileGrid.focusedTile = tiles[3];

        tileGrid.$container.triggerKeyInputCapture(keys.LEFT, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[1], tiles[2]]);

        tileGrid.$container.triggerKeyInputCapture(keys.LEFT, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[1]]);

        tileGrid.$container.triggerKeyInputCapture(keys.LEFT, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[0], tiles[1]]);
      });

      it('adds the correct tile to the selection if the focused tile gets invisible', () => {
        let tileGrid = createTileGrid(4, {
          selectable: true
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTiles([tiles[1], tiles[2]]);
        tileGrid.focusedTile = tiles[2];

        let filter = {
          accept: tile => {
            return tile !== tiles[2]; // Make tile 2 invisible
          }
        };
        tileGrid.addFilter(filter);
        expect(tileGrid.selectedTiles).toEqual([tiles[1]]);
        expect(tileGrid.focusedTile).toBe(null);

        tileGrid.$container.triggerKeyInputCapture(keys.LEFT, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[0], tiles[1]]);
      });

      it('connects two selections blocks and sets the focused tile to the beginning of the new block', () => {
        let tileGrid = createTileGrid(5, {
          selectable: true
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTiles([tiles[1], tiles[4]]);
        tileGrid.focusedTile = tiles[4];

        tileGrid.$container.triggerKeyInputCapture(keys.LEFT, 'shift');
        expect(arrays.equalsIgnoreOrder(tileGrid.selectedTiles, [tiles[1], tiles[3], tiles[4]])).toBe(true);

        tileGrid.$container.triggerKeyInputCapture(keys.LEFT, 'shift');
        expect(arrays.equalsIgnoreOrder(tileGrid.selectedTiles, [tiles[1], tiles[2], tiles[3], tiles[4]])).toBe(true);

        tileGrid.$container.triggerKeyInputCapture(keys.LEFT, 'shift');
        expect(arrays.equalsIgnoreOrder(tileGrid.selectedTiles, [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4]])).toBe(true);

        tileGrid.$container.triggerKeyInputCapture(keys.LEFT, 'shift');
        expect(arrays.equalsIgnoreOrder(tileGrid.selectedTiles, [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4]])).toBe(true);
      });

    });

  });

  describe('key down', () => {
    it('selects the tile below', () => {
      let tileGrid = createTileGrid(8, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();
      tileGrid.selectTile(tiles[0]);

      tileGrid.$container.triggerKeyDownCapture(keys.DOWN);
      expect(tileGrid.selectedTiles).toEqual([tiles[3]]);
      tileGrid.$container.triggerKeyUpCapture(keys.DOWN);

      tileGrid.$container.triggerKeyDownCapture(keys.DOWN);
      expect(tileGrid.selectedTiles).toEqual([tiles[6]]);
      tileGrid.$container.triggerKeyUpCapture(keys.DOWN);
    });

    it('selects the first tile if no tile is selected yet', () => {
      let tileGrid = createTileGrid(8, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();

      tileGrid.$container.triggerKeyDownCapture(keys.DOWN);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.DOWN);
    });

    it('does nothing if a tile in the last row is already selected', () => {
      let tileGrid = createTileGrid(8, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();
      tileGrid.selectTile(tiles[6]);

      tileGrid.$container.triggerKeyDownCapture(keys.DOWN);
      expect(tileGrid.selectedTiles).toEqual([tiles[6]]);
      tileGrid.$container.triggerKeyUpCapture(keys.DOWN);

      tileGrid.selectTile(arrays.last(tiles));
      expect(tileGrid.selectedTiles).toEqual([arrays.last(tiles)]);

      tileGrid.$container.triggerKeyDownCapture(keys.DOWN);
      expect(tileGrid.selectedTiles).toEqual([arrays.last(tiles)]);
      tileGrid.$container.triggerKeyUpCapture(keys.DOWN);
    });

    it('selects the only tile if there is only one', () => {
      let tileGrid = createTileGrid(1, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();

      tileGrid.$container.triggerKeyDownCapture(keys.DOWN);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.DOWN);

      tileGrid.$container.triggerKeyDownCapture(keys.DOWN);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.DOWN);

      tileGrid.deselectAllTiles();

      tileGrid.$container.triggerKeyDownCapture(keys.DOWN);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.DOWN);
    });

    it('selects the last tile if below the focused tile is no tile', () => {
      let tileGrid = createTileGrid(5, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles = tileGrid.tiles;
      tileGrid.selectTile(tiles[2]);
      tileGrid.render();
      tileGrid.validateLayout();

      tileGrid.$container.triggerKeyDownCapture(keys.DOWN);
      expect(tileGrid.selectedTiles).toEqual([tiles[4]]);
      tileGrid.$container.triggerKeyUpCapture(keys.DOWN);
    });

    describe('with shift', () => {
      it('adds the tiles between the focused and the newly focused tile to the selection', () => {
        let tileGrid = createTileGrid(9, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTile(tiles[1]);

        tileGrid.$container.triggerKeyInputCapture(keys.DOWN, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[1], tiles[2], tiles[3], tiles[4]]);

        tileGrid.$container.triggerKeyInputCapture(keys.DOWN, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7]]);
      });

      it('removes the tiles between the focused and the newly focused tiles from the selection if the focused tile is the first tile of the selection', () => {
        let tileGrid = createTileGrid(9, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTiles([tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7]]);
        tileGrid.focusedTile = tiles[1];

        tileGrid.$container.triggerKeyInputCapture(keys.DOWN, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[4], tiles[5], tiles[6], tiles[7]]);

        tileGrid.$container.triggerKeyInputCapture(keys.DOWN, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[7]]);
      });

      it('does nothing if a tile in the last row is already selected', () => {
        let tileGrid = createTileGrid(5, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTiles([tiles[1], tiles[2], tiles[3]]);

        tileGrid.$container.triggerKeyInputCapture(keys.DOWN, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[1], tiles[2], tiles[3]]);
      });

      it('adds the correct tile to the selection if the focused tile gets invisible', () => {
        let tileGrid = createTileGrid(9, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTiles([tiles[2], tiles[3], tiles[4]]);
        tileGrid.focusedTile = tiles[2];

        let filter = {
          accept: tile => {
            return tile !== tiles[2]; // Make tile 2 invisible
          }
        };
        tileGrid.addFilter(filter);
        expect(tileGrid.selectedTiles).toEqual([tiles[3], tiles[4]]);
        expect(tileGrid.focusedTile).toBe(null);

        tileGrid.$container.triggerKeyInputCapture(keys.DOWN, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[3], tiles[4], tiles[5], tiles[6], tiles[7]]);
      });

      it('connects two selections blocks and sets the focused tile to the beginning of the new block', () => {
        let tileGrid = createTileGrid(9, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTiles([tiles[0], tiles[1], tiles[5]]);
        tileGrid.focusedTile = tiles[1];

        tileGrid.$container.triggerKeyInputCapture(keys.DOWN, 'shift');
        expect(arrays.equalsIgnoreOrder(tileGrid.selectedTiles, [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5]])).toBe(true);

        tileGrid.$container.triggerKeyInputCapture(keys.DOWN, 'shift');
        expect(arrays.equalsIgnoreOrder(tileGrid.selectedTiles, [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]])).toBe(true);

        tileGrid.$container.triggerKeyInputCapture(keys.DOWN, 'shift');
        expect(arrays.equalsIgnoreOrder(tileGrid.selectedTiles, [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]])).toBe(true);
      });
    });

  });

  describe('key up', () => {
    it('selects the tile above', () => {
      let tileGrid = createTileGrid(8, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();
      tileGrid.selectTile(arrays.last(tiles));

      tileGrid.$container.triggerKeyDownCapture(keys.UP);
      expect(tileGrid.selectedTiles).toEqual([tiles[4]]);
      tileGrid.$container.triggerKeyUpCapture(keys.UP);

      tileGrid.$container.triggerKeyDownCapture(keys.UP);
      expect(tileGrid.selectedTiles).toEqual([tiles[1]]);
      tileGrid.$container.triggerKeyUpCapture(keys.UP);
    });

    it('selects the last tile if no tile is selected yet', () => {
      let tileGrid = createTileGrid(8, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();

      tileGrid.$container.triggerKeyDownCapture(keys.UP);
      expect(tileGrid.selectedTiles).toEqual([arrays.last(tiles)]);
      tileGrid.$container.triggerKeyUpCapture(keys.UP);
    });

    it('does nothing if a tile in the first row is already selected', () => {
      let tileGrid = createTileGrid(8, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();
      tileGrid.selectTile(tiles[2]);

      tileGrid.$container.triggerKeyDownCapture(keys.UP);
      expect(tileGrid.selectedTiles).toEqual([tiles[2]]);
      tileGrid.$container.triggerKeyUpCapture(keys.UP);

      tileGrid.selectTile(tiles[0]);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);

      tileGrid.$container.triggerKeyDownCapture(keys.UP);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.UP);
    });

    it('selects the only tile if there is only one', () => {
      let tileGrid = createTileGrid(1, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();

      tileGrid.$container.triggerKeyDownCapture(keys.UP);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.UP);

      tileGrid.$container.triggerKeyDownCapture(keys.UP);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.UP);

      tileGrid.deselectAllTiles();

      tileGrid.$container.triggerKeyDownCapture(keys.UP);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.UP);
    });

    describe('with shift', () => {
      it('adds the tiles between the focused and the newly focused tile to the selection', () => {
        let tileGrid = createTileGrid(9, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTile(tiles[7]);

        tileGrid.$container.triggerKeyInputCapture(keys.UP, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[4], tiles[5], tiles[6], tiles[7]]);

        tileGrid.$container.triggerKeyInputCapture(keys.UP, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7]]);
      });

      it('removes the tiles between the focused and the newly focused tiles from the selection if the focused tile is the first tile of the selection', () => {
        let tileGrid = createTileGrid(9, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTiles([tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7]]);
        tileGrid.focusedTile = tiles[7];

        tileGrid.$container.triggerKeyInputCapture(keys.UP, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[1], tiles[2], tiles[3], tiles[4]]);

        tileGrid.$container.triggerKeyInputCapture(keys.UP, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[1]]);
      });

      it('does nothing if a tile in the first row is already selected', () => {
        let tileGrid = createTileGrid(5, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTiles([tiles[1], tiles[2], tiles[3]]);
        tileGrid.focusedTile = tiles[2];

        tileGrid.$container.triggerKeyInputCapture(keys.UP, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[1], tiles[2], tiles[3]]);
      });

      it('adds the correct tile to the selection if the focused tile gets invisible', () => {
        let tileGrid = createTileGrid(9, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTiles([tiles[4], tiles[5], tiles[6]]);
        tileGrid.focusedTile = tiles[6];

        let filter = {
          accept: tile => {
            return tile !== tiles[6]; // Make tile 6 invisible
          }
        };
        tileGrid.addFilter(filter);
        expect(tileGrid.selectedTiles).toEqual([tiles[4], tiles[5]]);
        expect(tileGrid.focusedTile).toBe(null);

        tileGrid.$container.triggerKeyInputCapture(keys.UP, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[1], tiles[2], tiles[3], tiles[4], tiles[5]]);
      });

      it('connects two selections blocks and sets the focused tile to the beginning of the new block', () => {
        let tileGrid = createTileGrid(9, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTiles([tiles[3], tiles[7], tiles[8]]);
        tileGrid.focusedTile = tiles[7];

        tileGrid.$container.triggerKeyInputCapture(keys.UP, 'shift');
        expect(arrays.equalsIgnoreOrder(tileGrid.selectedTiles, [tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]])).toBe(true);

        tileGrid.$container.triggerKeyInputCapture(keys.UP, 'shift');
        expect(arrays.equalsIgnoreOrder(tileGrid.selectedTiles, [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]])).toBe(true);

        tileGrid.$container.triggerKeyInputCapture(keys.UP, 'shift');
        expect(arrays.equalsIgnoreOrder(tileGrid.selectedTiles, [tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]])).toBe(true);
      });
    });
  });

  describe('home', () => {
    it('selects the first tile', () => {
      let tileGrid = createTileGrid(8, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();
      tileGrid.selectTile(tiles[1]);

      tileGrid.$container.triggerKeyDownCapture(keys.HOME);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.HOME);
    });

    it('does nothing if the first tile is already selected', () => {
      let tileGrid = createTileGrid(4, {
        selectable: true
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();
      tileGrid.selectTile(tiles[0]);

      tileGrid.$container.triggerKeyDownCapture(keys.HOME);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.HOME);
    });

    it('selects only the first tile if first and other tiles are selected', () => {
      let tileGrid = createTileGrid(8, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();
      tileGrid.selectTiles([tiles[0], tiles[1]]);
      tileGrid.focusedTile = tiles[1];

      tileGrid.$container.triggerKeyDownCapture(keys.HOME);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.HOME);
    });

    it('selects the only tile if there is only one', () => {
      let tileGrid = createTileGrid(1, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();

      tileGrid.$container.triggerKeyDownCapture(keys.HOME);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.HOME);

      tileGrid.$container.triggerKeyDownCapture(keys.HOME);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.HOME);

      tileGrid.deselectAllTiles();

      tileGrid.$container.triggerKeyDownCapture(keys.HOME);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.HOME);
    });

    describe('with shift', () => {
      it('adds the tiles between the focused and the newly focused tile to the selection', () => {
        let tileGrid = createTileGrid(9, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTiles([tiles[6], tiles[7]]);

        tileGrid.$container.triggerKeyInputCapture(keys.HOME, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7]]);
      });
    });

  });

  describe('end', () => {
    it('selects the last tile', () => {
      let tileGrid = createTileGrid(8, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();

      tileGrid.$container.triggerKeyDownCapture(keys.END);
      expect(tileGrid.selectedTiles).toEqual([arrays.last(tiles)]);
      tileGrid.$container.triggerKeyUpCapture(keys.END);
    });

    it('does nothing if the first tile is already selected', () => {
      let tileGrid = createTileGrid(4, {
        selectable: true
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();
      tileGrid.selectTile(arrays.last(tiles));

      tileGrid.$container.triggerKeyDownCapture(keys.END);
      expect(tileGrid.selectedTiles).toEqual([arrays.last(tiles)]);
      tileGrid.$container.triggerKeyUpCapture(keys.END);
    });

    it('selects only the last tile if last and other tiles are selected', () => {
      let tileGrid = createTileGrid(4, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();
      tileGrid.selectTiles([tiles[2], tiles[3]]);
      tileGrid.focusedTile = tiles[2];

      tileGrid.$container.triggerKeyDownCapture(keys.END);
      expect(tileGrid.selectedTiles).toEqual([tiles[3]]);
      tileGrid.$container.triggerKeyUpCapture(keys.END);
    });

    it('selects the only tile if there is only one', () => {
      let tileGrid = createTileGrid(1, {
        selectable: true,
        gridColumnCount: 3
      });
      let tiles = tileGrid.tiles;
      tileGrid.render();
      tileGrid.validateLayout();

      tileGrid.$container.triggerKeyDownCapture(keys.END);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.END);

      tileGrid.$container.triggerKeyDownCapture(keys.END);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.END);

      tileGrid.deselectAllTiles();

      tileGrid.$container.triggerKeyDownCapture(keys.END);
      expect(tileGrid.selectedTiles).toEqual([tiles[0]]);
      tileGrid.$container.triggerKeyUpCapture(keys.END);
    });

    describe('with shift', () => {
      it('adds the tiles between the focused and the newly focused tile to the selection', () => {
        let tileGrid = createTileGrid(9, {
          selectable: true,
          gridColumnCount: 3
        });
        let tiles = tileGrid.tiles;
        tileGrid.render();
        tileGrid.validateLayout();
        tileGrid.selectTiles([tiles[4], tiles[5]]);

        tileGrid.$container.triggerKeyInputCapture(keys.END, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]]);

        // After pressing shift home afterwards all tiles should be selected
        tileGrid.$container.triggerKeyInputCapture(keys.HOME, 'shift');
        expect(tileGrid.selectedTiles).toEqual([tiles[0], tiles[1], tiles[2], tiles[3], tiles[4], tiles[5], tiles[6], tiles[7], tiles[8]]);
      });
    });

  });

});
