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
scout.TileGridSelectLastKeyStroke = function(tileGrid) {
  scout.TileGridSelectLastKeyStroke.parent.call(this, tileGrid);
  this.stopPropagation = true;
  this.which = [scout.keys.END];
};
scout.inherits(scout.TileGridSelectLastKeyStroke, scout.TileGridSelectKeyStroke);

scout.TileGridSelectLastKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TileGridSelectLastKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }
  if (!(this.getSelectionHandler().isHorizontalGridActive())) {
    return false;
  }
  return true;
};

scout.TileGridSelectLastKeyStroke.prototype._computeNewSelection = function(extend) {
  return this.getSelectionHandler().computeSelectionToLast(extend);
};
