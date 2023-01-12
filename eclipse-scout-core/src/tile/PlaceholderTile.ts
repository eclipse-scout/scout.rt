/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Tile} from '../index';

export class PlaceholderTile extends Tile {

  constructor() {
    super();
    this.cssClass = 'placeholder-tile';
    this.displayStyle = Tile.DisplayStyle.PLAIN;
  }

  protected override _setSelectable(selectable: boolean) {
    // Placeholder tiles should not be selectable
    super._setSelectable(false);
  }
}
