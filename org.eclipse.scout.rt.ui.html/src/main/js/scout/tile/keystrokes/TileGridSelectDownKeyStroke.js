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
scout.TileGridSelectDownKeyStroke = function(tileGrid) {
  scout.TileGridSelectDownKeyStroke.parent.call(this, tileGrid);
  this.stopPropagation = true;
  this.repeatable = true;
  this.which = [scout.keys.DOWN];
  this.renderingHints.text = '↓';
};
scout.inherits(scout.TileGridSelectDownKeyStroke, scout.TileGridSelectKeyStroke);

scout.TileGridSelectDownKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TileGridSelectDownKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }
  if (!(this.getSelectionHandler().isHorizontalGridActive())) {
    return false;
  }
  return true;
};

scout.TileGridSelectDownKeyStroke.prototype._computeNewSelection = function(extend) {
  return this.getSelectionHandler().computeSelectionY(1, extend);
};
