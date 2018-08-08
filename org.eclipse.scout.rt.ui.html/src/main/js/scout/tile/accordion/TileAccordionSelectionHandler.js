/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TileAccordionSelectionHandler = function(tileAccordion) {
  scout.TileAccordionSelectionHandler.parent.call(this, tileAccordion);
  // The difference to the main selectionHandler is that this one works on the TileAccordion rather than on the TileGrid
  this.tileAccordion = this.tileGrid;
};
scout.inherits(scout.TileAccordionSelectionHandler, scout.TileGridSelectionHandler);

/**
 * @override
 */
scout.TileAccordionSelectionHandler.prototype.getFilteredTiles = function() {
  return this.tileAccordion.getFilteredTiles();
};

/**
 * @override
 */
scout.TileAccordionSelectionHandler.prototype.getFilteredTileCount = function() {
  return this.tileAccordion.getFilteredTileCount();
};

/**
 * @override
 */
scout.TileAccordionSelectionHandler.prototype.getVisibleTiles = function() {
  return this.tileAccordion.getVisibleTiles();
};

/**
 * @override
 */
scout.TileAccordionSelectionHandler.prototype.getVisibleTileCount = function() {
  return this.tileAccordion.getVisibleTileCount();
};

/**
 * @override
 */
scout.TileAccordionSelectionHandler.prototype.getSelectedTiles = function(event) {
  return this.tileAccordion.getSelectedTiles();
};

/**
 * @override
 */
scout.TileAccordionSelectionHandler.prototype.getFocusedTile = function() {
  return this.tileAccordion.getFocusedTile();
};

/**
 * @override
 */
scout.TileAccordionSelectionHandler.prototype.getVisibleGridRowCount = function() {
  return this.tileAccordion.getVisibleGridRowCount();
};

/**
 * @override
 */
scout.TileAccordionSelectionHandler.prototype.getVisibleGridX = function(tile) {
  return this.tileAccordion.getVisibleGridX(tile);
};

/**
 * @override
 */
scout.TileAccordionSelectionHandler.prototype.getVisibleGridY = function(tile) {
  return this.tileAccordion.getVisibleGridY(tile);
};

/**
 * @override
 */
scout.TileAccordionSelectionHandler.prototype.scrollTo = function(tile) {
  var group = this.tileAccordion.getGroupByTile(tile);
  group.body.scrollTo(tile);
};

/**
 * @override
 */
scout.TileAccordionSelectionHandler.prototype.getTileGridByRow = function(rowIndex) {
  var group = this.tileAccordion.getGroupByVisibleRow(rowIndex);
  if (group) {
    return group.body;
  }
  return null;
};

/**
 * @override
 */
scout.TileAccordionSelectionHandler.prototype.findVisibleTileIndexAt = function(x, y, startIndex, reverse) {
  return this.tileAccordion.findVisibleTileIndexAt(x, y, startIndex, reverse);
};

/**
 * @override
 */
scout.TileAccordionSelectionHandler.prototype.isHorizontalGridActive = function() {
  if (this.tileAccordion.groups.length === 0) {
    return false;
  }
  return this.tileAccordion.groups[0].body.logicalGrid instanceof scout.HorizontalGrid;
};
