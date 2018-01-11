/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TileGridSelectAllKeyStroke = function(tileGrid) {
  scout.TileGridSelectAllKeyStroke.parent.call(this, tileGrid);
  this.ctrl = true;
  this.shift = false;
  this.which = [scout.keys.A];
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var tile = this.field.filteredTiles[0];
    if (tile) {
      // Draw in first tile so that other key stroke hints (e.g. left, right etc.) don't overlap this one
      return tile.$container;
    }
  }.bind(this);
};
scout.inherits(scout.TileGridSelectAllKeyStroke, scout.TileGridSelectKeyStroke);

scout.TileGridSelectAllKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TileGridSelectAllKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }
  if (!this.field.multiSelect) {
    return false;
  }
  return true;
};

scout.TileGridSelectAllKeyStroke.prototype.handle = function(event) {
  this.field.toggleSelection();
};
