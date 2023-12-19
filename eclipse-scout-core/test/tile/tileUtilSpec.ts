/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {GridData, scout, Tile, TileGrid, tileUtil} from '../../src';

describe('tileUtil', () => {
  let session: SandboxSession;
  let placeholderIgnorer = tile => !!tile.placeholder;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  function createTiles(gridDatas: GridData[], withPlaceholders = false) {
    let tileGrid = scout.create(TileGrid, {
      parent: session.desktop,
      tiles: gridDatas.map(data => ({
        objectType: Tile,
        gridDataHints: data
      }))
    });
    if (withPlaceholders) {
      tileGrid.insertTiles(tileUtil.createPlaceholders(tileUtil.buildMatrix(tileGrid.tiles), (x, y) => ({
        objectType: Tile,
        placeholder: true,
        gridDataHints: {
          x: x,
          y: y
        }
      })));
    }
    tileGrid.validateLogicalGrid();
    return tileGrid.tiles;
  }

  function assert(tiles: Tile[], gridDatas: GridData[]) {
    for (let i = 0; i < gridDatas.length; i++) {
      if (gridDatas[i]) {
        expect(tiles[i].gridDataHints).toEqual(new GridData(gridDatas[i]));
      }
    }
  }

  function verify(gridDatas: GridData[], tileIndex: number, newGridData: GridData, expectedDatas: GridData[]) {
    let tiles = createTiles(gridDatas);
    tileUtil.moveOtherTilesDown(tiles, tiles[tileIndex], new GridData(newGridData));
    assert(tiles, expectedDatas);

    tiles = createTiles(gridDatas, true);
    tileUtil.moveOtherTilesDown(tiles, tiles[tileIndex], new GridData(newGridData), placeholderIgnorer);
    assert(tiles, expectedDatas);
  }

  describe('moveOtherTilesDown', () => {
    it('moves bottom tile down', () => {
      /**
       * |_|
       * |_|
       */
      let gridDatas = [
        {x: 1, y: 1, w: 1, h: 1},
        {x: 1, y: 2, w: 1, h: 1}
      ];
      let expGridDatas = [
        null,
        {x: 1, y: 3, w: 1, h: 1}
      ];
      verify(gridDatas, 0, {x: 1, y: 1, w: 1, h: 2}, expGridDatas);
    });

    it('moves bottom tiles down', () => {
      /**
       * |_|
       * |_|
       * |_|
       */
      let gridDatas = [
        {x: 1, y: 1, w: 1, h: 1},
        {x: 1, y: 2, w: 1, h: 1},
        {x: 1, y: 3, w: 1, h: 1}
      ];
      let expGridDatas = [
        null,
        {x: 1, y: 3, w: 1, h: 1},
        {x: 1, y: 4, w: 1, h: 1}
      ];
      verify(gridDatas, 0, {x: 1, y: 1, w: 1, h: 2}, expGridDatas);
    });

    it('moves tile on the bottom left of a wide tile down ', () => {
      /**
       *  |_|
       * |__|
       * |_|
       */
      let gridDatas = [
        {x: 2, y: 1, w: 1, h: 1},
        {x: 1, y: 2, w: 2, h: 1},
        {x: 1, y: 3, w: 1, h: 1}
      ];
      let expGridDatas = [
        null,
        {x: 1, y: 3, w: 2, h: 1},
        {x: 1, y: 4, w: 1, h: 1}
      ];
      verify(gridDatas, 0, {x: 2, y: 1, w: 1, h: 2}, expGridDatas);
    });

    it('moves tile on the bottom right of a wide tile down ', () => {
      /**
       *  |_|
       *  |__|
       *   |_|
       */
      let gridDatas = [
        {x: 2, y: 1, w: 1, h: 1},
        {x: 2, y: 2, w: 2, h: 1},
        {x: 3, y: 3, w: 1, h: 1}
      ];
      let expGridDatas = [
        null,
        {x: 2, y: 3, w: 2, h: 1},
        {x: 3, y: 4, w: 1, h: 1}
      ];
      verify(gridDatas, 0, {x: 2, y: 1, w: 1, h: 2}, expGridDatas);
    });

    it('moves tile on the bottom of a wide tile down ', () => {
      /**
       *  |_|
       * |___|
       *  |_|
       */
      let gridDatas = [
        {x: 2, y: 1, w: 1, h: 1},
        {x: 1, y: 2, w: 3, h: 1},
        {x: 2, y: 3, w: 1, h: 1}
      ];
      let expGridDatas = [
        null,
        {x: 1, y: 3, w: 3, h: 1},
        {x: 2, y: 4, w: 1, h: 1}
      ];
      verify(gridDatas, 0, {x: 2, y: 1, w: 1, h: 2}, expGridDatas);
    });

    it('moves wide tile on the bottom of a tile down ', () => {
      /**
       *  |_|
       *  |_|
       * |___|
       */
      let gridDatas = [
        {x: 2, y: 1, w: 1, h: 1},
        {x: 2, y: 2, w: 1, h: 1},
        {x: 1, y: 3, w: 3, h: 1}
      ];
      let expGridDatas = [
        null,
        {x: 2, y: 3, w: 1, h: 1},
        {x: 1, y: 4, w: 3, h: 1}
      ];
      verify(gridDatas, 0, {x: 2, y: 1, w: 1, h: 2}, expGridDatas);
    });

    it('moves tile on the bottom right of a tile below a tile down ', () => {
      /**
       *  |_|
       *  |_|
       *  |___|
       *    |_|
       */
      let gridDatas = [
        {x: 1, y: 1, w: 1, h: 1},
        {x: 1, y: 2, w: 1, h: 1},
        {x: 1, y: 3, w: 3, h: 1},
        {x: 3, y: 4, w: 1, h: 1}
      ];
      let expGridDatas = [
        null,
        {x: 1, y: 3, w: 1, h: 1},
        {x: 1, y: 4, w: 3, h: 1},
        {x: 3, y: 5, w: 1, h: 1}
      ];
      verify(gridDatas, 0, {x: 1, y: 1, w: 1, h: 2}, expGridDatas);
    });

    it('moves tiles on the left ', () => {
      /**
       *   |_||_|
       *  |  |
       *  |__|
       *  |_|
       */
      let gridDatas = [
        {x: 2, y: 1, w: 1, h: 1},
        {x: 3, y: 1, w: 1, h: 1},
        {x: 1, y: 2, w: 2, h: 2},
        {x: 1, y: 4, w: 1, h: 1}
      ];
      let expGridDatas = [
        {x: 2, y: 2, w: 1, h: 1},
        null,
        {x: 1, y: 3, w: 2, h: 2},
        {x: 1, y: 5, w: 1, h: 1}
      ];
      verify(gridDatas, 1, {x: 2, y: 1, w: 2, h: 1}, expGridDatas);
    });

    it('moves tiles on the right with an empty row ', () => {
      /**
       *  | ||_|
       *  |_|
       *
       *  |__|
       *   |_|
       */
      let gridDatas = [
        {x: 1, y: 1, w: 1, h: 2},
        {x: 2, y: 1, w: 1, h: 1},
        {x: 1, y: 4, w: 2, h: 1},
        {x: 2, y: 5, w: 1, h: 1}
      ];
      let expGridDatas = [
        null,
        {x: 2, y: 3, w: 1, h: 1},
        {x: 1, y: 4, w: 2, h: 1},
        {x: 2, y: 5, w: 1, h: 1}
      ];
      verify(gridDatas, 0, {x: 1, y: 1, w: 2, h: 2}, expGridDatas);
    });

    it('does not move tiles on the same row', () => {
      /**
       * |_||_|
       */
      let gridDatas = [
        {x: 1, y: 1, w: 1, h: 1},
        {x: 2, y: 1, w: 1, h: 1}
      ];
      let expGridDatas = [
        null,
        {x: 2, y: 1, w: 1, h: 1}
      ];
      verify(gridDatas, 0, {x: 1, y: 1, w: 1, h: 2}, expGridDatas);
    });

    it('moves all tiles in a column', () => {
      /**
       * | ||_|
       * |_||_|
       */
      let gridDatas = [
        {x: 1, y: 1, w: 1, h: 2},
        {x: 2, y: 1, w: 1, h: 1},
        {x: 2, y: 2, w: 1, h: 1}
      ];
      let expGridDatas = [
        null,
        {x: 2, y: 3, w: 1, h: 1},
        {x: 2, y: 4, w: 1, h: 1}
      ];
      verify(gridDatas, 0, {x: 1, y: 1, w: 2, h: 2}, expGridDatas);
    });
  });
});
