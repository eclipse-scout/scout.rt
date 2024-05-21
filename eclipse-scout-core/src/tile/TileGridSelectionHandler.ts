/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, HorizontalGrid, PlaceholderTile, Tile, TileGrid} from '../index';
import $ from 'jquery';

export interface TileGridSelectionInstruction {
  selectedTiles: Tile[];
  focusedTile: Tile;
}

export class TileGridSelectionHandler {
  tileGrid: TileGrid;

  constructor(tileGrid: TileGrid) {
    this.tileGrid = tileGrid;
  }

  selectTileOnMouseDown(event: JQuery.MouseDownEvent) {
    if (!this.isSelectable()) {
      return;
    }

    let $tile = $(event.currentTarget);
    let tile = $tile.data('widget');
    if (tile instanceof PlaceholderTile) {
      return;
    }

    if (tile.selected && event.which === 3) {
      // Do not toggle if context menus should be shown and tile already is selected
      return;
    }

    // Clicking a tile selects it, CTRL-click toggles the selection
    let selected = true;
    if (event.ctrlKey) {
      selected = !tile.selected;
    }

    // If multiSelect is enabled, CTRL-Click on a tile adds or removes that tile to or from the selection
    if (event.ctrlKey && this.isMultiSelect()) {
      if (selected) {
        this.addTilesToSelection(tile);
        this._checkAndSetFocusedTile(event, tile);
      } else {
        this.deselectTile(tile);
        this._checkAndSetFocusedTile(event, null);
      }
      return;
    }

    // Shift-Click adds or removes the tiles between the last focused tile and the clicked tile to or from the selection
    if (event.shiftKey && this.isMultiSelect()) {
      if (!this.isHorizontalGridActive()) {
        return;
      }
      let tiles = this.getVisibleTiles();
      let focusedTile = this.getFocusedTile();
      if (!focusedTile) {
        focusedTile = tiles[0];
      }
      let result = this.computeSelectionBetween(tiles.indexOf(focusedTile), tiles.indexOf(tile), true);
      if (result) {
        this.selectTiles(result.selectedTiles);
        this._checkAndSetFocusedTile(event, result.focusedTile);
      }
      return;
    }

    // If multi selection is disabled or no CTRL key is pressed, only the clicked tile may be selected
    if (selected) {
      this.selectTile(tile);
      this._checkAndSetFocusedTile(event, tile);
    } else {
      this.deselectAllTiles();
      this._checkAndSetFocusedTile(event, null);
    }
  }

  getFilteredTiles(): Tile[] {
    return this.tileGrid.filteredTiles;
  }

  getFilteredTileCount(): number {
    return this.tileGrid.filteredTiles.length;
  }

  getVisibleTiles(): Tile[] {
    return this.tileGrid.filteredTiles;
  }

  getVisibleTileCount(): number {
    return this.tileGrid.filteredTiles.length;
  }

  getGridColumnCount(): number {
    return this.tileGrid.gridColumnCount;
  }

  getVisibleGridRowCount(): number {
    return this.tileGrid.logicalGrid.gridRows;
  }

  getVisibleGridX(tile: Tile): number {
    return tile.gridData.x;
  }

  getVisibleGridY(tile: Tile): number {
    return tile.gridData.y;
  }

  getSelectedTiles(): Tile[] {
    return this.tileGrid.selectedTiles;
  }

  isSelectable(): boolean {
    return this.tileGrid.selectable;
  }

  isMultiSelect(): boolean {
    return this.tileGrid.multiSelect;
  }

  addTilesToSelection(tiles: Tile[]) {
    this.tileGrid.addTilesToSelection(tiles);
  }

  selectTile(tile: Tile) {
    this.tileGrid.selectTile(tile);
  }

  selectTiles(tiles: Tile[]) {
    this.tileGrid.selectTiles(tiles);
  }

  deselectTile(tile: Tile) {
    this.tileGrid.deselectTile(tile);
  }

  deselectTiles(tiles: Tile[]) {
    this.tileGrid.deselectTiles(tiles);
  }

  deselectAllTiles() {
    this.tileGrid.deselectAllTiles();
  }

  toggleSelection() {
    this.tileGrid.toggleSelection();
  }

  getFocusedTile(): Tile {
    return this.tileGrid.focusedTile;
  }

  /**
   * @returns the focused tile if there is one.
   *          Otherwise, returns the last tile of the selection if diff is > 0 and the first tile if diff is <= 0.
   *          If there is no focused tile and no selection, null is returned.
   */
  computeFocusedTile(diff = 1): Tile {
    // Focused tile may be null if tile has been deleted or if the user has not made a selection before
    let focusedTile = this.getFocusedTile();
    if (focusedTile) {
      return focusedTile;
    }
    let selectedTiles = this.getSelectedTiles();
    if (selectedTiles.length === 0) {
      return null;
    }
    if (diff > 0) {
      // Navigate down/right
      return arrays.last(selectedTiles);
    }
    // Navigate up/left
    return arrays.first(selectedTiles);
  }

  /**
   * Only sets the focus if event does not prevent the default and the tile does not have the class 'unfocusable'.
   */
  protected _checkAndSetFocusedTile(event: JQuery.MouseDownEvent, tile: Tile) {
    if (event.isDefaultPrevented()) {
      return;
    }
    if (tile && tile.rendered && tile.$container.hasClass('unfocusable')) {
      return;
    }
    this.setFocusedTile(tile);
  }

  setFocusedTile(tile: Tile) {
    this.tileGrid.setFocusedTile(tile);
  }

  scrollTo(tile: Tile) {
    this.tileGrid.scrollTo(tile);
  }

  scrollToTop() {
    this.tileGrid.scrollToTop();
  }

  scrollToBottom() {
    this.tileGrid.scrollToBottom();
  }

  findVisibleTileIndexAt(x: number, y: number, startIndex?: number, reverse?: boolean): number {
    return this.tileGrid.findTileIndexAt(x, y, startIndex, reverse);
  }

  getTileGridByRow(rowIndex: number): TileGrid {
    if (rowIndex < 0 || rowIndex >= this.getVisibleGridRowCount()) {
      return null;
    }
    return this.tileGrid;
  }

  isHorizontalGridActive(): boolean {
    return this.tileGrid.logicalGrid instanceof HorizontalGrid;
  }

  computeSelectionX(xDiff: number, extend: boolean): TileGridSelectionInstruction {
    let result = this._computeFocusedTileOrSelection(xDiff);
    if (result.selectedTiles) {
      // New selection could be determined already -> return it;
      return result;
    }

    let focusedTile = result.focusedTile;
    let tiles = this.getVisibleTiles();
    let focusedTileIndex = tiles.indexOf(focusedTile);
    let focusedTileRow = this.getVisibleGridY(focusedTile);
    let focusedTileColumn = this.getVisibleGridX(focusedTile);
    // Look for the tile in the next or previous column (depending on xDiff) in the same row.
    // We cannot just take the next tile in the tiles array because tiles may span multiple rows (h > 1), so the tile next to the focused tile is not necessarily the next tile in the array.
    let newFocusedTileIndex = this.findVisibleTileIndexAt(focusedTileColumn + xDiff, focusedTileRow, xDiff > 0 ? 0 : focusedTileIndex, xDiff < 0);
    if (newFocusedTileIndex < 0) {
      // newFocusedTileIndex may be -1 if focusedTile is in the last column and xDiff > 0 or in the first column and xDiff < 0
      // In that case, just take the next tile
      newFocusedTileIndex = focusedTileIndex + (xDiff > 0 ? 1 : -1);
    }
    return this.computeSelectionBetween(focusedTileIndex, newFocusedTileIndex, extend);
  }

  computeSelectionY(yDiff: number, extend: boolean): TileGridSelectionInstruction {
    let result = this._computeFocusedTileOrSelection(yDiff);
    if (result.selectedTiles) {
      // New selection could be determined already -> return it;
      return result;
    }

    let rowCount = this.getVisibleGridRowCount();
    let focusedTile = result.focusedTile;
    let focusedTileRow = this.getVisibleGridY(focusedTile);
    let focusedTileColumn = this.getVisibleGridX(focusedTile);
    if (yDiff > 0 && focusedTileRow === rowCount - 1 ||
      yDiff < 0 && focusedTileRow === 0) {
      // Do nothing if focused tile is in the last row (navigate down) or first row (navigate up)
      return;
    }

    let tiles = this.getVisibleTiles();
    let focusedTileIndex = tiles.indexOf(focusedTile);
    let newFocusedTileIndex = this.findVisibleTileIndexAt(focusedTileColumn, focusedTileRow + yDiff, focusedTileIndex, yDiff < 0);
    if (newFocusedTileIndex < 0) {
      let tileGrid = this.getTileGridByRow(focusedTileRow + yDiff);
      if (!tileGrid) {
        return;
      }
      newFocusedTileIndex = tiles.indexOf(arrays.last(tileGrid.filteredTiles));
    }
    return this.computeSelectionBetween(focusedTileIndex, newFocusedTileIndex, extend);
  }

  computeSelectionToFirst(extend: boolean): TileGridSelectionInstruction {
    let tiles = this.getVisibleTiles();
    let selectedTiles = this.getSelectedTiles();
    if (selectedTiles.length === 0) {
      // Select first tile if no tiles are selected
      let focusedTile = arrays.first(tiles);
      return {
        selectedTiles: [focusedTile],
        focusedTile: focusedTile
      };
    }

    let focusedTileIndex = tiles.indexOf(this.computeFocusedTile());
    return this.computeSelectionBetween(focusedTileIndex, 0, extend);
  }

  computeSelectionToLast(extend: boolean): TileGridSelectionInstruction {
    let tiles = this.getVisibleTiles();
    let selectedTiles = this.getSelectedTiles();
    if (selectedTiles.length === 0) {
      // Select last tile if no tiles are selected
      let focusedTile = arrays.last(tiles);
      return {
        selectedTiles: [focusedTile],
        focusedTile: focusedTile
      };
    }
    let focusedTileIndex = tiles.indexOf(this.computeFocusedTile());
    return this.computeSelectionBetween(focusedTileIndex, tiles.length - 1, extend);
  }

  /**
   * If no tiles are selected, an object is returned containing the tile that should be focused and selected.
   * This is the first tile if diff is > 0 and the last tile otherwise.
   *
   * If there are tiles selected but there is no focused tile, an object is returned containing the tile to be focused.
   * This is the last tile if diff is > 0 and the first tile otherwise.
   */
  protected _computeFocusedTileOrSelection(diff: number): TileGridSelectionInstruction {
    let tiles = this.getVisibleTiles();
    let selectedTiles = this.getSelectedTiles();
    if (selectedTiles.length === 0) {
      let focusedTile;
      if (diff > 0) {
        // Select first tile if no tiles are selected (navigate down/right)
        focusedTile = arrays.first(tiles);
      } else {
        // Select last tile if no tiles are selected (navigate up/left)
        focusedTile = arrays.last(tiles);
      }
      return {
        focusedTile: focusedTile,
        selectedTiles: [focusedTile]
      };
    }

    return {
      focusedTile: this.computeFocusedTile(diff),
      selectedTiles: null
    };
  }

  computeSelectionBetween(focusedTileIndex: number, newFocusedTileIndex: number, extend: boolean): TileGridSelectionInstruction {
    let tiles = this.getVisibleTiles();
    let newFocusedTile = tiles[newFocusedTileIndex];
    if (focusedTileIndex < 0 || focusedTileIndex > tiles.length - 1 ||
      newFocusedTileIndex < 0 || newFocusedTileIndex > tiles.length - 1 ||
      focusedTileIndex === newFocusedTileIndex) {
      // Do nothing if indices are out of bounds or equal
      return;
    }

    if (!extend) {
      // Select only the tile at the newFocusedTileIndex
      return {
        selectedTiles: [newFocusedTile],
        focusedTile: newFocusedTile
      };
    }

    // Adjust existing selection
    let selectedTiles = this.getSelectedTiles();
    let newSelectedTiles = [];
    if (!newFocusedTile.selected) {
      // Add all tiles between focused tile and newly focused tile to selection
      if (newFocusedTileIndex > focusedTileIndex) {
        newSelectedTiles = arrays.union(selectedTiles, tiles.slice(focusedTileIndex, newFocusedTileIndex + 1));
        newFocusedTile = this._findLastSelectedTileAfter(tiles, newFocusedTileIndex);
      } else {
        newSelectedTiles = arrays.union(tiles.slice(newFocusedTileIndex, focusedTileIndex + 1), selectedTiles);
        newFocusedTile = this._findLastSelectedTileBefore(tiles, newFocusedTileIndex);
      }
    } else {
      if (newFocusedTileIndex > focusedTileIndex) {
        // Remove all tiles between focused tile and newly focused tile from selection if newly focused tile already is selected
        newSelectedTiles = selectedTiles.slice();
        arrays.removeAll(newSelectedTiles, tiles.slice(focusedTileIndex, newFocusedTileIndex));
      } else {
        newSelectedTiles = selectedTiles.slice();
        arrays.removeAll(newSelectedTiles, tiles.slice(newFocusedTileIndex + 1, focusedTileIndex + 1));
      }
    }

    return {
      selectedTiles: newSelectedTiles,
      focusedTile: newFocusedTile
    };
  }

  executeSelection(instruction: TileGridSelectionInstruction) {
    if (!instruction) {
      return;
    }
    if (instruction.selectedTiles.length > 0) {
      this.selectTiles(instruction.selectedTiles);
      this.scrollTo(instruction.focusedTile);

      // Scroll to the very top or very bottom if newly focused tile is on top or on bottom
      // Especially important for tile accordion because scrolling to top should reveal the group header as well
      let focusedTileRow = this.getVisibleGridY(instruction.focusedTile);
      let rowCount = this.getVisibleGridRowCount();
      if (focusedTileRow === 0) {
        this.scrollToTop();
      } else if (focusedTileRow === rowCount - 1) {
        this.scrollToBottom();
      }
    }
    this.setFocusedTile(instruction.focusedTile);
  }

  /**
   * Searches for the last selected tile in the current selection block, starting from tileIndex. Expects tile at tileIndex to be selected.
   */
  protected _findLastSelectedTileBefore(tiles: Tile[], tileIndex: number): Tile {
    if (tileIndex === 0) {
      return tiles[tileIndex];
    }
    let tile = arrays.findFromReverse(tiles, tileIndex, (tile, i) => {
      let previousTile = tiles[i - 1];
      if (!previousTile) {
        return false;
      }
      return !previousTile.selected;
    });
    // when no tile has been found, use first tile in tileGrid
    if (!tile) {
      tile = tiles[0];
    }
    return tile;
  }

  /**
   * Searches for the last selected tile in the current selection block, starting from tileIndex. Expects tile at tileIndex to be selected.
   */
  protected _findLastSelectedTileAfter(tiles: Tile[], tileIndex: number): Tile {
    if (tileIndex === tiles.length - 1) {
      return tiles[tileIndex];
    }
    let tile = arrays.findFrom(tiles, tileIndex, (tile, i) => {
      let nextTile = tiles[i + 1];
      if (!nextTile) {
        return false;
      }
      return !nextTile.selected;
    });
    // when no tile has been found, use last tile in tileGrid
    if (!tile) {
      tile = tiles[tiles.length - 1];
    }
    return tile;
  }
}
