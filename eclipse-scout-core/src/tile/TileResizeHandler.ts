/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {graphics, InitModelOf, Insets, LogicalGridLayout, Point, Rectangle, Resizable, ResizableModel, scout, Tile, TileGrid, tileUtil} from '..';

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
    let halfHgap = layoutInfo.hgap / 2;
    let halfVgap = layoutInfo.vgap / 2;
    for (let row = 0; row < layoutInfo.rows; row++) {
      for (let col = 0; col < layoutInfo.cols; col++) {
        // Enlarge the cell bounds by half of the gaps size to make resizing more reactive
        // -> This makes the whole area inside the grid reactive not only the cells
        // Example: if the user resizes a cell that spans two columns and moves the cursor to the bottom between two other cells,
        // it should resize to one of the two cells instead of doing nothing
        let topGap = row > 0 ? halfVgap : 0; // Don't add top gap for first row
        let bottomGap = row < layoutInfo.rows - 1 ? halfVgap : 0; // Don't add bottom gap for last row
        let leftGap = col > 0 ? halfHgap : 0; // Don't add left gap for first col
        let rightGap = col < layoutInfo.cols - 1 ? halfHgap : 0; // Don't add right gap for last col
        let cellBounds = layoutInfo.cellBounds[row][col];
        if (cellBounds.add(new Insets(topGap, rightGap, bottomGap, leftGap)).contains(position)) {
          return cellBounds.clone();
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
    this.tileGrid.resizeTile(resizedTile, logicalBounds, this.ignorer);
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
