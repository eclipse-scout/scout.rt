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
import {KeyStroke} from '../../index';

export default class TileGridSelectKeyStroke extends KeyStroke {

  constructor(tileGrid) {
    super();
    this.field = tileGrid;
    this.shift = !tileGrid.multiSelect ? false : undefined;
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      let result = this._computeNewSelection();
      if (result && result.focusedTile) {
        return result.focusedTile.$container;
      }
    };
    this.inheritAccessibility = false;
  }

  /**
   * Selection handler should be used for every interaction with the tileGrid.
   * This is necessary to provide the same selection behavior for the tile accordion which uses multiple tile grids
   */
  getSelectionHandler() {
    // Not stored as member variable by purpose because it will be exchanged later by the tile accordion
    return this.field.selectionHandler;
  }

  _accept(event) {
    let accepted = super._accept(event);
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
  }

  handle(event) {
    this.getSelectionHandler().executeSelection(this._computeNewSelection(event.shiftKey));
  }

  _computeNewSelection(extend) {
    // To be implemented by subclasses
  }
}
