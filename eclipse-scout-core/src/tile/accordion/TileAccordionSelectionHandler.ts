/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HorizontalGrid, Tile, TileAccordion, TileGridSelectionHandler} from '../../index';
import TileGrid from '../TileGrid';

export default class TileAccordionSelectionHandler extends TileGridSelectionHandler {
  /** The difference to the main selectionHandler is that this one works on the TileAccordion rather than on the TileGrid */
  tileAccordion: TileAccordion;

  constructor(tileAccordion: TileAccordion) {
    super(tileAccordion as unknown as TileGrid); // Not all methods are overridden (e.g. getGridColumnCount)
    this.tileAccordion = tileAccordion;
  }

  override getFilteredTiles(): Tile[] {
    return this.tileAccordion.getFilteredTiles();
  }

  override getFilteredTileCount(): number {
    return this.tileAccordion.getFilteredTileCount();
  }

  override getVisibleTiles(): Tile[] {
    return this.tileAccordion.getVisibleTiles();
  }

  override getVisibleTileCount(): number {
    return this.tileAccordion.getVisibleTileCount();
  }

  override getSelectedTiles(): Tile[] {
    return this.tileAccordion.getSelectedTiles();
  }

  override getFocusedTile(): Tile {
    return this.tileAccordion.getFocusedTile();
  }

  override getVisibleGridRowCount(): number {
    return this.tileAccordion.getVisibleGridRowCount();
  }

  override getVisibleGridX(tile: Tile): number {
    return this.tileAccordion.getVisibleGridX(tile);
  }

  override getVisibleGridY(tile: Tile): number {
    return this.tileAccordion.getVisibleGridY(tile);
  }

  override scrollTo(tile: Tile) {
    let group = this.tileAccordion.getGroupByTile(tile);
    group.body.scrollTo(tile);
  }

  override getTileGridByRow(rowIndex: number): TileGrid {
    let group = this.tileAccordion.getGroupByVisibleRow(rowIndex);
    if (group) {
      return group.body;
    }
    return null;
  }

  override findVisibleTileIndexAt(x: number, y: number, startIndex?: number, reverse?: boolean): number {
    return this.tileAccordion.findVisibleTileIndexAt(x, y, startIndex, reverse);
  }

  override isHorizontalGridActive(): boolean {
    if (this.tileAccordion.groups.length === 0) {
      return false;
    }
    return this.tileAccordion.groups[0].body.logicalGrid instanceof HorizontalGrid;
  }
}
