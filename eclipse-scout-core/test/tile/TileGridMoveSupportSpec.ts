/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, graphics, GridData, InitModelOf, scout, Tile, TileGrid, TileGridModel, TileMoveHandler} from '../../src';
import {JQueryTesting} from '../../src/testing';
import $ from 'jquery';

describe('TileGridMoveSupport', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    $.fx.off = true;
    $('<style>' +
      '.tile-grid {position: relative; border: 1px dotted black; padding: 5px; width: 200px; height: 200px}' +
      '.tile {position: absolute; background-color: grey;}' +
      '.tile.dragged {background-color: blue;}' +
      '.tile.dragged-clone {background-color: rgba(255, 0, 0, 0.3);}' +
      '.scrollbar {position: absolute;}' +
      '</style>').appendTo($('#sandbox'));
  });

  afterEach(() => {
    $.fx.off = false;
  });

  function createTileGrid(numTiles?: number, model?: TileGridModel): TileGrid {
    let tiles = [];
    for (let i = 0; i < numTiles; i++) {
      tiles.push({
        objectType: Tile,
        label: 'Tile ' + i,
        movable: true
      });
    }
    let defaults = {
      parent: session.desktop,
      tiles: tiles,
      layoutConfig: {
        columnWidth: 20,
        rowHeight: 40,
        hgap: 4,
        vgap: 4
      }
    };
    model = $.extend({}, defaults, model);
    return scout.create(TileGrid, model as InitModelOf<TileGrid>);
  }

  function createTileGridAndLayout(numTiles?: number, model?: TileGridModel): TileGrid {
    let grid = createTileGrid(numTiles, model);
    grid.render();
    grid.validateLayout();
    return grid;
  }

  function moveTo(from: Tile, to: Tile) {
    JQueryTesting.triggerMouseDown(from.$container);
    JQueryTesting.triggerMouseMove(to.$container);
    JQueryTesting.triggerMouseUp(to.$container);
  }

  async function finishMove(tile: Tile): Promise<Event> {
    tile.parent.validateLayout();
    return (tile.$container.data('movable') as TileMoveHandler).moveSupport.when('end cancel');
  }

  describe('drop on another tile', () => {

    it('creates a clone while dragging ', async () => {
      let grid = createTileGridAndLayout(3);
      let tile0 = grid.tiles[0];
      let tile2 = grid.tiles[2];
      moveTo(tile0, tile2);
      let $clone = $('.tile.dragged-clone');
      expect(graphics.bounds(tile2.$container).contains(graphics.bounds($clone).center()));
      expect(tile0.$container).toHaveClass('dragged');

      await finishMove(tile0);
      $clone = $('.tile.dragged-clone');
      expect($clone.length).toBe(0);
      expect(tile0.$container).not.toHaveClass('dragged');
    });

    it('switches tile positions', async () => {
      let grid = createTileGridAndLayout(3);
      let tile0 = grid.tiles[0];
      let tile2 = grid.tiles[2];
      moveTo(tile0, tile2);
      await finishMove(tile0);

      // Model order
      expect(grid.tiles[0]).toBe(tile2);
      expect(grid.tiles[2]).toBe(tile0);

      // Rendering order
      expect(tile2.$container.next()[0]).toBe(grid.tiles[1].$container[0]);
      expect(grid.tiles[1].$container.next()[0]).toBe(tile0.$container[0]);

      // Layouted bounds
      expect(graphics.bounds(tile2.$container).x).toBeLessThan(graphics.bounds(tile0.$container).x);
    });

    it('switches grid data', async () => {
      let grid = createTileGridAndLayout(0, {
        tiles: [{
          objectType: Tile,
          gridDataHints: {
            w: 2,
            h: 3
          },
          movable: true
        }, {
          objectType: Tile,
          gridDataHints: {
            w: 1,
            h: 1
          },
          movable: true
        }]
      });
      let tile0 = grid.tiles[0];
      let tile1 = grid.tiles[1];
      expect(tile0.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 2, h: 3}));
      expect(tile0.gridData).toEqual(new GridData({x: 0, y: 0, w: 2, h: 3}));
      expect(tile1.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 1, h: 1}));
      expect(tile1.gridData).toEqual(new GridData({x: 2, y: 0, w: 1, h: 1}));

      moveTo(tile0, tile1);
      await finishMove(tile0);
      expect(tile0.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 1, h: 1}));
      expect(tile0.gridData).toEqual(new GridData({x: 2, y: 0, w: 1, h: 1}));
      expect(tile1.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 2, h: 3}));
      expect(tile1.gridData).toEqual(new GridData({x: 0, y: 0, w: 2, h: 3}));
    });

    it('switches grid data with custom x/y', async () => {
      let grid = createTileGridAndLayout(0, {
        tiles: [{
          objectType: Tile,
          gridDataHints: {
            x: 1,
            y: 1,
            w: 2,
            h: 3
          },
          movable: true
        }, {
          objectType: Tile,
          gridDataHints: {
            x: 5,
            y: 4,
            w: 1,
            h: 1
          },
          movable: true
        }]
      });
      let tile0 = grid.tiles[0];
      let tile1 = grid.tiles[1];
      expect(tile0.gridDataHints).toEqual(new GridData({x: 1, y: 1, w: 2, h: 3}));
      expect(tile0.gridData).toEqual(new GridData({x: 1, y: 1, w: 2, h: 3}));
      expect(tile1.gridDataHints).toEqual(new GridData({x: 5, y: 4, w: 1, h: 1}));
      expect(tile1.gridData).toEqual(new GridData({x: 5, y: 4, w: 1, h: 1}));

      moveTo(tile0, tile1);
      await finishMove(tile0);
      expect(tile0.gridDataHints).toEqual(new GridData({x: 5, y: 4, w: 1, h: 1}));
      expect(tile0.gridData).toEqual(new GridData({x: 5, y: 4, w: 1, h: 1}));
      expect(tile1.gridDataHints).toEqual(new GridData({x: 1, y: 1, w: 2, h: 3}));
      expect(tile1.gridData).toEqual(new GridData({x: 1, y: 1, w: 2, h: 3}));
    });
  });

  describe('drop outside of a tile', () => {
    it('cancels moving', async () => {
      let grid = createTileGridAndLayout(3);
      let tile0 = grid.tiles[0];
      let gridBounds = graphics.offsetBounds(grid.$container);
      JQueryTesting.triggerMouseDown(tile0.$container);
      JQueryTesting.triggerMouseMove(grid.$container, {position: {top: gridBounds.bottom(), left: gridBounds.right()}});
      JQueryTesting.triggerMouseUp(grid.$container, {position: {top: gridBounds.bottom(), left: gridBounds.right()}});

      // Clone moved back to tile0
      let $clone = $('.tile.dragged-clone');
      expect(graphics.bounds(tile0.$container).contains(graphics.bounds($clone).center()));
      expect(tile0.$container).toHaveClass('dragged');

      await finishMove(tile0);

      // Clone has been removed
      $clone = $('.tile.dragged-clone');
      expect($clone.length).toBe(0);
      expect(tile0.$container).not.toHaveClass('dragged');

      // Tile did not move
      expect(grid.tiles[0]).toBe(tile0);
      expect(tile0.gridData.x).toBe(0);
      expect(tile0.gridData.y).toBe(0);
    });
  });

  it('cancels moving if widget is removed', async () => {
    let grid = createTileGridAndLayout(3);
    let tile0 = grid.tiles[0];
    let tile2 = grid.tiles[2];
    tile0.setGridDataHints(tile0.gridDataHints.clone({x: 1}));
    tile2.setGridDataHints(tile0.gridDataHints.clone({x: 3}));
    moveTo(tile0, tile2);

    // Clone moved to tile2
    let $clone = $('.tile.dragged-clone');
    expect(graphics.bounds(tile2.$container).contains(graphics.bounds($clone).center()));
    expect(tile0.$container).toHaveClass('dragged');

    grid.remove();

    // Clone is removed immediately
    $clone = $('.tile.dragged-clone');
    expect($clone.length).toBe(0);
    expect(tile0.$container).not.toHaveClass('dragged');

    // Tiles have been switched nevertheless
    expect(grid.tiles[2]).toBe(tile0);
    expect(grid.tiles[0]).toBe(tile2);
    expect(tile0.gridDataHints.x).toBe(3);
    expect(tile2.gridDataHints.x).toBe(1);
  });
});
