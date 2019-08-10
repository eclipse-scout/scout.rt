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
scout.TileGridSelectKeyStroke = function(tileGrid) {
  scout.TileGridSelectKeyStroke.parent.call(this);
  this.field = tileGrid;
  this.shift = !tileGrid.multiSelect ? false : undefined;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var result = this._computeNewSelection();
    if (result && result.focusedTile) {
      return result.focusedTile.$container;
    }
  }.bind(this);
};
scout.inherits(scout.TileGridSelectKeyStroke, scout.KeyStroke);

/**
 * Selection handler should be used for every interaction with the tileGrid.
 * This is necessary to provide the same selection behavior for the tile accordion which uses multiple tile grids
 */
scout.TileGridSelectKeyStroke.prototype.getSelectionHandler = function() {
  // Not stored as member variable by purpose because it will be exchanged later by the tile accordion
  return this.field.selectionHandler;
};

scout.TileGridSelectKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TileGridSelectKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }
  if (!this.getSelectionHandler().isSelectable()) {
    return false;
  }
  if (this.getSelectionHandler().getFilteredTileCount() === 0) {
    return false;
  }
  return true;
};

scout.TileGridSelectKeyStroke.prototype.handle = function(event) {
  this.getSelectionHandler().executeSelection(this._computeNewSelection(event.shiftKey));
};

scout.TileGridSelectKeyStroke.prototype._computeNewSelection = function(extend) {
  // To be implemented by subclasses
};
