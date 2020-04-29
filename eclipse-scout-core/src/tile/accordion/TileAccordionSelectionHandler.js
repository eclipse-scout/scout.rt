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
import {HorizontalGrid, TileGridSelectionHandler} from '../../index';

export default class TileAccordionSelectionHandler extends TileGridSelectionHandler {

  constructor(tileAccordion) {
    super(tileAccordion);
    // The difference to the main selectionHandler is that this one works on the TileAccordion rather than on the TileGrid
    this.tileAccordion = this.tileGrid;
  }

  /**
   * @override
   */
  getFilteredTiles() {
    return this.tileAccordion.getFilteredTiles();
  }

  /**
   * @override
   */
  getFilteredTileCount() {
    return this.tileAccordion.getFilteredTileCount();
  }

  /**
   * @override
   */
  getVisibleTiles() {
    return this.tileAccordion.getVisibleTiles();
  }

  /**
   * @override
   */
  getVisibleTileCount() {
    return this.tileAccordion.getVisibleTileCount();
  }

  /**
   * @override
   */
  getSelectedTiles(event) {
    return this.tileAccordion.getSelectedTiles();
  }

  /**
   * @override
   */
  getFocusedTile() {
    return this.tileAccordion.getFocusedTile();
  }

  /**
   * @override
   */
  getVisibleGridRowCount() {
    return this.tileAccordion.getVisibleGridRowCount();
  }

  /**
   * @override
   */
  getVisibleGridX(tile) {
    return this.tileAccordion.getVisibleGridX(tile);
  }

  /**
   * @override
   */
  getVisibleGridY(tile) {
    return this.tileAccordion.getVisibleGridY(tile);
  }

  /**
   * @override
   */
  scrollTo(tile) {
    let group = this.tileAccordion.getGroupByTile(tile);
    group.body.scrollTo(tile);
  }

  /**
   * @override
   */
  getTileGridByRow(rowIndex) {
    let group = this.tileAccordion.getGroupByVisibleRow(rowIndex);
    if (group) {
      return group.body;
    }
    return null;
  }

  /**
   * @override
   */
  findVisibleTileIndexAt(x, y, startIndex, reverse) {
    return this.tileAccordion.findVisibleTileIndexAt(x, y, startIndex, reverse);
  }

  /**
   * @override
   */
  isHorizontalGridActive() {
    if (this.tileAccordion.groups.length === 0) {
      return false;
    }
    return this.tileAccordion.groups[0].body.logicalGrid instanceof HorizontalGrid;
  }
}
