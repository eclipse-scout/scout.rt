/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TilesAccordionSelectionHandler = function(tilesAccordion) {
  scout.TilesAccordionSelectionHandler.parent.call(this, tilesAccordion);
  // The difference to the main selectionHandler is that this one works on the TilesAccordion rather than on the Tiles
};
scout.inherits(scout.TilesAccordionSelectionHandler, scout.TilesSelectionHandler);

/**
 * @override
 */
scout.TilesAccordionSelectionHandler.prototype.getSelectedTiles = function(event) {
  return this.tiles.getSelectedTiles();
};
