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
scout.TileGridSelectFirstKeyStroke = function(tileGrid) {
  scout.TileGridSelectFirstKeyStroke.parent.call(this, tileGrid);
  this.stopPropagation = true;
  this.which = [scout.keys.HOME];
};
scout.inherits(scout.TileGridSelectFirstKeyStroke, scout.TileGridSelectKeyStroke);

scout.TileGridSelectFirstKeyStroke.prototype._computeNewSelection = function(extend) {
  return this.getSelectionHandler().computeSelectionToFirst(extend);
};
