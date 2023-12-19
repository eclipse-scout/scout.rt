/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {GridData, Tile} from '..';

export const tileUtil = {

  /**
   * Creates a matrix containing references to the given tiles at the points where these tiles are.
   * If the tiles span some columns or rows, the spanned cells reference the tiles as well.
   *
   * Example: If a tile has a `gridData` with `x = 2`, `y = 4`, `w = 2`, `h = 1` it will be at position `matrix[2][4]` and `matrix[3][4]`.
   *
   * It uses gridDataHints if x/y are set explicitly. Otherwise, gridData is used.
   * Tiles with no gridData set or whose gridData.x or y is < 0 are ignored.
   *
   * @param minWidth the {@link TileMatrix.width} will have at least this value
   * @param minHeight the {@link TileMatrix.height} will have at least this value
   * @returns a two-dimensional array where the first dimension is the x-axis and the second the y-axis.
   */
  buildMatrix(tiles: Tile[], minWidth = 0, minHeight = 0): TileMatrix {
    let matrix: TileMatrix = $.extend([], {
      x: tiles.length > 0 ? Number.MAX_SAFE_INTEGER : 0,
      y: tiles.length > 0 ? Number.MAX_SAFE_INTEGER : 0,
      width: minWidth,
      height: minHeight
    });
    for (let tile of tiles) {
      // If tiles use explicit x/y values work with hints because they are always set. GridData will only be set after layouting.
      let gridData = tile.gridDataHints;
      if (gridData.x < 0 || gridData.y < 0) {
        // Use gridData object instead if available
        gridData = tile.gridData;
        if (!gridData || gridData.x < 0 || gridData.y < 0) {
          continue;
        }
      }
      matrix.x = Math.min(matrix.x, gridData.x);
      matrix.y = Math.min(matrix.y, gridData.y);
      for (let x = gridData.x; x < gridData.x + gridData.w; x++) {
        if (!matrix[x]) {
          matrix[x] = [];
          matrix.width = Math.max(matrix.width, minWidth, matrix.length - matrix.x);
        }
        for (let y = gridData.y; y < gridData.y + gridData.h; y++) {
          matrix[x][y] = tile;
          matrix.height = Math.max(matrix.height, minHeight, matrix[x].length - matrix.y);
        }
      }
    }
    return matrix;
  },

  /**
   * Moves the tiles down that intersect with the resized tile to make space for the resized tile.
   * Existing tiles on the bottom will be pushed down as well if they are in the way.
   *
   * This only works for tiles with an explicit position which means it only works if gridDataHints.x and gridDataHints.y are greater than or equal to 0.
   * Tiles without an explicit position are ignored because they are arranged automatically by the logical grid.
   *
   * @param ignore If the function returns true the passed tile will be ignored and never be moved. This is typically used for placeholder tiles.
   */
  moveOtherTilesDown(tiles: Tile[], resizedTile: Tile, newGridData: GridData, ignore?: (tile: Tile) => boolean) {
    let matrix = tileUtil.buildMatrix(tiles);
    let movedTiles = new Map();

    moveTilesDown();

    for (const [tile, gridData] of movedTiles.entries()) {
      tile.setGridDataHints(gridData);
    }

    function moveTilesDown() {
      /** The tiles will be moved to the bottom of these grid data positions */
      let bottomGridDataPerColumn = [];

      // Initially, only the resized tile is in the list
      // If a tile is moved, the list will be updated to make sure other tiles won't be moved to the same place
      updateBottomGridData(newGridData, bottomGridDataPerColumn);

      for (let y = newGridData.y; y < matrix.y + matrix.height; y++) {
        for (let x = matrix.x; x < matrix.x + matrix.width; x++) {
          if (!matrix[x]) {
            // No tiles in that column
            continue;
          }
          let tile = matrix[x][y];
          if (resizedTile === tile) {
            continue;
          }
          if (!tile || ignore && ignore(tile)) {
            // Ignored tiles (e.g. placeholders) must never be moved
            continue;
          }
          if (movedTiles.has(tile)) {
            // Tile has already been moved, skip it
            continue;
          }

          let gridData = new GridData(tile.gridDataHints);
          if (gridData.x < 0 || gridData.y < 0) {
            // Ignore tiles without explicit positions, they will be arranged automatically by the logical grid
            continue;
          }
          let bottomGridData = bottomGridDataPerColumn[x];
          if (!bottomGridData) {
            // If tile is not inside the affected columns, skip it
            continue;
          }
          if (gridData.y >= bottomGridData.y && !bottomGridData.toRectangle().intersects(gridData.toRectangle())) {
            // If the tile is below the resized or moved tile and does not overlap with it, skip it
            continue;
          }
          let newY = bottomGridData.y + bottomGridData.h;
          if (gridData.y >= newY) {
            // Do not move if tile is already at the correct position or below it (ensures tiles won't be moved up)
            continue;
          }
          // Move it below the resized or moved tile
          gridData.y = newY;
          movedTiles.set(tile, gridData);
          updateBottomGridData(gridData, bottomGridDataPerColumn);
        }
      }
    }

    /**
     * Replaces the grid data in the given bottomGridDataPerColumn at the columns the new grid data spans
     * if the new grid data is further down than the existing grid data in the spanned columns.
     */
    function updateBottomGridData(gridData: GridData, bottomGridDataPerColumn: GridData[]) {
      for (let x = gridData.x; x < gridData.x + gridData.w; x++) {
        if (!bottomGridDataPerColumn[x] || (gridData.y + gridData.h > bottomGridDataPerColumn[x].y + bottomGridDataPerColumn[x].h)) {
          bottomGridDataPerColumn[x] = gridData;
        }
      }
    }
  }
};

/**
 * A two-dimensional array where the first dimension is the x-axis and the second the y-axis.
 */
export type TileMatrix = Tile[][] & {
  /**
   * The starting point of the x-axis array. It is the lowest x of all grid datas in the matrix, never smaller than 0.
   */
  x: number;
  /**
   * The starting point of the y-axis array. It is the lowest y of all grid datas in the matrix, never smaller than 0.
   */
  y: number;
  /**
   * The length of the x-axis array adjusted by {@link x}.
   * It is the number of columns between the left most tile and the right side of the right most tile in the matrix.
   */
  width: number;
  /**
   * The length of the y-axis array adjusted by {@link y}.
   * It is the number of rows between the uppermost tile and the bottom side of the lowest tile in the matrix.
   */
  height: number;
};
