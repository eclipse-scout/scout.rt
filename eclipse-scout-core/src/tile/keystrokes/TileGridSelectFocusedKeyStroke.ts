/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, ScoutKeyboardEvent, TileGrid, TileGridSelectKeyStroke} from '../..';

export class TileGridSelectFocusedKeyStroke extends TileGridSelectKeyStroke {

  constructor(tileGrid: TileGrid) {
    super(tileGrid);
    this.which = [keys.SPACE, keys.ENTER];
    this.renderingHints.$drawingArea = ($drawingArea: JQuery, event: ScoutKeyboardEvent) => {
      return this.field.focusedTile?.$container;
    };
    this.stopPropagation = true;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }
    let focusedTile = this.getSelectionHandler().getFocusedTile();
    return focusedTile && !this.getSelectionHandler().getGridOfFocusedTile().isTileSelected(focusedTile);
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.getSelectionHandler().selectTile(this.getSelectionHandler().getFocusedTile());
  }
}
