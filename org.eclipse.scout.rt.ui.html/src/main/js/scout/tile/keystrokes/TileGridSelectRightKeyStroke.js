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
scout.TileGridSelectRightKeyStroke = function(tileGrid) {
  scout.TileGridSelectRightKeyStroke.parent.call(this, tileGrid);
  this.stopPropagation = true;
  this.repeatable = true;
  this.which = [scout.keys.RIGHT];
  this.renderingHints.text = '→';
};
scout.inherits(scout.TileGridSelectRightKeyStroke, scout.TileGridSelectKeyStroke);

scout.TileGridSelectRightKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TileGridSelectRightKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }
  if (!(this.getSelectionHandler().isHorizontalGridActive())) {
    return false;
  }
  return true;
};

scout.TileGridSelectRightKeyStroke.prototype._computeNewSelection = function(extend) {
  return this.getSelectionHandler().computeSelectionX(1, extend);
};
