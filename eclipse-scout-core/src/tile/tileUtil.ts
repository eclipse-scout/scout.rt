/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {GridData, ObjectOrChildModel, Tile} from '..';

export const tileUtil = {
  buildMatrix(tiles: Tile[], minWidth = 0, minHeight = 0): TileMatrix {
    let matrix: TileMatrix = [[]];
    matrix.maxWidth = minWidth - 1;
    matrix.maxHeight = minHeight - 1;
    for (let tile of tiles) {
      const gridData = tile.gridDataHints;
      for (let x = gridData.x; x < gridData.x + gridData.w; x++) {
        if (!matrix[x]) {
          matrix[x] = [];
          matrix.maxWidth = Math.max(matrix.maxWidth, x);
        }
        for (let y = gridData.y; y < gridData.y + gridData.h; y++) {
          matrix[x][y] = tile;
          matrix.maxHeight = Math.max(matrix.maxHeight, y);
        }
      }
    }
    return matrix;
  },

  moveOtherTilesDown(tiles: Tile[], resizedTile: Tile, newGridData: GridData, ignore?: (tile: Tile) => boolean) {
    let matrix = tileUtil.buildMatrix(tiles);
    let movedTiles = new Map();
    let numTilesMoved = 0;
    let i = 0;
    do {
      numTilesMoved = movedTiles.size;
      moveTilesDown();
      if (i > 10) {
        $.log.error(`Endless moving of tiles for matrix ${matrix} and newGridData ${newGridData}`);
      }
      i++;
      // If tiles have been moved, do it again in case more tiles need to be moved
      // This is necessary e.g. if a tile in the first column will be moved because of a tile in the second column
      // but the resized tile does not lap into the first column
    } while (movedTiles.size > numTilesMoved);

    for (const [tile, gridData] of movedTiles.entries()) {
      tile.setGridDataHints(gridData);
    }

    function moveTilesDown() {
      for (let x = 1; x <= matrix.maxWidth; x++) {
        if (!matrix[x]) {
          // No tiles in that column
          continue;
        }
        let topGridData = newGridData;
        for (let y = newGridData.y; y <= matrix.maxHeight; y++) {
          let tile = matrix[x][y];
          if (resizedTile === tile) {
            continue;
          }
          if (!tile || ignore && ignore(tile)) {
            continue;
          }
          if (movedTiles.has(tile)) {
            // Tile has already been moved, skip it
            // Use as new top boundary (tile may have been moved by processing another column)
            topGridData = movedTiles.get(tile);
            continue;
          }
          let gridData = new GridData(tile.gridDataHints);
          if (gridData.y >= topGridData.y && !topGridData.toRectangle().intersects(gridData.toRectangle())) {
            // If the tile does not overlap with the resized or moved tile, skip it
            continue;
          }
          let newY = topGridData.y + topGridData.h;
          if (gridData.y >= newY) {
            // Do not move if tile is already at the correct position or below it (ensures tiles won't be moved up)
            continue;
          }
          // Move it below the resized or moved tile
          gridData.y = newY;
          topGridData = gridData;
          movedTiles.set(tile, gridData);
        }
      }
    }
  },

  createPlaceholders<T extends ObjectOrChildModel<Tile>>(matrix: TileMatrix, placeholderProducer: (x: number, y: number) => T): T[] {
    let placeholders = [];
    for (let x = 1; x < matrix.maxWidth + 2; x++) {
      for (let y = 1; y < matrix.maxHeight + 2; y++) {
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
};

export type TileMatrix = Tile[][] & {
  maxWidth?: number;
  maxHeight?: number;
};
