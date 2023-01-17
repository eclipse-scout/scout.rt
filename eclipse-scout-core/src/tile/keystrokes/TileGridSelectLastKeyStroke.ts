/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, ScoutKeyboardEvent, TileGrid, TileGridSelectionInstruction, TileGridSelectKeyStroke} from '../../index';

export class TileGridSelectLastKeyStroke extends TileGridSelectKeyStroke {

  constructor(tileGrid: TileGrid) {
    super(tileGrid);
    this.stopPropagation = true;
    this.which = [keys.END];
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

  protected override _computeNewSelection(extend?: boolean): TileGridSelectionInstruction {
    return this.getSelectionHandler().computeSelectionToLast(extend);
  }
}
