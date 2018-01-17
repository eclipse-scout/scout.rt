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
scout.TileGridSelectLeftKeyStroke = function(tileGrid) {
  scout.TileGridSelectLeftKeyStroke.parent.call(this, tileGrid);
  this.stopPropagation = true;
  this.repeatable = true;
  this.which = [scout.keys.LEFT];
  this.renderingHints.text = '←';
};
scout.inherits(scout.TileGridSelectLeftKeyStroke, scout.TileGridSelectKeyStroke);

scout.TileGridSelectLeftKeyStroke.prototype._computeNewSelection = function(extend) {
  return this.getSelectionHandler().computeSelectionX(-1, extend);
};
