/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {GridData, GridDataModel, ObjectOrChildModel, scout, Tile, TileGrid, TileMatrix, tileUtil} from '../../src';

describe('tileUtil', () => {
  let session: SandboxSession;
  let placeholderIgnorer = tile => !!tile.placeholder;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  function createTiles(gridDatas: GridDataModel[], withPlaceholders = false) {
    let tileGrid = scout.create(TileGrid, {
      parent: session.desktop,
      tiles: gridDatas.map(data => ({
        objectType: Tile,
        gridDataHints: data
      }))
    });
    if (withPlaceholders) {
      tileGrid.insertTiles(createPlaceholders(tileUtil.buildMatrix(tileGrid.tiles), (x, y) => ({
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


  /**
   * Creates placeholders at the positions in the given matrix where no tiles are.
   */
  function createPlaceholders<T extends ObjectOrChildModel<Tile>>(matrix: TileMatrix, placeholderProducer: (x: number, y: number) => T): T[] {
    let placeholders = [];
    for (let x = matrix.x; x < matrix.x + matrix.width; x++) {
      for (let y = matrix.y; y < matrix.y + matrix.height; y++) {
        if (!matrix[x] || !matrix[x][y]) {
          let placeholder = placeholderProducer(x, y);
          if (placeholder) {
            placeholders.push(placeholder);
          }
        }
      }
    }
    return placeholders;
  }

  function assert(tiles: Tile[], gridDatas: GridDataModel[]) {
    for (let i = 0; i < gridDatas.length; i++) {
      if (gridDatas[i]) {
        expect(tiles[i].gridDataHints).withContext(`tile ${i}`).toEqual(new GridData(gridDatas[i]));
      }
    }
  }

  function verify(gridDatas: GridDataModel[], tileIndex: number, newGridData: GridDataModel, expectedDatas: GridDataModel[]) {
    let tiles = createTiles(gridDatas);
    tileUtil.moveOtherTilesDown(tiles, tiles[tileIndex], new GridData(newGridData));
    assert(tiles, expectedDatas);

    tiles = createTiles(gridDatas, true);
    tileUtil.moveOtherTilesDown(tiles, tiles[tileIndex], new GridData(newGridData), placeholderIgnorer);
    assert(tiles, expectedDatas);
  }

  describe('buildMatrix', () => {
    it('creates a matrix containing references to the given tiles at the points where these tiles are', () => {
      let tiles = createTiles([
        {x: 0, y: 0, w: 2, h: 2},
        {x: 2, y: 2, w: 2, h: 2}
      ]);
      let matrix = tileUtil.buildMatrix(tiles);
      expect(matrix[0][0]).toBe(tiles[0]);
      expect(matrix[1][0]).toBe(tiles[0]);
      expect(matrix[0][1]).toBe(tiles[0]);
      expect(matrix[1][1]).toBe(tiles[0]);

      expect(matrix[2][2]).toBe(tiles[1]);
      expect(matrix[3][2]).toBe(tiles[1]);
      expect(matrix[2][3]).toBe(tiles[1]);
      expect(matrix[3][3]).toBe(tiles[1]);

      expect(matrix[0][2]).toBe(undefined);
      expect(matrix[0][3]).toBe(undefined);
      expect(matrix[1][2]).toBe(undefined);
      expect(matrix[1][3]).toBe(undefined);

      expect(matrix[2][0]).toBe(undefined);
      expect(matrix[2][1]).toBe(undefined);
      expect(matrix[3][0]).toBe(undefined);
      expect(matrix[3][1]).toBe(undefined);

      // Tile does not start at 0/0
      tiles = createTiles([
        {x: 1, y: 1, w: 1, h: 1}
      ]);
      matrix = tileUtil.buildMatrix(tiles);
      expect(matrix[0]).toBe(undefined);
      expect(matrix[1][0]).toBe(undefined);
      expect(matrix[1][1]).toBe(tiles[0]);
      expect(matrix[1][2]).toBe(undefined);
      expect(matrix[2]).toBe(undefined);
    });

    it('sets x,y,width,height', () => {
      // No tiles
      let matrix = tileUtil.buildMatrix([]);
      expect(matrix.x).toBe(0);
      expect(matrix.y).toBe(0);
      expect(matrix.width).toBe(0);
      expect(matrix.height).toBe(0);

      // One tile
      let tiles = createTiles([
        {x: 0, y: 0, w: 2, h: 3}
      ]);
      matrix = tileUtil.buildMatrix(tiles);
      expect(matrix.x).toBe(0);
      expect(matrix.y).toBe(0);
      expect(matrix.width).toBe(2);
      expect(matrix.height).toBe(3);

      // More tiles, one starting at 0/0
      tiles = createTiles([
        {x: 0, y: 0, w: 2, h: 3},
        {x: 2, y: 3, w: 3, h: 4}
      ]);
      matrix = tileUtil.buildMatrix(tiles);
      expect(matrix.x).toBe(0);
      expect(matrix.y).toBe(0);
      expect(matrix.width).toBe(5);
      expect(matrix.height).toBe(7);

      // More tiles, no tile starts at 0/0
      tiles = createTiles([
        {x: 2, y: 3, w: 2, h: 3},
        {x: 3, y: 4, w: 3, h: 4}
      ]);
      matrix = tileUtil.buildMatrix(tiles);
      expect(matrix.x).toBe(2);
      expect(matrix.y).toBe(3);
      expect(matrix.width).toBe(4);
      expect(matrix.height).toBe(5);

      // First tile defines matrix height
      tiles = createTiles([
        {x: 2, y: 2, w: 1, h: 4},
        {x: 3, y: 2, w: 1, h: 2}
      ]);
      matrix = tileUtil.buildMatrix(tiles);
      expect(matrix.x).toBe(2);
      expect(matrix.y).toBe(2);
      expect(matrix.width).toBe(2);
      expect(matrix.height).toBe(4);

      // First tile defines matrix width
      tiles = createTiles([
        {x: 2, y: 2, w: 4, h: 1},
        {x: 2, y: 3, w: 2, h: 1}
      ]);
      matrix = tileUtil.buildMatrix(tiles);
      expect(matrix.x).toBe(2);
      expect(matrix.y).toBe(2);
      expect(matrix.width).toBe(4);
      expect(matrix.height).toBe(2);
    });
  });

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

    it('does not move tiles in an unaffected column on the right', () => {
      /**
       * |__|| |
       * |_| |_|
       */
      let gridDatas = [
        {x: 1, y: 1, w: 2, h: 1},
        {x: 3, y: 1, w: 1, h: 2},
        {x: 1, y: 2, w: 1, h: 1}
      ];
      let expGridDatas = [
        {x: 1, y: 1, w: 2, h: 1},
        {x: 3, y: 1, w: 1, h: 2},
        null
      ];
      verify(gridDatas, 2, {x: 1, y: 2, w: 2, h: 1}, expGridDatas);
    });

    it('does not move tiles in an unaffected column on the left', () => {
      /**
       * | ||__|
       * |_| |_|
       */
      let gridDatas = [
        {x: 1, y: 1, w: 1, h: 2},
        {x: 2, y: 1, w: 2, h: 1},
        {x: 3, y: 2, w: 1, h: 1}
      ];
      let expGridDatas = [
        {x: 1, y: 1, w: 1, h: 2},
        {x: 2, y: 1, w: 2, h: 1},
        null
      ];
      verify(gridDatas, 2, {x: 2, y: 2, w: 2, h: 1}, expGridDatas);
    });

    it('does not move tiles in an unaffected column far left', () => {
      /**
       * |  |
       * |__| |_|
       */
      let gridDatas = [
        {x: 1, y: 1, w: 2, h: 2},
        {x: 5, y: 2, w: 1, h: 1}
      ];
      let expGridDatas = [
        {x: 1, y: 1, w: 2, h: 2},
        null
      ];
      verify(gridDatas, 1, {x: 4, y: 2, w: 2, h: 1}, expGridDatas);
    });

    it('does not move tiles in an unaffected column far right', () => {
      /**
       *      |  |
       * |_|  |__|
       */
      let gridDatas = [
        {x: 1, y: 2, w: 1, h: 1},
        {x: 5, y: 1, w: 2, h: 2}
      ];
      let expGridDatas = [
        null,
        {x: 5, y: 1, w: 2, h: 2}
      ];
      verify(gridDatas, 0, {x: 1, y: 2, w: 2, h: 1}, expGridDatas);
    });

    it('does not move tile onto another moved tile', () => {
      /**
       *  |_|| |
       * |  || |
       * |__||_|
       */
      let gridDatas = [
        {x: 2, y: 1, w: 1, h: 1},
        {x: 3, y: 1, w: 1, h: 3},
        {x: 1, y: 2, w: 2, h: 2}
      ];
      let expGridDatas = [
        {x: 2, y: 4, w: 1, h: 1},
        null,
        {x: 1, y: 5, w: 2, h: 2}
      ];
      verify(gridDatas, 1, {x: 2, y: 1, w: 1, h: 3}, expGridDatas);
    });

    it('moves tile to the bottom of all moved tiles in the same column', () => {
      /**
       * |  ||_|| |
       * |__|   |_|
       */
      let gridDatas = [
        {x: 1, y: 1, w: 2, h: 2},
        {x: 3, y: 1, w: 1, h: 1},
        {x: 4, y: 1, w: 1, h: 2}
      ];
      let expGridDatas = [
        {x: 1, y: 3, w: 2, h: 2},
        {x: 3, y: 3, w: 1, h: 1},
        null
      ];
      verify(gridDatas, 2, {x: 1, y: 1, w: 4, h: 2}, expGridDatas);
    });
  });
});
