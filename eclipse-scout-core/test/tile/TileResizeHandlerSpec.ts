/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import $ from 'jquery';
import {graphics, GridData, InitModelOf, scout, Tile, TileGrid, TileGridModel} from '../../src';
import {JQueryTesting} from '../../src/testing';

describe('TileResizeHandler', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    $.fx.off = true;
    $('<style>' +
      '.tile-grid {position: relative; border: 1px dotted black; padding: 5px; width: 200px; height: 200px}' +
      '.tile {position: absolute; background-color: grey; display: flex; justify-content: center; align-items: center; color: white;}' +
      '.scrollbar {position: absolute;}' +
      '</style>').appendTo($('#sandbox'));
  });

  afterEach(() => {
    $.fx.off = false;
  });

  class SpecTile extends Tile {
    label: string;

    protected override _render() {
      super._render();
      this.$container.appendDiv().text(this.label);
    }
  }

  function createTileGrid(numTiles?: number, model?: TileGridModel): TileGrid {
    let tiles = [];
    for (let i = 0; i < numTiles; i++) {
      tiles.push({
        objectType: SpecTile,
        label: i,
        resizable: true
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

  function resizeTo($from: JQuery, to: Tile, position?: JQueryCoordinates) {
    JQueryTesting.triggerMouseDown($from);
    JQueryTesting.triggerMouseMove(to.$container, {position: position});
    JQueryTesting.triggerMouseUp(to.$container, {position: position});
    to.parent.validateLayout(); // Just to visualize it
  }

  describe('resize tile to right', () => {
    it('makes gridData.w bigger', () => {
      let grid = createTileGridAndLayout(3);
      let tile0 = grid.tiles[0];
      let tile1 = grid.tiles[1];
      expect(tile0.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 1, h: 1}));
      expect(tile1.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 1, h: 1}));

      resizeTo(tile0.$container.children('.resizable-e'), tile1);
      expect(tile0.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 2, h: 1}));
      expect(tile1.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 1, h: 1}));

      let tile0Bounds = graphics.offsetBounds(tile0.$container);
      resizeTo(tile1.$container.children('.resizable-w'), tile0, {left: tile0Bounds.right() - 2, top: tile0Bounds.center().y});
      expect(tile0.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 2, h: 1}));
      expect(tile1.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 2, h: 1}));
    });

    it('makes gridData.h bigger', () => {
      let grid = createTileGridAndLayout(6);
      let tile0 = grid.tiles[0];
      let tile1 = grid.tiles[1];
      let tile4 = grid.tiles[4];
      let tile5 = grid.tiles[5];
      expect(tile0.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 1, h: 1}));
      expect(tile4.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 1, h: 1}));

      resizeTo(tile0.$container.children('.resizable-s'), tile4);
      expect(tile0.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 1, h: 2}));
      expect(tile4.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 1, h: 1}));

      resizeTo(tile5.$container.children('.resizable-n'), tile1);
      expect(tile1.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 1, h: 1}));
      expect(tile5.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 1, h: 2}));
    });

    it('adjusts x/y hints if provided', () => {
      let grid = createTileGrid(5);
      grid.render();
      let tile0 = grid.tiles[0];
      let tile1 = grid.tiles[1];
      tile1.setGridDataHints(tile0.gridDataHints.clone({x: 1, y: 0}));
      grid.validateLayout();
      expect(tile0.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 1, h: 1}));
      expect(tile1.gridDataHints).toEqual(new GridData({x: 1, y: 0, w: 1, h: 1}));

      resizeTo(tile1.$container.children('.resizable-w'), tile0);
      expect(tile0.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 1, h: 1}));
      expect(tile1.gridDataHints).toEqual(new GridData({x: 0, y: 0, w: 2, h: 1}));
    });

    it('can handle x/y hints that don\'t start at 0/0', () => {
      let grid = createTileGrid(3);
      grid.render();
      let tile0 = grid.tiles[0];
      let tile1 = grid.tiles[1];
      let tile2 = grid.tiles[2];
      tile0.setGridDataHints(tile0.gridDataHints.clone({x: 2, y: 2}));
      tile1.setGridDataHints(tile1.gridDataHints.clone({x: 3, y: 2}));
      tile2.setGridDataHints(tile2.gridDataHints.clone({x: 4, y: 2}));
      grid.validateLayout();
      expect(tile0.gridDataHints).toEqual(new GridData({x: 2, y: 2, w: 1, h: 1}));
      expect(tile1.gridDataHints).toEqual(new GridData({x: 3, y: 2, w: 1, h: 1}));

      resizeTo(tile1.$container.children('.resizable-w'), tile0);
      expect(tile0.gridDataHints).toEqual(new GridData({x: 2, y: 3, w: 1, h: 1})); // Is moved down by tileUtil.moveOtherTilesDown, see tileUtilSpec
      expect(tile1.gridDataHints).toEqual(new GridData({x: 2, y: 2, w: 2, h: 1}));
    });

    it('can handle provided and non provided x/y hints', () => {
      // This is NOT supported properly because mixing explicit with non-explicit positions is very uncommon
      let grid = createTileGrid(3);
      grid.render();
      let tile0 = grid.tiles[0];
      let tile1 = grid.tiles[1];
      let tile2 = grid.tiles[2];
      tile0.setGridDataHints(tile0.gridDataHints.clone({x: 0, y: 0}));
      tile1.setGridDataHints(tile1.gridDataHints.clone({x: 1, y: 0}));
      grid.validateLayout();
      expect(tile0.gridDataHints).toEqual(new GridData({x: 0, y: 0, w: 1, h: 1}));
      expect(tile1.gridDataHints).toEqual(new GridData({x: 1, y: 0, w: 1, h: 1}));

      resizeTo(tile1.$container.children('.resizable-w'), tile0);
      expect(tile0.gridDataHints).toEqual(new GridData({x: 0, y: 1, w: 1, h: 1})); // Is moved down by tileUtil.moveOtherTilesDown, see tileUtilSpec
      expect(tile1.gridDataHints).toEqual(new GridData({x: 0, y: 0, w: 2, h: 1}));
      grid.destroy();

      grid = createTileGrid(3);
      grid.render();
      tile0 = grid.tiles[0];
      tile1 = grid.tiles[1];
      tile2 = grid.tiles[2];
      tile1.setGridDataHints(tile1.gridDataHints.clone({x: 1, y: 0}));
      tile2.setGridDataHints(tile2.gridDataHints.clone({x: 2, y: 0}));
      grid.validateLayout();

      resizeTo(tile2.$container.children('.resizable-w'), tile1);
      expect(tile0.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 1, h: 1}));
      expect(tile1.gridDataHints).toEqual(new GridData({x: 1, y: 1, w: 1, h: 1})); // Is moved down by tileUtil.moveOtherTilesDown, see tileUtilSpec
      expect(tile2.gridDataHints).toEqual(new GridData({x: 1, y: 0, w: 2, h: 1}));
    });

    it('moves to top left corner of selection', () => {
      let grid = createTileGridAndLayout(4);
      let tile0 = grid.tiles[0];
      let tile1 = grid.tiles[1];
      let tile2 = grid.tiles[2];

      resizeTo(tile1.$container.children('.resizable-e'), tile2);
      expect(grid.tiles[0]).toBe(tile0); // Nothing changed because top left corner did not change

      resizeTo(tile1.$container.children('.resizable-w'), tile0);
      expect(grid.tiles[0]).toBe(tile1);
      expect(grid.tiles[1]).toBe(tile0);

      let tile1Bounds = graphics.offsetBounds(tile1.$container);
      resizeTo(tile0.$container.children('.resizable-w'), tile1, {left: tile1Bounds.right() - 2, top: tile1Bounds.center().y});
      expect(grid.tiles[0]).toBe(tile0); // Tile 0 moved to first position even though mouse was released on second cell
      expect(grid.tiles[1]).toBe(tile1);

      resizeTo(tile0.$container.children('.resizable-e'), tile0);
      expect(grid.tiles[0]).toBe(tile0); // Nothing changed
      expect(grid.tiles[1]).toBe(tile1);
    });

    it('moves to the end if resized to empty space', () => {
      let grid = createTileGridAndLayout(6, {gridColumnCount: 5});
      let tile1 = grid.tiles[1];
      let tile2 = grid.tiles[2];
      let tile4 = grid.tiles[4];

      let tile4Bounds = graphics.offsetBounds(tile4.$container);
      resizeTo(tile4.$container.children('.resizable-s'), tile4, {left: tile4Bounds.center().x, top: tile4Bounds.bottom() + 10});
      expect(tile4.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 1, h: 2}));
      expect(grid.tiles[4]).toBe(tile4);

      tile4Bounds = graphics.offsetBounds(tile4.$container);
      resizeTo(tile4.$container.children('.resizable-n'), tile4, {left: tile4Bounds.center().x, top: tile4Bounds.bottom() - 2});
      expect(tile4.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 1, h: 1}));
      expect(grid.tiles[5]).toBe(tile4);

      let tile2Bounds = graphics.offsetBounds(tile2.$container);
      resizeTo(tile2.$container.children('.resizable-s'), tile2, {left: tile2Bounds.center().x, top: tile2Bounds.bottom() + 10});
      expect(tile2.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 1, h: 2}));
      expect(grid.tiles[2]).toBe(tile2);

      tile2Bounds = graphics.offsetBounds(tile2.$container);
      resizeTo(tile2.$container.children('.resizable-n'), tile2, {left: tile2Bounds.center().x, top: tile2Bounds.bottom() - 2});
      expect(tile1.gridDataHints).toEqual(new GridData({x: -1, y: -1, w: 1, h: 1}));
      expect(grid.tiles[5]).toBe(tile2);
    });

    it('moves away in various directions', () => {
      let grid = createTileGridAndLayout(6);
      let tile0 = grid.tiles[0];
      let tile1 = grid.tiles[1];
      let tile4 = grid.tiles[4];

      resizeTo(tile0.$container.children('.resizable-s'), tile4);
      expect(grid.tiles[0]).toBe(tile0); // Nothing changed because top left corner did not change

      resizeTo(tile4.$container.children('.resizable-n'), tile1);
      expect(grid.tiles[1]).toBe(tile4); // Tile 4 moved to second position
      expect(grid.tiles[2]).toBe(tile1); // Tile 1 moved to right

      resizeTo(tile4.$container.children('.resizable-w'), tile0);
      expect(grid.tiles[0]).toBe(tile4); // Tile 4 moved to first position
      expect(grid.tiles[1]).toBe(tile0); // Tile 0 moved to right

      resizeTo(tile1.$container.children('.resizable-w'), tile0);
      expect(grid.tiles[1]).toBe(tile1); // Tile 1 moved to second position
      expect(grid.tiles[2]).toBe(tile0);

      resizeTo(tile1.$container.children('.resizable-w'), tile4);
      expect(grid.tiles[0]).toBe(tile1); // Tile 1 moved to first position
      expect(grid.tiles[1]).toBe(tile4);
    });

    it('moves away if made smaller', () => {
      let grid = createTileGridAndLayout(6);
      let tile0 = grid.tiles[0];
      let tile1 = grid.tiles[1];
      let tile4 = grid.tiles[4];

      resizeTo(tile0.$container.children('.resizable-s'), tile4);
      expect(grid.tiles[0]).toBe(tile0);

      resizeTo(tile0.$container.children('.resizable-s'), tile0);
      expect(grid.tiles[0]).toBe(tile0); // Nothing changed

      resizeTo(tile0.$container.children('.resizable-s'), tile4);
      expect(grid.tiles[0]).toBe(tile0);

      resizeTo(tile0.$container.children('.resizable-n'), tile4);
      expect(grid.tiles[0]).toBe(tile1);
      expect(grid.tiles[3]).toBe(tile0);
    });
  });
});
