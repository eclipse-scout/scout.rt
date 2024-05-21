/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, ScoutKeyboardEvent, TileGrid, TileGridSelectionInstruction, TileGridSelectKeyStroke} from '../../index';

export class TileGridSelectDownKeyStroke extends TileGridSelectKeyStroke {

  constructor(tileGrid: TileGrid) {
    super(tileGrid);
    this.stopPropagation = true;
    this.repeatable = true;
    this.which = [keys.DOWN];
    this.renderingHints.text = '↓';
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }
    if (!(this.getSelectionHandler().isHorizontalGridActive())) {
      return false;
    }
    return true;
  }

  protected override _computeNewSelection(extend: boolean): TileGridSelectionInstruction {
    let focusedTile = this.getSelectionHandler().computeFocusedTile();
    return this.getSelectionHandler().computeSelectionY(focusedTile ? focusedTile.gridData.h : 1, extend);
  }
}
