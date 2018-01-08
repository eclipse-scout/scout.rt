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
scout.TileGridSelectKeyStroke = function(tileGrid) {
  scout.TileGridSelectKeyStroke.parent.call(this);
  this.field = tileGrid;
};
scout.inherits(scout.TileGridSelectKeyStroke, scout.KeyStroke);

scout.TileGridSelectKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TileGridSelectKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }
  if (!this.field.selectable) {
    return false;
  }
  if (this.field.filteredTiles.length === 0) {
    return false;
  }
  return true;
};
