/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, graphics, GridData, InitModelOf, LogicalGridLayout, Point, Predicate, Rectangle, Resizable, ResizableModel, scout, Tile, TileGrid, tileUtil} from '..';

export class TileResizeHandler extends Resizable implements TileResizeHandlerModel {
  declare model: TileResizeHandlerModel;
  tileGrid: TileGrid;
  ignorer: (tile: Tile) => boolean;

  override init(model: InitModelOf<TileResizeHandler>) {
    super.init(model);
    this.tileGrid = model.tileGrid;
    this.ignorer = model.ignorer;
  }

  get layout(): LogicalGridLayout {
    return this.tileGrid.htmlComp.layout as LogicalGridLayout;
  }

  protected override _computeBounds(event: JQuery.MouseMoveEvent): Rectangle {
    let cell = this._findCell(this._relativeCursorPos(this.tileGrid.$container, event));
    if (!cell) {
      return;
    }
    let initialCell = this._findInitialCell();
    if (scout.isOneOf(this._context.edge, 'w', 'e')) {
      // If resized horizontally, height must not change
      cell.y = initialCell.y;
      cell.height = initialCell.height;
    } else if (scout.isOneOf(this._context.edge, 'n', 's')) {
      // If resized vertically, width must not change
      cell.x = initialCell.x;
      cell.width = initialCell.width;
    }
    return initialCell.union(cell);
  }

  /**
   * @returns the logical bounds for the new range
   */
  protected _computeLogicalBounds(newBounds: Rectangle): Rectangle {
    let unionBounds;
    let layoutInfo = this.layout.info;
    for (let row = 0; row < layoutInfo.rows; row++) {
      for (let col = 0; col < layoutInfo.cols; col++) {
        let cellBounds = layoutInfo.cellBounds[row][col];
        if (newBounds.contains(cellBounds.point())) {
          let bounds = new Rectangle(col, row, 1, 1);
          if (!unionBounds) {
            unionBounds = bounds;
          } else {
            unionBounds = unionBounds.union(bounds);
          }
        }
      }
    }
    return unionBounds;
  }

  /**
   * @returns the cell bounds for the cell that is on the other side of the dragged edge.
   */
  protected _findInitialCell(): Rectangle {
    let tile = scout.widget(this.$container) as Tile;
    // Find the actual grid data object.
    // Compared to tile.gridData it may contain adjusted x/y values because of collapsed rows/cols
    // or if gridDataHints were used with explicit x/y values that don't start from 0
    let gridData = this.layout.info.gridDatas.find(gd => gd.widget === tile);
    let row = gridData.gridy;
    let col = gridData.gridx;
    let w = gridData.gridw - 1;
    let h = gridData.gridh - 1;
    let cellBounds = this.layout.info.cellBounds;
    switch (this._context.edge) {
      case 'n':
        return cellBounds[row + h][col].union(cellBounds[row + h][col + w]);
      case 'ne':
        return cellBounds[row + h][col];
      case 'e':
        return cellBounds[row][col].union(cellBounds[row + h][col]);
      case 'se':
        return cellBounds[row][col];
      case 's':
        return cellBounds[row][col].union(cellBounds[row][col + w]);
      case 'sw':
        return cellBounds[row][col + w];
      case 'w':
        return cellBounds[row][col + w].union(cellBounds[row + h][col + w]);
      case 'nw':
        return cellBounds[row + h][col + w];
    }
  }

  /**
   * @returns the cell bounds at the given position
   */
  protected _findCell(position: Point): Rectangle {
    let layoutInfo = this.layout.info;
    for (let row = 0; row < layoutInfo.rows; row++) {
      for (let col = 0; col < layoutInfo.cols; col++) {
        if (layoutInfo.cellBounds[row][col].contains(position)) {
          return layoutInfo.cellBounds[row][col].clone();
        }
      }
    }
    return null;
  }

  /**
   * @returns the position of the cursor relative to the given container
   */
  protected _relativeCursorPos($container: JQuery, event: JQuery.MouseEventBase): Point {
    let scrollPos = new Point($container[0].scrollLeft, $container[0].scrollTop);
    let offset = graphics.offset($container);
    return new Point(event.pageX, event.pageY).subtract(offset).add(scrollPos);
  }

  protected override _resizeEnd() {
    let newBounds = this._context.currentBounds;
    if (newBounds.equals(this._context.initialBounds)) {
      return;
    }
    let resizedTile = scout.widget(this.$container) as Tile;
    let logicalBounds = this._computeLogicalBounds(newBounds);
    let gridData = this._computeGridData(resizedTile, logicalBounds);
    this._moveTiles(resizedTile, gridData, logicalBounds);
    resizedTile.setGridDataHints(gridData);
  }

  protected _computeGridData(resizedTile: Tile, logicalBounds: Rectangle): GridData {
    let gridData = resizedTile.gridDataHints.clone({
      w: logicalBounds.width,
      h: logicalBounds.height
    });
    // If x and y are less than 0 it will be automatically set by the Logical Grid -> don't override the values in that case
    // The LogicalGridLayoutInfo removes empty rows and columns so the x/y values of the grid cells may not match the x/y values of the gridDataHints -> calculate the diffs
    let logicalGridData = this.layout.info.gridDatas.find(gd => gd.widget === resizedTile);
    if (resizedTile.gridDataHints.x >= 0) {
      let diffX = resizedTile.gridDataHints.x - logicalGridData.gridx;
      gridData.x = logicalBounds.x + diffX;
    }
    if (resizedTile.gridDataHints.y >= 0) {
      let diffY = resizedTile.gridDataHints.y - logicalGridData.gridy;
      gridData.y = logicalBounds.y + diffY;
    }
    return gridData;
  }

  protected _moveTiles(resizedTile: Tile, gridData: GridData, logicalBounds: Rectangle) {
    if (resizedTile.gridDataHints.x >= 0 && resizedTile.gridDataHints.y >= 0) {
      // If explicit x/y values are used, move the other tiles by adjusting their x/y values explicitly
      this._moveOtherTilesExplicitly(resizedTile, gridData);
    } else {
      // If the grid is automatically arranged, just move the resized tile to the new position and the other tiles will adjust automatically.
      // The tile needs to be moved if the top left corner of the bounds was moved to a different position on the grid.
      let tiles = this.tileGrid.tiles.slice();
      let topLeftTile = this._findTileBefore(tiles, logicalBounds.point(), t => t !== resizedTile);
      if (!topLeftTile) {
        tiles = arrays.moveTo(tiles, resizedTile, 0);
        this.tileGrid.setTiles(tiles);
      } else if (topLeftTile !== resizedTile) {
        this.tileGrid.moveTileAfter(resizedTile, topLeftTile);
      }
    }
  }

  protected _moveOtherTilesExplicitly(resizedTile: Tile, gridData: GridData) {
    tileUtil.moveOtherTilesDown(this.tileGrid.tiles, resizedTile, gridData, this.ignorer);
  }

  /**
   * @returns the tile before the given position. Only tiles accepted by the filter are considered.
   */
  protected _findTileBefore(tiles: Tile[], position: Point, filter?: Predicate<Tile>): Tile {
    let tileBefore;
    let matrix = tileUtil.buildMatrix(tiles);
    for (let y = matrix.y; y < matrix.y + matrix.height; y++) {
      for (let x = matrix.x; x < matrix.x + matrix.width; x++) {
        let tile;
        if (matrix[x] && matrix[x][y] && (!filter || filter(matrix[x][y]))) {
          tile = matrix[x][y];
        }
        // If the searched position is reached, return the tile before that position.
        // If the position is not reached yet but there is a tile spanning into that position, abort to return the tile before.
        if (position.equals(new Point(x, y)) || (tile && tile.gridData.toRectangle().contains(position))) {
          return tileBefore;
        }
        if (tile) {
          tileBefore = matrix[x][y];
        }
      }
    }
    return tileBefore;
  }
}

export interface TileResizeHandlerModel extends ResizableModel {
  tileGrid: TileGrid;
  /**
   * A function that can return true for tiles that should be ignored when tiles are being moved down to make room for the resized tile.
   * This is typically used for placeholder tiles.
   *
   * @see tileUtil.moveOtherTilesDown
   */
  ignorer?: (tile: Tile) => boolean;
}
