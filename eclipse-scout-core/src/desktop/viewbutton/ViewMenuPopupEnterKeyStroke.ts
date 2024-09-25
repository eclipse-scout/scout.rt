/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {keys, KeyStroke, ScoutKeyboardEvent, ViewMenuPopup} from '../../index';

export class ViewMenuPopupEnterKeyStroke extends KeyStroke {
  declare field: ViewMenuPopup;

  constructor(popup: ViewMenuPopup) {
    super();
    this.field = popup;
    this.which = [keys.ENTER, keys.SPACE];
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      let tile = this.field.content.selectedTiles[0];
      if (tile && tile.rendered) {
        return tile.$container;
      }
    };
    this.inheritAccessibility = false;
  }

  override accept(event: ScoutKeyboardEvent): boolean {
    return super.accept(event) && this.field.content.selectedTiles.length === 1 && this.field.content.selectedTiles[0].enabledComputed;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.activateTile(this.field.content.selectedTiles[0]);
  }
}
