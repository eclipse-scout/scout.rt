/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {graphics, GridData, InitModelOf, LogicalGridLayout, Point, Rectangle, Resizable, ResizableModel, scout, Tile, TileGrid, tileUtil} from '..';

/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
export class TileResizeHandler extends Resizable implements TileResizableModel {
  declare model: TileResizableModel;
  tileGrid: TileGrid;
  ignorer: (tile: Tile) => boolean;

  constructor(model: InitModelOf<TileResizeHandler>) {
    super(model);
    this.tileGrid = model.tileGrid;
    this.ignorer = model.ignorer;
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
   * @returns the cell bounds for the cell that is on the other side of the dragged edge.
   */
  protected _findInitialCell() {
    let tile = scout.widget(this.$container) as Tile;
    let gridData = tile.gridData;
    let row = gridData.y - 1;
    let col = gridData.x - 1;
    let w = gridData.w - 1;
    let h = gridData.h - 1;
    let layout = this.tileGrid.htmlComp.layout as LogicalGridLayout;
    let cellBounds = layout.info.cellBounds;
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

  protected _findCell(point: Point): Rectangle {
    let layout = this.tileGrid.htmlComp.layout as LogicalGridLayout;
    for (let row = 0; row < layout.info.rows; row++) {
      for (let col = 0; col < layout.info.cols; col++) {
        if (layout.info.cellBounds[row][col].contains(point)) {
          return layout.info.cellBounds[row][col].clone();
        }
      }
    }
    return null;
  }

  protected _relativeCursorPos($container: JQuery, event: JQuery.MouseEventBase): Point {
    let offset = graphics.offset($container);
    return new Point(
      event.pageX - offset.x,
      event.pageY - offset.y
    );
  }

  protected override _resizeEnd() {
    super._resizeEnd();
    let newBounds = this._context.currentBounds;
    if (newBounds.equals(this._context.initialBounds)) {
      return;
    }
    let tile = scout.widget(this.$container) as Tile;
    let newGridData = this._computeNewGridData(newBounds);
    let gridData = new GridData(tile.gridDataHints);
    gridData.w = newGridData.w;
    gridData.h = newGridData.h;
    // If x and y are less than 0 it will be automatically set by the Logical Grid -> don't override the values in that case
    if (tile.gridDataHints.x > 0) {
      gridData.x = newGridData.x;
    }
    if (tile.gridDataHints.y > 0) {
      gridData.y = newGridData.y;
    }
    this._moveOtherTiles(tile, gridData);
    tile.setGridDataHints(gridData);
  }

  protected _moveOtherTiles(resizedTile: Tile, newGridData: GridData) {
    tileUtil.moveOtherTilesDown(this.tileGrid.tiles, resizedTile, newGridData, this.ignorer);
  }

  protected _computeNewGridData(newBounds: Rectangle): GridData {
    let unionGridData;
    let layout = this.tileGrid.htmlComp.layout as LogicalGridLayout;
    for (let row = 0; row < layout.info.rows; row++) {
      for (let col = 0; col < layout.info.cols; col++) {
        let cellBounds = layout.info.cellBounds[row][col];
        if (newBounds.contains(cellBounds.point())) {
          let gridData = new Rectangle(col + 1, row + 1, 1, 1);
          if (!unionGridData) {
            unionGridData = gridData;
          } else {
            unionGridData = unionGridData.union(gridData);
          }
        }
      }
    }
    return new GridData({
      x: unionGridData.x,
      y: unionGridData.y,
      w: unionGridData.width,
      h: unionGridData.height
    });
  }
}

export interface TileResizableModel extends ResizableModel {
  tileGrid: TileGrid;
  ignorer?: (tile: Tile) => boolean;
}
