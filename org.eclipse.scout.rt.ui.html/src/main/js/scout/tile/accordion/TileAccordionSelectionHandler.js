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
scout.TileAccordionSelectionHandler = function(tileAccordion) {
  scout.TileAccordionSelectionHandler.parent.call(this, tileAccordion);
  // The difference to the main selectionHandler is that this one works on the TileAccordion rather than on the TileGrid
};
scout.inherits(scout.TileAccordionSelectionHandler, scout.TileGridSelectionHandler);

/**
 * @override
 */
scout.TileAccordionSelectionHandler.prototype.getSelectedTiles = function(event) {
  return this.tileGrid.getSelectedTiles();
};
