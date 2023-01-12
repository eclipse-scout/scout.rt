/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {KeyStroke, ScoutKeyboardEvent, TileGrid, TileGridSelectionHandler, TileGridSelectionInstruction} from '../../index';

export class TileGridSelectKeyStroke extends KeyStroke {
  declare field: TileGrid;

  constructor(tileGrid: TileGrid) {
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
  getSelectionHandler(): TileGridSelectionHandler {
    // Not stored as member variable by purpose because it will be exchanged later by the tile accordion
    return this.field.selectionHandler;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
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

  override handle(event: JQuery.KeyboardEventBase) {
    this.getSelectionHandler().executeSelection(this._computeNewSelection(event.shiftKey));
  }

  protected _computeNewSelection(extend?: boolean): TileGridSelectionInstruction {
    // To be implemented by subclasses
    return null;
  }
}
